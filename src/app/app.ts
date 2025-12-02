// src/app/app.ts
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class AppComponent {
  title = 'Panel principal';
  isSidebarExpanded = false;

  onSidebarHover(expanded: boolean): void {
    this.isSidebarExpanded = expanded;
  }
}
