<div align="center">
    <img src="/img/logo.png" alt="logo" title="logo" width="50%" style="text-align:center;">
</div>

<div align="center">
    <a href="/README.md">English</a>
</div>

# TikTube 一个能发弹幕的简单的视频网站


## 简介 

一个简单的视频网站

网站名称是 TikTok 与 YouTube 的缝合，Logo 为豆包 AI 生成

主要界面参考了 [Youtube](https://www.youtube.com/)，部分功能借鉴了 [哔哩哔哩](https://www.bilibili.com/)

后端 Spring Boot 4， MySQL

前端 Vue 3， Vuetify 3（Vite 构建）

已经完成所有核心功能，比如：

- 视频投稿
- 文章（文本稿件）投稿，支持多段落上传与标签分类展示
- 弹幕
- 视频播放
- 播放历史，收藏，评论，点赞
- 自动生成封面图，封面手动上传
- 创作者中心数据统计
- 数据管理
- 登录 TOTP 两步验证
- 配置多存储库，支持兼容 S3 API 的对象存储已测试：MinIO，Cloudflare R2- 
- 举报，内容审核（配置大模型后可以实现AI自动内容审核）
- 公告，消息通知等

还剩一些细节功能在逐渐优化中

为了使用与部署方便，唯一外部依赖只有数据库，可选配置为 Redis

通过设置 `application.yml` 中 `open-redis` 选项为 true，开启 Redis 缓存


## 在线体验

关于在线 DEMO: [https://tiktube.buguagaoshu.com/](https://tiktube.buguagaoshu.com/)

*该 DEMO 该版本仅为展示系统，故管理员关闭了普通用户的投稿，评论功能，如需要体验这两功能，请自行部署体验！*

测试账号为：test@test.com

密码：test123456test

**另外，你也可以注册自己的账号体验**

## 视频演示


哔哩哔哩：<a href="https://www.bilibili.com/video/BV1AV59z5ESV" target="_blank">https://www.bilibili.com/video/BV1AV59z5ESV</a>


## 截图

### 主页 

<img src="/img/home.jpg" title="首页" alt="首页">

### 播放页

<img src="/img/video.jpg" title="播放页" alt="播放页">

### 评论

<img src="/img/comment.jpg" title="评论" alt="评论">

### 播放历史

<img src="/img/history.jpg" title="历史记录" alt="历史记录">

### 订阅

<img src="/img/subscribe.jpg" title="订阅" alt="订阅">

### 用户主页

<img src="/img/user.jpg" title="个人主页" alt="个人主页">

### 消息通知

<img src="/img/notification.jpg" title="消息通知" alt="消息通知">


### 创作者中心

<img src="/img/data.jpg" title="studio" alt="studio">

### 投稿

<img src="/img/publish.jpg" title="投稿" alt="投稿">

#### 稿件自动截图

<img src="/img/Capture.png" title="视频自动截图" alt="视频自动截图">

### ADMIN

<img src="/img/admin.png" title="ADMIN" alt="ADMIN">

### AI 大模型自动内容审核

<img src="/img/ai.png" title="AI 大模型自动内容审核" alt="AI 大模型自动内容审核">


## 快速运行

**运行环境: Java25, Node 20+, Maven 3.9+， MySQL 8.0+**

使用 tik_tube.sql 创建数据库，配置数据库地址

已有旧版数据库时，可按 `SQL_upgrade` 目录中的升级脚本增量升级表结构

如果你有 Reids 服务，可以通过设置 `application.yml` 中 `open-redis` 选项为 true，此时系统将使用 Redis 缓存

该选项默认为 false，使用系统缓存

**运行后端服务**

```bash
cd TikTube
mvn clean package
```

**之后**

```bash
java -jar target/tiktube-*
```

生产部署（Linux x86_64）可使用打包脚本，自动剔除 javacv 中非 x86_64 的 native 库：

```bash
./package-linux-x86_64.sh
```

**运行前端服务**

```bash
cd TikTubeWeb
npm install
```

**之后**

```bash
npm run dev
```


**最后打开**


```
http://127.0.0.1:5173
```

**提示：** 第一个以admin为用户名注册的用户将自动成为管理员！


**关闭服务器之间请先到管理后台同步缓存数据，避免数据丢失！**


## 更新

[更新日志](/CHANGELOG.md)


## 其它地址

GitHub：https://github.com/PuZhiweizuishuai/TikTube

码云: https://gitee.com/puzhiweizuishuai/VideoWeb

## AI 开发建议

建议使用 AI 修改项目前，将分支修改到 ai 分支，待测试运行无误后再修改主分支

```
git checkout ai
git push origin ai
git checkout master
git merge ai
git push origin master
git checkout ai
```