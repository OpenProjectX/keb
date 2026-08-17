import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    host: "127.0.0.1",
    port: 4173,
    strictPort: true,
  },
  preview: {
    host: "0.0.0.0",
    allowedHosts: ["host.docker.internal"],
    port: 4173,
    strictPort: true,
  },
});
