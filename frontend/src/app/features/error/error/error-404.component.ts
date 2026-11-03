import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, FileQuestion, Home } from 'lucide-angular';
import { HlmButtonDirective } from '@shared/ui/button';

@Component({
  selector: 'app-error-404',
  imports: [RouterLink, LucideAngularModule, HlmButtonDirective],
  templateUrl: './error-404.component.html',
})
export class Error404Component {
  readonly FileQuestion = FileQuestion;
  readonly Home = Home;
}

