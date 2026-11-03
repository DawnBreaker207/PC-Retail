export interface Pagination {
  pageNumber: number;
  pageSize: number;
  totalElements: number;
}

export interface ApiRes<T> {
  code: number;
  message: string;
  data: T;
}

export interface ResponsePage<T> {
  content: T[];
  pagination: Pagination;
}

export interface User {
  id: number;
  fullName: string;
  email: string;
  phone?: string;
  systemRole: 'CUSTOMER' | 'STAFF' | 'ADMIN' | 'SUPER_ADMIN';
  status?: 'ACTIVE' | 'INACTIVE';
  createdAt?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  phone: string;
  password: string;
}

export interface CartItem {
  skuId: number;
  name: string;
  slug: string;
  imageUrl: string;
  price: number;
  quantity: number;
  stock: number;
  brandName: string;
  selected: boolean;
}

export interface ShippingAddress {
  fullName: string;
  phone: string;
  street: string;
  district: string;
  city: string;
}

export interface Address extends ShippingAddress {
  id: number;
  label?: string;
  isDefault?: boolean;
}

export interface Voucher {
  id: number;
  code: string;
  description: string;
  discount: number;
  type: 'FIXED' | 'PERCENT';
  minOrder: number;
  maxDiscount?: number;
  expiry: string;
}

export interface VoucherValidationResult {
  valid: boolean;
  code?: string;
  discount?: number;
  discountAmount?: number;
  type?: 'FIXED' | 'PERCENT';
  minOrder?: number;
  message?: string;
  errorMessage?: string;
}

export type FulfillmentType = 'DELIVERY' | 'PICKUP';

export interface TaxInfo {
  companyName: string;
  taxCode: string;
  companyAddress: string;
  invoiceEmail: string;
}

export interface SkuRatingSummary {
  skuId: number;
  averageScore: number;
  totalRatings: number;
  fiveStarCount: number;
  fourStarCount: number;
  threeStarCount: number;
  twoStarCount: number;
  oneStarCount: number;
}

export interface CheckoutRequest {
  idempotencyKey: string;
  fulfillmentType: FulfillmentType;
  shippingAddress?: ShippingAddress;
  voucherCode?: string;
  taxInfo?: TaxInfo;
  invoiceRequested?: boolean;
}

export interface CheckoutResponse {
  id: number;
  totalAmount: number;
  paymentStatus: string;
  paymentUrl?: string;
}

export interface OrderItem {
  id?: number;
  skuId: number;
  name?: string;
  skuName?: string;
  imageUrl?: string;
  primaryImageUrl?: string;
  quantity: number;
  unitPrice: number;
}

export interface SubOrder {
  id: number;
  status: string;
  fulfillmentType: string;
  subtotal: number;
  shippingFee: number;
  discountAmount: number;
  finalAmount: number;
  items: OrderItem[];
}

export interface Order {
  id: number;
  createdAt: string;
  shippingAddress?: ShippingAddress;
  totalAmount: number;
  paymentStatus: string;
  subOrders: SubOrder[];
}

export interface VnpayInitResponse {
  paymentUrl: string;
}

export interface SpecChip {
  label: string;
  value: string;
  icon: string;
}

export interface Sku {
  id: number;
  name: string;
  slug: string;
  price: number;
  originalPrice?: number;
  imageUrls: string[];
  brandName: string;
  status: string;
  soldCount: number;
  categoryName?: string;
  specs?: Record<string, string>;
  specChips?: SpecChip[];
  description?: string;
  stockAvailable?: number;
  rating?: number;
  reviewCount?: number;
}

export interface BlogPost {
  id: number;
  title: string;
  excerpt: string;
  content: string;
  imageUrl: string;
  slug: string;
  date: string;
  category: string;
}

export interface PromoBanner {
  id: number;
  title: string;
  subtitle?: string;
  imageUrl: string;
  link: string;
  tag?: string;
}

export interface HomeSection {
  title: string;
  link: string;
  subNav: { label: string; slug: string }[];
}

export interface Review {
  id: number;
  userId: number;
  fullName: string;
  rating: number;
  comment: string;
  images?: string[];
  createdAt: string;
  productName?: string;
  skuId?: number;
  status?: 'PENDING' | 'APPROVED' | 'HIDDEN';
}

export interface Category {
  id: number;
  name: string;
  slug: string;
  parentId: number | null;
  imageUrl?: string;
  icon?: string;
}

export type ReturnRequestType = 'DOA' | 'WARRANTY';
export type ReturnRequestStatus = 'PENDING' | 'REQUESTED' | 'APPROVED' | 'REJECTED' | 'REFUNDED' | 'DISPUTED';

export interface AdminReturnRequest {
  id: number;
  orderId: number;
  subOrderId: number;
  skuName: string;
  requestType: ReturnRequestType;
  reason: string;
  status: ReturnRequestStatus;
  createdByUserId: number;
  createdByName: string;
  createdAt: string;
  approvedBy?: string;
  approvedAt?: string;
  rejectReason?: string;
  disputeNote?: string;
}

export interface AuditLogEntry {
  id: number;
  action: string;
  entityType: string;
  entityId: number;
  actorName: string;
  actorRole: string;
  outcome: 'SUCCESS' | 'FAILURE';
  createdAt: string;
  oldValue?: Record<string, unknown>;
  newValue?: Record<string, unknown>;
}

export interface CreateReviewRequest {
  orderId: number;
  orderItemId?: number;
  skuId?: number;
  score?: number;
  rating?: number;
  comment: string;
  images?: string[];
  imageUrls?: string[];
}

export type BuilderSlotType =
  | 'CPU' | 'Mainboard' | 'RAM' | 'GPU' | 'PSU'
  | 'Case' | 'Storage' | 'Cooler'
  | 'Monitor' | 'Keyboard' | 'Mouse' | 'Headphone';

export interface BuilderSlot {
  slotType: BuilderSlotType;
  slotName: string;
  isRemovable: boolean;
  isRequired: boolean;
}

export interface BuilderSelection {
  slotType: BuilderSlotType;
  slotName: string;
  sku: Sku;
}

export interface BuilderConflict {
  slotType: BuilderSlotType;
  message: string;
}

export interface BuilderValidation {
  valid: boolean;
  conflicts: BuilderConflict[];
  warnings: string[];
}

export interface BuildTemplate {
  id: number;
  name: string;
  description?: string;
  tags: string[];
  totalPrice: number;
  imageUrl?: string;
  slots: Record<string, string>;
}

export interface BuildTemplateDetail {
  id: number;
  name: string;
  description?: string;
  slots: {
    slotType: BuilderSlotType;
    slotName: string;
    isRemovable: boolean;
    defaultSku?: Sku;
    allowedSkus?: Sku[];
  }[];
}
