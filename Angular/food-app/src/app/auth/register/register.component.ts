import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { AuthService } from '../service/AuthService';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ErrorDetails } from '../../shared/model/Auth';

@Component({
  selector: 'app-register',
  imports: [CommonModule, FormsModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {

  name: string = '';
  username: string = '';
  email: string = '';
  password: string = '';
  errorMessage: string = '';
  returnUrl: string = '/';
  returnParams: any = {};

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.returnUrl = params['returnUrl'] || '/';
      const { returnUrl, ...rest } = params;
      this.returnParams = rest;
    });
  }

  onRegister() {
    this.errorMessage = '';
    this.authService.register({
      name: this.name,
      username: this.username,
      email: this.email,
      password: this.password
    }).subscribe({
      next: () => {
        // Registration succeeded but does NOT log the user in — send them to login next
        this.router.navigate(['/login'], {
          queryParams: { returnUrl: this.returnUrl, registered: 'true', ...this.returnParams }
        });
      },
      // error: (err) => {
      //   if (err.error && err.error.message) {
      //     this.errorMessage = err.error.message;
      //   } else {
      //     this.errorMessage = 'Registration failed. Please check your details and try again.';
      //   }
      //   console.error('Registration failed:', err);
      // },
      error: (err: HttpErrorResponse) => {
        let errorDetails: ErrorDetails | null = null;
        try {
          errorDetails = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
        } catch {
          errorDetails = null;
        }
        this.errorMessage = errorDetails?.message || 'Registration failed. Please check your details and try again.';
        console.error('Registration failed:', err);
      }
    });
  }

  goToLogin() {
    this.router.navigate(['/login'], {
      queryParams: { returnUrl: this.returnUrl, ...this.returnParams }
    });
  }

}
