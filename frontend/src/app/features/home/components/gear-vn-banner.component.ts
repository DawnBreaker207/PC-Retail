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
  templateUrl: './gear-vn-banner.component.html',
  styleUrl: './gear-vn-banner.component.css',
})
export class GearVnBannerComponent {}
