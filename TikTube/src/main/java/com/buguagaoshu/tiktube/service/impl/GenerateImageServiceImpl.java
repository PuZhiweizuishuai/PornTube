package com.buguagaoshu.tiktube.service.impl;


import com.buguagaoshu.tiktube.service.GenerateImageService;
import com.buguagaoshu.tiktube.utils.VerifyCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author puzhiwei
 */
@Service
public class GenerateImageServiceImpl implements GenerateImageService {
    private final VerifyCodeUtil verifyCodeUtil;



    @Autowired
    public GenerateImageServiceImpl(VerifyCodeUtil verifyCodeUtil) {
        this.verifyCodeUtil = verifyCodeUtil;
    }

    @Override
    public Image imageWithDisturb(String string) {
        return imageWithDisturb(string, verifyCodeUtil.getWidth(), verifyCodeUtil.getHeight());
    }

    private Image imageWithDisturb(String string, int width, int height) {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) bi.getGraphics();
        // 填充背景
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        // 抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 画干扰圆
        verifyCodeUtil.drawOval(2, g2d);
        // 画干扰线
        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        verifyCodeUtil.drawBesselLine(1, g2d);

        FontMetrics fontMetrics = g2d.getFontMetrics();
        // 每一个字符所占的宽度
        int fW = width / string.length();
        // 字符的左右边距
        int fSp = (fW - (int) fontMetrics.getStringBounds("W", g2d).getWidth()) / 2;
        for (int i = 0; i < string.length(); i++) {
            g2d.setColor(verifyCodeUtil.color());
            // 文字的纵坐标
            int fY = height - ((height - (int) fontMetrics.getStringBounds(String.valueOf(string.charAt(i)), g2d).getHeight()) >> 1);
            g2d.drawString(String.valueOf(string.charAt(i)), i * fW + fSp + 3, fY - 3);
        }
        g2d.dispose();
        return bi;
    }
}
