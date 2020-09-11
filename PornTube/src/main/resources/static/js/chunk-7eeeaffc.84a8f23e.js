(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-7eeeaffc"],{"169a":function(t,e,i){"use strict";i("368e");var n=i("480e"),a=i("4ad4"),s=i("b848"),o=i("75eb"),r=i("e707"),c=i("e4d3"),l=i("21be"),d=i("f2e7"),h=i("a293"),u=i("58df"),v=i("d9bd"),m=i("80d2");const g=Object(u["a"])(a["a"],s["a"],o["a"],r["a"],c["a"],l["a"],d["a"]);e["a"]=g.extend({name:"v-dialog",directives:{ClickOutside:h["a"]},props:{dark:Boolean,disabled:Boolean,fullscreen:Boolean,light:Boolean,maxWidth:{type:[String,Number],default:"none"},noClickAnimation:Boolean,origin:{type:String,default:"center center"},persistent:Boolean,retainFocus:{type:Boolean,default:!0},scrollable:Boolean,transition:{type:[String,Boolean],default:"dialog-transition"},width:{type:[String,Number],default:"auto"}},data(){return{activatedBy:null,animate:!1,animateTimeout:-1,isActive:!!this.value,stackMinZIndex:200}},computed:{classes(){return{[("v-dialog "+this.contentClass).trim()]:!0,"v-dialog--active":this.isActive,"v-dialog--persistent":this.persistent,"v-dialog--fullscreen":this.fullscreen,"v-dialog--scrollable":this.scrollable,"v-dialog--animated":this.animate}},contentClasses(){return{"v-dialog__content":!0,"v-dialog__content--active":this.isActive}},hasActivator(){return Boolean(!!this.$slots.activator||!!this.$scopedSlots.activator)}},watch:{isActive(t){t?(this.show(),this.hideScroll()):(this.removeOverlay(),this.unbind())},fullscreen(t){this.isActive&&(t?(this.hideScroll(),this.removeOverlay(!1)):(this.showScroll(),this.genOverlay()))}},created(){this.$attrs.hasOwnProperty("full-width")&&Object(v["e"])("full-width",this)},beforeMount(){this.$nextTick(()=>{this.isBooted=this.isActive,this.isActive&&this.show()})},beforeDestroy(){"undefined"!==typeof window&&this.unbind()},methods:{animateClick(){this.animate=!1,this.$nextTick(()=>{this.animate=!0,window.clearTimeout(this.animateTimeout),this.animateTimeout=window.setTimeout(()=>this.animate=!1,150)})},closeConditional(t){const e=t.target;return!(this._isDestroyed||!this.isActive||this.$refs.content.contains(e)||this.overlay&&e&&!this.overlay.$el.contains(e))&&this.activeZIndex>=this.getMaxZIndex()},hideScroll(){this.fullscreen?document.documentElement.classList.add("overflow-y-hidden"):r["a"].options.methods.hideScroll.call(this)},show(){!this.fullscreen&&!this.hideOverlay&&this.genOverlay(),this.$nextTick(()=>{this.$refs.content.focus(),this.bind()})},bind(){window.addEventListener("focusin",this.onFocusin)},unbind(){window.removeEventListener("focusin",this.onFocusin)},onClickOutside(t){this.$emit("click:outside",t),this.persistent?this.noClickAnimation||this.animateClick():this.isActive=!1},onKeydown(t){if(t.keyCode===m["y"].esc&&!this.getOpenDependents().length)if(this.persistent)this.noClickAnimation||this.animateClick();else{this.isActive=!1;const t=this.getActivator();this.$nextTick(()=>t&&t.focus())}this.$emit("keydown",t)},onFocusin(t){if(!t||!this.retainFocus)return;const e=t.target;if(e&&![document,this.$refs.content].includes(e)&&!this.$refs.content.contains(e)&&this.activeZIndex>=this.getMaxZIndex()&&!this.getOpenDependentElements().some(t=>t.contains(e))){const t=this.$refs.content.querySelectorAll('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'),e=[...t].find(t=>!t.hasAttribute("disabled"));e&&e.focus()}},genContent(){return this.showLazyContent(()=>[this.$createElement(n["a"],{props:{root:!0,light:this.light,dark:this.dark}},[this.$createElement("div",{class:this.contentClasses,attrs:{role:"document",tabindex:this.isActive?0:void 0,...this.getScopeIdAttrs()},on:{keydown:this.onKeydown},style:{zIndex:this.activeZIndex},ref:"content"},[this.genTransition()])])])},genTransition(){const t=this.genInnerContent();return this.transition?this.$createElement("transition",{props:{name:this.transition,origin:this.origin,appear:!0}},[t]):t},genInnerContent(){const t={class:this.classes,ref:"dialog",directives:[{name:"click-outside",value:{handler:this.onClickOutside,closeConditional:this.closeConditional,include:this.getOpenDependentElements}},{name:"show",value:this.isActive}],style:{transformOrigin:this.origin}};return this.fullscreen||(t.style={...t.style,maxWidth:"none"===this.maxWidth?void 0:Object(m["g"])(this.maxWidth),width:"auto"===this.width?void 0:Object(m["g"])(this.width)}),this.$createElement("div",t,this.getContentSlot())}},render(t){return t("div",{staticClass:"v-dialog__container",class:{"v-dialog__container--attached":""===this.attach||!0===this.attach||"attach"===this.attach},attrs:{role:"dialog"}},[this.genActivator(),this.genContent()])}})},"368e":function(t,e,i){},"99d9":function(t,e,i){"use strict";i.d(e,"a",(function(){return s})),i.d(e,"b",(function(){return r})),i.d(e,"c",(function(){return c}));var n=i("b0af"),a=i("80d2");const s=Object(a["i"])("v-card__actions"),o=Object(a["i"])("v-card__subtitle"),r=Object(a["i"])("v-card__text"),c=Object(a["i"])("v-card__title");n["a"]},a8a6:function(t,e,i){"use strict";i.r(e);var n=function(){var t=this,e=t.$createElement,i=t._self._c||e;return i("v-container",{attrs:{"fill-height":"",fluid:""}},[i("v-row",{attrs:{justify:"center",align:"center"}},[i("v-col",[i("v-card",{staticClass:"mx-auto",attrs:{outlined:""}},[i("div",{attrs:{id:"top"}}),i("v-row",{attrs:{justify:"center"}},[i("div",{attrs:{id:"top"}}),i("v-col",{attrs:{cols:"12"}},[i("v-data-table",{attrs:{headers:t.headers,items:t.videoList,"hide-default-footer":"","items-per-page":t.size,page:t.page},on:{"update:page":function(e){t.page=e}},scopedSlots:t._u([{key:"item.createTime",fn:function(e){var i=e.item;return[t._v(" "+t._s(t.TimeUtil.renderTime(i.createTime))+" ")]}},{key:"item.type",fn:function(e){var i=e.item;return[t._v(" "+t._s(t.getType(i.type))+" ")]}},{key:"item.userId",fn:function(e){var n=e.item;return[i("a",{attrs:{href:"/user/"+n.userId,target:"_blank"}},[t._v(" "+t._s(n.userId)+" ")])]}},{key:"item.imgUrl",fn:function(t){var e=t.item;return[i("v-img",{attrs:{src:e.imgUrl,"max-height":"200px","aspect-ratio":"1.77"}})]}},{key:"item.actions",fn:function(e){var n=e.item;return[i("a",{attrs:{href:"/video/"+n.id,target:"_blank"}},[i("v-icon",{staticClass:"mr-2"},[t._v(" mdi-video ")])],1),i("v-tooltip",{attrs:{left:""},scopedSlots:t._u([{key:"activator",fn:function(e){var a=e.on,s=e.attrs;return[i("v-btn",t._g(t._b({staticClass:"mr-2",attrs:{icon:""},on:{click:function(e){return t.examine(n)}}},"v-btn",s,!1),a),[i("v-icon",[t._v("mdi-pencil")])],1)]}}],null,!0)},[i("span",[t._v("审核意见")])])]}},{key:"no-data",fn:function(){return[i("v-btn",{attrs:{color:"primary"},on:{click:t.init}},[t._v("重新加载")])]},proxy:!0}])})],1)],1),i("v-row",{attrs:{justify:"center"}},[i("v-pagination",{attrs:{length:t.length},on:{input:t.pageChange},model:{value:t.page,callback:function(e){t.page=e},expression:"page"}})],1)],1)],1)],1),i("v-dialog",{attrs:{persistent:"","max-width":"600px"},model:{value:t.dialog,callback:function(e){t.dialog=e},expression:"dialog"}},[i("v-card",[i("v-card-title",[i("span",{staticClass:"headline"},[t._v("审核 - "+t._s(t.nowExamineItem.title))])]),i("v-card-text",[i("v-container",[i("v-row",[i("v-col",[i("v-select",{attrs:{items:t.examineStatus,label:"审核结果"},on:{change:t.getexamineStatus}})],1)],1),t.showError?i("v-row",[i("v-col",[i("v-select",{attrs:{items:t.examineItem,label:"不通过原因"},on:{change:t.getErrorString}})],1)],1):t._e(),t.showError?i("v-row",[i("v-col",[i("v-text-field",{attrs:{label:"处理意见"},model:{value:t.examineMessage,callback:function(e){t.examineMessage=e},expression:"examineMessage"}})],1)],1):t._e()],1)],1),i("v-card-actions",[i("v-spacer"),i("v-btn",{attrs:{color:"blue darken-1",text:""},on:{click:function(e){t.dialog=!1}}},[t._v("取消")]),i("v-btn",{attrs:{color:"blue darken-1",text:""},on:{click:t.save}},[t._v("确认")])],1)],1)],1),i("v-snackbar",{attrs:{top:!0,timeout:3e3},scopedSlots:t._u([{key:"action",fn:function(e){var n=e.attrs;return[i("v-btn",t._b({attrs:{color:"pink",text:""},on:{click:function(e){t.showMessage=!1}}},"v-btn",n,!1),[t._v(" 关闭 ")])]}}]),model:{value:t.showMessage,callback:function(e){t.showMessage=e},expression:"showMessage"}},[t._v(" "+t._s(t.message)+" ")])],1)},a=[],s=(i("99af"),i("d3b7"),i("9878")),o={name:"Examine",data:function(){return{showMessage:!1,message:"",errorType:"",examineMessage:"",examineStatus:["通过","不通过"],showError:!1,examineItem:[],dialog:!1,nowExamineItem:{},TimeUtil:s["a"],videoList:[],headers:[{text:"视频ID",sortable:!1,value:"id"},{text:"创建时间",sortable:!1,value:"createTime"},{text:"用户ID",sortable:!1,value:"userId"},{text:"标题",sortable:!1,value:"title"},{text:"封面",sortable:!1,value:"imgUrl"},{text:"分区id",sortable:!1,value:"category"},{text:"类型",sortable:!1,value:"type"},{text:"标签",sortable:!1,value:"tag"},{text:"操作",value:"actions",sortable:!1}],page:1,size:20,length:1}},created:function(){this.init(),this.setExamineItem()},methods:{save:function(){var t=this,e=!0;e=!1===this.showError;var i={videoId:this.nowExamineItem.id,result:e,type:this.errorType,message:this.examineMessage};fetch("/api/admin/examine",{headers:{"Content-Type":"application/json; charset=UTF-8","X-XSRF-TOKEN":this.$cookies.get("XSRF-TOKEN")},method:"POST",credentials:"include",body:JSON.stringify(i)}).then((function(t){return t.json()})).then((function(e){200===e.status?(t.message="审核结束",t.showMessage=!0,t.init(),t.dialog=!1):(t.message="审核失败"+e.message,t.showMessage=!0,t.init(),t.dialog=!1)})).catch((function(t){return null}))},getErrorString:function(t){this.errorType=t,console.log(t)},getexamineStatus:function(t){this.showError="不通过"===t,console.log(t)},examine:function(t){this.dialog=!0,this.nowExamineItem=t},init:function(){var t=this;fetch("/api/admin/article/list?page=".concat(this.page,"&limit=").concat(this.size),{headers:{"Content-Type":"application/json; charset=UTF-8","X-XSRF-TOKEN":this.$cookies.get("XSRF-TOKEN")},method:"GET",credentials:"include"}).then((function(t){return t.json()})).then((function(e){t.videoList=e.data.list,t.page=e.data.currPage,t.length=e.data.totalPage})).catch((function(t){return null}))},setExamineItem:function(){var t=this;fetch("/api/examine/item",{headers:{"Content-Type":"application/json; charset=UTF-8","X-XSRF-TOKEN":this.$cookies.get("XSRF-TOKEN")},method:"GET",credentials:"include"}).then((function(t){return t.json()})).then((function(e){t.examineItem=e.data})).catch((function(t){return null}))},pageChange:function(t){this.page=t,this.videoList(),document.querySelector("#top").scrollIntoView()},getType:function(t){return 0===t?"视频":1===t?"图片":2===t?"文章":"音乐"}}},r=o,c=i("2877"),l=i("6544"),d=i.n(l),h=i("8336"),u=i("b0af"),v=i("99d9"),m=i("62ad"),g=i("a523"),f=i("8fea"),p=i("169a"),b=i("132d"),x=i("adda"),w=i("891e"),y=i("0fd9"),k=i("b974"),_=i("2db4"),T=i("2fa4"),C=i("8654"),S=i("3a2f"),E=Object(c["a"])(r,n,a,!1,null,null,null);e["default"]=E.exports;d()(E,{VBtn:h["a"],VCard:u["a"],VCardActions:v["a"],VCardText:v["b"],VCardTitle:v["c"],VCol:m["a"],VContainer:g["a"],VDataTable:f["a"],VDialog:p["a"],VIcon:b["a"],VImg:x["a"],VPagination:w["a"],VRow:y["a"],VSelect:k["a"],VSnackbar:_["a"],VSpacer:T["a"],VTextField:C["a"],VTooltip:S["a"]})}}]);