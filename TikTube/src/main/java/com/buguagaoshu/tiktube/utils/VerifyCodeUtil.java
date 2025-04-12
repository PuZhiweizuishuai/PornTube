package com.buguagaoshu.tiktube.utils;

import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.QuadCurve2D;
import java.security.SecureRandom;
import java.util.Random;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2019-11-30 14:33
 */
public class VerifyCodeUtil {
    /**
     * 常用颜色
     * */
    public static final int[][] COLOR = {{0, 135, 255}, {51, 153, 51}, {255, 102, 102}, {255, 153, 0}, {153, 102, 0}, {153, 102, 153}, {51, 153, 153}, {102, 102, 255}, {0, 102, 204}, {204, 51, 51}, {0, 153, 204}, {0, 51, 102}};
    public static final Random RANDOM = new SecureRandom();

    /**
     * 验证码随机字符长度
     * */
    protected int len = 5;

    /**
     * 验证码显示宽度
     * */
    protected int width = 130;

    /**
     * 验证码显示高度
     * */
    protected int height = 48;


    /**
     * 产生两个数之间的随机数
     *
     * @param min 最小值
     * @param max 最大值
     * @return 随机数
     */
    public static int num(int min, int max) {
        return min + RANDOM.nextInt(max - min);
    }

    /**
     * 产生0-num的随机数,不包括num
     *
     * @param num 最大值
     * @return 随机数
     */
    public static int num(int num) {
        return RANDOM.nextInt(num);
    }

    /**
     * 给定范围获得随机颜色
     *
     * @param fc 0-255
     * @param bc 0-255
     * @return 随机颜色
     */
    public Color color(int fc, int bc) {
        if (fc > 255)
            fc = 255;
        if (bc > 255)
            bc = 255;
        int r = fc + num(bc - fc);
        int g = fc + num(bc - fc);
        int b = fc + num(bc - fc);
        return new Color(r, g, b);
    }

    /**
     * 获取随机常用颜色
     *
     * @return 随机颜色
     */
    public Color color() {
        int[] color = COLOR[num(COLOR.length)];
        return new Color(color[0], color[1], color[2]);
    }


    /**
     * 随机画干扰线
     *
     * @param num 数量
     * @param g   Graphics2D
     */
    public void drawLine(int num, Graphics2D g) {
        drawLine(num, null, g);
    }

    /**
     * 随机画干扰线
     *
     * @param num   数量
     * @param color 颜色
     * @param g     Graphics2D
     */
    public void drawLine(int num, Color color, Graphics2D g) {
        for (int i = 0; i < num; i++) {
            g.setColor(color == null ? color() : color);
            int x1 = num(-10, width - 10);
            int y1 = num(5, height - 5);
            int x2 = num(10, width + 10);
            int y2 = num(2, height - 2);
            g.drawLine(x1, y1, x2, y2);
        }
    }

    /**
     * 随机画干扰圆
     *
     * @param num 数量
     * @param g   Graphics2D
     */
    public void drawOval(int num, Graphics2D g) {
        drawOval(num, null, g);
    }

    /**
     * 随机画干扰圆
     *
     * @param num   数量
     * @param color 颜色
     * @param g     Graphics2D
     */
    public void drawOval(int num, Color color, Graphics2D g) {
        for (int i = 0; i < num; i++) {
            g.setColor(color == null ? color() : color);
            int w = 5 + num(10);
            g.drawOval(num(width - 25), num(height - 15), w, w);
        }
    }

    /**
     * 随机画贝塞尔曲线
     *
     * @param num 数量
     * @param g   Graphics2D
     */
    public void drawBesselLine(int num, Graphics2D g) {
        drawBesselLine(num, null, g);
    }

    /**
     * 随机画贝塞尔曲线
     *
     * @param num   数量
     * @param color 颜色
     * @param g     Graphics2D
     */
    public void drawBesselLine(int num, Color color, Graphics2D g) {
        for (int i = 0; i < num; i++) {
            g.setColor(color == null ? color() : color);
            int x1 = 5, y1 = num(5, height / 2);
            int x2 = width - 5, y2 = num(height / 2, height - 5);
            int ctrlx = num(width / 4, width / 4 * 3), ctrly = num(5, height - 5);
            if (num(2) == 0) {
                int ty = y1;
                y1 = y2;
                y2 = ty;
            }
            // 二阶贝塞尔曲线
            if (num(2) == 0) {
                QuadCurve2D shape = new QuadCurve2D.Double();
                shape.setCurve(x1, y1, ctrlx, ctrly, x2, y2);
                g.draw(shape);
            } else {
                // 三阶贝塞尔曲线
                int ctrlx1 = num(width / 4, width / 4 * 3), ctrly1 = num(5, height - 5);
                CubicCurve2D shape = new CubicCurve2D.Double(x1, y1, ctrlx, ctrly, ctrlx1, ctrly1, x2, y2);
                g.draw(shape);
            }
        }
    }


    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
