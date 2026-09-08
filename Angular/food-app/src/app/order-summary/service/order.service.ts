import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
//import {  API_URL_Order } from '../../constants/url'; 
import { catchError } from 'rxjs/operators';
import { getServiceUrl } from '../../constants/url';

@Injectable({
  providedIn: 'root'
})

export class OrderService {

  //private apiUrl = API_URL_Order+'/order/saveOrder';
  private baseUrl = getServiceUrl('ORDER_SERVICE');

  constructor(private http: HttpClient) { }

  //  httpOptions = {
  //     headers: new HttpHeaders({
  //       'Content-Type':'text/plain',
  //       'Access-Control-Allow-Origin': 'http://localhost:4200' // Replace with your Angular app URL
  //     })
  //   };

  saveOrder(data: any): Observable<any> {
    //return this.http.post<any>(this.apiUrl, data);
    const token = localStorage.getItem('authToken');
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    headers = headers.set('Content-Type', 'application/json');

    console.log('Sending order with headers:', headers);
    console.log('Order data:', data);

    return this.http.post<any>(`${this.baseUrl}/order/saveOrder`, data, { headers })
      .pipe(
        catchError(this.handleError)
      );
  }

  // private handleError(error: any) {
  //   console.error('An error occurred:', error);
  //   return throwError(error.message || error);
  // }

  private handleError(error: any) {
    console.error('Order Service Error:', error);
    let errorMessage = 'Failed to place order. Please try again.';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = error.error.message;
    } else if (error.error && error.error.message) {
      // Backend error with message
      errorMessage = error.error.message;
    } else if (error.status === 401) {
      errorMessage = 'Please login to place an order.';
    } else if (error.status === 403) {
      errorMessage = 'You do not have permission to place an order.';
    } else if (error.status === 400) {
      errorMessage = 'Invalid order data. Please check your cart.';
    }

    return throwError(() => ({
      message: errorMessage,
      status: error.status,
      details: error.error
    }));
  }


}