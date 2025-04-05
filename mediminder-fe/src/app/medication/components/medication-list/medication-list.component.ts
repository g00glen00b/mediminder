import {Component, inject, input, output} from '@angular/core';
import {MatExpansionModule} from '@angular/material/expansion';
import {FormatPipeModule, ParseIsoPipeModule} from 'ngx-date-fns';
import {MatListModule} from '@angular/material/list';
import {MatButtonModule} from '@angular/material/button';
import {Router, RouterLink} from '@angular/router';
import {Medication} from '../../models/medication';
import {MatCard, MatCardContent} from '@angular/material/card';
import {MedicationTypeIconComponent} from '../medication-type-icon/medication-type-icon.component';
import {MatIcon} from '@angular/material/icon';

@Component({
    selector: 'mediminder-medication-list',
  imports: [
    MatExpansionModule,
    MatListModule,
    MatButtonModule,
    ParseIsoPipeModule,
    FormatPipeModule,
    RouterLink,
    MatCard,
    MedicationTypeIconComponent,
    MatIcon,
    MatCardContent,
  ],
    templateUrl: './medication-list.component.html',
    styleUrl: './medication-list.component.scss'
})
export class MedicationListComponent {
  private readonly router = inject(Router);
  medications = input.required<Medication[]>();
  delete = output<Medication>();

  navigateToDetailPage(medication: Medication) {
    this.router.navigate(['/medication', medication.id]);
  }
}
