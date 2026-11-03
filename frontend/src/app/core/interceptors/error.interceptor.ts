import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { ToastService } from '@core/services/toast.service';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const toast = inject(ToastService);

  return next(req).pipe(
    catchError((error) => {
      if (error.status === 500) {
        router.navigate(['/error/500']);
      } else if (error.status === 403) {
        router.navigate(['/error/403']);
      } else if (error.status === 401) {
        // auth.interceptor lo silent refresh; không toast để tránh báo lỗi oan
      } else if (error.error?.message) {
        toast.error(error.error.message);
      }
      return throwError(() => error);
    }),
  );
};
