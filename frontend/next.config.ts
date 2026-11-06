import type { NextConfig } from "next";
import { buildSecurityHeaders } from "./securityHeaders";

const securityHeaders = buildSecurityHeaders({
  apiUrl: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api",
  isDev: process.env.NODE_ENV !== "production",
});

const nextConfig: NextConfig = {
  /* config options here */
  reactCompiler: true,
  // Sin "X-Powered-By: Next.js": no se le regala a un atacante qué framework (y qué fallas) buscar.
  poweredByHeader: false,
  turbopack: {
    root: process.cwd(),
  },
  async headers() {
    return [{ source: "/:path*", headers: securityHeaders }];
  },
};

export default nextConfig;
