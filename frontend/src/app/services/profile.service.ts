import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ProfileResponse } from '../models/profile.model';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getProfile(username: string): Observable<ProfileResponse> {
    return this.http.get<ProfileResponse>(`${this.apiUrl}/profiles/${username}`);
  }

  followUser(username: string): Observable<ProfileResponse> {
    return this.http.post<ProfileResponse>(`${this.apiUrl}/profiles/${username}/follow`, {});
  }

  unfollowUser(username: string): Observable<ProfileResponse> {
    return this.http.delete<ProfileResponse>(`${this.apiUrl}/profiles/${username}/follow`);
  }
}
