package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.service.VerifyCodeService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpSession;
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

    private static final String IMAGE_FORMAT = "png";

    public VerifyCodeController(VerifyCodeService verifyCodeService) {
        this.verifyCodeService = verifyCodeService;
    }

    private static InputStreamResource imageToInputStreamResource(Image image, String format) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write((RenderedImage) image, format, byteArrayOutputStream);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        return new InputStreamResource(byteArrayInputStream);
    }

    @GetMapping("/api/verifyImage")
    public HttpEntity image(HttpSession session) throws IOException {
        Image image = verifyCodeService.image(session.getId());
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

//    @PostMapping("/image/verify")
//    public HttpEntity imageVerify(HttpSession session, String code) {
//        verifyCodeService.verify(session.getId(), code);
//        return ResponseEntity
//                .status(HttpStatus.NO_CONTENT)
//                .body(null);
//    }
}
