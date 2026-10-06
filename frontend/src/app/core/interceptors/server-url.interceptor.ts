import { HttpInterceptorFn } from '@angular/common/http';

// Server-only: Node can't call relative URLs, so prefix the backend origin.
export const serverUrlInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.startsWith('/api/')) {
    const base =
      typeof process !== 'undefined' && process.env['BACKEND_URL']
        ? process.env['BACKEND_URL']
        : 'http://localhost:8888';
    req = req.clone({ url: `${base}${req.url}` });
  }
  return next(req);
};
