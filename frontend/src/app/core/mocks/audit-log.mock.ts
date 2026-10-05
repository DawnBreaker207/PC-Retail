import { AuditLogEntry } from '@core/models/common.model';

export const MOCK_AUDIT_LOGS: AuditLogEntry[] = [
  {
    id: 1, action: 'ORDER_TRANSITION', entityType: 'SubOrder', entityId: 2001, actorName: 'Admin DAWN', actorRole: 'ADMIN',
    outcome: 'SUCCESS', createdAt: new Date(Date.now() - 86400000 * 1).toISOString(),
    oldValue: { status: 'PAID' }, newValue: { status: 'PROCESSING' },
  },
  {
    id: 2, action: 'INVENTORY_RESTOCK', entityType: 'Inventory', entityId: 13, actorName: 'Nguyễn Văn Staff', actorRole: 'STAFF',
    outcome: 'SUCCESS', createdAt: new Date(Date.now() - 86400000 * 1.2).toISOString(),
    oldValue: { stock: 12 }, newValue: { stock: 32 },
  },
  {
    id: 3, action: 'VOUCHER_DELETE', entityType: 'Voucher', entityId: 4, actorName: 'Admin DAWN', actorRole: 'ADMIN',
    outcome: 'SUCCESS', createdAt: new Date(Date.now() - 86400000 * 1.5).toISOString(),
    oldValue: { code: 'SUMMER10', remainingQuantity: 20 }, newValue: undefined,
  },
  {
    id: 4, action: 'REFUND_APPROVE', entityType: 'ReturnRequest', entityId: 3, actorName: 'Admin DAWN', actorRole: 'ADMIN',
    outcome: 'SUCCESS', createdAt: new Date(Date.now() - 86400000 * 5).toISOString(),
    oldValue: { status: 'PENDING' }, newValue: { status: 'APPROVED' },
  },
  {
    id: 5, action: 'REFUND_REJECT', entityType: 'ReturnRequest', entityId: 4, actorName: 'Admin DAWN', actorRole: 'ADMIN',
    outcome: 'SUCCESS', createdAt: new Date(Date.now() - 86400000 * 8).toISOString(),
    oldValue: { status: 'PENDING' }, newValue: { status: 'REJECTED', rejectReason: 'Đã quá 7 ngày quy định đổi mới DOA' },
  },
  {
    id: 6, action: 'USER_STATUS_UPDATE', entityType: 'User', entityId: 7, actorName: 'Admin DAWN', actorRole: 'ADMIN',
    outcome: 'SUCCESS', createdAt: new Date(Date.now() - 86400000 * 3).toISOString(),
    oldValue: { status: 'ACTIVE' }, newValue: { status: 'INACTIVE' },
  },
  {
    id: 7, action: 'PRODUCT_UPDATE', entityType: 'Sku', entityId: 1, actorName: 'Admin DAWN', actorRole: 'ADMIN',
    outcome: 'SUCCESS', createdAt: new Date(Date.now() - 86400000 * 4).toISOString(),
    oldValue: { price: 29790000 }, newValue: { price: 28790000 },
  },
  {
    id: 8, action: 'CATEGORY_CREATE', entityType: 'Category', entityId: 17, actorName: 'Admin DAWN', actorRole: 'ADMIN',
    outcome: 'SUCCESS', createdAt: new Date(Date.now() - 86400000 * 6).toISOString(),
    newValue: { name: 'Ổ cứng di động' },
  },
  {
    id: 9, action: 'REVIEW_MODERATE', entityType: 'Review', entityId: 2, actorName: 'Nguyễn Văn Staff', actorRole: 'STAFF',
    outcome: 'SUCCESS', createdAt: new Date(Date.now() - 86400000 * 2.5).toISOString(),
    oldValue: { status: 'PENDING' }, newValue: { status: 'HIDDEN' },
  },
  {
    id: 10, action: 'LOGIN', entityType: 'User', entityId: 1, actorName: 'Admin DAWN', actorRole: 'ADMIN',
    outcome: 'SUCCESS', createdAt: new Date(Date.now() - 86400000 * 0.2).toISOString(),
  },
  {
    id: 11, action: 'LOGIN', entityType: 'User', entityId: 99, actorName: 'unknown@example.com', actorRole: 'CUSTOMER',
    outcome: 'FAILURE', createdAt: new Date(Date.now() - 86400000 * 0.3).toISOString(),
  },
  {
    id: 12, action: 'VOUCHER_CREATE', entityType: 'Voucher', entityId: 9, actorName: 'Admin DAWN', actorRole: 'ADMIN',
    outcome: 'SUCCESS', createdAt: new Date(Date.now() - 86400000 * 7).toISOString(),
    newValue: { code: 'WELCOME10', discountValue: 500000 },
  },
  {
    id: 13, action: 'ORDER_CANCEL', entityType: 'SubOrder', entityId: 2003, actorName: 'Lê Văn C', actorRole: 'CUSTOMER',
    outcome: 'SUCCESS', createdAt: new Date(Date.now() - 86400000 * 10).toISOString(),
    oldValue: { status: 'PENDING_PAYMENT' }, newValue: { status: 'CANCELLED' },
  },
  {
    id: 14, action: 'INVENTORY_RESTOCK', entityType: 'Inventory', entityId: 7, actorName: 'Trần Thị Staff', actorRole: 'STAFF',
    outcome: 'FAILURE', createdAt: new Date(Date.now() - 86400000 * 0.6).toISOString(),
  },
  {
    id: 15, action: 'PRODUCT_DELETE', entityType: 'Sku', entityId: 24, actorName: 'Admin DAWN', actorRole: 'ADMIN',
    outcome: 'SUCCESS', createdAt: new Date(Date.now() - 86400000 * 12).toISOString(),
    oldValue: { name: 'Tai nghe cũ đã ngừng bán' },
  },
];
