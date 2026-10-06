import { Injectable, inject } from '@angular/core';
import { Title, Meta } from '@angular/platform-browser';
import { Router, NavigationEnd, ActivatedRoute } from '@angular/router';
import { filter } from 'rxjs';

export interface SeoData {
  title?: string;
  description?: string;
}

// Syncs <title> + meta description from the deepest route with SEO data. Started once in App.
@Injectable({ providedIn: 'root' })
export class SeoService {
  private title = inject(Title);
  private meta = inject(Meta);
  private router = inject(Router);
  private root = inject(ActivatedRoute);

  private readonly site = 'DAWN PC Retail';
  private started = false;

  start() {
    if (this.started) return;
    this.started = true;
    this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd))
      .subscribe(() => this.apply(this.collect(this.root)));
  }

  private collect(route: ActivatedRoute): SeoData {
    let node: ActivatedRoute | null = route;
    let deepest: SeoData = {};
    while (node) {
      const data = node.snapshot.data as SeoData;
      if (data?.title || data?.description) deepest = data;
      node = node.firstChild;
    }
    return deepest;
  }

  private apply(data: SeoData) {
    const title = data.title ? `${data.title} | ${this.site}` : this.site;
    this.title.setTitle(title);
    if (data.description) {
      this.meta.updateTag({ name: 'description', content: data.description });
    }
  }
}
