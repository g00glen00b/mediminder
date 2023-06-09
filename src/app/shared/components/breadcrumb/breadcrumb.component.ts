import {Component, Input} from '@angular/core';
import {Breadcrumb} from "../../models/breadcrumb";

@Component({
  selector: 'mediminder-breadcrumb',
  templateUrl: './breadcrumb.component.html',
  styleUrls: ['./breadcrumb.component.scss']
})
export class BreadcrumbComponent {
  @Input()
  breadcrumbs: Breadcrumb[] = [];
}
