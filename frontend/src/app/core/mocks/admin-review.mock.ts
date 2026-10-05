import { Review } from '@core/models/common.model';

export const MOCK_ADMIN_REVIEWS: Review[] = [
  { id: 1, userId: 4, fullName: 'Nguyễn Văn A', rating: 5, comment: 'Sản phẩm rất tốt, đóng gói cẩn thận, giao hàng nhanh.', createdAt: new Date(Date.now() - 86400000 * 2).toISOString(), productName: 'Laptop gaming Gigabyte A16', skuId: 1, status: 'APPROVED' },
  { id: 2, userId: 5, fullName: 'Trần Thị B', rating: 4, comment: 'Chất lượng tuyệt vời, giá hợp lý. Sẽ mua thêm.', createdAt: new Date(Date.now() - 86400000 * 5).toISOString(), productName: 'NVIDIA RTX 4070 Super 12GB', skuId: 13, status: 'APPROVED' },
  { id: 3, userId: 6, fullName: 'Lê Văn C', rating: 5, comment: 'Hàng chính hãng, máy chạy mượt, nhiệt độ thấp.', createdAt: new Date(Date.now() - 86400000 * 7).toISOString(), productName: 'PC GVN I3-12100F/ RTX 5050', skuId: 7, status: 'APPROVED' },
  { id: 4, userId: 7, fullName: 'Phạm Thị D', rating: 1, comment: 'Toàn quảng cáo shop khác trong đây, đừng mua.', createdAt: new Date(Date.now() - 86400000 * 1).toISOString(), productName: 'Laptop gaming Gigabyte A16', skuId: 1, status: 'PENDING' },
  { id: 5, userId: 8, fullName: 'Hoàng Văn E', rating: 3, comment: 'Máy ổn nhưng giao hàng hơi chậm so với dự kiến.', createdAt: new Date(Date.now() - 86400000 * 0.5).toISOString(), productName: 'Intel Core i7-14700K', skuId: 14, status: 'PENDING' },
  { id: 6, userId: 4, fullName: 'Nguyễn Văn A', rating: 2, comment: 'Từ ngữ không phù hợp đã được lọc — nội dung vi phạm quy định.', createdAt: new Date(Date.now() - 86400000 * 3).toISOString(), productName: 'Samsung Odyssey OLED G8 34"', skuId: 15, status: 'HIDDEN' },
  { id: 7, userId: 5, fullName: 'Trần Thị B', rating: 5, comment: 'Màn hình đẹp, màu sắc trung thực, đáng tiền.', createdAt: new Date(Date.now() - 86400000 * 4).toISOString(), productName: 'LG UltraGear 27GP850', skuId: 16, status: 'APPROVED' },
  { id: 8, userId: 6, fullName: 'Lê Văn C', rating: 4, comment: 'Đóng gói ổn, tuy nhiên hộp hơi móp góc khi nhận.', createdAt: new Date(Date.now() - 86400000 * 0.8).toISOString(), productName: 'Logitech G Pro X Superlight 2', skuId: 17, status: 'PENDING' },
];
