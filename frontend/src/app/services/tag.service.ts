import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TagsResponse } from '../models/tag.model';

@Injectable({
  providedIn: 'root'
})
export class TagService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getTags(): Observable<TagsResponse> {
    return this.http.get<TagsResponse>(`${this.apiUrl}/tags`);
  }
}
