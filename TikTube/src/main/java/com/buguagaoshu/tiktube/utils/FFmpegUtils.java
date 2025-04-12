package com.buguagaoshu.tiktube.utils;

import com.buguagaoshu.tiktube.config.WebConstant;
import com.buguagaoshu.tiktube.entity.FileTableEntity;
import com.buguagaoshu.tiktube.enums.FileTypeEnum;
import com.buguagaoshu.tiktube.vo.VideoInfo;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2022-06-06 19:24
 */
@Slf4j
public class FFmpegUtils {
    private final static String SUFFIX = ".JPG";

    /**
     * 获取视频文件信息
     * */
    public static VideoInfo getVideoInfo(File file) {
        VideoInfo videoInfo = new VideoInfo();
        FFmpegFrameGrabber grabber = null;
        try {
            grabber = new FFmpegFrameGrabber(file);
            grabber.start();

            videoInfo.setLengthInFrames(grabber.getLengthInVideoFrames());

            videoInfo.setFrameRate(grabber.getVideoFrameRate());

            videoInfo.setDuration(grabber.getLengthInTime() / 1000000.00);

            videoInfo.setWidth(grabber.getImageWidth());

            videoInfo.setHeight(grabber.getImageHeight());

            videoInfo.setAudioChannel(grabber.getAudioChannels());

            videoInfo.setVideoCode(grabber.getVideoCodecName());

            videoInfo.setAudioCode(grabber.getAudioCodecName());
            // String md5 = MD5Util.getMD5ByInputStream(new FileInputStream(file));

            videoInfo.setSampleRate(grabber.getSampleRate());
            return videoInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (grabber != null) {
                    grabber.stop();
                    grabber.release();
                }
            } catch (FFmpegFrameGrabber.Exception e) {
                log.error("getVideoInfo grabber.release failed 获取文件信息失败：{}", e.getMessage());
            }
        }
    }

    /**
     * 随机获取视频截图
     * @param videFile 视频文件
     * @param count 输出截图数量
     * @return 截图列表
     * */
    public static List<FileTableEntity> randomGrabberFFmpegImage(File videFile, int count, long userId) {
        FFmpegFrameGrabber grabber = null;

        String path = FileTypeEnum.filePath();
        try {
            List<FileTableEntity> images = new ArrayList<>(count);
            grabber = new FFmpegFrameGrabber(videFile);
            grabber.start();
            // 获取视频总帧数
            // int lengthInVideoFrames = grabber.getLengthInVideoFrames();
            // 获取视频时长， / 1000000 将单位转换为秒
            long delayedTime = grabber.getLengthInTime() / 1000000;

            Random random = SecureRandom.getInstanceStrong();
            int[] timeList = new int[count];
            for (int i = 0; i < count; i++) {
                timeList[i] = random.nextInt((int)delayedTime - 1) + 1;
            }
            // 让截图按时间线排列
            Arrays.sort(timeList);
            for (int i : timeList) {
                // 跳转到响应时间
                grabber.setTimestamp(i * 1000000L);
                Frame f = grabber.grabImage();
                //System.out.println(f.imageWidth);
                Java2DFrameConverter converter = new Java2DFrameConverter();
                BufferedImage bi = converter.getBufferedImage(f);
                String imageName = FileTypeEnum.newFilename(SUFFIX);
                File out = Paths.get(path, imageName).toFile();
                ImageIO.write(bi, "jpg", out);
                FileTableEntity fileTable = FileUtils.createFileTableEntity(imageName, SUFFIX, path, f.image.length, WebConstant.SYSTEM_CREATE_SCREENSHOT, userId, FileTypeEnum.VIDEO_PHOTO.getCode());
                images.add(fileTable);
            }
            return images;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("截图失败：文件名： {}， 错误信息：{}",videFile.getName(), e.getMessage());
            return null;
        } finally {
            try {
                if (grabber != null) {
                    grabber.stop();
                    grabber.release();
                }
            } catch (FFmpegFrameGrabber.Exception e) {
                log.error("getVideoInfo grabber.release failed 获取文件信息失败：{}", e.getMessage());
            }
        }
    }
}
