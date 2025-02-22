import {Component, computed, input} from '@angular/core';
import {MedicationPlan} from '../../models/medication-plan';
import {ColorIndicatorComponent} from '../../../shared/components/color-indicator/color-indicator.component';
import {
  MatExpansionPanel,
  MatExpansionPanelDescription,
  MatExpansionPanelHeader,
  MatExpansionPanelTitle
} from '@angular/material/expansion';
import {MatList, MatListItem, MatListItemLine, MatListItemTitle} from '@angular/material/list';
import {
  MedicationTypeIconComponent
} from '../../../medication/components/medication-type-icon/medication-type-icon.component';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'mediminder-planner-list-item',
  imports: [
    ColorIndicatorComponent,
    MatExpansionPanel,
    MatExpansionPanelDescription,
    MatExpansionPanelHeader,
    MatExpansionPanelTitle,
    MatList,
    MatListItem,
    MatListItemLine,
    MatListItemTitle,
    MedicationTypeIconComponent,
    MatIcon,

  ],
  templateUrl: './planner-list-item.component.html',
  styleUrl: './planner-list-item.component.scss'
})
export class PlannerListItemComponent {
  plan = input.required<MedicationPlan>();
  percentageAvailable = computed(() => {
    const {requiredDoses, availableDoses} = this.plan();
    return requiredDoses == 0 ? 100 : (availableDoses / requiredDoses) * 100;
  });
  prescriptionsRequired = computed(() => {
    const {requiredDoses, availableDoses, medication: {dosesPerPackage}} = this.plan();
    return Math.ceil(Math.max(requiredDoses - availableDoses, 0) / dosesPerPackage);
  });
}
