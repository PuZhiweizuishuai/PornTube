<template>
  <v-container>
    <!-- 展示搜到的用户 -->
    <v-row v-if="userList.length > 0">
      <v-col cols="12">
        <div class="d-flex align-center mb-3">
          <h3 class="text-subtitle-1 font-weight-medium mb-0">相关用户</h3>
          <v-spacer></v-spacer>
          <v-btn
            v-if="userTotalCount > userSize"
            variant="text"
            color="primary"
            density="comfortable"
            @click="showAllUsers"
          >
            查看全部
            <v-icon end>mdi-chevron-right</v-icon>
          </v-btn>
        </div>

        <!-- 用户列表 -->
        <v-card variant="flat" class="mb-4 pa-0 overflow-hidden">
          <v-slide-group show-arrows class="py-3">
            <v-slide-group-item v-for="user in userList" :key="user.id">
              <v-hover v-slot="{ isHovering, props }">
                <v-card
                  v-bind="props"
                  :elevation="isHovering ? 3 : 0"
                  class="ma-2 rounded-lg"
                  width="120"
                  height="160"
                  :class="isHovering ? 'on-hover' : ''"
                  @click="goToUserPage(user.id)"
                >
                  <div class="d-flex flex-column align-center py-3 px-2 h-100 mb-2">
                    <v-avatar size="70" class="mb-2">
                      <v-img :src="user.avatarUrl" :alt="user.username" cover></v-img>
                    </v-avatar>
                    <div class="text-center mb-2">
                      <div
                        class="text-subtitle-2 font-weight-medium text-truncate"
                        style="max-width: 110px"
                      >
                        {{ user.username }}
                      </div>
                      <div class="text-caption text-medium-emphasis mt-1">
                        {{ user.fansCount }} 粉丝
                      </div>
                    </div>
                  </div>
                </v-card>
              </v-hover>
            </v-slide-group-item>
          </v-slide-group>
        </v-card>
      </v-col>
    </v-row>

    <!-- 展示搜到的视频 -->
    <v-row>
      <v-col> 共找到 {{ totalCount }} 个与 {{ searchKey }} 相关的结果 </v-col>
    </v-row>
    <v-row>
      <v-col cols="12" sm="6" md="4" xl="3" v-for="item in videoList" :key="item.id">
        <VideoCared v-if="item.type == 0" :video="item" />
        <ArticleCard v-if="item.type == 2" :text="item" />
      </v-col>
    </v-row>
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

    <!-- 全部用户对话框 -->
    <v-dialog v-model="userDialog" max-width="800" transition="dialog-bottom-transition">
      <v-card>
        <v-toolbar color="primary" density="comfortable">
          <v-toolbar-title class="text-white">全部相关用户</v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn icon @click="userDialog = false">
            <v-icon color="white">mdi-close</v-icon>
          </v-btn>
        </v-toolbar>

        <v-card-text class="pa-4">
          <v-row v-if="allUsers.length === 0" justify="center" class="my-8">
            <v-progress-circular
              indeterminate
              color="primary"
              :size="40"
              :width="3"
            ></v-progress-circular>
          </v-row>

          <v-row v-else>
            <v-col
              v-for="user in allUsers"
              :key="user.id"
              cols="6"
              sm="4"
              md="3"
              class="d-flex"
            >
              <v-hover v-slot="{ isHovering, props }">
                <v-card
                  v-bind="props"
                  :elevation="isHovering ? 3 : 1"
                  class="ma-2 rounded-lg flex-grow-1"
                  :class="isHovering ? 'on-hover' : ''"
                  @click="goToUserPage(user.id)"
                >
                  <div class="d-flex flex-column align-center pa-3">
                    <v-avatar size="70" class="mb-3">
                      <v-img :src="user.avatarUrl" :alt="user.username" cover></v-img>
                    </v-avatar>
                    <div class="text-center">
                      <div class="text-subtitle-2 font-weight-medium text-truncate" style="max-width: 110px">
                        {{ user.username }}
                      </div>
                      <div class="text-caption text-medium-emphasis mt-1">
                        {{ user.fansCount }} 粉丝，{{ user.submitCount }} 投稿
                      </div>
                      <v-btn
                        size="small"
                        class="mt-3"
                        color="primary"
                        variant="tonal"
                        block
                        :to="`/user/${user.id}`"
                      >
                        查看
                      </v-btn>
                    </div>
                  </div>
                </v-card>
              </v-hover>
            </v-col>
          </v-row>

          <!-- 分页控件 - 全部用户弹窗 -->
          <div v-if="userLength > 1" class="d-flex justify-center mt-4">
            <Pagination
              v-model="userPage"
              :length="userLength"
              :total-visible="7"
              rounded="circle"
              @update:model-value="loadAllUsers"
            />
          </div>
        </v-card-text>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script>
import VideoCared from '@/components/card/VideoCard.vue'
import ArticleCard from '@/components/card/ArticleCard.vue'
import Pagination from '@/components/common/Pagination.vue'
export default {
  components: {
    VideoCared,
    ArticleCard,
    Pagination
  },
  data() {
    return {
      videoList: [],
      page: 1,
      size: 24,
      length: 0,
      categoryList: [],
      searchKey: '',
      totalCount: 0,
      userList: [],
      allUsers: [],
      userPage: 1,
      userSize: 12,
      userLength: 0,
      userTotalCount: 0,
      userDialog: false,
    }
  },
  created() {
    const page = parseInt(this.$route.query.page)
    if (!isNaN(page)) {
      if (page <= 0) {
        this.page = 1
      } else {
        this.page = page
      }
    }
    this.searchKey = this.$route.query.key
    console.log(this.searchKey)
    this.getVideoList()
    this.getUserList()
  },
  methods: {
    getVideoList() {
      this.httpGet(
        `/article/home/list?page=${this.page}&limit=${this.size}&search=${this.searchKey}`,
        (json) => {
          this.videoList = json.data.list
          this.page = json.data.currPage
          this.length = json.data.totalPage
          this.totalCount = json.data.totalCount
        }
      )
    },
    getUserList() {
      this.httpGet(
        `/user/list/search?page=${this.userPage}&limit=${this.userSize}&search=${this.searchKey}`,
        (json) => {
          if (json.status === 200) {
            this.userList = json.data.list || []
            this.userPage = json.data.currPage
            this.userLength = json.data.totalPage
            this.userTotalCount = json.data.totalCount
          }
        }
      )
    },
    loadAllUsers() {
      this.httpGet(
        `/user/list/search?page=${this.userPage}&limit=${this.userSize}&search=${this.searchKey}`,
        (json) => {
          if (json.status === 200) {
            this.allUsers = json.data.list || []
            this.userLength = json.data.totalPage
            this.userTotalCount = json.data.totalCount
          }
        }
      )
    },
    showAllUsers() {
      this.userPage = 1
      this.loadAllUsers()
      this.userDialog = true
    },
    goToUserPage(userId) {
      this.$router.push(`/user/${userId}`)
    },
    pageChange(value) {
      this.page = value
      this.$router.push({ query: { page: this.page, key: this.searchKey } })
      this.getVideoList()
      window.scrollTo({
        top: 0,
        behavior: 'smooth',
      })
    },
    setSearchKey(value) {
      this.searchKey = value
      this.getVideoList()
      this.getUserList()
    },
  },
}
</script>

<style scoped>
.on-hover {
  transform: translateY(-4px);
  transition: transform 0.3s ease;
}

.v-card {
  transition: all 0.3s ease;
}

@media (max-width: 600px) {
  .v-slide-group__content {
    padding-left: 16px;
  }
}
</style>