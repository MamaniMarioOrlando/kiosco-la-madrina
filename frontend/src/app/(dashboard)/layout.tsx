import { Navbar } from '@/components/layout/Navbar';
import { Sidebar } from '@/components/layout/Sidebar';
import { Breadcrumbs } from '@/components/layout/Breadcrumbs';

import { TooltipProvider } from '@/components/ui/tooltip';

export default function DashboardLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    return (
        <TooltipProvider>
            <div className="flex min-h-screen bg-slate-50 dark:bg-slate-950 transition-colors duration-300">
                <Sidebar />
                <div className="flex flex-[1_1_0%] flex-col min-w-0">
                    <Navbar />
                    <main className="flex-1 p-4 sm:p-6 overflow-x-hidden overflow-y-auto w-full">
                        <Breadcrumbs />
                        {children}
                    </main>
                </div>
            </div>
        </TooltipProvider>
    );
}
