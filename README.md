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

<img src="/img/home.png" title="Home" alt="Home">

### Video Page

<img src="/img/video.png" title="Video Page" alt="Video Page">

### Comment

<img src="/img/comment.png" title="Comment" alt="Comment">

### History

<img src="/img/history.png" title="History" alt="History">

### User Home

<img src="/img/user.png" title="User Home" alt="User Home">

### Publish

<img src="/img/publish.png" title="Publish" alt="Publish">

#### Video Capture

<img src="/img/Capture.png" title="Video Capture" alt="Video Capture">


### User Management

<img src="/img/admin_user.png" title="User Management" alt="User Management">


## introduction 

A simple video website

Interface design reference Youtube

Adopted Spring Boot， Vue， vuetifyjs

Still in development. But most of the gong'n has been done.

for example, Video upload, danmaku, historical records, Auto generate cover image, etc.

The web folder contains the legacy frontend interface. Please compile the new frontend interface using the project under PornTubeWeb.

## Quick start

**Run Environment: Java17, Node 20, Maven 3.9**

Creating a database, Configuring Database Addresses

**Run back end server**

```bash
cd PornTube
mvn clean package
```

**Then**

```bash
java -jar target/porntube-*-SNAPSHOT
```

**Run front end server**

```bash
cd PornTubeWeb
npm install
```

**Then**

```bash
npm run dev
```

**Final**

Open

```
http://127.0.0.1:5173
```


### Question

If you are unable to compile using an older version of the frontend

Frontend Fails to Compile

If you encounter the error `ESLint is not a constructor`, add `lintOnSave: false` to your `vue.config.js`.


**Tips:** The first user registered with the name admin will automatically become an administrator.

## CHANGE LOG

[CHANGELOG](/CHANGELOG.md)


## Disclaimer


The project name and logo are inspired by a well-known website, intended solely for fun and reference. Any use of this project for illegal purposes is strictly prohibited. If you utilize this project to build a platform with functionality similar to that of the aforementioned website, the developer shall not be held liable for any resulting consequences or responsibilities.

