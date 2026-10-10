<template>
  <v-container fluid class="pa-0">
    <v-row justify="center" class="ma-0">
      <v-col cols="12" class="pa-0">
        <v-card class="mx-auto rounded-lg elevation-3" variant="elevated">
          <v-card-title class="text-h5 font-weight-bold px-6 pt-6 pb-4">
            <v-icon icon="mdi-file-document-edit" size="large" class="mr-2" color="primary"></v-icon>
            上传文章
          </v-card-title>

          <v-divider></v-divider>

          <!-- 基本信息区域 -->
          <v-container fluid>
            <!-- 分区选择 -->
            <v-row>
              <v-col cols="12" md="6">
                <v-select
                  v-model="selectedMainCategory"
                  :items="category"
                  label="主分区"
                  variant="outlined"
                  density="comfortable"
                  @update:model-value="getMainCategory"
                  prepend-inner-icon="mdi-shape-outline"
                  placeholder="请选择主分区"
                ></v-select>
              </v-col>
              <v-col cols="12" md="6">
                <v-select
                  v-model="selectedSubCategory"
                  :items="categoryChildren"
                  label="子分区"
                  variant="outlined"
                  density="comfortable"
                  @update:model-value="getArticleCategory"
                  prepend-inner-icon="mdi-tag-outline"
                  placeholder="请选择子分区"
                  :disabled="!categoryChildren.length"
                ></v-select>
              </v-col>
            </v-row>

            <!-- 标题和简介 -->
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="article.title"
                  placeholder="为你的文章添加一个吸引人的标题"
                  label="标题"
                  variant="outlined"
                  density="comfortable"
                  counter="100"
                  :rules="[
                    (v) => !!v || '标题不能为空',
                    (v) => (v && v.length <= 100) || '标题不能超过100个字符',
                  ]"
                  prepend-inner-icon="mdi-format-title"
                ></v-text-field>
              </v-col>
            </v-row>

            <v-row>
              <v-col cols="12">
                <v-textarea
                  v-model="article.describe"
                  label="简介"
                  variant="outlined"
                  counter="1000"
                  auto-grow
                  rows="3"
                  placeholder="填写更全面的文章信息，让更多人找到你的文章"
                  prepend-inner-icon="mdi-text-box-outline"
                  :rules="[(v) => !v || v.length <= 1000 || '简介不能超过1000个字符']"
                ></v-textarea>
              </v-col>
            </v-row>


            <!-- 封面图上传 -->
            <!-- <v-row class="my-3">
              <v-col cols="12" md="6">
                <v-card
                  variant="outlined"
                  class="thumbnail-preview"
                  :color="article.imgUrl ? 'background' : 'grey-lighten-3'"
                  height="200"
                >
                  <v-img
                    v-if="article.imgUrl"
                    :src="article.imgUrl"
                    aspect-ratio="16/9"
                    height="200"
                    alt="封面图预览"
                  >
                  </v-img>
                  <div v-else class="d-flex align-center justify-center fill-height text-grey">
                    <v-icon icon="mdi-image" size="large" class="mr-2"></v-icon>
                    <span>封面预览（可选）</span>
                  </div>
                </v-card>
              </v-col>

              <v-col cols="12" md="6">
                <v-card variant="outlined" class="pa-4">
                  <v-file-input
                    :rules="rules"
                    accept="image/png, image/jpeg, image/bmp"
                    placeholder="选择图片文件"
                    prepend-icon="mdi-camera"
                    label="自定义封面（可选）"
                    variant="outlined"
                    density="comfortable"
                    chips
                    show-size
                    @update:model-value="setFile"
                  ></v-file-input>
                  <v-btn
                    block
                    color="primary"
                    class="mt-2"
                    :disabled="!files.length"
                    @click="uploadFile"
                  >
                    <v-icon icon="mdi-cloud-upload" class="mr-1"></v-icon>
                    上传
                  </v-btn>
                </v-card>
              </v-col>
            </v-row> -->

            <v-divider class="my-4"></v-divider>

            <!-- Markdown编辑器 -->
            <v-row>
              <v-col cols="12">
                <div class="d-flex justify-space-between align-center mb-3">
                  <h3 class="text-h6 font-weight-medium">
                    <v-icon icon="mdi-file-document-edit-outline" class="mr-2"></v-icon>
                    文章内容
                  </h3>
                  <v-btn
                    v-if="articleSegments.length < 3"
                    color="primary"
                    variant="outlined"
                    prepend-icon="mdi-plus"
                    @click="addNewSegment"
                  >
                    添加段落
                  </v-btn>
                </div>
                
                <!-- 文章段落列表 -->
                <article-segment
                  v-for="(segment, index) in articleSegments"
                  :key="index"
                  :segment="segment"
                  :index="index"
                  :segments="articleSegments"
                  :article-type-options="articleTypeOptions"
                  @update-content="updateSegmentContent"
                  @update-type="updateSegmentType"
                  @remove-segment="removeSegment"
                  :ref="`segment${index}`"
                ></article-segment>
              </v-col>
            </v-row>

            
            <!-- 标签 -->
            <v-row>
              <v-col cols="12">
                <v-combobox
                  v-model="article.tag"
                  label="标签"
                  placeholder="添加标签让更多人找到你的文章（最多6个）"
                  multiple
                  chips
                  closable-chips
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-tag-multiple-outline"
                  :rules="[(v) => v && v.length > 0 || '至少添加一个标签', (v) => !v || v.length <= 6 || '最多只能添加6个标签']"
                ></v-combobox>
              </v-col>
            </v-row>

            <!-- 提交按钮 -->
            <v-row class="mt-4">
              <v-col cols="12" class="d-flex justify-end">
                <v-btn
                  color="primary"
                  size="large"
                  rounded
                  min-width="120"
                  :loading="isSubmitting"
                  :disabled="isSubmitting || !isFormValid"
                  @click="publishArticle"
                >
                  <v-icon icon="mdi-send" class="mr-1"></v-icon>
                  立即投稿
                </v-btn>
              </v-col>
            </v-row>
          </v-container>
        </v-card>
      </v-col>
    </v-row>

    <!-- 消息提示：统一由 useMessage + AppSnackbar 承载 -->
    <AppSnackbar :state="toast" />
  </v-container>
