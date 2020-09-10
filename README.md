<div align="center">
    <img src="http://p.ananas.chaoxing.com/star3/origin/5e008a6cd46ea6ed07252f656acb5d2b.png" alt="logo" title="logo" width="50%" style="text-align:center;">
</div>

<br/>

<div align="center">
    <a href="https://github.com/PuZhiweizuishuai/PornTube/blob/master/README_zh_CN.md">简体中文</a>
</div>

<br/>

# PornTube


## screenshot

### Home 

<img src="http://p.ananas.chaoxing.com/star3/origin/a093b1ef4a410eea6bd616a2e985ac97.png" title="首页" alt="首页">

### history

<img src="http://p.ananas.chaoxing.com/star3/origin/496e8f398667a6238ffd2fa734e01a5e.png" title="历史记录" alt="历史记录">

## User Home

<img src="http://p.ananas.chaoxing.com/star3/origin/53580b104b00aebe98ae008ecaee41b6.png" title="个人主页" alt="个人主页">

### publish

<img src="http://p.ananas.chaoxing.com/star3/origin/1a0f4668b74041c843d16f71c842152e.png" title="投稿" alt="投稿">


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



