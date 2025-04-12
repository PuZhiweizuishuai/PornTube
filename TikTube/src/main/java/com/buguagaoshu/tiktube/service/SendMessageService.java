package com.buguagaoshu.tiktube.service;

/**
 * @author puzhiwei
 * 发送验证消息
 * */
public interface SendMessageService {

    void send(String key, String message);
}
