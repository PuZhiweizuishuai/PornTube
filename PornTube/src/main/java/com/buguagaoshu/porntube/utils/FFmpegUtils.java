package com.buguagaoshu.porntube.utils;

import com.buguagaoshu.porntube.vo.VideoInfo;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.File;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2022-06-06 19:24
 */
@Slf4j
public class FFmpegUtils {
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
}
