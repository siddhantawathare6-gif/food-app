export interface Restaurant {
    id: number;
    name: string;
    address: string ;
    city: string;
    restaurantDescription: string;
    imageName?: string;      
    rating?: number;        
    reviewCount?: number; 
}