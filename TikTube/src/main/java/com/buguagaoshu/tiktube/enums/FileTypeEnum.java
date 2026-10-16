package com.buguagaoshu.tiktube.enums;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
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
    final int code;
    final String msg;

    /**
     * 视频投稿允许的后缀（统一大写比对）
     */
    private static final Set<String> VIDEO_TYPE = Set.of(
            ".MP4", ".MKV", ".OGM", ".WMV", ".MPG", ".WEBM", ".OGV", ".MOV", ".ASX", ".MPEG", ".M4V", ".AVI", ".FLV"
    );

    /**
     * 图片类文件允许的后缀（统一大写比对）
     */
    private static final Set<String> PHOTO_TYPE = Set.of(
            ".JPEG", ".JPG", ".GIF", ".PNG", ".TIFF", ".PJP", ".JFIF", ".SVG", ".BMP", ".SVGZ", ".WEBP", ".ICO"
            , ".XBM", ".DIB", ".TIF", ".PJEPG", ".AVIF"
    );

    /**
     * 文章（Vditor 编辑器）允许上传的附件后缀：图片，加上编辑器 accept 列表里的文档、压缩包与音视频。
     * 刻意不含 .html/.htm/.xhtml/.xml/.js/.svg 这类能被浏览器当成文档执行的类型
     */
    private static final Set<String> ARTICLE_TYPE = Set.of(
            ".JPEG", ".JPG", ".GIF", ".PNG", ".TIFF", ".TIF", ".BMP", ".WEBP", ".AVIF", ".ICO",
            ".MP3", ".WAV", ".MP4",
            ".ZIP", ".RAR", ".7Z", ".TXT", ".PDF",
            ".DOC", ".DOCX", ".PPT", ".PPTX", ".XLS", ".XLSX", ".APK"
    );

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
     * 获取文件后缀名，无后缀或文件名为空时返回空串
     */
    public static String getFileSuffix(String filename) {
        if (filename == null) {
            return "";
        }
        int number = filename.lastIndexOf(".");
        if (number <= 0) {
            return "";
        }
        // 用 ROOT 而不是默认 Locale：土耳其语等区域 toUpperCase 会把 i 转成 İ，导致白名单比对失效
        return filename.substring(number).toUpperCase(Locale.ROOT);
    }

    /**
     * 检查文件类型，后缀为空或为 null 时按其它附件处理
     */
    public static FileTypeEnum getFileType(String suffix) {
        if (suffix == null) {
            return FileTypeEnum.ARTICLE;
        }
        if (VIDEO_TYPE.contains(suffix)) {
            return FileTypeEnum.VIDEO;
        }
        if (PHOTO_TYPE.contains(suffix)) {
            return FileTypeEnum.PHOTO;
        }
        return FileTypeEnum.ARTICLE;
    }

    public static boolean checkPhotoType(int code) {
        return code == FileTypeEnum.PHOTO.getCode() || code == FileTypeEnum.AVATAR.getCode() || code == FileTypeEnum.TOP_IMAGE.getCode();
    }

    /**
     * 是否是文章允许的附件后缀。
     * 白名单项都是「. + 大写字母数字」，因此校验通过的同时也保证了后缀里不会夹带
     * 路径分隔符或 ".."（newFilename 会把后缀直接拼进落盘文件名）
     */
    public static boolean isArticleAttachment(String suffix) {
        return suffix != null && ARTICLE_TYPE.contains(suffix);
    }


    /**
     * SimpleDateFormat 非线程安全，多个上传请求并发格式化会写出错乱的日期目录，这里换成不可变的 DateTimeFormatter
     */
    private final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ROOT);

    public final static String ROOT = "file";

    public final static String OSS = "oss";

    public static String filePath() {
        return ROOT + "/" + LocalDate.now().format(DATE_FORMAT);
    }

    public static String ossFilePath() {
        return LocalDate.now().format(DATE_FORMAT);
    }

    public static String ossFileURL(int saveLocation, String path, String fileName) {
        return  "/api/upload/" + saveLocation + "/oss/" + path + "/" + fileName;
    }

    public static String newFilename(String suffix) {
        return UUID.randomUUID().toString().replace("-", "") + suffix;
    }
}
