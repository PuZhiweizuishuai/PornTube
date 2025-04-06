import{_ as g}from"./time-util-nx3sX2E9.js";import{_ as I}from"./string-utils-8vaaKgF8.js";import{_ as x,b as y,a as t,w as e,r as d,o as c,f as i,e as s,t as r,c as v,g as u}from"./index-TiTfTDly.js";const C={name:"VideoCard",props:{video:{type:Object,default:()=>{}}},data(){return{TimeUtil:g,StringUtils:I,videoInfo:this.video}},created(){}},b={style:{width:"350px"}},h={class:"d-flex fill-height align-end"},k={style:{"font-size":"20px","margin-bottom":"0px",color:"black"}},w={style:{"font-size":"10px",color:"#606060"}},T=["textContent"],N=["textContent"],U=["textContent"];function V(z,n,B,S,o,D){const a=d("v-chip"),f=d("v-img"),l=d("router-link"),m=d("v-avatar"),_=d("v-col"),p=d("v-row");return c(),y("div",b,[t(l,{to:`/video/${o.videoInfo.id}`},{default:e(()=>[t(f,{src:o.videoInfo.imgUrl,"aspect-ratio":16/9,cover:"",class:"rounded",height:"100%"},{default:e(()=>[i("div",h,[t(a,{style:{"background-color":"rgba(0, 0, 0, 50%)"},variant:"text",color:"white",size:"small",class:"mb-1 mr-1 bg-black-transparent"},{default:e(()=>[s(r(o.TimeUtil.timeCover(o.videoInfo.duration)),1)]),_:1})])]),_:1},8,["src"])]),_:1},8,["to"]),t(p,{style:{"padding-top":"12px","padding-bottom":"12px"}},{default:e(()=>[t(_,{cols:"2"},{default:e(()=>[t(l,{to:`/user/${o.videoInfo.userId}`},{default:e(()=>[t(m,{size:"48"},{default:e(()=>[t(f,{alt:o.videoInfo.username,src:o.videoInfo.avatarUrl},null,8,["alt","src"])]),_:1})]),_:1},8,["to"])]),_:1}),t(_,{cols:"10"},{default:e(()=>[i("p",k,[t(l,{to:`/video/${o.videoInfo.id}`,style:{color:"black"}},{default:e(()=>[s(r(o.videoInfo.title)+" ",1),o.videoInfo.pixelsNumber>=2073600?(c(),v(a,{key:0,class:"ma-2",color:"orange","x-small":"","text-color":"white"},{default:e(()=>[s(r(o.StringUtils.clarityDisplay(o.videoInfo.pixelsNumber)),1)]),_:1})):u("",!0)]),_:1},8,["to"])]),i("p",w,[s(r(o.videoInfo.viewCount)+" 观看 ",1),n[0]||(n[0]=i("span",{innerHTML:"  "},null,-1)),s(" "+r(o.videoInfo.danmakuCount)+" 条弹幕 ",1),n[1]||(n[1]=i("span",{innerHTML:"  "},null,-1)),o.videoInfo.childrenCategory.fatherId!==0?(c(),v(l,{key:0,to:`/v/${o.videoInfo.fatherCategory.id}`,class:"category-link"},{default:e(()=>[i("span",{textContent:r(o.videoInfo.fatherCategory.name)},null,8,T)]),_:1},8,["to"])):u("",!0),n[2]||(n[2]=s(" / ")),t(l,{to:`/v/${o.videoInfo.childrenCategory.id}`,class:"category-link"},{default:e(()=>[i("span",{textContent:r(o.videoInfo.childrenCategory.name)},null,8,N)]),_:1},8,["to"]),n[3]||(n[3]=i("br",null,null,-1)),t(l,{to:`/user/${o.videoInfo.userId}`},{default:e(()=>[s(r(o.videoInfo.username),1)]),_:1},8,["to"]),n[4]||(n[4]=i("br",null,null,-1)),i("span",{textContent:r(o.TimeUtil.timeToNowStrning(o.videoInfo.createTime))},null,8,U)])]),_:1})]),_:1})])}const j=x(C,[["render",V]]);export{j as V};
