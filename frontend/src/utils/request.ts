import axios from 'axios'
import { message } from 'ant-design-vue'

// Create Axios instance
const myAxios = axios.create({
  baseURL: 'http://localhost:8567/api',
  timeout: 60000,
  withCredentials: true,  // Required! Carry cookies
})

// Global response interceptor
myAxios.interceptors.response.use(
  function (response) {
    const { data } = response
    // Not logged in
    if (data.code === 40100) {
      if (
        !response.request.responseURL.includes('user/get/login') &&
        !window.location.pathname.includes('/user/login')
      ) {
        message.warning('Please login first')
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  function (error) {
    return Promise.reject(error)
  },
)

export default myAxios
