import { Component } from '@angular/core';
import { SidebarNavComponent } from './sidebar-nav.component';
import { PromoBannerGridComponent } from './promo-banner-grid.component';
import { PromoCardComponent } from './promo-card.component';
import { CategoryStripComponent } from './category-strip.component';
@Component({
  selector: 'app-gear-vn-banner',
  standalone: true,
  imports: [
    SidebarNavComponent,
    PromoBannerGridComponent,
    PromoCardComponent,
    CategoryStripComponent,
  ],
  template: `
    <div class="max-w-screen-2xl mx-auto px-4 py-6 lg:py-8">
      <div class="flex gap-6">
        <aside class="hidden lg:block">
          <app-sidebar-nav />
        </aside>

        <div class="flex-1 min-w-0 space-y-6 lg:space-y-8">
          <app-promo-banner-grid />

          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <app-promo-card title="Build PC Cao Cấp" image="https://dummyimage.com/400x300/7c3aed/white?text=Build+PC+i9" link="/products?category=pc" bgColor="linear-gradient(135deg, var(--color-purple), #5b21b6)" badgeText="INTEL" priceText="Intel Core Ultra 9" class="h-[220px]" />
            <app-promo-card title="Bàn Phím Cơ Gaming" image="https://dummyimage.com/400x300/0891b2/white?text=Ban+Phim+Co" link="/products?category=ban-phim" bgColor="linear-gradient(135deg, var(--color-cyan), #155e75)" badgeText="CƠ" priceText="Giảm đến 35%" class="h-[220px]" />
            <app-promo-card title="Chuột Không Dây" image="https://dummyimage.com/400x300/d97706/white?text=Chuot+Game" link="/products?category=chuot" bgColor="linear-gradient(135deg, var(--color-warning), #92400e)" badgeText="LOGITECH" priceText="Giảm đến 25%" class="h-[220px]" />
          </div>

          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <app-promo-card title="Laptop Gaming" image="https://dummyimage.com/400x300/dc2626/white?text=Laptop+Gaming" link="/products?category=laptop-gaming" bgColor="linear-gradient(135deg, var(--color-error), #991b1b)" badgeText="GAMING" priceText="RTX 50 Series" class="h-[200px]" />
            <app-promo-card title="Laptop Văn Phòng" image="https://dummyimage.com/400x300/2563eb/white?text=Laptop+Office" link="/products?category=laptop" bgColor="linear-gradient(135deg, var(--color-info), #1e40af)" badgeText="OFFICE" priceText="Intel Core Ultra" class="h-[200px]" />
            <app-promo-card title="PC Đồ Họa" image="https://dummyimage.com/400x300/059669/white?text=PC+Do+Hoa" link="/products?category=pc" bgColor="linear-gradient(135deg, var(--color-green), #065f46)" badgeText="WORKSTATION" priceText="RTX 5090" class="h-[200px]" />
          </div>

          <app-category-strip />
        </div>
      </div>
    </div>
  `,
})
export class GearVnBannerComponent {}
