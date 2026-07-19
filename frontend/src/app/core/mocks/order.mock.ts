const baseUrl = 'https://dummyimage.com';

export const MOCK_ORDERS = [
  {
    id: 1001, createdAt: new Date(Date.now() - 86400000 * 3).toISOString(),
    totalAmount: 28790000, paymentStatus: 'PAID',
    shippingAddress: { fullName: 'Nguyễn Văn A', phone: '0912345678', street: '123 Nguyễn Huệ', district: 'Quận 1', city: 'TP HCM' },
    subOrders: [{
      id: 2001, status: 'DELIVERED', fulfillmentType: 'DELIVERY', subtotal: 28790000, shippingFee: 0, discountAmount: 0, finalAmount: 28790000,
      items: [{ id: 3001, skuId: 1, name: 'Laptop gaming Gigabyte A16', imageUrl: `${baseUrl}/400x400/1e293b/white?text=Gigabyte`, quantity: 1, unitPrice: 28790000 }],
    }],
  },
  {
    id: 1002, createdAt: new Date(Date.now() - 86400000 * 10).toISOString(),
    totalAmount: 15990000, paymentStatus: 'PAID',
    shippingAddress: { fullName: 'Trần Thị B', phone: '0987654321', street: '456 Lê Lợi', district: 'Hải Châu', city: 'Đà Nẵng' },
    subOrders: [{
      id: 2002, status: 'PROCESSING', fulfillmentType: 'DELIVERY', subtotal: 15990000, shippingFee: 0, discountAmount: 0, finalAmount: 15990000,
      items: [{ id: 3002, skuId: 13, name: 'NVIDIA RTX 4070 Super 12GB', imageUrl: `${baseUrl}/400x400/1e293b/white?text=RTX+4070`, quantity: 1, unitPrice: 15990000 }],
    }],
  },
  {
    id: 1003, createdAt: new Date(Date.now() - 86400000 * 15).toISOString(),
    totalAmount: 17990000, paymentStatus: 'PENDING',
    shippingAddress: { fullName: 'Lê Văn C', phone: '0909123456', street: '789 Trần Hưng Đạo', district: 'Thanh Khê', city: 'Đà Nẵng' },
    subOrders: [{
      id: 2003, status: 'PENDING_PAYMENT', fulfillmentType: 'PICKUP', subtotal: 17990000, shippingFee: 0, discountAmount: 0, finalAmount: 17990000,
      items: [{ id: 3003, skuId: 7, name: 'PC GVN I3-12100F/ RTX 5050', imageUrl: `${baseUrl}/400x400/1e293b/white?text=PC+I3`, quantity: 1, unitPrice: 17990000 }],
    }],
  },
];
