<template>
  <div>
    <v-container fluid>
      <!-- 添加 Snackbar 组件 -->
      <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="2000" location="top">
        {{ snackbar.text }}
      </v-snackbar>
    </v-container>

    <!-- 文本稿件详情 -->
    <v-container v-if="textData != null" style="padding-top: 0px">
      <v-row>
        <v-col :cols="colsWidth">
          <!-- 文本标题 -->
          <v-card flat>
            <v-card-title class="text-h5 px-0">
              {{ textData.title }}
            </v-card-title>

            <!-- 文本交互工具栏 -->
            <v-card-actions class="px-0 py-2">
              <div class="d-flex flex-wrap align-center w-100">
                <div class="d-flex flex-wrap">
                  <v-btn
                    prepend-icon="mdi-thumb-up"
                    :variant="isLiked ? 'flat' : 'tonal'"
                    color="primary"
                    @click="likeText"
                    class="ma-1"
                  >
                    {{ textData.likeCount }}
                  </v-btn>
                  <v-btn
                    prepend-icon="mdi-thumb-down"
                    :variant="isDisliked ? 'flat' : 'tonal'"
                    color="error"
                    @click="dislikeText"
                    class="ma-1"
                  >
                    {{ textData.dislikeCount }}
                  </v-btn>
                  <v-btn
                    prepend-icon="mdi-share"
                    variant="tonal"
                    @click="shareDialog = true"
                    class="ma-1"
                  >
                    分享
                  </v-btn>
                  <v-btn
                    prepend-icon="mdi-content-save"
                    color="orange-lighten-2"
                    :variant="isFavorited ? 'flat' : 'tonal'"
                    @click="favoritesClick"
                    class="ma-1"
                  >
                    <span v-if="isFavorited">取消收藏</span>
                    <span v-else>收藏</span>
                  </v-btn>
                </div>

                <v-spacer class="d-none d-sm-block"></v-spacer>

                <div class="d-flex flex-wrap mt-2 mt-sm-0">
                  <v-chip variant="outlined" color="orange-lighten-2" class="ma-1">
                    {{ textData.favoriteCount }} 次收藏
                  </v-chip>
                  <v-chip variant="outlined" color="blue" class="ma-1">
                    {{ textData.viewCount }} 次查看
                  </v-chip>
                </div>
              </div>
            </v-card-actions>

            <!-- 文本分类信息 -->
            <v-card-subtitle class="px-0 py-2">
              <router-link
                v-if="textData.childrenCategory.fatherId !== 0"
                :to="`/v/${textData.fatherCategory.id}`"
                class="category-link text-decoration-none"
              >
                {{ textData.fatherCategory.name }}
              </router-link>
              /
              <router-link
                :to="`/v/${textData.childrenCategory.id}`"
                class="category-link text-decoration-none"
              >
                {{ textData.childrenCategory.name }}
              </router-link>
              &nbsp;&nbsp;&nbsp;&nbsp; 发布于：
              {{ TimeUtil.renderTime(textData.createTime) }}

              <v-btn
                class="ml-2"
                color="red"
                size="small"
                variant="text"
                @click="showReportDialog = true"
              >
                举报
              </v-btn>
            </v-card-subtitle>

            <v-divider class="my-2"></v-divider>
          </v-card>

          <!-- 文本内容显示区域 -->
          <v-card class="my-2" variant="elevated">
            <v-card-title>文章内容</v-card-title>
            <v-divider></v-divider>
            <v-card-text>
              <!-- 循环展示所有段落内容 -->
              <div v-for="(segment, index) in textData.text" :key="index" class="mb-6">
                <!-- 如果有多个段落，显示段落标题 -->
                <!-- <div v-if="textData.text.length > 1" class="text-subtitle-1 font-weight-medium mb-2">
                  第 {{ index + 1 }} 段
                </div> -->
                
                <!-- 根据文本类型显示不同内容 -->
                <div v-if="segment.type === 0 || segment.content">
                  <ShowMarkdown :markdown="segment.content" :speech="false" :anchor="index + 1" />
                </div>
                <div v-else-if="segment.type === 1" class="text-center py-4">
                  <v-icon size="large" color="warning" class="mb-2">mdi-comment-alert</v-icon>
                  <p class="text-h6">回复后可见</p>
                  <p class="text-body-2 text-grey">评论本文章后即可查看全部内容</p>
                </div>
                <div v-else-if="segment.type === 2" class="text-center py-4">
                  <v-icon size="large" color="warning" class="mb-2">mdi-lock</v-icon>
                  <p class="text-h6">密码保护</p>
                  <p class="text-body-2 text-grey mb-4">此内容已被作者加密，请输入密码查看</p>

                  <v-form @submit.prevent="checkPassword(segment, index)">
                    <v-row justify="center">
                      <v-col cols="12" sm="8" md="6">
                        <v-text-field
                          v-model="passwordInputs[index]"
                          label="请输入密码"
                          variant="outlined"
                          :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
                          :type="showPassword ? 'text' : 'password'"
                          @click:append-inner="showPassword = !showPassword"
                        ></v-text-field>
                      </v-col>
                    </v-row>
                    <v-btn color="primary" type="submit" class="mt-2">确认</v-btn>
                  </v-form>
                </div>
                <div v-else class="text-center py-4">
                  <v-icon size="large" color="error" class="mb-2">mdi-alert-circle</v-icon>
                  <p class="text-body-1">内容不可用</p>
                </div>
                
                <!-- 如果有多个段落且不是最后一个，添加分隔线 -->
                <v-divider v-if="textData.text.length > 1 && index < textData.text.length - 1" class="my-4"></v-divider>
              </div>
            </v-card-text>
          </v-card>

          <!-- 用户信息和文本描述集成 -->
          <v-card class="my-2" variant="elevated">
            <v-row no-gutters>
              <!-- 左侧用户信息 -->
              <v-col cols="12" sm="auto" class="pa-2">
                <div class="d-flex align-center">
                  <router-link :to="`/user/${textData.userId}`">
                    <v-avatar size="48" class="mr-2">
                      <v-img :src="textData.avatarUrl" alt="用户头像"></v-img>
                    </v-avatar>
                  </router-link>
                  <div>
                    <router-link :to="`/user/${textData.userId}`" class="text-decoration-none">
                      <div class="text-subtitle-1 font-weight-bold">{{ textData.username }}</div>
                    </router-link>
                    <div class="text-caption text-grey">个性签名: {{ textData.introduction }}</div>
                  </div>
                  <v-spacer></v-spacer>
                  <v-btn
                    v-if="textData.userId !== userInfo.userData?.id"
                    :color="isFollowed ? 'grey' : 'primary'"
                    :variant="isFollowed ? 'flat' : 'outlined'"
                    density="comfortable"
                    class="ml-2"
                    :prepend-icon="isFollowed ? 'mdi-account-check' : 'mdi-account-plus'"
                    @click="toggleFollow"
                  >
                    {{ textData.fansCount }} {{ isFollowed ? '已关注' : '关注' }}
                  </v-btn>
                </div>
              </v-col>
            </v-row>

            <v-divider></v-divider>

            <!-- 文本描述 -->
            <v-card-text class="py-2">
              <div class="d-flex align-start">
                <div class="text-body-2">{{ textData.describes }}</div>
              </div>
            </v-card-text>

            <!-- 标签 -->
            <v-card-text class="pt-0 pb-2">
              <v-chip-group>
                <v-chip
                  v-for="item in textData.tag"
                  :key="item"
                  size="small"
                  variant="flat"
                  color="primary"
                  density="comfortable"
                  class="mr-1"
                >
                  {{ item }}
                </v-chip>
              </v-chip-group>
            </v-card-text>
          </v-card>

          <!-- 评论区 -->
          <Comment
            :article="id"
            :author-id="textData.userId"
            :typenum="1"
            :count="textData.commentCount"
          />
        </v-col>
      </v-row>

      <v-dialog v-model="showReportDialog" width="50vh">
        <OpinionCard
          :targetId="id"
          :typeNum="0"
          :target-title="textData.title"
          :isReport="true"
          @close="showReportDialog = false"
          @success="handleReportSuccess"
        />
      </v-dialog>

      <!-- 分享卡片弹窗 -->
      <v-dialog max-width="600" v-model="shareDialog">
        <ShareCard :article="textData" />
      </v-dialog>
    </v-container>
  </div>
