import { defineStore } from 'pinia'
import { ref } from 'vue'

// Example store
export const useAppStore = defineStore('app', () => {
  const count = ref(0)

  function increment() {
    count.value++
  }

  return { count, increment }
})
