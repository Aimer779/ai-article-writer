import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { getLoginUser, userLogout } from '@/api/userController'
import { USER_ROLE } from '@/constants/user'

export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.LoginUserVO>({})
  const loading = ref(false)

  const isLoggedIn = computed(() => Boolean(loginUser.value.id))
  const isAdmin = computed(() => loginUser.value.userRole === USER_ROLE.ADMIN)
  const isVip = computed(() => loginUser.value.userRole === USER_ROLE.VIP || loginUser.value.vip === true)

  function setLoginUser(user: API.LoginUserVO = {}) {
    loginUser.value = user
  }

  async function fetchLoginUser() {
    loading.value = true
    try {
      const response = await getLoginUser()
      const { data } = response
      if (data.code === 0 && data.data) {
        setLoginUser(data.data)
      } else {
        setLoginUser({})
      }
      return loginUser.value
    } finally {
      loading.value = false
    }
  }

  async function logout() {
    const response = await userLogout()
    if (response.data.code === 0 && response.data.data) {
      setLoginUser({})
    }
    return response
  }

  return {
    loginUser,
    loading,
    isLoggedIn,
    isAdmin,
    isVip,
    setLoginUser,
    fetchLoginUser,
    logout,
  }
})
