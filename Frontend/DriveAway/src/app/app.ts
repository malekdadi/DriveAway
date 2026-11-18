import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { services } from './drive-page';

@Component({
  selector: 'app-root',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './app.html',
  host: { '[class.night-mode]': 'nightMode' },
})
export class App {
  protected readonly services = services;
  protected menuOpen = false;
  protected nightMode = this.readTheme();

  protected setTheme(night: boolean) {
    this.nightMode = night;
    try {
      localStorage.setItem('driveaway-theme', this.nightMode ? 'night' : 'day');
    } catch {
      // Keep the theme active for this visit if storage is unavailable.
    }
  }

  private readTheme() {
    try {
      return localStorage.getItem('driveaway-theme') === 'night';
    } catch {
      return false;
    }
  }

  protected closeMenu(button: HTMLButtonElement) {
    this.menuOpen = false;
    button.focus();
  }
}
