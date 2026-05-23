<template>
  <div class="login-page">
    <a-card title="Login" style="width: 400px; margin: 100px auto;">
      <a-form
        :model="formState"
        name="login"
        @finish="handleLogin"
      >
        <a-form-item
          label="Account"
          name="userAccount"
          :rules="[{ required: true, message: 'Please input your account!' }]"
        >
          <a-input v-model:value="formState.userAccount" />
        </a-form-item>

        <a-form-item
          label="Password"
          name="userPassword"
          :rules="[{ required: true, message: 'Please input your password!' }]"
        >
          <a-input-password v-model:value="formState.userPassword" />
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" block :loading="loading">
            Login
          </a-button>
        </a-form-item>

        <div style="text-align: center;">
          <router-link to="/user/register">Don't have an account? Register</router-link>
        </div>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { userLogin } from '@/api/userController'
import { useLoginUserStore } from '@/stores'

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

const loading = ref(false)

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const getRedirectPath = () => {
  const redirect = route.query.redirect
  if (typeof redirect !== 'string') {
    return '/'
  }
  if (redirect.startsWith('http')) {
    const target = new URL(redirect)
    return target.origin === window.location.origin
      ? `${target.pathname}${target.search}${target.hash}`
      : '/'
  }
  return redirect
}

const handleLogin = async (values: API.UserLoginRequest) => {
  loading.value = true
  try {
    const response = await userLogin(values)
    if (response.data.code === 0 && response.data.data) {
      loginUserStore.setLoginUser(response.data.data)
      message.success('Login successful')
      router.push(getRedirectPath())
    } else {
      message.error(response.data.message || 'Login failed')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: #f0f2f5;
}
</style>
