import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { map } from 'rxjs';
import { ApiRes } from '@core/models/common.model';

export const apiResInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    map((event) => {
      if (event instanceof HttpResponse && event.body && typeof event.body === 'object' && 'code' in event.body && 'data' in event.body) {
        return event.clone({ body: (event.body as ApiRes<unknown>).data });
      }
      return event;
    }),
  );
};
