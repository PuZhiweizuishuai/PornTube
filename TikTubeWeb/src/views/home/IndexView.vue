<template>
  <v-container fluid>

    <v-banner @click="clickBanner()" class="my-4" color="warning" icon="$warning" lines="one" v-if="systemNotice != null">
      <v-banner-text>
        {{ systemNotice.content }}
      </v-banner-text>
    </v-banner>
    <!-- 分区 -->
    <v-row>
      <v-col>
        <v-menu v-for="item in categoryList" :key="item.id" open-on-hover offset-y>
          <template v-slot:activator="{ props }">
            <v-btn variant="text" color="primary" v-bind="props" @click="setCategory(item)">
              {{ item.name }}
            </v-btn>
          </template>
          <v-list>
            <v-list-item
              v-for="c in item.children"
              :key="c.id"
              :title="c.name"
              @click="setCategory(c)"
            >
            </v-list-item>
          </v-list>
        </v-menu>
      </v-col>
    </v-row>
    <v-row>
      <v-divider />
    </v-row>
    <v-col />
    <!-- 视频卡片 -->
    <v-row>
      <v-col cols="12" sm="6" md="4" lg="3" xxl="2" v-for="item in videoList" :key="item.id">
        <VideoCared :video="item" />
      </v-col>
    </v-row>
    <v-container>
      <v-row justify="center">
        <Pagination
          rounded="circle"
          :total-visible="7"
          v-model="page"
          :length="length"
          color="red"
          @update:model-value="pageChange"
        />
      </v-row>
    </v-container>
    <v-col> &nbsp; </v-col>
    <!-- 文章卡片 -->
    <v-row>
      <v-col cols="12">
        <v-row class="d-flex align-center">
          <v-col>
            <h2 class="text-h5">文章</h2>
          </v-col>
          <v-col class="text-right">
            <v-btn
              variant="text"
              color="primary"
              prepend-icon="mdi-arrow-right"
              @click="goToArticleList"
            >
              查看更多
            </v-btn>
          </v-col>
        </v-row>
      </v-col>
    </v-row>
    <v-row>
      <v-col cols="12" v-for="item in textList" :key="item.id">
        <TextInfoCard :text="item" />
      </v-col>
    </v-row>



    <!-- 引入弹窗广告组件 -->
    <NoticeDialogCard
      v-model="popupDialog" 
      :notice="popupNotice"
      :closed-popup-id="'closedHomePopupId'"
    />
  </v-container>
</template>

<script>
import VideoCared from '@/components/card/VideoCard.vue'
import NoticeDialogCard from '@/components/card/NoticeDialogCard.vue'
import TextInfoCard from '@/components/card/TextInfoCard.vue'
import Pagination from '@/components/common/Pagination.vue'

export default {
  components: {
    VideoCared,
    NoticeDialogCard,
    TextInfoCard,
    Pagination
  },
  data() {
    return {
      videoList: [],
      textList: [],
      page: 1,
      size: 24,
      length: 0,
      categoryList: [],
      systemNotice: {},
      // 弹窗相关数据
      popupDialog: false,
      popupNotice: null,
      notice: null,
    }
  },
  created() {
    if (this.$route.query.page === undefined) {
      this.page = 1
    } else {
      this.page = parseInt(this.$route.query.page)
    }
    this.getCategory()
    this.getVideoList()
    this.getTextList()
    this.getSystemNotice()
    this.getPopupNotice()
  },
  methods: {
    getCategory() {
      this.httpGet(`/category/tree`, (json) => {
        this.categoryList = json.data
      })
    },
    getSystemNotice() {
      this.httpGet(`/web/notice?type=0`, (json) => {
        if (json.data != null && json.data.length != 0) {
          this.systemNotice = json.data[0]
        } else {
          this.systemNotice = null
        }
      })
    },
    // 获取弹窗公告
    getPopupNotice() {
      this.httpGet(`/web/notice?type=1`, (json) => {
        if (json.data != null && json.data.length > 0) {

          this.notice = json.data[0];
          
          // 从 localStorage 获取已关闭的弹窗ID
          const closedPopupId = localStorage.getItem('closedHomePopupId');
          
          // 判断是否需要显示弹窗
          // 1. 如果没有关闭过弹窗，或者关闭的弹窗ID与当前弹窗ID不同，则显示弹窗
          if (!closedPopupId || parseInt(closedPopupId) !== this.notice.id) {
            this.popupNotice = this.notice;
            this.popupDialog = true;
          }
        }
      })
    },
    clickBanner() {
      if (this.notice != null) {
        this.popupNotice = this.notice;
        this.popupDialog = true;
      }
    },
    setCategory(value) {
      this.$router.push(`/v/${value.id}`)
    },
    getVideoList() {
      this.httpGet(`/article/home/list?page=${this.page}&limit=${this.size}&type=0`, (json) => {
        this.videoList = json.data.list
        this.page = json.data.currPage
        this.length = json.data.totalPage
      })
    },
    getTextList() {
      this.httpGet(`/article/home/list?page=1&limit=15&type=2`, (json) => {
        this.textList = json.data.list
      })
    },
    pageChange(value) {
      this.page = value
      this.$router.push({ query: { page: this.page } })
      this.getVideoList()
      window.scrollTo({
        top: 0,
        behavior: 'smooth',
      })
    },
        // 跳转到文章列表页面
    goToArticleList() {
      this.$router.push('/articles')
    },
  },
}
</script>

<style scoped>
a {
  text-decoration: none;
}
</style>
