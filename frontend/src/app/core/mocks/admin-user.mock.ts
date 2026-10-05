import { User } from '@core/models/common.model';

export const MOCK_ADMIN_USERS: User[] = [
  { id: 1, fullName: 'Admin DAWN', email: 'admin@dawn.vn', phone: '0900000001', systemRole: 'ADMIN', status: 'ACTIVE', createdAt: '2026-01-05T08:00:00.000Z' },
  { id: 2, fullName: 'Nguyễn Văn Staff', email: 'staff1@dawn.vn', phone: '0900000002', systemRole: 'STAFF', status: 'ACTIVE', createdAt: '2026-01-10T08:00:00.000Z' },
  { id: 3, fullName: 'Trần Thị Staff', email: 'staff2@dawn.vn', phone: '0900000003', systemRole: 'STAFF', status: 'ACTIVE', createdAt: '2026-02-01T08:00:00.000Z' },
  { id: 4, fullName: 'Nguyễn Văn A', email: 'user@dawn.vn', phone: '0912345678', systemRole: 'CUSTOMER', status: 'ACTIVE', createdAt: '2026-02-15T08:00:00.000Z' },
  { id: 5, fullName: 'Trần Thị B', email: 'tranthib@example.com', phone: '0987654321', systemRole: 'CUSTOMER', status: 'ACTIVE', createdAt: '2026-03-01T08:00:00.000Z' },
  { id: 6, fullName: 'Lê Văn C', email: 'levanc@example.com', phone: '0909123456', systemRole: 'CUSTOMER', status: 'ACTIVE', createdAt: '2026-03-20T08:00:00.000Z' },
  { id: 7, fullName: 'Phạm Thị D', email: 'phamthid@example.com', phone: '0933112233', systemRole: 'CUSTOMER', status: 'INACTIVE', createdAt: '2026-04-02T08:00:00.000Z' },
  { id: 8, fullName: 'Hoàng Văn E', email: 'hoangvane@example.com', phone: '0977889900', systemRole: 'CUSTOMER', status: 'ACTIVE', createdAt: '2026-05-10T08:00:00.000Z' },
];
