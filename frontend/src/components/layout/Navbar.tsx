'use client';

import { Button } from '@/components/ui/button';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { LogOut } from 'lucide-react';

export function Navbar() {
    const router = useRouter();
    const [username, setUsername] = useState<string>('');

    useEffect(() => {
        const userData = localStorage.getItem('user');
        if (userData) {
            try {
                const parsed = JSON.parse(userData);
                setUsername(parsed.username || 'Usuario');
            } catch (e) {
                setUsername('Usuario');
            }
        }
    }, []);

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        router.push('/login');
    };

    return (
        <header className="flex h-16 items-center justify-end gap-4 border-b bg-white px-6 shadow-sm">
            <div className="text-sm font-medium text-slate-600">
                Hola, <span className="text-slate-900">{username}</span>
            </div>
            <Button
                variant="ghost"
                size="sm"
                onClick={handleLogout}
                className="text-slate-500 hover:text-red-600"
            >
                <LogOut className="mr-2 h-4 w-4" />
                Salir
            </Button>
        </header>
    );
}
