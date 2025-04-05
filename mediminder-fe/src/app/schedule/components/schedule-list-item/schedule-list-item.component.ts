import {Component, inject, input} from '@angular/core';
import {DecimalPipe} from '@angular/common';
import {FormatPipeModule, ParsePurePipeModule} from 'ngx-date-fns';
import {IntervalPipe} from '../../pipes/interval.pipe';
import {MatIcon} from '@angular/material/icon';
import {MatIconAnchor} from '@angular/material/button';
import {Schedule} from '../../models/schedule';
import {Router, RouterLink} from '@angular/router';
import {ListItemComponent} from '../../../shared/components/list-item/list-item.component';
import {ListItemIconDirective} from '../../../shared/components/list-item/list-item-icon.directive';
import {ListItemTitleDirective} from '../../../shared/components/list-item/list-item-title.directive';
import {ListItemDescriptionDirective} from '../../../shared/components/list-item/list-item-description.directive';
import {ListItemActionsDirective} from '../../../shared/components/list-item/list-item-actions.directive';

@Component({
  selector: 'mediminder-schedule-list-item',
  imports: [
    DecimalPipe,
    FormatPipeModule,
    IntervalPipe,
    MatIcon,
    MatIconAnchor,
    ParsePurePipeModule,
    RouterLink,
    ListItemComponent,
    ListItemIconDirective,
    ListItemTitleDirective,
    ListItemDescriptionDirective,
    ListItemActionsDirective,
  ],
  templateUrl: './schedule-list-item.component.html',
  styleUrl: './schedule-list-item.component.scss'
})
export class ScheduleListItemComponent {
  private readonly router = inject(Router);
  schedule = input.required<Schedule>();

  navigateToScheduleEdit() {
    this.router.navigate(['/medication', this.schedule().medication.id, 'schedule', this.schedule().id, 'edit']);
  }
}
