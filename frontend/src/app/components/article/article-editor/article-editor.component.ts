import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ArticleService } from '../../../services/article.service';

@Component({
  selector: 'app-article-editor',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="editor-page">
      <div class="container page">
        <div class="row">
          <div class="col-md-10 offset-md-1 col-xs-12">
            <ul class="error-messages" *ngIf="errors.length">
              <li *ngFor="let error of errors">{{ error }}</li>
            </ul>

            <form (ngSubmit)="onSubmit()">
              <fieldset>
                <fieldset class="form-group">
                  <input
                    type="text"
                    class="form-control form-control-lg"
                    placeholder="Article Title"
                    [(ngModel)]="article.title"
                    name="title"
                  />
                </fieldset>
                <fieldset class="form-group">
                  <input
                    type="text"
                    class="form-control"
                    placeholder="What's this article about?"
                    [(ngModel)]="article.description"
                    name="description"
                  />
                </fieldset>
                <fieldset class="form-group">
                  <textarea
                    class="form-control"
                    rows="8"
                    placeholder="Write your article (in markdown)"
                    [(ngModel)]="article.body"
                    name="body"
                  ></textarea>
                </fieldset>
                <fieldset class="form-group">
                  <input
                    type="text"
                    class="form-control"
                    placeholder="Enter tags (comma separated)"
                    [(ngModel)]="tagInput"
                    name="tags"
                  />
                  <div class="tag-list">
                    <span class="tag-default tag-pill" *ngFor="let tag of article.tagList; let i = index">
                      <i class="ion-close-round" (click)="removeTag(i)"></i>
                      {{ tag }}
                    </span>
                  </div>
                </fieldset>
                <button class="btn btn-lg pull-xs-right btn-primary" type="submit" [disabled]="isSubmitting">
                  {{ isEditing ? 'Update' : 'Publish' }} Article
                </button>
              </fieldset>
            </form>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .editor-page { padding-top: 2rem; }
    .error-messages { color: #b85c5c; list-style: none; padding: 0; }
    .pull-xs-right { float: right; }
    .tag-list { display: flex; flex-wrap: wrap; gap: 0.25rem; margin-top: 0.5rem; }
    .tag-pill { padding: 0.2rem 0.6rem; background: #818a91; color: #fff; border-radius: 10rem; font-size: 0.8rem; cursor: pointer; }
    .tag-pill i { margin-right: 0.25rem; }
  `]
})
export class ArticleEditorComponent implements OnInit {
  article = {
    title: '',
    description: '',
    body: '',
    tagList: [] as string[]
  };
  tagInput = '';
  errors: string[] = [];
  isSubmitting = false;
  isEditing = false;
  slug = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private articleService: ArticleService
  ) {}

  ngOnInit(): void {
    this.slug = this.route.snapshot.paramMap.get('slug') || '';
    if (this.slug) {
      this.isEditing = true;
      this.loadArticle();
    }
  }

  loadArticle(): void {
    this.articleService.getArticle(this.slug).subscribe({
      next: (response) => {
        this.article = {
          title: response.article.title,
          description: response.article.description,
          body: response.article.body,
          tagList: response.article.tagList
        };
      }
    });
  }

  onSubmit(): void {
    this.isSubmitting = true;
    this.errors = [];

    if (this.tagInput.trim()) {
      const newTags = this.tagInput.split(',').map(t => t.trim()).filter(t => t);
      this.article.tagList = [...new Set([...this.article.tagList, ...newTags])];
    }

    const request = this.isEditing
      ? this.articleService.updateArticle(this.slug, this.article)
      : this.articleService.createArticle(this.article);

    request.subscribe({
      next: (response) => {
        this.router.navigate(['/article', response.article.slug]);
      },
      error: (err) => {
        this.isSubmitting = false;
        if (err.error?.errors) {
          this.errors = Object.entries(err.error.errors)
            .map(([key, value]) => `${key} ${(value as string[]).join(', ')}`);
        } else {
          this.errors = ['An error occurred'];
        }
      }
    });
  }

  removeTag(index: number): void {
    this.article.tagList.splice(index, 1);
  }
}
