<template>
  <v-container>
    <!-- 显示标题 -->
    <v-row align="center" class="mb-4">
      <v-col>
        <h2 class="text-h5 font-weight-bold">
          <v-icon icon="mdi-fire" color="red" class="mr-2"></v-icon>
          时下流行
        </h2>
        <p class="text-body-2 text-grey">发现最热门的视频内容</p>
      </v-col>
    </v-row>

    <!-- 热点视频 -->
    <v-row v-if="hotList.length > 0">
      <v-col v-for="(item, index) in hotList" :key="item.id">
        <div class="d-flex align-start position-relative">
          <!-- 排名标签 -->
          <div
            class="rank-badge d-flex justify-center align-center"
            :class="{ 'rank-top3': index < 3 }"
          >
            <span>{{ index + 1 }}</span>
            <v-icon v-if="index === 0" size="x-small" color="white" class="crown-icon"
              >mdi-crown</v-icon
            >
          </div>
          <!-- 视频卡片 -->
          <VideoCard :video="item" />
        </div>
      </v-col>
    </v-row>

    <!-- 无内容显示 -->
    <v-row v-else class="mt-8">
      <v-col cols="12" class="text-center">
        <v-icon size="x-large" color="grey" class="mb-4">mdi-fire-off</v-icon>
        <p class="text-body-1">暂无热门视频内容</p>
        <v-btn color="primary" prepend-icon="mdi-refresh" @click="getHotList" class="mt-4">
          刷新
        </v-btn>
      </v-col>
    </v-row>

    <!-- 加载动画 -->
    <v-row v-if="loading" class="mt-4">
      <v-col cols="12" class="text-center">
        <v-progress-circular indeterminate color="red"></v-progress-circular>
        <p class="mt-2 text-body-2 text-grey">加载中...</p>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import VideoCard from '@/components/card/VideoCard.vue'

export default {
  components: {
    VideoCard,
  },
  data() {
    return {
      hotList: [],
      loading: false,
    }
  },
  created() {
    this.getHotList()
  },
  methods: {
    getHotList() {
      this.loading = true
      this.httpGet(
        '/article/hot',
        (json) => {
          this.hotList = json.data
          this.loading = false
        },
        () => {
          this.loading = false
        }
      )
    },
  },
}
</script>

<style scoped>
.v-col {
  transition: transform 0.2s;
}
.v-col:hover {
  transform: translateY(-5px);
}

.rank-badge {
  position: absolute;
  left: -5px;
  top: 10px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background-color: rgba(100, 100, 100, 0.8);
  color: white;
  font-weight: bold;
  z-index: 2;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.rank-top3 {
  background: linear-gradient(135deg, #f2c94c 0%, #f2994a 100%);
  width: 35px;
  height: 35px;
  animation: pulse 2s infinite;
}

.crown-icon {
  position: absolute;
  top: -10px;
  font-size: 12px;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(242, 201, 76, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(242, 201, 76, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(242, 201, 76, 0);
  }
}
</style>