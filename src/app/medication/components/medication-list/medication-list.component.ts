import {Component, EventEmitter, Input, OnChanges, Output} from '@angular/core';
import {Medication} from "../../models/medication";
import {MatDialog} from "@angular/material/dialog";
import {MedicationDialogComponent} from "../medication-dialog/medication-dialog.component";

@Component({
  selector: 'mediminder-medication-list',
  templateUrl: './medication-list.component.html',
  styleUrls: ['./medication-list.component.scss']
})
export class MedicationListComponent {
  @Input()
  medications: Medication[] = [];
  @Output()
  delete: EventEmitter<Medication> = new EventEmitter<Medication>();

  constructor(private dialog: MatDialog) {
  }

  onClickItem(medication: Medication): void {
    this.dialog
      .open(MedicationDialogComponent, {data: medication, height: '100vh', width: '100vw', maxWidth: '100vw'})
      .afterClosed()
      .subscribe(event => {
        if (event === 'delete') this.delete.emit(medication);
      });
  }
}
