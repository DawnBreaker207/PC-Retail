import { Injectable } from '@angular/core';
import { of, Observable, delay } from 'rxjs';
import { Sku, Review, ResponsePage, BlogPost, PromoBanner, HomeSection, SpecChip } from '@core/models/common.model';
import { PRODUCT_TEMPLATES, ProductTemplate } from '@core/mocks/product-template.mock';
import { MOCK_CATEGORIES, MOCK_BRANDS } from '@core/mocks/category.mock';
import { MOCK_FLASH_SALE_CAMPAIGN } from '@core/mocks/flash-sale.mock';

@Injectable({ providedIn: 'root' })
export class MockDataService {
  private readonly baseUrl = 'https://dummyimage.com';

  categories = MOCK_CATEGORIES;
  brands = MOCK_BRANDS;

  generateProducts(): Sku[] {
    return PRODUCT_TEMPLATES.map((t: ProductTemplate, i: number) => ({
      id: i + 1,
      name: t.name,
      slug: t.name.toLowerCase()
        .replace(/[^a-z0-9]+/g, '-')
        .replace(/(^-|-$)/g, '')
        .substring(0, 120),
      price: t.price,
      originalPrice: t.origPrice || undefined,
      imageUrls: [
        `${this.baseUrl}/400x400/1e293b/white?text=${encodeURIComponent(t.name.substring(0, 20))}`,
        `${this.baseUrl}/400x400/e53935/white?text=${encodeURIComponent(t.name.substring(0, 20))}`,
      ],
      brandName: t.brand,
      status: 'ACTIVE',
      soldCount: t.sold,
      rating: t.rating,
      reviewCount: t.reviews,
      specChips: t.specChips as SpecChip[],
      specs: t.specChips.reduce((acc: Record<string, string>, chip) => ({ ...acc, [chip.label]: chip.value }), {}),
      description: `${t.name} — Sản phẩm cao cấp chính hãng. Được kiểm tra kỹ lưỡng trước khi bán. Bảo hành 24 tháng. Hỗ trợ đổi trả trong 30 ngày. Miễn phí giao hàng toàn quốc.`,
      stockAvailable: Math.floor(Math.random() * 50) + 5,
      categoryName: t.cat,
    })) as Sku[];
  }

  getFlashSale() {
    return MOCK_FLASH_SALE_CAMPAIGN;
  }

  getHomeSections(): HomeSection[] {
    return [
      {
        title: 'PC bán chạy',
        link: '/products?category=pc',
        subNav: [
          { label: 'PC I3', slug: 'pc-i3' }, { label: 'PC I5', slug: 'pc-i5' },
          { label: 'PC I7', slug: 'pc-i7' }, { label: 'PC I9', slug: 'pc-i9' },
          { label: 'PC R3', slug: 'pc-r3' }, { label: 'PC R5', slug: 'pc-r5' },
          { label: 'PC R7', slug: 'pc-r7' }, { label: 'PC R9', slug: 'pc-r9' },
        ],
      },
      {
        title: 'Laptop gaming bán chạy',
        link: '/products?category=laptop-gaming',
        subNav: [
          { label: 'ASUS', slug: 'asus-rog' }, { label: 'ACER', slug: 'acer-predator' },
          { label: 'MSI', slug: 'msi-gaming' }, { label: 'LENOVO', slug: 'lenovo-legion' },
          { label: 'GIGABYTE', slug: 'gigabyte' }, { label: 'DELL', slug: 'dell-gaming' },
        ],
      },
      {
        title: 'Laptop văn phòng bán chạy',
        link: '/products?category=laptop',
        subNav: [
          { label: 'ASUS', slug: 'laptop-asus' }, { label: 'ACER', slug: 'laptop-acer' },
          { label: 'MSI', slug: 'laptop-msi' }, { label: 'DELL', slug: 'laptop-dell' },
          { label: 'HP', slug: 'laptop-hp' }, { label: 'LENOVO', slug: 'laptop-lenovo' },
        ],
      },
    ];
  }

