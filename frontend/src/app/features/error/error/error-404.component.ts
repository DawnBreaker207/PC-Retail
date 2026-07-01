import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzResultModule } from 'ng-zorro-antd/result';

@Component({
  selector: 'app-error-404',
  imports: [RouterLink, NzButtonModule, NzResultModule],
  templateUrl: './error-404.component.html',
})
export class Error404Component {}
