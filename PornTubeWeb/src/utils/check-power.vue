<script>
function checkPower(userInfo) {
  if (userInfo.userRoleEntity.role === 'ROLE_ADMIN') {
    return 'admin'
  }
  if (userInfo.userRoleEntity.role === 'ROLE_VIP') {
    if (userInfo.userRoleEntity.vipStopTime < new Date().getTime()) {
      return 'user'
    }
    return 'vip'
  }
  if (userInfo.userRoleEntity.role === 'ROLE_USER') {
    return 'user'
  }
}

function checkVip(userInfo) {
  if (userInfo.userRoleEntity.role === 'ROLE_VIP') {
    if (userInfo.userRoleEntity.vipStopTime < new Date().getTime()) {
      return false
    }
    return true
  }
  if (userInfo.userRoleEntity.role === 'ROLE_ADMIN') {
    return true
  }
  return false
}

function updateUserRole(userInfo) {
  if (userInfo === null) {
    return false
  }
  if (
    userInfo.userRoleEntity.role === 'ROLE_VIP' &&
    userInfo.userRoleEntity.vipStopTime < new Date().getTime()
  ) {
    return true
  }
  return false
}

export default {
  checkPower,
  updateUserRole,
  checkVip,
}
</script>
