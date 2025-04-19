package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.entity.NotificationEntity;
import com.buguagaoshu.tiktube.service.NotificationService;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create  2025-04-19
 */
@RestController
@RequestMapping("/api")
public class NotificationController {
    final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }


    @GetMapping("/notification/get")
    public ResponseDetails getNotification(@RequestParam Map<String, Object> params,
                                           HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);
        return ResponseDetails.ok().put("data", notificationService.queryNotification(params, userId));
    }

    @PostMapping("/notification/read")
    public ResponseDetails read(@RequestBody NotificationEntity notification,
                                HttpServletRequest request) {
        return ResponseDetails.ok().put("data",
                notificationService.readNotification(notification, JwtUtil.getUserId(request)));
    }


    @PostMapping("/notification/readAll")
    public ResponseDetails readAll(HttpServletRequest request) {
        return ResponseDetails.ok().put("data", notificationService.readAllNotification(JwtUtil.getUserId(request)));
    }

    @GetMapping("/notification/count")
    public ResponseDetails count(HttpServletRequest request) {
        return ResponseDetails.ok().put("data", notificationService.countNotification(JwtUtil.getUserId(request)));
    }
}
