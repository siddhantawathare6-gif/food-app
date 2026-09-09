import { Component, ElementRef, HostListener } from '@angular/core';
import { AuthService } from '../../auth/service/AuthService';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-header',
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {

  username: string | null = null;
  isDropdownOpen: boolean = false;

  constructor(private authService: AuthService,
    private router: Router,
    private elementRef: ElementRef
  ) { }

  ngOnInit() {
    this.authService.currentUsername$.subscribe(name => {
      this.username = name;
      console.log('username', this.username);
    });

  }

  // Get avatar image with fallback
  getAvatarImage(): string {
    // If user is logged in, use avatar.jpg
    if (this.username) {
      return 'assets/avatar-pics/avatar.jpg';
    }
    // Default avatar for guest
    return 'assets/avatar-pics/default-avatar.png';
  }

  // Handle image load error
  onAvatarError(event: Event) {
    const img = event.target as HTMLImageElement;
    // White background with dark user icon as fallback
    img.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="100" height="100"%3E%3Crect width="100" height="100" fill="%23ffffff" rx="50"/%3E%3Ctext x="50" y="55" font-size="45" text-anchor="middle" fill="%232c3e50"%3E👤%3C/text%3E%3C/svg%3E';
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
    // this.router.navigate(['/profile']);
  }

  navigateToOrders() {
    this.isDropdownOpen = false;
    // Navigate to orders page when implemented
    // this.router.navigate(['/orders']);
  }

  navigateToFavorites() {
    this.isDropdownOpen = false;
    // Navigate to favorites page when implemented
    // this.router.navigate(['/favorites']);
  }

}
