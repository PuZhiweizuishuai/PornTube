<div align="center">
    <img src="/img/logo.png" alt="logo" title="logo" width="50%" style="text-align:center;">
</div>

<br/>

<div align="center">
    <a href="https://github.com/PuZhiweizuishuai/PornTube/blob/master/README_zh_CN.md">简体中文</a>
</div>

<br/>

# PornTube


## screenshot

### Home 

<img src="/img/home.png" title="首页" alt="首页">

### Video Page

<img src="/img/play.jpg" title="播放页" alt="播放页">


### history

<img src="/img/history.png" title="历史记录" alt="历史记录">

### User Home

<img src="/img/user.jpg" title="个人主页" alt="个人主页">

### publish

<img src="/img/publish01.png" title="投稿" alt="投稿">


## introduction 

A simple video website

Interface design reference Youtube

Adopted Spring Boot， Vue， vuetifyjs

Still in development. But most of the gong'n has been done.

for example, Video upload, danmaku, historical records, Auto generate cover image, etc.


## Quick start

**Run Environment: Java11, Node 14, Maven3**

Creating a database, Configuring Database Addresses

**Run back end server**

```bash
cd PornTube
mvn clean package
```

**Then**

```bash
java -jar target/porntube-0.1.2-SNAPSHOT
```

**Run front end server**

```bash
cd web
npm install
```

**Then**

```bash
npm run serve
```

**Final**

Open

```
http://127.0.0.1:8000
```


### Question

Frontend Fails to Compile

If you encounter the error `ESLint is not a constructor`, add `lintOnSave: false` to your `vue.config.js`.


**Tips:** The first user registered with the name admin will automatically become an administrator.

## CHANGE LOG

[CHANGELOG](/CHANGELOG.md)


## Disclaimer


The project name and logo are inspired by a well-known website, intended solely for fun and reference. Any use of this project for illegal purposes is strictly prohibited. If you utilize this project to build a platform with functionality similar to that of the aforementioned website, the developer shall not be held liable for any resulting consequences or responsibilities.

