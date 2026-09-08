import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
//import { catchError } from 'rxjs/operators';
import { API_URL_RL } from '../../constants/url';
import { RestaurantPage } from '../../shared/model/RestaurantPage';
import { Restaurant } from '../../shared/model/Restaurant';
//import { getServiceUrl } from '../../constants/url';

@Injectable({
  providedIn: 'root'
})
export class RestaurantService {

  private apiUrl = API_URL_RL + '/restaurant/fetchAllRestaurant';
  private baseUrl = API_URL_RL + '/restaurant';

  //private baseUrl = getServiceUrl('RESTAURANT_SERVICE');


  constructor(private http: HttpClient) { }

  getAllRestaurants(pageNo: number = 0, pageSize: number = 10, sortBy: string = 'id', sortDir: string = 'asc'): Observable<RestaurantPage> {
    let params = new HttpParams()
      .set('pageNo', pageNo)
      .set('pageSize', pageSize)
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    return this.http.get<RestaurantPage>(this.apiUrl, { params });

    // return this.http.get<any>(`${this.baseUrl}/restaurant/fetchAllRestaurant`)
    //   .pipe(
    //     catchError(this.handleError)
    //   );
  }

  // private handleError(error: any) {
  //   console.error('An error occurred:', error);
  //   return throwError(error.message || error);
  // }

  // Add restaurant (Admin only)
  addRestaurant(restaurantData: any): Observable<Restaurant> {
    const token = localStorage.getItem('authToken');
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    headers = headers.set('Content-Type', 'application/json');
    console.log('Adding restaurant with token:', token ? 'Present' : 'Missing');
    return this.http.post<Restaurant>(`${this.baseUrl}/addRestaurant`, restaurantData, { headers });
  }

  //  Upload restaurant image (Admin only)
  uploadRestaurantImage(restaurantId: number, imageFile: File): Observable<string> {
    const token = localStorage.getItem('authToken');
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    // Don't set Content-Type for FormData - browser will set it with boundary
    const formData = new FormData();
    formData.append('image', imageFile);
    console.log('Uploading image for restaurant:', restaurantId);
    console.log('File name:', imageFile.name);
    console.log('File size:', imageFile.size);
    console.log('File type:', imageFile.type);
    console.log('Token present:', token ? 'Yes' : 'No');
    return this.http.post<string>(`${this.baseUrl}/uploadImage/${restaurantId}`, formData, {
      headers: headers,
      responseType: 'text' as 'json' // This tells Angular to expect text response
    });
  }

  // NEW: Get single restaurant by ID
  getRestaurantById(id: number): Observable<Restaurant> {
    return this.http.get<Restaurant>(`${this.baseUrl}/fetchById/${id}`);
  }
}