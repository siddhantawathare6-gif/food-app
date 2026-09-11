import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { getServiceUrl } from '../../constants/url';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private baseUrl = getServiceUrl('USER_DETAILS');

  constructor(private http: HttpClient) { }

  // ===== GET USER BY ID =====
  getUserProfile(userId: number): Observable<any> {
    const token = localStorage.getItem('authToken');
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return this.http.get<any>(`${this.baseUrl}/user/fetchUserById/${userId}`, { headers });
  }

  // ===== GET CURRENT USER PROFILE =====
  getCurrentUserProfile(): Observable<any> {
    const token = localStorage.getItem('authToken');
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return this.http.get<any>(`${this.baseUrl}/user/profile`, { headers });
  }

  // ===== UPDATE CURRENT USER PROFILE =====
  updateUserProfile(updateData: any): Observable<any> {
    const token = localStorage.getItem('authToken');
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    headers = headers.set('Content-Type', 'application/json');

    return this.http.put<any>(`${this.baseUrl}/user/profile`, updateData, { headers });
  }

  // ===== UPLOAD PROFILE IMAGE =====
  uploadUserImage(userId: number, file: File): Observable<string> {
    const token = localStorage.getItem('authToken');
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    const formData = new FormData();
    formData.append('image', file);
    return this.http.post<string>(
      `${this.baseUrl}/user/uploadImage/${userId}`,
      formData,
      { headers, responseType: 'text' as 'json' }
    );
  }

  // ===== GET PROFILE IMAGE URL =====
  getUserImageUrl(userId: number): string {
    return `${this.baseUrl}/user/image/${userId}`;
  }
}