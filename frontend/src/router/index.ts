import { createRouter, createWebHistory } from 'vue-router'
import { message } from 'ant-design-vue'
import HomePage from '@/pages/HomePage.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import { useLoginUserStore } from '@/stores'
import { USER_ROLE } from '@/constants/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: HomePage,
    },
    {
      path: '/create',
      name: 'Create Article',
      component: () => import('@/pages/article/ArticleCreatePage.vue'),
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/articles',
      name: 'My Articles',
      component: () => import('@/pages/article/ArticleListPage.vue'),
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/article/:id',
      name: 'Article Detail',
      component: () => import('@/pages/article/ArticleDetailPage.vue'),
      meta: {
        requiresAuth: true,
      },
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
      meta: {
        requiresAuth: true,
        requiresAdmin: true,
      },
    },
  ],
})

router.beforeEach(async (to) => {
  const loginUserStore = useLoginUserStore()

  if (!loginUserStore.isLoggedIn && !to.path.startsWith('/user/')) {
    await loginUserStore.fetchLoginUser()
  }

  if (to.meta.requiresAuth && !loginUserStore.isLoggedIn) {
    return {
      path: '/user/login',
      query: {
        redirect: to.fullPath,
      },
    }
  }

  if (to.meta.requiresAdmin && loginUserStore.loginUser.userRole !== USER_ROLE.ADMIN) {
    message.warning('No permission')
    return '/'
  }
})

export default router
