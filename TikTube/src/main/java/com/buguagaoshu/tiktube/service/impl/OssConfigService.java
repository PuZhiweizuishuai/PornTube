package com.buguagaoshu.tiktube.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.buguagaoshu.tiktube.dao.OSSConfigDao;
import com.buguagaoshu.tiktube.entity.OSSConfigEntity;
import com.buguagaoshu.tiktube.entity.WebConfigEntity;
import com.buguagaoshu.tiktube.exception.OssConfigException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.util.List;

/**
 * OSS配置服务实现类
 * 
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-30
 */
@Service
@Slf4j
public class OssConfigService extends ServiceImpl<OSSConfigDao, OSSConfigEntity> {

    /**
     * 保存OSS配置
     * 禁用状态的配置不做连通性校验，允许先存着以后再启用
     *
     * @param ossConfig OSS配置信息
     * @return 保存结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOssConfig(OSSConfigEntity ossConfig) {
        long now = System.currentTimeMillis();
        if (ossConfig.getCreateTime() == null) {
            ossConfig.setCreateTime(now);
        }
        ossConfig.setUpdateTime(now);

        if (isEnabled(ossConfig) && !validateOssConfig(ossConfig)) {
            throw new OssConfigException("启用失败：对象存储连通性校验未通过，请检查端点、访问密钥与存储桶是否存在");
        }

        boolean saved = this.save(ossConfig);
        if (saved && isEnabled(ossConfig)) {
            // 同时只能启用一个对象存储，启用新配置时自动停用其它配置
            disableOtherConfig(ossConfig.getId());
        }
        return saved;
    }

    /**
     * 更新OSS配置
     * 把某条配置置为启用时，其它配置会在同一事务内自动停用
     *
     * @param ossConfig OSS配置信息
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOssConfig(OSSConfigEntity ossConfig) {
        if (ossConfig.getId() == null) {
            throw new OssConfigException("缺少配置ID，无法更新");
        }
        if (this.getById(ossConfig.getId()) == null) {
            throw new OssConfigException("配置不存在，可能已被删除");
        }
        ossConfig.setUpdateTime(System.currentTimeMillis());

        if (isEnabled(ossConfig) && !validateOssConfig(ossConfig)) {
            throw new OssConfigException("启用失败：对象存储连通性校验未通过，请检查端点、访问密钥与存储桶是否存在");
        }

        boolean updated = this.updateById(ossConfig);
        if (updated && isEnabled(ossConfig)) {
            disableOtherConfig(ossConfig.getId());
        }
        return updated;
    }

    /**
     * 删除OSS配置
     * 启用中的配置需要先停用，避免正在使用的存储被误删
     *
     * @param id 配置ID
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOssConfig(Integer id) {
        OSSConfigEntity ossConfig = this.getById(id);
        if (ossConfig == null) {
            throw new OssConfigException("配置不存在，可能已被删除");
        }
        if (isEnabled(ossConfig)) {
            throw new OssConfigException("该配置正在启用中，请先停用后再删除");
        }
        return this.removeById(id);
    }

    /**
     * 是否为启用状态的配置，status 为空按禁用处理
     * */
    private static boolean isEnabled(OSSConfigEntity ossConfig) {
        return Integer.valueOf(1).equals(ossConfig.getStatus());
    }

    /**
     * 停用除 keepId 之外的其它配置，保证同时只有一条启用
     * */
    private void disableOtherConfig(Integer keepId) {
        UpdateWrapper<OSSConfigEntity> wrapper = new UpdateWrapper<>();
        wrapper.eq("status", 1)
                .ne("id", keepId)
                .set("status", 0)
                .set("update_time", System.currentTimeMillis());
        this.update(wrapper);
    }

    /**
     * 根据ID获取OSS配置
     *
     * @param id 配置ID
     * @return OSS配置信息
     */
    public OSSConfigEntity getOssConfigById(Integer id) {
        return this.getById(id);
    }

    /**
     * 获取所有OSS配置
     *
     * @return OSS配置列表
     */
    public List<OSSConfigEntity> listAllOssConfigs() {
        QueryWrapper<OSSConfigEntity> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("update_time");
        return this.list(wrapper);
    }


    /**
     * 验证OSS配置是否可用
     * 检查存储桶是否存在
     *
     * @param ossConfig OSS配置信息
     * @return 验证结果
     */
    public boolean validateOssConfig(OSSConfigEntity ossConfig) {
        try (S3Client s3Client = buildS3Client(ossConfig)) {
            s3Client.headBucket(
                    HeadBucketRequest.builder()
                            .bucket(ossConfig.getBucketName())
                            .build()
            );
            return true;
        } catch (NoSuchBucketException e) {
            log.warn("存储桶不存在: {}", ossConfig.getBucketName());
            return false;
        } catch (Exception e) {
            log.error("验证OSS配置失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 构建 S3 客户端
     *
     * @param ossConfig OSS配置信息
     * @return S3Client实例
     */
    public S3Client buildS3Client(OSSConfigEntity ossConfig) {
        S3ClientBuilder builder = S3Client.builder()
                .region(regionOf(ossConfig))
                .credentialsProvider(credentialsProviderOf(ossConfig))
                .forcePathStyle(isPathStyleAccess(ossConfig));

        URI endpoint = endpointUriOf(ossConfig.getEndpoint());
        if (endpoint != null) {
            builder.endpointOverride(endpoint);
        }

        return builder.build();
    }

    /**
     * 构建 S3 预签名客户端
     *
     * @param ossConfig OSS配置信息
     * @return S3Presigner实例
     */
    public S3Presigner buildS3Presigner(OSSConfigEntity ossConfig) {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(regionOf(ossConfig))
                .credentialsProvider(credentialsProviderOf(ossConfig))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(isPathStyleAccess(ossConfig))
                        .build());

        URI endpoint = endpointUriOf(ossConfig.getEndpoint());
        if (endpoint != null) {
            builder.endpointOverride(endpoint);
        }

        return builder.build();
    }

    /**
     * 未配置地区时按 AWS 惯例回落到 us-east-1，S3 兼容服务普遍接受该值
     * */
    private static Region regionOf(OSSConfigEntity ossConfig) {
        String region = ossConfig.getRegion();
        return (region == null || region.isBlank()) ? Region.US_EAST_1 : Region.of(region);
    }

    private static AwsCredentialsProvider credentialsProviderOf(OSSConfigEntity ossConfig) {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(ossConfig.getAccessKey(), ossConfig.getSecretKey()));
    }

    /**
     * pathStyleAccess：0 或未配置使用路径风格，1 使用虚拟主机风格
     * */
    private static boolean isPathStyleAccess(OSSConfigEntity ossConfig) {
        return ossConfig.getPathStyleAccess() == null || ossConfig.getPathStyleAccess() == 0;
    }

    /**
     * 配置里可能存成 play.min.io 或 http://127.0.0.1:9000/，AWS SDK 只接受带协议的绝对 URI
     * 端点为空时返回 null，表示使用真实的 AWS S3 端点
     * */
    private static URI endpointUriOf(String endpoint) {
        if (endpoint == null || endpoint.isBlank()) {
            return null;
        }
        String value = endpoint.trim();
        if (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            value = "https://" + value;
        }
        return URI.create(value);
    }
}
