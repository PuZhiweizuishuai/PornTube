package com.buguagaoshu.porntube;

import org.apache.tomcat.util.security.MD5Encoder;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.File;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2022-06-06 17:23
 * 测试java cv ffmpge 用法
 */
public class FFmpgeTest {
    public static void main(String[] args) {
        File file = new File("E:\\Spring\\PornTube\\PornTube\\file\\2022-06-01\\7c5db335754849918b42041bd3722d46.MP4");
        FFmpegFrameGrabber grabber = null;
        try {
            grabber = new FFmpegFrameGrabber(file);
            grabber.start();

            int lengthInFrames = grabber.getLengthInVideoFrames();
            System.out.println("总帧数：" + lengthInFrames);
            double frameRate = grabber.getVideoFrameRate();
            System.out.println("帧率：" + frameRate);
            double duration = grabber.getLengthInTime() / 1000000.00;
            System.out.println("时长：" + duration);
            int width = grabber.getImageWidth();
            System.out.println(width);
            int height = grabber.getImageHeight();
            System.out.println(height);
            int audioChannel = grabber.getAudioChannels();
            System.out.println(audioChannel);
            String videoCode = grabber.getVideoCodecName();
            System.out.println(videoCode);
            String audioCode = grabber.getAudioCodecName();
            System.out.println(audioCode);
            // String md5 = MD5Util.getMD5ByInputStream(new FileInputStream(file));
            int sampleRate = grabber.getSampleRate();
            System.out.println("音频采样率：" + sampleRate);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (grabber != null) {
                    grabber.stop();
                    grabber.release();
                }
            } catch (FFmpegFrameGrabber.Exception e) {
                System.out.println("getVideoInfo grabber.release failed");
            }
        }
    }
}
