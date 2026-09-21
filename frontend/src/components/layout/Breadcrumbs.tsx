'use client';

import { usePathname } from 'next/navigation';
import Link from 'next/link';

export function Breadcrumbs() {
    const pathname = usePathname();
    const paths = pathname.split('/').filter(p => p !== '');

    if (paths.length === 0) return null; // We are at the root/dashboard

    return (
        <nav className="flex items-center space-x-2 text-sm text-muted-foreground mb-6">
            <Link href="/" className="hover:text-foreground transition-colors">
                Dashboard
            </Link>
            {paths.map((path, index) => {
                const href = `/${paths.slice(0, index + 1).join('/')}`;
                const isLast = index === paths.length - 1;
                const label = path.charAt(0).toUpperCase() + path.slice(1);

                return (
                    <div key={path} className="flex items-center space-x-2">
                        <span>&gt;</span>
                        {isLast ? (
                            <span className="text-foreground font-medium">{label}</span>
                        ) : (
                            <Link href={href} className="hover:text-foreground transition-colors">
                                {label}
                            </Link>
                        )}
                    </div>
                );
            })}
        </nav>
    );
}
