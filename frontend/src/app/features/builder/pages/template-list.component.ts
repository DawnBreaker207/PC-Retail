import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { CurrencyFormatPipe } from '@shared/pipes/currency-format.pipe';
import { ResponsePage, BuildTemplate } from '@core/models/common.model';
import { ApiEndpoints } from '@core/api/api.endpoints';

@Component({
  selector: 'app-template-list',
  imports: [
    CommonModule, RouterLink, NzCardModule, NzButtonModule, NzTagModule,
    NzSpinModule, NzEmptyModule, CurrencyFormatPipe,
  ],
  templateUrl: './template-list.component.html',
  styleUrl: './template-list.component.css',
})
export class TemplateListComponent {
  private http = inject(HttpClient);

  templates = signal<BuildTemplate[]>([]);
  loading = signal(true);

  constructor() {
    this.loadTemplates();
  }

  private loadTemplates() {
    this.loading.set(true);
    this.http.get<ResponsePage<BuildTemplate>>(ApiEndpoints.builder.templates, {
      params: { page: '0', size: '12' },
    }).subscribe(res => {
      this.templates.set(res.content);
      this.loading.set(false);
    });
  }

  getTags(tags: string[] | undefined): string[] {
    return tags ?? [];
  }

  tagColor(tag: string): string {
    const map: Record<string, string> = {
      Gaming: 'red', 'Đồ họa': 'purple', 'Văn phòng': 'blue',
      Workstation: 'orange', 'Cao cấp': 'gold',
    };
    return map[tag] || 'default';
  }
}
