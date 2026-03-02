package com.xuan.service.service.impl;

import cn.hutool.core.util.StrUtil;
import com.xuan.common.service.INotificationService;
import com.xuan.service.service.ISysSettingService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 通知服务实现类
 *
 * @author 玄〤
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements INotificationService {

    private final JavaMailSender mailSender;
    private final ISysSettingService sysSettingService;

    /**
     * 发送友情链接申请通知
     * @param friendLinkName 友情链接名称
     * @param friendLinkUrl 友情链接URL
     * @param friendLinkEmail 友情链接邮箱
     */
    @Override
    public void sendFriendLinkApplyNotification(String friendLinkName, String friendLinkUrl, String friendLinkEmail) {
        try {
            // 获取管理员邮箱
            String adminEmail = sysSettingService.getAdminEmail();
            if (StrUtil.isBlank(adminEmail)) {
                log.warn("管理员邮箱未配置，无法发送友情链接申请通知");
                return;
            }

            // 创建邮件消息
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // 设置邮件内容
            helper.setTo(adminEmail);
            helper.setSubject("【OpusNocturne】新的友情链接申请通知");
            
            String content = "<html>" +
                    "<body>" +
                    "<h3>新的友情链接申请</h3>" +
                    "<p>网站名称: <strong>" + friendLinkName + "</strong></p>" +
                    "<p>网站地址: <a href=\"" + friendLinkUrl + "\">" + friendLinkUrl + "</a></p>" +
                    "<p>站长邮箱: " + (StrUtil.isNotBlank(friendLinkEmail) ? friendLinkEmail : "未提供") + "</p>" +
                    "<p>请登录后台管理系统进行审核。</p>" +
                    "</body>" +
                    "</html>";
            
            helper.setText(content, true);

            // 发送邮件
            mailSender.send(message);
            log.info("友情链接申请通知邮件发送成功: {}", friendLinkName);
        } catch (MessagingException e) {
            log.error("发送友情链接申请通知邮件失败", e);
        }
    }

    /**
     * 发送系统异常告警
     * @param errorMessage 错误信息
     * @param errorStack 错误堆栈
     */
    @Override
    public void sendSystemErrorNotification(String errorMessage, String errorStack) {
        try {
            // 获取管理员邮箱
            String adminEmail = sysSettingService.getAdminEmail();
            if (StrUtil.isBlank(adminEmail)) {
                log.warn("管理员邮箱未配置，无法发送系统异常告警");
                return;
            }

            // 创建邮件消息
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // 设置邮件内容
            helper.setTo(adminEmail);
            helper.setSubject("【OpusNocturne】系统异常告警");
            
            // 限制错误堆栈长度，避免邮件过大
            String limitedErrorStack = StrUtil.isNotBlank(errorStack) ? 
                    StrUtil.sub(errorStack, 0, 5000) : "无详细堆栈信息";
            
            String content = "<html>" +
                    "<body>" +
                    "<h3>系统异常告警</h3>" +
                    "<p>错误信息: <strong>" + errorMessage + "</strong></p>" +
                    "<p>错误堆栈:</p>" +
                    "<pre style=\"background-color: #f5f5f5; padding: 10px; border-radius: 4px;\">" + limitedErrorStack + "</pre>" +
                    "<p>请及时登录服务器查看详细日志并处理。</p>" +
                    "</body>" +
                    "</html>";
            
            helper.setText(content, true);

            // 发送邮件
            mailSender.send(message);
            log.info("系统异常告警邮件发送成功");
        } catch (MessagingException e) {
            log.error("发送系统异常告警邮件失败", e);
        }
    }
}
