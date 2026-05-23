import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { getLoginUser, userLogout, type LoginUserVO } from '@/api/user'
import { USER_ROLE } from '@/constants/user'

export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<LoginUserVO>({})
  const loading = ref(false)

  const isLoggedIn = computed(() => Boolean(loginUser.value.id))
  const isAdmin = computed(() => loginUser.value.userRole === USER_ROLE.ADMIN)

  function setLoginUser(user: LoginUserVO = {}) {
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
    setLoginUser,
    fetchLoginUser,
    logout,
  }
})
