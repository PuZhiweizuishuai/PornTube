<div align="center">
    <img src="/img/logo.png" alt="logo" title="logo" width="50%" style="text-align:center;">
</div>

# PornTube 一个能发弹幕的简单的视频网站


## 截图

### 主页 

<img src="/img/home.png" title="首页" alt="首页">

### 播放页

<img src="/img/play.jpg" title="播放页" alt="播放页">

### 播放历史

<img src="/img/history.png" title="历史记录" alt="历史记录">

### 用户主页

<img src="/img/user.jpg" title="个人主页" alt="个人主页">

### 投稿

<img src="/img/publish01.png" title="投稿" alt="投稿">


## 简介 

一个简单的视频网站

主要界面参考了Youtube，部分功能借鉴了B站

后端 Spring Boot， Mysql，前端 Vue， Vuetifyjs

正在不断更新开发中，已经完成部分功能

比如视频投稿，发送弹幕，视频播放，播放历史, 自动生成封面图等


## 快速运行

**运行环境: Java17, Node 20, Maven3.9**

使用 porn_tube.sql 创建数据库，配置数据库地址

**运行后端服务**

```bash
cd PornTube
mvn clean package
```

**之后**

```bash
java -jar target/porntube-0.1.2-SNAPSHOT
```

**运行前端服务**

```bash
cd web
npm install
```

**之后**

```bash
npm run serve
```


**最后打开**


```
http://127.0.0.1:8000
```

**提示：** 第一个以admin为用户名注册的用户将自动成为管理员！


### 前端无法编译

如果出现 `ESLint is not a constructor` 的错误，请在 `vue.config.js` 中添加：`lintOnSave: false`


## 更新

[更新日志](/CHANGELOG.md)


## 免责声明

本项目的名称与 Logo 设计灵感来源于某知名网站，仅为趣味性参考，严禁将本项目用于任何非法用途。

若用户擅自使用本项目搭建与某知名网站功能相似的平台，由此产生的一切责任与后果均与开发者无关，开发者概不负责。
