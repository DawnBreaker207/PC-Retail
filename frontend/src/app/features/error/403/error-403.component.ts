import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, ShieldAlert, Home } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';

@Component({
  selector: 'app-error-403',
  imports: [RouterLink, LucideAngularModule, HlmButtonDirective],
  templateUrl: './error-403.component.html',
})
export class Error403Component {
  readonly ShieldAlert = ShieldAlert;
  readonly Home = Home;
}

