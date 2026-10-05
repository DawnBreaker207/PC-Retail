import { Component, computed, inject, input, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MockDataService } from '@core/services/mock-data.service';
import { ProductCardComponent } from '@shared/components/product-card.component';
import { CartStore } from '@features/cart/store/cart.store';
import { HomeSection, Sku } from '@core/models/common.model';
import { splitLinkQuery } from '@shared/utils/link-query.util';

@Component({
  selector: 'app-product-section',
  imports: [RouterLink, ProductCardComponent],
  templateUrl: './product-section.component.html',
  styleUrl: './product-section.component.css',
})
export class ProductSectionComponent implements OnInit {
  private mock = inject(MockDataService);
  readonly cartStore = inject(CartStore);

  section = input.required<HomeSection>();

  activeSubNav = signal('');
  filteredProducts = signal<Sku[]>([]);

  linkPath = computed(() => splitLinkQuery(this.section().link).path);
  linkQueryParams = computed(() => splitLinkQuery(this.section().link).queryParams);

  private sectionCategory(): string {
    const m = this.section().link.match(/[?&]category=([^&]+)/);
    return m ? decodeURIComponent(m[1]) : '';
  }

  ngOnInit() {
    const cat = this.sectionCategory();
    if (cat) {
      this.filteredProducts.set(this.mock.getProductsByCategory(cat));
    } else {
      this.filteredProducts.set(this.mock.getSectionProducts());
    }
  }

  filterBy(slug: string) {
    this.activeSubNav.set(slug);
    this.filteredProducts.set(this.mock.getSectionProducts(slug));
  }
}
