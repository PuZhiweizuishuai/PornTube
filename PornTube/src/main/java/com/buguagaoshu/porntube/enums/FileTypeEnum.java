package com.buguagaoshu.porntube.enums;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-06 14:04
 */
public enum FileTypeEnum {
    /**
     * 文件类型
     */
    VIDEO(0, "视频投稿"),
    PHOTO(1, "图片投稿"),
    ARTICLE(2, "文章投稿"),
    AVATAR(3, "头像数据"),
    TOP_IMAGE(4, "个人主页顶部大图"),
    VIDEO_PHOTO(5, "系统生成视频封面")
    ;
    int code;
    String msg;

    private static String[] videoType = {
            ".MP4", ".MKV", ".OGM", ".WMV", ".MPG", ".WEBM", ".OGV", ".MOV", ".ASX", ".MPEG", "M4V", ".AVI", ".FLV"
    };

    private static String[] photoType = {
            ".JPEG", ".JPG", ".GIF", ".PNG", ".TIFF", ".PJP", ".JFIF", ".SVG", ".BMP", ".SVGZ", ".WEBP", ".ICO"
            , ".XBM", ".DIB", ".TIF", ".PJEPG", ".AVIF"

    };

    FileTypeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    /**
     * 获取文件后缀名
     */
    public static String getFileSuffix(String filename) {
        int number = filename.lastIndexOf(".");
        if (number <= 0) {
            return "";
        }
        return filename.substring(number).toUpperCase();
    }

    /**
     * 检查文件类型
     */
    public static FileTypeEnum getFileType(String suffix) {
        for (String v : videoType) {
            if (v.equals(suffix)) {
                return FileTypeEnum.VIDEO;
            }
        }
        for (String p : photoType) {
            if (p.equals(suffix)) {
                return FileTypeEnum.PHOTO;
            }
        }
        return FileTypeEnum.ARTICLE;
    }

    public static boolean checkPhotoType(int code) {
        return code == FileTypeEnum.PHOTO.getCode() || code == FileTypeEnum.AVATAR.getCode() || code == FileTypeEnum.TOP_IMAGE.getCode();
    }


    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public final static String ROOT = "file";

    public static String filePath() {
        return ROOT + "/" + DATE_FORMAT.format(new Date());
    }

    public static String newFilename(String suffix) {
        return UUID.randomUUID().toString().replace("-", "") + suffix;
    }
}
