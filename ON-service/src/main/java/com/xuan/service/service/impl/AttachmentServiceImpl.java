package com.xuan.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuan.common.exceptions.BusinessException;
import com.xuan.common.utils.UploadUtils;
import com.xuan.entity.dto.system.AttachmentPageQueryDTO;
import com.xuan.entity.po.interact.Attachment;
import com.xuan.service.mapper.AttachmentMapper;
import com.xuan.service.service.IAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.xuan.common.enums.ErrorCode.FILE_NOT_FOUND;
import static com.xuan.common.enums.ErrorCode.FILE_PARTIAL_NOT_FOUND;

/**
 * 附件服务实现类
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl extends ServiceImpl<AttachmentMapper, Attachment> implements IAttachmentService {

    private final UploadUtils uploadUtils;

    /**
     * 分页查询附件列表
     * 支持按文件名（模糊）和文件类型（精确）筛选，结果按创建时间倒序排列
     *
     * @param dto 分页查询参数
     * @return 附件分页结果
     */
    @Override
    public Page<Attachment> pageAttachment(AttachmentPageQueryDTO dto) {
        LambdaQueryWrapper<Attachment> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getFileName())) {
            queryWrapper.like(Attachment::getFileName, dto.getFileName());
        }
        if (StringUtils.hasText(dto.getFileType())) {
            queryWrapper.eq(Attachment::getFileType, dto.getFileType());
        }
        queryWrapper.orderByDesc(Attachment::getCreateTime);
        return this.page(new Page<>(dto.getCurrent(), dto.getSize()), queryWrapper);
    }

    /**
     * 删除附件
     * 删除数据库记录；如后续接入对象存储，可在此处补充物理文件删除逻辑
     *
     * @param id 附件ID
     */
    @Override
    public void deleteAttachment(Long id) {
        Attachment attachment = this.getById(id);
        if (attachment == null) {
            throw new BusinessException("附件不存在");
        }
        // TODO: 若需删除物理文件，可在此处根据 attachment.getFilePath() 调用文件存储服务
        this.removeById(id);
    }

    /**
     * 批量删除附件
     * 使用 listByIds 一次性查出所有记录（校验存在性），再用 removeByIds 批量删除
     * 相比逐条删除，将 SQL 从 2N 次降低到 2 次
     *
     * @param ids 附件ID列表
     */
    @Override
    public void batchDeleteAttachment(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(FILE_NOT_FOUND);
        }
        // 1. 批量查询，校验是否存在
        List<Attachment> attachments = this.listByIds(ids);
        if (attachments.size() != ids.size()) {
            throw new BusinessException(FILE_PARTIAL_NOT_FOUND);
        }
        // 2. TODO: 若需批量删除物理文件，可遍历 attachments 调用文件存储服务
        // 3. 一次性批量删除数据库记录
        this.removeByIds(ids);
    }

    /**
     * 上传文件并保存附件记录
     *
     * @param file    上传的文件
     * @param bizType 业务类型，可为 null
     * @param bizId   关联业务ID，可为 null
     * @return 已保存的附件记录
     */
    @Override
    public Attachment uploadAttachment(MultipartFile file, String bizType, Long bizId) {
        // 上传文件到本地存储，返回访问 URL
        String fileUrl = uploadUtils.upload(file);
        // 反推物理存储路径（可用于后续删除物理文件）
        String filePath = uploadUtils.getStoragePath(fileUrl);
        // 推断 MIME 类型
        String fileType = uploadUtils.getContentType(file.getOriginalFilename());
        // 保存附件记录并返回
        return saveAttachment(
                file.getOriginalFilename(),
                fileUrl,
                filePath,
                fileType,
                file.getSize(),
                bizType,
                bizId);
    }

    /**
     * 保存附件记录（内部工具方法）
     *
     * @param fileName 文件名
     * @param fileUrl  文件访问URL
     * @param filePath 文件路径
     * @param fileType 文件类型
     * @param fileSize 文件大小
     * @param bizType  业务类型
     * @param bizId    业务ID
     * @return 附件记录
     */
    private Attachment saveAttachment(String fileName, String fileUrl, String filePath,
            String fileType, Long fileSize, String bizType, Long bizId) {
        Attachment attachment = new Attachment();
        attachment.setFileName(fileName);
        attachment.setFileUrl(fileUrl);
        attachment.setFilePath(filePath);
        attachment.setFileType(fileType);
        attachment.setFileSize(fileSize);
        attachment.setBizType(bizType);
        attachment.setBizId(bizId);
        this.save(attachment);
        return attachment;
    }
}