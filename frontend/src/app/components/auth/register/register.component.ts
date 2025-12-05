import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="auth-page">
      <div class="container page">
        <div class="row">
          <div class="col-md-6 offset-md-3 col-xs-12">
            <h1 class="text-xs-center">Sign up</h1>
            <p class="text-xs-center">
              <a routerLink="/login">Have an account?</a>
            </p>

            <ul class="error-messages" *ngIf="errors.length">
              <li *ngFor="let error of errors">{{ error }}</li>
            </ul>

            <form (ngSubmit)="onSubmit()">
              <fieldset class="form-group">
                <input
                  class="form-control form-control-lg"
                  type="text"
                  placeholder="Username"
                  [(ngModel)]="username"
                  name="username"
                  required
                />
              </fieldset>
              <fieldset class="form-group">
                <input
                  class="form-control form-control-lg"
                  type="email"
                  placeholder="Email"
                  [(ngModel)]="email"
                  name="email"
                  required
                />
              </fieldset>
              <fieldset class="form-group">
                <input
                  class="form-control form-control-lg"
                  type="password"
                  placeholder="Password"
                  [(ngModel)]="password"
                  name="password"
                  required
                />
              </fieldset>
              <button class="btn btn-lg btn-primary pull-xs-right" [disabled]="isSubmitting">
                Sign up
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth-page { padding-top: 2rem; }
    .text-xs-center { text-align: center; }
    .error-messages { color: #b85c5c; list-style: none; padding: 0; }
    .pull-xs-right { float: right; }
  `]
})
export class RegisterComponent {
  username = '';
  email = '';
  password = '';
  errors: string[] = [];
  isSubmitting = false;

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(): void {
    this.isSubmitting = true;
    this.errors = [];

    this.authService.register(this.email, this.username, this.password).subscribe({
      next: () => {
        this.router.navigate(['/']);
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
}
