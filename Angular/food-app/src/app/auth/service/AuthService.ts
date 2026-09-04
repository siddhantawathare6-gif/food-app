import { Injectable } from "@angular/core";
import { getServiceUrl } from "../../constants/url";
import { HttpClient } from "@angular/common/http";
import { BehaviorSubject, Observable, tap } from "rxjs";
import { JwtAuthResponse, LoginDTO, RegisterDTO } from "../../shared/model/Auth";
import { jwtDecode } from 'jwt-decode';

interface DecodedToken {
    sub: string;        // typically username or email — confirm against your backend's JWT claims
    userId?: number;     // only present if your backend actually includes this claim
    exp: number;
    [key: string]: any;
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {


    private baseUrl = getServiceUrl('USER_DETAILS');

    // Broadcasts the current username (or null if logged out) to anyone listening
    private currentUsernameSubject = new BehaviorSubject<string | null>(this.getStoredUsername());
    currentUsername$: Observable<string | null> = this.currentUsernameSubject.asObservable();


    constructor(private http: HttpClient) { }

    login(request: LoginDTO): Observable<JwtAuthResponse> {
        return this.http.post<JwtAuthResponse>(`${this.baseUrl}/api/auth/login`, request)
            .pipe(
                tap(response => this.storeSession(response)));
    }

    register(request: RegisterDTO): Observable<String> {
        return this.http.post(`${this.baseUrl}/api/auth/register`, request, { responseType: 'text' });
    }

    private storeSession(response: JwtAuthResponse) {
        localStorage.setItem('authToken', response.accessToken);
        localStorage.setItem('userId', String(response.userId));
        try {
            const decoded = jwtDecode<DecodedToken>(response.accessToken);
            localStorage.setItem('username', decoded.sub);
            this.currentUsernameSubject.next(decoded.sub);
            console.log(localStorage.getItem('authToken'));
            console.log(localStorage.getItem('userId'));
        } catch (error) {
            console.error('Failed to decode JWT:', error);

        }
    }

    logout() {
        localStorage.removeItem('authToken');
        localStorage.removeItem('username');
        localStorage.removeItem('userId');
    }

    isLoggedIn(): boolean {
        const token = localStorage.getItem('authToken');
        if (!token) return false;
        try {
            const decoded = jwtDecode<DecodedToken>(token);
            const isExpired = decoded.exp * 1000 < Date.now();
            return !isExpired;
        } catch {
            return false;
        }
    }

    getToken(): string | null {
        return localStorage.getItem('authToken');
    }

    getUserId(): number | null {
        const id = localStorage.getItem('userId');
        return id ? Number(id) : null;
    }

    getUsername(): string | null {
        return localStorage.getItem('username');
    }

    private getStoredUsername(): string | null {
        return this.isLoggedIn() ? localStorage.getItem('username') : null;
    }
}