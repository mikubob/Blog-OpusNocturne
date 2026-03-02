package com.xuan.common.service;

/**
 * 通知服务接口
 *
 * @author 玄〤
 */
public interface INotificationService {
    /**
     * 发送友情链接申请通知
     * @param friendLinkName 友情链接名称
     * @param friendLinkUrl 友情链接URL
     * @param friendLinkEmail 友情链接邮箱
     */
    void sendFriendLinkApplyNotification(String friendLinkName, String friendLinkUrl, String friendLinkEmail);

    /**
     * 发送系统异常告警
     * @param errorMessage 错误信息
     * @param errorStack 错误堆栈
     */
    void sendSystemErrorNotification(String errorMessage, String errorStack);
}
