// src/services/api.js
import axios from 'axios';

// Create axios instance with base configuration
const api = axios.create({
  // Relative by default: nginx (container) or the CRA dev-server "proxy" forwards /api to the backend.
  baseURL: process.env.REACT_APP_API_URL || '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, 
});

// Add request interceptor to include JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // Log the request for debugging
    console.log('Making request to:', config.baseURL + config.url);
    console.log('Request config:', {
      method: config.method,
      url: config.url,
      headers: config.headers,
    });
    
    return config;
  },
  (error) => {
    console.error('Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Add response interceptor to handle errors
api.interceptors.response.use(
  (response) => {
    console.log('API Response received:', {
      status: response.status,
      url: response.config.url,
      data: response.data
    });
    return response;
  },
  (error) => {
    console.error('API Error:', {
      message: error.message,
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      url: error.config?.url,
    });

    // Handle different types of errors
    if (error.code === 'ECONNABORTED') {
      console.error('Request timeout');
      error.userMessage = 'Request timeout. Please check your connection.';
    } else if (error.code === 'ERR_NETWORK') {
      console.error('Network error - possibly CORS or server not running');
      error.userMessage = 'Cannot connect to server. Please check if the server is running.';
    } else if (error.response?.status === 401) {
      // Token expired or invalid
      console.log('Authentication failed, clearing tokens');
      localStorage.removeItem('authToken');
      localStorage.removeItem('username');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
      error.userMessage = 'Session expired. Please log in again.';
    } else if (error.response?.status === 403) {
      error.userMessage = 'Access forbidden.';
    } else if (error.response?.status >= 500) {
      error.userMessage = 'Server error. Please try again later.';
    } else if (error.response?.data?.message) {
      error.userMessage = error.response.data.message;
    } else {
      error.userMessage = 'An unexpected error occurred.';
    }

    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  login: (credentials) => {
    console.log('Attempting login for username:', credentials.username);
    return api.post('/auth/login', credentials);
  },
};

// Clients API
export const clientsAPI = {
  getAll: (page = 0, size = 10) => api.get(`/clients?page=${page}&size=${size}`),
  getById: (id) => api.get(`/clients/${id}`),
  create: (data) => api.post('/clients', data),
  update: (data) => api.put('/clients', data),
  delete: (id) => api.delete(`/clients/${id}`),
};

// Drivers API
export const driversAPI = {
  getAll: (page = 0, size = 10) => api.get(`/drivers?page=${page}&size=${size}`),
  getById: (id) => api.get(`/drivers/${id}`),
  create: (data) => api.post('/drivers', data),
  update: (data) => api.put('/drivers', data),
  delete: (id) => api.delete(`/drivers/${id}`),
};

// Transport Logs API
export const transportLogsAPI = {
  getAll: (page = 0, size = 10) => api.get(`/trans_logs?page=${page}&size=${size}`),
  getById: (id) => api.get(`/trans_logs/${id}`),
  create: (data) => api.post('/trans_logs', data),
  update: (data) => api.put('/trans_logs', data),
  delete: (id) => api.delete(`/trans_logs/${id}`),
};

export default api;