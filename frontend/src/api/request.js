import axios from 'axios'
import { ElMessage } from 'element-plus'

// 统一 axios 实例
// 后端统一返回 Result { code, msg, data }，code=200 成功、500 失败、401 未登录
const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截：注入 Sa-Token（后端 token-name: token）
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['token'] = token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截：解包 Result，统一处理错误
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 非标准结构（如文件下载）直接返回
    if (res === null || typeof res !== 'object' || res.code === undefined) {
      return res
    }

    if (res.code === 200) {
      return res
    }

    // 未登录 / 登录失效
    if (res.code === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      ElMessage.error('登录已失效，请重新登录')
      // 避免循环跳转
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.msg || '请先登录'))
    }

    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => {
    const msg =
      error.response?.data?.msg ||
      error.message ||
      '网络异常，请检查后端服务是否启动'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default service
