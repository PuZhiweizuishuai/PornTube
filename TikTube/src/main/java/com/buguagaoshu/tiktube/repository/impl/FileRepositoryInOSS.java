package com.buguagaoshu.tiktube.repository.impl;


import com.buguagaoshu.tiktube.cache.SharedCache;
import com.buguagaoshu.tiktube.cache.SharedCacheVersionStore;
import com.buguagaoshu.tiktube.config.WebConfig;
import com.buguagaoshu.tiktube.config.WebConstant;
import com.buguagaoshu.tiktube.entity.OSSConfigEntity;
import com.buguagaoshu.tiktube.service.impl.OssConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-28
 */
@Repository
@Slf4j
public class FileRepositoryInOSS {
    private final OssConfigService ossConfigService;

    private final SharedCacheVersionStore versionStore;

    /**
     * 三张表必须整体替换：先 clear 再逐个填充会让并发上传读到空表，
     * 进而按 FILE_SAVE_LOCATION=0 走本地落盘，或在找不到客户端时上传失败
     */
    private volatile Map<Integer, S3Client> s3Clients = new HashMap<>();

    private volatile Map<Integer, S3Presigner> s3Presigners = new HashMap<>();

    private volatile Map<Integer, OSSConfigEntity> configEntityMap = new HashMap<>();

    @Autowired
    public FileRepositoryInOSS(OssConfigService ossConfigService, SharedCacheVersionStore versionStore) {
        this.ossConfigService = ossConfigService;
        this.versionStore = versionStore;
        // 初始化时更新客户端
        updateS3Client();
    }

    /**
     * 当对象存储配置发生变化后，更新客户端，并把新版本广播给其他实例
     * 更新 S3 客户端
     * */
    public void updateS3Client() {
        Map<Integer, S3Client> newClients = new HashMap<>();
        Map<Integer, S3Presigner> newPresigners = new HashMap<>();
        Map<Integer, OSSConfigEntity> newConfigEntityMap = new HashMap<>();
        int saveLocation = 0;
        // 获取所有对象存储配置
        List<OSSConfigEntity> ossConfigEntities = this.ossConfigService.listAllOssConfigs();
        if (ossConfigEntities != null) {
            // 创建客户端并保存
            for (OSSConfigEntity ossConfigEntity : ossConfigEntities) {
                newConfigEntityMap.put(ossConfigEntity.getId(), ossConfigEntity);
                try {
                    if (Integer.valueOf(1).equals(ossConfigEntity.getStatus())) {
                        saveLocation = ossConfigEntity.getId();
                    }
                    S3Client s3Client = this.ossConfigService.buildS3Client(ossConfigEntity);
                    newClients.put(ossConfigEntity.getId(), s3Client);
                    newPresigners.put(ossConfigEntity.getId(), this.ossConfigService.buildS3Presigner(ossConfigEntity));

                    // 检查存储桶是否存在，不存在则创建
                    if (!bucketExists(s3Client, ossConfigEntity.getBucketName())
                            && Integer.valueOf(1).equals(ossConfigEntity.getStatus())) {
                        s3Client.createBucket(
                                CreateBucketRequest.builder()
                                        .bucket(ossConfigEntity.getBucketName())
                                        .build()
                        );
                        log.info("创建存储桶成功: {}", ossConfigEntity.getBucketName());
                    }

                    log.info("初始化对象存储客户端成功: {}", ossConfigEntity.getConfigName());
                } catch (Exception e) {
                    log.error("初始化对象存储客户端失败: {}, 错误信息: {}", ossConfigEntity.getConfigName(), e.getMessage(), e);
                }
            }
        }

        List<S3Client> outdatedClients = new ArrayList<>(s3Clients.values());
        List<S3Presigner> outdatedPresigners = new ArrayList<>(s3Presigners.values());
        // 先换表再换落盘位置，保证读到新位置的线程一定拿得到对应的客户端
        configEntityMap = newConfigEntityMap;
        s3Clients = newClients;
        s3Presigners = newPresigners;
        WebConfig.FILE_SAVE_LOCATION = saveLocation;
        closeOutdated(outdatedClients, outdatedPresigners);
        versionStore.markUpdated(SharedCache.OSS_CONFIG);
    }

    /**
     * 存储桶是否存在
     * */
    private boolean bucketExists(S3Client s3Client, String bucketName) {
        try {
            s3Client.headBucket(
                    HeadBucketRequest.builder()
                            .bucket(bucketName)
                            .build()
            );
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        }
    }

