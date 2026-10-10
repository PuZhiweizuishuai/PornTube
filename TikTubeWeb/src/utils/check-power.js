/**
 * 角色 / 权限判断
 * 由 utils/check-power.vue 迁移而来。
 * 注意：这里只用于前端 UI 的显示隐藏，真正的鉴权必须由后端接口独立完成（见文档 F1）。
 */
export function checkPower(userInfo) {
  const userRole = userInfo?.userRoleEntity
  if (!userRole) {
    return undefined
  }
  if (userRole.role === 'ROLE_ADMIN') {
    return 'admin'
  }
  if (userRole.role === 'ROLE_VIP') {
    if (userRole.vipStopTime < new Date().getTime()) {
      return 'user'
    }
    return 'vip'
  }
  if (userRole.role === 'ROLE_USER') {
    return 'user'
  }
  return undefined
}

export function checkVip(userInfo) {
  const userRole = userInfo?.userRoleEntity
  if (!userRole) {
    return false
  }
  if (userRole.role === 'ROLE_VIP') {
    return userRole.vipStopTime >= new Date().getTime()
  }
  if (userRole.role === 'ROLE_ADMIN') {
    return true
  }
  return false
}

/** VIP 是否刚刚过期（用于在 UI 上提示用户刷新权限） */
export function updateUserRole(userInfo) {
  if (userInfo === null || userInfo?.userRoleEntity == null) {
    return false
  }
  return (
    userInfo.userRoleEntity.role === 'ROLE_VIP' &&
    userInfo.userRoleEntity.vipStopTime < new Date().getTime()
  )
}

export default {
  checkPower,
  updateUserRole,
  checkVip,
}
