package com.buguagaoshu.tiktube.vo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2022-06-06 19:15
 */
@Getter
@Setter
public class VideoInfo {
    /**
     * 总帧数
     **/
    private int lengthInFrames;

    /**
     * 帧率
     **/
    private double frameRate;

    /**
     * 时长
     **/
    private double duration;

    /**
     * 视频编码
     */
    private String videoCode;
    /**
     * 音频编码
     */
    private String audioCode;

    private int width;
    private int height;
    private int audioChannel;
    private String md5;
    /**
     * 音频采样率
     */
    private Integer sampleRate;

    public String toJson() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        } catch (Exception e) {
            return "";
        }
    }
}
