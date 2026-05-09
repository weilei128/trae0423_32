import axios from 'axios'
import { Message } from 'element-ui'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('Response error:', error)
    if (error.response) {
      const responseData = error.response.data
      let message = ''
      
      if (error.response.status === 400 && responseData && responseData.errors) {
        const errors = responseData.errors
        message = Object.values(errors).join('; ')
      } else if (responseData && responseData.message) {
        message = responseData.message
      } else if (responseData && responseData.error) {
        message = responseData.error
      } else {
        message = error.response.statusText || '请求失败'
      }
      
      Message.error(message)
    } else {
      Message.error('网络错误，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
