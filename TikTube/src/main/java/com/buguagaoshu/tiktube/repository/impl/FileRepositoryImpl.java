package com.buguagaoshu.tiktube.repository.impl;

import com.buguagaoshu.tiktube.config.WebConfig;
import com.buguagaoshu.tiktube.entity.FileTableEntity;
import com.buguagaoshu.tiktube.enums.FileStatusEnum;
import com.buguagaoshu.tiktube.enums.FileTypeEnum;
import com.buguagaoshu.tiktube.repository.FileRepository;
import com.buguagaoshu.tiktube.repository.FileStorageRouter;
import com.buguagaoshu.tiktube.service.FileTableService;
import com.buguagaoshu.tiktube.utils.FFmpegUtils;
import com.buguagaoshu.tiktube.utils.FileUtils;
import com.buguagaoshu.tiktube.vo.VditorFiles;
import com.buguagaoshu.tiktube.vo.VideoInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-06 14:24
 */
@Repository
@Slf4j
public class FileRepositoryImpl implements FileRepository {

    /**
     * 文章附件（Vditor 上传）单文件大小上限
     */
    private static final long MAX_ARTICLE_SIZE = 20 * 1024 * 1024;

    /**
     * 头像与首页大图单文件大小上限，与 FileController 的校验值一致
     */
    private static final long MAX_PROFILE_IMAGE_SIZE = 2048000;

    /**
     * 单次请求处理的文件数上限，避免一次提交海量文件把磁盘和请求线程占满
     */
    private static final int MAX_FILES_PER_REQUEST = 20;

    private final Path location;
    private final FileTableService fileTableService;

    private final FileStorageRouter fileStorageRouter;

    @Autowired
    public FileRepositoryImpl(FileTableService fileTableService,
                              FileStorageRouter fileStorageRouter) throws IOException {
        this.fileStorageRouter = fileStorageRouter;
        this.location = Paths.get(FileTypeEnum.ROOT);
        this.fileTableService = fileTableService;
        if (Files.notExists(this.location)) {
            Files.createDirectories(this.location);
        }
    }

    @Override
    public VditorFiles vditorFileSave(MultipartFile[] files, Long userId) {
        Map<String, Object> succMap = new HashMap<>(4);
        List<String> errFiles = new ArrayList<>();
        String msg = "上传成功";

        if (files == null || files.length == 0) {
            msg = "上传失败，没有收到文件";
        } else if (userId == null) {
            msg = "上传失败，登录状态已失效";
        } else if (files.length > MAX_FILES_PER_REQUEST) {
            msg = "上传失败，单次最多上传 " + MAX_FILES_PER_REQUEST + " 个文件";
        } else {
            // 存储位置与日期目录在一次请求内取一次，避免中途后台改了配置导致落盘目录与落库地址不一致
            int saveLocation = WebConfig.FILE_SAVE_LOCATION;
            String path = saveLocation == 0 ? FileTypeEnum.filePath() : FileTypeEnum.ossFilePath();
            for (MultipartFile file : files) {
                String originalName = originalName(file);
                if (file == null || file.isEmpty()) {
                    errFiles.add(originalName);
                    continue;
                }
                if (file.getSize() > MAX_ARTICLE_SIZE) {
                    errFiles.add(originalName);
                    continue;
                }
                // 文章附件走的是白名单，.html/.svg/.js 这类能在浏览器里当文档执行的类型一律拒绝
                String suffix = FileTypeEnum.getFileSuffix(originalName);
                if (!FileTypeEnum.isArticleAttachment(suffix)) {
                    log.info("文章附件后缀不在白名单，已拒绝: 原名 {}，后缀 {}", originalName, suffix);
                    errFiles.add(originalName);
                    continue;
                }
                FileTableEntity saved = save(file, suffix, path, FileTypeEnum.ARTICLE.getCode(), userId, saveLocation);
                if (saved == null) {
                    errFiles.add(originalName);
                } else {
                    // 同名文件在一次提交里会互相覆盖映射，Vditor 按原名取回 URL，取最后一个即可
                    succMap.put(originalName, saved.getFileUrl());
                }
            }
            if (!errFiles.isEmpty()) {
                msg = succMap.isEmpty() ? "上传失败，请检查文件大小与格式" : "部分文件上传失败";
            }
        }

        Map<String, Object> data = new HashMap<>(4);
        data.put("succMap", succMap);
        data.put("errFiles", errFiles);

        VditorFiles vditorFiles = new VditorFiles();
        // 固定返回 0：Vditor 只按 code 判断请求是否被受理，逐文件的成败由 succMap / errFiles 表达
        vditorFiles.setCode(0);
        vditorFiles.setMsg(msg);
        vditorFiles.setData(data);
        return vditorFiles;
    }

