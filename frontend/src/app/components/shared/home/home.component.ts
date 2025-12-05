import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ArticleListComponent } from '../../article/article-list/article-list.component';
import { TagService } from '../../../services/tag.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, ArticleListComponent],
  template: `
    <div class="home-page">
      <div class="banner" *ngIf="!authService.isAuthenticated()">
        <div class="container">
          <h1 class="logo-font">conduit</h1>
          <p>A place to share your knowledge.</p>
        </div>
      </div>

      <div class="container page">
        <div class="row">
          <div class="col-md-9">
            <div class="feed-toggle">
              <ul class="nav nav-pills outline-active">
                <li class="nav-item" *ngIf="authService.isAuthenticated()">
                  <a class="nav-link" [class.active]="feedType === 'feed'" (click)="setFeed('feed')">
                    Your Feed
                  </a>
                </li>
                <li class="nav-item">
                  <a class="nav-link" [class.active]="feedType === 'global'" (click)="setFeed('global')">
                    Global Feed
                  </a>
                </li>
                <li class="nav-item" *ngIf="selectedTag">
                  <a class="nav-link active">
                    <i class="ion-pound"></i> {{ selectedTag }}
                  </a>
                </li>
              </ul>
            </div>

            <app-article-list 
              [feed]="feedType === 'feed'" 
              [tag]="selectedTag">
            </app-article-list>
          </div>

          <div class="col-md-3">
            <div class="sidebar">
              <p>Popular Tags</p>
              <div class="tag-list">
                <a 
                  *ngFor="let tag of tags" 
                  class="tag-pill tag-default"
                  (click)="selectTag(tag)">
                  {{ tag }}
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .banner { background: #5cb85c; padding: 2rem; text-align: center; color: #fff; margin-bottom: 2rem; }
    .banner h1 { font-size: 3.5rem; text-shadow: 0 1px 3px rgba(0,0,0,0.3); }
    .feed-toggle { margin-bottom: 1rem; }
    .nav-pills { display: flex; border-bottom: 1px solid #e5e5e5; }
    .nav-link { padding: 0.5rem 1rem; color: #aaa; cursor: pointer; border-bottom: 2px solid transparent; }
    .nav-link.active { color: #5cb85c; border-bottom-color: #5cb85c; }
    .sidebar { padding: 0.5rem 1rem; background: #f3f3f3; border-radius: 4px; }
    .sidebar p { margin-bottom: 0.5rem; }
    .tag-list { display: flex; flex-wrap: wrap; gap: 0.25rem; }
    .tag-pill { padding: 0.2rem 0.6rem; background: #818a91; color: #fff; border-radius: 10rem; font-size: 0.8rem; cursor: pointer; text-decoration: none; }
    .tag-pill:hover { background: #687077; color: #fff; }
  `]
})
export class HomeComponent implements OnInit {
  tags: string[] = [];
  feedType: 'feed' | 'global' = 'global';
  selectedTag: string | undefined;

  constructor(
    private tagService: TagService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadTags();
    if (this.authService.isAuthenticated()) {
      this.feedType = 'feed';
    }
  }

  loadTags(): void {
    this.tagService.getTags().subscribe({
      next: (response) => {
        this.tags = response.tags;
      }
    });
  }

  setFeed(type: 'feed' | 'global'): void {
    this.feedType = type;
    this.selectedTag = undefined;
  }

  selectTag(tag: string): void {
    this.selectedTag = tag;
    this.feedType = 'global';
  }
}
