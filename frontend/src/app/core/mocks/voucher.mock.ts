export const MOCK_VOUCHERS = [
  { id: 1, code: 'WELCOME10', description: 'Giảm 500k cho đơn hàng đầu tiên', discount: 500000, type: 'FIXED', minOrder: 1000000, expiry: '31/12/2026' },
  { id: 2, code: 'SALE10', description: 'Giảm 10% cho đơn hàng trên 5 triệu', discount: 10, type: 'PERCENT', maxDiscount: 1000000, minOrder: 5000000, expiry: '31/12/2026' },
];
