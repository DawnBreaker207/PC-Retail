import { ApplicationConfig, LOCALE_ID, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { registerLocaleData } from '@angular/common';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideClientHydration, withHttpTransferCacheOptions } from '@angular/platform-browser';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideNzI18n, vi_VN } from 'ng-zorro-antd/i18n';
import { provideNzConfig } from 'ng-zorro-antd/core/config';
import vi from '@angular/common/locales/vi';
import { routes } from './app.routes';
import { authInterceptor } from '@core/interceptors/auth.interceptor';
import { errorInterceptor } from '@core/interceptors/error.interceptor';
import { mockInterceptor } from '@core/interceptors/mock.interceptor';
import { apiResInterceptor } from '@core/interceptors/api-res.interceptor';

registerLocaleData(vi);

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    // Reuse SSR HTML + cache GET responses (no refetch after hydration).
    provideClientHydration(withHttpTransferCacheOptions({})),
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor, mockInterceptor, apiResInterceptor])),
    provideAnimations(),
    provideNzI18n(vi_VN),
    provideNzConfig({
      theme: {
        primaryColor: '#e53935',
      },
    }),
    { provide: LOCALE_ID, useValue: 'vi' },
  ],
};
