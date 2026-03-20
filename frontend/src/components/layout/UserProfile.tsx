'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { motion, AnimatePresence } from 'framer-motion';
import { LogOut, Settings, ChevronDown } from 'lucide-react';
import { cn } from '@/lib/utils';

export function UserProfile() {
    const router = useRouter();
    const [isOpen, setIsOpen] = useState(false);
    const [username, setUsername] = useState<string>('');
    const [role, setRole] = useState<string>('');
    const [avatarUrl, setAvatarUrl] = useState<string>('');
    const dropdownRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const userData = localStorage.getItem('user');
        if (userData) {
            try {
                const parsed = JSON.parse(userData);
                setUsername(parsed.username || 'Usuario');
                if (parsed.roles && parsed.roles.length > 0) {
                    setRole(parsed.roles[0].replace('ROLE_', ''));
                }
                if (parsed.avatarUrl) {
                    setAvatarUrl(parsed.avatarUrl);
                }
            } catch (e) {
                setUsername('Usuario');
            }
        }
        
        const handleClickOutside = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };
        
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleLogout = () => {
        setIsOpen(false);
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        router.push('/login');
    };

    const isRoleAdmin = role.toLowerCase().includes('admin');
    const initial = username ? username.charAt(0).toUpperCase() : 'U';

    return (
        <div className="relative" ref={dropdownRef}>
            {/* Botón Disparador (Avatar + Nombre) */}
            <button 
                onClick={() => setIsOpen(!isOpen)}
                className="flex items-center gap-2 p-1 pr-2 rounded-full hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors focus:outline-none focus:ring-2 focus:ring-orange-500/50"
            >
                {/* Círculo del Avatar con gradiente */}
                <div className="flex h-8 w-8 overflow-hidden items-center justify-center rounded-full bg-gradient-to-tr from-orange-500 to-orange-400 text-white font-bold text-sm shadow-sm ring-2 ring-white dark:ring-slate-900">
                    {avatarUrl ? (
                         <img src={avatarUrl} alt="Avatar" className="h-full w-full object-cover" />
                    ) : (
                         initial
                    )}
                </div>
                {/* Nombre y Rol Visibles en Escritorio */}
                <div className="hidden sm:flex flex-col items-start mr-1 text-left">
                    <span className="text-sm font-semibold text-slate-700 dark:text-slate-200 leading-none capitalize">
                        {username || 'Usuario'}
                    </span>
                    <span className="text-[10px] font-medium text-slate-500 dark:text-slate-400 mt-0.5">
                        {role || 'User'}
                    </span>
                </div>
                {/* Flecha Animada (indicador de dropdown) */}
                <motion.div animate={{ rotate: isOpen ? 180 : 0 }} transition={{ duration: 0.2 }}>
                    <ChevronDown size={16} className="text-slate-400" />
                </motion.div>
            </button>

            {/* Menú Flotante (Dropdown) animado con Framer Motion */}
            <AnimatePresence>
                {isOpen && (
                    <motion.div
                        initial={{ opacity: 0, y: 10, scale: 0.95 }}
                        animate={{ opacity: 1, y: 0, scale: 1 }}
                        exit={{ opacity: 0, y: 10, scale: 0.95 }}
                        transition={{ duration: 0.15, ease: 'easeOut' }}
                        className="absolute right-0 mt-2 w-56 origin-top-right rounded-xl border border-slate-200 dark:border-slate-800 bg-white/95 dark:bg-slate-900/95 backdrop-blur-md p-1 shadow-lg ring-1 ring-black/5 focus:outline-none z-50"
                    >
                        {/* Información en la cabecera del menú */}
                        <div className="px-3 py-3 border-b border-slate-100 dark:border-slate-800 mb-1 flex flex-col items-start">
                            <p className="text-sm font-bold text-slate-900 dark:text-white truncate w-full capitalize">
                                {username}
                            </p>
                            <p className="text-xs text-slate-500 dark:text-slate-400 truncate w-full mb-1">
                                Panel de Control
                            </p>
                            <div className={cn(
                                "mt-1.5 inline-flex items-center px-2 py-0.5 rounded text-[10px] font-black uppercase tracking-tighter border",
                                isRoleAdmin
                                    ? "bg-orange-50 border-orange-200 text-orange-600 dark:bg-orange-900/20 dark:border-orange-900/50 dark:text-orange-400"
                                    : "bg-blue-50 border-blue-200 text-blue-600 dark:bg-blue-900/20 dark:border-blue-900/50 dark:text-blue-400"
                            )}>
                                {role || 'User'}
                            </div>
                        </div>

                        {/* Opciones del menú */}
                        <div className="space-y-0.5">
                            <button
                                onClick={() => { setIsOpen(false); router.push('/settings'); }}
                                className="group flex w-full items-center gap-2 rounded-md px-2 py-2 text-sm font-medium text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-white transition-colors"
                            >
                                <Settings size={16} className="text-slate-400 group-hover:text-amber-500 transition-colors" />
                                Configuración
                            </button>
                            
                            <div className="h-[1px] bg-slate-100 dark:bg-slate-800 my-1 mx-2" />
                            
                            <button
                                onClick={handleLogout}
                                className="group flex w-full items-center gap-2 rounded-md px-2 py-2 text-sm font-medium text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-950/30 transition-colors"
                            >
                                <LogOut size={16} className="text-red-400 group-hover:text-red-600 dark:group-hover:text-red-300 transition-colors" />
                                Cerrar Sesión
                            </button>
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    );
}
