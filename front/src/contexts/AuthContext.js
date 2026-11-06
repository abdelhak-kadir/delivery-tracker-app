// src/contexts/AuthContext.js
import React, { createContext, useState, useContext, useEffect } from 'react';
import { authAPI } from '../services/api';

const AuthContext = createContext();


export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is logged in on app start
    const token = localStorage.getItem('authToken');
    const username = localStorage.getItem('username');
    
    console.log('Auth check on startup:', { token: !!token, username });
    
    if (token && username) {
      setIsAuthenticated(true);
      setUser({ username });
    }
    setLoading(false);
  }, []);

  const login = async (credentials) => {
    try {
      console.log('AuthContext: Attempting login...');
      console.log('API URL being used:', process.env.REACT_APP_API_URL || 'http://localhost:8080/api/v1');
      
      const response = await authAPI.login(credentials);
      
      console.log('Login response:', response);
      
      // Check if response has the expected structure
      if (!response.data || !response.data.success || !response.data.data) {
        console.error('Unexpected response structure:', response.data);
        return { 
          success: false, 
          error: 'Invalid response from server' 
        };
      }
      
      const { data } = response.data;
      
      if (!data.token || !data.username) {
        console.error('Missing token or username in response:', data);
        return { 
          success: false, 
          error: 'Invalid login response' 
        };
      }
      
      console.log('Login successful, storing tokens...');
      localStorage.setItem('authToken', data.token);
      localStorage.setItem('username', data.username);
      
      setIsAuthenticated(true);
      setUser({ username: data.username });
      
      return { success: true };
      
    } catch (error) {
      console.error('Login error details:', {
        message: error.message,
        response: error.response,
        request: error.request,
        code: error.code,
        userMessage: error.userMessage
      });
      
      let errorMessage = 'Login failed';
      
      if (error.userMessage) {
        errorMessage = error.userMessage;
      } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.code === 'ERR_NETWORK') {
        errorMessage = 'Cannot connect to server. Please check if the server is running on http://localhost:8080';
      } else if (error.code === 'ECONNABORTED') {
        errorMessage = 'Connection timeout. Please try again.';
      } else if (error.message.includes('404')) {
        errorMessage = 'Login endpoint not found. Please check server configuration.';
      }
      
      return { 
        success: false, 
        error: errorMessage
      };
    }
  };

  const logout = () => {
    console.log('Logging out...');
    localStorage.removeItem('authToken');
    localStorage.removeItem('username');
    setIsAuthenticated(false);
    setUser(null);
  };

  const value = {
    isAuthenticated,
    user,
    login,
    logout,
    loading
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};