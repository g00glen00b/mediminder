import {Component, Input} from '@angular/core';
import {MedicationPlan} from '../../models/medication-plan';
import {MatAccordion} from '@angular/material/expansion';
import {PlannerListItemComponent} from '../planner-list-item/planner-list-item.component';

@Component({
    selector: 'mediminder-planner-list',
    imports: [
        MatAccordion,
        PlannerListItemComponent,
    ],
    templateUrl: './planner-list.component.html',
    styleUrl: './planner-list.component.scss'
})
export class PlannerListComponent {
  @Input({required: true})
  plans!: MedicationPlan[];
}
