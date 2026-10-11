package com.buguagaoshu.tiktube.repository;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储策略
 *
 * 背景：FileRepositoryImpl 里「本地 / 对象存储」×「视频 / 图片 / 文章附件」
 * 一共散落了四处几乎相同的分支（143-264 行），每处都要重复
 * 「建目录 → 写文件 → 拼 URL → 判空」这套逻辑。
 * 现在收敛成策略接口：调用方只管 {@link #store}，不再关心存到哪。
 */
public interface FileStorageStrategy {

    /**
     * 当前策略是否负责该存储位置
     *
     * @param saveLocation 存储位置配置（0 本地，其它为 OSS 配置 ID）
     */
    boolean supports(int saveLocation);

    /**
     * 保存文件
     *
     * @param file     上传的文件
     * @param path     目录（已按存储位置生成好）
     * @param filename 文件名（含后缀）
     * @return 可访问的文件地址，失败返回 null
     */
    String store(MultipartFile file, String path, String filename);

    /**
     * 删除文件
     *
     * @param fileUrl      文件的完整访问地址
     * @param saveLocation 存储位置配置
     * @return 是否删除成功
     */
    boolean delete(String fileUrl, int saveLocation);
}
