import { Component, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { RestaurantService } from '../service/restaurant.service';
import { Restaurant } from '../../shared/model/Restaurant';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { extractErrorMessage } from '../../shared/util/error-utils';
import { AuthService } from '../../auth/service/AuthService';
import { API_URL_RL } from '../../constants/url';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-restaurant-listing',
  imports: [CommonModule],
  templateUrl: './restaurant-listing.component.html',
  styleUrl: './restaurant-listing.component.css'
})
export class RestaurantListingComponent implements OnDestroy {


  public restaurantList: Restaurant[] = [];
  public totalPages: number = 0;
  public currentPage: number = 0;
  public pageSize: number = 4;
  public pageSizeOptions: number[] = [4, 8, 12, 16];
  public errorMessage: string | null = null;
  public isAdmin: boolean = false;

  public showDeleteDialog = false;
  public restaurantToDelete: Restaurant | null = null;
  public isDeleting = false;

  private authSubscription: Subscription | null = null;


  ngOnInit() {
    this.getAllRestaurants();
    this.checkAdminStatus();

    // Subscribe to auth changes to update admin status on logout/login
    this.authSubscription = this.authService.userRoles$.subscribe(() => {
      this.checkAdminStatus();
    });
  }

  ngOnDestroy() {
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
  }

  constructor(private router: Router,
    private restaurantService: RestaurantService,
    private authService: AuthService) { }

  checkAdminStatus() {
    this.isAdmin = this.authService.isAdmin();
    console.log('Is Admin:', this.isAdmin);
  }

  getAllRestaurants() {
    this.errorMessage = null;
    this.restaurantService.getAllRestaurants(this.currentPage, this.pageSize).subscribe({
      next: data => {
        this.restaurantList = data.restaurantList || [];
        this.totalPages = data.totalPage;
        console.log('Restaurants loaded:', this.restaurantList);
        this.restaurantList.forEach(restaurant => {
          console.log(`Restaurant ${restaurant.id} - ${restaurant.name}:`, {
            imageName: restaurant.imageUrl,
            imageUrl: this.getImageUrl(restaurant.id)
          });
        });
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = extractErrorMessage(err, 'Unable to load restaurants. Please try again later.');
        console.error('Failed to fetch restaurants:', err);
      }
    });
  }

  // Construct image URL using restaurant ID
  getImageUrl(restaurantId: number): string {
    return `${API_URL_RL}/restaurant/image/${restaurantId}`;
  }

  goToPage(page: number) {
    if (page < 0 || page >= this.totalPages) {
      return;
    }
    this.currentPage = page;
    this.getAllRestaurants();
  }

  nextPage() {
    this.goToPage(this.currentPage + 1);
  }

  prevPage() {
    this.goToPage(this.currentPage - 1);
  }

  onPageSizeChange(event: Event) {
    const newSize = Number((event.target as HTMLSelectElement).value);
    this.pageSize = newSize;
    this.currentPage = 0; // reset to first page when page size changes
    this.getAllRestaurants();
  }

  // Builds an array [0, 1, 2, ..., totalPages-1] so *ngFor can render page number buttons
  getPageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }

  // Helper method to get rating stars
  getRatingStars(rating: number | undefined): string {
    if (!rating) return '';
    const fullStars = Math.floor(rating);
    const halfStar = rating % 1 >= 0.5 ? 1 : 0;
    const emptyStars = 5 - fullStars - halfStar;
    return '★'.repeat(fullStars) + (halfStar ? '½' : '') + '☆'.repeat(emptyStars);
  }

  // Helper to get rating color based on rating value
  getRatingColor(rating: number | undefined): string {
    if (!rating) return '#6c757d';
    if (rating >= 4.5) return '#28a745';
    if (rating >= 4.0) return '#5cb85c';
    if (rating >= 3.5) return '#ffc107';
    if (rating >= 3.0) return '#fd7e14';
    return '#dc3545';
  }


  // getRandomNumber(min: number, max: number): number {
  //   return Math.floor(Math.random() * (max - min + 1)) + min;
  // }

  // getRandomImage(): string {
  //   const imageCount = 8; // Adjust this number based on the number of images in your asset folder
  //   const randomIndex = this.getRandomNumber(1, imageCount);
  //   return `${randomIndex}.jpg`; // Replace with your image filename pattern
  // }


  onButtonClick(id: number) {
    this.router.navigate(['/food-catalogue', id]);
  }

  navigateToAddRestaurant() {
    this.router.navigate(['/restaurant/add']);
  }

  navigateToEditRestaurant(id: number) {
    this.router.navigate(['/restaurant/edit', id]);
  }

  confirmDelete(restaurant: Restaurant) {
    this.restaurantToDelete = restaurant;
    this.showDeleteDialog = true;
  }

  cancelDelete() {
    this.showDeleteDialog = false;
    this.restaurantToDelete = null;
    this.isDeleting = false;
  }

  deleteRestaurant() {
    if (!this.restaurantToDelete) return;

    this.isDeleting = true;
    this.errorMessage = null;

    const id = this.restaurantToDelete.id;

    this.restaurantService.deleteRestaurantWithFoodItems(id).subscribe({
      next: () => {
        this.isDeleting = false;
        this.showDeleteDialog = false;
        this.restaurantToDelete = null;
        // Refresh the list
        this.getAllRestaurants();
        console.log('Restaurant deleted successfully');
      },
      error: (err: HttpErrorResponse) => {
        this.isDeleting = false;
        this.errorMessage = extractErrorMessage(err, 'Failed to delete restaurant. Please try again.');
        console.error('Failed to delete restaurant:', err);
        this.showDeleteDialog = false;
        this.restaurantToDelete = null;
      }
    });
  }

  // Handle image loading error
  onImageError(event: Event) {
    const img = event.target as HTMLImageElement;
    // Use default image from backend
    img.src = `${API_URL_RL}/restaurant/image/default`;
  }

}
