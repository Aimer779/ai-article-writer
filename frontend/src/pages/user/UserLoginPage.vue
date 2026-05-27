<template>
  <div class="auth-page">
    <div class="auth-card surface-card">
      <div class="auth-header">
        <div class="logo-mark">A</div>
        <h1 class="auth-title">Welcome back</h1>
        <p class="auth-subtitle">Log in to your AI Article Writer account</p>
      </div>

      <a-form
        :model="formState"
        name="login"
        class="auth-form"
        @finish="handleLogin"
      >
        <a-form-item
          name="userAccount"
          :rules="[{ required: true, message: 'Please enter your account' }]"
        >
          <a-input
            v-model:value="formState.userAccount"
            placeholder="Account"
            size="large"
          />
        </a-form-item>

        <a-form-item
          name="userPassword"
          :rules="[{ required: true, message: 'Please enter your password' }]"
        >
          <a-input-password
            v-model:value="formState.userPassword"
            placeholder="Password"
            size="large"
          />
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            block
            size="large"
            :loading="loading"
            class="submit-btn"
          >
            Log in
          </a-button>
        </a-form-item>

        <div class="auth-footer">
          <span>Don't have an account?</span>
          <router-link to="/user/register">Get started</router-link>
        </div>
      </a-form>
    </div>
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
      message.success('Logged in')
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
.auth-page {
  min-height: calc(100vh - 60px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-5);
}

.auth-card {
  width: 100%;
  max-width: 420px;
  padding: var(--space-6) var(--space-5);
}

.auth-header {
  text-align: center;
  margin-bottom: var(--space-5);
}

.logo-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: var(--accent);
  color: #fff;
  font-weight: 700;
  font-size: 18px;
  margin-bottom: var(--space-3);
}

.auth-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--ink);
  margin-bottom: var(--space-1);
  letter-spacing: -0.018em;
}

.auth-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
}

.auth-form :deep(.ant-input),
.auth-form :deep(.ant-input-password) {
  font-size: 15px;
}

.submit-btn {
  font-weight: 600;
  font-size: 15px;
}

.auth-footer {
  text-align: center;
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: var(--space-2);
}

.auth-footer a {
  margin-left: 4px;
  font-weight: 600;
}
</style>
