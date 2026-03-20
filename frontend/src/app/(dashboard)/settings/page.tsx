'use client';

import { useEffect, useState, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from '@/components/ui/card';
import { Settings, Shield, LogOut, Camera, Loader2 } from 'lucide-react';
import { motion } from 'framer-motion';
import { toast } from 'sonner';
import api from '@/lib/api';

export default function SettingsPage() {
    const router = useRouter();
    const [user, setUser] = useState<any>(null);
    const [isUploading, setIsUploading] = useState(false);
    const fileInputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        const userData = localStorage.getItem('user');
        if (userData) {
            setUser(JSON.parse(userData));
        }
    }, []);

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        router.push('/login');
    };

    const handleAvatarClick = () => {
        fileInputRef.current?.click();
    };

    const compressImage = (file: File): Promise<string> => {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = (event) => {
                const img = new Image();
                img.src = event.target?.result as string;
                img.onload = () => {
                    const canvas = document.createElement('canvas');
                    const MAX_WIDTH = 200;
                    const MAX_HEIGHT = 200;
                    let width = img.width;
                    let height = img.height;

                    if (width > height) {
                        if (width > MAX_WIDTH) {
                            height *= MAX_WIDTH / width;
                            width = MAX_WIDTH;
                        }
                    } else {
                        if (height > MAX_HEIGHT) {
                            width *= MAX_HEIGHT / height;
                            height = MAX_HEIGHT;
                        }
                    }

                    canvas.width = width;
                    canvas.height = height;
                    const ctx = canvas.getContext('2d');
                    ctx?.drawImage(img, 0, 0, width, height);
                    
                    // Compress to JPEG with 80% quality
                    const dataUrl = canvas.toDataURL('image/jpeg', 0.8);
                    resolve(dataUrl);
                };
                img.onerror = (error) => reject(error);
            };
            reader.onerror = (error) => reject(error);
        });
    };

    const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file || !user?.id) return;

        if (!file.type.startsWith('image/')) {
            toast.error('Por favor, selecciona una imagen válida.');
            return;
        }

        try {
            setIsUploading(true);
            const base64Image = await compressImage(file);
            
            await api.patch(`/users/${user.id}/avatar`, {
                avatarUrl: base64Image
            });

            // Update local storage and state
            const updatedUser = { ...user, avatarUrl: base64Image };
            localStorage.setItem('user', JSON.stringify(updatedUser));
            setUser(updatedUser);
            
            toast.success('Avatar actualizado con éxito.');
            // Refresh to update Navbar avatar immediately
            window.location.reload();
        } catch (err) {
            console.error('Failed to upload avatar', err);
            toast.error('Ocurrió un error al subir el avatar.');
        } finally {
            setIsUploading(false);
            if (fileInputRef.current) {
                fileInputRef.current.value = '';
            }
        }
    };

    const initial = user?.username ? user.username.charAt(0).toUpperCase() : 'U';

    return (
        <div className="space-y-6">
            <div className="flex items-center gap-2">
                <Settings className="h-6 w-6 text-slate-600" />
                <h2 className="text-2xl font-bold text-slate-900">Configuración</h2>
            </div>

            <motion.div
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                className="max-w-2xl"
            >
                <Card>
                    <CardHeader>
                        <CardTitle>Perfil de Usuario</CardTitle>
                        <CardDescription>Información del usuario y foto de perfil.</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        {/* Avatar Upload Section */}
                        <div className="flex flex-col sm:flex-row items-center gap-6 p-6 bg-slate-50 border border-slate-100 rounded-xl">
                            <div className="relative group">
                                <div className="h-24 w-24 rounded-full overflow-hidden bg-gradient-to-tr from-orange-500 to-orange-400 flex items-center justify-center text-white text-3xl font-bold shadow-md ring-4 ring-white">
                                    {user?.avatarUrl ? (
                                        <img src={user.avatarUrl} alt="Avatar" className="h-full w-full object-cover" />
                                    ) : (
                                        initial
                                    )}
                                </div>
                                <button 
                                    onClick={handleAvatarClick}
                                    disabled={isUploading}
                                    className="absolute inset-0 flex items-center justify-center rounded-full bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer disabled:cursor-not-allowed"
                                >
                                    {isUploading ? <Loader2 className="h-6 w-6 text-white animate-spin" /> : <Camera className="h-6 w-6 text-white" />}
                                </button>
                                <input 
                                    type="file" 
                                    ref={fileInputRef}
                                    onChange={handleFileChange}
                                    accept="image/*"
                                    className="hidden" 
                                />
                            </div>
                            <div className="text-center sm:text-left">
                                <p className="text-lg font-bold text-slate-900 capitalize">{user?.username || 'Cargando...'}</p>
                                <p className="text-sm text-slate-500 mb-2">Haz clic en la imagen para cambiar tu avatar.</p>
                                <Button 
                                    variant="outline" 
                                    size="sm" 
                                    onClick={handleAvatarClick}
                                    disabled={isUploading}
                                >
                                    {isUploading ? 'Subiendo...' : 'Cambiar Foto'}
                                </Button>
                            </div>
                        </div>

                        <div className="flex items-center gap-4 p-4 bg-slate-50 rounded-lg">
                            <div className="h-12 w-12 rounded-full bg-blue-100 flex items-center justify-center">
                                <Shield className="h-6 w-6 text-blue-600" />
                            </div>
                            <div>
                                <p className="text-sm font-medium text-slate-500">Rol asignado</p>
                                <div className="flex gap-2 mt-1">
                                    {user?.roles?.map((role: string) => (
                                        <span
                                            key={role}
                                            className="px-2 py-0.5 bg-blue-600 text-white text-xs font-bold rounded uppercase tracking-wider"
                                        >
                                            {role.replace('ROLE_', '')}
                                        </span>
                                    ))}
                                </div>
                            </div>
                        </div>
                    </CardContent>
                    <CardFooter className="bg-slate-50/50 flex justify-end">
                        <Button
                            variant="destructive"
                            onClick={handleLogout}
                            className="flex items-center gap-2"
                        >
                            <LogOut className="h-4 w-4" /> Cerrar Sesión
                        </Button>
                    </CardFooter>
                </Card>

                <div className="mt-8 p-6 border border-dashed border-slate-300 rounded-xl bg-slate-50/50">
                    <h3 className="text-sm font-bold text-slate-400 uppercase tracking-widest mb-2">Próximamente</h3>
                    <p className="text-sm text-slate-500">
                        Próximamente podrás configurar el nombre del Kiosco, el logo y los ticket de impresión desde aquí.
                    </p>
                </div>
            </motion.div>
        </div>
    );
}