</template>

<script>
import ArticleSegment from '@/components/upload/ArticleSegment.vue'
import AppSnackbar from '@/components/common/AppSnackbar.vue'
import { useMessage } from '@/composables/useMessage'
import { uploadCover } from '@/utils/upload'

export default {
  components: {
    ArticleSegment,
    AppSnackbar
  },
  setup() {
    const { toast, showSuccessMessage, showErrorMessage } = useMessage()
    return { toast, showSuccessMessage, showErrorMessage }
  },
  data() {
    return {
      rules: [(value) => !value || value.size < 2000000 || '封面图大小不能超过2MB'],
      article: {
        title: '',
        describe: '',
        imgUrl: '',
        category: -1,
        tag: [],
        imageId: '',
        textList: []
      },
      articleSegments: [
        
      ],
      markdownContent: '',
      articleType: 0, // 默认为普通文章
      articlePassword: '', // 加密文章密码
      articleTypeOptions: [
        { title: '普通文章', value: 0 },
        { title: '回复可见', value: 1 },
        { title: '加密文章', value: 2 }
      ],
      categoryMap: {
        Set: function (key, value) {
          this[key] = value
        },
        Get: function (key) {
          return this[key]
        },
        Contains: function (key) {
          return this.Get(key) != null
        },
        Remove: function (key) {
          delete this[key]
        },
      },
      category: [],
      categoryChildren: [],
      selectedMainCategory: null,
      selectedSubCategory: null,
      nowCategory: {},
      files: [],
      isSubmitting: false,
      editCode: -1
    }
  },
  computed: {
    isFormValid() {
      return (
        this.article.title &&
        this.article.tag.length > 0 &&
        this.article.category !== -1 &&
        this.articleSegments.every(segment => segment.content && segment.content.trim() !== '') &&
        !this.articleSegments.some(segment => segment.type === 2 && !segment.password)
      )
    },
  },
  created() {
    this.getCategory()
    // 判断编辑状态
    this.editCode = parseInt(this.$route.query.edit) || -1

    if (this.editCode && this.editCode !== -1) {
      this.editArticle()
    } else {
      this.articleSegments.push(
        {
          content: '',
          type: 0, // 默认为普通文章
          password: '', // 加密文章密码
          sort: 0 // 排序值从0开始
        }
      )
    }
  },
  methods: {
    // 添加新段落
    addNewSegment() {
      if (this.articleSegments.length >= 3) {
        this.showErrorMessage('最多只能添加3个段落')
        return
      }
      
      // 找出当前最大的sort值
      const maxSort = Math.max(...this.articleSegments.map(segment => segment.sort))
      
      this.articleSegments.push({
        content: '',
        type: 0,
        password: '',
        sort: maxSort + 1 // 使用最大sort值+1，确保递增
      })
    },
    
    // 更新段落内容
    updateSegmentContent(index, content) {
      this.articleSegments[index].content = content
    },
    
    // 更新段落类型
    updateSegmentType(index, type) {
      this.articleSegments[index].type = type
      // 如果不是加密文章，清空密码
      if (type !== 2) {
        this.articleSegments[index].password = ''
      }
    },
    
    // 删除段落
    removeSegment(index) {
      this.articleSegments.splice(index, 1)
      // 更新排序值
      this.articleSegments.forEach((segment, idx) => {
        segment.sort = idx
      })
    },
    
    // 修改编辑文章方法
    editArticle() {
      this.httpGet(`/article/text/${this.editCode}`, (json) => {
        if (json.data != null) {
          // 回显数据
          const editData = json.data

          // 基础信息回显
          this.article.id = editData.id
          this.article.title = editData.title
          this.article.describe = editData.describes
          this.article.imgUrl = editData.imgUrl
          this.article.imageId = editData.imageId
          this.article.tag = [...editData.tag]

          // 处理分类数据
          if (editData.fatherCategory) {
            // 设置主分区
            this.selectedMainCategory = editData.fatherCategory.name
            this.nowCategory = editData.fatherCategory

            // 加载子分区列表
            this.getMainCategory(editData.fatherCategory.name)

            // 设置子分区（延迟执行确保子分区列表已加载）
            if (editData.childrenCategory) {
              this.selectedSubCategory = editData.childrenCategory.name
              this.article.category = editData.childrenCategory.id
            } else {
              this.article.category = editData.fatherCategory.id
            }
          }

          // 加载文章内容
          if (editData.text && editData.text.length > 0) {
            // 清空默认段落
            this.articleSegments = []
            
            // 添加所有段落
            editData.text.forEach((item, index) => {
              this.articleSegments.push({
                content: item.content,
                type: item.type !== undefined ? item.type : 0,
                password: item.password || '',
                sort: item.sort !== undefined ? item.sort : index
              })
            })
            
            // 设置Markdown编辑器内容
            // this.$nextTick(() => {
            //   this.articleSegments.forEach((segment, index) => {
            //     this.$refs[`segment${index}`][0].setTextValue(segment.content)
            //   })
            // })
          }

          this.showSuccessMessage('文章数据加载成功，可以进行编辑')
        } else {
          this.showErrorMessage('无法加载文章数据')
          this.$router.push('/studio/upload')
        }
      })
    },
    
    // 修改发布文章方法
    publishArticle() {
      if (
        this.article.title === '' ||
        this.article.tag.length === 0 ||
        this.article.category === -1
      ) {
        this.showErrorMessage('标题、标签和分区不能为空')
        return
      }
      
      // 检查是否有内容为空的段落
      const emptySegments = this.articleSegments.filter(segment => !segment.content.trim())
      if (emptySegments.length > 0) {
        this.showErrorMessage('文章段落内容不能为空')
        return
      }
      
      // 检查加密文章是否设置了密码
      const encryptedSegmentsWithoutPassword = this.articleSegments.filter(
        segment => segment.type === 2 && !segment.password
      )
      if (encryptedSegmentsWithoutPassword.length > 0) {
        this.showErrorMessage('加密段落必须设置访问密码')
        return
      }
      
      if (this.article.tag.length > 6) {
        this.showErrorMessage('标签不能超过6个')
        return
      }

      this.isSubmitting = true

      // 准备文章内容
      this.article.textList = this.articleSegments
      
      // 根据是否为编辑模式选择不同的API端点
      const apiEndpoint =
        this.editCode && this.editCode !== -1 ? `/article/text/update` : '/article/text'

      this.httpPost(apiEndpoint, this.article, (json) => {
        this.isSubmitting = false

        if (json.status === 200) {
          this.showSuccessMessage(
            this.editCode !== -1 ? '更新成功' : '投稿成功，等待审核通过后你就可以看到你的文章了'
          )
          this.$router.push('/studio')
        } else {
          this.showErrorMessage(json.message || '提交失败')
        }
      })
    },

    // showSuccessMessage / showErrorMessage 由 useMessage 提供（见 setup），此处不再重复实现

    setFile(value) {
      this.files = []
      if (value) this.files.push(value)
    },

    async uploadFile() {
      try {
        const { imageId, imgUrl } = await uploadCover(this.files, {
          emptyMessage: '请先选择文章封面，然后上传！',
        })
        this.article.imageId = imageId
        this.article.imgUrl = imgUrl
        this.showSuccessMessage('封面上传成功')
      } catch (error) {
        this.showErrorMessage(error?.message || '封面上传失败')
      }
    },

    getCategory() {
      this.httpGet('/category/list', (json) => {
        for (let i = 0; i < json.data.length; i++) {
          const name = json.data[i].name
          this.category.push(name)
          this.categoryMap.Set(name, json.data[i])
        }
      })
    },

    getMainCategory(value) {
      this.categoryChildren = []
      this.selectedSubCategory = null
      this.nowCategory = this.categoryMap.Get(value)
      this.article.category = this.nowCategory.id

      const c = this.nowCategory.children
      if (c) {
        for (let i = 0; i < c.length; i++) {
          this.categoryChildren.push(c[i].name)
        }
      }
    },

    getArticleCategory(value) {
      const c = this.nowCategory.children
      for (let i = 0; i < c.length; i++) {
        if (c[i].name === value) {
          this.article.category = c[i].id
        }
      }
    },
    handleArticleTypeChange(value) {
      this.articleType = value
      // 如果不是加密文章，清空密码
      if (value !== 2) {
        this.articlePassword = ''
      }
    },
  },
}
</script>

<style>
.thumbnail-preview {
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}
</style>