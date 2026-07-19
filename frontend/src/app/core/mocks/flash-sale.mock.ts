const baseUrl = 'https://dummyimage.com';

export const MOCK_FLASH_SALE_CAMPAIGN = {
  id: 1, campaignName: 'FLASH SALE - DEAL GẤP RÚT',
  endTime: new Date(Date.now() + 3 * 3600 * 1000).toISOString(),
  items: [
    { skuId: 13, name: 'NVIDIA RTX 4070 Super 12GB', slug: 'rtx-4070-super', imageUrl: `${baseUrl}/200x200/e53935/white?text=RTX+4070`, originalPrice: 18990000, salePrice: 15990000, remaining: 8, total: 20, category: 'Linh kiện', discountPercent: 16 },
    { skuId: 1, name: 'Gigabyte A16 i5-13420H', slug: 'gigabyte-a16', imageUrl: `${baseUrl}/200x200/e53935/white?text=Gigabyte`, originalPrice: 30790000, salePrice: 28790000, remaining: 5, total: 15, category: 'Laptop', discountPercent: 6 },
    { skuId: 20, name: 'Logitech G Pro X Superlight 2', slug: 'g-pro-x-superlight-2', imageUrl: `${baseUrl}/200x200/e53935/white?text=Superlight`, originalPrice: 3990000, salePrice: 3290000, remaining: 12, total: 30, category: 'Phụ kiện', discountPercent: 18 },
    { skuId: 17, name: 'Samsung Odyssey OLED G8 34"', slug: 'odyssey-oled-g8', imageUrl: `${baseUrl}/200x200/e53935/white?text=Odyssey`, originalPrice: 29990000, salePrice: 25990000, remaining: 3, total: 10, category: 'Màn hình', discountPercent: 13 },
    { skuId: 15, name: 'Intel Core i7-14700K', slug: 'i7-14700k', imageUrl: `${baseUrl}/200x200/e53935/white?text=i7-14700K`, originalPrice: 10990000, salePrice: 8990000, remaining: 15, total: 40, category: 'Linh kiện', discountPercent: 18 },
  ],
};
