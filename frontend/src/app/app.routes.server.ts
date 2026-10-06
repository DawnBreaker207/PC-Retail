import { RenderMode, ServerRoute } from '@angular/ssr';

// Pages that depend on the session are client-rendered: the server never
// has a session, so SSR would serve the wrong (guest) state and break
// hydration. Public content stays on runtime SSR for SEO.
export const serverRoutes: ServerRoute[] = [
  { path: 'products/:slug', renderMode: RenderMode.Server },
  { path: 'tin-tuc/:slug', renderMode: RenderMode.Server },
  { path: 'chinh-sach/:slug', renderMode: RenderMode.Server },
  // Fully static content: bake at build time (no data to go stale).
  { path: 'gioi-thieu', renderMode: RenderMode.Prerender },
  { path: 'lien-he', renderMode: RenderMode.Prerender },
  { path: 'faq', renderMode: RenderMode.Prerender },
  { path: 'he-thong-showroom', renderMode: RenderMode.Prerender },
  { path: 'cart', renderMode: RenderMode.Client },
  { path: 'checkout', renderMode: RenderMode.Client },
  { path: 'checkout/**', renderMode: RenderMode.Client },
  { path: 'orders', renderMode: RenderMode.Client },
  { path: 'orders/**', renderMode: RenderMode.Client },
  { path: 'account', renderMode: RenderMode.Client },
  { path: 'account/**', renderMode: RenderMode.Client },
  { path: 'yeu-thich', renderMode: RenderMode.Client },
  { path: 'admin', renderMode: RenderMode.Client },
  { path: 'admin/**', renderMode: RenderMode.Client },
  // Catch-all: everything else renders per request; new public routes keep
  // working without touching this file.
  { path: '**', renderMode: RenderMode.Server },
];
