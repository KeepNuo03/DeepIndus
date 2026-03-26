<script setup>
// Vue Router 会自动处理路由，不需要手动引入组件
</script>

<template>
  <router-view />
</template>

<style>
/* 全局样式重置，确保页面全屏显示 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body,
#app {
  width: 100%;
  height: 100%;
}
</style>
