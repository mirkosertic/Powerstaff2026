import { defineConfig } from 'vite';

export default defineConfig({
  build: {
    rollupOptions: {
      input: 'src/main.js',
      output: {
        entryFileNames: 'app.js',
        assetFileNames: '[name][extname]',
      },
    },
    outDir: '../src/main/resources/static/generated',
    emptyOutDir: true,
  },
});
