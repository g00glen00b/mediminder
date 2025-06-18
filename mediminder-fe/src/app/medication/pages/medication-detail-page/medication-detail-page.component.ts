import {Component, DestroyRef, inject, input, OnInit} from '@angular/core';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {switchMap} from 'rxjs';
import {MedicationService} from '../../services/medication.service';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {MedicationTypeIconComponent} from '../../components/medication-type-icon/medication-type-icon.component';
import {NavbarService} from '../../../shared/services/navbar.service';
import {
  MedicationDetailsCardComponent
} from '../../components/medication-details-card/medication-details-card.component';
import {ActionHeaderComponent} from '../../../shared/components/action-header/action-header.component';
import {MatAnchor, MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {Router, RouterLink} from '@angular/router';
import {ScheduleService} from '../../../schedule/services/schedule.service';
import {CabinetService} from '../../../cabinet/services/cabinet.service';
import {DocumentService} from '../../../document/services/document.service';
import {defaultPageRequest} from '../../../shared/models/page-request';
import {emptyPage} from '../../../shared/models/page';
import {Schedule} from '../../../schedule/models/schedule';
import {CabinetEntry} from '../../../cabinet/models/cabinet-entry';
import {ScheduleListComponent} from '../../../schedule/components/schedule-list/schedule-list.component';
import {CabinetEntryListComponent} from '../../../cabinet/components/cabinet-entry-list/cabinet-entry-list.component';
import {ConfirmationService} from '../../../shared/services/confirmation.service';
import {ToastrService} from 'ngx-toastr';
import {ErrorResponse} from '../../../shared/models/error-response';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {Document} from '../../../document/models/document';
import {DocumentListComponent} from '../../../document/components/document-list/document-list.component';
import {UserService} from '../../../user/services/user.service';

@Component({
  selector: 'mediminder-medication-detail-page',
  imports: [
    ContainerComponent,
    HeroComponent,
    HeroTitleDirective,
    MedicationTypeIconComponent,
    MedicationDetailsCardComponent,
    ActionHeaderComponent,
    MatAnchor,
    MatIcon,
    RouterLink,
    ScheduleListComponent,
    CabinetEntryListComponent,
    MatButton,
    AlertComponent,
    DocumentListComponent,
  ],
  templateUrl: './medication-detail-page.component.html',
  styleUrl: './medication-detail-page.component.scss'
})
export class MedicationDetailPageComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly medicationService = inject(MedicationService);
  private readonly scheduleService = inject(ScheduleService);
  private readonly cabinetService = inject(CabinetService);
  private readonly documentService = inject(DocumentService);
  private readonly navbarService = inject(NavbarService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly userService = inject(UserService);
  private readonly toastr = inject(ToastrService);
  private readonly router = inject(Router);

  id = input.required<string>();
  medication = toSignal(toObservable(this.id).pipe(
    takeUntilDestroyed(this.destroyRef),
    switchMap(id => this.medicationService.findById(id))
  ));
  schedules = toSignal(toObservable(this.id).pipe(
    takeUntilDestroyed(this.destroyRef),
    switchMap(id => this.scheduleService.findAll(defaultPageRequest(['id,asc']), id))
  ), {initialValue: emptyPage<Schedule>()});
  cabinetEntries = toSignal(toObservable(this.id).pipe(
    takeUntilDestroyed(this.destroyRef),
    switchMap(id => this.cabinetService.findAll(defaultPageRequest(['id,asc']), id))
  ), {initialValue: emptyPage<CabinetEntry>()});
  documents = toSignal(toObservable(this.id).pipe(
    takeUntilDestroyed(this.destroyRef),
    switchMap(id => this.documentService.findAll(defaultPageRequest(['id,asc']), id))
  ), {initialValue: emptyPage<Document>()});
  showDocuments = toSignal(this.userService.hasAuthority('Document'), {initialValue: false});
  error?: ErrorResponse;

  ngOnInit() {
    this.navbarService.enableBackButton(['/medication']);
    this.navbarService.setTitle('Medication Details');
  }

  delete() {
    this.confirmationService.show({
      cancelLabel: 'Cancel',
      content: `Are you sure you want to delete '${this.medication()?.name}'?`,
      title: 'Confirm',
      okLabel: 'Delete',
      type: 'error',
    }).pipe(
      switchMap(() => this.medicationService.delete(this.id()))
    ).subscribe({
      next: () => {
        this.toastr.success(`Successfully deleted '${this.medication()?.name}'`);
        this.router.navigate([`/medication`]);
      },
      error: response => this.error = response.error,
    });
  }
}
