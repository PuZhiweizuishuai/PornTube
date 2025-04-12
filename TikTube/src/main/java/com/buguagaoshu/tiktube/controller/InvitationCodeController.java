package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.service.InvitationCodeService;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-08 17:01
 */
@RestController
public class InvitationCodeController {

    private final InvitationCodeService invitationCodeService;

    @Autowired
    public InvitationCodeController(InvitationCodeService invitationCodeService) {
        this.invitationCodeService = invitationCodeService;
    }

    @GetMapping("/api/admin/invitation/list")
    public ResponseDetails list(@RequestParam Map<String, Object> params) {
        return ResponseDetails.ok().put("data", invitationCodeService.queryPage(params));
    }

    @PostMapping("/api/admin/invitation/create")
    public ResponseDetails create(HttpServletRequest request) {
        return ResponseDetails.ok(invitationCodeService.create(request));
    }
}
