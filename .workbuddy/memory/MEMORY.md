# TikTube 项目长期记忆

## 项目结构
- 后端 `TikTube/`：Spring Boot 4.1 + MyBatis-Plus 3.5 + Java 25 + Maven（离线 `mvn -o`）
- 前端 `TikTubeWeb/`：Vue 3.5 + Vuetify 3.8 + Pinia + vue-router，构建 `vite`
- 数据库：`tik_tube.sql`（建表），增量改动放根目录 `upgrade-YYYY-MM-DD-*.sql`

## 构建命令（有坑，直接记）
- 后端：`mvn -o -q -DskipTests compile` / `mvn -o test`（26 个测试）
- 前端：`npx vite build --emptyOutDir false`
  —— **必须加 `--emptyOutDir false`**，否则 Vite 清空 `dist/`（几百个文件）会触发批量删除保护导致构建失败
- 前端 lint：`npx eslint <路径>`

## 代码约定
- 新前端组件一律 `<script setup>` + Composition API
- 请求走 `@/api/index`（Promise 化）；列表走 `composables/useTable`；
  提示走 `composables/useMessage` + `<AppSnackbar>`；数字格式化走 `utils/format.js`
- 后端接口返回一律 `ResponseDetails.ok().put("data", x)`
- 排序字段必须走 Java 侧白名单，禁止把用户输入拼进 ORDER BY（M8 安全整改口径）
- 当前用户用 `support/CurrentUserResolver` 或 `JwtUtil.getUserId(request)`

## Vuetify 3 踩坑（本项目已遇到）
- **`v-list-item` 不要在同一个 `v-list` 里重复用相同的 `to`**：
  `VListItem` 的嵌套注册表 ID = `props.value ?? link.href`，相同 `to` 会触发
  `Vuetify error: Multiple nodes with the same ID`。解决：显式给 `:value="唯一键"`。
  不传 `to`/`href` 时 ID 是唯一 Symbol，不会冲突。
- **`v-sparkline` 的 `gradient` / `color` 不能传主题名或 CSS 变量**：
  它们会被原样写进 SVG 的 `stop-color` / `stroke` **属性**，而 SVG 呈现属性不支持 `var()`。
  要用 `useTheme()` 取真实色值，再用 `utils/studio.js` 的 `withAlpha()` 转 rgba。
- **`v-sparkline` 没有悬停交互**（只有静态 `show-labels`）。需要「鼠标移上去显示日期+数值」
  时直接用 `components/studio/TrendSparkline.vue`：它在 sparkline 上叠一层命中层 + `v-tooltip`，
  按 VTrendline 的坐标公式 `x_i = padding + i*(width-2*padding)/(n-1)` 切热区。
- `useTable()` 返回的是 `reactive()`，模板里直接用 `table.items` / `table.page` / `table.loading`，
  **不要写 `.value`**。
- rail 模式下的 `v-navigation-drawer` 不会自动给图标加 tooltip，需要自己用 `v-tooltip` 包 `v-list-item`。

## 创作中心统计口径（2026-10-12 落地）
- `article` 表只有累计计数、没有时间维度；区间新增一律取事件表 `create_time`
- `play_recording` 是「用户 × 分P」唯一一条、重复观看只更新 `update_time`
  → 按天统计得到的是**观看人数（去重）**，不是播放次数
- 按天分桶：`(时间 + tzOffset) DIV dayMillis`，tzOffset = `TimeZone.getDefault().getRawOffset()`
- 只统计 `status = 0` 的稿件
