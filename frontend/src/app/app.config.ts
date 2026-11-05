import { ApplicationConfig, LOCALE_ID, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, withComponentInputBinding, withViewTransitions } from '@angular/router';
import { registerLocaleData } from '@angular/common';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import {
  provideClientHydration,
  withHttpTransferCacheOptions,
  withNoIncrementalHydration,
} from '@angular/platform-browser';
import { provideAnimations } from '@angular/platform-browser/animations';
import vi from '@angular/common/locales/vi';
import { routes } from './app.routes';
import { authInterceptor } from '@core/interceptors/auth.interceptor';
import { errorInterceptor } from '@core/interceptors/error.interceptor';
import { apiResInterceptor } from '@core/interceptors/api-res.interceptor';

registerLocaleData(vi);

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding(), withViewTransitions()),
    // Reuse SSR HTML + cache GET responses (no refetch after hydration).
    provideClientHydration(withHttpTransferCacheOptions({}), withNoIncrementalHydration()),
    provideHttpClient(
      withFetch(),
      withInterceptors([authInterceptor, errorInterceptor, apiResInterceptor]),
    ),
    provideAnimations(),
    { provide: LOCALE_ID, useValue: 'vi' },
  ],
};
