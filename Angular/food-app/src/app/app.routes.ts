import { Routes } from '@angular/router';
import { HeaderComponent } from './header/component/header.component';
import { RestaurantListingComponent } from './restaurant-listing/component/restaurant-listing.component';
import { FoodCatalogueComponent } from './food-catalogue/component/food-catalogue.component';
import { OrderSummaryComponent } from './order-summary/component/order-summary.component';

export const routes: Routes = [
    { path: '', component: RestaurantListingComponent },
    { path: 'header', component: HeaderComponent },
    { path: 'food-catalogue/:id', component: FoodCatalogueComponent },
    { path: 'orderSummary', component: OrderSummaryComponent }

];
