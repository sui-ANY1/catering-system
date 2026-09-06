import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    username: localStorage.getItem('username') || ''
  }),
  getters: {
    isLogin: (state) => !!state.token
  },
  actions: {
    async login(form) {
      const res = await loginApi(form)
      // 后端登录接口 data 为 token 字符串
      this.token = res.data
      this.username = form.username
      localStorage.setItem('token', res.data)
      localStorage.setItem('username', form.username)
    },
    async logout() {
      try {
        await logoutApi()
      } catch (e) {
        // 登出失败也不阻塞本地清理
      }
      this.token = ''
      this.username = ''
      localStorage.removeItem('token')
      localStorage.removeItem('username')
    }
  }
})
