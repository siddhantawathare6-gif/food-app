import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/service/AuthService';
import { RestaurantService } from '../service/restaurant.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-add-restaurant',
  imports: [CommonModule, FormsModule],
  templateUrl: './add-restaurant.component.html',
  styleUrl: './add-restaurant.component.css'
})
export class AddRestaurantComponent implements OnInit {

  restaurantData = {
    name: '',
    address: '',
    city: '',
    restaurantDescription: ''
  };

  selectedImage: File | null = null;
  imagePreview: string | null = null;
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  isImageUploading = false;
  foodItems: any[] = [];
  showFoodItemsForm = false;

  constructor(
    private restaurantService: RestaurantService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit() {
    // Redirect if not admin
    if (!this.authService.isAdmin()) {
      this.router.navigate(['/']);
    }
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedImage = input.files[0];

      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview = reader.result as string;
      };
      reader.readAsDataURL(this.selectedImage);
    }
  }

  // onSubmit() {
  //   if (!this.restaurantData.name || !this.restaurantData.city) {
  //     this.errorMessage = 'Name and City are required fields.';
  //     return;
  //   }

  //   this.isLoading = true;
  //   this.errorMessage = null;
  //   this.successMessage = null;

  //   this.restaurantService.addRestaurant(this.restaurantData).subscribe({
  //     next: (restaurant) => {
  //       this.successMessage = `Restaurant "${restaurant.name}" created successfully!`;

  //       if (this.selectedImage && restaurant.id) {
  //         this.uploadImage(restaurant.id);
  //       } else {
  //         this.isLoading = false;
  //         setTimeout(() => {
  //           this.resetForm();
  //         }, 2000);
  //       }
  //     },
  //     error: (error: HttpErrorResponse) => {
  //       this.isLoading = false;
  //       this.errorMessage = error.error?.message || 'Failed to create restaurant. Please try again.';
  //       console.error('Error creating restaurant:', error);
  //     }
  //   });
  // }

   onSubmit() {
    // Validation
    if (!this.restaurantData.name || !this.restaurantData.city) {
      this.errorMessage = 'Name and City are required fields.';
      return;
    }

    // Filter out empty food items
    const validFoodItems = this.foodItems.filter(item => 
      item.itemName && item.itemName.trim() !== '' && item.price > 0
    );

    // Prepare request data for Food Catalogue Service
    const requestData = {
      restaurant: this.restaurantData,
      foodItems: validFoodItems
    };

    console.log('Sending request to Food Catalogue Service:', requestData);

    this.isLoading = true;
    this.errorMessage = null;
    this.successMessage = null;

    // Call Food Catalogue Service (orchestrator)
    this.restaurantService.addRestaurantWithFoodItems(requestData).subscribe({
      next: (response) => {
        const restaurant = response.restaurant;
        console.log('Restaurant created:', restaurant);
        
        this.successMessage = `Restaurant "${restaurant.name}" created successfully!`;

        // Upload image if selected
        if (this.selectedImage && restaurant.id) {
          this.uploadImage(restaurant.id);
        } else {
          this.isLoading = false;
          setTimeout(() => {
            this.resetForm();
            this.router.navigate(['/']);
          }, 2000);
        }
      },
      error: (error: HttpErrorResponse) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Failed to create restaurant. Please try again.';
        console.error('Error creating restaurant:', error);
      }
    });
  }

  uploadImage(restaurantId: number) {
    if (this.selectedImage) {
      this.isImageUploading = true;
      console.log('Uploading image for restaurant ID:', restaurantId);
      console.log('Image file:', this.selectedImage.name, this.selectedImage.size, this.selectedImage.type);
      this.restaurantService.uploadRestaurantImage(restaurantId, this.selectedImage).subscribe({
        next: (imageUrl) => {
          this.isLoading = false;
          this.isImageUploading = false;
          this.successMessage = `Restaurant created successfully with image!`;
          console.log('Image uploaded successfully:', imageUrl);
          setTimeout(() => {
            this.resetForm();
            this.router.navigate(['/']);
          }, 2000);
        },
        error: (error: HttpErrorResponse) => {
          this.isLoading = false;
          this.isImageUploading = false;
          if (error.status === 200 || error.status === 201) {
            // If status is success, treat it as success even if error callback was triggered
            this.successMessage = `Restaurant created successfully with image!`;
            console.log('Image uploaded (success despite error callback)');
            setTimeout(() => {
              this.resetForm();
              this.router.navigate(['/']);
            }, 2000);
            return;
          }
          this.errorMessage = 'Restaurant created but image upload failed. You can update it later.';
          console.error('Error uploading image:', error);
          setTimeout(() => {
            this.resetForm();
            this.router.navigate(['/']);
          }, 3000);
        }
      });
    }
  }

  resetForm() {
    this.restaurantData = {
      name: '',
      address: '',
      city: '',
      restaurantDescription: ''
    };
    this.selectedImage = null;
    this.imagePreview = null;
    this.successMessage = null;
    this.errorMessage = null;
    this.isImageUploading = false;
    this.foodItems = [];
    this.showFoodItemsForm = false;
    this.isLoading = false;
  }

  goBack() {
    this.router.navigate(['/']);
  }

   addFoodItem() {
    this.foodItems.push({
      itemName: '',
      itemDescription: '',
      isVeg: false,
      price: 0,
      quantity: 1
    });
  }

  removeFoodItem(index: number) {
    this.foodItems.splice(index, 1);
    if (this.foodItems.length === 0) {
      this.showFoodItemsForm = false;
    }
  }

  toggleFoodItemsForm() {
    this.showFoodItemsForm = !this.showFoodItemsForm;
    if (this.showFoodItemsForm && this.foodItems.length === 0) {
      this.addFoodItem();
    }
  }
}
