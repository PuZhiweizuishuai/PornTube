package com.buguagaoshu.tiktube.repository;

import com.buguagaoshu.tiktube.config.WebConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件存储路由
 *
 * 按 WebConfig.FILE_SAVE_LOCATION 在 {@link FileStorageStrategy} 实现中挑选一个，
 * 把 FileRepositoryImpl 里四处「本地 / OSS」if-else 收敛成一次调用。
 */
@Slf4j
@Component
public class FileStorageRouter {

    private final List<FileStorageStrategy> strategies;

    @Autowired
    public FileStorageRouter(List<FileStorageStrategy> strategies) {
        this.strategies = strategies;
    }

    /** 按当前全局配置选择策略 */
    public FileStorageStrategy route() {
        return route(WebConfig.FILE_SAVE_LOCATION);
    }

    public FileStorageStrategy route(int saveLocation) {
        for (FileStorageStrategy strategy : strategies) {
            if (strategy.supports(saveLocation)) {
                return strategy;
            }
        }
        throw new IllegalStateException("没有找到匹配的文件存储策略，saveLocation=" + saveLocation);
    }

    /**
     * 保存文件
     *
     * @return 成功返回文件标识/地址，失败返回 null
     */
    public String store(MultipartFile file, String path, String filename) {
        return route().store(file, path, filename);
    }

    /** 删除文件 */
    public boolean delete(String fileUrl, int saveLocation) {
        return route(saveLocation).delete(fileUrl, saveLocation);
    }
}
