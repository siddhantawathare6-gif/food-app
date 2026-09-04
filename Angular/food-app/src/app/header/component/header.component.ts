import { Component } from '@angular/core';
import { AuthService } from '../../auth/service/AuthService';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-header',
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {

  username: string | null = null;

  constructor(private authService: AuthService) { }

  ngOnInit() {
    this.authService.currentUsername$.subscribe(name => {
      this.username = name;
      console.log('username', this.username);
    });
  }

  logout() {
    this.authService.logout();
  }

}
