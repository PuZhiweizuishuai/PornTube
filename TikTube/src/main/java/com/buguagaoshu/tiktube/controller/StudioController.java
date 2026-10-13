package com.buguagaoshu.tiktube.controller;

import com.buguagaoshu.tiktube.service.CreatorStatsService;
import com.buguagaoshu.tiktube.utils.JwtUtil;
import com.buguagaoshu.tiktube.vo.CreatorDataSummary;
import com.buguagaoshu.tiktube.vo.CreatorOverview;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 创作中心
 *
 * 全部接口落在 /api/studio/**，已被 LoginInterceptor 覆盖（WebConfig 的排除名单里没有它），
 * 因此这里不需要再判登录，直接取 JWT 里的用户 ID 即可，也只返回本人的数据。
 *
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 */
@RestController
@RequestMapping("/api/studio")
public class StudioController {

    private final CreatorStatsService creatorStatsService;

    @Autowired
    public StudioController(CreatorStatsService creatorStatsService) {
        this.creatorStatsService = creatorStatsService;
    }

    /**
     * 信息中心总览：累计数据 + 今日/近 7 日新增 + 审核状态分布 + 未读通知 + 近 7 日趋势
     */
    @GetMapping("/overview")
    public ResponseDetails overview(HttpServletRequest request) {
        CreatorOverview overview = creatorStatsService.overview(JwtUtil.getUserId(request));
        return ResponseDetails.ok().put("data", overview);
    }

    /**
     * 数据分析：区间合计 + 上一周期合计（算环比）+ 每日趋势 + 峰值日
     *
     * @param days 统计天数，支持 7 / 30 / 90，其它值回落默认 30
     */
    @GetMapping("/data/summary")
    public ResponseDetails summary(@RequestParam(value = "days", required = false) Integer days,
                                   HttpServletRequest request) {
        CreatorDataSummary summary = creatorStatsService.summary(JwtUtil.getUserId(request), days);
        return ResponseDetails.ok().put("data", summary);
    }

    /**
     * 稿件表现榜
     *
     * @param sort 排序列，只接受 CreatorStatsService.ALLOWED_SORTS 里的值
     */
    @GetMapping("/data/articles")
    public ResponseDetails articles(@RequestParam Map<String, Object> params,
                                    @RequestParam(value = "days", required = false) Integer days,
                                    @RequestParam(value = "sort", required = false) String sort,
                                    HttpServletRequest request) {
        return ResponseDetails.ok().put("data",
                creatorStatsService.articleStats(params, JwtUtil.getUserId(request), days, sort));
    }

    /**
     * 分区分布：每个分区的稿件数、区间观看人数、累计播放
     */
    @GetMapping("/data/category")
    public ResponseDetails category(@RequestParam(value = "days", required = false) Integer days,
                                    HttpServletRequest request) {
        List<?> stats = creatorStatsService.categoryStats(JwtUtil.getUserId(request), days);
        return ResponseDetails.ok().put("data", stats);
    }
}
