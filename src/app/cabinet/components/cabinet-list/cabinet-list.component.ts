import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CabinetEntry} from "../../models/cabinet-entry";
import {MatDialog} from "@angular/material/dialog";
import {CabinetEntryDialogComponent} from "../cabinet-entry-dialog/cabinet-entry-dialog.component";
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { NgFor, NgIf, DecimalPipe, DatePipe } from '@angular/common';
import { MatListModule } from '@angular/material/list';

@Component({
  selector: 'mediminder-cabinet-list',
  templateUrl: './cabinet-list.component.html',
  styleUrls: ['./cabinet-list.component.scss'],
  standalone: true,
  imports: [
    MatListModule,
    NgFor,
    NgIf,
    EmptyStateComponent,
    DecimalPipe,
    DatePipe
  ]
})
export class CabinetListComponent {
  @Input()
  entries: CabinetEntry[] = [];
  @Output()
  copy: EventEmitter<CabinetEntry> = new EventEmitter<CabinetEntry>();
  @Output()
  delete: EventEmitter<CabinetEntry> = new EventEmitter<CabinetEntry>();
  @Output()
  edit: EventEmitter<CabinetEntry> = new EventEmitter<CabinetEntry>();
  @Output()
  takeOne: EventEmitter<CabinetEntry> = new EventEmitter<CabinetEntry>();

  constructor(private dialog: MatDialog) {
  }

  onItemClick(entry: CabinetEntry): void {
    this.dialog
      .open(CabinetEntryDialogComponent, {data: entry, height: '100vh', width: '100vw', maxWidth: '100vw', maxHeight: '100vh'})
      .afterClosed()
      .subscribe(event => {
        if (event === 'copy') this.copy.emit(entry);
        if (event === 'edit') this.edit.emit(entry);
        if (event === 'delete') this.delete.emit(entry);
        if (event === 'takeOne') this.takeOne.emit(entry);
      });
  }
}
