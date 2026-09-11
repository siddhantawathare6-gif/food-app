import { Component, ElementRef, HostListener } from '@angular/core';
import { AuthService } from '../../auth/service/AuthService';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { API_URL_UD } from '../../constants/url';

@Component({
  selector: 'app-header',
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {

  username: string | null = null;
  isDropdownOpen: boolean = false;
  avatarVersion: number = 0;

  private usernameSub: Subscription | null = null;
  private avatarVersionSub: Subscription | null = null;

  constructor(private authService: AuthService,
    private router: Router,
    private elementRef: ElementRef
  ) { }

  ngOnInit() {
    this.authService.currentUsername$.subscribe(name => {
      this.username = name;
      console.log('username', this.username);
    });
    this.avatarVersionSub = this.authService.avatarVersion$.subscribe(v => {
      this.avatarVersion = v;
    });

  }

  ngOnDestroy() {
    this.usernameSub?.unsubscribe();
    this.avatarVersionSub?.unsubscribe();
  }

  // Get avatar image with fallback
  getAvatarImage(): string {
    const userId = this.authService.getUserId();

    // 1. Not logged in → local default guest avatar
    if (!userId) {
      return 'assets/avatar-pics/default-avatar.jpg';
    }

    // 2. Logged in → backend endpoint
    //    Backend returns avatar.jpg if no upload, else the uploaded file
    return `${API_URL_UD}/user/image/${userId}?v=${this.avatarVersion}`;
  }

  // Handle image load error
  onAvatarError(event: Event) {
    const img = event.target as HTMLImageElement;
    const userId = this.authService.getUserId();

    if (userId) {
      img.src = 'assets/avatar-pics/avatar.jpg';          // logged-in fallback
    } else {
      img.src = 'assets/avatar-pics/default-avatar.jpg';  // guest fallback
    }
  }

  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  closeDropdown() {
    this.isDropdownOpen = false;
  }

  goHome() {
    this.router.navigate(['/']);
    this.isDropdownOpen = false;
  }

  // ===== CLICK OUTSIDE DETECTION =====
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    const clickedInside = this.elementRef.nativeElement.contains(target);

    if (!clickedInside && this.isDropdownOpen) {
      this.isDropdownOpen = false;
    }
  }

  logout() {
    this.authService.logout();
    this.isDropdownOpen = false;
    this.router.navigate(['/']);
  }

  navigateToLogin() {
    this.isDropdownOpen = false;
    this.router.navigate(['/login']);
  }

  navigateToRegister() {
    this.isDropdownOpen = false;
    this.router.navigate(['/register']);
  }

  navigateToProfile() {
    this.isDropdownOpen = false;
    // Navigate to profile page when implemented
    this.router.navigate(['/profile']);
  }

  navigateToOrders() {
    this.isDropdownOpen = false;
    // Navigate to orders page when implemented
    this.router.navigate(['/orders']);
  }

  navigateToFavorites() {
    this.isDropdownOpen = false;
    // Navigate to favorites page when implemented
    // this.router.navigate(['/favorites']);
  }

}
