import { Component } from '@angular/core';
import { FoodCataloguePage } from '../../shared/model/FoodCataloguePage';
import { FoodItem } from '../../shared/model/FoodItem';
import { ActivatedRoute, Router } from '@angular/router';
import { FoodItemService } from '../service/fooditem.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-food-catalogue',
  imports: [CommonModule],
  templateUrl: './food-catalogue.component.html',
  styleUrl: './food-catalogue.component.css'
})
export class FoodCatalogueComponent {


  restaurantId!: number;
  foodItemResponse!: FoodCataloguePage;
  foodItemCart: FoodItem[] = [];
  orderSummary: FoodCataloguePage;


  constructor(private route: ActivatedRoute, private foodItemService: FoodItemService, private router: Router) {
  }

  ngOnInit() {

    this.route.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (id) {
        this.restaurantId = Number(id);
        this.getFoodItemsByRestaurant(this.restaurantId);
      }
    });

  }

  getFoodItemsByRestaurant(restaurant: number) {
    this.foodItemService.getFoodItemsByRestaurant(restaurant).subscribe(
      data => {
        console.log('Food Catalogue API Response:', data);

        this.foodItemResponse = data;
        // Initialize cart with already selected items
        this.foodItemCart = data.foodItemsList
          .filter(food => food.quantity > 0);
        console.log('Food Items:', data.foodItemsList);
        console.log('Initial Cart:', this.foodItemCart);
      }
    )
  }

  increment(food: FoodItem) {
    food.quantity++;
    const index = this.foodItemCart.findIndex(item => item.id === food.id);
    if (index === -1) {
      // If record does not exist, add it to the array
      this.foodItemCart.push(food);
    } else {
      // If record exists, update it in the array
      this.foodItemCart[index] = food;
    }
    console.log('Cart:', this.foodItemCart);

  }

  decrement(food: FoodItem) {

    if (food.quantity <= 0) {
      return;
    }

    food.quantity--;

    const index = this.foodItemCart.findIndex(
      item => item.id === food.id
    );

    if (index === -1) {
      return;
    }
    if (food.quantity === 0) {
      this.foodItemCart.splice(index, 1);
    } else {
      this.foodItemCart[index] = food;
    }
    console.log('Cart:', this.foodItemCart);
  }

  onCheckOut() {

    console.log('================ CHECKOUT ================');
    console.log('Cart before checkout:', this.foodItemCart);

    if (this.foodItemCart.length === 0) {
      alert('Please select at least one food item');
      return;
    }

    const orderSummary = {
      foodItemsList: this.foodItemCart,
      restaurant: this.foodItemResponse.restaurant
    };

    console.log('Order Summary before navigation:', orderSummary);

    const jsonData = JSON.stringify(orderSummary);

    console.log('JSON Data:', jsonData);

    this.router.navigate(['/orderSummary'], { queryParams: { data: jsonData } });
  }

}
