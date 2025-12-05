import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Article } from '../../../models/article.model';
import { Comment } from '../../../models/comment.model';
import { ArticleService } from '../../../services/article.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-article-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="article-page" *ngIf="article">
      <div class="banner">
        <div class="container">
          <h1>{{ article.title }}</h1>
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
            <ng-container *ngIf="isAuthor">
              <a [routerLink]="['/editor', article.slug]" class="btn btn-sm btn-outline-secondary">
                <i class="ion-edit"></i> Edit Article
              </a>
              <button class="btn btn-sm btn-outline-danger" (click)="deleteArticle()">
                <i class="ion-trash-a"></i> Delete Article
              </button>
            </ng-container>
          </div>
        </div>
      </div>

      <div class="container page">
        <div class="row article-content">
          <div class="col-md-12">
            <p>{{ article.body }}</p>
            <ul class="tag-list">
              <li class="tag-default tag-pill tag-outline" *ngFor="let tag of article.tagList">
                {{ tag }}
              </li>
            </ul>
          </div>
        </div>

        <hr />

        <div class="row">
          <div class="col-xs-12 col-md-8 offset-md-2">
            <form class="card comment-form" *ngIf="authService.isAuthenticated()" (ngSubmit)="addComment()">
              <div class="card-block">
                <textarea
                  class="form-control"
                  placeholder="Write a comment..."
                  rows="3"
                  [(ngModel)]="commentBody"
                  name="comment"
                ></textarea>
              </div>
              <div class="card-footer">
                <img [src]="authService.currentUser()?.image || 'https://api.realworld.io/images/smiley-cyrus.jpeg'" class="comment-author-img" />
                <button class="btn btn-sm btn-primary" type="submit">Post Comment</button>
              </div>
            </form>

            <div class="card" *ngFor="let comment of comments">
              <div class="card-block">
                <p class="card-text">{{ comment.body }}</p>
              </div>
              <div class="card-footer">
                <a [routerLink]="['/profile', comment.author.username]" class="comment-author">
                  <img [src]="comment.author.image || 'https://api.realworld.io/images/smiley-cyrus.jpeg'" class="comment-author-img" />
                </a>
                &nbsp;
                <a [routerLink]="['/profile', comment.author.username]" class="comment-author">
                  {{ comment.author.username }}
                </a>
                <span class="date-posted">{{ comment.createdAt | date:'mediumDate' }}</span>
                <span class="mod-options" *ngIf="comment.author.username === authService.currentUser()?.username">
                  <i class="ion-trash-a" (click)="deleteComment(comment.id)"></i>
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .banner { background: #333; padding: 2rem 0; color: #fff; }
    .banner h1 { font-size: 2.8rem; font-weight: 600; }
    .article-meta { display: flex; align-items: center; gap: 0.5rem; }
    .article-meta img { width: 32px; height: 32px; border-radius: 50%; }
    .info { display: flex; flex-direction: column; margin-right: 1rem; }
    .author { color: #fff; }
    .date { color: #bbb; font-size: 0.8rem; }
    .article-content { margin: 2rem 0; }
    .tag-list { display: flex; flex-wrap: wrap; gap: 0.25rem; list-style: none; padding: 0; }
    .tag-pill { padding: 0.1rem 0.6rem; border: 1px solid #ddd; border-radius: 10rem; font-size: 0.8rem; color: #aaa; }
    .comment-form { margin-bottom: 1rem; }
    .card { margin-bottom: 1rem; border: 1px solid #e5e5e5; }
    .card-block { padding: 1.25rem; }
    .card-footer { padding: 0.75rem 1.25rem; background: #f5f5f5; display: flex; align-items: center; }
    .comment-author-img { width: 20px; height: 20px; border-radius: 50%; margin-right: 0.5rem; }
    .comment-author { color: #5cb85c; }
    .date-posted { color: #bbb; font-size: 0.8rem; margin-left: 0.5rem; }
    .mod-options { margin-left: auto; cursor: pointer; color: #bbb; }
    .mod-options:hover { color: #333; }
  `]
})
export class ArticleDetailComponent implements OnInit {
  article: Article | null = null;
  comments: Comment[] = [];
  commentBody = '';
  isAuthor = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private articleService: ArticleService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug');
    if (slug) {
      this.loadArticle(slug);
      this.loadComments(slug);
    }
  }

  loadArticle(slug: string): void {
    this.articleService.getArticle(slug).subscribe({
      next: (response) => {
        this.article = response.article;
        this.isAuthor = this.article.author.username === this.authService.currentUser()?.username;
      }
    });
  }

  loadComments(slug: string): void {
    this.articleService.getComments(slug).subscribe({
      next: (response) => {
        this.comments = response.comments;
      }
    });
  }

  addComment(): void {
    if (!this.article || !this.commentBody.trim()) return;

    this.articleService.addComment(this.article.slug, this.commentBody).subscribe({
      next: (response) => {
        this.comments.unshift(response.comment);
        this.commentBody = '';
      }
    });
  }

  deleteComment(commentId: number): void {
    if (!this.article) return;

    this.articleService.deleteComment(this.article.slug, commentId).subscribe({
      next: () => {
        this.comments = this.comments.filter(c => c.id !== commentId);
      }
    });
  }

  deleteArticle(): void {
    if (!this.article) return;

    this.articleService.deleteArticle(this.article.slug).subscribe({
      next: () => {
        this.router.navigate(['/']);
      }
    });
  }
}
