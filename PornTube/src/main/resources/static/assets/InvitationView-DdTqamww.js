import{_ as V}from"./time-util-nx3sX2E9.js";import{_ as C,c as g,w as t,r as i,o as m,a as o,e as r,f as _,t as l,b as v}from"./index-TiTfTDly.js";const S={name:"InvitationView",data(){return{showMessage:!1,message:"",TimeUtil:V,invitationList:[],headers:[{title:"ID",align:"start",sortable:!1,key:"id"},{title:"创建时间",sortable:!1,key:"createTime",align:"center"},{title:"生成人",sortable:!1,key:"createUser",align:"center"},{title:"邀请码",sortable:!1,key:"code",align:"center"},{title:"状态",sortable:!1,key:"useStatus",align:"center"},{title:"使用人",sortable:!1,key:"useUser",align:"center"},{title:"使用时间",sortable:!1,key:"useTime",align:"center"}],page:1,size:20,length:1}},created(){this.getList()},methods:{getStatus(n){return n===1?"可用":"已使用"},getList(){this.httpGet(`/admin/invitation/list?page=${this.page}&limit=${this.size}`,n=>{this.invitationList=n.data.list,this.page=n.data.currPage,this.length=n.data.totalPage})},create(){this.httpPost("/admin/invitation/create",null,n=>{n.status===200?(this.message="生成成功，请到列表查看",this.showMessage=!0,this.getList()):(this.message="生成失败，"+n.message,this.showMessage=!0)})},pageChange({page:n,itemsPerPage:s,sortBy:h}){this.loading=!0,this.page=n,this.getList(),window.scrollTo({top:0,behavior:"smooth"})}}},z={key:1},M={key:1},B={class:"d-flex justify-center pa-4"},P={class:"text-center pt-2"};function I(n,s,h,N,a,d){const f=i("v-toolbar-title"),b=i("v-spacer"),c=i("v-btn"),k=i("v-toolbar"),u=i("v-divider"),p=i("v-chip"),y=i("v-pagination"),w=i("v-data-table-server"),U=i("v-card-text"),x=i("v-card"),T=i("v-snackbar"),L=i("v-container");return m(),g(L,{class:"fill-height"},{default:t(()=>[o(x,{class:"mx-auto w-100",elevation:"2",rounded:"lg"},{default:t(()=>[o(k,{color:"red"},{default:t(()=>[o(f,{class:"text-h5 font-weight-medium"},{default:t(()=>s[4]||(s[4]=[r("邀请码管理")])),_:1}),o(b),o(c,{class:"mr-2","prepend-icon":"mdi-plus",color:"white",variant:"tonal",onClick:d.create},{default:t(()=>s[5]||(s[5]=[r(" 生成邀请码 ")])),_:1},8,["onClick"])]),_:1}),o(u),o(U,null,{default:t(()=>[o(w,{headers:a.headers,itemsLength:a.invitationList.length,items:a.invitationList,"items-per-page":a.size,page:a.page,"onUpdate:page":s[1]||(s[1]=e=>a.page=e),hover:"","onUpdate:options":d.pageChange},{"item.createUser":t(({item:e})=>[o(c,{to:`/user/${e.createUser}`,variant:"text",color:"primary",density:"comfortable",target:"_blank"},{default:t(()=>[r(l(e.createUser),1)]),_:2},1032,["to"])]),"item.createTime":t(({item:e})=>[o(p,{size:"small"},{default:t(()=>[r(l(a.TimeUtil.renderTime(e.createTime)),1)]),_:2},1024)]),"item.useTime":t(({item:e})=>[e.useTime?(m(),g(p,{key:0,size:"small",color:"grey-lighten-3"},{default:t(()=>[r(l(a.TimeUtil.renderTime(e.useTime)),1)]),_:2},1024)):(m(),v("span",z,"-"))]),"item.useStatus":t(({item:e})=>[o(p,{size:"small",color:e.useStatus===1?"success":"error",text:d.getStatus(e.useStatus)},null,8,["color","text"])]),"item.useUser":t(({item:e})=>[e.useUser?(m(),g(c,{key:0,to:`/user/${e.useUser}`,variant:"text",color:"primary",density:"comfortable",target:"_blank"},{default:t(()=>[r(l(e.useUser),1)]),_:2},1032,["to"])):(m(),v("span",M,"-"))]),"item.code":t(({item:e})=>[o(p,{color:"primary-lighten-1",class:"font-weight-medium monospace"},{default:t(()=>[r(l(e.code),1)]),_:2},1024)]),"no-data":t(()=>[_("div",B,[o(c,{color:"primary",onClick:d.getList},{default:t(()=>s[6]||(s[6]=[r("重新加载")])),_:1},8,["onClick"])])]),bottom:t(()=>[_("div",P,[o(y,{modelValue:a.page,"onUpdate:modelValue":s[0]||(s[0]=e=>a.page=e),length:a.length,"total-visible":7,rounded:""},null,8,["modelValue","length"])])]),_:2},1032,["headers","itemsLength","items","items-per-page","page","onUpdate:options"])]),_:1}),o(u)]),_:1}),o(T,{modelValue:a.showMessage,"onUpdate:modelValue":s[3]||(s[3]=e=>a.showMessage=e),timeout:3e3,location:"top",color:"primary"},{actions:t(()=>[o(c,{color:"white",variant:"text",onClick:s[2]||(s[2]=e=>a.showMessage=!1)},{default:t(()=>s[7]||(s[7]=[r(" 关闭 ")])),_:1})]),default:t(()=>[r(l(a.message)+" ",1)]),_:1},8,["modelValue"])]),_:1})}const G=C(S,[["render",I],["__scopeId","data-v-86e85905"]]);export{G as default};
