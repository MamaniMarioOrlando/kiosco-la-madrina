import type { Metadata } from "next";
import { Outfit, Geist } from "next/font/google"; // Changed Inter to Outfit
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

const geist = Geist({subsets:['latin'],variable:'--font-sans'});


export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="es" suppressHydrationWarning className={cn("font-sans", geist.variable)}>
      <body className={cn(outfit.className, "min-h-screen antialiased transition-colors duration-300")}>
        <ThemeProvider>
          {children}
          <Toaster position="top-center" richColors />
        </ThemeProvider>
      </body>
    </html>
  );
}
