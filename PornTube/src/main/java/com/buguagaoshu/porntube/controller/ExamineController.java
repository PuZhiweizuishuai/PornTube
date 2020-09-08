package com.buguagaoshu.porntube.controller;

import com.buguagaoshu.porntube.dto.ExamineDto;
import com.buguagaoshu.porntube.enums.ExamineTypeEnum;
import com.buguagaoshu.porntube.service.ArticleService;
import com.buguagaoshu.porntube.vo.ResponseDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2020-09-08 19:06
 */
@RestController
public class ExamineController {

    private final ArticleService articleService;

    @Autowired
    public ExamineController(ArticleService articleService) {
        this.articleService = articleService;
    }


    @GetMapping("/api/examine/item")
    public ResponseDetails examineTypeItem() {

        List<String> item = new ArrayList<>();
        for (ExamineTypeEnum e : ExamineTypeEnum.values()) {
            item.add(e.getMsg());
        }
        return ResponseDetails.ok().put("data", item);
    }


    @PostMapping("/api/admin/examine")
    public ResponseDetails examine(@Valid @RequestBody ExamineDto examineDto,
                                   HttpServletRequest request) {
        System.out.println(examineDto);
        return ResponseDetails.ok(articleService.examine(examineDto, request));
    }

}
