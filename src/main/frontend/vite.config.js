import { defineConfig } from 'vite';

export default defineConfig({
  build: {
    rollupOptions: {
      input: 'src/main.js',
      output: {
        entryFileNames: 'app.js',
        assetFileNames: 'app[extname]',
      },
    },
    outDir: '../../../target/classes/static/generated',
    emptyOutDir: true,
  },
});
