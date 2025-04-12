package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.entity.FileTableEntity;
import com.buguagaoshu.tiktube.repository.FileRepository;
import com.buguagaoshu.tiktube.service.FileTableService;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2025-04-06
 */
@RestController
public class FileInfoController {
    private final FileTableService fileTableService;

    private final FileRepository fileRepository;

    @Autowired
    public FileInfoController(FileTableService fileTableService, FileRepository fileRepository) {
        this.fileTableService = fileTableService;
        this.fileRepository = fileRepository;
    }

    @GetMapping("/api/admin/files/list")
    public ResponseDetails list(@RequestParam Map<String, Object> params) {
        return ResponseDetails.ok().put("data", fileTableService.queryPage(params));
    }

    @PostMapping("/api/admin/files/delete")
    public ResponseDetails delete(@RequestBody FileTableEntity fileTableEntity) {
        return ResponseDetails.ok().put("data", fileRepository.deleteFileWithDatabase(fileTableEntity));
    }
}
