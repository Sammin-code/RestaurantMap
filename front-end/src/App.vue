<template>
  <el-config-provider :locale="locale">
    <div class="app">
      <NavBar />
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </el-config-provider>
</template>

<script setup>
import { ElConfigProvider } from 'element-plus';
import zhTw from 'element-plus/dist/locale/zh-tw.mjs';
import NavBar from './components/NavBar.vue';
import { onMounted } from 'vue';
import { useUserStore } from './stores/user';

const userStore = useUserStore();
const locale = zhTw;

onMounted(async () => {
  await userStore.initialize();
});
</script>

<style>
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}
</style> 