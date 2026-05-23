import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '@/pages/HomePage.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: HomePage,
    },
    {
      path: '/user/login',
      name: 'Login',
      component: UserLoginPage,
    },
    {
      path: '/user/register',
      name: 'Register',
      component: UserRegisterPage,
    },
    {
      path: '/admin/userManage',
      name: 'User Management',
      component: () => import('@/pages/admin/UserManagePage.vue'),
    },
  ],
})

export default router
