import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/axiosConfig';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token') || null);
  const [loading, setLoading] = useState(true);

  // Initialize auth state on application startup
  useEffect(() => {
    const initializeAuth = async () => {
      const storedToken = localStorage.getItem('token');
      const storedUser = localStorage.getItem('user');

      if (storedToken && storedUser) {
        try {
          setUser(JSON.parse(storedUser));
          setToken(storedToken);
          // Verify token validity with backend
          const response = await api.get('/api/auth/me');
          if (response.data && response.data.data) {
            setUser(response.data.data);
            localStorage.setItem('user', JSON.stringify(response.data.data));
          }
        } catch (error) {
          console.error('Session verification failed:', error);
          logout();
        }
      }
      setLoading(false);
    };

    initializeAuth();
  }, []);

  const login = async (email, password) => {
    try {
      const response = await api.post('/api/auth/login', { email, password });
      const authData = response.data.data;
      const jwtToken = authData.token;
      const userData = authData.user || {
        id: authData.id,
        name: authData.name,
        email: authData.email,
        role: authData.role,
      };

      localStorage.setItem('token', jwtToken);
      localStorage.setItem('user', JSON.stringify(userData));

      setToken(jwtToken);
      setUser(userData);

      return {
        success: true,
        user: userData,
        role: userData?.role,
      };
    } catch (error) {
      console.error('Login error:', error);
      const message =
        error.response?.data?.message || 'Invalid email or password. Please try again.';
      return {
        success: false,
        message,
      };
    }
  };

  const register = async (registerData) => {
    try {
      const response = await api.post('/api/auth/register', registerData);
      const authData = response.data.data;
      const jwtToken = authData.token;
      const userData = authData.user || {
        id: authData.id,
        name: authData.name,
        email: authData.email,
        role: authData.role,
      };

      localStorage.setItem('token', jwtToken);
      localStorage.setItem('user', JSON.stringify(userData));

      setToken(jwtToken);
      setUser(userData);

      return {
        success: true,
        user: userData,
        role: userData?.role,
      };
    } catch (error) {
      console.error('Registration error:', error);
      const message =
        error.response?.data?.message || 'Registration failed. Please check your details.';
      return {
        success: false,
        message,
      };
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  };

  const hasRole = (requiredRole) => {
    if (!user || !user.role) return false;
    if (Array.isArray(requiredRole)) {
      return requiredRole.includes(user.role);
    }
    return user.role === requiredRole;
  };

  const isAdmin = () => hasRole('ADMIN');
  const isStudent = () => hasRole('STUDENT');
  const isAccountant = () => hasRole('ACCOUNTANT');

  const value = {
    user,
    token,
    loading,
    isAuthenticated: !!token && !!user,
    login,
    register,
    logout,
    hasRole,
    isAdmin,
    isStudent,
    isAccountant,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;
