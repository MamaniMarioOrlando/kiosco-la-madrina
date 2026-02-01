'use client';

import { useEffect, useState } from 'react';
import api from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { cn, formatCurrency } from '@/lib/utils';
import { toast } from 'sonner';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import { Search, ShoppingCart, Trash2, Plus, Minus, CheckCircle2, Loader2, Barcode } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

interface Product {
    id: number;
    barcode: string;
    name: string;
    price: number;
    stockQuantity: number;
}

interface CartItem extends Product {
    quantity: number;
}

export default function SalesPage() {
    const [allProducts, setAllProducts] = useState<Product[]>([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [cart, setCart] = useState<CartItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [amountPaid, setAmountPaid] = useState<string>('');
    const [saleSuccess, setSaleSuccess] = useState(false);

    useEffect(() => {
        fetchProducts();
    }, []);

    const fetchProducts = async () => {
        try {
            const response = await api.get('/products');
            setAllProducts(response.data);
        } catch (err) {
            console.error('Error fetching products', err);
        } finally {
            setLoading(false);
        }
    };

    const filteredProducts = allProducts.filter(p =>
        p.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        p.barcode.includes(searchTerm)
    ).slice(0, 5);

    const addToCart = (product: Product) => {
        if (product.stockQuantity <= 0) {
            alert('Producto sin stock disponible.');
            return;
        }

        setCart(prev => {
            const existing = prev.find(item => item.id === product.id);
            if (existing) {
                if (existing.quantity >= product.stockQuantity) {
                    alert('No hay más stock disponible.');
                    return prev;
                }
                return prev.map(item =>
                    item.id === product.id ? { ...item, quantity: item.quantity + 1 } : item
                );
            }
            return [...prev, { ...product, quantity: 1 }];
        });
        setSearchTerm('');
    };

    const updateQuantity = (id: number, delta: number) => {
        setCart(prev => prev.map(item => {
            if (item.id === id) {
                const newQty = item.quantity + delta;
                if (newQty <= 0) return item;
                if (newQty > item.stockQuantity) {
                    alert('Stock insuficiente.');
                    return item;
                }
                return { ...item, quantity: newQty };
            }
            return item;
        }));
    };

    const removeFromCart = (id: number) => {
        setCart(prev => prev.filter(item => item.id !== id));
    };

    const total = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const change = parseFloat(amountPaid) > 0 ? parseFloat(amountPaid) - total : 0;

    const handleCheckout = async () => {
        if (cart.length === 0) return;
        setSubmitting(true);
        try {
            const payload = {
                items: cart.map(item => ({
                    productId: item.id,
                    quantity: item.quantity
                }))
            };
            await api.post('/sales', payload);
            setSaleSuccess(true);
            setCart([]);
            setAmountPaid('');

            // Check for low stock after sale
            const updatedProductsRes = await api.get('/products');
            const updatedProducts: Product[] = updatedProductsRes.data;
            setAllProducts(updatedProducts);

            updatedProducts.forEach(p => {
                if (p.stockQuantity < 5) {
                    toast.warning(`Stock bajo: ${p.name}`, {
                        description: `Quedan solo ${p.stockQuantity} unidades.`,
                        duration: 5000,
                    });
                }
            });

            setTimeout(() => setSaleSuccess(false), 3000);
        } catch (err: any) {
            console.error(err);
            alert('Error al procesar la venta. Revisa el stock o tu conexión.');
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) {
        return (
            <div className="flex h-full items-center justify-center">
                <Loader2 className="h-8 w-8 animate-spin text-orange-500" />
            </div>
        );
    }

    return (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 space-y-6">
                <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-200">
                    <h2 className="text-xl font-bold mb-4 text-slate-800 flex items-center gap-2">
                        <Search className="h-5 w-5 text-orange-500" /> Buscar Producto
                    </h2>
                    <div className="relative">
                        <Barcode className="absolute left-3 top-3 h-5 w-5 text-slate-400" />
                        <Input
                            placeholder="Nombre o código de barras..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="pl-10 h-12 text-lg"
                            autoFocus
                        />
                    </div>

                    {searchTerm && (
                        <div className="mt-4 border rounded-lg overflow-hidden divide-y bg-slate-50">
                            {filteredProducts.length > 0 ? filteredProducts.map(p => (
                                <div
                                    key={p.id}
                                    className="p-4 hover:bg-orange-50 cursor-pointer flex justify-between items-center transition-colors"
                                    onClick={() => addToCart(p)}
                                >
                                    <div>
                                        <div className="font-bold text-slate-900">{p.name}</div>
                                        <div className="text-sm text-slate-500 font-mono italic">{p.barcode}</div>
                                    </div>
                                    <div className="text-right">
                                        <div className="text-lg font-bold text-orange-600 dark:text-orange-400">{formatCurrency(p.price)}</div>
                                        <div className="text-xs text-slate-500">{p.stockQuantity} disponibles</div>
                                    </div>
                                </div>
                            )) : (
                                <div className="p-4 text-center text-slate-500">No se encontraron productos.</div>
                            )}
                        </div>
                    )}
                </div>
            </div>

            <div className="bg-white rounded-xl shadow-lg border border-slate-200 flex flex-col h-[calc(100vh-140px)]">
                <div className="p-4 border-b bg-slate-900 text-white rounded-t-xl flex items-center justify-between">
                    <h2 className="font-bold flex items-center gap-2">
                        <ShoppingCart className="h-5 w-5" /> Carrito
                    </h2>
                    <span className="bg-orange-600 text-[10px] px-2 py-0.5 rounded-full uppercase font-bold">
                        {cart.length} items
                    </span>
                </div>

                <div className="flex-1 overflow-auto p-4 space-y-4">
                    <AnimatePresence initial={false}>
                        {cart.length === 0 ? (
                            <div className="h-full flex flex-col items-center justify-center text-slate-400 opacity-50 space-y-2">
                                <ShoppingCart className="h-12 w-12" />
                                <p>El carrito está vacío</p>
                            </div>
                        ) : (
                            cart.map(item => (
                                <motion.div
                                    key={item.id}
                                    initial={{ opacity: 0, x: 20 }}
                                    animate={{ opacity: 1, x: 0 }}
                                    exit={{ opacity: 0, x: -20 }}
                                    className="bg-slate-50 p-3 rounded-lg border border-slate-100 flex flex-col gap-2"
                                >
                                    <div className="flex justify-between items-start">
                                        <span className="font-medium text-slate-800">{item.name}</span>
                                        <button onClick={() => removeFromCart(item.id)} className="text-slate-400 hover:text-red-500">
                                            <Trash2 className="h-4 w-4" />
                                        </button>
                                    </div>
                                    <div className="flex justify-between items-center">
                                        <div className="flex items-center gap-2 border rounded-md bg-white p-1">
                                            <button onClick={() => updateQuantity(item.id, -1)} className="p-1 hover:bg-slate-100 rounded text-slate-600">
                                                <Minus className="h-3 w-3" />
                                            </button>
                                            <span className="w-8 text-center font-bold text-sm">{item.quantity}</span>
                                            <button onClick={() => updateQuantity(item.id, 1)} className="p-1 hover:bg-slate-100 dark:hover:bg-slate-800 rounded text-slate-600 dark:text-slate-400">
                                                <Plus className="h-3 w-3" />
                                            </button>
                                        </div>
                                        <span className="font-bold text-slate-900 dark:text-white">{formatCurrency(item.price * item.quantity)}</span>
                                    </div>
                                </motion.div>
                            ))
                        )}
                    </AnimatePresence>
                </div>

                <div className="p-4 bg-slate-50 dark:bg-slate-900/50 border-t border-slate-200 dark:border-slate-800 space-y-4">
                    <div className="flex justify-between items-center text-lg">
                        <span className="text-slate-600 dark:text-slate-400">Total:</span>
                        <span className="text-3xl font-black text-slate-900 dark:text-white">{formatCurrency(total)}</span>
                    </div>

                    {cart.length > 0 && (
                        <div className="space-y-3 pt-2 border-t border-slate-200">
                            <div className="space-y-1">
                                <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Paga con:</span>
                                <Input
                                    type="number"
                                    placeholder="0.00"
                                    value={amountPaid}
                                    onChange={(e) => setAmountPaid(e.target.value)}
                                    className="text-lg font-bold h-11"
                                />
                            </div>

                            {parseFloat(amountPaid) > 0 && (
                                <motion.div
                                    initial={{ opacity: 0, height: 0 }}
                                    animate={{ opacity: 1, height: 'auto' }}
                                    className="flex justify-between items-center p-3 rounded-lg bg-white border border-slate-200"
                                >
                                    <span className="text-slate-600 font-medium">Vuelto:</span>
                                    <span className={cn(
                                        "text-xl font-bold",
                                        change < 0 ? "text-red-500" : "text-green-600 dark:text-green-400"
                                    )}>
                                        {formatCurrency(change)}
                                    </span>
                                </motion.div>
                            )}
                        </div>
                    )}

                    {saleSuccess && (
                        <motion.div
                            initial={{ opacity: 0, y: 10 }}
                            animate={{ opacity: 1, y: 0 }}
                            className="bg-green-100 text-green-700 p-2 rounded text-center text-sm font-bold flex items-center justify-center gap-2"
                        >
                            <CheckCircle2 className="h-4 w-4" /> Venta registrada con éxito
                        </motion.div>
                    )}

                    <Button
                        onClick={handleCheckout}
                        className="w-full h-14 text-xl font-bold bg-orange-600 hover:bg-orange-700"
                        disabled={cart.length === 0 || submitting || (parseFloat(amountPaid) > 0 && change < 0)}
                    >
                        {submitting ? 'Procesando...' : 'COBRAR'}
                    </Button>
                </div>
            </div>
        </div>
    );
}