    /**
     * 重建客户端时关闭上一批客户端，避免连接池泄漏
     * */
    private void closeOutdated(List<S3Client> clients, List<S3Presigner> presigners) {
        for (S3Client client : clients) {
            try {
                client.close();
            } catch (Exception e) {
                log.warn("关闭对象存储客户端失败: {}", e.getMessage());
            }
        }
        for (S3Presigner presigner : presigners) {
            try {
                presigner.close();
            } catch (Exception e) {
                log.warn("关闭对象存储预签名客户端失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 保存文件到对象存储
     * 
     * @param file 文件
     * @param objectName 对象名称，如果为空则自动生成
     * @param contentType 内容类型
     * @param ossConfigId 对象存储配置ID，如果为空则使用默认配置
     * @return 文件URL
     */
    public String saveFile(MultipartFile file, String objectName, String contentType, Integer ossConfigId) {
        try {

            OSSConfigEntity ossConfigEntity = configEntityMap.get(ossConfigId);
            // 获取S3Client
            S3Client s3Client = s3Clients.get(ossConfigId);
            if (s3Client == null) {
                log.error("未找到对应的S3Client WebConfig.FILE_SAVE_LOCATION = {}", ossConfigId);
                return null;
            }
            
            // 如果对象名为空，则自动生成
            if (objectName == null || objectName.isEmpty()) {
                objectName = UUID.randomUUID().toString().replaceAll("-", "") + 
                        getFileExtension(file.getOriginalFilename());
            }
            
            // 上传文件
            try (InputStream inputStream = file.getInputStream()) {
                s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(ossConfigEntity.getBucketName())
                                .key(objectName)
                                .contentType(contentType != null ? contentType : file.getContentType())
                                .build(),
                        RequestBody.fromInputStream(inputStream, file.getSize())
                );
                return objectName;
            } catch (Exception e) {
                log.error("文件上传失败: {}", e.getMessage());
                return null;
            }
        } catch (Exception e) {
            log.error("保存文件失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 删除文件
     * 
     * @param objectName 对象名称
     * @param ossConfigId 对象存储配置ID，如果为空则使用默认配置
     * @return 是否删除成功
     */
    public boolean deleteFile(String objectName, Integer ossConfigId) {
        try {
            // 获取对象存储配置
            OSSConfigEntity ossConfig = configEntityMap.get(ossConfigId);
            
            // 获取S3Client
            S3Client s3Client = s3Clients.get(ossConfigId);
            if (s3Client == null) {
                log.error("未找到对应的S3Client: {}", ossConfig.getConfigName());
                return false;
            }
            
            // 删除文件
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(ossConfig.getBucketName())
                            .key(objectName)
                            .build()
            );
            
            return true;
        } catch (Exception e) {
            log.error("删除文件失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 获取文件URL
     * 
     * @param objectName 对象名称
     * @param ossConfigId 对象存储配置ID，如果为空则使用默认配置
     * @return 文件URL
     */
    public String getFileUrl(String objectName, Integer ossConfigId) {
        try {
            // 获取对象存储配置
            OSSConfigEntity ossConfig = configEntityMap.get(ossConfigId);
            
            // 获取S3Presigner
            S3Presigner s3Presigner = s3Presigners.get(ossConfigId);
            if (s3Presigner == null) {
                log.error("未找到对应的S3Presigner: {}", ossConfig.getConfigName());
                return null;
            }
            
            // 如果配置了自定义域名，则使用自定义域名
            if (ossConfig.getUrlPrefix() != null && !ossConfig.getUrlPrefix().isEmpty()) {
                return ossConfig.getUrlPrefix() + "/" + objectName;
            }
            
            // 生成预签名URL

            return s3Presigner.presignGetObject(
                    GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofDays(WebConstant.OSS_DAY_TIME))
                            .getObjectRequest(
                                    GetObjectRequest.builder()
                                            .bucket(ossConfig.getBucketName())
                                            .key(objectName)
                                            .build()
                            )
                            .build()
            ).url().toString();
        } catch (Exception e) {
            log.error("获取文件URL失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 获取文件扩展名
     * 
     * @param filename 文件名
     * @return 文件扩展名（包含点号）
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty() || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 上传本地文件到对象存储
     * 
     * @param localFilePath 本地文件路径
     * @param objectName 对象名称，如果为空则自动生成
     * @param ossConfigId 对象存储配置ID，如果为空则使用默认配置
     * @return 文件URL
     */
    public String uploadLocalFile(String localFilePath, String objectName, Integer ossConfigId) {
        try {
            // 获取对象存储配置
            OSSConfigEntity ossConfigEntity = configEntityMap.get(ossConfigId);
            if (ossConfigEntity == null) {
                log.error("未找到对应的对象存储配置: {}", ossConfigId);
                return null;
            }
            
            // 获取S3Client
            S3Client s3Client = s3Clients.get(ossConfigId);
            if (s3Client == null) {
                log.error("未找到对应的S3Client: {}", ossConfigId);
                return null;
            }
            
            // 创建File对象
            Path path = Path.of(localFilePath);
            if (!Files.isRegularFile(path)) {
                log.error("本地文件不存在或不是文件: {}", localFilePath);
                return null;
            }
            
            // 如果对象名为空，则自动生成
            if (objectName == null || objectName.isEmpty()) {
                objectName = UUID.randomUUID().toString().replaceAll("-", "") + 
                        getFileExtension(path.getFileName().toString());
            }
            
            // 获取文件内容类型
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                // 如果无法确定内容类型，使用通用二进制类型
                contentType = "application/octet-stream";
            }
            
            // 上传文件
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(ossConfigEntity.getBucketName())
                            .key(objectName)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromFile(path)
            );
            
            return objectName;
        } catch (Exception e) {
            log.error("上传本地文件失败: {}, 错误信息: {}", localFilePath, e.getMessage(), e);
            return null;
        }
    }
}
