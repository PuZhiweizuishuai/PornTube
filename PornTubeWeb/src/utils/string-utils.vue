<script>
function clarityDisplay(pixelsNumber) {
  if (pixelsNumber == null || pixelsNumber === '') {
    return
  }
  if (pixelsNumber >= 8294400) {
    return '4K'
  }
  if (pixelsNumber >= 2073600) {
    return 'HD'
  }
  return 'SD'
}

export default {
  clarityDisplay,
}
</script>
