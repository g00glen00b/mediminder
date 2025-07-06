import {Component, inject, input} from '@angular/core';
import {MedicationTypeIconComponent} from '../medication-type-icon/medication-type-icon.component';
import {Router, RouterLink} from '@angular/router';
import {Medication} from '../../models/medication';
import {MatIconAnchor, MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {ListItemComponent} from '../../../shared/components/list-item/list-item.component';
import {ListItemTitleDirective} from '../../../shared/components/list-item/list-item-title.directive';
import {ListItemDescriptionDirective} from '../../../shared/components/list-item/list-item-description.directive';
import {ListItemIconDirective} from '../../../shared/components/list-item/list-item-icon.directive';
import {ListItemActionsDirective} from '../../../shared/components/list-item/list-item-actions.directive';

@Component({
  selector: 'mediminder-medication-list-item',
  imports: [
    MedicationTypeIconComponent,
    MatIconAnchor,
    RouterLink,
    MatIcon,
    ListItemComponent,
    ListItemTitleDirective,
    ListItemDescriptionDirective,
    ListItemIconDirective,
    ListItemActionsDirective,
    MatIconButton,
  ],
  templateUrl: './medication-list-item.component.html',
  styleUrl: './medication-list-item.component.scss'
})
export class MedicationListItemComponent {
  private readonly router = inject(Router);
  medication = input.required<Medication>();

  navigateToDetailPage() {
    this.router.navigate(['/medication', this.medication().id]);
  }
}
