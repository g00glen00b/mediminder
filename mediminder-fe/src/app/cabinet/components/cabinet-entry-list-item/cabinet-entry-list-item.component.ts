import {Component, computed, input, output} from '@angular/core';
import {ColorIndicatorComponent} from '../../../shared/components/color-indicator/color-indicator.component';
import {FormatDistanceToNowPurePipeModule, FormatPipeModule, ParseIsoPipeModule} from 'ngx-date-fns';
import {MatAnchor, MatButton} from '@angular/material/button';
import {
  MatExpansionPanel,
  MatExpansionPanelActionRow,
  MatExpansionPanelDescription,
  MatExpansionPanelHeader,
  MatExpansionPanelTitle
} from '@angular/material/expansion';
import {MatList, MatListItem, MatListItemLine, MatListItemTitle} from '@angular/material/list';
import {CabinetEntry} from '../../models/cabinet-entry';
import {RouterLink} from '@angular/router';
import {differenceInDays} from 'date-fns';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'mediminder-cabinet-entry-list-item',
  imports: [
    ColorIndicatorComponent,
    FormatDistanceToNowPurePipeModule,
    FormatPipeModule,
    MatAnchor,
    MatButton,
    MatExpansionPanel,
    MatExpansionPanelActionRow,
    MatExpansionPanelDescription,
    MatExpansionPanelHeader,
    MatExpansionPanelTitle,
    MatList,
    MatListItem,
    MatListItemLine,
    MatListItemTitle,
    ParseIsoPipeModule,
    RouterLink,
    MatIcon
  ],
  templateUrl: './cabinet-entry-list-item.component.html',
  styleUrl: './cabinet-entry-list-item.component.scss'
})
export class CabinetEntryListItemComponent {
  entry = input.required<CabinetEntry>();
  delete = output<CabinetEntry>();
  expiresSoon = computed(() => differenceInDays(this.entry().expiryDate, new Date()) <= 7);
  runsOutSoon = computed(() => (this.entry().remainingDoses / this.entry().medication.dosesPerPackage) <= 0.1);
}
