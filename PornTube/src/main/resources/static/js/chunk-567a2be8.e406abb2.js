(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-567a2be8"],{"6a3a":function(t,e,a){"use strict";a("af77")},a192:function(t,e,a){"use strict";a.r(e);var i=a("8336"),s=a("62ad"),o=a("a523"),r=a("ce7e"),h=a("891e"),g=a("0fd9"),n=function(){var t=this,e=t._self._c;return e(o["a"],{staticStyle:{"padding-left":"24px","padding-right":"24px"},attrs:{"fill-height":"",fluid:""}},[e(g["a"],[e(s["a"],[e(i["a"],{staticStyle:{"font-weight":"bold","font-size":"20px"},attrs:{large:"",text:"",color:"primary"},on:{click:function(e){return t.setCategoryFather()}}},[t._v(" "+t._s(t.categoryList.name)+" ")]),t.showChileName?e("span",{staticStyle:{"font-weight":"bold","font-size":"20px"}},[t._v(" / "),e(i["a"],{staticStyle:{"font-weight":"bold","font-size":"20px"},attrs:{large:"",text:"",color:"primary"}},[t._v(" "+t._s(t.categoryChiled.name)+" ")])],1):t._e()],1)],1),e(g["a"],[e(s["a"],t._l(t.categoryList.children,(function(a){return e(i["a"],{key:a.id,attrs:{text:"",color:"primary"},on:{click:function(e){return t.setCategory(a)}}},[t._v(" "+t._s(a.name)+" ")])})),1)],1),e(g["a"],[e(r["a"])],1),e(s["a"]),e(g["a"],{staticStyle:{"padding-top":"12px","padding-bottom":"12px"},attrs:{"no-gutters":""}},t._l(t.videoList,(function(t){return e(s["a"],{key:t.id},[e("VideoCared",{attrs:{video:t}})],1)})),1),e(g["a"],{attrs:{justify:"center"}},[e(h["a"],{attrs:{length:t.length},on:{input:t.pageChange},model:{value:t.page,callback:function(e){t.page=e},expression:"page"}})],1),e(s["a"],[t._v("   ")])],1)},d=[],c=(a("14d9"),a("cbe6")),l={name:"Category",components:{VideoCared:c["a"]},data(){return{videoList:[],page:1,size:24,length:0,categoryId:0,categoryList:[],categoryChiled:{},showChileName:!1}},created(){void 0===this.$route.query.page?this.page=1:this.page=this.$route.query.page,this.categoryId=parseInt(this.$route.params.id),this.getCategory(),this.getVideoList()},methods:{setCategoryFather(){this.categoryList.id!==this.categoryId&&(this.categoryId=this.categoryList.id,this.showChileName=!1,this.getVideoList())},getCategory(){this.httpGet("/category/tree?category="+this.categoryId,t=>{this.categoryList=t.data,this.categoryList.id!==this.categoryId&&(this.showChileName=!0,this.getNowCategory())})},getNowCategory(){for(let t=0;t<this.categoryList.children.length;t++){const e=this.categoryList.children[t];if(e.id===this.categoryId)return void(this.categoryChiled=e)}},getVideoList(){this.httpGet(`/article/category/${this.categoryId}?page=${this.page}&limit=${this.size}`,t=>{null==t.data?(this.videoList=[],this.page=1,this.length=0):(this.videoList=t.data.list,this.page=t.data.currPage,this.length=t.data.totalPage)})},setCategory(t){this.categoryId=t.id,this.showChileName=!0,this.categoryChiled=t,this.getVideoList()},pageChange(t){this.page=t,this.$router.push({query:{page:this.page}}),this.getVideoList(),this.$vuetify.goTo(0)}}},y=l,p=(a("6a3a"),a("2877")),u=Object(p["a"])(y,n,d,!1,null,null,null);e["default"]=u.exports},af77:function(t,e,a){}}]);