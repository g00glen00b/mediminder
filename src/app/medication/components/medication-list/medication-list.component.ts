import {Component, EventEmitter, inject, Input, Output} from '@angular/core';
import {Medication} from "../../models/medication";
import {MatDialog} from "@angular/material/dialog";
import {MedicationDialogComponent} from "../medication-dialog/medication-dialog.component";
import {EmptyStateComponent} from '../../../shared/components/empty-state/empty-state.component';
import {NgFor, NgIf} from '@angular/common';
import {MatListModule} from '@angular/material/list';

@Component({
  selector: 'mediminder-medication-list',
  templateUrl: './medication-list.component.html',
  styleUrls: ['./medication-list.component.scss'],
  standalone: true,
  imports: [
    MatListModule,
    NgFor,
    NgIf,
    EmptyStateComponent
  ]
})
export class MedicationListComponent {
  @Input()
  medications: Medication[] = [];
  @Output()
  delete: EventEmitter<Medication> = new EventEmitter<Medication>();
  private dialog = inject(MatDialog);

  onClickItem(medication: Medication): void {
    this.dialog
      .open(MedicationDialogComponent, {data: medication, height: '100vh', width: '100vw', maxWidth: '100vw', maxHeight: '100vh'})
      .afterClosed()
      .subscribe(event => {
        if (event === 'delete') this.delete.emit(medication);
      });
  }
}
