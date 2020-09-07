<template>
  <div>
    <v-row>
      <v-col>
        {{ commentCount }} 条评论
      </v-col>
    </v-row>
    <v-row>
      <v-col cols="2" style="padding-top: 0px;">
        <router-link v-if="this.$store.state.userInfo" :to="`/user/${this.$store.state.userInfo.id}`">
          <v-avatar size="62">
            <v-img
              :src="this.$store.state.userInfo.avatarUrl"
              :alt="this.$store.state.userInfo.username"
              :title="this.$store.state.userInfo.username"
            />
          </v-avatar>
        </router-link>
        <v-avatar v-if="this.$store.state.userInfo == null" size="62">
          <v-img
            src="/images/head.png"
            alt="头像"
            title="请登录后评论"
          />
        </v-avatar>
      </v-col>
      <v-col style="padding-top: 0px;">
        <v-textarea
          placeholder="发表公开评论"
          label="评论"
          rows="2"
          :no-resize="true"
        />
      </v-col>
    </v-row>
    <v-row>
      <v-col>
        <v-btn color="primary" style=" float:right ">评论</v-btn>
      </v-col>
    </v-row>
    <v-divider />
    <v-row>
      <v-col>
        评论列表，待完成
      </v-col>
    </v-row>
  </div>
</template>

<script>
export default {
  name: 'VideoComment',
  props: {
    count: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      commentCount: this.count
    }
  }
}
</script>

<style>

</style>
