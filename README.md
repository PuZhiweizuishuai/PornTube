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

<img src="https://sc03.alicdn.com/kf/U0b4883e1bcff49b4b01b663d594838bbv.jpg" title="首页" alt="首页">

## Video Page

<img src="https://ae04.alicdn.com/kf/U94c7f938e2d14c2c81c9e4af25ee94966.jpg" title="播放页" alt="播放页">


### history

<img src="https://ae04.alicdn.com/kf/Uf6234d40690f4f45ab8c77b6709dd689i.jpg" title="历史记录" alt="历史记录">

## User Home

<img src="https://sc01.alicdn.com/kf/U2052324455ff4565a98489a4cc0975f8Y.jpg" title="个人主页" alt="个人主页">

### publish

<img src="https://ae04.alicdn.com/kf/U3e6186ea90704e1381da8494434aaf1cN.jpg" title="投稿" alt="投稿">


## introduction 

A simple video website

Interface design reference Youtube

Adopted Spring Boot， Vue， vuetifyjs

Still in development. But most of the gong'n has been done.

for example, Video upload, danmaku, historical records, etc.


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

**Tips:** The first user registered with the name admin will automatically become an administrator.



