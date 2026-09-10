import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { AuthService } from '../../auth/service/AuthService';
import { OrderService } from '../service/order.service';
import { Router } from '@angular/router';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-order',
  imports: [CommonModule],
  templateUrl: './order.component.html',
  styleUrl: './order.component.css'
})
export class OrderComponent implements OnInit, OnDestroy {

  orders: any[] = [];
  isLoading = true;
  errorMessage = '';
  successMessage = '';
  selectedStatus: string = 'ALL';  // ← ADD THIS
  private refreshSubscription: Subscription | null = null;  // ← ADD THIS

  // ===== STATUS OPTIONS ===== (ADD THIS)
  statusOptions = [
    { value: 'ALL', label: 'All Orders' },
    { value: 'PENDING', label: 'Pending' },
    { value: 'CONFIRMED', label: 'Confirmed' },
    { value: 'PREPARING', label: 'Preparing' },
    { value: 'READY', label: 'Ready' },
    { value: 'OUT_FOR_DELIVERY', label: 'Out for Delivery' },
    { value: 'DELIVERED', label: 'Delivered' },
    { value: 'CANCELLED', label: 'Cancelled' }
  ];


  constructor(
    private authService: AuthService,
    private orderService: OrderService,
    private router: Router
  ) { }

  ngOnInit() {
    this.loadOrders();
    this.startAutoRefresh();
  }

  ngOnDestroy() {  // ← ADD THIS
    if (this.refreshSubscription) {
      this.refreshSubscription.unsubscribe();
    }
  }

  startAutoRefresh() {
    this.refreshSubscription = interval(25000).subscribe(() => {
      console.log('Auto-refreshing orders...');
      this.loadOrders();
    });
  }

  loadOrders() {
    const userId = this.authService.getUserId();
    if (!userId) {
      this.router.navigate(['/login']);
      return;
    }

    if (this.selectedStatus === 'ALL') {
      this.loadOrderHistory();
    } else {
      this.loadOrdersByStatus(this.selectedStatus);
    }
  }

  // ===== LOAD ORDER HISTORY =====
  loadOrderHistory() {
    const userId = this.authService.getUserId();
    if (!userId) {
      this.router.navigate(['/login']);
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.orderService.getOrderHistory(userId).subscribe({
      next: (data) => {
        console.log('Order history loaded:', data);
        this.orders = data || [];
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Failed to load order history. Please try again.';
        this.isLoading = false;
        console.error('Error loading order history:', error);
      }
    });
  }

  loadOrdersByStatus(status: string) {
    const userId = this.authService.getUserId();
    if (!userId) {
      this.router.navigate(['/login']);
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.orderService.getOrdersByStatus(status).subscribe({
      next: (data) => {
        console.log(`Orders with status ${status}:`, data);
        // Filter orders for the current user
        this.orders = (data || []).filter(order => order.userDTO?.id === userId);
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = error.error?.message || `Failed to load ${status} orders.`;
        this.isLoading = false;
        console.error('Error loading orders by status:', error);
      }
    });
  }

  checkForStatusChanges() {
    // Get active orders (not delivered or cancelled)
    const activeOrders = this.orders.filter(
      order => order.status !== 'DELIVERED' && order.status !== 'CANCELLED'
    );
    if (activeOrders.length > 0) {
      console.log(`${activeOrders.length} active order(s) found`);
      // Show status for each active order
      activeOrders.forEach(order => {
        console.log(`Order #${order.orderId}: ${order.status} - ${order.restaurant?.name}`);
      });
    }
  }

  onStatusChange(event: Event) {
    const select = event.target as HTMLSelectElement;
    this.selectedStatus = select.value;
    this.loadOrders();
  }

  // ===== VIEW ORDER DETAILS =====
  viewOrder(orderId: number) {
    this.router.navigate(['/order-details', orderId]);
  }

  // ===== CANCEL ORDER =====
  cancelOrder(orderId: number) {
    if (!confirm('Are you sure you want to cancel this order?')) {
      return;
    }

    this.orderService.cancelOrder(orderId).subscribe({
      next: (response) => {
        console.log('Order cancelled:', response);
        this.successMessage = 'Order cancelled successfully!';
        // Refresh the list
        this.loadOrders();
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Failed to cancel order. Please try again.';
        console.error('Error cancelling order:', error);
        setTimeout(() => {
          this.errorMessage = '';
        }, 3000);
      }
    });
  }

  // ===== GET STATUS COLOR =====
  getStatusColor(status: string): string {
    switch (status?.toUpperCase()) {
      case 'PENDING': return '#ffc107';
      case 'CONFIRMED': return '#00A7E1';
      case 'PREPARING': return '#17a2b8';
      case 'OUT_FOR_DELIVERY': return '#fd7e14';
      case 'DELIVERED': return '#28a745';
      case 'CANCELLED': return '#dc3545';
      default: return '#6c757d';
    }
  }

  // ===== GET STATUS BADGE CLASS =====
  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'PENDING': return 'status-pending';
      case 'CONFIRMED': return 'status-confirmed';
      case 'PREPARING': return 'status-preparing';
      case 'OUT_FOR_DELIVERY': return 'status-out-for-delivery';
      case 'DELIVERED': return 'status-delivered';
      case 'CANCELLED': return 'status-cancelled';
      default: return 'status-default';
    }
  }

  // ===== GO BACK =====
  goBack() {
    this.router.navigate(['/']);
  }

}
