import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { of, Observable, delay, map, catchError, switchMap } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { Sku, Review, ResponsePage, BlogPost, PromoBanner, HomeSection, SpecChip } from '@core/models/common.model';
import { PRODUCT_TEMPLATES, ProductTemplate } from '@core/mocks/product-template.mock';
import { MOCK_CATEGORIES, MOCK_BRANDS } from '@core/mocks/category.mock';
import { MOCK_FLASH_SALE_CAMPAIGN } from '@core/mocks/flash-sale.mock';

const CATEGORY_IMAGES: Record<string, string[]> = {
  'laptop-gaming': [
    'https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=600&auto=format&fit=crop&q=80',
  ],
  'laptop': [
    'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=600&auto=format&fit=crop&q=80',
  ],
  'pc': [
    'https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1587202372634-32705e3bf49c?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1624705002806-5d72df19c3ad?w=600&auto=format&fit=crop&q=80',
  ],
  'man-hinh': [
    'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1551645120-d70bfe84c826?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1547082299-de196ea013d6?w=600&auto=format&fit=crop&q=80',
  ],
  'linh-kien': [
    'https://images.unsplash.com/photo-1591799264318-7e6ef8ddb7ea?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1587202372573-03c00424a1dc?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1555680202-c86f0e12f086?w=600&auto=format&fit=crop&q=80',
  ],
  'case-nguon-tan': [
    'https://images.unsplash.com/photo-1587202372634-32705e3bf49c?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=600&auto=format&fit=crop&q=80',
  ],
  'ban-phim': [
    'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1595225476474-87563907a212?w=600&auto=format&fit=crop&q=80',
  ],
  'chuot': [
    'https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=600&auto=format&fit=crop&q=80',
  ],
  'tai-nghe': [
    'https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80',
  ],
  'o-cung-ram': [
    'https://images.unsplash.com/photo-1562976540-1502c2145186?w=600&auto=format&fit=crop&q=80',
    'https://images.unsplash.com/photo-1597872200969-2b65d56bd16b?w=600&auto=format&fit=crop&q=80',
  ],
  'ghe-ban': [
    'https://images.unsplash.com/photo-1598550476439-6847785fcea6?w=600&auto=format&fit=crop&q=80',
  ],
  'phu-kien': [
    'https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=600&auto=format&fit=crop&q=80',
  ],
};

@Injectable({ providedIn: 'root' })
export class MockDataService {
  private http = inject(HttpClient);
  categories = MOCK_CATEGORIES;
  brands = MOCK_BRANDS;

