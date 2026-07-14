const isSagemaker = process.env.SAGEMAKER === '1';
const basePath = process.env.NEXT_PUBLIC_BASE_PATH || '';

/** @type {import('next').NextConfig} */
const nextConfig = {
  basePath: basePath || undefined,
  trailingSlash: true,
  rewrites: async () => [
    {
      source: '/api/:path*',
      destination: 'http://localhost:8080/api/:path*',
    },
  ],
};

export default nextConfig;
