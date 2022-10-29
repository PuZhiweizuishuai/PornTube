<div align="center">
    <img src="https://ae04.alicdn.com/kf/Ue5827dbcd7cd47f9aa05e3d528e56d7aO.jpg" alt="logo" title="logo" width="50%" style="text-align:center;">
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

<img src="https://ae04.alicdn.com/kf/U94c7f938e2d14c2c81c9e4af25ee94966.jpg" title="播放页" alt="播放页">


### history

<img src="/img/history.png" title="历史记录" alt="历史记录">

### User Home

<img src="https://sc01.alicdn.com/kf/U2052324455ff4565a98489a4cc0975f8Y.jpg" title="个人主页" alt="个人主页">

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

前端无法编译

如果出现 `ESLint is not a constructor` 的错误，请在 `vue.config.js` 中添加：`lintOnSave: false`

**Tips:** The first user registered with the name admin will automatically become an administrator.

## CHANGE LOG

[CHANGELOG](/CHANGELOG.md)


