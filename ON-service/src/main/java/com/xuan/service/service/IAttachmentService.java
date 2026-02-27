package com.xuan.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xuan.entity.dto.system.AttachmentPageQueryDTO;
import com.xuan.entity.po.interact.Attachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 附件服务接口
 *
 * @author 玄〤
 * @since 2026-02-20
 */
public interface IAttachmentService extends IService<Attachment> {

    /**
     * 分页查询附件列表
     *
     * @param dto 分页查询参数（文件名模糊匹配、文件类型筛选）
     * @return 附件分页结果
     */
    Page<Attachment> pageAttachment(AttachmentPageQueryDTO dto);

    /**
     * 删除附件（删除数据库记录）
     *
     * @param id 附件ID
     */
    void deleteAttachment(Long id);

    /**
     * 批量删除附件
     * 先批量查询校验存在性，再一次性删除，避免 N 次单条 SQL
     *
     * @param ids 附件ID列表
     */
    void batchDeleteAttachment(List<Long> ids);

    /**
     * 上传文件并保存附件记录
     *
     * @param file    上传的文件
     * @param bizType 业务类型（如 article / avatar），可为 null
     * @param bizId   关联业务ID，可为 null
     * @return 已保存的附件记录（含 fileUrl）
     */
    Attachment uploadAttachment(MultipartFile file, String bizType, Long bizId);
}