import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-error-layout',
  imports: [RouterOutlet],
  templateUrl: './error-layout.component.html',
  changeDetection: ChangeDetectionStrategy.Eager,
  styleUrl: './error-layout.component.css',
})
export class ErrorLayoutComponent {}
