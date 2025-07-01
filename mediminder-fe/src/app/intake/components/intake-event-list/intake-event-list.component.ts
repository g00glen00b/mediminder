import {Component, computed, inject, input, output} from '@angular/core';
import {IntakeEvent} from '../../models/intake-event';
import {groupPerTime} from '../../models/intake-events-per-time';
import {MatCardHeader, MatCardModule} from '@angular/material/card';
import {MatButton, MatIconButton} from '@angular/material/button';
import {
  MedicationTypeIconComponent
} from '../../../medication/components/medication-type-icon/medication-type-icon.component';
import {FormatPipeModule, ParseIsoPipeModule} from 'ngx-date-fns';
import {MatIcon} from '@angular/material/icon';
import {Router} from '@angular/router';

@Component({
  selector: 'mediminder-intake-event-list',
  standalone: true,
  imports: [
    MatCardModule,
    MatButton,
    MedicationTypeIconComponent,
    FormatPipeModule,
    ParseIsoPipeModule,
    MatCardHeader,
    MatIconButton,
    MatIcon
  ],
  templateUrl: './intake-event-list.component.html',
  styleUrl: './intake-event-list.component.scss'
})
export class IntakeEventListComponent {
  private readonly router = inject(Router);
  events = input.required<IntakeEvent[]>();
  complete = output<IntakeEvent>();
  eventsPerTime = computed(() => groupPerTime(this.events()));

  viewDetailPage(event: IntakeEvent) {
    this.router.navigate(['/medication', event.medication.id]);
  }
}
