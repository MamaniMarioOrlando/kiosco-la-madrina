'use client';

import { Button } from '@/components/ui/button';
import { Sun, Moon } from 'lucide-react';
import { useTheme } from '@/components/providers/ThemeProvider';
import { UserProfile } from './UserProfile';

export function Navbar() {
    const { theme, toggleTheme } = useTheme();

    return (
        <header className="flex h-16 items-center justify-end gap-2 border-b bg-white dark:bg-slate-900 px-6 shadow-sm border-slate-200 dark:border-slate-800">
            {/* Theme Toggle */}
            <Button
                variant="ghost"
                size="icon"
                onClick={toggleTheme}
                className="text-slate-500 dark:text-slate-400 hover:text-orange-500 dark:hover:text-orange-400 hover:bg-orange-50 dark:hover:bg-orange-950/20 rounded-full transition-colors"
            >
                {theme === 'light' ? <Moon size={20} /> : <Sun size={20} />}
            </Button>

            {/* Separator */}
            <div className="h-4 w-[1px] bg-slate-200 dark:bg-slate-800 mx-1" />

            {/* User Profile Menu */}
            <UserProfile />
        </header>
    );
}
