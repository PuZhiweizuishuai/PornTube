package com.buguagaoshu.porntube.controller;

import com.buguagaoshu.porntube.config.DPlayerConstants;
import com.buguagaoshu.porntube.dto.DanmakuDto;
import com.buguagaoshu.porntube.enums.ReturnCodeEnum;
import com.buguagaoshu.porntube.service.DanmakuService;
import com.buguagaoshu.porntube.vo.ResponseDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;


/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-07 16:02
 */
@RestController
public class DanmakuController {

    private final DanmakuService danmakuService;

    @Autowired
    public DanmakuController(DanmakuService danmakuService) {
        this.danmakuService = danmakuService;
    }


    @GetMapping("/api/danmaku/v3")
    public ResponseDetails get(@RequestParam("id") Long id,
                               @RequestParam("max") Integer max) {
        return ResponseDetails.ok().put("data", danmakuService.danmakuList(id, max))
                .put("code", DPlayerConstants.DPLAYER_SUCCESS_CODE);
    }


    @PostMapping("/api/danmaku/v3")
    public ResponseDetails post(@RequestBody DanmakuDto danmakuDto,
                                HttpServletRequest request) {
        ReturnCodeEnum codeEnum = danmakuService.saveDanmaku(danmakuDto, request);
        if (codeEnum.equals(ReturnCodeEnum.NO_LOGIN)) {
            return ResponseDetails.ok(codeEnum).put("code", 1);
        }
        return ResponseDetails.ok(codeEnum)
                .put("code", DPlayerConstants.DPLAYER_SUCCESS_CODE);
    }

}
