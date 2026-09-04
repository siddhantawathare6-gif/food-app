import { Component } from '@angular/core';
import { OrderDTO } from '../model/OrderDTO';
import { ActivatedRoute, Router } from '@angular/router';
import { OrderService } from '../service/order.service';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../auth/service/AuthService';

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

  constructor(private route: ActivatedRoute,
    private orderService: OrderService,
    private router: Router,
    private authService: AuthService) { }

  ngOnInit() {
    const data = this.route.snapshot.queryParams['data'];
    console.log('Query Param Data:', data);
    if (!data) {
      console.error('No order summary data found');
      return;
    }
    try {

      const parsedData: OrderDTO = JSON.parse(data);

      console.log('Parsed Data:', parsedData);
      console.log('Food Items:', parsedData.foodItemsList);

      parsedData.userId = 1;

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

    }

  }

  saveOrder() {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/register'], {
        queryParams: {
          returnUrl: '/orderSummary',
          data: JSON.stringify(this.orderSummary)
        }
      });
      return;
    }
    this.orderSummary.userId = this.authService.getUserId()!;
    this.orderService.saveOrder(this.orderSummary)
      .subscribe(
        response => {
          this.showDialog = true;
        },
        error => {
          console.error('Failed to save data:', error);
        }
      );
  }

  closeDialog() {
    this.showDialog = false;
    this.router.navigate(['/']); // Replace '/home' with the actual route for your home page
  }


}
