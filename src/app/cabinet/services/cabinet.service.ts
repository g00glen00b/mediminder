import {Injectable} from '@angular/core';
import {filter, from, groupBy, map, mergeMap, Observable, of, reduce, take, tap, throwIfEmpty, toArray} from "rxjs";
import {CabinetEntry} from "../models/cabinet-entry";
import {Sort} from "../../shared/models/sort";
import {Comparator, compareBy, compareByField, reversed} from "../../shared/utils/compare-utils";
import {CreateCabinetEntry} from "../models/create-cabinet-entry";
import {CabinetEntryEntity} from "../models/cabinet-entry-entity";
import {NgxIndexedDBService} from "ngx-indexed-db";
import {MedicationService} from "../../medication/services/medication.service";
import {EditCabinetEntry} from "../models/edit-cabinet-entry";
import {ScheduleEntity} from "../../schedule/models/schedule-entity";
import {MediminderEventService} from "../../shared/services/mediminder-event.service";
import {MedicationDeletedEvent} from "../../medication/models/medication-deleted-event";
import {TotalAvailableDose} from "../models/total-available-dose";
import {IntakeCompletedEvent} from "../../intake/models/intake-completed-event";
import {isPast} from "date-fns";
import {reduceToArrayByProperties} from "../../shared/utils/rxjs-utils";
import {average, sum} from "../../shared/utils/array-utils";

const STORE_NAME = 'cabinet';

@Injectable({
  providedIn: 'root'
})
export class CabinetService {

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
    this.eventService.events()
      .pipe(
        filter(event => event instanceof IntakeCompletedEvent),
        map(event => event as IntakeCompletedEvent),
        mergeMap(({medicationId, dose}) => this.subtractUnitsByMedicationId(medicationId, dose)))
      .subscribe();
  }

  findAll(sort: Sort): Observable<CabinetEntry[]> {
    const comparator: Comparator<CabinetEntry> = this.getCompareFunctionBySort(sort);
    return this.dbService
      .getAll<CabinetEntryEntity>(STORE_NAME)
      .pipe(
        take(1),
        mergeMap(entities => from(entities)),
        mergeMap(entity => this.mapEntityToCabinetEntry(entity)),
        toArray(),
        map(entries => entries.sort(comparator)));
  }

  findAllAvailableDoses(): Observable<TotalAvailableDose[]> {
    return this
      .findAll({field: 'name', direction: 'asc'})
      .pipe(
        mergeMap(entries => from(entries)),
        groupBy(entry => entry.medication.id),
        mergeMap(group => group.pipe(
          reduceToArrayByProperties(entry => entry.units, entry => entry.initialUnits),
          map(([units, initialUnits]) => [sum(units), average(initialUnits)]),
          map(([totalAvailableDose, averageInitialDose]) => ({totalAvailableDose, averageInitialDose, medicationId: group.key}))
        )),
        toArray());
  }

  create(request: CreateCabinetEntry): Observable<CabinetEntry> {
    const {typeId, initialUnits, units, medicationName, expiryDate} = request;
    const id = crypto.randomUUID();
    return this.medicationService
      .findOrCreate({name: medicationName, typeId})
      .pipe(
        map(({id}) => id),
        map(medicationId => ({id, expiryDate, units, initialUnits, medicationId}) as CabinetEntryEntity),
        mergeMap(entity => this.dbService.add(STORE_NAME, entity)),
        take(1),
        mergeMap(result => this.findById(result.id)),
        throwIfEmpty());
  }

  findById(id: string): Observable<CabinetEntry> {
    return this.dbService
      .getByID<CabinetEntryEntity>(STORE_NAME, id)
      .pipe(
        take(1),
        mergeMap(entity => this.mapEntityToCabinetEntry(entity)));
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
        map(value => void 0));
  }

  edit(id: string, request: EditCabinetEntry): Observable<CabinetEntry> {
    return this.dbService
      .getByID<CabinetEntryEntity>(STORE_NAME, id)
      .pipe(
        map(entity => ({...entity, ...request})),
        mergeMap(entity => this.dbService.update(STORE_NAME, entity)),
        take(1),
        mergeMap(result => this.findById(result.id)));
  }

  subtractUnits(id: string, units: number): Observable<CabinetEntry> {
    return this.dbService
      .getByID<CabinetEntryEntity>(STORE_NAME, id)
      .pipe(
        map(entity => ({...entity, units: entity.units - units})),
        filter(entity => entity.units >= 0),
        throwIfEmpty(() => new Error('There are not enough units left over')),
        mergeMap(entity => this.dbService.update(STORE_NAME, entity)),
        take(1),
        mergeMap(result => this.findById(result.id)));
  }

  subtractUnitsByMedicationId(medicationId: string, units: number): Observable<void> {
    return this.dbService
      .getAllByIndex<CabinetEntryEntity>(STORE_NAME, 'medicationId', IDBKeyRange.only(medicationId))
      .pipe(
        mergeMap(entries => from(entries)),
        filter(entry => entry.units > 0),
        filter(entry => !isPast(entry.expiryDate)),
        toArray(),
        map(entries => entries.sort(
          compareBy([
            compareByField(entry => entry.expiryDate),
            compareByField(entry => entry.units)
        ]))),
        tap(entries => this.subtractUnitsFromEntities(entries, units)),
        mergeMap(entities => this.dbService.bulkPut(STORE_NAME, entities)),
        map(() => void 0));

  }

  private subtractUnitsFromEntities(entities: CabinetEntryEntity[], units: number): void {
    let unitsLeft: number = units;
    entities.forEach(entity => {
      if (entity.units > unitsLeft) entity.units -= unitsLeft;
      else entity.units = 0;
    });
  }

  private mapEntityToCabinetEntry(entity: CabinetEntryEntity): Observable<CabinetEntry> {
    const {id, medicationId, units, initialUnits, expiryDate} = entity;
    return this.medicationService
      .findById(medicationId)
      .pipe(map(medication => ({id, expiryDate, medication, units, initialUnits})));
  }

  private getCompareFunctionByField(field: string): Comparator<CabinetEntry> {
    switch (field) {
      case 'units': return compareByField(entry => entry.units / entry.initialUnits);
      case 'expiryDate': return compareByField(entry => entry.expiryDate);
      default: return compareByField(entry => entry.medication.name);
    }
  }

  private getCompareFunctionBySort(sort: Sort): Comparator<CabinetEntry> {
    const comparator = this.getCompareFunctionByField(sort.field);
    return sort.direction === 'asc' ? comparator : reversed(comparator);
  }
}
