/**
 * Artplayer 配置工厂
 *
 * 背景：PlayerVideo.vue(403 行) 把「有广告」(62-221) 与「无广告」(222-339) 两段配置
 * 各写了一遍，~110 行逐行重复，且 beforeEmit 存在明显 bug：
 *   - fetch 没有 await，函数末尾恒 return true → 被后端拒绝（未登录 / 被限流）的弹幕
 *     仍然会在本地渲染出来，刷新后消失，用户以为发送成功
 * 现在统一由本工厂产出配置：
 *   new Artplayer(buildArtplayerOptions({ container, url, poster, videoId, ads }))
 */
import artplayerPluginDanmuku from 'artplayer-plugin-danmuku'
import artplayerPluginAds from 'artplayer-plugin-ads'
import http from '@/api/index'

/** 统一的播放器行为开关（原来两个分支里各写一份，易于漏改） */
const BASE_OPTIONS = {
  flip: true,
  playbackRate: true,
  aspectRatio: true,
  screenshot: true,
  setting: true,
  hotkey: true,
  pip: true,
  fullscreen: true,
  fullscreenWeb: true,
  fastForward: true,
  autoOrientation: true,
}

const DEFAULT_CONTEXTMENU = [
  {
    html: '不挂高数出品',
    click: function (...args) {
      window.open('https://www.buguagaoshu.com/')
    },
  },
]

/**
 * 拉取弹幕列表，转换成 Artplayer 弹幕插件需要的结构
 * @param {number|string} videoId
 */
export async function fetchDanmakuList(videoId) {
  try {
    const json = await http.get('/danmaku/v1', { id: videoId })
    if (!json || json.status !== 200 || !Array.isArray(json.data)) return []
    return json.data.map((d) => ({
      text: d.text,
      time: d.time,
      mode: d.type,
      color: d.color,
    }))
  } catch (e) {
    // 弹幕加载失败不应影响视频播放，返回空列表即可
    console.warn('[danmaku] 加载弹幕失败:', e?.message || e)
    return []
  }
}

/**
 * 发送弹幕。返回 true 表示允许本地渲染。
 *
 * 后端语义（DanmakuController#saveArt）：
 *   code = 0   发送成功，立即可见
 *   code = -1  发送成功但需审核通过后可见
 *   code = 1   未登录 / 触发限流，应当拒绝
 * @param {object} danmu artplayer 弹幕对象
 * @param {number|string} videoId
 * @param {object} [hooks] { onLoginRequired, onLimited, onError }
 */
export async function emitDanmaku(danmu, videoId, hooks = {}) {
  const payload = {
    text: danmu.text,
    time: danmu.time,
    type: danmu.mode,
    color: danmu.color,
    id: videoId,
  }
  try {
    const json = await http.post('/danmaku/v1', payload)
    const code = json?.code
    if (code === 0) return true
    if (code === -1) {
      // 已进入审核队列，本地依旧显示，但需提示用户
      if (typeof hooks.onPending === 'function') {
        hooks.onPending('弹幕发送成功，待管理员审核通过后其他观众即可看见你的弹幕！')
      }
      return true
    }
    if (code === 1) {
      if (typeof hooks.onLoginRequired === 'function') {
        hooks.onLoginRequired(json?.message || '请先登录后再发送弹幕，或稍后再试')
      }
      return false
    }
    if (typeof hooks.onError === 'function') {
      hooks.onError(json?.message || '弹幕发送失败')
    }
    return false
  } catch (e) {
    if (typeof hooks.onError === 'function') {
      hooks.onError(e?.message || '弹幕发送失败，请检查网络后重试')
    }
    return false
  }
}

/**
 * 广告图片 URL 白名单校验：只放行 http(s) 与站内相对路径，
 * 避免把后台配置字段直接拼进 HTML 字符串造成 DOM XSS（对应文档 F5）
 */
export function sanitizeImageUrl(url) {
  if (!url || typeof url !== 'string') return ''
  const trimmed = url.trim()
  if (/^(https?:)?\/\//i.test(trimmed) || trimmed.startsWith('/')) {
    // 转义会被插入 HTML 属性中的关键字符
    return trimmed.replace(/["'<>\\]/g, '')
  }
  return ''
}

/**
 * 构造 Artplayer 配置
 *
 * @param {object} options
 * @param {HTMLElement} options.container 播放器挂载点
 * @param {string} options.url 视频地址（已含 key）
 * @param {string} options.poster 封面
 * @param {number|string} options.videoId 分 P 的 video id
 * @param {object|null} [options.ads] 传入则启用贴片广告插件，需含 {id,imageUrl,videoUrl,url}
 * @param {object} [options.adsOptions] 覆盖广告默认时长配置
 * @param {Function} [options.isLogin] 是否已登录的判断函数
 * @param {object} [options.danmakuHooks] 弹幕发送回调 {onLoginRequired,onPending,onError}
 * @returns {object} Artplayer 构造参数
 */
export function buildArtplayerOptions(options = {}) {
  const { container, url, poster, videoId, ads = null, adsOptions = {}, danmakuHooks = {}, isLogin } = options

  const plugins = [
    artplayerPluginDanmuku({
      // 弹幕列表：走统一的 HTTP 层，不再裸写 fetch（对应文档第三节第 8 条）
      danmuku: function () {
        return fetchDanmakuList(videoId)
      },
      /**
       * 发送前的服务端校验。
       * 修复要点：必须 await，且由服务端结果决定是否 return true。
       */
      async beforeEmit(danmu) {
        if (typeof isLogin === 'function' && !isLogin()) {
          if (typeof danmakuHooks.onLoginRequired === 'function') {
            danmakuHooks.onLoginRequired('请先登录')
          }
          return false
        }
        return emitDanmaku(danmu, videoId, danmakuHooks)
      },
    }),
  ]

  if (ads) {
    plugins.push(
      artplayerPluginAds({
        // 只接受经过白名单校验的图片地址，避免拼接 HTML 带来的 XSS
        html: sanitizeImageUrl(ads.imageUrl) ? `<img src="${sanitizeImageUrl(ads.imageUrl)}">` : '',
        video: ads.videoUrl,
        url: ads.url,
        playDuration: adsOptions.playDuration ?? 5,
        totalDuration: adsOptions.totalDuration ?? 10,
        i18n: {
          close: '关闭广告',
          countdown: '%s秒',
          detail: '查看详情',
          canBeClosed: '%s秒后可关闭广告',
        },
      })
    )
  }

  return {
    ...BASE_OPTIONS,
    container,
    url,
    poster,
    contextmenu: DEFAULT_CONTEXTMENU,
    plugins,
  }
}

export default buildArtplayerOptions