  getHeroSlides() {
    return [
      { tag: 'Laptop Gaming Acer', title: 'ACER NITRO PROPANEL SERIES', subtitle: 'Laptop gaming ACER Nitro ProPanel — hiệu năng vượt trội, màn hình 180Hz, giá cực tốt', ctaLabel: 'Khám phá ngay', ctaLink: '/products?category=laptop-gaming&brand=acer', image: `${this.baseUrl}/1200x400/1e293b/e53935?text=ACER+Nitro+ProPanel`, discountText: 'Giảm đến 30%' },
      { tag: 'PC GVN', title: 'PC ACER ATLOS — Tặng màn hình', subtitle: 'Sở hữu PC ACER ATLOS chính hãng, nhận ngay màn hình miễn phí', ctaLabel: 'Xem ngay', ctaLink: '/products?category=pc&brand=acer', image: `${this.baseUrl}/1200x400/0f172a/e53935?text=PC+Acer+Atlos` },
      { tag: 'PC BUILD', title: 'BUILD PC INTEL CORE ULTRA 9', subtitle: 'PC Intel thế hệ mới với RTX 5090 — hiệu năng đỉnh cao cho gaming và đồ họa', ctaLabel: 'Build ngay', ctaLink: '/products?category=pc', image: `${this.baseUrl}/1200x400/1e293b/ffffff?text=Build+PC+i9` },
      { tag: 'Màn hình', title: 'MÀN HÌNH MÁY TÍNH THÁNG 5/26', subtitle: 'Màn hình từ 24" đến 34" — giảm đến 40%, nhiều ưu đãi đặc biệt', ctaLabel: 'Xem màn hình', ctaLink: '/products?category=man-hinh', image: `${this.baseUrl}/1200x400/0f172a/2563eb?text=Man+hinh+thang+5`, discountText: 'Giảm 40%' },
      { tag: 'Laptop Gaming', title: 'LAPTOP GAMING ACER PREDATOR', subtitle: 'Acer Predator Helios Neo 16 — RTX 4060, màn hình 2.5K 240Hz', ctaLabel: 'Mua ngay', ctaLink: '/products?category=laptop-gaming&brand=acer', image: `${this.baseUrl}/1200x400/1e293b/e53935?text=Acer+Predator` },
      { tag: 'Laptop Gaming', title: 'LAPTOP GAMING MSI', subtitle: 'Laptop MSI Gaming - Sức mạnh từ Intel Core i9 kết hợp RTX 50 Series', ctaLabel: 'Khám phá', ctaLink: '/products?category=laptop-gaming&brand=msi', image: `${this.baseUrl}/1200x400/0f172a/e53935?text=MSI+Gaming` },
      { tag: 'Laptop Gaming', title: 'LAPTOP GAMING GIGABYTE', subtitle: 'Gigabyte A16 Series — RTX 50 Series, màn hình 165Hz, tản nhiệt WINDFORCE', ctaLabel: 'Xem thêm', ctaLink: '/products?category=laptop-gaming&brand=gigabyte', image: `${this.baseUrl}/1200x400/1e293b/ffffff?text=Gigabyte+Gaming` },
      { tag: 'Laptop Gaming', title: 'LAPTOP GAMING LENOVO LEGION', subtitle: 'Lenovo Legion Pro 5 — Intel Gen 13, RTX 4060, màn hình 2.5K 240Hz', ctaLabel: 'Xem ngay', ctaLink: '/products?category=laptop-gaming&brand=lenovo', image: `${this.baseUrl}/1200x400/0f172a/e53935?text=Lenovo+Legion` },
    ];
  }

  getPromoBanners(): PromoBanner[] {
    return [
      { id: 1, title: 'FLASH SALE - DEAL GẤP RÚT', imageUrl: `${this.baseUrl}/340x200/e53935/white?text=FLASH+SALE`, link: '/products?sort=flash-sale' },
      { id: 2, title: 'Màn hình máy tính tháng 5/26', subtitle: 'Giảm đến 40%', imageUrl: `${this.baseUrl}/340x200/2563eb/white?text=Man+hinh`, link: '/products?category=man-hinh' },
      { id: 3, title: 'Chuột chơi game tháng 5/26', subtitle: 'Giảm đến 30%', imageUrl: `${this.baseUrl}/340x200/d97706/white?text=Chuot+game`, link: '/products?category=phu-kien&brand=logitech' },
      { id: 4, title: 'Build PC GVN i9 tặng Màn OLED', subtitle: 'Giảm đến 10 triệu', imageUrl: `${this.baseUrl}/340x200/b71c1c/white?text=Build+PC+i9`, link: '/products?category=pc' },
    ];
  }

