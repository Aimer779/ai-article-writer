<template>
  <div class="auth-page">
    <div class="auth-card surface-card">
      <div class="auth-header">
        <div class="logo-mark">A</div>
        <h1 class="auth-title">Create an account</h1>
        <p class="auth-subtitle">Start generating articles with AI agents today</p>
      </div>

      <a-form
        :model="formState"
        name="register"
        class="auth-form"
        @finish="handleRegister"
      >
        <a-form-item
          name="userAccount"
          :rules="[{ required: true, message: 'Please enter an account' }]"
        >
          <a-input
            v-model:value="formState.userAccount"
            placeholder="Account"
            size="large"
          />
        </a-form-item>

        <a-form-item
          name="userPassword"
          :rules="[{ required: true, message: 'Please enter a password' }]"
        >
          <a-input-password
            v-model:value="formState.userPassword"
            placeholder="Password"
            size="large"
          />
        </a-form-item>

        <a-form-item
          name="checkPassword"
          :rules="[
            { required: true, message: 'Please confirm your password' },
            { validator: validateConfirmPassword }
          ]"
        >
          <a-input-password
            v-model:value="formState.checkPassword"
            placeholder="Confirm password"
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
            Get started
          </a-button>
        </a-form-item>

        <div class="auth-footer">
          <span>Already have an account?</span>
          <router-link to="/user/login">Log in</router-link>
        </div>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { userRegister } from '@/api/userController'

const router = useRouter()

const loading = ref(false)

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const validateConfirmPassword = async (_rule: any, value: string) => {
  if (value && value !== formState.userPassword) {
    throw new Error('Passwords do not match')
  }
}

const handleRegister = async (values: API.UserRegisterRequest) => {
  loading.value = true
  try {
    const response = await userRegister(values)
    if (response.data.code === 0) {
      message.success('Registration successful')
      router.push('/user/login')
    } else {
      message.error(response.data.message || 'Registration failed')
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
