import {Component, input, output} from '@angular/core';
import {Document} from '../../models/document';
import {DocumentListItemComponent} from '../document-list-item/document-list-item.component';
import {MatAccordion} from '@angular/material/expansion';

@Component({
  selector: 'mediminder-document-list',
  imports: [
    DocumentListItemComponent,
    MatAccordion
  ],
  templateUrl: './document-list.component.html',
  styleUrl: './document-list.component.scss'
})
export class DocumentListComponent {
  documents = input.required<Document[]>();
  delete = output<Document>();
  download = output<Document>();
}
