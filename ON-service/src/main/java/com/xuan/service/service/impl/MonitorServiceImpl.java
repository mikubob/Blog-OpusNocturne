package com.xuan.service.service.impl;

import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.core.util.NumberUtil;
import com.xuan.common.utils.ServerMonitorUtils;
import com.xuan.entity.vo.monitor.ServerMonitorVO;
import com.xuan.service.service.IMonitorService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 服务器监控服务实现类
 *
 * <h3>设计思路概述</h3>
 * <p>
 * 本实现采用 <b>「缓存 + 定时刷新」</b> 模式，而非每次请求时实时读取硬件数据。
 * 其核心原因在于：直接读取 CPU / 内存等硬件信息是 <b>阻塞式 I/O 操作</b>（尤其是 CPU
 * 使用率计算必须等待一个采样间隔），若在 HTTP 请求链路中同步调用，将严重拖慢接口响应，
 * 甚至导致线程池堆积。
 * </p>
 *
 * <h3>并发安全保障</h3>
 * <ul>
 * <li>对外暴露的 {@link #getServerInfo()} 方法通过 {@link AtomicReference#get()} 读取缓存，
 * 属于无锁操作，读取性能极高（O(1) 时间复杂度）。</li>
 * <li>缓存的更新由 {@link #updateMonitorInfo()} 定时任务完成，写操作通过
 * {@link AtomicReference#set(Object)} 原子替换，保证引用可见性，不存在"脏读"。</li>
 * <li>{@code prevTicks}（上一帧 CPU 滴答）仅在单线程（Spring 定时任务调度线程 / 启动线程）
 * 中被读写，所以无需额外加锁。</li>
 * </ul>
 *
 * <h3>依赖说明</h3>
 * <ul>
 * <li><b>OSHI</b>（Operating System and Hardware Information）：跨平台本地系统信息库，
 * 支持 Windows / Linux / macOS，无需额外安装 Agent。</li>
 * <li><b>Hutool</b>：工具集，这里用于数据大小格式化（{@link DataSizeUtil}）和数值保留精度（{@link NumberUtil}）。</li>
 * </ul>
 *
 * @see IMonitorService
 * @see ServerMonitorVO
 */
@Slf4j
@Service
public class MonitorServiceImpl implements IMonitorService {

    /**
     * 核心缓存：保存最新一次收集的服务器监控快照。
     *
     * <p>
     * <b>为什么使用 AtomicReference？</b><br>
     * 在多线程环境下，对普通对象引用的赋值操作并不保证对其他线程的立即可见性（happens-before）。
     * {@link AtomicReference} 内部通过 {@code volatile} 语义，确保任意线程在调用 {@code get()} 后
     * 都能读取到由定时任务写入的最新值，且整个替换操作是原子的，不会出现中间状态。
     * </p>
     */
    private final AtomicReference<ServerMonitorVO> cache = new AtomicReference<>();

    // ======================== 全局复用的 OSHI 实例 ========================
    // 【设计原因】：OSHI 初始化 SystemInfo 时会调用底层 native 库加载系统信息，
    // 这是一次性且相对耗时的操作，因此将其作为成员变量单例持有，在整个应用生命周期内复用，
    // 避免每次采集数据时重复初始化带来的性能损耗。

    /** OSHI 顶层入口，用于获取硬件层（HAL）和操作系统层（OS）句柄 */
    private final SystemInfo si = new SystemInfo();

    /**
     * 硬件抽象层（Hardware Abstraction Layer）：
     * 提供对 CPU、内存、磁盘、网络等物理硬件的统一访问接口，屏蔽底层平台差异。
     */
    private final HardwareAbstractionLayer hal = si.getHardware();

    /**
     * 操作系统信息句柄：
     * 提供对进程列表、系统运行时长（uptime）、文件系统等 OS 级别数据的访问能力。
     */
    private final OperatingSystem os = si.getOperatingSystem();

    /**
     * 上一次采集周期的 CPU 时间滴答（Ticks）数组快照。
     *
     * <h4>什么是 CPU Ticks？</h4>
     * <p>
     * 现代操作系统将 CPU 时间划分为若干类别（如：用户态、内核态、IO 等待、空闲等），
     * 并用累计计数器（称为"滴答"或"jiffies"）来统计各类别的耗时。该数值随系统运行单调递增。
     * </p>
     *
     * <h4>为什么需要保存上一次的值？</h4>
     * <p>
     * CPU 使用率 = (某段时间内的非空闲滴答总数) / (该段时间内的所有滴答总数)。
     * 单次快照的绝对值没有意义，必须通过两次快照的 <b>差值</b> 才能计算出这段时间内的 CPU 占用率。
     * 例如：如果在 T1 时刻 idle 滴答为 1000，在 T2 时刻（2秒后）为 1100，则：
     * 
     * <pre>
     *   idle_delta = 1100 - 1000 = 100
     *   total_delta = (所有类型增量之和) = 200
     *   usage = (200 - 100) / 200 = 50%
     * </pre>
     * </p>
     */
    private long[] prevTicks = hal.getProcessor().getSystemCpuLoadTicks();

    /**
     * Spring 容器完成 Bean 属性注入后自动执行的预热方法（由 {@link PostConstruct} 触发）。
     *
     * <h4>预热的必要性</h4>
     * <p>
     * 如不进行预热，那么在应用启动后、第一次定时任务触发前（最长 2 秒）这段时间内，
     * 前端若立即请求监控数据，{@code cache} 中的值为 {@code null}，接口将返回空对象。
     * 预热将此等待时间缩短为 1 秒，并在容器就绪时便填充好有效数据。
     * </p>
     *
     * <h4>为什么要先 sleep 1 秒？</h4>
     * <p>
     * CPU 使用率的计算依赖两次采样的差值。若 {@code prevTicks} 与当前获取的 ticks
     * 时间间隔太短（例如毫秒级），差值几乎为零，计算结果会严重失真（接近 0% 或 100%）。
     * sleep 1 秒可以确保采样区间足够长，使第一次计算也能得到合理准确的 CPU 使用率。
     * </p>
     */
    @PostConstruct
    public void init() {
        log.info("开始预热服务器监控缓存...");
        try {
            // 等待约 1 秒以获得有意义的 CPU 采样时间差
            Thread.sleep(1000);

            // 手动触发一次完整的数据采集，确保缓存在容器就绪时便已有效
            updateMonitorInfo();
            log.info("服务器监控缓存预热完成！");
        } catch (InterruptedException e) {
            log.warn("监控预热被中断", e);
            // 重要：捕获 InterruptedException 后必须恢复中断状态，
            // 否则外部调用者（如 Spring 容器关闭钩子）无法感知到线程被中断这一事件。
            Thread.currentThread().interrupt();// 恢复中断状态
        }
    }

    /**
     * 定时数据采集任务：每 2000 毫秒（2 秒）在 Spring 调度线程池中自动执行一次。
     *
     * <h4>工作原理</h4>
     * <ol>
     * <li>分别调用 {@link #getCpuInfo}、{@link #getMemoryInfo}、{@link #getSystemInfo}
     * 采集各维度数据。</li>
     * <li>将三部分数据组装成 {@link ServerMonitorVO} 对象。</li>
     * <li>通过 {@link AtomicReference#set(Object)} 原子地替换旧缓存，确保读线程始终拿到完整一致的新对象。</li>
     * </ol>
     *
     * <h4>异常处理策略</h4>
     * <p>
     * 由于定时任务一旦抛出未捕获的异常，Spring 调度器通常会终止该任务的后续执行，
     * 因此这里用 try-catch 兜底，仅记录日志而不重新抛出，保证任务持续运行。
     * </p>
     *
     * <p>
     * <b>注意</b>：该方法也在 {@link #init()} 中被手动调用一次，此时运行在启动线程中，
     * 而非调度线程，但因 {@code prevTicks} 的访问是串行的，不存在并发问题。
     * </p>
     */
    @Scheduled(fixedRate = 2000)
    public void updateMonitorInfo() {
        try {
            ServerMonitorVO vo = ServerMonitorVO.builder()
                    .cpu(getCpuInfo(hal.getProcessor()))
                    .memory(getMemoryInfo(hal.getMemory()))
                    .system(getSystemInfo(os))
                    .build();
            // 原子替换引用：该操作不会让读线程看到"一半新一半旧"的混合数据，
            // 读线程要么拿到旧的完整对象，要么拿到新的完整对象
            cache.set(vo);
        } catch (Exception e) {
            log.error("定时更新监控信息失败", e);
        }
    }

    /**
     * 对外暴露的服务接口：获取最新的服务器监控快照。
     *
     * <h4>性能特性</h4>
     * <ul>
     * <li>时间复杂度：O(1)——仅一次 volatile 读操作，无任何 I/O 或锁竞争。</li>
     * <li>完全非阻塞：不会因硬件采集耗时而阻塞 HTTP 请求处理线程。</li>
     * </ul>
     *
     * <h4>兜底逻辑</h4>
     * <p>
     * 理论上经过 {@link #init()} 预热后，缓存不会为 {@code null}。
     * 但在极端情况下（如：预热被中断、应用关闭过程中仍有请求进入），
     * 返回一个空的 Builder 对象（非 null）可以防止上层 Controller 发生 NPE，
     * 同时也避免了在 HTTP 请求链路中临时发起实时的硬件查询。
     * </p>
     *
     * @return 最新的 {@link ServerMonitorVO} 监控数据对象，不会为 {@code null}
     */
    @Override
    public ServerMonitorVO getServerInfo() {
        ServerMonitorVO vo = cache.get();
        if (vo == null) {
            // 兜底：返回空壳对象，内部所有字段均为 null，前端应做空值保护
            return ServerMonitorVO.builder().build();
        }
        return vo;
    }

    // ======================== 私有采集方法 ========================

    /**
     * 采集并计算 CPU 核心监控指标。
     *
     * <h4>CPU 使用率计算算法详解</h4>
     * <p>
     * {@link CentralProcessor#getSystemCpuLoadTicks()} 返回一个 long 数组，索引对应
     * {@link CentralProcessor.TickType} 枚举中的各 CPU 时间类型：
     * </p>
     * <table border="1">
     * <tr>
     * <th>索引</th>
     * <th>TickType</th>
     * <th>含义</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>CPU_CLK_UNHALTED</td>
     * <td>（部分平台）</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>NICE</td>
     * <td>低优先级用户进程时间</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>SOFTIRQ</td>
     * <td>软中断时间</td>
     * </tr>
     * <tr>
     * <td>3</td>
     * <td>IRQ</td>
     * <td>硬件中断时间</td>
     * </tr>
     * <tr>
     * <td>4</td>
     * <td>SYSTEM</td>
     * <td>内核态时间</td>
     * </tr>
     * <tr>
     * <td>5</td>
     * <td>USER</td>
     * <td>用户态程序时间</td>
     * </tr>
     * <tr>
     * <td>6</td>
     * <td>IOWAIT</td>
     * <td>等待 I/O 的时间（Linux）</td>
     * </tr>
     * <tr>
     * <td>7</td>
     * <td>IDLE</td>
     * <td>CPU 空闲时间</td>
     * </tr>
     * <tr>
     * <td>8</td>
     * <td>STEAL</td>
     * <td>被虚拟机管理程序占用的时间</td>
     * </tr>
     * </table>
     *
     * <p>
     * 计算公式：
     * </p>
     * 
     * <pre>
     *   totalDelta = Σ(所有类型的 ticksDelta)
     *   usedDelta  = totalDelta - idleDelta
     *   usage(%)   = usedDelta / totalDelta × 100
     * </pre>
     *
     * <h4>prevTicks 更新时机</h4>
     * <p>
     * 每次调用结束前将当前 ticks 赋值给 {@code prevTicks}，供下一次定时任务使用，
     * 形成"滚动窗口"式的差值采样。
     * </p>
     *
     * @param processor OSHI 的 CPU 处理器对象，提供底层 tick 数据
     * @return 包含 CPU 型号、物理封装数、逻辑核心数、使用率的 {@link ServerMonitorVO.CpuVO}
     */
    private ServerMonitorVO.CpuVO getCpuInfo(CentralProcessor processor) {
        // ---- Step 1：获取当前帧的 CPU 滴答快照 ----
        long[] ticks = processor.getSystemCpuLoadTicks();

        // ---- Step 2：计算各 CPU 时间类型与上次快照的增量（差值）----
        // 差值代表在两次采样间隔内该类型 CPU 时间的消耗量
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()]
                - prevTicks[CentralProcessor.TickType.NICE.getIndex()];// 低优先级用户进程时间
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()]
                - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];// 硬件中断时间
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()]
                - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];// 软中断时间
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()]
                - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];// 被虚拟机管理程序占用的时间
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()]
                - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];// 内核态时间
        long user = ticks[CentralProcessor.TickType.USER.getIndex()]
                - prevTicks[CentralProcessor.TickType.USER.getIndex()];// 用户态程序时间
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()]
                - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];// 等待 I/O 的时间（Linux）
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()]
                - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];// CPU 空闲时间

        // ---- Step 3：刷新基准快照，供下一个采样周期使用 ----
        this.prevTicks = ticks;

        // ---- Step 4：计算总滴答数 ----
        // totalCpu 为本周期内 CPU 在所有状态的总"时钟单位"消耗总和
        long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;

        double usage = 0.0;
        if (totalCpu > 0) {
            // totalCpu - idle = 实际"工作"时间（用户态 + 内核态 + IRQ 等）
            usage = 100d * (totalCpu - idle) / totalCpu;
            // 边界保护：防止浮点运算精度问题导致结果超出 [0, 100] 区间
            // Math.max(0, ...) 防止出现负数（极低概率，如初次采样时差值为负）
            // Math.min(100, ...) 防止出现大于 100% 的非法值
            usage = Math.max(0, Math.min(100, usage));
        }

        return ServerMonitorVO.CpuVO.builder()
                .name(processor.getProcessorIdentifier().getName()) // 如 "Intel(R) Core(TM) i9-13900K"
                .packages(processor.getPhysicalPackageCount()) // 物理 CPU 数量（路数），通常服务器为 2
                .cores(processor.getLogicalProcessorCount()) // 逻辑核心数（含超线程），前端展示此值
                .usage(NumberUtil.round(usage, 2).doubleValue()) // 保留两位小数，避免前端显示过多无效精度
                .build();
    }

    /**
     * 采集内存（RAM）使用情况。
     *
     * <h4>内存数据来源</h4>
     * <p>
     * OSHI 通过读取 {@code /proc/meminfo}（Linux）或 Windows Memory API 获取物理内存信息。
     * {@link GlobalMemory#getAvailable()} 返回的是系统对应用程序"实际可用"的内存
     * （含 OS 缓存/缓冲区可被回收的部分），比 Java 层面的 {@code Runtime.maxMemory()} 更贴近实际。
     * </p>
     *
     * <h4>边界保护说明</h4>
     * <p>
     * 在部分老旧 Linux 内核或 ARM 嵌入式设备上，存在 {@code available > total} 或
     * {@code available < 0} 的 Bug，此处做双向截断，确保数据合理性。
     * </p>
     *
     * @param memory OSHI 的全局内存对象
     * @return 包含总内存、已用内存、可用内存（人类可读格式）及使用率的 {@link ServerMonitorVO.MemoryVO}
     */
    private ServerMonitorVO.MemoryVO getMemoryInfo(GlobalMemory memory) {
        long total = memory.getTotal(); // 物理内存总量（字节）
        long available = memory.getAvailable(); // 系统当前可用内存（字节）

        // 边界保护：校正异常值，防止后续计算出现负数或超范围结果
        if (available < 0)
            available = 0;
        if (available > total)
            available = total;

        long used = total - available; // 已用内存 = 总内存 - 可用内存
        // 使用率：若 total 为 0（极端异常情况），则返回 0.0，防止 ArithmeticException
        double usage = (total > 0) ? (100d * used / total) : 0.0;

        return ServerMonitorVO.MemoryVO.builder()
                // DataSizeUtil.format() 将字节数自动转换为人类可读字符串（如 "16.00 GB"）
                .total(DataSizeUtil.format(total))
                .used(DataSizeUtil.format(used))
                .free(DataSizeUtil.format(available))
                .usage(NumberUtil.round(usage, 2).doubleValue())
                .build();
    }

    /**
     * 采集操作系统基础信息。
     *
     * <h4>数据来源</h4>
     * <ul>
     * <li>{@code os.name}、{@code os.arch}：来自 JVM
     * 系统属性（{@link System#getProperties()}），
     * 值由 JVM 在启动时从底层 OS 读取并缓存，例如 "Linux"、"amd64"。</li>
     * <li>系统运行时长（uptime）：来自 OSHI 的 {@link OperatingSystem#getSystemUptime()}，
     * 返回自系统启动以来的秒数，经 {@link #formatUptime(long)} 转换为人类可读格式。</li>
     * </ul>
     *
     * @param os OSHI 的操作系统信息对象
     * @return 包含 OS 名称、CPU 架构、系统运行时长的 {@link ServerMonitorVO.SystemVO}
     */
    private ServerMonitorVO.SystemVO getSystemInfo(OperatingSystem os) {
        Properties props = System.getProperties();
        long uptimeSeconds = os.getSystemUptime(); // 系统运行时长（秒）

        return ServerMonitorVO.SystemVO.builder()
                .os(props.getProperty("os.name")) // JVM 系统属性，如 "Windows 11" / "Linux"
                .arch(props.getProperty("os.arch")) // CPU 指令集架构，如 "amd64" / "aarch64"
                .uptime(ServerMonitorUtils.formatUptime(uptimeSeconds)) // 格式化后的运行时长字符串（委托工具类处理）
                .build();
    }

}