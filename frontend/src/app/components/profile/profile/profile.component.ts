import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Profile } from '../../../models/profile.model';
import { ProfileService } from '../../../services/profile.service';
import { AuthService } from '../../../services/auth.service';
import { ArticleListComponent } from '../../article/article-list/article-list.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, RouterLink, ArticleListComponent],
  template: `
    <div class="profile-page" *ngIf="profile">
      <div class="user-info">
        <div class="container">
          <div class="row">
            <div class="col-xs-12 col-md-10 offset-md-1">
              <img [src]="profile.image || 'https://api.realworld.io/images/smiley-cyrus.jpeg'" class="user-img" />
              <h4>{{ profile.username }}</h4>
              <p>{{ profile.bio }}</p>
              <button 
                *ngIf="!isCurrentUser"
                class="btn btn-sm action-btn"
                [class.btn-outline-secondary]="!profile.following"
                [class.btn-secondary]="profile.following"
                (click)="toggleFollow()">
                <i class="ion-plus-round"></i>
                &nbsp; {{ profile.following ? 'Unfollow' : 'Follow' }} {{ profile.username }}
              </button>
              <a 
                *ngIf="isCurrentUser"
                routerLink="/settings"
                class="btn btn-sm btn-outline-secondary action-btn">
                <i class="ion-gear-a"></i> Edit Profile Settings
              </a>
            </div>
          </div>
        </div>
      </div>

      <div class="container">
        <div class="row">
          <div class="col-xs-12 col-md-10 offset-md-1">
            <div class="articles-toggle">
              <ul class="nav nav-pills outline-active">
                <li class="nav-item">
                  <a class="nav-link" [class.active]="!showFavorites" (click)="showFavorites = false">
                    My Articles
                  </a>
                </li>
                <li class="nav-item">
                  <a class="nav-link" [class.active]="showFavorites" (click)="showFavorites = true">
                    Favorited Articles
                  </a>
                </li>
              </ul>
            </div>

            <app-article-list 
              [author]="!showFavorites ? profile.username : undefined"
              [favorited]="showFavorites ? profile.username : undefined">
            </app-article-list>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .user-info { background: #f3f3f3; padding: 2rem 0; text-align: center; }
    .user-img { width: 100px; height: 100px; border-radius: 50%; margin-bottom: 1rem; }
    .user-info h4 { font-weight: 700; }
    .action-btn { margin-top: 0.5rem; }
    .articles-toggle { margin: 1.5rem 0; }
    .nav-pills { display: flex; border-bottom: 1px solid #e5e5e5; }
    .nav-link { padding: 0.5rem 1rem; color: #aaa; cursor: pointer; border-bottom: 2px solid transparent; }
    .nav-link.active { color: #5cb85c; border-bottom-color: #5cb85c; }
  `]
})
export class ProfileComponent implements OnInit {
  profile: Profile | null = null;
  showFavorites = false;
  isCurrentUser = false;

  constructor(
    private route: ActivatedRoute,
    private profileService: ProfileService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const username = params.get('username');
      if (username) {
        this.loadProfile(username);
      }
    });
  }

  loadProfile(username: string): void {
    this.profileService.getProfile(username).subscribe({
      next: (response) => {
        this.profile = response.profile;
        this.isCurrentUser = this.profile.username === this.authService.currentUser()?.username;
      }
    });
  }

  toggleFollow(): void {
    if (!this.profile) return;

    if (this.profile.following) {
      this.profileService.unfollowUser(this.profile.username).subscribe({
        next: (response) => {
          this.profile = response.profile;
        }
      });
    } else {
      this.profileService.followUser(this.profile.username).subscribe({
        next: (response) => {
          this.profile = response.profile;
        }
      });
    }
  }
}
