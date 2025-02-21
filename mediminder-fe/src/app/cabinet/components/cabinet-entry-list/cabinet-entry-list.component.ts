import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CabinetEntry} from '../../models/cabinet-entry';
import {MatExpansionModule} from '@angular/material/expansion';
import {CabinetEntryListItemComponent} from '../cabinet-entry-list-item/cabinet-entry-list-item.component';

@Component({
    selector: 'mediminder-cabinet-entry-list',
    imports: [
        MatExpansionModule,
        CabinetEntryListItemComponent,
    ],
    templateUrl: './cabinet-entry-list.component.html',
    styleUrl: './cabinet-entry-list.component.scss'
})
export class CabinetEntryListComponent {
  @Input({required: true})
  entries!: CabinetEntry[];
  @Output()
  delete: EventEmitter<CabinetEntry> = new EventEmitter<CabinetEntry>();
}
