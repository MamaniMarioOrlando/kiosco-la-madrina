import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  reactCompiler: true,
  // Using ts-ignore because the types might not perfectly align with Next 15's new turbopack config
  // @ts-ignore
  turbopack: {
    root: process.cwd(),
  },
};

export default nextConfig;
