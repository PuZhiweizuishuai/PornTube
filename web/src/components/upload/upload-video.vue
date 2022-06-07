<template>
  <v-row justify="center" align="center">
    <v-col>
      <v-card
        class="mx-auto"
        outlined
      >
        <v-row justify="center" style="padding-top: 12px;">
          <v-col cols="10">
            <h2>上传视频</h2>
          </v-col>
        </v-row>
        <v-row justify="center">
          <v-col cols="10">
            <!-- <v-file-input prepend-icon="mdi-video" show-size counter accept="video/*,.flv" chips label="请选择视频文件" @change="setFile" /> -->
            <FilePondUpdate ref="videoUploadTool" @video="videoUploadSuccess" />
          </v-col>
        </v-row>
        <!-- 显示系统自动生成的截图 -->
        <!-- <v-row justify="center">
          <v-col cols="10">
            <h2>显示系统生成的封面图</h2>
          </v-col>
        </v-row> -->
        <v-col />
        <v-row no-gutters justify="center">
          <v-col
            v-for="item in videPhotoList"
            :key="item.id"
          >
            <div style="width: 350px;padding-top: 12px;">
              <v-row>
                <v-col>
                  <v-img :src="item.fileUrl" aspect-ratio="1.77" contain max-height="150" alt="封面图，推荐16：9" />
                </v-col>
              </v-row>
              <v-col />
              <v-row justify="center">
                <v-btn text color="success" @click="setAutoFaceImage(item)">选择</v-btn>
              </v-row>
            </div>
          </v-col>
        </v-row>
        <v-col />
        <v-row justify="center">
          <v-col cols="10">
            <v-btn v-if="isShowUploadFaceButton" color="success" @click="isShowUploadFaceImage = true">没有想要的封面图，我要上传</v-btn>
          </v-col>
        </v-row>

        <v-row justify="center">
          <v-col cols="10">
            <h2>基本信息</h2>
          </v-col>
        </v-row>
        <v-row justify="center">
          <v-col cols="5">
            <v-card outlined>
              <v-img class="white--text align-end" :src="article.imgUrl" aspect-ratio="1.77" contain max-height="150" alt="封面图，推荐16：9">
                封面图
              </v-img>
            </v-card>
          </v-col>

          <v-col v-if="isShowUploadFaceImage" cols="5">
            <v-file-input
              :rules="rules"
              accept="image/png, image/jpeg, image/bmp"
              placeholder="上传视频封面"
              prepend-icon="mdi-camera"
              label="封面"
              @change="setFile"
            />
            <v-btn color="primary" @click="uploadFile">
              上传
            </v-btn>
          </v-col>
        </v-row>
        <v-row justify="center">
          <v-col cols="5">
            <v-select
              :items="category"
              label="主分区"
              @change="getMainCategory"
            />
          </v-col>
          <v-col cols="5">
            <v-select
              :items="categoryChildren"
              label="分区"
              @change="getVideoCategory"
            />
          </v-col>
        </v-row>
        <v-row justify="center">
          <v-col cols="10">
            <v-text-field
              v-model="article.title"
              placeholder="标题"
              label="标题(50字以内)"
              clearable
              :rules="[() => article.title != null || '标题不能为空']"
            />
          </v-col>
        </v-row>
        <v-row justify="center">
          <v-col cols="10">
            <v-textarea
              v-model="article.describe"
              label="简介(200字以内)"
              clearable
              placeholder="填写更全面的视频信息，让更多的人找到你！"
            />
          </v-col>
        </v-row>
        <v-row justify="center">
          <v-col cols="10">
            <v-combobox
              v-model="article.tag"
              label="添加标签让更多人找到你（最多6个）"
              multiple
              chips
              clearable
            />
          </v-col>
        </v-row>
        <v-row justify="center" style=" padding-bottom: 12px">
          <v-col cols="10">
            <v-btn large color="primary" @click="publis">立即投稿</v-btn>
          </v-col>
        </v-row>
      </v-card>

    </v-col>
    <v-snackbar
      v-model="showMessage"
      :top="true"
      :timeout="3000"
    >
      {{ message }}

      <template v-slot:action="{ attrs }">
        <v-btn
          color="pink"
          text
          v-bind="attrs"
          @click="showMessage = false"
        >
          关闭
        </v-btn>
      </template>
    </v-snackbar>
  </v-row>
</template>

