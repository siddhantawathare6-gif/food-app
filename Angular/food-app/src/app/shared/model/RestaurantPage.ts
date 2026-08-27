import { Restaurant } from "./Restaurant";

export interface RestaurantPage {
  restaurantList: Restaurant[];
  pageNo: number;
  pageSize: number;
  totalElement: number;
  totalPage: number;
  last: boolean;
}