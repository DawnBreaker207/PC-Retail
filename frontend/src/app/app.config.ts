import {
  ApplicationConfig,
  importProvidersFrom,
  LOCALE_ID,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
} from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { icons, LucideAngularModule } from 'lucide-angular';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { CurrencyFormatPipe } from './shared/pipes/currency-format.pipe';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    importProvidersFrom(
      CommonModule,
      FormsModule,
      ReactiveFormsModule,
      LucideAngularModule.pick(icons),
    ),
    CurrencyFormatPipe,
    { provide: LOCALE_ID, useValue: 'vi' },
    provideAnimationsAsync(),
  ],
};
