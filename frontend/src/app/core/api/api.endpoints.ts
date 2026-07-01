import { environment } from '@env/environment';

const API = environment.apiUrl;

export const ApiEndpoints = {
  auth: {
    login: `${API}/api/auth/login`,
    register: `${API}/api/auth/register`,
    refresh: `${API}/api/auth/refresh`,
  },
  catalog: {
    skus: `${API}/api/catalog/skus`,
    skuDetail: (slug: string) => `${API}/api/catalog/skus/${slug}`,
    related: (slug: string) => `${API}/api/catalog/skus/${slug}/related`,
    reviews: (id: number) => `${API}/api/catalog/skus/${id}/reviews`,
    brands: `${API}/api/catalog/brands`,
    categories: `${API}/api/catalog/categories`,
  },
  cart: {
    base: `${API}/api/cart`,
    item: (skuId: number) => `${API}/api/cart/items/${skuId}`,
  },
  orders: {
    my: `${API}/api/orders/my`,
    detail: (id: number) => `${API}/api/orders/${id}`,
    cancel: (id: number) => `${API}/api/orders/${id}/cancel`,
    checkout: `${API}/api/orders/checkout`,
    review: (orderId: number, orderItemId: number) => `${API}/api/orders/${orderId}/items/${orderItemId}/reviews`,
  },
  payment: {
    vnpayInit: `${API}/api/payments/vnpay/initiate`,
  },
  promotion: {
    flashSales: `${API}/api/promotion/flash-sales/active`,
    vouchers: `${API}/api/promotions/vouchers`,
    validateVoucher: `${API}/api/promotions/vouchers/validate`,
  },
  admin: {
    dashboard: `${API}/api/admin/dashboard/summary`,
    revenue: `${API}/api/admin/dashboard/revenue`,
    orders: `${API}/api/admin/orders`,
    orderStatus: (id: number) => `${API}/api/admin/orders/${id}/status`,
  },
  inventory: {
    base: `${API}/api/inventory`,
    restock: (skuId: number) => `${API}/api/inventory/${skuId}/restock`,
  },
  builder: {
    slots: `${API}/api/builder/slots`,
    validate: `${API}/api/builder/validate`,
    templates: `${API}/api/builder/templates`,
    templateDetail: (id: number) => `${API}/api/builder/templates/${id}`,
  },
};
