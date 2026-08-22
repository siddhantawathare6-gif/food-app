import { FoodItem } from "../../shared/model/FoodItem";
import { Restaurant } from "../../shared/model/Restaurant";

export interface OrderDTO {

    foodItemsList: FoodItem[];
    userId: number;
    restaurant: Restaurant | null;
}