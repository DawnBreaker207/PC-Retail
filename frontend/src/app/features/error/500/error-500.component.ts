import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, AlertTriangle, Home } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';

@Component({
  selector: 'app-error-500',
  imports: [RouterLink, LucideAngularModule, HlmButtonDirective],
  templateUrl: './error-500.component.html',
})
export class Error500Component {
  readonly AlertTriangle = AlertTriangle;
  readonly Home = Home;
}

