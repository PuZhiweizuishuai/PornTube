<!--
  稿件收藏时的收藏夹选择弹框
  VideoView / TextView 首次收藏时弹出：选择已有收藏夹、新建收藏夹，或什么都不选（后端归入默认收藏夹）
-->
<template>
  <v-dialog
    :model-value="modelValue"
    max-width="480"
    @update:model-value="(v) => $emit('update:modelValue', v)"
  >
    <v-card v-if="modelValue">
      <v-card-title class="text-h6">收藏到</v-card-title>
      <v-card-text>
        <v-progress-linear v-if="loading" indeterminate color="primary" rounded />
        <template v-else>
          <v-radio-group v-if="labels.length" v-model="selectedLabelId" hide-details class="mt-0">
            <v-radio
              v-for="label in labels"
              :key="label.id"
              :value="label.id"
              :label="labelText(label)"
            />
          </v-radio-group>
          <div v-else class="text-body-2 text-medium-emphasis mb-2">
            你还没有收藏夹，将自动收藏到「默认收藏夹」
          </div>
        </template>

        <v-divider class="my-3"></v-divider>

        <div class="d-flex align-center ga-2">
          <v-text-field
            v-model="newName"
            density="compact"
            hide-details
            placeholder="新建收藏夹"
            maxlength="30"
            class="flex-grow-1"
            @keyup.enter="createLabel"
          />
          <v-checkbox
            v-model="newPublic"
            density="compact"
            hide-details
            label="公开"
            class="flex-shrink-0 mt-0"
          />
          <v-btn
            variant="tonal"
            color="primary"
            :disabled="!newName.trim() || saving"
            :loading="saving"
            @click="createLabel"
          >
            新建
          </v-btn>
        </div>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn color="grey" variant="text" :disabled="submitting" @click="$emit('update:modelValue', false)">
          取消
        </v-btn>
        <v-btn color="primary" variant="elevated" :loading="submitting" @click="confirm">收藏</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  name: 'FavoriteDialog',
  props: {
    modelValue: {
      type: Boolean,
      default: false,
    },
    articleId: {
      type: [Number, String],
      required: true,
    },
  },
  emits: ['update:modelValue', 'favorited', 'error'],
  data() {
    return {
      loading: false,
      labels: [],
      selectedLabelId: null,
      newName: '',
      newPublic: false,
      saving: false,
      submitting: false,
    }
  },
  watch: {
    modelValue(value) {
      if (value) {
        this.loadLabels()
      }
    },
  },
  methods: {
    labelText(label) {
      let text = label.name
      if (label.isDefault === 1) {
        text += '（默认）'
      }
      text += ` · ${label.count} 个`
      if (label.isPublic === 1) {
        text += ' · 公开'
      }
      return text
    },
    loadLabels() {
      this.loading = true
      const done = () => {
        this.loading = false
      }
      this.httpGet(
        '/favorites/label/list',
        (json) => {
          done()
          if (json.status === 200) {
            this.labels = json.data || []
            const defaultLabel = this.labels.find((label) => label.isDefault === 1)
            this.selectedLabelId = defaultLabel ? defaultLabel.id : (this.labels[0] ? this.labels[0].id : null)
          } else {
            this.$emit('error', json.message || '获取收藏夹失败')
          }
        },
        () => {
          done()
          this.$emit('error', '获取收藏夹失败')
        }
      )
    },
    createLabel() {
      const name = this.newName.trim()
      if (!name || this.saving) {
        return
      }
      this.saving = true
      this.httpPost(
        '/favorites/label/save',
        { name, isPublic: this.newPublic ? 1 : 0 },
        (json) => {
          this.saving = false
          if (json.status === 200) {
            this.labels.push({ ...json.data, count: 0 })
            this.selectedLabelId = json.data.id
            this.newName = ''
            this.newPublic = false
          } else {
            this.$emit('error', json.message || '新建收藏夹失败')
          }
        },
        () => {
          this.saving = false
          this.$emit('error', '新建收藏夹失败')
        }
      )
    },
    confirm() {
      if (this.submitting) {
        return
      }
      this.submitting = true
      const body = { articeId: this.articleId }
      if (this.selectedLabelId != null) {
        body.favoritesLabelId = this.selectedLabelId
      }
      this.httpPost(
        '/favorites/toggle',
        body,
        (json) => {
          this.submitting = false
          if (json.status === 200 && json.data && json.data.id != null) {
            this.$emit('favorited')
            this.$emit('update:modelValue', false)
          } else if (json.status === 200) {
            // 后端 toggle 语义：已收藏时返回对象没有 id，视为已在收藏夹中
            this.$emit('update:modelValue', false)
          } else {
            this.$emit('error', json.message || '收藏失败')
          }
        },
        () => {
          this.submitting = false
          this.$emit('error', '收藏失败')
        }
      )
    },
  },
}
</script>
