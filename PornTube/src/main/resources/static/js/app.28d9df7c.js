(function(e){function t(t){for(var o,a,u=t[0],s=t[1],c=t[2],l=0,d=[];l<u.length;l++)a=u[l],Object.prototype.hasOwnProperty.call(i,a)&&i[a]&&d.push(i[a][0]),i[a]=0;for(o in s)Object.prototype.hasOwnProperty.call(s,o)&&(e[o]=s[o]);h&&h(t);while(d.length)d.shift()();return r.push.apply(r,c||[]),n()}function n(){for(var e,t=0;t<r.length;t++){for(var n=r[t],o=!0,a=1;a<n.length;a++){var u=n[a];0!==i[u]&&(o=!1)}o&&(r.splice(t--,1),e=s(s.s=n[0]))}return e}var o={},a={app:0},i={app:0},r=[];function u(e){return s.p+"js/"+({}[e]||e)+"."+{"chunk-043c6199":"60eac6f8","chunk-2d0b723a":"2c1e5ef5","chunk-2d0cb6f3":"a934465a","chunk-2d0e95df":"cd2f58e9","chunk-2d210469":"ad462af9","chunk-2d21f0c8":"1d52e452","chunk-2d22b903":"b841aa6e","chunk-3837eddc":"6713945f","chunk-4b38fef3":"112a66cc","chunk-16cfe78c":"6008a853","chunk-2d224cf0":"dab9fae0","chunk-635a326e":"dad971a1","chunk-68d81bda":"efc98286","chunk-64ab89fc":"9e35f11b","chunk-4538f784":"78c593cd","chunk-25735359":"a02999c8","chunk-2d0ab41c":"88a61c9e","chunk-54bfc0f9":"dda076e5","chunk-5965d189":"5c35243a","chunk-5a2912da":"c33912ec","chunk-018f1c14":"920cc1ff","chunk-676035a8":"2086830b","chunk-6c70e334":"19ac1499","chunk-bd95188a":"46384ee2","chunk-dd9fe650":"248c29cb"}[e]+".js"}function s(t){if(o[t])return o[t].exports;var n=o[t]={i:t,l:!1,exports:{}};return e[t].call(n.exports,n,n.exports,s),n.l=!0,n.exports}s.e=function(e){var t=[],n={"chunk-043c6199":1,"chunk-3837eddc":1,"chunk-4b38fef3":1,"chunk-16cfe78c":1,"chunk-635a326e":1,"chunk-68d81bda":1,"chunk-64ab89fc":1,"chunk-4538f784":1,"chunk-25735359":1,"chunk-54bfc0f9":1,"chunk-5965d189":1,"chunk-5a2912da":1,"chunk-018f1c14":1,"chunk-676035a8":1,"chunk-6c70e334":1,"chunk-bd95188a":1,"chunk-dd9fe650":1};a[e]?t.push(a[e]):0!==a[e]&&n[e]&&t.push(a[e]=new Promise((function(t,n){for(var o="css/"+({}[e]||e)+"."+{"chunk-043c6199":"6717cf06","chunk-2d0b723a":"31d6cfe0","chunk-2d0cb6f3":"31d6cfe0","chunk-2d0e95df":"31d6cfe0","chunk-2d210469":"31d6cfe0","chunk-2d21f0c8":"31d6cfe0","chunk-2d22b903":"31d6cfe0","chunk-3837eddc":"057ab481","chunk-4b38fef3":"daca5a6e","chunk-16cfe78c":"5b29b0e0","chunk-2d224cf0":"31d6cfe0","chunk-635a326e":"0ea2a470","chunk-68d81bda":"22b26e55","chunk-64ab89fc":"668a2c76","chunk-4538f784":"13580070","chunk-25735359":"79686926","chunk-2d0ab41c":"31d6cfe0","chunk-54bfc0f9":"2cebfbf9","chunk-5965d189":"7bd289c1","chunk-5a2912da":"76256289","chunk-018f1c14":"8ad0bb85","chunk-676035a8":"116b7b5f","chunk-6c70e334":"11057901","chunk-bd95188a":"2cebfbf9","chunk-dd9fe650":"2cebfbf9"}[e]+".css",i=s.p+o,r=document.getElementsByTagName("link"),u=0;u<r.length;u++){var c=r[u],l=c.getAttribute("data-href")||c.getAttribute("href");if("stylesheet"===c.rel&&(l===o||l===i))return t()}var d=document.getElementsByTagName("style");for(u=0;u<d.length;u++){c=d[u],l=c.getAttribute("data-href");if(l===o||l===i)return t()}var h=document.createElement("link");h.rel="stylesheet",h.type="text/css",h.onload=t,h.onerror=function(t){var o=t&&t.target&&t.target.src||i,r=new Error("Loading CSS chunk "+e+" failed.\n("+o+")");r.code="CSS_CHUNK_LOAD_FAILED",r.request=o,delete a[e],h.parentNode.removeChild(h),n(r)},h.href=i;var p=document.getElementsByTagName("head")[0];p.appendChild(h)})).then((function(){a[e]=0})));var o=i[e];if(0!==o)if(o)t.push(o[2]);else{var r=new Promise((function(t,n){o=i[e]=[t,n]}));t.push(o[2]=r);var c,l=document.createElement("script");l.charset="utf-8",l.timeout=120,s.nc&&l.setAttribute("nonce",s.nc),l.src=u(e);var d=new Error;c=function(t){l.onerror=l.onload=null,clearTimeout(h);var n=i[e];if(0!==n){if(n){var o=t&&("load"===t.type?"missing":t.type),a=t&&t.target&&t.target.src;d.message="Loading chunk "+e+" failed.\n("+o+": "+a+")",d.name="ChunkLoadError",d.type=o,d.request=a,n[1](d)}i[e]=void 0}};var h=setTimeout((function(){c({type:"timeout",target:l})}),12e4);l.onerror=l.onload=c,document.head.appendChild(l)}return Promise.all(t)},s.m=e,s.c=o,s.d=function(e,t,n){s.o(e,t)||Object.defineProperty(e,t,{enumerable:!0,get:n})},s.r=function(e){"undefined"!==typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(e,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(e,"__esModule",{value:!0})},s.t=function(e,t){if(1&t&&(e=s(e)),8&t)return e;if(4&t&&"object"===typeof e&&e&&e.__esModule)return e;var n=Object.create(null);if(s.r(n),Object.defineProperty(n,"default",{enumerable:!0,value:e}),2&t&&"string"!=typeof e)for(var o in e)s.d(n,o,function(t){return e[t]}.bind(null,o));return n},s.n=function(e){var t=e&&e.__esModule?function(){return e["default"]}:function(){return e};return s.d(t,"a",t),t},s.o=function(e,t){return Object.prototype.hasOwnProperty.call(e,t)},s.p="/",s.oe=function(e){throw console.error(e),e};var c=window["webpackJsonp"]=window["webpackJsonp"]||[],l=c.push.bind(c);c.push=t,c=c.slice();for(var d=0;d<c.length;d++)t(c[d]);var h=l;r.push([0,"chunk-vendors"]),n()})({0:function(e,t,n){e.exports=n("56d7")},"0ff4":function(e,t,n){},1072:function(e,t,n){"use strict";n("0ff4")},"1c1e":function(e,t){t.install=function(e,t){e.prototype.httpGet=function(e,t){fetch(`${this.SERVER_API_URL}${e}`,{headers:{"Content-Type":"application/json; charset=UTF-8","X-XSRF-TOKEN":this.$cookies.get("XSRF-TOKEN")},method:"GET",credentials:"include"}).then(e=>e.json()).then(e=>{t(e)}).catch(e=>null)},e.prototype.httpPost=function(e,t,n){fetch(`${this.SERVER_API_URL}${e}`,{headers:{"Content-Type":"application/json; charset=UTF-8","X-XSRF-TOKEN":this.$cookies.get("XSRF-TOKEN")},method:"POST",credentials:"include",body:JSON.stringify(t)}).then(e=>e.json()).then(e=>{n(e)}).catch(e=>null)},e.prototype.uploadFiles=function(e,t,n){fetch(`${this.SERVER_API_URL}${e}`,{headers:{"X-XSRF-TOKEN":this.$cookies.get("XSRF-TOKEN")},method:"POST",credentials:"include",body:t}).then(e=>e.json()).then(e=>{n(e)}).catch(e=>null)}}},"56d7":function(e,t,n){"use strict";n.r(t);var o=n("2b0e"),a=n("7496"),i=function(){var e=this,t=e._self._c;return t(a["a"],[t("router-view")],1)},r=[],u={name:"App",data:()=>({})},s=u,c=n("2877"),l=Object(c["a"])(s,i,r,!1,null,null,null),d=l.exports,h=n("8c4f"),p=n("40dc"),f=n("5bc1"),m=n("8336"),b=n("132d"),k=n("da13"),g=n("1800"),v=n("5d23"),y=n("f6c4"),I=n("f774"),_=n("2fa4"),T=n("8654"),S=n("2a7f"),x=n("3a2f"),$=function(){var e=this,t=e._self._c;return t("div",[t(I["a"],{attrs:{app:"",clipped:""},model:{value:e.drawer,callback:function(t){e.drawer=t},expression:"drawer"}},e._l(e.items,(function(n){return t("router-link",{key:n.text,attrs:{to:n.link}},[t(k["a"],{attrs:{link:""}},[t(g["a"],[t(b["a"],[e._v(e._s(n.icon))])],1),t(v["a"],[t(v["b"],[e._v(" "+e._s(n.text)+" ")])],1)],1)],1)})),1),t(p["a"],{attrs:{"clipped-left":e.$vuetify.breakpoint.lgAndUp,app:"",color:"red",dark:""}},[t(f["a"],{on:{click:function(t){t.stopPropagation(),e.drawer=!e.drawer}}}),t(S["a"],{staticClass:"hidden-sm-and-down ml-0 pl-4",staticStyle:{width:"300px"}},[t("span",{staticStyle:{cursor:"pointer"},on:{click:function(t){return e.goToHome()}}},[e._v(e._s(this.$store.state.webInfo.name))])]),t(T["a"],{attrs:{flat:"","solo-inverted":"","hide-details":"","prepend-inner-icon":"mdi-magnify",label:"搜索"}}),t(_["a"]),t(x["a"],{attrs:{bottom:""},scopedSlots:e._u([{key:"activator",fn:function({on:n,attrs:o}){return[t(m["a"],e._g(e._b({attrs:{icon:""},on:{click:e.goToPublish}},"v-btn",o,!1),n),[t(b["a"],[e._v("mdi-video-plus")])],1)]}}])},[t("span",[e._v("发布")])]),t(x["a"],{attrs:{bottom:""},scopedSlots:e._u([{key:"activator",fn:function({on:n,attrs:o}){return[t(m["a"],e._g(e._b({attrs:{icon:""}},"v-btn",o,!1),n),[t(b["a"],[e._v("mdi-bell")])],1)]}}])},[t("span",[e._v("通知")])]),this.$store.state.userInfo?t("Head"):e._e(),null==this.$store.state.userInfo?t(m["a"],{attrs:{outlined:""},on:{click:e.goToLoginPage}},[t(b["a"],{attrs:{left:"",dark:""}},[e._v("mdi-account")]),e._v(" 登录 ")],1):e._e()],1),t(y["a"],[t("router-view")],1)],1)},E=[],P=n("8a54"),w={components:{Head:P["a"]},data:()=>({userInfo:{},drawer:!1,items:[{icon:"mdi-home",text:"首页",link:"/"},{icon:"mdi-trending-up",text:"时下流行",link:"/hot"},{icon:"mdi-youtube-subscription",text:"订阅",link:"/subscribe"},{icon:"mdi-history",text:"历史记录",link:"/history"},{icon:"mdi-playlist-play",text:"稍后再看",link:"/playlist"}],headItem:[{icon:"mdi-head",text:"个人主页",link:"/user/",id:0},{icon:"mdi-wrench",text:"创作中心",link:"/studio",id:1},{icon:"mdi-logout",text:"退出",link:"/logout",id:2}]}),mounted(){},created(){this.userInfo=this.$store.state.userInfo,this.$vuetify.theme.dark=this.$store.state.darkThemOpen},methods:{headClick(e){0===e?this.$router.push("/user/"+this.userInfo.id):1===e?this.$router.push("/studio"):this.logout()},logout(){fetch("/api/logout",{headers:{"Content-Type":"application/json; charset=UTF-8","X-XSRF-TOKEN":this.$cookies.get("XSRF-TOKEN")},method:"GET",credentials:"include"}).then(e=>e.json()).then(e=>{200===e.status&&(this.$store.commit("setUserInfo",null),"/"===this.$route.path?location.reload():this.$router.push("/"))}).catch(e=>null)},goToLoginPage(){this.$router.push("/login")},goToPublish(){this.$router.push("/studio/upload")},goToHome(){"/"!==this.$route.path&&this.$router.push("/")},goToUserHome(){this.$route.path!=="/user/"+this.$store.state.userInfo.id&&this.$router.push("/user/"+this.$store.state.userInfo.id)}}},O=w,R=(n("1072"),Object(c["a"])(O,$,E,!1,null,"24305e46",null)),C=R.exports,j=n("62ad"),U=n("a523"),L=n("891e"),N=n("0fd9"),A=function(){var e=this,t=e._self._c;return t(U["a"],{staticStyle:{"padding-left":"24px","padding-right":"24px"},attrs:{"fill-height":"",fluid:""}},[t(N["a"],{staticStyle:{"padding-top":"12px","padding-bottom":"12px"},attrs:{"no-gutters":""}},e._l(e.videoList,(function(e){return t(j["a"],{key:e.id},[t("VideoCared",{attrs:{video:e}})],1)})),1),t(N["a"],{attrs:{justify:"center"}},[t(L["a"],{attrs:{length:e.length},on:{input:e.pageChange},model:{value:e.page,callback:function(t){e.page=t},expression:"page"}})],1)],1)},F=[],V=n("8212"),q=n("cc20"),D=n("adda"),M=function(){var e=this,t=e._self._c;return t("div",{staticStyle:{width:"350px"}},[t("router-link",{attrs:{to:"/video/"+e.videoInfo.id}},[t(D["a"],{staticClass:"white--text align-end",attrs:{src:e.videoInfo.imgUrl,outlined:"","aspect-ratio":"1.77"}},[t("span",{staticStyle:{"background-color":"rgba(0,0,0,0.4)","border-radius":"5px"}},[e._v(" "+e._s(e.TimeUtil.timeCover(e.videoInfo.duration))+" ")])])],1),t(N["a"],{staticStyle:{"padding-top":"12px","padding-bottom":"12px"}},[t(j["a"],{attrs:{cols:"2"}},[t("router-link",{attrs:{to:"/user/"+e.videoInfo.userId}},[t(V["a"],{attrs:{size:"48"}},[t(D["a"],{attrs:{src:e.videoInfo.avatarUrl}})],1)],1)],1),t(j["a"],{attrs:{cols:"10"}},[t("p",{staticStyle:{"font-size":"20px","margin-bottom":"0px",color:"black"}},[t("router-link",{staticStyle:{color:"black"},attrs:{to:"/video/"+e.videoInfo.id}},[e._v(" "+e._s(e.videoInfo.title)+" "),e.videoInfo.pixelsNumber>=2073600?t(q["a"],{staticClass:"ma-2",attrs:{color:"orange","x-small":"","text-color":"white"}},[e._v(" "+e._s(e.StringUtils.clarityDisplay(e.videoInfo.pixelsNumber))+" ")]):e._e()],1)],1),t("p",{staticStyle:{"font-size":"10px",color:"#606060"}},[e._v(" "+e._s(e.videoInfo.viewCount)+" 观看 "),t("span",{domProps:{innerHTML:e._s("&nbsp;&nbsp;")}}),e._v(" "+e._s(e.videoInfo.danmakuCount)+" 条弹幕 "),t("span",{domProps:{innerHTML:e._s("&nbsp;&nbsp;")}}),0!==e.videoInfo.childrenCategory.fatherId?t("router-link",{staticClass:"category-link",attrs:{to:"/v/"+e.videoInfo.fatherCategory.id}},[t("span",{domProps:{textContent:e._s(e.videoInfo.fatherCategory.name)}})]):e._e(),e._v(" / "),t("router-link",{staticClass:"category-link",attrs:{to:"/v/"+e.videoInfo.childrenCategory.id}},[t("span",{domProps:{textContent:e._s(e.videoInfo.childrenCategory.name)}})]),t("br"),t("router-link",{attrs:{to:"/user/"+e.videoInfo.userId}},[e._v(" "+e._s(e.videoInfo.username))]),t("br"),t("span",{domProps:{textContent:e._s(e.TimeUtil.timeToNowStrning(e.videoInfo.createTime))}})],1)])],1)],1)},X=[],H=n("9878"),K=n("c919"),z={name:"VideoCard",props:{video:{type:Object,default:()=>{}}},data(){return{TimeUtil:H["a"],StringUtils:K["a"],videoInfo:this.video}},created(){}},G=z,J=Object(c["a"])(G,M,X,!1,null,null,null),B=J.exports,Y={name:"Index",components:{VideoCared:B},data(){return{videoList:[],page:1,size:24,length:0}},created(){console.log(this.$route.query.page),void 0===this.$route.query.page?this.page=1:this.page=this.$route.query.page,this.getVideoList()},methods:{getVideoList(){fetch(`/api/article/home/list?page=${this.page}&limit=${this.size}`,{headers:{"Content-Type":"application/json; charset=UTF-8","X-XSRF-TOKEN":this.$cookies.get("XSRF-TOKEN")},method:"GET",credentials:"include"}).then(e=>e.json()).then(e=>{this.videoList=e.data.list,this.page=e.data.currPage,this.length=e.data.totalPage}).catch(e=>null)},pageChange(e){this.page=e,this.$router.push({query:{page:this.page}}),this.getVideoList(),this.$vuetify.goTo(0)}}},W=Y,Q=(n("fb19"),Object(c["a"])(W,A,F,!1,null,null,null)),Z=Q.exports,ee=n("f3a1");o["default"].use(h["a"]);const te=[{path:"/",name:"Index",component:C,meta:{title:"PornTube"},children:[{path:"/",name:"Index",component:Z,meta:{title:"PornTube"}},{path:"/hot",name:"Hot",component:()=>n.e("chunk-2d0cb6f3").then(n.bind(null,"4a3c")),meta:{title:"PornTube 时下流行"}},{path:"/subscribe",name:"Subscribe",component:()=>n.e("chunk-dd9fe650").then(n.bind(null,"1fae")),meta:{title:"PornTube 订阅"}},{path:"/history",name:"History",component:()=>n.e("chunk-6c70e334").then(n.bind(null,"4342")),meta:{title:"PornTube 播放历史"}},{path:"/playlist",name:"Playlist",component:()=>n.e("chunk-54bfc0f9").then(n.bind(null,"ed70")),meta:{title:"PornTube 稍后再看"}},{path:"/video/:id",name:"Vide",component:()=>Promise.all([n.e("chunk-4538f784"),n.e("chunk-25735359")]).then(n.bind(null,"85bc")),meta:{title:"播放"}},{path:"/user/setting",name:"UserSetting",component:()=>Promise.all([n.e("chunk-3837eddc"),n.e("chunk-4b38fef3"),n.e("chunk-635a326e"),n.e("chunk-68d81bda")]).then(n.bind(null,"4852")),meta:{title:"个人设置",requireAuth:!0}},{path:"/vip",name:"VIP",component:()=>n.e("chunk-bd95188a").then(n.bind(null,"e169")),meta:{title:"VIP"}},{path:"/vip/pay",name:"VipPay",component:()=>n.e("chunk-5965d189").then(n.bind(null,"b43d")),meta:{title:"VIP"}},{path:"/user/:id",name:"User",component:()=>Promise.all([n.e("chunk-635a326e"),n.e("chunk-018f1c14")]).then(n.bind(null,"e382")),meta:{title:"个人主页"}},{path:"/live",name:"live",component:()=>Promise.all([n.e("chunk-4538f784"),n.e("chunk-2d0ab41c")]).then(n.bind(null,"1502")),meta:{title:"直播"}}]},{path:"/studio",name:"Studio",component:()=>n.e("chunk-043c6199").then(n.bind(null,"d1b8")),meta:{title:"创作中心"},children:[{path:"/studio",name:"StudioIndex",component:()=>n.e("chunk-2d22b903").then(n.bind(null,"f003")),meta:{title:"创作中心",requireAuth:!0}},{path:"/studio/upload",name:"Upload",component:()=>Promise.all([n.e("chunk-3837eddc"),n.e("chunk-635a326e"),n.e("chunk-64ab89fc")]).then(n.bind(null,"e23f")),meta:{title:"投稿",requireAuth:!0}},{path:"/studio/comment",name:"Comment",component:()=>n.e("chunk-2d210469").then(n.bind(null,"b6cb")),meta:{title:"评论",requireAuth:!0}},{path:"/studio/admin/invitation",name:"invitation",component:()=>Promise.all([n.e("chunk-3837eddc"),n.e("chunk-4b38fef3"),n.e("chunk-2d224cf0")]).then(n.bind(null,"e25b")),meta:{title:"邀请码",requireAuth:!0}},{path:"/studio/admin/examine",name:"Examine",component:()=>Promise.all([n.e("chunk-3837eddc"),n.e("chunk-4b38fef3"),n.e("chunk-16cfe78c")]).then(n.bind(null,"a8a6")),meta:{title:"审核视频",requireAuth:!0}},{path:"/studio/admin/userlist",name:"Examine",component:()=>n.e("chunk-2d0b723a").then(n.bind(null,"1fc2")),meta:{title:"用户列表",requireAuth:!0}},{path:"/studio/admin/websetting",name:"Examine",component:()=>n.e("chunk-676035a8").then(n.bind(null,"1f91")),meta:{title:"网页设置",requireAuth:!0}},{path:"/studio/admin/category",name:"Examine",component:()=>n.e("chunk-2d21f0c8").then(n.bind(null,"d7ba")),meta:{title:"分区设置",requireAuth:!0}}]},{path:"/login",name:"Login",component:()=>n.e("chunk-5a2912da").then(n.bind(null,"dd7b")),meta:{title:"登录"}},{path:"*",name:"404",component:()=>n.e("chunk-2d0e95df").then(n.bind(null,"8cdb")),meta:{title:"404"}}],ne=new h["a"]({mode:"history",routes:te});ne.beforeEach((e,t,n)=>{null==ne.app.$options.store.state.webInfo.name&&fetch("/api/web/info",{headers:{"Content-Type":"application/json; charset=UTF-8"},method:"GET",credentials:"include"}).then(e=>e.json()).then(e=>{ne.app.$options.store.state.webInfo=e.data}).catch(e=>null),e.meta.title&&(document.title=e.meta.title),ee["a"].updateUserRole(ne.app.$options.store.state.userInfo)&&(ne.app.$options.store.state.userInfo.userRoleEntity.role="ROLE_USER",ne.app.$options.store.commit("setUserInfo",ne.app.$options.store.state.userInfo));const o=(new Date).getTime();return null!=ne.app.$options.store.state.userInfo?ne.app.$options.store.state.userInfo.expireTime>o?"/login"===e.path?n({path:"/"}):n():(ne.app.$options.store.commit("setUserInfo",null),n({path:"/login",query:{redirect:e.fullPath}})):e.meta.requireAuth?n({path:"/login",query:{redirect:e.fullPath}}):n()});var oe=ne,ae=n("2f62");o["default"].use(ae["a"]);var ie=new ae["a"].Store({state:{webInfo:{},darkThemOpen:!1,userInfo:null!=localStorage.getItem("user")&&"undefined"!==localStorage.getItem("user")&&""!==localStorage.getItem("user")?JSON.parse(localStorage.getItem("user")):null,uploadVideoDateTemp:{}},mutations:{setUserInfo(e,t){localStorage.setItem("user",JSON.stringify(t)),e.userInfo=t},setWebInfo(e,t){localStorage.setItem("webInfo",JSON.stringify(t)),e.userInfo=t}},actions:{},modules:{}}),re=n("f309");o["default"].use(re["a"]);var ue=new re["a"]({}),se=n("2b27"),ce=n.n(se),le=n("1c1e"),de=n.n(le);o["default"].use(ce.a),o["default"].use(de.a),o["default"].config.productionTip=!1,o["default"].prototype.SERVER_API_URL="/api",new o["default"]({router:oe,store:ie,vuetify:ue,render:e=>e(d)}).$mount("#app")},"8a54":function(e,t,n){"use strict";var o=n("8212"),a=n("8336"),i=n("132d"),r=n("adda"),u=n("8860"),s=n("da13"),c=n("1800"),l=n("5d23"),d=n("e449"),h=function(){var e=this,t=e._self._c;return this.$store.state.userInfo?t(d["a"],{attrs:{top:"","offset-y":""},scopedSlots:e._u([{key:"activator",fn:function({on:n,attrs:i}){return[t(a["a"],e._g(e._b({attrs:{dark:"",icon:"",large:""}},"v-btn",i,!1),n),[t(o["a"],{attrs:{size:"32px",item:""}},[t(r["a"],{attrs:{src:e.userInfo.avatarUrl,alt:e.userInfo.username,title:e.userInfo.username}})],1)],1)]}}],null,!1,2968636732)},[t(u["a"],e._l(e.headItem,(function(n,o){return t(s["a"],{key:o,attrs:{link:""},on:{click:function(t){return e.headClick(n.id)}}},[t(c["a"],[t(i["a"],[e._v(e._s(n.icon))])],1),t(l["a"],[t(l["b"],[e._v(" "+e._s(n.text)+" ")])],1)],1)})),1)],1):e._e()},p=[],f={name:"Head",data(){return{userInfo:{},headItem:[{icon:"mdi-account",text:"个人主页",link:"/user/",id:0},{icon:"mdi-cash-usd",text:"付费会员",link:"/vip",id:3},{icon:"mdi-application",text:"创作中心",link:"/studio",id:1},{icon:"mdi-wrench",text:"自定义频道",link:"/user/setting",id:4},{icon:"mdi-logout",text:"退出",link:"/logout",id:2}]}},created(){this.userInfo=this.$store.state.userInfo},methods:{headClick(e){if(0===e)location.replace("/user/"+this.userInfo.id);else if(1===e){if("/studio"===this.$route.path)return;this.$router.push("/studio")}else if(3===e)this.$router.push("/vip");else if(4===e){if("/user/setting"===this.$route.path)return;this.$router.push("/user/setting")}else this.logout()},logout(){this.$store.commit("setUserInfo",null),fetch("/api/logout",{headers:{"Content-Type":"application/json; charset=UTF-8","X-XSRF-TOKEN":this.$cookies.get("XSRF-TOKEN")},method:"GET",credentials:"include"}).then(e=>e.json()).then(e=>{200===e.status&&(this.$store.commit("setUserInfo",null),"/"===this.$route.path?location.reload():this.$router.push("/"))}).catch(e=>null)}}},m=f,b=n("2877"),k=Object(b["a"])(m,h,p,!1,null,null,null);t["a"]=k.exports},9878:function(e,t,n){"use strict";function o(e){if(""===e||null==e)return"";const t=new Date(e);return t.getFullYear()+"-"+(t.getMonth()+1)+"-"+t.getDate()+" "+t.getHours()+":"+t.getMinutes()+":"+t.getSeconds()}function a(e){if(""===e||null==e)return"";const t=new Date(e);return t.getFullYear()+"年"+(t.getMonth()+1)+"月"+t.getDate()+"日 "+t.getHours()+"时"+t.getMinutes()+"分"}function i(e,t){return a(e)+" ~ "+a(t)}function r(e){const t=(new Date).getTime();let n=t-e;return n=Math.trunc(n/1e3),n<60?n+"秒前":(n=Math.trunc(n/60),n<60?n+"分钟前":(n=Math.trunc(n/60),n<24?n+"小时前":(n=Math.trunc(n/24),n<30?n+"天前":o(e))))}function u(e){if(null==e||""===e||"null"===e)return"";let t=Math.floor(e/60);if(t>=60){const n=Math.floor(t/60);return t%=60,n+":"+t+":"+parseInt(e%60)}return t+":"+parseInt(e%60)}var s,c,l={timeToNowStrning:r,renderTime:o,formateTimeToChinese:a,formateTime:i,timeCover:u},d=l,h=n("2877"),p=Object(h["a"])(d,s,c,!1,null,null,null);t["a"]=p.exports},c919:function(e,t,n){"use strict";function o(e){if(null!=e&&""!==e)return e>=8294400?"4K":e>=2073600?"HD":"SD"}var a,i,r={clarityDisplay:o},u=r,s=n("2877"),c=Object(s["a"])(u,a,i,!1,null,null,null);t["a"]=c.exports},e957:function(e,t,n){},f3a1:function(e,t,n){"use strict";function o(e){return"ROLE_ADMIN"===e.userRoleEntity.role?"admin":"ROLE_VIP"===e.userRoleEntity.role?e.userRoleEntity.vipStopTime<(new Date).getTime()?"user":"vip":"ROLE_USER"===e.userRoleEntity.role?"user":void 0}function a(e){return"ROLE_VIP"===e.userRoleEntity.role?!(e.userRoleEntity.vipStopTime<(new Date).getTime()):"ROLE_ADMIN"===e.userRoleEntity.role}function i(e){return null!==e&&("ROLE_VIP"===e.userRoleEntity.role&&e.userRoleEntity.vipStopTime<(new Date).getTime())}var r,u,s={checkPower:o,updateUserRole:i,checkVip:a},c=s,l=n("2877"),d=Object(l["a"])(c,r,u,!1,null,null,null);t["a"]=d.exports},fb19:function(e,t,n){"use strict";n("e957")}});