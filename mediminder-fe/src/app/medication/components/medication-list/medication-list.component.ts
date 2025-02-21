import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatExpansionModule} from '@angular/material/expansion';
import {FormatDistanceToNowPurePipeModule, FormatPipeModule, ParseIsoPipeModule} from 'ngx-date-fns';
import {MatListModule} from '@angular/material/list';
import {MatButtonModule} from '@angular/material/button';
import {RouterLink} from '@angular/router';
import {MedicationTypeIconComponent} from '../medication-type-icon/medication-type-icon.component';
import {ColorIndicatorComponent} from '../../../shared/components/color-indicator/color-indicator.component';
import {Medication} from '../../models/medication';

@Component({
    selector: 'mediminder-medication-list',
    imports: [
        MatExpansionModule,
        MatListModule,
        MatButtonModule,
        ParseIsoPipeModule,
        FormatPipeModule,
        RouterLink,
        MedicationTypeIconComponent,
        ColorIndicatorComponent,
        FormatDistanceToNowPurePipeModule,
    ],
    templateUrl: './medication-list.component.html',
    styleUrl: './medication-list.component.scss'
})
export class MedicationListComponent {
  @Input({required: true})
  medications!: Medication[];
  @Output()
  delete: EventEmitter<Medication> = new EventEmitter<Medication>();
}
