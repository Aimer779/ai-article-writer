<template>
  <div class="register-page">
    <a-card title="Register" style="width: 400px; margin: 100px auto;">
      <a-form
        :model="formState"
        name="register"
        @finish="handleRegister"
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

        <a-form-item
          label="Confirm Password"
          name="checkPassword"
          :rules="[
            { required: true, message: 'Please confirm your password!' },
            { validator: validateConfirmPassword }
          ]"
        >
          <a-input-password v-model:value="formState.checkPassword" />
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" block :loading="loading">
            Register
          </a-button>
        </a-form-item>

        <div style="text-align: center;">
          <router-link to="/user/login">Already have an account? Login</router-link>
        </div>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { userRegister, type UserRegisterRequest } from '@/api/user'

const router = useRouter()

const loading = ref(false)

const formState = reactive<UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const validateConfirmPassword = async (_rule: any, value: string) => {
  if (value && value !== formState.userPassword) {
    throw new Error('Passwords do not match!')
  }
}

const handleRegister = async (values: UserRegisterRequest) => {
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
.register-page {
  min-height: 100vh;
  background: #f0f2f5;
}
</style>
