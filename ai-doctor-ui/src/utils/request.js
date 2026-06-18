//定制请求的实例

//导入axios  npm install axios
import axios from 'axios';
import { ElMessage } from 'element-plus'

//定义一个变量,记录公共的前缀  ,  baseURL
// const baseURL = 'http://localhost:8080';
const baseURL = '/api';
const instance = axios.create({
    baseURL,
    timeout: 30000,
    headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
    }
})

// 添加请求拦截器
instance.interceptors.request.use(
    config => {
        console.log(`Request: ${config.method.toUpperCase()} ${config.url}`);
        
        // 从本地存储中获取token
        const userInfo = localStorage.getItem('userInfo')
        if (userInfo) {
            try {
                const parsedInfo = JSON.parse(userInfo)
                const token = parsedInfo.token
                if (token) {
                    config.headers.Authorization = token; // backend expects raw token
                    console.log('Token added to request headers');
                } else {
                    console.log('No token found in userInfo');
                }
            } catch (e) {
                console.error('Error parsing userInfo from localStorage:', e);
            }
        } else {
            console.log('No userInfo in localStorage');
        }

        // 如果是表单数据，确保正确处理
        if (config.headers['Content-Type'] === 'application/x-www-form-urlencoded' && config.data instanceof URLSearchParams) {
            config.data = config.data.toString()
        }

        return config
    },
    error => {
        console.error('Request error:', error);
        return Promise.reject(error)
    }
)

//添加响应拦截器
instance.interceptors.response.use(
    response => {
        console.log(`Response from ${response.config.url}:`, response.data);
        
        if (response.data.code === 0) {
            return response.data
        } else {
          //  ElMessage.error(response.data.msg || '服务器响应错误')
            console.error('API error response:', response.data);
            return Promise.reject(response.data)
        }
    },
    error => {
        console.error('Response error:', error);
        
        if (error.response) {
            switch (error.response.status) {
                case 401:
                    ElMessage.error('未授权，请重新登录')
                    localStorage.removeItem('userInfo')
                    setTimeout(() => {
                        window.location.href = '/login'
                    }, 1500)
                    break
                case 403:
                    ElMessage.error('拒绝访问')
                    break
                case 404:
                    ElMessage.error('请求错误，未找到该资源')
                    break
                case 500:
                    ElMessage.error('服务器错误')
                    break
                default:
                    ElMessage.error(`网络错误(${error.response.status})`)
            }
        } else if (error.request) {
            ElMessage.error('服务器未响应，请检查服务器是否运行')
            console.error('No response received:', error.request);
        } else {
            ElMessage.error('网络错误，请检查您的网络连接')
        }
        return Promise.reject(error)
    }
)

export default instance;