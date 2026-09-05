import { Component, ElementRef, QueryList, ViewChildren } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
  isLoggedIn = false;

  @ViewChildren('cardEl', { read: ElementRef }) cards!: QueryList<ElementRef>;

  constructor(private authService: AuthService) {}

  ngOnInit() {
    this.isLoggedIn = this.authService.isLoggedIn();
  }

  login(): void {
    this.authService.login();
  }

  register(): void {
    this.authService.register();
  }

  onMouseMove(event: MouseEvent, index: number) {
    const card = this.cards?.toArray()[index]?.nativeElement;
    if (!card) return;
    const rect = card.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    const centerX = rect.width / 2;
    const centerY = rect.height / 2;
    const rotateX = ((y - centerY) / centerY) * -8;
    const rotateY = ((x - centerX) / centerX) * 8;
    card.style.transform =
      `perspective(800px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateY(-8px) scale(1.02)`;
  }

  onMouseLeave(index: number) {
    const card = this.cards?.toArray()[index]?.nativeElement;
    if (!card) return;
    card.style.transform = '';
  }
}
