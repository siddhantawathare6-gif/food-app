import { Component } from '@angular/core';
import { OrderDTO } from '../model/OrderDTO';
import { ActivatedRoute, Router } from '@angular/router';
import { OrderService } from '../service/order.service';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../auth/service/AuthService';
import { HttpErrorResponse } from '@angular/common/http';
import { extractErrorMessage } from '../../shared/util/error-utils';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../auth/service/UserService';

@Component({
  selector: 'app-order-summary',
  imports: [CommonModule, FormsModule],
  templateUrl: './order-summary.component.html',
  styleUrl: './order-summary.component.css'
})
export class OrderSummaryComponent {


  orderSummary!: OrderDTO;
  obj: OrderDTO;
  total: number = 0;
  showDialog = false;
  errorMessage: string | null = null;
  isLoading = false;
  orderResponse: any = null;
  restaurantId: number | null = null;
  deliveryAddress: string = '';
  paymentMethod: string = 'CASH';
  deliveryInstructions: string = '';
  userProfileLoaded: boolean = false;

  constructor(private route: ActivatedRoute,
    private orderService: OrderService,
    private router: Router,
    private authService: AuthService,
    private userService: UserService) { }

  ngOnInit() {
    this.loadOrderData();
    this.loadUserDeliveryAddress();
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

  loadUserDeliveryAddress() {
    const userId = this.authService.getUserId();
    if (!userId) {
      console.log('User not logged in, skipping address load');
      this.userProfileLoaded = true;
      return;
    }

    this.userService.getUserProfile(userId).subscribe({
      next: (data) => {
        console.log('User profile data:', data);
        this.userProfileLoaded = true;

        // ===== Build address from user profile =====
        if (data.address) {
          const addressParts = [];

          if (data.address.addressLine1) {
            addressParts.push(data.address.addressLine1);
          }
          if (data.address.addressLine2) {
            addressParts.push(data.address.addressLine2);
          }
          if (data.address.city) {
            addressParts.push(data.address.city);
          }
          if (data.address.state) {
            addressParts.push(data.address.state);
          }
          if (data.address.pincode) {
            addressParts.push(data.address.pincode);
          }
          if (data.address.country) {
            addressParts.push(data.address.country);
          }

          if (addressParts.length > 0) {
            this.deliveryAddress = addressParts.join(', ');
            console.log('Loaded delivery address from profile:', this.deliveryAddress);
          } else {
            console.log('No address found in user profile');
          }
        } else {
          console.log('No address found in user profile');
        }
      },
      error: (error) => {
        console.error('Failed to load user profile:', error);
        this.userProfileLoaded = true;
      }
    });
  }

  saveOrder() {
    this.errorMessage = null;
    this.isLoading = true;

    if (!this.authService.isLoggedIn()) {
      this.isLoading = false;

      this.router.navigate(['/login'], {
        queryParams: {
          returnUrl: '/orderSummary',
          data: JSON.stringify(this.orderSummary)
        }
      });
      return;
    }
    const userId = this.authService.getUserId();
    console.log('User ID from auth service:', userId);

    if (!userId) {
      this.isLoading = false;
      this.errorMessage = 'User ID not found. Please login again.';
      return;
    }

    if (!this.deliveryAddress || this.deliveryAddress.trim() === '') {
      this.isLoading = false;
      this.errorMessage = 'Please enter a delivery address.';
      return;
    }

    const orderData = {
      userId: userId,
      restaurant: this.orderSummary.restaurant,
      foodItemsList: this.orderSummary.foodItemsList,
      deliveryAddress: this.deliveryAddress,
      paymentMethod: this.paymentMethod,
      totalAmount: this.total,
      deliveryInstructions: this.deliveryInstructions
    };

    console.log('Final order data being sent:', orderData);

    this.orderService.saveOrder(orderData)
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
    // ===== ALWAYS NAVIGATE TO HOME =====
    setTimeout(() => {
        this.router.navigate(['/orders']);
    }, 500);
  }


  goBack() {
    console.log('Going back with restaurantId:', this.restaurantId);
    if (this.restaurantId) {
      // Navigate back to food catalogue with the restaurant ID
      this.router.navigate(['/food-catalogue', this.restaurantId]);
    } else {
      // If no restaurant ID, go to home page
      this.router.navigate(['/']);
    }
  }

}
