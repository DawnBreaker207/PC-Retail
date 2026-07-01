import { Routes } from '@angular/router';
import { ProductListComponent } from './pages/product-list.component';
import { ProductDetailComponent } from './pages/product-detail.component';

export const CATALOG_ROUTES: Routes = [
  { path: '', component: ProductListComponent },
  { path: ':slug', component: ProductDetailComponent },
];
