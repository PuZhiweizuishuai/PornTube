<div align="center">
    <img src="http://p.ananas.chaoxing.com/star3/origin/5e008a6cd46ea6ed07252f656acb5d2b.png" alt="logo" title="logo" width="50%" style="text-align:center;">
</div>

# PornTube 一个能发弹幕的简单的视频网站


## 截图

### 主页 

<img src="http://p.ananas.chaoxing.com/star3/origin/a093b1ef4a410eea6bd616a2e985ac97.png" title="首页" alt="首页">

## 播放页

<img src="http://p.ananas.chaoxing.com/star3/origin/9d4efd81781cc3a8cf78c788b0cfc8f4.png" title="播放页" alt="播放页">

### 播放历史

<img src="http://p.ananas.chaoxing.com/star3/origin/496e8f398667a6238ffd2fa734e01a5e.png" title="历史记录" alt="历史记录">

## 用户主页

<img src="http://p.ananas.chaoxing.com/star3/origin/53580b104b00aebe98ae008ecaee41b6.png" title="个人主页" alt="个人主页">

### 投稿

<img src="http://p.ananas.chaoxing.com/star3/origin/1a0f4668b74041c843d16f71c842152e.png" title="投稿" alt="投稿">


## 简介 

一个简单的视频网站

主要界面参考了Youtube，部分功能借鉴了B站

后端 Spring Boot， Mysql，前端 Vue， Vuetifyjs

正在不断更新开发中，已经完成部分功能

比如视频投稿，发送弹幕，视频播放，播放历史等


## 快速运行

**运行环境: Java11, Node 14, Maven3**

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
