import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { API_URL_FC } from '../../constants/url'; 
import { FoodCataloguePage } from '../../shared/model/FoodCataloguePage';

@Injectable({
    providedIn: 'root'
})
export class FoodItemService {

    //private apiUrl = API_URL_FC+'/foodCatalogue/fetchRestaurantAndFoodItemsById/';

    private baseUrl = getServiceUrl('FOOD_CATALOGUE');
  // Or directly: private baseUrl = API_URLS.FOOD_CATALOGUE;


    constructor(private http: HttpClient) { }

    getFoodItemsByRestaurant(id:number): Observable<FoodCataloguePage> {
        // return this.http.get<any>(`${this.apiUrl+id}`)
        //   .pipe(
        //     catchError(this.handleError)
        //   );

        return this.http.get<any>(`${this.baseUrl}/foodCatalogue/fetchRestaurantAndFoodItemsById/${id}`)
          .pipe(
            catchError(this.handleError)
          );
      }
    
      private handleError(error: any) {
        console.error('An error occurred:', error);
        return throwError(error.message || error);
      }

}