import { Routes } from '@angular/router';
import { authGuard, noAuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/shared/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./components/auth/login/login.component').then(m => m.LoginComponent),
    canActivate: [noAuthGuard]
  },
  {
    path: 'register',
    loadComponent: () => import('./components/auth/register/register.component').then(m => m.RegisterComponent),
    canActivate: [noAuthGuard]
  },
  {
    path: 'settings',
    loadComponent: () => import('./components/shared/settings/settings.component').then(m => m.SettingsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'editor',
    loadComponent: () => import('./components/article/article-editor/article-editor.component').then(m => m.ArticleEditorComponent),
    canActivate: [authGuard]
  },
  {
    path: 'editor/:slug',
    loadComponent: () => import('./components/article/article-editor/article-editor.component').then(m => m.ArticleEditorComponent),
    canActivate: [authGuard]
  },
  {
    path: 'article/:slug',
    loadComponent: () => import('./components/article/article-detail/article-detail.component').then(m => m.ArticleDetailComponent)
  },
  {
    path: 'profile/:username',
    loadComponent: () => import('./components/profile/profile/profile.component').then(m => m.ProfileComponent)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
