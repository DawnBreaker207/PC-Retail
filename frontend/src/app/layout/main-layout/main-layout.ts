import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './header/header.component';
import { MegaMenuComponent } from './mega-menu/mega-menu.component';
import { FooterComponent } from './footer/footer.component';

@Component({
  selector: 'app-main-layout',
  host: { class: 'bg-bg dark:bg-bg-dark' },
  imports: [
    RouterOutlet,
    HeaderComponent,
    MegaMenuComponent,
    FooterComponent,
  ],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.css',
})
export class MainLayout {}
