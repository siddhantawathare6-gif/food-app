import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { AuthService } from '../service/AuthService';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ErrorDetails } from '../../shared/model/Auth';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

  emailOrUsername: string = '';
  password: string = '';
  errorMessage: string = '';
  successMessage: string = '';
  returnUrl: string = '/';
  returnParams: any = {};

  constructor(private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.returnUrl = params['returnUrl'] || '/';
      const { returnUrl, registered, ...rest } = params;
      this.returnParams = rest;

      if (registered) {
        this.successMessage = 'Registration successful! Please log in to continue.';
      }
    })
  }

  onLogin() {
    this.errorMessage = '';
    this.authService.login({ emailOrUsername: this.emailOrUsername, password: this.password }).subscribe({
      next: () => {
        this.router.navigate([this.returnUrl], { queryParams: this.returnParams });
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
        const errorDetails = err.error as ErrorDetails;
        this.errorMessage = errorDetails?.message || 'Registration failed. Please check your details and try again.';
        console.error('Registration failed:', err);
      }
    })
  }

  goToRegister() {
    this.router.navigate(['/register'], {
      queryParams: { returnUrl: this.returnUrl, ...this.returnParams }
    });
  }
}
