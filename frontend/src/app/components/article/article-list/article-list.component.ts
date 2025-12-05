import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Article } from '../../../models/article.model';
import { ArticleService } from '../../../services/article.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-article-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="article-preview" *ngFor="let article of articles">
      <div class="article-meta">
        <a [routerLink]="['/profile', article.author.username]">
          <img [src]="article.author.image || 'https://api.realworld.io/images/smiley-cyrus.jpeg'" />
        </a>
        <div class="info">
          <a [routerLink]="['/profile', article.author.username]" class="author">
            {{ article.author.username }}
          </a>
          <span class="date">{{ article.createdAt | date:'mediumDate' }}</span>
        </div>
        <button 
          class="btn btn-sm pull-xs-right"
          [class.btn-primary]="article.favorited"
          [class.btn-outline-primary]="!article.favorited"
          (click)="toggleFavorite(article)"
          *ngIf="authService.isAuthenticated()">
          <i class="ion-heart"></i> {{ article.favoritesCount }}
        </button>
      </div>
      <a [routerLink]="['/article', article.slug]" class="preview-link">
        <h1>{{ article.title }}</h1>
        <p>{{ article.description }}</p>
        <span>Read more...</span>
        <ul class="tag-list">
          <li class="tag-default tag-pill tag-outline" *ngFor="let tag of article.tagList">
            {{ tag }}
          </li>
        </ul>
      </a>
    </div>
    <div class="article-preview" *ngIf="!loading && articles.length === 0">
      No articles are here... yet.
    </div>
    <div class="article-preview" *ngIf="loading">
      Loading articles...
    </div>
  `,
  styles: [`
    .article-preview { border-top: 1px solid #e5e5e5; padding: 1.5rem 0; }
    .article-meta { display: flex; align-items: center; margin-bottom: 1rem; }
    .article-meta img { width: 32px; height: 32px; border-radius: 50%; margin-right: 0.5rem; }
    .info { display: flex; flex-direction: column; margin-right: auto; }
    .author { color: #5cb85c; font-weight: 500; }
    .date { color: #bbb; font-size: 0.8rem; }
    .preview-link { color: inherit; text-decoration: none; }
    .preview-link h1 { font-size: 1.5rem; margin-bottom: 0.25rem; }
    .preview-link p { color: #999; font-size: 1rem; }
    .tag-list { display: flex; flex-wrap: wrap; gap: 0.25rem; list-style: none; padding: 0; margin-top: 0.5rem; }
    .tag-pill { padding: 0.1rem 0.6rem; border: 1px solid #ddd; border-radius: 10rem; font-size: 0.8rem; color: #aaa; }
    .pull-xs-right { float: right; }
  `]
})
export class ArticleListComponent implements OnInit {
  @Input() tag?: string;
  @Input() author?: string;
  @Input() favorited?: string;
  @Input() feed = false;

  articles: Article[] = [];
  loading = true;

  constructor(
    private articleService: ArticleService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadArticles();
  }

  loadArticles(): void {
    this.loading = true;
    
    if (this.feed) {
      this.articleService.getFeed().subscribe({
        next: (response) => {
          this.articles = response.articles;
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
    } else {
      this.articleService.getArticles({
        tag: this.tag,
        author: this.author,
        favorited: this.favorited
      }).subscribe({
        next: (response) => {
          this.articles = response.articles;
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
    }
  }

  toggleFavorite(article: Article): void {
    if (article.favorited) {
      this.articleService.unfavoriteArticle(article.slug).subscribe({
        next: (response) => {
          article.favorited = response.article.favorited;
          article.favoritesCount = response.article.favoritesCount;
        }
      });
    } else {
      this.articleService.favoriteArticle(article.slug).subscribe({
        next: (response) => {
          article.favorited = response.article.favorited;
          article.favoritesCount = response.article.favoritesCount;
        }
      });
    }
  }
}