    @Override
    public List<FileTableEntity> videoAndPhotoSave(MultipartFile[] files, Integer type, Long userId) throws FileNotFoundException {
        List<FileTableEntity> list = new ArrayList<>();
        if (files == null || files.length == 0 || files.length > MAX_FILES_PER_REQUEST) {
            return list;
        }
        if (type == null || userId == null) {
            throw new FileNotFoundException("上传参数有误，请重新登录后再试！");
        }

        // 头像与首页大图的尺寸限制（图片投稿通道不限大小，交给上层与容器配置）
        if (FileTypeEnum.AVATAR.getCode() == type || FileTypeEnum.TOP_IMAGE.getCode() == type) {
            for (MultipartFile file : files) {
                if (file != null && file.getSize() > MAX_PROFILE_IMAGE_SIZE) {
                    return null;
                }
            }
        }

        // 先把所有文件的后缀校验完再落盘：原实现边存边校验，请求里最后一个文件不合法时，
        // 前面已经存好的文件既没写库也没人回收，会留在磁盘上变成孤儿文件
        String[] suffixes = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            String suffix = FileTypeEnum.getFileSuffix(originalName(files[i]));
            if (!isAllowedFileType(type, suffix)) {
                throw new FileNotFoundException(FileTypeEnum.checkPhotoType(type)
                        ? "上传文件格式不正确，需要图片格式" : "上传文件格式不正确，需要视频格式");
            }
            suffixes[i] = suffix;
        }

