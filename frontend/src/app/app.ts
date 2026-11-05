import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from '@core/services/theme.service';
import { SeoService } from '@core/services/seo.service';
import { ToastContainerComponent } from '@shared/components/toast-container.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ToastContainerComponent],
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './app.html',
})
export class App {
  private themeService = inject(ThemeService);

  constructor() {
    this.themeService.init();
    inject(SeoService).start();
  }
}
