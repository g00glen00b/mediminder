import {Component, computed, inject, input} from '@angular/core';
import {
  FormatDistanceToNowPurePipeModule,
  FormatPipeModule,
  ParseIsoPipeModule,
  ParsePurePipeModule
} from 'ngx-date-fns';
import {CabinetEntry} from '../../models/cabinet-entry';
import {differenceInDays} from 'date-fns';
import {DecimalPipe} from '@angular/common';
import {MatIcon} from '@angular/material/icon';
import {MatIconAnchor} from '@angular/material/button';
import {Router, RouterLink} from '@angular/router';
import {ListItemComponent} from '../../../shared/components/list-item/list-item.component';
import {ListItemTitleDirective} from '../../../shared/components/list-item/list-item-title.directive';
import {ListItemDescriptionDirective} from '../../../shared/components/list-item/list-item-description.directive';
import {ListItemActionsDirective} from '../../../shared/components/list-item/list-item-actions.directive';
import {ListItemIconDirective} from '../../../shared/components/list-item/list-item-icon.directive';
import {TagComponent} from '../../../shared/components/tag/tag.component';

@Component({
  selector: 'mediminder-cabinet-entry-list-item',
  imports: [
    FormatDistanceToNowPurePipeModule,
    FormatPipeModule,
    ParseIsoPipeModule,
    DecimalPipe,
    MatIcon,
    MatIconAnchor,
    ParsePurePipeModule,
    RouterLink,
    ListItemComponent,
    ListItemTitleDirective,
    ListItemDescriptionDirective,
    ListItemActionsDirective,
    ListItemIconDirective,
    TagComponent,
  ],
  templateUrl: './cabinet-entry-list-item.component.html',
  styleUrl: './cabinet-entry-list-item.component.scss'
})
export class CabinetEntryListItemComponent {
  private readonly router = inject(Router);
  entry = input.required<CabinetEntry>();
  expiresSoon = computed(() => differenceInDays(this.entry().expiryDate, new Date()) <= 7);
  expiresSoonColor = computed(() => this.expiresSoon() ? 'warning' : 'default');

  navigateToCabinetEntryEdit() {
    this.router.navigate(['/medication', this.entry().medication.id, 'cabinet', this.entry().id, 'edit']);
  }
}
