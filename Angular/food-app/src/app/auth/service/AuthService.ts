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
    roles?: any[];
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {


    private baseUrl = getServiceUrl('USER_DETAILS');

    // Broadcasts the current username (or null if logged out) to anyone listening
    private currentUsernameSubject = new BehaviorSubject<string | null>(this.getStoredUsername());
    currentUsername$: Observable<string | null> = this.currentUsernameSubject.asObservable();

    // Add BehaviorSubject for roles
    private userRolesSubject = new BehaviorSubject<string[]>(this.getStoredRoles());
    userRoles$: Observable<string[]> = this.userRolesSubject.asObservable();

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
            console.log('===== DECODED JWT TOKEN =====');
            console.log('Full decoded token:', decoded);
            console.log('All claims:', Object.keys(decoded));
            console.log('Roles from token:', decoded.roles);
            console.log('==============================');

            localStorage.setItem('username', decoded.sub);

            // Extract roles properly - handle both string and object formats
            let roles: string[] = [];

            if (decoded.roles && Array.isArray(decoded.roles)) {
                roles = decoded.roles.map((role: any) => {
                    // If role is a string, use it directly
                    if (typeof role === 'string') {
                        return role;
                    }
                    // If role is an object with authority property (Spring Boot format)
                    if (role && typeof role === 'object' && role.authority) {
                        return role.authority;
                    }
                    // If role is an object with role property
                    if (role && typeof role === 'object' && role.role) {
                        return role.role;
                    }
                    // Fallback: convert to string
                    return String(role);
                }).filter(role => role && role !== ''); // Remove empty roles
            }
            console.log('Extracted roles as strings:', roles);

            localStorage.setItem('userRoles', JSON.stringify(roles));
            this.userRolesSubject.next(roles);

            this.currentUsernameSubject.next(decoded.sub);
            console.log('Token stored:', localStorage.getItem('authToken'));
            console.log('User ID:', localStorage.getItem('userId'));
            console.log('User Roles:', roles);
            console.log('==============================');

        } catch (error) {
            console.error('Failed to decode JWT:', error);

        }
    }

    logout() {
        localStorage.removeItem('authToken');
        localStorage.removeItem('username');
        localStorage.removeItem('userId');
        localStorage.removeItem('userRoles'); // Remove roles on logout
        this.currentUsernameSubject.next(null);
        this.userRolesSubject.next([]);
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

    // NEW: Get stored roles
    private getStoredRoles(): string[] {
        const roles = localStorage.getItem('userRoles');
        return roles ? JSON.parse(roles) : [];
    }

    // NEW: Check if user has admin role
    isAdmin(): boolean {
        const roles = this.getStoredRoles();
        console.log('isAdmin() - Checking roles:', roles);
        const isAdmin = roles.includes('ROLE_ADMIN');
        console.log('isAdmin() - Result:', isAdmin);
        return isAdmin;
    }

    // NEW: Check if user has a specific role
    hasRole(role: string): boolean {
        const roles = this.getStoredRoles();
        return roles.includes(role);
    }

    // NEW: Get user roles
    getUserRoles(): string[] {
        return this.getStoredRoles();
    }

    private getStoredUsername(): string | null {
        return this.isLoggedIn() ? localStorage.getItem('username') : null;
    }
}