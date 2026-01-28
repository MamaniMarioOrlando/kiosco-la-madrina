import type { Metadata } from "next";
import { Outfit } from "next/font/google"; // Changed Inter to Outfit
import "./globals.css";
import { cn } from "@/lib/utils";

// Initialize the Outfit font
const outfit = Outfit({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "Kiosco La Madrina",
  description: "Sistema de gestión para Kiosco La Madrina",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="es">
      <body className={cn(outfit.className, "min-h-screen bg-background font-sans antialiased")}>
        {children}
      </body>
    </html>
  );
}
