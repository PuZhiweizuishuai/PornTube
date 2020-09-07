package com.buguagaoshu.porntube.service.impl;


import com.buguagaoshu.porntube.exception.VerifyFailedException;
import com.buguagaoshu.porntube.repository.VerifyCodeRepository;
import com.buguagaoshu.porntube.service.GenerateImageService;
import com.buguagaoshu.porntube.service.SendMessageService;
import com.buguagaoshu.porntube.service.VerifyCodeService;
import com.buguagaoshu.porntube.utils.VerifyCodeUtil;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.security.SecureRandom;
import java.util.Objects;


/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2019-11-26 17:07
 * 验证码服务
 */
@Service
public class DigitsVerifyCodeServiceImpl implements VerifyCodeService {
    private final VerifyCodeRepository verifyCodeRepository;

    private final GenerateImageService generateImageService;

    private final SendMessageService sendMessageService;

    private final VerifyCodeUtil verifyCodeUtil;

    public static final long VERIFY_CODE_EXPIRE_TIMEOUT = 60000L;

    public static final long SEND_VERIFY_CODE_EXPIRE_TIMEOUT = 15L;

    public DigitsVerifyCodeServiceImpl(VerifyCodeRepository verifyCodeRepository, GenerateImageService generateImageService, SendMessageService sendMessageService, VerifyCodeUtil verifyCodeUtil) {
        this.verifyCodeRepository = verifyCodeRepository;
        this.generateImageService = generateImageService;
        this.sendMessageService = sendMessageService;
        this.verifyCodeUtil = verifyCodeUtil;
    }

    private static String randomDigitString(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(random.nextInt(10));
        }
        return stringBuilder.toString();
    }

    /**
     * @param string 验证码
     * @param type 验证码类型
     *             S 手机或邮箱发送的验证码，有效时间长
     *             W web 页面的图形验证码，时间短
     * */
    private static String appendTimestamp(String string, String type) {
        return string + "#" + System.currentTimeMillis() + "#" + type;
    }

    @Override
    public void send(String key) {
        String verifyCode = randomDigitString(verifyCodeUtil.getLen());
        String verifyCodeWithTimestamp = appendTimestamp(verifyCode, "S");

        verifyCodeRepository.save(key, verifyCodeWithTimestamp, SEND_VERIFY_CODE_EXPIRE_TIMEOUT);
        sendMessageService.send(key, verifyCode);
    }

    @Override
    public void verify(String key, String code) {
        String lastVerifyCodeWithTimestamp = verifyCodeRepository.find(key);
        // 如果没有验证码，则随机生成一个
        if (lastVerifyCodeWithTimestamp == null) {
            lastVerifyCodeWithTimestamp = appendTimestamp(randomDigitString(verifyCodeUtil.getLen()), "W");
        }
        String[] lastVerifyCodeAndTimestamp = lastVerifyCodeWithTimestamp.split("#");
        String lastVerifyCode = lastVerifyCodeAndTimestamp[0];
        long timestamp = Long.parseLong(lastVerifyCodeAndTimestamp[1]);
        long expTime = VERIFY_CODE_EXPIRE_TIMEOUT;
        if ("S".equals(lastVerifyCodeAndTimestamp[2])) {
            expTime = VERIFY_CODE_EXPIRE_TIMEOUT * SEND_VERIFY_CODE_EXPIRE_TIMEOUT;
        }
        if (timestamp + expTime < System.currentTimeMillis()) {
            throw new VerifyFailedException("验证码已过期！");
        } else if (!Objects.equals(code, lastVerifyCode)) {
            throw new VerifyFailedException("验证码错误！");
        }
        verifyCodeRepository.remove(key);
    }

    @Override
    public Image image(String key) {
        String verifyCode = randomDigitString(verifyCodeUtil.getLen());
        String verifyCodeWithTimestamp = appendTimestamp(verifyCode, "W");
        Image image = generateImageService.imageWithDisturb(verifyCode);
        verifyCodeRepository.save(key, verifyCodeWithTimestamp);
        return image;
    }
}
