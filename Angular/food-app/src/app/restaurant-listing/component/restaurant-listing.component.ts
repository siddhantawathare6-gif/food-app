import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { RestaurantService } from '../service/restaurant.service';
import { Restaurant } from '../../shared/model/Restaurant';
import { CommonModule, NgIf } from '@angular/common';

@Component({
  selector: 'app-restaurant-listing',
  imports: [CommonModule],
  templateUrl: './restaurant-listing.component.html',
  styleUrl: './restaurant-listing.component.css'
})
export class RestaurantListingComponent {


  public restaurantList: Restaurant[] = [];
  public totalPages: number = 0;
  public currentPage: number = 0;
  public pageSize: number = 4;
  public pageSizeOptions: number[] = [4, 8, 12, 16];

  ngOnInit() {
    this.getAllRestaurants();
  }

  constructor(private router: Router, private restaurantService: RestaurantService) { }

  getAllRestaurants() {
    this.restaurantService.getAllRestaurants(this.currentPage, this.pageSize).subscribe(
      data => {
        this.restaurantList = data.restaurantList || [];
        this.totalPages = data.totalPage;
      }
    )
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

  getRandomNumber(min: number, max: number): number {
    return Math.floor(Math.random() * (max - min + 1)) + min;
  }


  getRandomImage(): string {
    const imageCount = 8; // Adjust this number based on the number of images in your asset folder
    const randomIndex = this.getRandomNumber(1, imageCount);
    return `${randomIndex}.jpg`; // Replace with your image filename pattern
  }

  onButtonClick(id: number) {
    this.router.navigate(['/food-catalogue', id]);
  }

}
