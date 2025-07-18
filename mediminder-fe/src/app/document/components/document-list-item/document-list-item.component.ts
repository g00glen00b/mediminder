import {Component, computed, inject, input, output} from '@angular/core';
import {Document} from '../../models/document';
import {differenceInDays} from 'date-fns';
import {FormatDistanceToNowPurePipeModule, FormatPipeModule, ParseIsoPipeModule} from 'ngx-date-fns';
import {MatIconAnchor, MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {ListItemComponent} from '../../../shared/components/list-item/list-item.component';
import {TagComponent} from '../../../shared/components/tag/tag.component';
import {ListItemTitleDirective} from '../../../shared/components/list-item/list-item-title.directive';
import {ListItemIconDirective} from '../../../shared/components/list-item/list-item-icon.directive';
import {ListItemActionsDirective} from '../../../shared/components/list-item/list-item-actions.directive';
import {ListItemDescriptionDirective} from '../../../shared/components/list-item/list-item-description.directive';
import {Router, RouterLink} from '@angular/router';

@Component({
  selector: 'mediminder-document-list-item',
  imports: [
    FormatDistanceToNowPurePipeModule,
    FormatPipeModule,
    MatIcon,
    ParseIsoPipeModule,
    ListItemComponent,
    TagComponent,
    MatIconAnchor,
    ListItemTitleDirective,
    ListItemIconDirective,
    ListItemActionsDirective,
    ListItemDescriptionDirective,
    RouterLink,
    MatIconButton,
  ],
  templateUrl: './document-list-item.component.html',
  styleUrl: './document-list-item.component.scss'
})
export class DocumentListItemComponent {
  private readonly router = inject(Router);
  document = input.required<Document>();
  delete = output<Document>();
  download = output<Document>();
  expiresSoon = computed(() => {
    return this.document().expiryDate != null && differenceInDays(this.document().expiryDate!, new Date()) <= 7;
  });

  navigateToDocumentEdit() {
    this.router.navigate(['/medication', this.document().relatedMedication.id, 'document', this.document().id, 'edit']);
  }
}
