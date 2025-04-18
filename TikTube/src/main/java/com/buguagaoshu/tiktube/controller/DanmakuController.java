package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.config.DPlayerConstants;
import com.buguagaoshu.tiktube.dto.ArtDanmakuDto;
import com.buguagaoshu.tiktube.dto.DanmakuDto;
import com.buguagaoshu.tiktube.entity.DanmakuEntity;
import com.buguagaoshu.tiktube.enums.ReturnCodeEnum;
import com.buguagaoshu.tiktube.service.DanmakuService;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;


/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-07 16:02
 * TODO 使用消息队列，拆分弹幕服务，达到读写分离
 */
@RestController
public class DanmakuController {

    private final DanmakuService danmakuService;

    @Autowired
    public DanmakuController(DanmakuService danmakuService) {
        this.danmakuService = danmakuService;
    }

    @GetMapping("/api/danmaku/v3/")
    public ResponseDetails get(@RequestParam("id") Long id,
                               @RequestParam("max") Integer max) {
        return ResponseDetails.ok().put("data", danmakuService.danmakuList(id, max))
                .put("code", DPlayerConstants.DPLAYER_SUCCESS_CODE);
    }


    @PostMapping("/api/danmaku/v3/")
    public ResponseDetails post(@RequestBody DanmakuDto danmakuDto,
                                HttpServletRequest request) {
        ReturnCodeEnum codeEnum = danmakuService.saveDanmaku(danmakuDto, request);
        if (codeEnum.equals(ReturnCodeEnum.NO_LOGIN)) {
            return ResponseDetails.ok(codeEnum).put("code", 1);
        }
        return ResponseDetails.ok(codeEnum)
                .put("code", DPlayerConstants.DPLAYER_SUCCESS_CODE);
    }

    @GetMapping("/api/danmaku/v1")
    public ResponseDetails getArt(@RequestParam("id") Long id) {
        return ResponseDetails.ok().put("data", danmakuService.artDanmakuList(id, 1000));
    }

    @PostMapping("/api/danmaku/v1")
    public ResponseDetails saveArt(@RequestBody ArtDanmakuDto danmakuDto,
                                   HttpServletRequest request) {
        ReturnCodeEnum codeEnum = danmakuService.saveArtDanmaku(danmakuDto, request);
        if (codeEnum.equals(ReturnCodeEnum.NO_LOGIN)) {
            return ResponseDetails.ok(codeEnum).put("code", 1);
        }
        return ResponseDetails.ok(codeEnum).put("code", 0);
    }

    /**
     * 获取所有弹幕列表（管理员接口）
     */
    @GetMapping("/api/admin/danmaku/list")
    public ResponseDetails getAllDanmaku(@RequestParam Map<String, Object> params) {
        return ResponseDetails.ok().put("data", danmakuService.getAllDanmaku(params));
    }


    /**
     * 切换弹幕状态（管理员接口）
     */
    @PostMapping("/api/admin/danmaku/toggle")
    public ResponseDetails toggleDanmakuStatus(@RequestBody DanmakuEntity danmakuEntity) {
        return ResponseDetails.ok().put("data", danmakuService.toggleDanmakuStatus(danmakuEntity.getId()));
    }

}
