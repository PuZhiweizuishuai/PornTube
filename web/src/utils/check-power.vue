<script>

function checkPower(userInfo) {
  if (userInfo.userRoleEntity.role === 'ROLE_ADMIN') {
    return 'admin'
  }
  if (userInfo.userRoleEntity.role === 'ROLE_VIP') {
    if (userInfo.userRoleEntity.vipStopTime < new Date().getTime()) {
      return 'vip_stop'
    }
    return 'vip'
  }
  if (userInfo.userRoleEntity.role === 'ROLE_USER') {
    return 'user'
  }
}

function updateUserRole(userInfo) {
  if (userInfo.userRoleEntity.role === 'ROLE_VIP' && userInfo.userRoleEntity.vipStopTime < new Date().getTime()) {
    return true
  }
  return false
}

export default {
  checkPower,
  updateUserRole
}
</script>
