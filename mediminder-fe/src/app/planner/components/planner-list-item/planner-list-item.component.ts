import {Component, computed, input} from '@angular/core';
import {MedicationPlan} from '../../models/medication-plan';
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from '@angular/material/card';
import {
  MedicationTypeIconComponent
} from '../../../medication/components/medication-type-icon/medication-type-icon.component';

@Component({
  selector: 'mediminder-planner-list-item',
  imports: [
    MatCardContent,
    MatCard,
    MatCardHeader,
    MatCardTitle,
    MedicationTypeIconComponent,
  ],
  templateUrl: './planner-list-item.component.html',
  styleUrl: './planner-list-item.component.scss'
})
export class PlannerListItemComponent {
  plan = input.required<MedicationPlan>();
  missingDoses = computed(() => {
    const {requiredDoses, availableDoses} = this.plan();
    return Math.max(requiredDoses - availableDoses, 0);
  });
  prescriptionsRequired = computed(() => {
    const {requiredDoses, availableDoses, medication: {dosesPerPackage}} = this.plan();
    return Math.ceil(Math.max(requiredDoses - availableDoses, 0) / dosesPerPackage);
  });
}
