import {Injectable} from '@angular/core';
import {filter, from, map, mergeMap, Observable, take, throwIfEmpty, toArray} from "rxjs";
import {Schedule} from "../models/schedule";
import {NgxIndexedDBService} from "ngx-indexed-db";
import {Comparator, compareByField, reversed} from "../../shared/utils/compare-utils";
import {CabinetEntryEntity} from "../../cabinet/models/cabinet-entry-entity";
import {Sort} from "../../shared/models/sort";
import {ScheduleEntity} from "../models/schedule-entity";
import {MedicationService} from "../../medication/services/medication.service";
import {CreateSchedule} from "../models/create-schedule";
import {EditSchedule} from "../models/edit-schedule";
import {MediminderEventService} from "../../shared/services/mediminder-event.service";
import {MedicationDeletedEvent} from "../../medication/models/medication-deleted-event";
import {set} from "date-fns";
import {MIDNIGHT} from "../../shared/utils/date-fns-utils";
import {SchedulePeriod} from "../models/schedule-period";

const STORE_NAME = 'schedule';

@Injectable({
  providedIn: 'root'
})
export class ScheduleService {

  constructor(
    private dbService: NgxIndexedDBService,
    private medicationService: MedicationService,
    private eventService: MediminderEventService) {
    this.eventService.events()
      .pipe(
        filter(event => event instanceof MedicationDeletedEvent),
        map(event => (event as MedicationDeletedEvent).medicationId),
        mergeMap(medicationId => this.deleteByMedicationId(medicationId)))
      .subscribe();
  }

  findAll(sort: Sort): Observable<Schedule[]> {
    const comparator: Comparator<Schedule> = this.getCompareFunctionBySort(sort);
    return this.dbService
      .getAll<ScheduleEntity>(STORE_NAME)
      .pipe(
        take(1),
        mergeMap(entities => from(entities)),
        mergeMap(entity => this.mapEntityToSchedule(entity)),
        toArray(),
        map(entries => entries.sort(comparator)));
  }

  create(request: CreateSchedule): Observable<Schedule> {
    const id = crypto.randomUUID();
    const startDateAtMidnight: Date = set(request.period.startingAt, MIDNIGHT);
    const endDateAtMidnight: Date | undefined = request.period.endingAtInclusive == null ? undefined : set(request.period.endingAtInclusive, MIDNIGHT);
    const period: SchedulePeriod = {startingAt: startDateAtMidnight, endingAtInclusive: endDateAtMidnight};
    const entity: ScheduleEntity = {id, ...request, period};
    return this.dbService
      .add(STORE_NAME, entity)
      .pipe(
        take(1),
        mergeMap(result => this.findById(result.id)),
        throwIfEmpty());
  }

  findById(id: string): Observable<Schedule> {
    return this.dbService
      .getByID<ScheduleEntity>(STORE_NAME, id)
      .pipe(
        take(1),
        mergeMap(entity => this.mapEntityToSchedule(entity)));
  }

  deleteMany(ids: string[]): Observable<void> {
    return this.dbService.bulkDelete(STORE_NAME, ids)
      .pipe(
        take(1),
        map(() => void 0));
  }

  deleteByMedicationId(medicationId: string): Observable<void> {
    return this.dbService
      .getAllByIndex<ScheduleEntity>(STORE_NAME, 'medicationId', IDBKeyRange.only(medicationId))
      .pipe(
        take(1),
        map(results => results.map(({id}) => id)),
        mergeMap(ids => this.deleteMany(ids)));
  }

  delete(id: string): Observable<void> {
    return this.dbService.deleteByKey(STORE_NAME, id)
      .pipe(
        take(1),
        filter(value => value),
        throwIfEmpty(),
        map(() => void 0));
  }

  edit(id: string, request: EditSchedule): Observable<Schedule> {
    const startDateAtMidnight: Date = set(request.period.startingAt, MIDNIGHT);
    const endDateAtMidnight: Date | undefined = request.period.endingAtInclusive == null ? undefined : set(request.period.endingAtInclusive, MIDNIGHT);
    const period: SchedulePeriod = {startingAt: startDateAtMidnight, endingAtInclusive: endDateAtMidnight};
    return this.dbService
      .getByID<CabinetEntryEntity>(STORE_NAME, id)
      .pipe(
        map(entity => ({...entity, ...request, period})),
        mergeMap(entity => this.dbService.update(STORE_NAME, entity)),
        take(1),
        mergeMap(result => this.findById(result.id)));
  }

  private getCompareFunctionByField(field: string): Comparator<Schedule> {
    switch (field) {
      case 'period': return compareByField(entry => entry.period.startingAt);
      case 'time': return compareByField(entry => entry.time);
      default: return compareByField(entry => entry.medication.name);
    }
  }

  private getCompareFunctionBySort(sort: Sort): Comparator<Schedule> {
    const comparator = this.getCompareFunctionByField(sort.field);
    return sort.direction === 'asc' ? comparator : reversed(comparator);
  }

  private mapEntityToSchedule(entity: ScheduleEntity): Observable<Schedule> {
    const {id, medicationId, dose, period, time, recurrence, description} = entity;
    return this.medicationService
      .findById(medicationId)
      .pipe(map(medication => ({id, time, period, dose, recurrence, medication, description})));
  }
}
