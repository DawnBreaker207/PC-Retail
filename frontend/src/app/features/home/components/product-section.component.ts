import {
  Component,
  computed,
  inject,
  input,
  signal,
  OnInit,
  ChangeDetectionStrategy,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, ChevronRight, Sparkles, Flame, ArrowRight } from 'lucide-angular';
import { MockDataService } from '@core/services/mock-data.service';
import { ProductCardComponent } from '@shared/components/product-card.component';
import { CartStore } from '@features/cart/store/cart.store';
import { HomeSection, Sku } from '@core/models/common.model';
import { splitLinkQuery } from '@shared/utils/link-query.util';

@Component({
  selector: 'app-product-section',
  imports: [RouterLink, ProductCardComponent, LucideAngularModule],
  templateUrl: './product-section.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './product-section.component.css',
})
export class ProductSectionComponent implements OnInit {
  readonly ChevronRight = ChevronRight;
  readonly Sparkles = Sparkles;
  readonly Flame = Flame;
  readonly ArrowRight = ArrowRight;

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
    this.resetFilter();
  }

  resetFilter() {
    this.activeSubNav.set('');
    const cat = this.sectionCategory();
    if (cat) {
      this.filteredProducts.set(this.mock.getProductsByCategory(cat));
    } else {
      this.filteredProducts.set(this.mock.getSectionProducts());
    }
  }

  filterBy(slug: string) {
    if (this.activeSubNav() === slug) {
      this.resetFilter();
      return;
    }
    this.activeSubNav.set(slug);
    const res = this.mock.getSectionProducts(slug);
    if (res.length > 0) {
      this.filteredProducts.set(res);
    } else {
      const cat = this.sectionCategory();
      this.filteredProducts.set(
        cat ? this.mock.getProductsByCategory(cat) : this.mock.getSectionProducts(),
      );
    }
  }
}