        int saveLocation = WebConfig.FILE_SAVE_LOCATION;
        String path = saveLocation == 0 ? FileTypeEnum.filePath() : FileTypeEnum.ossFilePath();

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            if (file == null || file.isEmpty()) {
                continue;
            }
            FileTableEntity saved = save(file, suffixes[i], path, type, userId, saveLocation);
            if (saved != null) {
                list.add(saved);
            }
        }

        return list;
    }

    /**
     * 后缀是否与该上传通道的类型匹配：图片区只收图片，其余通道只收视频
     */
    private boolean isAllowedFileType(int type, String suffix) {
        if (FileTypeEnum.checkPhotoType(type)) {
            return FileTypeEnum.getFileType(suffix) == FileTypeEnum.PHOTO;
        }
        return FileTypeEnum.getFileType(suffix) == FileTypeEnum.VIDEO;
    }

    /**
     * 存储单个文件并写入文件表
     *
     * <p>顺序为「解析视频信息 → 落存储 → 写库」：解析放在最前面，坏文件不会白占磁盘；
     * 写库失败则把已经写入的文件删掉，否则清理任务只按数据库记录回收，这个文件会永久残留。
     *
     * @param suffix 已通过白名单校验的后缀
     * @return 成功返回已落库的文件实体，任何一步失败返回 null
     */
    private FileTableEntity save(MultipartFile file, String suffix, String path,
                                 int type, Long userId, int saveLocation) {
        String filename = FileTypeEnum.newFilename(suffix);
        FileTableEntity fileTableEntity = FileUtils.createFileTableEntity(filename, suffix, path, file.getSize(),
                file.getOriginalFilename(), userId, type, saveLocation);
        boolean stored = false;

        try {
            // 视频与图片共用同一套存储流程，视频额外多做一步信息解析
            if (FileTypeEnum.VIDEO.getCode() == type && !fillVideoInfo(file, fileTableEntity)) {
                return null;
            }

            // 具体存到本地还是对象存储由策略决定
            stored = fileStorageRouter.store(file, path, filename) != null;
            if (!stored) {
                return null;
            }

            if (!fileTableService.save(fileTableEntity)) {
                throw new IllegalStateException("文件表未写入任何记录");
            }
            return fileTableEntity;
        } catch (Exception e) {
            log.error("文件上传失败，用户ID：{}，文件名：{}，错误：{}", userId, file.getOriginalFilename(), e.getMessage(), e);
            if (stored) {
                deleteFile(fileTableEntity);
                log.warn("文件表写入失败，已回滚存储文件: {}", fileTableEntity.getFileUrl());
            }
            return null;
        }
    }

    /**
     * 解析视频信息并回填实体
     *
     * @return 解析成功返回 true
     */
    private boolean fillVideoInfo(MultipartFile videoFile, FileTableEntity fileTableEntity) {
        VideoInfo videoInfo = FFmpegUtils.getVideoInfoFromMultipartFile(videoFile);
        if (videoInfo == null) {
            log.warn("视频文件无法解析: {}", fileTableEntity.getFileNewName());
            return false;
        }

        fileTableEntity.setDuration(videoInfo.getDuration());
        fileTableEntity.setHeight(videoInfo.getHeight());
        fileTableEntity.setWidth(videoInfo.getWidth());
        fileTableEntity.setPixelsNumber((long) videoInfo.getWidth() * videoInfo.getHeight());
        fileTableEntity.setFrameRate(videoInfo.getFrameRate());
        fileTableEntity.setInfo(videoInfo.toJson());
        return true;
    }

    /**
     * 上传文件名可能为空（客户端不发送 filename），统一取一个用于回显与校验的名称
     */
    private String originalName(MultipartFile file) {
        if (file == null) {
            return "";
        }
        String name = file.getOriginalFilename();
        return name == null ? "" : name;
    }

    @Override
    public Path load(String filePath) throws FileNotFoundException {
        return FileUtils.resolveSafely(location, filePath);
    }

    @Override
    public boolean deleteFileWithDatabase(FileTableEntity fileTableEntity) {
        if (fileTableEntity == null || fileTableEntity.getId() == null) {
            return false;
        }

        FileTableEntity file = fileTableService.getById(fileTableEntity.getId());
        if (file == null || file.getArticleId() != null) {
            // 记录不存在，或仍被稿件引用，都不能删
            return false;
        }

        if (deleteFile(file)) {
            return fileTableService.removeById(file.getId());
        }
        // 文件已经删不掉、且数据库里早已标记为删除（文件管理里删过一遍），把这条记录一并清掉
        if (Objects.equals(file.getStatus(), FileStatusEnum.DELETE.getCode())) {
            log.info("文件无法在文件系统中删除，只会在数据库中删除！: {}", file.getFileUrl());
            return fileTableService.removeById(file.getId());
        }
        log.warn("文件删除失败，保留数据库记录: id {}, url {}", file.getId(), file.getFileUrl());
        return false;
    }

    @Override
    public boolean deleteFile(FileTableEntity fileTableEntity) {
        if (fileTableEntity == null || fileTableEntity.getFileUrl() == null) {
            return false;
        }

        try {
            // 按文件保存时使用的存储位置路由到对应策略
            return fileStorageRouter.delete(fileTableEntity.getFileUrl(), fileTableEntity.getSaveLocation());
        } catch (Exception e) {
            log.warn("删除文件失败: {}, 原因: {}", fileTableEntity.getFileUrl(), e.getMessage());
            return false;
        }
    }
}
