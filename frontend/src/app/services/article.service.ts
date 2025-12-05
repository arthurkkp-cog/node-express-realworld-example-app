import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { 
  Article, 
  ArticleResponse, 
  ArticlesResponse, 
  CreateArticleRequest, 
  UpdateArticleRequest 
} from '../models/article.model';
import { Comment, CommentResponse, CommentsResponse, CreateCommentRequest } from '../models/comment.model';

@Injectable({
  providedIn: 'root'
})
export class ArticleService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getArticles(params: {
    tag?: string;
    author?: string;
    favorited?: string;
    offset?: number;
    limit?: number;
  } = {}): Observable<ArticlesResponse> {
    let httpParams = new HttpParams();
    
    if (params.tag) httpParams = httpParams.set('tag', params.tag);
    if (params.author) httpParams = httpParams.set('author', params.author);
    if (params.favorited) httpParams = httpParams.set('favorited', params.favorited);
    if (params.offset !== undefined) httpParams = httpParams.set('offset', params.offset.toString());
    if (params.limit !== undefined) httpParams = httpParams.set('limit', params.limit.toString());

    return this.http.get<ArticlesResponse>(`${this.apiUrl}/articles`, { params: httpParams });
  }

  getFeed(offset: number = 0, limit: number = 10): Observable<ArticlesResponse> {
    const params = new HttpParams()
      .set('offset', offset.toString())
      .set('limit', limit.toString());
    return this.http.get<ArticlesResponse>(`${this.apiUrl}/articles/feed`, { params });
  }

  getArticle(slug: string): Observable<ArticleResponse> {
    return this.http.get<ArticleResponse>(`${this.apiUrl}/articles/${slug}`);
  }

  createArticle(article: CreateArticleRequest['article']): Observable<ArticleResponse> {
    const request: CreateArticleRequest = { article };
    return this.http.post<ArticleResponse>(`${this.apiUrl}/articles`, request);
  }

  updateArticle(slug: string, article: UpdateArticleRequest['article']): Observable<ArticleResponse> {
    const request: UpdateArticleRequest = { article };
    return this.http.put<ArticleResponse>(`${this.apiUrl}/articles/${slug}`, request);
  }

  deleteArticle(slug: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/articles/${slug}`);
  }

  getComments(slug: string): Observable<CommentsResponse> {
    return this.http.get<CommentsResponse>(`${this.apiUrl}/articles/${slug}/comments`);
  }

  addComment(slug: string, body: string): Observable<CommentResponse> {
    const request: CreateCommentRequest = { comment: { body } };
    return this.http.post<CommentResponse>(`${this.apiUrl}/articles/${slug}/comments`, request);
  }

  deleteComment(slug: string, commentId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/articles/${slug}/comments/${commentId}`);
  }

  favoriteArticle(slug: string): Observable<ArticleResponse> {
    return this.http.post<ArticleResponse>(`${this.apiUrl}/articles/${slug}/favorite`, {});
  }

  unfavoriteArticle(slug: string): Observable<ArticleResponse> {
    return this.http.delete<ArticleResponse>(`${this.apiUrl}/articles/${slug}/favorite`);
  }
}
