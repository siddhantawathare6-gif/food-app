import { Routes } from '@angular/router';
import { HeaderComponent } from './header/component/header.component';
import { RestaurantListingComponent } from './restaurant-listing/component/restaurant-listing.component';
import { FoodCatalogueComponent } from './food-catalogue/component/food-catalogue.component';
import { OrderSummaryComponent } from './order-summary/component/order-summary.component';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { AddRestaurantComponent } from './restaurant-listing/add-restaurant/add-restaurant.component';
import { EditRestaurantComponent } from './restaurant-listing/edit-restaurant/edit-restaurant.component';

export const routes: Routes = [
    { path: '', component: RestaurantListingComponent },
    { path: 'header', component: HeaderComponent },
    { path: 'food-catalogue/:id', component: FoodCatalogueComponent },
    { path: 'orderSummary', component: OrderSummaryComponent },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'restaurant/add', component: AddRestaurantComponent },
    { path: 'restaurant/edit/:id', component: EditRestaurantComponent }

];
