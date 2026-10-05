import { AdminReturnRequest } from '@core/models/common.model';

export const MOCK_RETURN_REQUESTS: AdminReturnRequest[] = [
  {
    id: 1, orderId: 1001, subOrderId: 2001, skuName: 'Laptop gaming Gigabyte A16',
    requestType: 'DOA', reason: 'Máy không lên nguồn ngay khi mở hộp, nghi lỗi mainboard.',
    status: 'PENDING', createdByUserId: 4, createdByName: 'Nguyễn Văn A',
    createdAt: new Date(Date.now() - 86400000 * 1).toISOString(),
  },
  {
    id: 2, orderId: 1002, subOrderId: 2002, skuName: 'NVIDIA RTX 4070 Super 12GB',
    requestType: 'WARRANTY', reason: 'Card bị artifact màn hình sau 3 tháng sử dụng.',
    status: 'PENDING', createdByUserId: 5, createdByName: 'Trần Thị B',
    createdAt: new Date(Date.now() - 86400000 * 2).toISOString(),
  },
  {
    id: 3, orderId: 1003, subOrderId: 2003, skuName: 'PC GVN I3-12100F/ RTX 5050',
    requestType: 'WARRANTY', reason: 'Máy tự khởi động lại khi chơi game nặng.',
    status: 'APPROVED', createdByUserId: 6, createdByName: 'Lê Văn C',
    createdAt: new Date(Date.now() - 86400000 * 6).toISOString(),
    approvedBy: 'Admin DAWN', approvedAt: new Date(Date.now() - 86400000 * 5).toISOString(),
  },
  {
    id: 4, orderId: 1001, subOrderId: 2001, skuName: 'Laptop gaming Gigabyte A16',
    requestType: 'DOA', reason: 'Bàn phím bị liệt 3 phím sau 2 ngày sử dụng.',
    status: 'REJECTED', createdByUserId: 4, createdByName: 'Nguyễn Văn A',
    createdAt: new Date(Date.now() - 86400000 * 9).toISOString(),
    approvedBy: 'Admin DAWN', approvedAt: new Date(Date.now() - 86400000 * 8).toISOString(),
    rejectReason: 'Đã quá 7 ngày quy định đổi mới DOA, chuyển sang bảo hành sửa chữa thông thường.',
  },
  {
    id: 5, orderId: 1002, subOrderId: 2002, skuName: 'NVIDIA RTX 4070 Super 12GB',
    requestType: 'WARRANTY', reason: 'Quạt tản nhiệt phát ra tiếng kêu lạ khi tải nặng.',
    status: 'PENDING', createdByUserId: 5, createdByName: 'Trần Thị B',
    createdAt: new Date(Date.now() - 86400000 * 0.5).toISOString(),
  },
];
