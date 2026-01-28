'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import api from '@/lib/api';
import { motion } from 'framer-motion';
import { Loader2 } from 'lucide-react';

export default function Home() {
  const router = useRouter();
  const [stats, setStats] = useState({
    todaySales: 0,
    productCount: 0,
    categoryCount: 0
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Basic client-side auth check
    const token = localStorage.getItem('token');
    if (!token) {
      router.push('/login');
    } else {
      fetchDashboardData();
    }
  }, [router]);

  const fetchDashboardData = async () => {
    try {
      const [productsRes, categoriesRes, salesRes] = await Promise.all([
        api.get('/products'),
        api.get('/categories'),
        api.get('/sales')
      ]);

      const today = new Date().toLocaleDateString();
      const todayTotal = salesRes.data
        .filter((s: any) => new Date(s.dateTime).toLocaleDateString() === today)
        .reduce((sum: number, s: any) => sum + s.totalAmount, 0);

      setStats({
        productCount: productsRes.data.length,
        categoryCount: categoriesRes.data.length,
        todaySales: todayTotal
      });
    } catch (err) {
      console.error('Error fetching dashboard stats', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-900">Resumen del Sistema</h2>
        <p className="text-slate-500">Estado actual de tu negocio hoy.</p>
      </div>

      <motion.div
        initial={{ scale: 0.9, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        className="grid gap-6 md:grid-cols-2 lg:grid-cols-3"
      >
        <div className="p-6 bg-white rounded-xl shadow-md border-l-4 border-l-orange-500 border border-slate-100">
          <h3 className="text-sm font-semibold text-slate-500 uppercase tracking-wider">Ventas de Hoy</h3>
          <p className="text-4xl font-black text-slate-900 mt-2">${stats.todaySales.toFixed(2)}</p>
          <div className="mt-4 text-xs text-orange-600 font-medium bg-orange-50 inline-block px-2 py-1 rounded">Actualizado ahora</div>
        </div>

        <div className="p-6 bg-white rounded-xl shadow-md border-l-4 border-l-blue-500 border border-slate-100">
          <h3 className="text-sm font-semibold text-slate-500 uppercase tracking-wider">Productos</h3>
          <p className="text-4xl font-black text-slate-900 mt-2">{stats.productCount}</p>
          <div className="mt-4 text-xs text-blue-600 font-medium bg-blue-50 inline-block px-2 py-1 rounded">En inventario</div>
        </div>

        <div className="p-6 bg-white rounded-xl shadow-md border-l-4 border-l-green-500 border border-slate-100">
          <h3 className="text-sm font-semibold text-slate-500 uppercase tracking-wider">Categorías</h3>
          <p className="text-4xl font-black text-slate-900 mt-2">{stats.categoryCount}</p>
          <div className="mt-4 text-xs text-green-600 font-medium bg-green-50 inline-block px-2 py-1 rounded">Organizadas</div>
        </div>
      </motion.div>
    </div>
  );
}
