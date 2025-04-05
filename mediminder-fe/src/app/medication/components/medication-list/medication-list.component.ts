import {Component, input} from '@angular/core';
import {MatExpansionModule} from '@angular/material/expansion';
import {FormatPipeModule, ParseIsoPipeModule} from 'ngx-date-fns';
import {MatListModule} from '@angular/material/list';
import {MatButtonModule} from '@angular/material/button';
import {Medication} from '../../models/medication';
import {MatCard, MatCardContent} from '@angular/material/card';
import {MedicationListItemComponent} from '../medication-list-item/medication-list-item.component';

@Component({
    selector: 'mediminder-medication-list',
  imports: [
    MatExpansionModule,
    MatListModule,
    MatButtonModule,
    ParseIsoPipeModule,
    FormatPipeModule,
    MatCard,
    MatCardContent,
    MedicationListItemComponent,
  ],
    templateUrl: './medication-list.component.html',
    styleUrl: './medication-list.component.scss'
})
export class MedicationListComponent {
  medications = input.required<Medication[]>();
}
