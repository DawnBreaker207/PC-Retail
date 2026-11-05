import { mergeApplicationConfig, ApplicationConfig } from '@angular/core';
import { provideServerRendering, withRoutes } from '@angular/ssr';
import { provideHttpClient, withInterceptors, withXhr } from '@angular/common/http';
import { appConfig } from './app.config';
import { serverRoutes } from './app.routes.server';
import { serverUrlInterceptor } from '@core/interceptors/server-url.interceptor';
import { authInterceptor } from '@core/interceptors/auth.interceptor';
import { errorInterceptor } from '@core/interceptors/error.interceptor';
import { mockInterceptor } from '@core/interceptors/mock.interceptor';
import { apiResInterceptor } from '@core/interceptors/api-res.interceptor';

// Server HttpClient overrides the client one (last provider wins): server-url first, then the shared chain.
const serverConfig: ApplicationConfig = {
  providers: [
    provideServerRendering(withRoutes(serverRoutes)),
    provideHttpClient(
      withXhr(),
      withInterceptors([
        serverUrlInterceptor,
        authInterceptor,
        errorInterceptor,
        mockInterceptor,
        apiResInterceptor,
      ]),
    ),
  ],
};

export const config = mergeApplicationConfig(appConfig, serverConfig);
