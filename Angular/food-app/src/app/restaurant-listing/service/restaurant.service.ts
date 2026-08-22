import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { API_URL_RL } from '../../constants/url';

@Injectable({
  providedIn: 'root'
})
export class RestaurantService {

  //private apiUrl = API_URL_RL+'/restaurant/fetchAllRestaurant'; 

    private baseUrl = getServiceUrl('RESTAURANT_SERVICE');


  constructor(private http: HttpClient) { }

  getAllRestaurants(): Observable<any> {

    // return this.http.get<any>(`${this.apiUrl}`)
    //   .pipe(
    //     catchError(this.handleError)
    //   );

    return this.http.get<any>(`${this.baseUrl}/fetchAllRestaurant`)
      .pipe(
        catchError(this.handleError)
      );
  }

  private handleError(error: any) {
    console.error('An error occurred:', error);
    return throwError(error.message || error);
  }
}