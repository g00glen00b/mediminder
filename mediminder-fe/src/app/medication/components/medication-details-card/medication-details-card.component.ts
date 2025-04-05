import {Component, input} from '@angular/core';
import {Medication} from '../../models/medication';
import {MatCard, MatCardContent} from '@angular/material/card';
import {MatList, MatListItem, MatListItemLine, MatListItemTitle} from '@angular/material/list';
import {DecimalPipe} from '@angular/common';

@Component({
  selector: 'mediminder-medication-details-card',
  imports: [
    MatCardContent,
    MatCard,
    MatList,
    MatListItem,
    MatListItemLine,
    MatListItemTitle,
    DecimalPipe
  ],
  templateUrl: './medication-details-card.component.html',
  styleUrl: './medication-details-card.component.scss'
})
export class MedicationDetailsCardComponent {
  medication = input.required<Medication>();
}
