<script>
function checkPower(userInfo) {
  if (userInfo.userRoleEntity.role === 'ROLE_ADMIN') {
    return 'admin'
  }
  if (userInfo.userRoleEntity.role === 'ROLE_VIP') {
    return 'vip'
  }
  if (userInfo.userRoleEntity.role === 'ROLE_USER') {
    return 'user'
  }
}

export default {
  checkPower
}
</script>
