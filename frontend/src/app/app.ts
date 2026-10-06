import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from '@core/services/theme.service';
import { SeoService } from '@core/services/seo.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
})
export class App {
  private themeService = inject(ThemeService);

  constructor() {
    this.themeService.init();
    inject(SeoService).start();
  }
}
