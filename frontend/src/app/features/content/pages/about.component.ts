import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';

@Component({
  selector: 'app-about',
  imports: [RouterLink, NzBreadCrumbModule],
  templateUrl: './about.component.html',
})
export class AboutComponent {}
