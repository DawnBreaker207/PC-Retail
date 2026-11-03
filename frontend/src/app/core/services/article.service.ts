import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of } from 'rxjs';
import { ApiEndpoints } from '@core/api/api.endpoints';
import { BlogPost } from '@core/models/common.model';
import { MockDataService } from './mock-data.service';

export interface ArticleBackend {
  id: number;
  title: string;
  slug: string;
  content: string;
  thumbnailUrl?: string;
  category?: string;
  status?: string;
  publishedAt?: string;
  createdAt?: string;
}

@Injectable({ providedIn: 'root' })
export class ArticleService {
  private http = inject(HttpClient);
  private mock = inject(MockDataService);

  getArticles(): Observable<BlogPost[]> {
    return this.http.get<ArticleBackend[]>(ApiEndpoints.articles.base).pipe(
      map((list) => {
        if (Array.isArray(list) && list.length > 0) {
          return list.map((a) => this.toBlogPost(a));
        }
        return this.mock.getBlogPosts();
      }),
      catchError(() => of(this.mock.getBlogPosts()))
    );
  }

  getArticleBySlug(slug: string): Observable<BlogPost | null> {
    return this.http.get<ArticleBackend>(ApiEndpoints.articles.detail(slug)).pipe(
      map((a) => (a ? this.toBlogPost(a) : null)),
      catchError(() => {
        const found = this.mock.getBlogPosts().find((p) => p.slug === slug);
        return of(found || null);
      })
    );
  }

  private toBlogPost(a: ArticleBackend): BlogPost {
    return {
      id: a.id,
      title: a.title,
      slug: a.slug,
      excerpt: a.content ? a.content.replace(/<[^>]*>?/gm, '').substring(0, 150) + '...' : '',
      content: a.content || '',
      imageUrl: a.thumbnailUrl || 'https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=800&auto=format&fit=crop&q=80',
      date: a.publishedAt ? new Date(a.publishedAt).toLocaleDateString('vi-VN') : new Date().toLocaleDateString('vi-VN'),
      category: a.category || 'Tin công nghệ',
    };
  }
}
