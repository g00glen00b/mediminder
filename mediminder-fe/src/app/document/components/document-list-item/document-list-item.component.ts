import {Component, computed, input, output} from '@angular/core';
import {Document} from '../../models/document';
import {differenceInDays} from 'date-fns';
import {FormatDistanceToNowPurePipeModule, FormatPipeModule, ParseIsoPipeModule} from 'ngx-date-fns';
import {MatAnchor, MatButton} from '@angular/material/button';
import {
  MatExpansionPanel,
  MatExpansionPanelActionRow,
  MatExpansionPanelDescription,
  MatExpansionPanelHeader,
  MatExpansionPanelTitle
} from '@angular/material/expansion';
import {MatIcon} from '@angular/material/icon';
import {MatList, MatListItem, MatListItemLine, MatListItemTitle} from '@angular/material/list';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'mediminder-document-list-item',
  imports: [
    FormatDistanceToNowPurePipeModule,
    FormatPipeModule,
    MatAnchor,
    MatButton,
    MatExpansionPanel,
    MatExpansionPanelActionRow,
    MatExpansionPanelDescription,
    MatExpansionPanelHeader,
    MatExpansionPanelTitle,
    MatIcon,
    MatList,
    MatListItem,
    MatListItemLine,
    MatListItemTitle,
    ParseIsoPipeModule,
    RouterLink
  ],
  templateUrl: './document-list-item.component.html',
  styleUrl: './document-list-item.component.scss'
})
export class DocumentListItemComponent {
  document = input.required<Document>();
  delete = output<Document>();
  download = output<Document>();
  expiresSoon = computed(() => {
    return this.document().expiryDate != null && differenceInDays(this.document().expiryDate!, new Date()) <= 7;
  });
}
