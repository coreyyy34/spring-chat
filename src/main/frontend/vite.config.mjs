import { defineConfig } from 'vite'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
    plugins: [
        tailwindcss(),
    ],
    build: {
        manifest: true,
        outDir: "dist",
        emptyOutDir: true,
        minify: false, // todo change for prod
        rollupOptions: {
            input: [
                "css/main.css",
                "js/main.js"
            ],
        },
    },
})