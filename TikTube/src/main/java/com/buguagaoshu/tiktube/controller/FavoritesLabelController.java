package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.entity.FavoritesLabelEntity;
import com.buguagaoshu.tiktube.enums.ReturnCodeEnum;
import com.buguagaoshu.tiktube.service.FavoritesLabelService;
import com.buguagaoshu.tiktube.service.FavoritesTableService;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 收藏夹分组
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * @create 2026-10-14
 */
@RestController
@RequestMapping("/api/favorites")
public class FavoritesLabelController {
    final FavoritesLabelService favoritesLabelService;

    final FavoritesTableService favoritesTableService;

    @Autowired
    public FavoritesLabelController(FavoritesLabelService favoritesLabelService,
                                    FavoritesTableService favoritesTableService) {
        this.favoritesLabelService = favoritesLabelService;
        this.favoritesTableService = favoritesTableService;
    }

    /**
     * 当前用户的收藏夹列表（含数量）
     * */
    @GetMapping("/label/list")
    public ResponseDetails labelList(HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);
        return ResponseDetails.ok().put("data", favoritesLabelService.listWithCount(userId));
    }

    /**
     * 新建收藏夹
     * */
    @PostMapping("/label/save")
    public ResponseDetails saveLabel(@RequestBody FavoritesLabelEntity label, HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);
        String name = checkName(label.getName());
        if (name == null) {
            return ResponseDetails.ok(ReturnCodeEnum.DATA_VALID_EXCEPTION.getCode(), "收藏夹名称需为 1-30 个字符");
        }
        if (favoritesLabelService.existsName(userId, name, null)) {
            return ResponseDetails.ok(ReturnCodeEnum.DATA_VALID_EXCEPTION.getCode(), "已有同名收藏夹");
        }
        FavoritesLabelEntity entity = new FavoritesLabelEntity();
        entity.setName(name);
        entity.setUserId(userId);
        entity.setIsPublic(Integer.valueOf(1).equals(label.getIsPublic()) ? 1 : 0);
        entity.setIsDefault(0);
        entity.setCreateTime(System.currentTimeMillis());
        favoritesLabelService.save(entity);
        return ResponseDetails.ok().put("data", entity);
    }

    /**
     * 重命名 / 修改是否公开
     * */
    @PostMapping("/label/update")
    public ResponseDetails updateLabel(@RequestBody FavoritesLabelEntity label, HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);
        if (label.getId() == null) {
            return ResponseDetails.ok(ReturnCodeEnum.LACK_ID);
        }
        FavoritesLabelEntity one = favoritesLabelService.getById(label.getId());
        if (one == null || !one.getUserId().equals(userId)) {
            return ResponseDetails.ok(ReturnCodeEnum.NO_POWER);
        }
        String name = checkName(label.getName());
        if (name == null) {
            return ResponseDetails.ok(ReturnCodeEnum.DATA_VALID_EXCEPTION.getCode(), "收藏夹名称需为 1-30 个字符");
        }
        if (favoritesLabelService.existsName(userId, name, one.getId())) {
            return ResponseDetails.ok(ReturnCodeEnum.DATA_VALID_EXCEPTION.getCode(), "已有同名收藏夹");
        }
        one.setName(name);
        one.setIsPublic(Integer.valueOf(1).equals(label.getIsPublic()) ? 1 : 0);
        favoritesLabelService.updateById(one);
        return ResponseDetails.ok().put("data", one);
    }

    /**
     * 删除收藏夹，其中收藏移入默认收藏夹；默认收藏夹不可删除
     * */
    @PostMapping("/label/delete")
    public ResponseDetails deleteLabel(@RequestBody FavoritesLabelEntity label, HttpServletRequest request) {
        long userId = JwtUtil.getUserId(request);
        if (label.getId() == null) {
            return ResponseDetails.ok(ReturnCodeEnum.LACK_ID);
        }
        favoritesLabelService.deleteLabel(userId, label.getId());
        return ResponseDetails.ok();
    }

    /**
     * 游客可访问：某用户公开的收藏夹列表（在 LoginInterceptor 白名单内）
     * */
    @GetMapping("/label/public/list")
    public ResponseDetails publicLabelList(@RequestParam Long userId) {
        return ResponseDetails.ok().put("data", favoritesLabelService.publicLabelList(userId));
    }

    /**
     * 游客可访问：公开收藏夹内的收藏稿件（在 LoginInterceptor 白名单内）
     * */
    @GetMapping("/label/public/article/list")
    public ResponseDetails publicLabelArticleList(@RequestParam Long labelId,
                                                  @RequestParam Map<String, Object> params) {
        FavoritesLabelEntity label = favoritesLabelService.getById(labelId);
        if (label == null) {
            return ResponseDetails.ok(ReturnCodeEnum.NOO_FOUND);
        }
        if (!Integer.valueOf(1).equals(label.getIsPublic())) {
            return ResponseDetails.ok(ReturnCodeEnum.NO_POWER.getCode(), "该收藏夹未公开");
        }
        params.put("labelId", labelId);
        return ResponseDetails.ok().put("data", favoritesTableService.userFavorites(params, label.getUserId()));
    }

    private String checkName(String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty() || trimmed.length() > 30) {
            return null;
        }
        return trimmed;
    }
}
