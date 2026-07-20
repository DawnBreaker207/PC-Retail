import { environment } from '@env/environment';

const API = environment.apiUrl;

export const ApiEndpoints = {
  auth: {
    login: `${API}/api/v1/auth/login`,
    register: `${API}/api/v1/auth/register`,
    refresh: `${API}/api/v1/auth/refresh`,
    logout: `${API}/api/v1/auth/logout`,
    forgotPassword: `${API}/api/v1/auth/forgot-password`,
    resetPassword: `${API}/api/v1/auth/reset-password`,
    verifyOtp: `${API}/api/v1/auth/verify-otp`,
    resendOtp: `${API}/api/v1/auth/resend-otp`,
  },
  user: {
    me: `${API}/api/v1/users/me`,
    addresses: `${API}/api/v1/users/me/addresses`,
    address: (id: number) => `${API}/api/v1/users/me/addresses/${id}`,
    deactivate: `${API}/api/v1/users/me/deactivate`,
  },
  catalog: {
    skus: `${API}/api/v1/skus`,
    skuDetail: (id: number) => `${API}/api/v1/skus/${id}`,
    compare: `${API}/api/v1/skus/compare`,
    brands: `${API}/api/v1/brands`,
    categories: `${API}/api/v1/categories`,
  },
  cart: {
    base: `${API}/api/v1/cart`,
    count: `${API}/api/v1/cart/count`,
    items: `${API}/api/v1/cart/items`,
    item: (skuId: number) => `${API}/api/v1/cart/items/${skuId}`,
  },
  builder: {
    slots: `${API}/api/v1/builder/slots`,
    validate: `${API}/api/v1/builder/validate`,
    templates: `${API}/api/v1/builder/templates`,
    templateDetail: (id: number) => `${API}/api/v1/builder/templates/${id}`,
  },
  orders: {
    base: `${API}/api/v1/orders`,
    checkout: `${API}/api/v1/orders/checkout`,
    detail: (id: number) => `${API}/api/v1/orders/${id}`,
    cancelSubOrder: (id: number) => `${API}/api/v1/orders/sub-orders/${id}/cancel`,
  },
  staffOrders: {
    transition: (id: number) => `${API}/api/v1/staff/orders/sub-orders/${id}/transition`,
  },
  carriers: {
    assign: (subOrderId: number) => `${API}/api/v1/sub-orders/${subOrderId}/assign-carrier`,
    deliver: (subOrderId: number) => `${API}/api/v1/sub-orders/${subOrderId}/deliver`,
  },
  payment: {
    create: `${API}/api/v1/payments/create`,
    callback: (provider: string) => `${API}/api/v1/payments/callback/${provider}`,
    webhook: (provider: string) => `${API}/api/v1/payments/webhook/${provider}`,
    reCheck: (provider: string, id: number) => `${API}/api/v1/payments/re-check/${provider}/${id}`,
  },
  promotion: {
    vouchers: `${API}/api/v1/vouchers`,
    voucherDetail: (id: number) => `${API}/api/v1/vouchers/${id}`,
    validateVoucher: `${API}/api/v1/vouchers/validate`,
  },
  reviews: {
    create: `${API}/api/v1/reviews`,
  },
  inventory: {
    base: `${API}/api/v1/inventory`,
    detail: (skuId: number) => `${API}/api/v1/inventory/${skuId}`,
    restock: (skuId: number) => `${API}/api/v1/inventory/${skuId}/restock`,
    transactions: (skuId: number) => `${API}/api/v1/inventory/${skuId}/transactions`,
    lowStock: `${API}/api/v1/inventory/low-stock`,
  },
  admin: {
    orders: `${API}/api/v1/staff/orders`,
    dashboard: `${API}/api/v1/admin/dashboard/summary`,
    revenue: `${API}/api/v1/admin/dashboard/revenue`,
  },
};
