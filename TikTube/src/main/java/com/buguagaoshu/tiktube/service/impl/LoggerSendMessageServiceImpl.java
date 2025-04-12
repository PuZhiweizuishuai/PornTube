package com.buguagaoshu.tiktube.service.impl;

import com.buguagaoshu.tiktube.service.SendMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author puzhiwei
 */
@Service
@Slf4j
public class LoggerSendMessageServiceImpl implements SendMessageService {

    @Override
    public void send(String key, String message) {
        log.info("Send to key:[{}] with message:[{}]", key, message);
    }
}
