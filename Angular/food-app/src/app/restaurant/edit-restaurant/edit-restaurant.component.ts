import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../auth/service/AuthService';
import { RestaurantService } from '../service/restaurant.service';
import { HttpErrorResponse } from '@angular/common/http';
import { API_URL_RL } from '../../constants/url';

@Component({
  selector: 'app-edit-restaurant',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './edit-restaurant.component.html',
  styleUrl: './edit-restaurant.component.css'
})
export class EditRestaurantComponent implements OnInit {

  restaurantId!: number;
  
  // Restaurant Data
  restaurantData = {
    name: '',
    address: '',
    city: '',
    restaurantDescription: ''
  };

  // Food Items
  foodItems: any[] = [];
  showFoodItemsForm = true;

  // Image
  selectedImage: File | null = null;
  imagePreview: string | null = null;
  currentImageUrl: string | null = null;

  // UI State
  isLoading = false;
  isImageUploading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  isDataLoaded = false;

  constructor(
    private restaurantService: RestaurantService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit() {
    // Redirect if not admin
    if (!this.authService.isAdmin()) {
      this.router.navigate(['/']);
      return;
    }

    // Get restaurant ID from route
    this.route.params.subscribe(params => {
      this.restaurantId = +params['id'];
      if (this.restaurantId) {
        this.loadRestaurantData();
      } else {
        this.router.navigate(['/']);
      }
    });
  }

  // ========== LOAD RESTAURANT DATA ==========

  loadRestaurantData() {
    this.isLoading = true;
    this.errorMessage = null;

    // Get restaurant with food items from Food Catalogue Service
    this.restaurantService.getRestaurantWithFoodItems(this.restaurantId).subscribe({
      next: (response) => {
        console.log('Restaurant data loaded:', response);
        
        // Set restaurant data
        this.restaurantData = {
          name: response.restaurant?.name || '',
          address: response.restaurant?.address || '',
          city: response.restaurant?.city || '',
          restaurantDescription: response.restaurant?.restaurantDescription || ''
        };

        // Set food items
        this.foodItems = response.foodItemsList || [];
        if (this.foodItems.length === 0) {
          this.addFoodItem();
        }

        // Set current image URL
        this.currentImageUrl = this.getImageUrl(this.restaurantId);

        this.isDataLoaded = true;
        this.isLoading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Failed to load restaurant data. Please try again.';
        console.error('Error loading restaurant:', error);
      }
    });
  }

  // ========== FOOD ITEMS MANAGEMENT ==========

  addFoodItem() {
    this.foodItems.push({
      id: null,
      itemName: '',
      itemDescription: '',
      isVeg: false,
      price: 0,
      restaurantId: this.restaurantId,
      quantity: 1
    });
  }

  removeFoodItem(index: number) {
    this.foodItems.splice(index, 1);
    if (this.foodItems.length === 0) {
      this.addFoodItem();
    }
  }

  toggleFoodItemsForm() {
    this.showFoodItemsForm = !this.showFoodItemsForm;
  }

  // ========== IMAGE MANAGEMENT ==========

  getImageUrl(restaurantId: number): string {
    return `${API_URL_RL}/restaurant/image/${restaurantId}`;
  }

  onImageError(event: Event) {
    const img = event.target as HTMLImageElement;
    img.src = `${API_URL_RL}/restaurant/image/default`;
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

  // ========== FORM SUBMISSION ==========

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

    if (validFoodItems.length === 0) {
      this.errorMessage = 'Please add at least one food item with name and price.';
      return;
    }

    // Prepare request data for Food Catalogue Service
    const requestData = {
      restaurant: this.restaurantData,
      foodItems: validFoodItems
    };

    console.log('Updating restaurant with food items:', requestData);

    this.isLoading = true;
    this.errorMessage = null;
    this.successMessage = null;

    // Call Food Catalogue Service (orchestrator)
    this.restaurantService.updateRestaurantWithFoodItems(this.restaurantId, requestData).subscribe({
      next: (response) => {
        console.log('Restaurant updated:', response);
        
        this.successMessage = `Restaurant "${response.restaurant.name}" updated successfully!`;

        // Upload new image if selected
        if (this.selectedImage) {
          this.uploadImage();
        } else {
          this.isLoading = false;
          setTimeout(() => {
            this.router.navigate(['/']);
          }, 2000);
        }
      },
      error: (error: HttpErrorResponse) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Failed to update restaurant. Please try again.';
        console.error('Error updating restaurant:', error);
      }
    });
  }

  // ========== IMAGE UPLOAD ==========

  uploadImage() {
    if (this.selectedImage) {
      this.isImageUploading = true;
      console.log('Uploading image for restaurant ID:', this.restaurantId);
      
      this.restaurantService.uploadRestaurantImage(this.restaurantId, this.selectedImage).subscribe({
        next: (imageUrl) => {
          this.isLoading = false;
          this.isImageUploading = false;
          this.successMessage = 'Restaurant updated successfully with image!';
          console.log('Image uploaded successfully:', imageUrl);
          setTimeout(() => {
            this.router.navigate(['/']);
          }, 2000);
        },
        error: (error: HttpErrorResponse) => {
          this.isLoading = false;
          this.isImageUploading = false;
          
          if (error.status === 200 || error.status === 201) {
            this.successMessage = 'Restaurant updated successfully with image!';
            console.log('Image uploaded successfully (status 200/201)');
            setTimeout(() => {
              this.router.navigate(['/']);
            }, 2000);
            return;
          }
          
          this.errorMessage = 'Restaurant updated but image upload failed. You can update it later.';
          console.error('Error uploading image:', error);
          setTimeout(() => {
            this.router.navigate(['/']);
          }, 3000);
        }
      });
    }
  }

  // ========== UTILITY METHODS ==========

  goBack() {
    this.router.navigate(['/']);
  }

  cancelEdit() {
    if (confirm('Are you sure you want to cancel? Your changes will be lost.')) {
      this.router.navigate(['/']);
    }
  }
}