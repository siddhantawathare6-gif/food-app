import { Routes } from '@angular/router';
import { HeaderComponent } from './header/component/header.component';
import { FoodCatalogueComponent } from './food-catalogue/component/food-catalogue.component';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { AddRestaurantComponent } from './restaurant/add-restaurant/add-restaurant.component';
import { EditRestaurantComponent } from './restaurant/edit-restaurant/edit-restaurant.component';
import { RestaurantListingComponent } from './restaurant/restaurant-listing/restaurant-listing.component';
import { ProfileComponent } from './profile/profile.component';
import { OrderComponent } from './order-summary/order/order.component';
import { OrderSummaryComponent } from './order-summary/order-summary/order-summary.component';

export const routes: Routes = [
    { path: '', component: RestaurantListingComponent },
    { path: 'header', component: HeaderComponent },
    { path: 'food-catalogue/:id', component: FoodCatalogueComponent },
    { path: 'orderSummary', component: OrderSummaryComponent },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'restaurant/add', component: AddRestaurantComponent },
    { path: 'restaurant/edit/:id', component: EditRestaurantComponent },
    { path: 'profile', component: ProfileComponent },
    { path: 'orders', component: OrderComponent }

];