  getPromoCampaigns(): PromoBanner[] {
    return [
      { id: 1, title: 'Thu cũ đổi mới', imageUrl: `${this.baseUrl}/280x180/1e293b/white?text=Thu+cu+doi+moi`, link: '#' },
      { id: 2, title: 'Microsoft Office', subtitle: '365 giá tốt', imageUrl: `${this.baseUrl}/280x180/2563eb/white?text=Office+365`, link: '#' },
      { id: 3, title: 'Màn hình máy tính Tháng 10', subtitle: 'Giảm sốc', imageUrl: `${this.baseUrl}/280x180/e53935/white?text=Man+hinh`, link: '#' },
      { id: 4, title: 'Giảm lên đến 2 triệu đồng', subtitle: 'Khi mua PC', imageUrl: `${this.baseUrl}/280x180/d97706/white?text=Giam+2tr`, link: '#' },
    ];
  }

  getBlogPosts(): BlogPost[] {
    return [
      { id: 1, title: 'RTX 5060 vs RTX 4060: Có nên nâng cấp?', excerpt: 'So sánh hiệu năng RTX 5060 và RTX 4060 trong các tựa game phổ biến...', imageUrl: `${this.baseUrl}/280x180/1e293b/e53935?text=RTX+5060+vs+4060`, slug: 'rtx-5060-vs-4060', date: '28/06/2026', category: 'Đánh giá' },
      { id: 2, title: 'Hướng dẫn build PC Gaming dưới 30 triệu', excerpt: 'Cấu hình PC Gaming tối ưu với ngân sách 30 triệu đồng...', imageUrl: `${this.baseUrl}/280x180/0f172a/2563eb?text=Build+PC+30tr`, slug: 'build-pc-duoi-30tr', date: '27/06/2026', category: 'Hướng dẫn' },
      { id: 3, title: 'Top Laptop Gaming đáng mua nhất 2026', excerpt: 'Danh sách những mẫu laptop gaming tốt nhất ở từng phân khúc...', imageUrl: `${this.baseUrl}/280x180/1e293b/e53935?text=Top+Laptop+Gaming`, slug: 'top-laptop-gaming-2026', date: '25/06/2026', category: 'Danh sách' },
      { id: 4, title: 'Intel Core Ultra 9 285K: Review hiệu năng', excerpt: 'Đánh giá chi tiết về CPU Intel thế hệ mới nhất với kiến trúc Arrow Lake...', imageUrl: `${this.baseUrl}/280x180/0f172a/ffffff?text=Ultra+9+285K`, slug: 'intel-ultra-9-285k-review', date: '22/06/2026', category: 'Đánh giá' },
    ];
  }

  getCategories(): Observable<typeof MOCK_CATEGORIES> {
    return of(this.categories).pipe(delay(100));
  }

  getBrands(): Observable<typeof MOCK_BRANDS> {
    return of(this.brands).pipe(delay(100));
  }

  getProducts(params?: Record<string, string>): Observable<ResponsePage<Sku>> {
    let filtered = this.generateProducts();
    if (params) {
      if (params['category']) filtered = filtered.filter(p => p.categoryName === params['category']);
      if (params['brand']) filtered = filtered.filter(p => p.brandName.toLowerCase() === params['brand'].toLowerCase());
      if (params['q']) filtered = filtered.filter(p => p.name.toLowerCase().includes(params['q']!.toLowerCase()));
      if (params['minPrice']) filtered = filtered.filter(p => p.price >= Number(params['minPrice']));
      if (params['maxPrice']) filtered = filtered.filter(p => p.price <= Number(params['maxPrice']));
      if (params['sort']) {
        switch (params['sort']) {
          case 'price-asc': filtered.sort((a, b) => a.price - b.price); break;
          case 'price-desc': filtered.sort((a, b) => b.price - a.price); break;
          case 'sold': filtered.sort((a, b) => b.soldCount - a.soldCount); break;
          default: break;
        }
      }
    }
    const page = params?.['page'] ? Number(params['page']) : 0;
    const size = params?.['size'] ? Number(params['size']) : 20;
    const start = page * size;
    const paged = filtered.slice(start, start + size);
    return of({ content: paged, pagination: { pageNumber: page, pageSize: size, totalElements: filtered.length } }).pipe(delay(100));
  }

