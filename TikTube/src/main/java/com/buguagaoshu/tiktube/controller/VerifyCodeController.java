package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.entity.UserEntity;
import com.buguagaoshu.tiktube.enums.ReturnCodeEnum;
import com.buguagaoshu.tiktube.repository.CountLimitRepository;
import com.buguagaoshu.tiktube.service.UserService;
import com.buguagaoshu.tiktube.service.VerifyCodeService;
import com.buguagaoshu.tiktube.utils.IpUtil;
import com.buguagaoshu.tiktube.utils.VerifyCodeKeyUtil;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2019-11-27 19:57
 */
@RestController
public class VerifyCodeController {
    private final VerifyCodeService verifyCodeService;

    private final UserService userService;

    private final CountLimitRepository countLimitRepository;

    private final IpUtil ipUtil;

    private static final String IMAGE_FORMAT = "png";

    public VerifyCodeController(VerifyCodeService verifyCodeService,
                                UserService userService,
                                CountLimitRepository countLimitRepository,
                                IpUtil ipUtil) {
        this.verifyCodeService = verifyCodeService;
        this.userService = userService;
        this.countLimitRepository = countLimitRepository;

        this.ipUtil = ipUtil;
    }

    private static InputStreamResource imageToInputStreamResource(Image image, String format) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write((RenderedImage) image, format, byteArrayOutputStream);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        return new InputStreamResource(byteArrayInputStream);
    }

    @GetMapping("/api/verifyImage")
    public HttpEntity image(HttpServletResponse response) throws IOException {
        // 每次取图都换一个键，旧键下的验证码随 TTL 自然过期
        Image image = verifyCodeService.image(VerifyCodeKeyUtil.issue(response));
        InputStreamResource inputStreamResource = imageToInputStreamResource(image, IMAGE_FORMAT);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Pragma", "No-cache");
        httpHeaders.set("Cache-Control", "no-cache");
        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(httpHeaders)
                .contentType(MediaType.IMAGE_PNG)
                .body(inputStreamResource);
    }

    /**
     * 发送验证码
     */
    @PostMapping("/api/verify/send")
    public ResponseDetails send(@RequestBody UserEntity user, HttpServletRequest request) {
        String ip = ipUtil.getIpAddr(request);
        verifyCodeService.verify(VerifyCodeKeyUtil.read(request), user.getVerifyCode());
        // 同时限制 IP 与 邮箱地址
        boolean resEmail = countLimitRepository.allowSendEmail(user.getMail());
        if (!resEmail) {
            return ResponseDetails.ok(ReturnCodeEnum.COUNT_LIMIT);
        }
        boolean resIP = countLimitRepository.allowSendEmail(ip);
        if (!resIP) {
            return ResponseDetails.ok(ReturnCodeEnum.COUNT_LIMIT);
        }
        countLimitRepository.recordEmailSent(user.getMail());
        countLimitRepository.recordEmailSent(ip);

        verifyCodeService.send(user.getMail());
        return ResponseDetails.ok();
    }
}
