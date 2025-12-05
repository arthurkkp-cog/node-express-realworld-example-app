import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="settings-page">
      <div class="container page">
        <div class="row">
          <div class="col-md-6 offset-md-3 col-xs-12">
            <h1 class="text-xs-center">Your Settings</h1>

            <ul class="error-messages" *ngIf="errors.length">
              <li *ngFor="let error of errors">{{ error }}</li>
            </ul>

            <form (ngSubmit)="onSubmit()">
              <fieldset>
                <fieldset class="form-group">
                  <input
                    class="form-control"
                    type="text"
                    placeholder="URL of profile picture"
                    [(ngModel)]="user.image"
                    name="image"
                  />
                </fieldset>
                <fieldset class="form-group">
                  <input
                    class="form-control form-control-lg"
                    type="text"
                    placeholder="Username"
                    [(ngModel)]="user.username"
                    name="username"
                  />
                </fieldset>
                <fieldset class="form-group">
                  <textarea
                    class="form-control form-control-lg"
                    rows="8"
                    placeholder="Short bio about you"
                    [(ngModel)]="user.bio"
                    name="bio"
                  ></textarea>
                </fieldset>
                <fieldset class="form-group">
                  <input
                    class="form-control form-control-lg"
                    type="email"
                    placeholder="Email"
                    [(ngModel)]="user.email"
                    name="email"
                  />
                </fieldset>
                <fieldset class="form-group">
                  <input
                    class="form-control form-control-lg"
                    type="password"
                    placeholder="New Password"
                    [(ngModel)]="user.password"
                    name="password"
                  />
                </fieldset>
                <button class="btn btn-lg btn-primary pull-xs-right" [disabled]="isSubmitting">
                  Update Settings
                </button>
              </fieldset>
            </form>
            <hr />
            <button class="btn btn-outline-danger" (click)="logout()">
              Or click here to logout.
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .settings-page { padding-top: 2rem; }
    .text-xs-center { text-align: center; }
    .error-messages { color: #b85c5c; list-style: none; padding: 0; }
    .pull-xs-right { float: right; }
  `]
})
export class SettingsComponent implements OnInit {
  user = {
    image: '',
    username: '',
    bio: '',
    email: '',
    password: ''
  };
  errors: string[] = [];
  isSubmitting = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const currentUser = this.authService.currentUser();
    if (currentUser) {
      this.user = {
        image: currentUser.image || '',
        username: currentUser.username,
        bio: currentUser.bio || '',
        email: currentUser.email,
        password: ''
      };
    }
  }

  onSubmit(): void {
    this.isSubmitting = true;
    this.errors = [];

    const updateData: any = {};
    if (this.user.image) updateData.image = this.user.image;
    if (this.user.username) updateData.username = this.user.username;
    if (this.user.bio) updateData.bio = this.user.bio;
    if (this.user.email) updateData.email = this.user.email;
    if (this.user.password) updateData.password = this.user.password;

    this.authService.updateUser(updateData).subscribe({
      next: () => {
        this.router.navigate(['/profile', this.user.username]);
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

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
