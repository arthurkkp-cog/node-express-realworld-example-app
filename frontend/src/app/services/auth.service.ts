import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { User, UserResponse, RegisterRequest, LoginRequest, UpdateUserRequest } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = environment.apiUrl;
  private currentUserSignal = signal<User | null>(null);
  
  currentUser = computed(() => this.currentUserSignal());
  isAuthenticated = computed(() => !!this.currentUserSignal());

  constructor(private http: HttpClient) {
    this.loadUserFromStorage();
  }

  private loadUserFromStorage(): void {
    const token = localStorage.getItem('token');
    const userJson = localStorage.getItem('user');
    if (token && userJson) {
      try {
        const user = JSON.parse(userJson);
        this.currentUserSignal.set(user);
      } catch {
        this.logout();
      }
    }
  }

  register(email: string, username: string, password: string): Observable<UserResponse> {
    const request: RegisterRequest = {
      user: { email, username, password }
    };
    return this.http.post<UserResponse>(`${this.apiUrl}/users`, request).pipe(
      tap(response => this.setAuth(response.user))
    );
  }

  login(email: string, password: string): Observable<UserResponse> {
    const request: LoginRequest = {
      user: { email, password }
    };
    return this.http.post<UserResponse>(`${this.apiUrl}/users/login`, request).pipe(
      tap(response => this.setAuth(response.user))
    );
  }

  getCurrentUser(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/user`).pipe(
      tap(response => this.setAuth(response.user))
    );
  }

  updateUser(userData: UpdateUserRequest['user']): Observable<UserResponse> {
    const request: UpdateUserRequest = { user: userData };
    return this.http.put<UserResponse>(`${this.apiUrl}/user`, request).pipe(
      tap(response => this.setAuth(response.user))
    );
  }

  private setAuth(user: User): void {
    localStorage.setItem('token', user.token);
    localStorage.setItem('user', JSON.stringify(user));
    this.currentUserSignal.set(user);
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.currentUserSignal.set(null);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }
}