  getProduct(slug: string): Observable<Sku | null> {
    const products = this.generateProducts();
    const p = products.find(pr => pr.slug === slug) || null;
    return of(p).pipe(delay(100));
  }

  getRelatedProducts(slug: string): Observable<Sku[]> {
    const products = this.generateProducts();
    const idx = products.findIndex(p => p.slug === slug);
    const related = products.filter((_, i) => i !== idx).slice(0, 4);
    return of(related).pipe(delay(100));
  }

  getReviews(): Observable<Review[]> {
    const reviewers = ['Nguyễn Văn A', 'Trần Thị B', 'Lê Văn C', 'Phạm Thị D', 'Hoàng Văn E'];
    const comments = ['Sản phẩm rất tốt, đóng gói cẩn thận', 'Chất lượng tuyệt vời, giá hợp lý', 'Hàng chính hãng, giao nhanh', 'Máy chạy mượt, nhiệt độ thấp', 'Màn hình đẹp, màu sắc trung thực'];
    const reviews: Review[] = Array.from({ length: 5 }, (_, i) => ({
      id: i + 1,
      userId: i + 1,
      fullName: reviewers[i],
      rating: Math.floor(Math.random() * 2) + 4,
      comment: comments[i],
      createdAt: new Date(Date.now() - Math.random() * 30 * 86400000).toISOString(),
    }));
    return of(reviews).pipe(delay(100));
  }

  getProductsByCategory(category: string, limit = 8): Sku[] {
    return this.generateProducts().filter(p => p.categoryName === category).slice(0, limit);
  }

  getSectionProducts(subNavSlug?: string, limit = 10): Sku[] {
    const all = this.generateProducts();
    if (subNavSlug === 'pc-i3') return all.filter(p => p.name.toLowerCase().includes('i3')).slice(0, limit);
    if (subNavSlug === 'pc-i5') return all.filter(p => p.name.toLowerCase().includes('i5')).slice(0, limit);
    if (subNavSlug === 'pc-i7') return all.filter(p => p.name.toLowerCase().includes('i7')).slice(0, limit);
    if (subNavSlug === 'pc-i9') return all.filter(p => p.name.toLowerCase().includes('i9')).slice(0, limit);
    if (subNavSlug?.startsWith('pc-r')) return all.filter(p => p.name.toLowerCase().includes('r')).slice(0, limit);
    if (subNavSlug?.includes('asus')) return all.filter(p => p.brandName === 'ASUS').slice(0, limit);
    if (subNavSlug?.includes('acer')) return all.filter(p => p.brandName === 'ACER').slice(0, limit);
    if (subNavSlug?.includes('msi')) return all.filter(p => p.brandName === 'MSI').slice(0, limit);
    if (subNavSlug?.includes('lenovo')) return all.filter(p => p.brandName === 'LENOVO').slice(0, limit);
    if (subNavSlug?.includes('gigabyte')) return all.filter(p => p.brandName === 'GIGABYTE').slice(0, limit);
    if (subNavSlug?.includes('dell')) return all.filter(p => p.brandName === 'DELL').slice(0, limit);
    return all.slice(0, limit);
  }

  getOrders(): Observable<{ content: Order[] }> {
    return of({ content: [] }).pipe(delay(100));
  }
}

interface Order {
  id: number; createdAt: string; totalAmount: number; paymentStatus: string;
  subOrders: { id: number; status: string; fulfillmentType: string; subtotal: number; shippingFee: number; discountAmount: number; finalAmount: number; items: { skuId: number; name: string; imageUrl: string; quantity: number; unitPrice: number; id: number }[] }[];
}
