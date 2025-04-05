import {Component, DestroyRef, inject, input, OnInit, signal} from '@angular/core';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {combineLatest, switchMap} from 'rxjs';
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
import {MatAnchor} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {RouterLink} from '@angular/router';
import {ScheduleService} from '../../../schedule/services/schedule.service';
import {CabinetService} from '../../../cabinet/services/cabinet.service';
import {DocumentService} from '../../../document/services/document.service';
import {defaultPageRequest} from '../../../shared/models/page-request';
import {emptyPage} from '../../../shared/models/page';
import {Schedule} from '../../../schedule/models/schedule';
import {CabinetEntry} from '../../../cabinet/models/cabinet-entry';
import {ScheduleListComponent} from '../../../schedule/components/schedule-list/schedule-list.component';

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
  private cabinetPageRequest= signal(defaultPageRequest(['id,asc']));
  private schedulePageRequest= signal(defaultPageRequest(['id,asc']));

  id = input.required<string>();
  medication = toSignal(toObservable(this.id).pipe(
    takeUntilDestroyed(this.destroyRef),
    switchMap(id => this.medicationService.findById(id))
  ));
  schedules = toSignal(combineLatest([
    toObservable(this.id),
    toObservable(this.schedulePageRequest),
  ]).pipe(
    takeUntilDestroyed(this.destroyRef),
    switchMap(([id, pageRequest]) => this.scheduleService.findAll(pageRequest, id))
  ), {initialValue: emptyPage<Schedule>()});
  cabinetEntries = toSignal(combineLatest([
    toObservable(this.id),
    toObservable(this.cabinetPageRequest),
  ]).pipe(
    takeUntilDestroyed(this.destroyRef),
    switchMap(([id, pageRequest]) => this.cabinetService.findAll(pageRequest, id))
  ), {initialValue: emptyPage<CabinetEntry>()});


  ngOnInit() {
    this.navbarService.enableBackButton(['/medication']);
    this.navbarService.setTitle('Medication Details');
  }
}
