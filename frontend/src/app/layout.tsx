import type { Metadata } from "next";
import { Outfit } from "next/font/google"; // Changed Inter to Outfit
import "./globals.css";
import { cn } from "@/lib/utils";

import { ThemeProvider } from "@/components/providers/ThemeProvider";

// Initialize the Outfit font
const outfit = Outfit({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "Kiosco La Madrina",
  description: "Sistema de gestión para Kiosco La Madrina",
};

import { Toaster } from "sonner";

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="es" suppressHydrationWarning>
      <body className={cn(outfit.className, "min-h-screen antialiased transition-colors duration-300")}>
        <ThemeProvider>
          {children}
          <Toaster position="top-right" richColors />
        </ThemeProvider>
      </body>
    </html>
  );
}
