import { Component, OnDestroy, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit, OnDestroy {
  isLoggedIn = false;
  citizenId = '';
  isDarkTheme = false;

  private authSub?: Subscription;

  constructor(private authService: AuthService) {}

  ngOnInit() {
    this.refreshAuthState();

    this.authSub = this.authService.citizenId$.subscribe(() => {
      this.citizenId = this.authService.citizenId;
      this.isLoggedIn = this.authService.isLoggedIn();
    });

    const savedTheme = localStorage.getItem('theme');
    const prefersDark = window.matchMedia?.('(prefers-color-scheme: dark)').matches;
    this.isDarkTheme = savedTheme ? savedTheme === 'dark' : prefersDark;
    this.applyTheme();
  }

  ngOnDestroy() {
    this.authSub?.unsubscribe();
  }

  login(): void {
    this.authService.login();
  }

  register(): void {
    this.authService.register();
  }

  logout(): void {
    this.authService.logout();
  }

  toggleTheme() {
    this.isDarkTheme = !this.isDarkTheme;
    localStorage.setItem('theme', this.isDarkTheme ? 'dark' : 'light');
    this.applyTheme();
  }

  private refreshAuthState(): void {
    this.isLoggedIn = this.authService.isLoggedIn();
    this.citizenId = this.authService.citizenId;
  }

  private applyTheme() {
    document.documentElement.setAttribute('data-theme', this.isDarkTheme ? 'dark' : 'light');
  }
}