</template>

<script>
import TimeUtil from '@/utils/time-util'
import Comment from '@/views/comment/VideoComment.vue'
import { useUserStore } from '@/stores/userStore'
import ShareCard from '@/components/card/ShareCard.vue'
import ShowMarkdown from '@/components/vditor/ShowMarkdown.vue'
import Power from '@/utils/check-power'
import OpinionCard from '@/components/card/OpinionCard.vue'

export default {
  name: 'TextView',
  components: {
    Comment,
    ShareCard,
    ShowMarkdown,
    OpinionCard,
  },
  data() {
    return {
      TimeUtil,
      id: 0,
      textData: null,
      textContent: null,  // 保留这个变量以兼容现有代码
      passwordInputs: [], // 存储每个段落的密码输入
      windowSize: {},
      colsWidth: 12,
      isLiked: false,
      isDisliked: false,
      isFavorited: false,
      isFollowed: false,
      userInfo: useUserStore(),
      snackbar: {
        show: false,
        text: '',
        color: 'success',
      },
      shareDialog: false,
      showReportDialog: false,
      password: '',
      showPassword: false,
    }
  },
  created() {
    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    })
    this.id = parseInt(this.$route.params.id)
    this.getTextInfo()
    window.addEventListener('resize', this.onResize)
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.onResize)
  },
  methods: {
    handleReportSuccess() {
      // 处理举报成功的逻辑
    },
    getTextInfo() {
      this.httpGet(`/article/text/${this.id}`, (json) => {
        if (json.status === 200 && json.data.isShow) {
          this.textData = json.data
          // 保留textContent以兼容现有代码
          this.textContent = this.textData.text.length > 0 ? this.textData.text[0] : null
          // 初始化密码输入数组
          this.passwordInputs = new Array(this.textData.text.length).fill('')
          document.title = json.data.title
          // 假设数据中包含点赞和点踩数量，如果没有则使用默认值
          this.likeCount = json.data.likeCount || 0
          this.dislikeCount = json.data.dislikeCount || 0
          this.checkLike()
          this.checkDislike()
          this.checkFavorites()
          this.checkFollow()
        } else {
          // 显示 404
          this.$router.push('/')
        }
      })
    },
    // 检查密码
    checkPassword(segment, index) {
      if (!this.passwordInputs[index]) {
        this.showMessage('请输入密码', 'warning')
        return
      }

      // 准备发送到服务器的数据
      const passwordData = {
        ...segment,
        password: this.passwordInputs[index],
      }

      // 发送密码验证请求
      this.httpPost('/article/text/password', passwordData, (json) => {
        if (json.status === 200 && json.data) {
          // 密码正确，服务器返回了包含content的完整内容
          this.textData.text[index] = json.data
          // 更改类型为0以显示内容
          this.textData.text[index].type = 0
          this.showMessage('密码正确，已显示内容', 'success')
        } else {
          // 密码错误或请求失败
          this.showMessage(json.message || '密码错误，请重试', 'error')
        }
      })
    },
    // 点赞功能
    likeText() {
      // 如果用户未登录
      if (this.userInfo.userData == null) {
        this.showMessage('请先登录后再点赞', 'warning')
        return
      }
      this.httpPost(`/like/toggle?likeObjId=${this.id}&type=0`, {}, (json) => {
        if (json.status === 200) {
          // 更新点赞状态
          if (json.data.like) {
            this.isLiked = !this.isLiked
            // 更新点赞数量
            this.textData.likeCount += this.isLiked ? 1 : -1
            // 显示消息提示
            if (this.isLiked) {
              this.showMessage('点赞成功', 'success')
            } else {
              this.showMessage('取消点赞成功', 'info')
            }
          }
        } else {
          this.showMessage(json.data.info || '操作失败', 'error')
        }
      })
    },
    favoritesClick() {
      if (this.userInfo.userData == null) {
        this.showMessage('请先登录后再收藏', 'warning')
        return
      }
      this.httpPost('/favorites/toggle', { articeId: this.id }, (json) => {
        if (json.data.id != null) {
          this.showMessage('收藏成功！', 'success')
          this.isFavorited = true
          this.textData.favoriteCount += 1
        } else {
          this.showMessage('取消收藏成功！', 'info')
          this.isFavorited = false
          this.textData.favoriteCount -= 1
        }
      })
    },
    checkFavorites() {
      if (this.userInfo.userData == null) {
        this.isFavorited = false
        return
      }
      this.httpGet(`/favorites/check?articleId=${this.id}`, (json) => {
        if (json.status === 200) {
          this.isFavorited = json.data
        }
      })
    },
    // 关注功能
    toggleFollow() {
      if (this.userInfo.userData == null) {
        this.showMessage('请先登录后再关注', 'warning')
        return
      }

      if (this.isFollowed) {
        this.unfollowUser()
      } else {
        this.followUser()
      }
    },
    followUser() {
      if (this.userInfo.userData == null) {
        this.showMessage('请先登录后再关注', 'warning')
        return
      }

      const data = {
        followUser: this.textData.userId,
        createUser: this.userInfo.userData.id,
      }

      this.httpPost('/follow/add', data, (json) => {
        if (json.data) {
          this.isFollowed = true
          if (this.textData.fansCount !== undefined) {
            this.textData.fansCount += 1
          }
          this.showMessage('关注成功', 'success')
        } else {
          this.showMessage('关注失败，请稍后重试', 'error')
        }
      })
    },
    unfollowUser() {
      if (this.userInfo.userData == null) {
        this.showMessage('请先登录', 'warning')
        return
      }

      const data = {
        followUser: this.textData.userId,
        createUser: this.userInfo.userData.id,
      }

      this.httpPost('/follow/delete', data, (json) => {
        if (json.data) {
          this.isFollowed = false
          if (this.textData.fansCount !== undefined && this.textData.fansCount > 0) {
            this.textData.fansCount -= 1
          }
          this.showMessage('已取消关注', 'info')
        } else {
          this.showMessage('取消关注失败，请稍后重试', 'error')
        }
      })
    },
    checkFollow() {
      if (this.userInfo.userData == null || this.textData.userId === this.userInfo.userData.id) {
        this.isFollowed = false
        return
      }

      this.httpGet(`/follow/check?followId=${this.textData.userId}`, (json) => {
        this.isFollowed = json.data
      })
    },
    checkLike() {
      if (this.userInfo.userData == null) {
        this.isLiked = false
        return
      }
      this.httpGet(`/like/status?likeObjId=${this.id}&type=0`, (json) => {
        if (json.status === 200) {
          this.isLiked = json.data
        }
      })
    },
    checkDislike() {
      if (this.userInfo.userData == null) {
        this.isDisliked = false
        return
      }
      this.httpGet(`/dislike/status?dislikeObjId=${this.id}&type=0`, (json) => {
        if (json.status === 200) {
          this.isDisliked = json.data
        }
      })
    },
    // 点踩功能
    dislikeText() {
      if (this.userInfo.userData == null) {
        this.showMessage('请先登录后再点踩', 'warning')
        return
      }
      this.httpPost(`/dislike/toggle?dislikeObjId=${this.id}&type=0`, {}, (json) => {
        if (json.status === 200) {
          // 更新点踩状态
          if (json.data.dislike) {
            this.isDisliked = !this.isDisliked
            // 更新点踩数量
            this.textData.dislikeCount += this.isDisliked ? 1 : -1
            // 显示消息提示
            this.showMessage(json.data.info, 'success')
          }
        } else {
          this.showMessage(json.data.info || '操作失败', 'error')
        }
      })
    },
    // 显示消息提示
    showMessage(text, color = 'success') {
      this.snackbar.text = text
      this.snackbar.color = color
      this.snackbar.show = true
    },
  },
}
</script>

<style>
.category-link {
  color: #1867c0;
}
</style>