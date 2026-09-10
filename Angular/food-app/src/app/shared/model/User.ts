export interface Address {
    addressLine1?: string;
    addressLine2?: string;
    city?: string;
    state?: string;
    pincode?: string;
    country?: string;
}

export interface ContactInfo {
    mobileNumber?: string;
    alternateMobileNumber?: string;
    email?: string;
}

export interface User {
    id: number;
    name: string;
    username: string;
    contactInfo?: ContactInfo;
    address?: Address;
    createdAt?: Date;
    updatedAt?: Date;
    roles?: string[];
}