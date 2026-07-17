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
  citizenId: string = '';
  isDarkTheme = false;

  private authSub!: Subscription;

  constructor(private authService: AuthService) {}

  ngOnInit() {
    // Subscribe so the header reacts immediately whenever a new citizen
    // registers or logs in — no more stale citizenId in the navbar link.
    this.authSub = this.authService.citizenId$.subscribe(id => {
      this.citizenId = id;
    });

    const savedTheme = localStorage.getItem('theme');
    const prefersDark = window.matchMedia?.('(prefers-color-scheme: dark)').matches;
    this.isDarkTheme = savedTheme ? savedTheme === 'dark' : prefersDark;
    this.applyTheme();
  }

  ngOnDestroy() {
    this.authSub?.unsubscribe();
  }

  toggleTheme() {
    this.isDarkTheme = !this.isDarkTheme;
    localStorage.setItem('theme', this.isDarkTheme ? 'dark' : 'light');
    this.applyTheme();
  }

  private applyTheme() {
    document.documentElement.setAttribute('data-theme', this.isDarkTheme ? 'dark' : 'light');
  }
}
