import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Location } from '@angular/common';
import { AuthService } from '../auth/service/AuthService';
import { UserService } from '../auth/service/UserService';

@Component({
  selector: 'app-profile',
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {

  // ===== PROPERTIES =====
  user: any = {
    id: null,
    name: '',
    username: '',
    email: '',
    mobileNumber: '',
    alternateMobileNumber: '',
    address: {
      addressLine1: '',
      addressLine2: '',
      city: '',
      state: '',
      pincode: '',
      country: ''
    }
  };

  isEditing = false;
  isLoading = true;
  isSaving = false;
  errorMessage = '';
  successMessage = '';
  previousUrl: string = '/';

  // ===== CONSTRUCTOR =====
  constructor(
    private authService: AuthService,
    private userService: UserService,
    private router: Router,
    private location: Location
  ) { }

  // ===== LIFECYCLE =====
  ngOnInit() {
    this.previousUrl = this.getPreviousUrl();
    this.loadUserProfile();
  }

  // ===== GET PREVIOUS URL =====
  private getPreviousUrl(): string {
    const navigation = this.router.getCurrentNavigation();
    if (navigation?.previousNavigation) {
      const previousUrl = navigation.previousNavigation.finalUrl?.toString();
      if (previousUrl && previousUrl !== '/profile') {
        return previousUrl;
      }
    }
    const returnUrl = this.router.url.split('?').find(param => param.startsWith('returnUrl='));
    if (returnUrl) {
      return returnUrl.split('=')[1];
    }
    return '/';
  }

  // ===== LOAD PROFILE =====
  loadUserProfile() {
    const userId = this.authService.getUserId();
    if (!userId) {
      this.router.navigate(['/login']);
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    
    this.userService.getUserProfile(userId).subscribe({
      next: (data) => {
        console.log('Profile data received:', data); // Debug log
        
        // Map the response to the user object
        this.user = {
          id: data.id || null,
          name: data.name || '',
          username: data.username || '',
          email: data.email || '',
          mobileNumber: data.mobileNumber || '',
          alternateMobileNumber: data.alternateMobileNumber || '',
          address: data.address || {
            addressLine1: '',
            addressLine2: '',
            city: '',
            state: '',
            pincode: '',
            country: ''
          }
        };
        
        this.isLoading = false;
        console.log('User object after mapping:', this.user); // Debug log
      },
      error: (error) => {
        this.errorMessage = 'Failed to load profile';
        this.isLoading = false;
        console.error('Error loading profile:', error);
      }
    });
  }

  // ===== TOGGLE EDIT =====
  toggleEdit() {
    this.isEditing = !this.isEditing;
    this.errorMessage = '';
    this.successMessage = '';
    if (!this.isEditing) {
      this.loadUserProfile();
    }
  }

  // ===== SAVE PROFILE =====
  saveProfile() {
    this.isSaving = true;
    this.errorMessage = '';
    this.successMessage = '';

    const updateData = {
      name: this.user.name,
      alternateMobileNumber: this.user.alternateMobileNumber,
      address: {
        addressLine1: this.user.address?.addressLine1,
        addressLine2: this.user.address?.addressLine2,
        city: this.user.address?.city,
        state: this.user.address?.state,
        pincode: this.user.address?.pincode,
        country: this.user.address?.country
      }
    };

    console.log('Update data being sent:', updateData); // Debug log

    this.userService.updateUserProfile(updateData).subscribe({
      next: (data) => {
        this.user = data;
        this.isSaving = false;
        this.isEditing = false;
        this.successMessage = 'Profile updated successfully!';
        setTimeout(() => {
          this.goBack();
        }, 1500);
      },
      error: (error) => {
        this.isSaving = false;
        this.errorMessage = error.error?.message || 'Failed to update profile.';
        console.error('Error updating profile:', error);
      }
    });
  }

  // ===== GO BACK =====
  goBack() {
    if (this.previousUrl && this.previousUrl !== '/profile') {
      this.router.navigate([this.previousUrl]);
    } else {
      this.location.back();
    }
  }
}