import {
  AngularNodeAppEngine,
  createNodeRequestHandler,
  isMainModule,
  writeResponseToNodeResponse,
} from '@angular/ssr/node';
import express from 'express';
import { join } from 'node:path';

const browserDistFolder = join(import.meta.dirname, '../browser');

const app = express();
const angularApp = new AngularNodeAppEngine({
  // TODO: This is a security-sensitive option. Remove if not needed. For more information, see https://angular.dev/best-practices/security#configuring-trusted-proxy-headers
  trustProxyHeaders: ['x-forwarded-host', 'x-forwarded-proto'],
});

// robots.txt + sitemap.xml use the request host, so they stay correct
// behind any domain (nginx, previews) without rebuilds.
const STATIC_ROUTES = ['/', '/products', '/pc-builder', '/khuyen-mai', '/tin-tuc'];
app.get('/robots.txt', (req, res) => {
  const site = `${req.protocol}://${req.get('host')}`;
  res
    .type('text/plain')
    .send(
      [
        'User-agent: *',
        'Allow: /',
        'Disallow: /admin/',
        'Disallow: /checkout/',
        'Disallow: /account/',
        'Disallow: /orders/',
        '',
        `Sitemap: ${site}/sitemap.xml`,
        '',
      ].join('\n'),
    );
});
app.get('/sitemap.xml', (req, res) => {
  const site = `${req.protocol}://${req.get('host')}`;
  const urls = STATIC_ROUTES.map(
    (p) => `  <url><loc>${site}${p}</loc><changefreq>daily</changefreq></url>`,
  ).join('\n');
  res
    .type('application/xml')
    .send(
      `<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n${urls}\n</urlset>`,
    );
});

app.use(
  express.static(browserDistFolder, {
    maxAge: '1y',
    index: false,
    redirect: false,
  }),
);

// Proxy /api requests to backend API gateway (http://localhost:8080)
const BACKEND_API = process.env['API_GATEWAY_URL'] || 'http://localhost:8080';
app.use('/api', async (req, res) => {
  try {
    const targetUrl = `${BACKEND_API}${req.originalUrl}`;
    const headers = { ...req.headers };
    delete headers.host;
    const fetchOptions: RequestInit = {
      method: req.method,
      headers: headers as any,
    };
    if (req.method !== 'GET' && req.method !== 'HEAD') {
      const chunks: Buffer[] = [];
      for await (const chunk of req) chunks.push(chunk);
      fetchOptions.body = Buffer.concat(chunks);
      (fetchOptions as any).duplex = 'half';
    }
    const backendRes = await fetch(targetUrl, fetchOptions);
    res.status(backendRes.status);
    backendRes.headers.forEach((val, key) => {
      if (key !== 'transfer-encoding' && key !== 'content-encoding') res.setHeader(key, val);
    });
    const buf = await backendRes.arrayBuffer();
    res.send(Buffer.from(buf));
  } catch (err: any) {
    res.status(502).json({ code: 502, message: err?.message || 'Gateway proxy error' });
  }
});

app.use((req, res, next) => {
  angularApp
    .handle(req)
    .then((response) => (response ? writeResponseToNodeResponse(response, res) : next()))
    .catch(next);
});

if (isMainModule(import.meta.url) || process.env['pm_id']) {
  const port = process.env['PORT'] || 4000;
  app.listen(port, (error) => {
    if (error) {
      throw error;
    }

    console.log(`Node Express server listening on http://localhost:${port}`);
  });
}

// Request handler for dev-server and serverless adapters.
export const reqHandler = createNodeRequestHandler(app);