<script>
import FilePondUpdate from '@/components/upload/filepond-upload.vue'
export default {
  components: {
    FilePondUpdate
  },
  data() {
    return {
      rules: [
        value => !value || value.size < 2000000 || 'Avatar size should be less than 2 MB!'
      ],
      article: {
        title: '',
        describe: '',
        imgUrl: '',
        category: -1,
        tag: [],
        video: {},
        imageId: ''
      },
      videPhotoList: [],
      categoryMap: {
        Set: function(key, value) { this[key] = value },
        Get: function(key) { return this[key] },
        Contains: function(key) { return this.Get(key) != null },
        Remove: function(key) { delete this[key] }
      },
      category: [],
      categoryChildren: [],
      nowCategory: {},
      files: [],
      showMessage: false,
      message: '',
      isShowUploadFaceImage: false,
      isShowUploadFaceButton: false
    }
  },
  created() {
    this.getCategory()
  },
  methods: {
    setAutoFaceImage(item) {
      this.article.imgUrl = item.fileUrl
      this.article.imageId = item.id
    },

    publis() {
      if (!this.article.video.id) {
        this.message = '你还没有上传视频'
        this.showMessage = true
        return
      }
      if (this.article.title === '' || this.imgUrl === '' || this.article.tag.length === 0 || this.article.category === -1) {
        this.message = '标题，封面，标签,分区不能为空'
        this.showMessage = true
        return
      }
      if (this.article.tag.length > 6) {
        this.message = '标签超过6个'
        this.showMessage = true
        return
      }
      fetch(`/api/article/video`, {
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
        },
        method: 'POST',
        credentials: 'include',
        body: JSON.stringify(this.article)
      }).then(response => response.json())
        .then(json => {
          if (json.status === 200) {
            this.message = '投稿成功,等待审核通过后你就可以看到你的视频了'
            this.showMessage = true
            this.$router.push('/studio')
          } else {
            this.message = json.message
            this.showMessage = true
            return
          }
        })
        .catch(e => {
          return null
        })

      console.log(this.article)
    },
    setFile(value) {
      this.files = []
      this.files.push(value)
    },
    videoUploadSuccess(value) {
      // console.log(value)
      if (value.status === 200) {
        // 设置封面图和视频
        for (let i = 0; i < value.data.length; i++) {
          if (value.data[i].type === 5) {
            this.videPhotoList.push(value.data[i])
            continue
          }
          if (value.data[i].type === 0) {
            this.article.video = value.data[i]
            this.setTitle(value.data[i].fileOriginalName)
          }
        }
        this.isShowUploadFaceButton = true
        this.message = '上传视频成功'
        this.showMessage = true
      } else {
        this.message = '上传出现异常，请重试！' + value.message + ' ' + value.data
        this.showMessage = true
        this.$refs.videoUploadTool.handleFilePondInit()
      }
    },
    setTitle(title) {
      if (title.length > 50) {
        this.article.title = title.substring(0, 50)
      } else {
        this.article.title = title
      }
    },
    uploadFile() {
      if (this.files.length === 0) {
        this.message = '请先选择视频封面，然后上传！'
        this.showMessage = true
        return
      }
      const formData = new FormData()
      for (let i = 0; i < this.files.length; i++) {
        formData.append('file[]', this.files[i])
      }
      fetch(`/api/upload/photo`, {
        headers: {
          'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
        },
        method: 'POST',
        credentials: 'include',
        body: formData
      }).then(response => response.json())
        .then(json => {
          if (json.status === 200) {
            //
            this.article.imageId = json.data[0].id
            this.article.imgUrl = json.data[0].fileUrl
          } else {
            this.message = '上传失败，请重试！' + json.message
            this.showMessage = true
          }
        })
        .catch(e => {
          return null
        })
    },
    getCategory() {
      fetch(`/api/category/list`, {
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
        },
        method: 'GET',
        credentials: 'include'
      }).then(response => response.json())
        .then(json => {
          for (let i = 0; i < json.data.length; i++) {
            const name = json.data[i].name

            this.category.push(name)
            this.categoryMap.Set(name, json.data[i])
          }
        })
        .catch(e => {
          return null
        })
    },
    getMainCategory(value) {
      this.categoryChildren = []
      this.nowCategory = this.categoryMap.Get(value)
      this.article.category = this.nowCategory.id

      const c = this.nowCategory.children
      if (c) {
        for (let i = 0; i < c.length; i++) {
          this.categoryChildren.push(c[i].name)
        }
      }
    },
    getVideoCategory(value) {
      const c = this.nowCategory.children
      for (let i = 0; i < c.length; i++) {
        if (c[i].name === value) {
          this.article.category = c[i].id
        }
      }
    }
  }
}
</script>

<style>

</style>
