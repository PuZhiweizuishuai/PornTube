<template>
  <div class="upload-form">
    <!-- 基本信息 -->
    <UploadSectionCard
      class="mb-4"
      icon="mdi-information-outline"
      title="基本信息"
      hint="分区决定稿件出现在哪个频道，标题和简介会展示在文章卡片上"
      :done="basicDone"
    >
      <div class="d-flex ga-4 flex-wrap">
        <v-select
          v-model="selectedMainCategory"
          class="flex-grow-1"
          style="min-width: 220px"
          :items="category"
          label="主分区"
          placeholder="请选择主分区"
          prepend-inner-icon="mdi-shape-outline"
          @update:model-value="getMainCategory"
        ></v-select>
        <v-select
          v-model="selectedSubCategory"
          class="flex-grow-1"
          style="min-width: 220px"
          :items="categoryChildren"
          label="子分区"
          placeholder="请选择子分区"
          prepend-inner-icon="mdi-tag-outline"
          :disabled="!categoryChildren.length"
          @update:model-value="getArticleCategory"
        ></v-select>
      </div>

      <v-text-field
        v-model="article.title"
        class="mt-4"
        placeholder="为你的文章添加一个吸引人的标题"
        label="标题"
        counter="100"
        :rules="[(v) => !!v || '标题不能为空', (v) => (v && v.length <= 100) || '标题不能超过100个字符']"
        prepend-inner-icon="mdi-format-title"
      ></v-text-field>

      <v-textarea
        v-model="article.describe"
        label="简介"
        auto-grow
        rows="3"
        counter="1000"
        placeholder="填写更全面的文章信息，让更多人找到你的文章"
        prepend-inner-icon="mdi-text-box-outline"
        :rules="[(v) => !v || v.length <= 1000 || '简介不能超过1000个字符']"
      ></v-textarea>
    </UploadSectionCard>

    <!-- 封面图 -->
    <UploadSectionCard
      class="mb-4"
      icon="mdi-image-outline"
      title="封面图"
      hint="可不填，也能随时换图或移除，保存后生效；有封面时文章卡片显示封面，没有则显示默认渐变底图"
      :done="!!article.imgUrl"
    >
      <CoverUploadPanel
        v-model:img-url="article.imgUrl"
        v-model:image-id="article.imageId"
        endpoint="/upload/article/cover"
        empty-message="请先选择文章封面，然后上传！"
        clearable
        @notify="showMessage"
      />
    </UploadSectionCard>

    <!-- 正文内容 -->
    <UploadSectionCard
      class="mb-4"
      icon="mdi-file-document-edit-outline"
      title="正文内容"
      hint="支持 Markdown 与图片，最多 3 个段落；加密段落必须设置访问密码"
      :done="contentDone"
    >
      <template #actions>
        <v-btn
          v-if="articleSegments.length < 3"
          size="small"
          variant="tonal"
          color="primary"
          prepend-icon="mdi-plus"
          @click="addNewSegment"
        >
          添加段落
        </v-btn>
      </template>

      <article-segment
        v-for="(segment, index) in articleSegments"
        :key="index"
        :ref="`segment${index}`"
        :segment="segment"
        :index="index"
        :segments="articleSegments"
        :article-type-options="articleTypeOptions"
        @update-content="updateSegmentContent"
        @update-type="updateSegmentType"
        @remove-segment="removeSegment"
      ></article-segment>
      <div v-if="!articleSegments.length" class="text-body-2 text-medium-emphasis">
        还没有段落，点击右上角「添加段落」开始写作。
      </div>
    </UploadSectionCard>

    <!-- 标签 -->
    <UploadSectionCard
      class="mb-4"
      icon="mdi-tag-multiple-outline"
      title="标签"
      hint="最多 6 个，标签越准确越容易被搜到"
      :done="tagDone"
    >
      <v-combobox
        v-model="article.tag"
        label="标签"
        placeholder="添加标签让更多人找到你的文章（最多6个）"
        multiple
        chips
        closable-chips
        prepend-inner-icon="mdi-tag-multiple-outline"
        :rules="[
          (v) => (v && v.length > 0) || '至少添加一个标签',
          (v) => !v || v.length <= 6 || '最多只能添加6个标签',
        ]"
      ></v-combobox>
    </UploadSectionCard>

    <UploadSubmitBar
      :missing="missingLabels"
      :ready="isFormValid"
      :submitting="isSubmitting"
      :submit-text="editCode !== -1 ? '保存修改' : '立即投稿'"
      @submit="publishArticle"
    />

    <!-- 消息提示：统一由 useMessage + AppSnackbar 承载 -->
    <AppSnackbar :state="toast" />
  </div>
</template>

<script>
import ArticleSegment from '@/components/upload/ArticleSegment.vue'
import AppSnackbar from '@/components/common/AppSnackbar.vue'
import UploadSectionCard from '@/components/upload/UploadSectionCard.vue'
import CoverUploadPanel from '@/components/upload/CoverUploadPanel.vue'
import UploadSubmitBar from '@/components/upload/UploadSubmitBar.vue'
import { useMessage } from '@/composables/useMessage'

export default {
  components: {
    ArticleSegment,
    AppSnackbar,
    UploadSectionCard,
    CoverUploadPanel,
    UploadSubmitBar,
  },
  emits: ['checklist'],
  setup() {
    const { toast, showMessage, showSuccessMessage, showErrorMessage } = useMessage()
    return { toast, showMessage, showSuccessMessage, showErrorMessage }
  },
  data() {
    return {
      article: {
        title: '',
        describe: '',
        imgUrl: '',
        category: -1,
        tag: [],
        imageId: '',
        textList: []
      },
      articleSegments: [],
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
      isSubmitting: false,
      editCode: -1
    }
  },
  computed: {
    basicDone() {
      return this.article.category !== -1 && !!(this.article.title || '').trim()
    },
    tagDone() {
      return this.article.tag.length > 0 && this.article.tag.length <= 6
    },
    contentDone() {
      return (
        this.articleSegments.length > 0 &&
        this.articleSegments.every(
          (segment) =>
            (segment.content || '').trim() !== '' && (segment.type !== 2 || segment.password)
        )
      )
    },
    isFormValid() {
      return this.basicDone && this.tagDone && this.contentDone
    },
    /** 右侧「发布检查清单」的数据源，封面是可选项 */
    checklist() {
      return [
        { key: 'category', label: '选择分区', done: this.article.category !== -1 },
        { key: 'title', label: '填写标题', done: !!(this.article.title || '').trim() },
        { key: 'cover', label: '上传封面', done: !!this.article.imgUrl, optional: true },
        { key: 'content', label: '撰写正文', done: this.contentDone },
        { key: 'tag', label: '添加标签', done: this.tagDone },
      ]
    },
    missingLabels() {
      return this.checklist.filter((item) => !item.done && !item.optional).map((item) => item.label)
    },
  },
  watch: {
    checklist: {
      immediate: true,
      deep: true,
      handler(value) {
        this.$emit('checklist', value)
      },
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
      if (!this.article.title || this.article.tag.length === 0 || this.article.category === -1) {
        this.showErrorMessage('标题、标签和分区不能为空')
        return
      }
      
      // 检查是否有内容为空的段落
      const emptySegments = this.articleSegments.filter((segment) => !(segment.content || '').trim())
      if (emptySegments.length > 0) {
        this.showErrorMessage('文章段落内容不能为空')
        return
      }
      
      // 检查加密文章是否设置了密码
      const encryptedSegmentsWithoutPassword = this.articleSegments.filter(
        (segment) => segment.type === 2 && !segment.password
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
  },
}
</script>
