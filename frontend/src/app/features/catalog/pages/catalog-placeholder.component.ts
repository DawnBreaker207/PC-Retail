import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzResultModule } from 'ng-zorro-antd/result';

@Component({
  selector: 'app-catalog-placeholder',
  imports: [RouterLink, NzButtonModule, NzResultModule],
  templateUrl: './catalog-placeholder.component.html',
  styleUrl: './catalog-placeholder.component.css',
})
export class CatalogPlaceholderComponent {}
