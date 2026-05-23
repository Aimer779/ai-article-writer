<template>
  <div class="register-page">
    <a-card title="Register" style="width: 400px; margin: 100px auto;">
      <a-form
        :model="formState"
        name="register"
        @finish="handleRegister"
      >
        <a-form-item
          label="Username"
          name="username"
          :rules="[{ required: true, message: 'Please input your username!' }]"
        >
          <a-input v-model:value="formState.username" />
        </a-form-item>

        <a-form-item
          label="Password"
          name="password"
          :rules="[{ required: true, message: 'Please input your password!' }]"
        >
          <a-input-password v-model:value="formState.password" />
        </a-form-item>

        <a-form-item
          label="Confirm Password"
          name="confirmPassword"
          :rules="[
            { required: true, message: 'Please confirm your password!' },
            { validator: validateConfirmPassword }
          ]"
        >
          <a-input-password v-model:value="formState.confirmPassword" />
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" block>
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
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

const router = useRouter()

const formState = reactive({
  username: '',
  password: '',
  confirmPassword: '',
})

const validateConfirmPassword = async (_rule: any, value: string) => {
  if (value && value !== formState.password) {
    throw new Error('Passwords do not match!')
  }
}

const handleRegister = async (values: any) => {
  // TODO: Call register API
  console.log('Register:', values)
  message.success('Registration successful')
  router.push('/user/login')
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: #f0f2f5;
}
</style>
