import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzResultModule } from 'ng-zorro-antd/result';

@Component({
  selector: 'app-error-403',
  imports: [RouterLink, NzButtonModule, NzResultModule],
  templateUrl: './error-403.component.html',
})
export class Error403Component {}
