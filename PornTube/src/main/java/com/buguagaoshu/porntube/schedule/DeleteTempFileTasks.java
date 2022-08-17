package com.buguagaoshu.porntube.schedule;

import com.buguagaoshu.porntube.config.WebConstant;
import com.buguagaoshu.porntube.entity.FileTableEntity;
import com.buguagaoshu.porntube.enums.FileStatusEnum;
import com.buguagaoshu.porntube.repository.FileRepository;
import com.buguagaoshu.porntube.service.FileTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2022-06-05 1:31
 * 删除用户上传的未使用文件
 */
@Component
@Slf4j
public class DeleteTempFileTasks {
    private final FileRepository repository;

    private final FileTableService fileTableService;

    private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Autowired
    public DeleteTempFileTasks(FileRepository repository, FileTableService fileTableService) {
        this.repository = repository;
        this.fileTableService = fileTableService;
    }

    /**
     * 定时删除过期文件
     * 默认 6 小时一次
     * */
    @Scheduled(fixedRate = 21600000)
    public void deleteFile() {

        long endTime = System.currentTimeMillis() - WebConstant.DEPRECATED_FILE_TIME;
        int count = 100;
        long num = 0;

        log.info("开始删除 {} 之前的过期文件", SIMPLE_DATE_FORMAT.format(endTime));

        while (true) {
            List<FileTableEntity> fileTableEntities = fileTableService.deprecatedFileList(endTime, count);
            if (fileTableEntities.size() == 0) {
                break;
            }
            List<FileTableEntity> tempImg = new ArrayList<>();
            for (FileTableEntity file : fileTableEntities) {
                repository.deleteFile(file);
                file.setStatus(FileStatusEnum.DELETE.getCode());
                if ("系统生成截图".equals(file.getFileOriginalName())) {
                    tempImg.add(file);
                }
                num += 1;
            }
            fileTableService.updateBatchById(fileTableEntities);
            fileTableService.removeBatchByIds(tempImg);
        }

        log.info(" {} 之前的过期文件删除完成", SIMPLE_DATE_FORMAT.format(endTime));
        log.info("共删除 {} 条过期文件！", num);
    }
}
