<template>
  <div class="login-page">
    <a-card title="Login" style="width: 400px; margin: 100px auto;">
      <a-form
        :model="formState"
        name="login"
        @finish="handleLogin"
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

        <a-form-item>
          <a-button type="primary" html-type="submit" block>
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
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

const router = useRouter()

const formState = reactive({
  username: '',
  password: '',
})

const handleLogin = async (values: any) => {
  // TODO: Call login API
  console.log('Login:', values)
  message.success('Login successful')
  router.push('/')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: #f0f2f5;
}
</style>
