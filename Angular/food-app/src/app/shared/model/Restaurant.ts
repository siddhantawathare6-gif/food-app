export interface Restaurant {
    id: number;
    name: string;
    address: string ;
    city: string;
    restaurantDescription: string;
    imageUrl?: string;
    rating?: number;
    reviewCount?: number;
}