  generateProducts(): Sku[] {
    return PRODUCT_TEMPLATES.map((t: ProductTemplate, i: number) => {
      const pool = CATEGORY_IMAGES[t.cat] || CATEGORY_IMAGES['linh-kien'];
      const img1 = pool[i % pool.length];
      const img2 = pool[(i + 1) % pool.length];

      return {
        id: i + 1,
        name: t.name,
        slug: t.name.toLowerCase()
          .replace(/[^a-z0-9]+/g, '-')
          .replace(/(^-|-$)/g, '')
          .substring(0, 120),
        price: t.price,
        originalPrice: t.origPrice || undefined,
        imageUrls: [img1, img2],
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
      };
    }) as Sku[];
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
      {
        tag: 'Laptop Gaming Acer',
        title: 'ACER NITRO PROPANEL SERIES',
        subtitle: 'Laptop gaming ACER Nitro ProPanel — hiệu năng vượt trội, màn hình 180Hz, giá cực tốt',
        ctaLabel: 'Khám phá ngay',
        ctaLink: '/products?category=laptop-gaming&brand=acer',
        image: 'https://images.unsplash.com/photo-1542751371-adc38448a05e?w=1600&auto=format&fit=crop&q=85',
        discountText: 'Giảm đến 30%',
      },
      {
        tag: 'PC DAWN Hi-End',
        title: 'PC DAWN ATLOS — TẶNG MÀN HÌNH GAMING',
        subtitle: 'Sở hữu PC DAWN cấu hình độc quyền, nhận ngay màn hình cong gaming 165Hz miễn phí',
        ctaLabel: 'Xem ngay',
        ctaLink: '/products?category=pc',
        image: 'https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=1600&auto=format&fit=crop&q=85',
      },
      {
        tag: 'PC BUILD INTEL',
        title: 'BUILD PC INTEL CORE ULTRA 9 + RTX 5090',
        subtitle: 'Thế hệ vi xử lý đột phá, làm mát tản chất lỏng Custom, sẵn sàng mọi game AAA 4K',
        ctaLabel: 'Build ngay',
        ctaLink: '/pc-builder/custom',
        image: 'https://images.unsplash.com/photo-1587202372634-32705e3bf49c?w=1600&auto=format&fit=crop&q=85',
      },
      {
        tag: 'Màn hình OLED',
        title: 'MÀN HÌNH GAMING CONG 240HZ 0.03MS',
        subtitle: 'Tấm nền OLED siêu thực, chuẩn màu DCI-P3 99%, giảm đến 40% trong tháng này',
        ctaLabel: 'Xem màn hình',
        ctaLink: '/products?category=man-hinh',
        image: 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=1600&auto=format&fit=crop&q=85',
        discountText: 'Giảm 40%',
      },
      {
        tag: 'Laptop Gaming Predator',
        title: 'ACER PREDATOR HELIOS NEO 16',
        subtitle: 'Acer Predator Helios Neo 16 — RTX 4070, màn hình 2.5K 240Hz sắc nét đỉnh cao',
        ctaLabel: 'Mua ngay',
        ctaLink: '/products?category=laptop-gaming&brand=acer',
        image: 'https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=1600&auto=format&fit=crop&q=85',
      },
      {
        tag: 'MSI Gaming',
        title: 'LAPTOP GAMING MSI TITAN & KATANA',
        subtitle: 'Sức mạnh từ Intel Core i9 kết hợp đồ họa RTX 50 Series thế hệ mới nhất',
        ctaLabel: 'Khám phá',
        ctaLink: '/products?category=laptop-gaming&brand=msi',
        image: 'https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=1600&auto=format&fit=crop&q=85',
      },
    ];
  }

  getPromoBanners(): PromoBanner[] {
    return [
      { id: 1, title: 'FLASH SALE - DEAL GẤP RÚT', imageUrl: 'https://images.unsplash.com/photo-1542751371-adc38448a05e?w=600&auto=format&fit=crop&q=80', link: '/products?sort=flash-sale' },
      { id: 2, title: 'Màn hình máy tính OLED 2K/4K', subtitle: 'Giảm đến 40%', imageUrl: 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=600&auto=format&fit=crop&q=80', link: '/products?category=man-hinh' },
      { id: 3, title: 'Chuột chơi game không dây Esports', subtitle: 'Giảm đến 30%', imageUrl: 'https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=600&auto=format&fit=crop&q=80', link: '/products?category=chuot' },
      { id: 4, title: 'Build PC DAWN i9 tặng Màn Gaming', subtitle: 'Giảm đến 10 triệu', imageUrl: 'https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=600&auto=format&fit=crop&q=80', link: '/products?category=pc' },
    ];
  }

  getPromoCampaigns(): PromoBanner[] {
    return [
      { id: 1, title: 'Thu cũ đổi mới trợ giá 2.5 triệu', imageUrl: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=500&auto=format&fit=crop&q=80', link: '/khuyen-mai' },
      { id: 2, title: 'Microsoft Office 365 bản quyền', subtitle: 'Giảm 30% khi mua kèm PC/Laptop', imageUrl: 'https://images.unsplash.com/photo-1544197150-b99a580bb7a8?w=500&auto=format&fit=crop&q=80', link: '/products?category=phan-mem-mang' },
      { id: 3, title: 'Màn hình máy tính gaming 240Hz', subtitle: 'Sẵn hàng tại showroom', imageUrl: 'https://images.unsplash.com/photo-1551645120-d70bfe84c826?w=500&auto=format&fit=crop&q=80', link: '/products?category=man-hinh' },
      { id: 4, title: 'Tặng Voucher 2 triệu khi mua PC DAWN', subtitle: 'Áp dụng mọi cấu hình', imageUrl: 'https://images.unsplash.com/photo-1587202372634-32705e3bf49c?w=500&auto=format&fit=crop&q=80', link: '/products?category=pc' },
    ];
  }

  getBlogPosts(): BlogPost[] {
    return [
      {
        id: 1, title: 'RTX 5060 vs RTX 4060: Có nên nâng cấp?',
        excerpt: 'So sánh hiệu năng RTX 5060 và RTX 4060 trong các tựa game phổ biến...',
        content: 'RTX 5060 mang lại mức tăng hiệu năng khoảng 15-20% so với RTX 4060 ở độ phân giải 1440p nhờ kiến trúc nhân mới và băng thông bộ nhớ được cải thiện.\n\nVới các tựa game AAA mới, RTX 5060 cho khung hình ổn định hơn đáng kể khi bật ray tracing, trong khi RTX 4060 vẫn là lựa chọn hợp lý cho người dùng chơi ở độ phân giải 1080p với ngân sách hạn chế.\n\nNếu bạn đang dùng RTX 3060 hoặc thấp hơn, RTX 5060 là lựa chọn đáng cân nhắc. Nếu đã có RTX 4060, mức chênh lệch hiệu năng chưa đủ hấp dẫn để nâng cấp ngay.',
        imageUrl: 'https://images.unsplash.com/photo-1591799264318-7e6ef8ddb7ea?w=800&auto=format&fit=crop&q=80', slug: 'rtx-5060-vs-4060', date: '28/06/2026', category: 'Đánh giá',
      },
      {
        id: 2, title: 'Hướng dẫn build PC Gaming dưới 30 triệu',
        excerpt: 'Cấu hình PC Gaming tối ưu với ngân sách 30 triệu đồng...',
        content: 'Với ngân sách 30 triệu, bạn hoàn toàn có thể sở hữu một dàn PC gaming chiến tốt hầu hết tựa game ở độ phân giải 1440p.\n\nGợi ý cấu hình: CPU Intel Core i5 hoặc AMD Ryzen 5 thế hệ mới, GPU RTX 4060/5060, 16-32GB RAM DDR5, SSD NVMe 512GB-1TB, nguồn 650W 80+ Bronze trở lên.\n\nƯu tiên phân bổ ngân sách nhiều hơn cho GPU nếu mục tiêu chính là chơi game, vì đây là linh kiện ảnh hưởng trực tiếp đến khung hình nhất.',
        imageUrl: 'https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=800&auto=format&fit=crop&q=80', slug: 'build-pc-duoi-30tr', date: '27/06/2026', category: 'Hướng dẫn',
      },
      {
        id: 3, title: 'Top Laptop Gaming đáng mua nhất 2026',
        excerpt: 'Danh sách những mẫu laptop gaming tốt nhất ở từng phân khúc...',
        content: 'Phân khúc phổ thông (dưới 25 triệu): các mẫu trang bị RTX 4050/5050 là lựa chọn cân bằng giữa hiệu năng và giá thành.\n\nPhân khúc tầm trung (25-40 triệu): RTX 4060/4070 cho trải nghiệm chơi game 1440p mượt mà, phù hợp đa số game thủ.\n\nPhân khúc cao cấp (trên 40 triệu): RTX 4080/4090 hoặc dòng RTX 50 series mới nhất, dành cho người dùng cần hiệu năng đỉnh cao và làm nội dung sáng tạo.',
        imageUrl: 'https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=800&auto=format&fit=crop&q=80', slug: 'top-laptop-gaming-2026', date: '25/06/2026', category: 'Danh sách',
      },
      {
        id: 4, title: 'Intel Core Ultra 9 285K: Review hiệu năng',
        excerpt: 'Đánh giá chi tiết về CPU Intel thế hệ mới nhất với kiến trúc Arrow Lake...',
        content: 'Intel Core Ultra 9 285K sử dụng kiến trúc Arrow Lake hoàn toàn mới, cải thiện đáng kể hiệu suất trên mỗi watt so với thế hệ Raptor Lake trước đó.\n\nTrong các bài test đa nhân, hiệu năng render và biên dịch của 285K vượt trội nhờ số nhân/luồng tăng và bộ nhớ đệm lớn hơn.\n\nVề chơi game, mức chênh lệch so với thế hệ trước không quá lớn, nhưng mức tiêu thụ điện năng và nhiệt độ được kiểm soát tốt hơn rõ rệt.',
        imageUrl: 'https://images.unsplash.com/photo-1555680202-c86f0e12f086?w=800&auto=format&fit=crop&q=80', slug: 'intel-ultra-9-285k-review', date: '22/06/2026', category: 'Đánh giá',
      },
      {
        id: 5, title: 'SSD NVMe Gen5 có thực sự đáng nâng cấp?',
        excerpt: 'Tốc độ đọc/ghi vượt trội nhưng liệu có tạo khác biệt trong thực tế?...',
        content: 'SSD NVMe Gen5 đạt tốc độ đọc/ghi lên tới 10-14GB/s, gấp đôi so với Gen4, nhưng phần lớn tác vụ hàng ngày như chơi game hay mở ứng dụng không tận dụng hết băng thông này.\n\nGen5 phát huy tác dụng rõ nhất với các tác vụ dựng phim 8K, xử lý dữ liệu lớn hoặc làm việc với file game thế hệ mới có dung lượng cực lớn.\n\nVới đa số người dùng phổ thông, SSD NVMe Gen4 vẫn là lựa chọn cân bằng chi phí-hiệu năng tốt hơn ở thời điểm hiện tại.',
        imageUrl: 'https://images.unsplash.com/photo-1597872200969-2b65d56bd16b?w=800&auto=format&fit=crop&q=80', slug: 'ssd-nvme-gen5-co-dang-nang-cap', date: '18/06/2026', category: 'Đánh giá',
      },
      {
        id: 6, title: 'Cách chọn màn hình chơi game phù hợp',
        excerpt: 'Tần số quét, độ phân giải hay tấm nền — nên ưu tiên thông số nào?...',
        content: 'Với game thủ eSports (FPS, MOBA), tần số quét cao (165Hz-240Hz) và thời gian phản hồi thấp quan trọng hơn độ phân giải.\n\nVới game thủ ưa trải nghiệm đồ họa (AAA, nhập vai), độ phân giải 1440p/4K và tấm nền OLED/IPS cho màu sắc trung thực sẽ mang lại trải nghiệm tốt hơn.\n\nNgoài ra cần cân nhắc khả năng đáp ứng của GPU hiện có — màn hình tần số quét cao chỉ phát huy hiệu quả khi GPU đủ mạnh để đạt khung hình tương ứng.',
        imageUrl: 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=800&auto=format&fit=crop&q=80', slug: 'cach-chon-man-hinh-choi-game', date: '15/06/2026', category: 'Hướng dẫn',
      },
    ];
  }

  getCategories(): Observable<typeof MOCK_CATEGORIES> {
    return this.http.get<any>(ApiEndpoints.catalog.categories).pipe(
      map(res => {
        const list = res?.data ?? (Array.isArray(res) ? res : []);
        if (list && list.length > 0) {
          const mapped = list.map((c: any) => ({
            id: c.id,
            name: c.name,
            slug: c.slug,
            icon: 'laptop',
            productCount: 10,
            children: c.children || [],
          }));
          this.categories = mapped as any;
          return mapped as any;
        }
        return this.categories;
      }),
      catchError(() => of(this.categories))
    );
  }

  getBrands(): Observable<typeof MOCK_BRANDS> {
    return this.http.get<any>(ApiEndpoints.catalog.brands).pipe(
      map(res => {
        const list = res?.data ?? (Array.isArray(res) ? res : []);
        if (list && list.length > 0) {
          const mapped = list.map((b: any) => ({
            id: b.id,
            name: b.name,
            slug: b.name.toLowerCase().replace(/[^a-z0-9]+/g, '-'),
            logo: b.logoUrl || '',
            productCount: 5,
          }));
          this.brands = mapped as any;
          return mapped as any;
        }
        return this.brands;
      }),
      catchError(() => of(this.brands))
    );
  }

  getProducts(params?: Record<string, string>, specFilters?: Record<string, string[]>): Observable<ResponsePage<Sku>> {
    let httpParams = new HttpParams();
    if (params) {
      for (const [k, v] of Object.entries(params)) {
        if (v) httpParams = httpParams.set(k, v);
      }
    }
    return this.http.get<any>(ApiEndpoints.catalog.skus, { params: httpParams }).pipe(
      map(res => {
        const rawList = res?.data?.content || res?.content || (Array.isArray(res?.data) ? res.data : (Array.isArray(res) ? res : []));
        if (rawList.length > 0) {
          const mapped: Sku[] = rawList.map((raw: any) => ({
            id: raw.id,
            name: raw.name,
            slug: raw.slug || (raw.name ? raw.name.toLowerCase().replace(/[^a-z0-9]+/g, '-') : String(raw.id)),
            price: Number(raw.price || 0),
            originalPrice: raw.originalPrice ? Number(raw.originalPrice) : undefined,
            imageUrls: raw.imageUrls && raw.imageUrls.length > 0 ? raw.imageUrls : [raw.primaryImageUrl || 'https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=600&auto=format&fit=crop&q=80'],
            brandName: raw.brandName || 'ASUS',
            status: raw.status || 'ACTIVE',
            soldCount: raw.soldCount || 0,
            categoryName: raw.categoryName || 'pc',
            specs: raw.specs || {},
            specChips: raw.specChips || [],
            description: raw.description,
            stockAvailable: raw.stockAvailable ?? 10,
            rating: raw.rating ?? 5.0,
            reviewCount: raw.reviewCount ?? 1,
          }));
          const totalElements = res?.data?.totalElements ?? res?.totalElements ?? mapped.length;
          const pageSize = res?.data?.size ?? res?.size ?? mapped.length;
          const pageNumber = res?.data?.number ?? res?.number ?? 1;
          return {
            content: mapped,
            pagination: { pageNumber, pageSize, totalElements }
          };
        }
        return this.getMockProducts(params, specFilters);
      }),
      catchError(() => of(this.getMockProducts(params, specFilters)))
    );
  }

  private getMockProducts(params?: Record<string, string>, specFilters?: Record<string, string[]>): ResponsePage<Sku> {
    let filtered = this.generateProducts();
    if (params) {
      if (params['category']) filtered = filtered.filter(p => p.categoryName === params['category']);
      if (params['brand']) filtered = filtered.filter(p => p.brandName.toLowerCase() === params['brand'].toLowerCase());
      if (params['q']) filtered = filtered.filter(p => p.name.toLowerCase().includes(params['q']!.toLowerCase()));
      if (params['minPrice']) filtered = filtered.filter(p => p.price >= Number(params['minPrice']));
      if (params['maxPrice']) filtered = filtered.filter(p => p.price <= Number(params['maxPrice']));
      if (specFilters) {
        for (const [label, values] of Object.entries(specFilters)) {
          if (values.length > 0) filtered = filtered.filter(p => p.specs?.[label] && values.includes(p.specs[label]));
        }
      }
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
    return { content: paged, pagination: { pageNumber: page, pageSize: size, totalElements: filtered.length } };
  }

  private mapRawToSku(raw: any, fallbackSlug: string): Sku {
    return {
      id: raw.id,
      name: raw.name,
      slug: raw.slug || fallbackSlug,
      price: Number(raw.price || 0),
      originalPrice: raw.originalPrice ? Number(raw.originalPrice) : undefined,
      imageUrls: raw.imageUrls && raw.imageUrls.length > 0 ? raw.imageUrls : [raw.primaryImageUrl || 'https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=600&auto=format&fit=crop&q=80'],
      brandName: raw.brandName || 'ASUS',
      status: raw.status || 'ACTIVE',
      soldCount: raw.soldCount || 0,
      categoryName: raw.categoryName || 'pc',
      specs: raw.specs || {},
      specChips: raw.specChips || [],
      description: raw.description || `${raw.name} — Sản phẩm chính hãng.`,
      stockAvailable: raw.stockAvailable ?? 10,
      rating: raw.rating ?? 5.0,
      reviewCount: raw.reviewCount ?? 1,
    };
  }

  getProduct(slug: string): Observable<Sku | null> {
    const isNum = /^\d+$/.test(slug);
    if (isNum) {
      return this.http.get<any>(ApiEndpoints.catalog.skuDetail(Number(slug))).pipe(
        map(res => {
          const raw = res?.data ?? res;
          if (!raw || !raw.id) return null;
          return this.mapRawToSku(raw, slug);
        }),
        catchError(() => of(this.generateProducts().find(p => String(p.id) === slug) || null))
      );
    }
    return this.getProducts({ size: '100' }).pipe(
      map(res => res.content.find(p => p.slug === slug || String(p.id) === slug)),
      switchMap(found => {
        if (!found) {
          const fallback = this.generateProducts().find(p => p.slug === slug || String(p.id) === slug);
          return of(fallback || null);
        }
        return this.http.get<any>(ApiEndpoints.catalog.skuDetail(found.id)).pipe(
          map(res => {
            const raw = res?.data ?? res;
            return raw && raw.id ? this.mapRawToSku(raw, slug) : found;
          }),
          catchError(() => of(found))
        );
      }),
      catchError(() => of(this.generateProducts().find(p => p.slug === slug || String(p.id) === slug) || null))
    );
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

  getSpecFilterOptions(category?: string): { label: string; values: string[] }[] {
    const products = this.generateProducts().filter(p => !category || p.categoryName === category);
    const byLabel = new Map<string, Set<string>>();
    for (const p of products) {
      for (const [label, value] of Object.entries(p.specs || {})) {
        if (!byLabel.has(label)) byLabel.set(label, new Set());
        byLabel.get(label)!.add(value);
      }
    }
    return Array.from(byLabel.entries())
      .filter(([, values]) => values.size > 1)
      .map(([label, values]) => ({ label, values: Array.from(values) }));
  }

  getProductsByCategory(category: string, limit = 8): Sku[] {
    return this.generateProducts().filter(p => p.categoryName === category).slice(0, limit);
  }

  getSectionProducts(subNavSlug?: string, limit = 10): Sku[] {
    const all = this.generateProducts();
    if (!subNavSlug) return all.slice(0, limit);

    const byCpuTier = (tier: string, category: string) =>
      all.filter(p => p.categoryName === category && (p.specs?.['CPU']?.toLowerCase().includes(tier) ?? false)).slice(0, limit);

    const byBrand = (brand: string, category: string) =>
      all.filter(p => p.categoryName === category && p.brandName.toUpperCase() === brand.toUpperCase()).slice(0, limit);

    switch (subNavSlug) {
      case 'pc-i3': return byCpuTier('i3', 'pc');
      case 'pc-i5': return byCpuTier('i5', 'pc');
      case 'pc-i7': return byCpuTier('i7', 'pc');
      case 'pc-i9': return byCpuTier('i9', 'pc');
      case 'pc-r3': return byCpuTier('r3', 'pc');
      case 'pc-r5': return byCpuTier('r5', 'pc');
      case 'pc-r7': return byCpuTier('r7', 'pc');
      case 'pc-r9': return byCpuTier('r9', 'pc');
      case 'asus-rog': return byBrand('ASUS', 'laptop-gaming');
      case 'acer-predator': return byBrand('ACER', 'laptop-gaming');
      case 'msi-gaming': return byBrand('MSI', 'laptop-gaming');
      case 'lenovo-legion': return byBrand('LENOVO', 'laptop-gaming');
      case 'gigabyte': return byBrand('GIGABYTE', 'laptop-gaming');
      case 'dell-gaming': return byBrand('DELL', 'laptop-gaming');
      case 'laptop-asus': return byBrand('ASUS', 'laptop');
      case 'laptop-acer': return byBrand('ACER', 'laptop');
      case 'laptop-msi': return byBrand('MSI', 'laptop');
      case 'laptop-dell': return byBrand('DELL', 'laptop');
      case 'laptop-hp': return byBrand('HP', 'laptop');
      case 'laptop-lenovo': return byBrand('LENOVO', 'laptop');
      default: return all.slice(0, limit);
    }
  }

  getOrders(): Observable<{ content: Order[] }> {
    return of({ content: [] }).pipe(delay(100));
  }
}

interface Order {
  id: number; createdAt: string; totalAmount: number; paymentStatus: string;
  subOrders: { id: number; status: string; fulfillmentType: string; subtotal: number; shippingFee: number; discountAmount: number; finalAmount: number; items: { skuId: number; name: string; imageUrl: string; quantity: number; unitPrice: number; id: number }[] }[];
}
