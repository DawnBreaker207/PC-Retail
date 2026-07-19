const baseUrl = 'https://dummyimage.com';

export const MOCK_DASHBOARD_SUMMARY = {
  todayRevenue: 256900000,
  yesterdayRevenue: 235000000,
  todayOrders: 45,
  yesterdayOrders: 38,
  newCustomers: 12,
  lowStockCount: 5,
  recentOrders: [
    { id: 1001, customerName: 'Nguyễn Văn A', total: 28790000, status: 'COMPLETED', createdAt: new Date(Date.now() - 86400000 * 3).toISOString() },
    { id: 1002, customerName: 'Trần Thị B', total: 15990000, status: 'PROCESSING', createdAt: new Date(Date.now() - 86400000 * 10).toISOString() },
    { id: 1003, customerName: 'Lê Văn C', total: 17990000, status: 'PENDING', createdAt: new Date(Date.now() - 86400000 * 15).toISOString() },
  ],
};

export const MOCK_REVENUE_7_DAYS = Array.from({ length: 7 }, (_, i) => ({
  date: new Date(Date.now() - (6 - i) * 86400000).toISOString().split('T')[0],
  revenue: Math.floor(Math.random() * 50000000) + 10000000,
}));

export const MOCK_LOW_STOCK_ITEMS = [
  { skuId: 17, name: 'Samsung Odyssey OLED G8 34"', stock: 3, imageUrl: `${baseUrl}/400x400/1e293b/white?text=Odyssey`, price: 25990000 },
  { skuId: 1, name: 'Gigabyte A16 i5-13420H', stock: 2, imageUrl: `${baseUrl}/400x400/1e293b/white?text=Gigabyte`, price: 28790000 },
  { skuId: 5, name: 'DELL XPS 15 9530', stock: 4, imageUrl: `${baseUrl}/400x400/1e293b/white?text=XPS+15`, price: 32990000 },
];
