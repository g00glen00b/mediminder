import {Component, OnInit} from '@angular/core';
import {CabinetService} from "../../services/cabinet.service";
import {BehaviorSubject, map, mergeMap, Observable} from "rxjs";
import {CabinetEntry} from "../../models/cabinet-entry";
import {SortOption} from "../../../shared/models/sort-option";
import {ConfirmationService} from "../../../shared/services/confirmation.service";
import {ConfirmationDialogData} from "../../../shared/models/confirmation-dialog-data";
import {ToastrService} from "ngx-toastr";
import { Router, RouterLink } from "@angular/router";
import { AsyncPipe } from '@angular/common';
import { CabinetListComponent } from '../../components/cabinet-list/cabinet-list.component';
import { SortButtonComponent } from '../../../shared/components/sort-button/sort-button.component';
import { ContainerComponent } from '../../../shared/components/container/container.component';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { HeroActionsDirective } from '../../../shared/components/hero/hero-actions.directive';
import { HeroDescriptionDirective } from '../../../shared/components/hero/hero-description.directive';
import { HeroTitleDirective } from '../../../shared/components/hero/hero-title.directive';
import { HeroComponent } from '../../../shared/components/hero/hero.component';

export const SORT_OPTIONS: SortOption[] = [
  {sort: {direction: 'asc', field: 'name'}, label: 'Name'},
  {sort: {direction: 'asc', field: 'units'}, label: 'Least units first'},
  {sort: {direction: 'desc', field: 'units'}, label: 'Most units first'},
  {sort: {direction: 'asc', field: 'expiryDate'}, label: 'Nearest expiry date first'},
  {sort: {direction: 'desc', field: 'expiryDate'}, label: 'Furthest expiry date first'},
];

@Component({
    selector: 'mediminder-cabinet-overview-page',
    templateUrl: './cabinet-overview-page.component.html',
    styleUrls: ['./cabinet-overview-page.component.scss'],
    standalone: true,
    imports: [HeroComponent, HeroTitleDirective, HeroDescriptionDirective, HeroActionsDirective, MatButtonModule, RouterLink, MatIconModule, ContainerComponent, SortButtonComponent, CabinetListComponent, AsyncPipe]
})
export class CabinetOverviewPageComponent implements OnInit {
  entries$!: Observable<CabinetEntry[]>;
  activeSort$$: BehaviorSubject<SortOption> = new BehaviorSubject<SortOption>(SORT_OPTIONS[0]);
  sortOptions: SortOption[] = [...SORT_OPTIONS];

  constructor(
    private service: CabinetService,
    private confirmationService: ConfirmationService,
    private toastrService: ToastrService,
    private router: Router) {
  }

  ngOnInit() {
    this.initializeEntries();
  }

  private initializeEntries() {
    this.entries$ = this.activeSort$$
      .pipe(
        map(({sort}) => sort),
        mergeMap(sort => this.service.findAll(sort)));
  }

  onSubtractUnits(entry: CabinetEntry, units: number) {
    this.service.subtractUnits(entry.id, units).subscribe({
      next: () => {
        this.toastrService.success(`The units of ${entry.medication.name} were updated`);
        this.initializeEntries();
      },
      error: (err) => {
        this.toastrService.error(err);
      }
    });
  }

  onDeleteEntry(entry: CabinetEntry) {
    const data: ConfirmationDialogData = {
      title: 'Are you sure you want to delete this entry?',
      content: 'If you continue, the entry is permanently lost.',
      okLabel: 'YES',
      cancelLabel: 'NO',
    };
    this.confirmationService
      .show(data)
      .pipe(mergeMap(() => this.service.delete(entry.id)))
      .subscribe({
        next: () => {
          this.toastrService.success('The entry was successfully deleted');
          this.initializeEntries();
        },
        error: () => this.toastrService.error('The entry could not be removed'),
      });
  }

  onCopyEntry(entry: CabinetEntry): void {
    this.router.navigate(['cabinet', entry.id, 'copy']);
  }

  onEditEntry(entry: CabinetEntry) {
    this.router.navigate(['cabinet', entry.id, 'edit']);
  }
}
