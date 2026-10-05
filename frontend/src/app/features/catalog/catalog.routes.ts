import { Routes } from '@angular/router';
import { ProductListComponent } from './pages/product-list.component';
import { ProductDetailComponent } from './pages/product-detail.component';
import { CompareComponent } from './pages/compare.component';

export const CATALOG_ROUTES: Routes = [
  { path: '', component: ProductListComponent },
  { path: 'compare', component: CompareComponent },
  { path: ':slug', component: ProductDetailComponent },
];
