import axios from 'axios';
import { clearSession, getToken } from './session';

const api = axios.create({
    baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

api.interceptors.request.use(
    (config) => {
        if (typeof window !== 'undefined') {
            const token = getToken();
            if (token) {
                config.headers.Authorization = `Bearer ${token}`;
            }
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

api.interceptors.response.use(
    (response) => response,
    (error) => {
        // Un 401 en el login significa credenciales inválidas, no sesión vencida:
        // redirigir recargaría la página y borraría el mensaje de error.
        const isLoginRequest = error.config?.url?.includes('/auth/signin');
        if (error.response?.status === 401 && !isLoginRequest) {
            if (typeof window !== 'undefined') {
                clearSession();
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

export default api;
