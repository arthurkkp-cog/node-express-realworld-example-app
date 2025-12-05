import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  template: `
    <nav class="navbar navbar-light">
      <div class="container">
        <a class="navbar-brand" routerLink="/">conduit</a>
        <ul class="nav navbar-nav pull-xs-right">
          <li class="nav-item">
            <a class="nav-link" routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}">
              Home
            </a>
          </li>
          <ng-container *ngIf="authService.isAuthenticated(); else loggedOut">
            <li class="nav-item">
              <a class="nav-link" routerLink="/editor" routerLinkActive="active">
                <i class="ion-compose"></i>&nbsp;New Article
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link" routerLink="/settings" routerLinkActive="active">
                <i class="ion-gear-a"></i>&nbsp;Settings
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link" [routerLink]="['/profile', authService.currentUser()?.username]" routerLinkActive="active">
                <img [src]="authService.currentUser()?.image || 'https://api.realworld.io/images/smiley-cyrus.jpeg'" class="user-pic" />
                {{ authService.currentUser()?.username }}
              </a>
            </li>
          </ng-container>
          <ng-template #loggedOut>
            <li class="nav-item">
              <a class="nav-link" routerLink="/login" routerLinkActive="active">Sign in</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" routerLink="/register" routerLinkActive="active">Sign up</a>
            </li>
          </ng-template>
        </ul>
      </div>
    </nav>
  `,
  styles: [`
    .navbar { background: #fff; box-shadow: 0 1px 0 rgba(0,0,0,0.1); padding: 0.5rem 0; }
    .navbar-brand { color: #5cb85c; font-size: 1.5rem; font-weight: 700; }
    .pull-xs-right { margin-left: auto; }
    .nav { display: flex; list-style: none; margin: 0; padding: 0; }
    .nav-link { color: #aaa; padding: 0.5rem 1rem; text-decoration: none; }
    .nav-link:hover, .nav-link.active { color: #555; }
    .user-pic { width: 26px; height: 26px; border-radius: 50%; margin-right: 0.25rem; vertical-align: middle; }
  `]
})
export class HeaderComponent {
  constructor(public authService: AuthService) {}
}
