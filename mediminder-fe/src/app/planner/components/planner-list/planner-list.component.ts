import {Component, input} from '@angular/core';
import {MedicationPlan} from '../../models/medication-plan';
import {PlannerListItemComponent} from '../planner-list-item/planner-list-item.component';

@Component({
    selector: 'mediminder-planner-list',
  imports: [
    PlannerListItemComponent,
  ],
    templateUrl: './planner-list.component.html',
    styleUrl: './planner-list.component.scss'
})
export class PlannerListComponent {
  plans = input.required<MedicationPlan[]>();
}
