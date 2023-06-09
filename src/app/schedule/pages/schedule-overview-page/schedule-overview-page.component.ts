import {Component} from '@angular/core';
import {SortOption} from "../../../shared/models/sort-option";
import {BehaviorSubject, map, mergeMap, Observable} from "rxjs";
import {ConfirmationService} from "../../../shared/services/confirmation.service";
import {ToastrService} from "ngx-toastr";
import {Router} from "@angular/router";
import {ConfirmationDialogData} from "../../../shared/models/confirmation-dialog-data";
import {Schedule} from "../../models/schedule";
import {ScheduleService} from "../../services/schedule.service";

export const SORT_OPTIONS: SortOption[] = [
  {sort: {direction: 'asc', field: 'name'}, label: 'Name'},
  {sort: {direction: 'asc', field: 'period'}, label: 'Earliest scheduled first'},
  {sort: {direction: 'desc', field: 'period'}, label: 'Latest scheduled first'},
  {sort: {direction: 'asc', field: 'time'}, label: 'Earliest intake time first'},
  {sort: {direction: 'desc', field: 'time'}, label: 'Latest intake time first'},
];

@Component({
  selector: 'mediminder-schedule-overview-page',
  templateUrl: './schedule-overview-page.component.html',
  styleUrls: ['./schedule-overview-page.component.scss']
})
export class ScheduleOverviewPageComponent {
  entries$!: Observable<Schedule[]>;
  activeSort$$: BehaviorSubject<SortOption> = new BehaviorSubject<SortOption>(SORT_OPTIONS[0]);
  sortOptions: SortOption[] = [...SORT_OPTIONS];
  selectedEntries: Schedule[] = [];

  constructor(
    private service: ScheduleService,
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

  onDeleteEntry(entry: Schedule) {
    const data: ConfirmationDialogData = {
      title: 'Are you sure you want to delete this schedule?',
      content: 'If you continue, the schedule is permanently lost.',
      okLabel: 'YES',
      cancelLabel: 'NO',
    };
    this.confirmationService
      .show(data)
      .pipe(mergeMap(() => this.service.delete(entry.id)))
      .subscribe({
        next: () => {
          this.toastrService.success('The schedule was successfully deleted');
          this.initializeEntries();
        },
        error: () => this.toastrService.error('The schedule could not be removed'),
      });
  }

  onCopyEntry(entry: Schedule): void {
    this.router.navigate(['schedule', entry.id, 'copy']);
  }

  onEditEntry(entry: Schedule): void {
    this.router.navigate(['schedule', entry.id, 'edit']);
  }
}
