import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./layout/main-layout/main-layout').then((m) => m.MainLayout),
    children: [
      {
        path: '',
        loadChildren: () => import('./features/home/home.routes').then((r) => r.HOME_ROUTES),
      },
      { path: 'home', redirectTo: '/', pathMatch: 'full' },
      {
        path: 'auth',
        loadChildren: () => import('./features/auth/auth.routes').then((r) => r.AUTH_ROUTES),
      },
      {
        path: 'cart',
        loadChildren: () => import('./features/cart/cart.routes').then((r) => r.CART_ROUTES),
        data: { title: 'Giỏ hàng' },
      },
      {
        path: 'checkout',
        loadChildren: () => import('./features/checkout/checkout.routes').then((r) => r.CHECKOUT_ROUTES),
        data: { title: 'Thanh toán' },
      },
      {
        path: 'orders',
        loadChildren: () => import('./features/orders/orders.routes').then((r) => r.ORDER_ROUTES),
      },
      {
        path: 'tra-cuu-don-hang',
        loadComponent: () => import('./features/orders/pages/order-lookup.component').then((m) => m.OrderLookupComponent),
      },
      {
        path: 'danh-muc',
        loadComponent: () => import('./features/catalog/pages/category-grid.component').then((m) => m.CategoryGridComponent),
      },
      {
        path: 'pc-builder',
        loadChildren: () => import('./features/builder/builder.routes').then((r) => r.BUILDER_ROUTES),
        data: {
          title: 'Build PC',
          description: 'Tự build PC theo nhu cầu, kiểm tra tương thích linh kiện tự động.',
        },
      },
      {
        path: 'products',
        loadChildren: () => import('./features/catalog/catalog.routes').then((r) => r.CATALOG_ROUTES),
        data: {
          title: 'Sản phẩm',
          description: 'Laptop, PC, linh kiện chính hãng giá tốt tại DAWN PC Retail.',
        },
      },
      {
        path: 'yeu-thich',
        loadChildren: () => import('./features/wishlist/wishlist.routes').then((r) => r.WISHLIST_ROUTES),
      },
      {
        path: 'khuyen-mai',
        loadChildren: () => import('./features/promotions/promotions.routes').then((r) => r.PROMOTIONS_ROUTES),
      },
      {
        path: 'account',
        loadChildren: () => import('./features/account/account.routes').then((r) => r.ACCOUNT_ROUTES),
      },
      {
        path: 'tin-tuc',
        loadChildren: () => import('./features/blog/blog.routes').then((r) => r.BLOG_ROUTES),
      },
      {
        path: '',
        loadChildren: () => import('./features/content/content.routes').then((r) => r.CONTENT_ROUTES),
      },
      {
        path: 'error',
        loadChildren: () => import('./features/error/error.routes').then((r) => r.ERROR_ROUTES),
      },
    ],
  },
  {
    path: 'admin',
    loadChildren: () => import('./features/admin/admin.routes').then((r) => r.ADMIN_ROUTES),
  },
  { path: '**', redirectTo: '/error/404' },
];
