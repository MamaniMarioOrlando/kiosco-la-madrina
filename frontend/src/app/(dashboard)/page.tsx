'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import api from '@/lib/api';
import { motion } from 'framer-motion';
import { Loader2, AlertTriangle, TrendingUp, Package, Tag, DollarSign } from 'lucide-react';
import { cn, formatCurrency } from '@/lib/utils';

interface Product {
  id: number;
  name: string;
  stockQuantity: number;
  price: number;
}

interface SaleDetail {
  productName: string;
  quantity: number;
}

interface Sale {
  dateTime: string;
  totalAmount: number;
  details: SaleDetail[];
}

export default function Home() {
  const router = useRouter();
  const [stats, setStats] = useState({
    todaySales: 0,
    productCount: 0,
    categoryCount: 0
  });
  const [lowStockProducts, setLowStockProducts] = useState<Product[]>([]);
  const [topSellers, setTopSellers] = useState<{ name: string, quantity: number }[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
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

      const products: Product[] = productsRes.data;
      const sales: Sale[] = salesRes.data;

      // Calculate Today's Sales
      const today = new Date().toLocaleDateString('es-AR');
      const todayTotal = sales
        .filter((s) => new Date(s.dateTime).toLocaleDateString('es-AR') === today)
        .reduce((sum, s) => sum + s.totalAmount, 0);

      // Low Stock Alerts (Stock < 5)
      const lowStock = products.filter(p => p.stockQuantity < 5);

      // Top Selling Products
      const productSales: Record<string, number> = {};
      sales.forEach(sale => {
        sale.details.forEach(detail => {
          productSales[detail.productName] = (productSales[detail.productName] || 0) + detail.quantity;
        });
      });

      const sortedSellers = Object.entries(productSales)
        .map(([name, quantity]) => ({ name, quantity }))
        .sort((a, b) => b.quantity - a.quantity)
        .slice(0, 5);

      setStats({
        productCount: products.length,
        categoryCount: categoriesRes.data.length,
        todaySales: todayTotal
      });
      setLowStockProducts(lowStock);
      setTopSellers(sortedSellers);
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
    <div className="space-y-8">
      <header>
        <h2 className="text-3xl font-black text-slate-900 dark:text-white">Panel de Control</h2>
        <p className="text-slate-500 dark:text-slate-400">Bienvenido al resumen de tu negocio en tiempo real.</p>
      </header>

      {/* Top Summary Cards */}
      <motion.div
        initial={{ y: 20, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        className="grid gap-6 md:grid-cols-2 lg:grid-cols-3"
      >
        <div className="p-6 bg-white dark:bg-slate-900 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-800 relative overflow-hidden group">
          <div className="absolute right-[-10px] top-[-10px] opacity-5 transform group-hover:scale-110 transition-transform">
            <DollarSign size={100} />
          </div>
          <h3 className="text-sm font-bold text-slate-400 dark:text-slate-500 uppercase tracking-widest">Ventas de Hoy</h3>
          <p className="text-4xl font-black text-orange-600 mt-2">{formatCurrency(stats.todaySales)}</p>
          <div className="mt-4 flex items-center text-xs font-bold text-orange-500 bg-orange-50 dark:bg-orange-950/30 px-2 py-1 rounded-full w-fit">
            Actualizado en vivo
          </div>
        </div>

        <div className="p-6 bg-white dark:bg-slate-900 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-800 relative overflow-hidden group">
          <div className="absolute right-[-10px] top-[-10px] opacity-5 transform group-hover:scale-110 transition-transform">
            <Package size={100} />
          </div>
          <h3 className="text-sm font-bold text-slate-400 dark:text-slate-500 uppercase tracking-widest">Productos</h3>
          <p className="text-4xl font-black text-blue-600 mt-2">{stats.productCount}</p>
          <div className="mt-4 flex items-center text-xs font-bold text-blue-500 bg-blue-50 dark:bg-blue-950/30 px-2 py-1 rounded-full w-fit">
            En el catálogo
          </div>
        </div>

        <div className="p-6 bg-white dark:bg-slate-900 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-800 relative overflow-hidden group">
          <div className="absolute right-[-10px] top-[-10px] opacity-5 transform group-hover:scale-110 transition-transform">
            <Tag size={100} />
          </div>
          <h3 className="text-sm font-bold text-slate-400 dark:text-slate-500 uppercase tracking-widest">Categorías</h3>
          <p className="text-4xl font-black text-green-600 mt-2">{stats.categoryCount}</p>
          <div className="mt-4 flex items-center text-xs font-bold text-green-500 bg-green-50 dark:bg-green-950/30 px-2 py-1 rounded-full w-fit">
            Organizadas
          </div>
        </div>
      </motion.div>

      {/* Secondary Info Grid */}
      <div className="grid gap-6 lg:grid-cols-2">
        {/* Low Stock Alerts */}
        <motion.div
          initial={{ x: -20, opacity: 0 }}
          animate={lowStockProducts.some(p => p.stockQuantity < 3) ? {
            x: 0,
            opacity: 1,
            boxShadow: ["0px 0px 0px rgba(239, 68, 68, 0)", "0px 0px 20px rgba(239, 68, 68, 0.2)", "0px 0px 0px rgba(239, 68, 68, 0)"],
            borderColor: ["rgba(226, 232, 240, 1)", "rgba(239, 68, 68, 0.5)", "rgba(226, 232, 240, 1)"]
          } : {
            x: 0,
            opacity: 1
          }}
          transition={lowStockProducts.some(p => p.stockQuantity < 3) ? {
            x: { duration: 0.5 },
            opacity: { duration: 0.5 },
            boxShadow: { duration: 2, repeat: Infinity },
            borderColor: { duration: 2, repeat: Infinity }
          } : { duration: 0.5 }}
          className="bg-white dark:bg-slate-900 p-6 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-800"
        >        <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-2">
              <div className={cn(
                "p-2 rounded-lg",
                lowStockProducts.some(p => p.stockQuantity < 3) ? "bg-red-100 text-red-600 animate-pulse" : "bg-orange-100 text-orange-600"
              )}>
                <AlertTriangle size={20} />
              </div>
              <h3 className="text-xl font-bold text-slate-900 dark:text-white">
                Reposición Urgente
              </h3>
            </div>
            <span className="bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400 px-3 py-1 rounded-full text-xs font-black">
              {lowStockProducts.length} ítems
            </span>
          </div>

          {lowStockProducts.length > 0 ? (
            <div className="space-y-3">
              {lowStockProducts.sort((a, b) => a.stockQuantity - b.stockQuantity).map(p => (
                <div key={p.id} className={cn(
                  "flex items-center justify-between p-3 rounded-xl border transition-all",
                  p.stockQuantity < 3
                    ? "bg-red-50 dark:bg-red-950/20 border-red-200 dark:border-red-900/40 shadow-sm"
                    : "bg-orange-50/50 dark:bg-orange-950/10 border-orange-100 dark:border-orange-900/20"
                )}>
                  <div className="flex items-center gap-3">
                    {p.stockQuantity < 3 && <div className="w-2 h-2 rounded-full bg-red-600 animate-ping" />}
                    <div className="font-bold text-slate-800 dark:text-slate-200">{p.name}</div>
                  </div>
                  <div className="flex items-center gap-4">
                    <span className="text-xs font-bold text-slate-500 dark:text-slate-400 uppercase">Quedan:</span>
                    <span className={cn(
                      "px-2 py-0.5 rounded font-black text-sm",
                      p.stockQuantity < 3 ? "bg-red-600 text-white" : "bg-orange-500 text-white"
                    )}>{p.stockQuantity}</span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="py-12 text-center text-slate-400 flex flex-col items-center gap-2">
              <Package className="opacity-20" size={48} />
              <p>No hay productos con stock bajo. ¡Buen trabajo!</p>
            </div>
          )}
        </motion.div>

        {/* Top Selling Products */}
        <motion.div
          initial={{ x: 20, opacity: 0 }}
          animate={{ x: 0, opacity: 1 }}
          className="bg-white dark:bg-slate-900 p-6 rounded-2xl shadow-sm border border-slate-100 dark:border-slate-800"
        >
          <div className="flex items-center justify-between mb-6">
            <h3 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
              <TrendingUp className="text-blue-500" /> Los Más Vendidos
            </h3>
            <span className="text-slate-400 dark:text-slate-500 text-xs font-bold uppercase tracking-widest">Total Histórico</span>
          </div>

          {topSellers.length > 0 ? (
            <div className="space-y-4">
              {topSellers.map((item, index) => (
                <div key={item.name} className="relative">
                  <div className="flex justify-between items-center mb-1">
                    <span className="font-bold text-slate-700 dark:text-slate-300 flex items-center gap-2">
                      <span className="w-5 h-5 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center text-[10px] text-slate-500">
                        {index + 1}
                      </span>
                      {item.name}
                    </span>
                    <span className="font-black text-slate-900 dark:text-white">{item.quantity} u.</span>
                  </div>
                  <div className="h-2 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-blue-500 rounded-full"
                      style={{ width: `${(item.quantity / topSellers[0].quantity) * 100}%` }}
                    ></div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="py-12 text-center text-slate-400 flex flex-col items-center gap-2">
              <TrendingUp className="opacity-20" size={48} />
              <p>Vende productos para ver el ranking aquí.</p>
            </div>
          )}
        </motion.div>
      </div>
    </div>
  );
}
