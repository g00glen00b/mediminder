import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CabinetEntry} from "../../models/cabinet-entry";
import {MatDialog} from "@angular/material/dialog";
import {CabinetEntryDialogComponent} from "../cabinet-entry-dialog/cabinet-entry-dialog.component";

@Component({
  selector: 'mediminder-cabinet-list',
  templateUrl: './cabinet-list.component.html',
  styleUrls: ['./cabinet-list.component.scss']
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
      .open(CabinetEntryDialogComponent, {data: entry, height: '100vh', width: '100vw', maxWidth: '100vw'})
      .afterClosed()
      .subscribe(event => {
        if (event === 'copy') this.copy.emit(entry);
        if (event === 'edit') this.edit.emit(entry);
        if (event === 'delete') this.delete.emit(entry);
        if (event === 'takeOne') this.takeOne.emit(entry);
      });
  }
}
