import { HttpContextToken, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '@core/auth/auth.service';
import { catchError, switchMap, throwError } from 'rxjs';

/**
 * Đánh dấu request best-effort: lỗi 401 KHÔNG kích hoạt refresh/logout toàn cục.
 * Dùng cho các call phụ (vd. gộp giỏ sau login) — caller đã tự xử lý lỗi,
 * không được để nó đá văng session vừa tạo (xóa nhầm phiên đăng nhập).
 */
export const SKIP_AUTH_LOGOUT = new HttpContextToken<boolean>(() => false);

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  if (token) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }

  return next(req).pipe(
    catchError((error) => {
      // Chỉ refresh/logout khi request CÓ gắn token (session hết hạn).
      // Khách vãng lai dính 401 ở API optional (vd. AI gợi ý) không được văng về login.
      if (error.status === 401 && !req.url.includes('/auth/') && token && !req.context.get(SKIP_AUTH_LOGOUT)) {
        return authService.refresh().pipe(
          switchMap((res) => {
            const cloned = req.clone({
              setHeaders: { Authorization: `Bearer ${res.accessToken}` },
            });
            return next(cloned);
          }),
          catchError(() => {
            authService.logout();
            return throwError(() => error);
          }),
        );
      }
      return throwError(() => error);
    }),
  );
};
