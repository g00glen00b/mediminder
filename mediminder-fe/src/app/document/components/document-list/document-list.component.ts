import {Component, input, output} from '@angular/core';
import {Document} from '../../models/document';
import {DocumentListItemComponent} from '../document-list-item/document-list-item.component';
import {MatCard, MatCardContent} from '@angular/material/card';
import {MatDivider} from '@angular/material/divider';

@Component({
  selector: 'mediminder-document-list',
  imports: [
    DocumentListItemComponent,
    MatCard,
    MatCardContent,
    MatDivider
  ],
  templateUrl: './document-list.component.html',
  styleUrl: './document-list.component.scss'
})
export class DocumentListComponent {
  documents = input.required<Document[]>();
  download = output<Document>();
}
