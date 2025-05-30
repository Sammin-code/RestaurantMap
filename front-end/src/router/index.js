import { createRouter, createWebHistory } from 'vue-router';
import { useUserStore } from '@/stores/user';

// 使用同步導入
import Home from '../views/Home.vue';
import Login from '../views/Login.vue';
import Register from '../views/Register.vue';
import RestaurantList from '../views/RestaurantList.vue';
import RestaurantForm from '../views/RestaurantForm.vue';
import RestaurantDetail from '../views/RestaurantDetail.vue';
import Profile from '../views/Profile.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home
    },
    {
      path: '/login',
      name: 'login',
      component: Login
    },
    {
      path: '/register',
      name: 'register',
      component: Register,
      meta: { 
        requiresGuest: true,  // 只允許未登入用戶訪問
        requiresAuth: false
      }
    },
    {
      path: '/restaurants',
      name: 'restaurants',
      component: RestaurantList
    },
    {
      path: '/restaurants/new',
      name: 'RestaurantCreate',
      component: RestaurantForm,
      meta: { requiresAuth: true },
    },
    {
      path: '/restaurants/:id',
      name: 'RestaurantDetail',
      component: RestaurantDetail,
    },
    {
      path: '/restaurants/:id/edit',
      name: 'RestaurantEditById',
      component: RestaurantForm,
      meta: { requiresAuth: true, isEdit: true },
    },
    {
      path: '/restaurants/edit/:id',
      name: 'RestaurantEdit',
      component: RestaurantForm,
      meta: { requiresAuth: true, isEdit: true },
    },
    {
      path: '/profile',
      name: 'Profile',
      component: Profile,
      meta: { requiresAuth: true },
    },
    {
      path: '/user',
      name: 'user',
      component: Profile,
      meta: { requiresAuth: true }
    }
  ]
});

// 簡單的路由守衛，只處理基本認證
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore();
  
  // 如果 store 還沒初始化，先初始化
  if (!userStore.isInitialized) {
    await userStore.initialize();
  }

  // 檢查是否需要登入
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login');
    return;
  }

  next();
});

export default router; 