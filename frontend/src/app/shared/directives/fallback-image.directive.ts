import { Directive, input, HostListener } from '@angular/core';

@Directive({
  selector: 'img[appFallbackImage]',
  standalone: true,
})
export class FallbackImageDirective {
  fallback = input("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 400 300'%3E%3Crect fill='%23f1f5f9' width='400' height='300'/%3E%3Cpath fill='%23cbd5e1' d='M160 110h80v60h-80z'/%3E%3Cpath fill='%2394a3b8' d='M180 130h40v20h-40z'/%3E%3Ctext fill='%2394a3b8' font-family='sans-serif' font-size='14' x='200' y='220' text-anchor='middle'%3ENo image%3C/text%3E%3C/svg%3E");

  @HostListener('error', ['$event'])
  onError(event: Event) {
    const img = event.target as HTMLImageElement;
    if (img.src !== this.fallback()) {
      img.src = this.fallback();
    }
  }
}
