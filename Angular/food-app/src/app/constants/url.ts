// export const API_URL_RL ='http://localhost:9091';
// export const API_URL_Order ='http://localhost:9097';
// export const API_URL_FC ='http://localhost:9095';
// export const API_URL_UD ='http://localhost:9093';

//export const K8ExternalIp = 'http://k8s-default-awsingre-3d4b7f90d4-122545836.eu-west-3.elb.amazonaws.com';

// Development URLs (local)
export const API_URLS = {
  RESTAURANT_SERVICE: 'http://localhost:9091',
  ORDER_SERVICE: 'http://localhost:9097',
  FOOD_CATALOGUE: 'http://localhost:9095',
  USER_DETAILS: 'http://localhost:9093'
};

// Production URLs (Docker/K8s)
export const API_URLS_PROD = {
  RESTAURANT_SERVICE: 'http://restaurant-service:9091',
  ORDER_SERVICE: 'http://order-service:9097',
  FOOD_CATALOGUE: 'http://food-catalogue-service:9095',
  USER_DETAILS: 'http://user-info-service:9093'
};

// Kubernetes URLs
export const K8S_API_URLS = {
  RESTAURANT_SERVICE: 'http://k8s-default-awsir...',
  ORDER_SERVICE: 'http://k8s-default-awsir...',
  FOOD_CATALOGUE: 'http://k8s-default-awsir...',
  USER_DETAILS: 'http://k8s-default-awsir...'
};

// Type for API URLs
type ApiUrls = typeof API_URLS;

// Environment-based selection
export const getApiUrls = (): ApiUrls => {
  const hostname = window.location.hostname;
  
  if (hostname.includes('k8s') || hostname.includes('kubernetes')) {
    return K8S_API_URLS;
  }
  
  if (hostname !== 'localhost' && hostname !== '127.0.0.1') {
    return API_URLS_PROD;
  }
  
  return API_URLS;
};

// Helper function to get specific service URL
export const getServiceUrl = <K extends keyof ApiUrls>(service: K): string => {
  const urls = getApiUrls();
  return urls[service];
};

// Export individual service URLs for backward compatibility
export const API_URL_Order = getServiceUrl('ORDER_SERVICE');
export const API_URL_FC = getServiceUrl('FOOD_CATALOGUE');
export const API_URL_UD = getServiceUrl('USER_DETAILS');
export const API_URL_RL = getServiceUrl('RESTAURANT_SERVICE');