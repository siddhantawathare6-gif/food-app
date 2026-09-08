import { Component } from '@angular/core';
import { OrderDTO } from '../model/OrderDTO';
import { ActivatedRoute, Router } from '@angular/router';
import { OrderService } from '../service/order.service';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../auth/service/AuthService';
import { HttpErrorResponse } from '@angular/common/http';
import { extractErrorMessage } from '../../shared/util/error-utils';

@Component({
  selector: 'app-order-summary',
  imports: [CommonModule],
  templateUrl: './order-summary.component.html',
  styleUrl: './order-summary.component.css'
})
export class OrderSummaryComponent {


  orderSummary!: OrderDTO;
  obj: OrderDTO;
  total?: number = 0;
  showDialog = false;
  errorMessage: string | null = null;
  isLoading = false;
  orderResponse: any = null;
  restaurantId: number | null = null;

  constructor(private route: ActivatedRoute,
    private orderService: OrderService,
    private router: Router,
    private authService: AuthService) { }

  ngOnInit() {
    this.loadOrderData();
  }

  loadOrderData() {
    const data = this.route.snapshot.queryParams['data'];
    console.log('Query Param Data:', data);
    if (!data) {
      console.error('No order summary data found');
      this.errorMessage = 'No order data found. Please add items to your cart.';
      return;
    }
    try {

      const parsedData: OrderDTO = JSON.parse(data);

      console.log('Parsed Data:', parsedData);
      console.log('Food Items:', parsedData.foodItemsList);

      // Store restaurant ID for back navigation
      if (parsedData.restaurant) {
        this.restaurantId = parsedData.restaurant.id;
        console.log('Restaurant ID for back navigation:', this.restaurantId);
      }

      this.orderSummary = parsedData;

      this.total = (this.orderSummary.foodItemsList ?? [])
        .reduce((accumulator, currentValue) => {
          return accumulator +
            (currentValue.quantity * (currentValue.price ?? 0));
        }, 0);

      console.log('Final Order Summary:', this.orderSummary);
      console.log('Total:', this.total);

    } catch (error) {

      console.error('Error parsing order summary:', error);
      this.errorMessage = 'Invalid order data. Please try again.';

    }

  }

  saveOrder() {
    this.errorMessage = null;
    this.isLoading = true;

    if (!this.authService.isLoggedIn()) {
      this.isLoading = false;

      this.router.navigate(['/register'], {
        queryParams: {
          returnUrl: '/orderSummary',
          data: JSON.stringify(this.orderSummary)
        }
      });
      return;
    }
    const userId = this.authService.getUserId();

    if (!userId) {
      this.isLoading = false;
      this.errorMessage = 'User ID not found. Please login again.';
      return;
    }

    // Set the userId
    this.orderSummary.userId = userId;
    console.log('Final order data being sent:', this.orderSummary);


    this.orderService.saveOrder(this.orderSummary)
      .subscribe({
        next: (response) => {
          this.isLoading = false;
          console.log('Order saved successfully:', response);
          this.orderResponse = response;
          this.showDialog = true;
        },
        error: (err: HttpErrorResponse) => {
          this.isLoading = false;
          console.error('Full error details:', err);

          // Extract error message
          const errorMsg = err.error?.message || err.message || 'Failed to place your order.';
          this.errorMessage = extractErrorMessage(err, 'Failed to place your order. Please try again.');

          // Log more details for debugging
          console.error('Error status:', err.status);
          console.error('Error body:', err.error);
          console.error('Error headers:', err.headers);
        }
      });
  }

  closeDialog() {
    this.showDialog = false;
    this.router.navigate(['/']); // Replace '/home' with the actual route for your home page
  }


  goBack() {
    console.log('Going back with restaurantId:', this.restaurantId);
    this.router.navigate(['/food-catalogue']);
    if (this.restaurantId) {
      // Navigate back to food catalogue with the restaurant ID
      this.router.navigate(['/food-catalogue', this.restaurantId]);
    } else {
      // If no restaurant ID, go to home page
      this.router.navigate(['/']);
    }
  }

}
