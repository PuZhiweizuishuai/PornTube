package com.buguagaoshu.tiktube.utils;

import com.buguagaoshu.tiktube.config.WebConfig;
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

import org.springframework.web.multipart.MultipartFile;

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
        return grabVideoInfo(() -> new FFmpegFrameGrabber(file), "本地文件");
    }

    /**
     * 直接从 MultipartFile 获取视频文件信息，无需保存到本地
     * @param file MultipartFile 视频文件
     * @return 视频信息对象
     */
    public static VideoInfo getVideoInfoFromMultipartFile(MultipartFile file) {
        // 从 MultipartFile 获取输入流作为抓帧器数据源
        return grabVideoInfo(() -> new FFmpegFrameGrabber(file.getInputStream()), "MultipartFile");
    }

    /**
     * 抓帧器的构造方式（File 或 InputStream），允许抛出受检异常
     */
    @FunctionalInterface
    private interface GrabberFactory {
        FFmpegFrameGrabber create() throws Exception;
    }

    /**
     * 视频信息读取的统一实现
     *
     * 原实现把「构造抓帧器 → start → 逐个字段赋值 → 释放资源」在
     * getVideoInfo 与 getVideoInfoFromMultipartFile 里各写了一整遍（约 40 行重复），
     * 现在只保留一份，两个入口只负责提供数据源。
     *
     * @param factory 抓帧器工厂
     * @param source  来源描述，仅用于日志
     * @return 视频信息，解析失败返回 null
     */
    private static VideoInfo grabVideoInfo(GrabberFactory factory, String source) {
        FFmpegFrameGrabber grabber = null;
        try {
            grabber = factory.create();
            grabber.start();

            VideoInfo videoInfo = new VideoInfo();
            videoInfo.setLengthInFrames(grabber.getLengthInVideoFrames());
            videoInfo.setFrameRate(grabber.getVideoFrameRate());
            videoInfo.setDuration(grabber.getLengthInTime() / 1000000.00);
            videoInfo.setWidth(grabber.getImageWidth());
            videoInfo.setHeight(grabber.getImageHeight());
            videoInfo.setAudioChannel(grabber.getAudioChannels());
            videoInfo.setVideoCode(grabber.getVideoCodecName());
            videoInfo.setAudioCode(grabber.getAudioCodecName());
            videoInfo.setSampleRate(grabber.getSampleRate());
            return videoInfo;
        } catch (Exception e) {
            // 原实现是 e.printStackTrace()，改为落日志
            log.error("从{}获取视频信息失败: {}", source, e.getMessage(), e);
            return null;
        } finally {
            releaseGrabber(grabber);
        }
    }

    /**
     * 释放抓帧器资源
     */
    private static void releaseGrabber(FFmpegFrameGrabber grabber) {
        if (grabber == null) {
            return;
        }
        try {
            grabber.stop();
            grabber.release();
        } catch (FFmpegFrameGrabber.Exception e) {
            log.error("grabber.release failed 释放视频解析器失败：{}", e.getMessage());
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
                FileTableEntity fileTable = FileUtils.createFileTableEntity(imageName, SUFFIX, path, f.image.length, WebConstant.SYSTEM_CREATE_SCREENSHOT, userId, FileTypeEnum.VIDEO_PHOTO.getCode(), WebConfig.FILE_SAVE_LOCATION);
                images.add(fileTable);
            }
            return images;
        } catch (Exception e) {
            // 原实现是 e.printStackTrace()，改为落日志
            log.error("截图失败：文件名： {}， 错误信息：{}", videFile.getName(), e.getMessage(), e);
            return null;
        } finally {
            releaseGrabber(grabber);
        }
    }
}
