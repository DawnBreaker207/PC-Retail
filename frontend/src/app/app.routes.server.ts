import { RenderMode, ServerRoute } from '@angular/ssr';

// Param routes can't prerender without getPrerenderParams; '**' keeps everything else on runtime SSR.
export const serverRoutes: ServerRoute[] = [
  { path: 'products/:slug', renderMode: RenderMode.Server },
  { path: 'tin-tuc/:slug', renderMode: RenderMode.Server },
  { path: 'chinh-sach/:slug', renderMode: RenderMode.Server },
  { path: 'orders/:id', renderMode: RenderMode.Server },
  { path: 'pc-builder/templates/:id', renderMode: RenderMode.Server },
  { path: 'admin/orders/:id', renderMode: RenderMode.Server },
  { path: 'admin/returns/:id', renderMode: RenderMode.Server },
  // Catch-all: everything else renders per request, so the build never breaks on new routes.
  { path: '**', renderMode: RenderMode.Server },
];
