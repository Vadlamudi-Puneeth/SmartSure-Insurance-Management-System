import axios, { InternalAxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/';

const axiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,
  withCredentials: true, // Required so browser sends/receives HttpOnly cookies
});

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: any) => void;
}> = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

const forceLogout = () => {
  localStorage.clear();
  window.location.href = '/login';
};

// ── REQUEST INTERCEPTOR ──
axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// ── RESPONSE INTERCEPTOR (Handling 401 & Token Refresh) ──
axiosInstance.interceptors.response.use(
  (response: AxiosResponse) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as any;

    // Avoid infinite loops on refresh or login calls
    if (
      error.response?.status !== 401 ||
      !originalRequest ||
      originalRequest._retry ||
      originalRequest.url?.includes('refresh-token') ||
      originalRequest.url?.includes('login')
    ) {
      return Promise.reject(error);
    }

    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      }).then((token) => {
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = 'Bearer ' + token;
        }
        return axiosInstance(originalRequest);
      }).catch((err) => Promise.reject(err));
    }

    originalRequest._retry = true;
    isRefreshing = true;

    try {
      // Use clean axios to avoid interceptor loop. 
      // Refresh token is now sent automatically via HttpOnly cookie.
      const response = await axios.post(`${BASE_URL}auth-service/api/auth/refresh-token`, null, {
        withCredentials: true
      });
      
      const { token: newAccessToken } = response.data;
      if (!newAccessToken) throw new Error('Refresh failed');

      localStorage.setItem('token', newAccessToken);
      // No need to store refreshToken in localStorage anymore!

      processQueue(null, newAccessToken);
      
      if (originalRequest.headers) {
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
      }
      return axiosInstance(originalRequest);
    } catch (refreshError) {
      processQueue(refreshError, null);
      forceLogout();
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  }
);

export default axiosInstance;
