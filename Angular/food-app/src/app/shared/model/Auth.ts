export interface RegisterDTO {
  name: string;
  username: string;
  email: string;
  password: string;
}

export interface LoginDTO {
  emailOrUsername: string;
  password: string;
}

export interface JwtAuthResponse {
  accessToken: string;
  tokenType: string;
  userId: number;

}

export interface ErrorDetails {
  timestamp: string;
  message: string;
  details: string;
}