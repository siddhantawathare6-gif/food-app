import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { API_URL_RL } from '../../constants/url';
import { RestaurantPage } from '../../shared/model/RestaurantPage';
//import { getServiceUrl } from '../../constants/url';

@Injectable({
  providedIn: 'root'
})
export class RestaurantService {

  private apiUrl = API_URL_RL + '/restaurant/fetchAllRestaurant';

  //private baseUrl = getServiceUrl('RESTAURANT_SERVICE');


  constructor(private http: HttpClient) { }

  getAllRestaurants(pageNo: number = 0, pageSize: number = 10, sortBy: string = 'id', sortDir: string = 'asc'): Observable<RestaurantPage> {
    let params = new HttpParams()
      .set('pageNo', pageNo)
      .set('pageSize', pageSize)
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    return this.http.get<RestaurantPage>(this.apiUrl, { params })
      .pipe(
        catchError(this.handleError)
      );

    // return this.http.get<any>(`${this.baseUrl}/restaurant/fetchAllRestaurant`)
    //   .pipe(
    //     catchError(this.handleError)
    //   );
  }

  private handleError(error: any) {
    console.error('An error occurred:', error);
    return throwError(error.message || error);
  }
}