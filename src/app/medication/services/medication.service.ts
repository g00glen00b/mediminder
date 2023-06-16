import {inject, Injectable} from '@angular/core';
import {CreateMedication} from "../models/create-medication";
import {filter, from, map, mergeMap, Observable, take, tap, throwIfEmpty, toArray} from "rxjs";
import {Medication} from "../models/medication";
import {MedicationTypeService} from "./medication-type.service";
import {NgxIndexedDBService} from "ngx-indexed-db";
import {MedicationEntity} from "../models/medication-entity";
import {concatIfEmpty} from "rxjs-etc/operators";
import {compareByField} from "../../shared/utils/compare-utils";
import {MediminderEventService} from "../../shared/services/mediminder-event.service";
import {MedicationDeletedEvent} from "../models/medication-deleted-event";

const STORE_NAME = 'medication';

@Injectable({
  providedIn: 'root'
})
export class MedicationService {
  private medicationTypeService = inject(MedicationTypeService);
  private eventService = inject(MediminderEventService);
  private dbService = inject(NgxIndexedDBService);

  findAll(): Observable<Medication[]> {
    return this.dbService
      .getAll<MedicationEntity>(STORE_NAME)
      .pipe(
        take(1),
        mergeMap(entities => from(entities)),
        mergeMap(entity => this.mapEntityToMedication(entity)),
        toArray(),
        map(results => results.sort(compareByField(medication => medication.name))));
  }

  findOrCreate(request: CreateMedication): Observable<Medication> {
    return this.findByName(request.name).pipe(concatIfEmpty(this.create(request)));
  }

  findById(id: string): Observable<Medication> {
    return this.dbService
      .getByID<MedicationEntity>(STORE_NAME, id)
      .pipe(
        take(1),
        filter(type => type != undefined),
        mergeMap(entity => this.mapEntityToMedication(entity)));
  }

  findByName(name: string): Observable<Medication> {
    return this.dbService
      .getByIndex<MedicationEntity>(STORE_NAME, 'name', name)
      .pipe(
        take(1),
        filter(type => type != undefined),
        mergeMap(entity => this.mapEntityToMedication(entity)));
  }

  delete(id: string): Observable<void> {
    return this.dbService.deleteByKey(STORE_NAME, id)
      .pipe(
        take(1),
        filter(value => value),
        throwIfEmpty(),
        tap(() => this.eventService.publish(new MedicationDeletedEvent(id))),
        map(() => void 0));
  }

  private create(request: CreateMedication): Observable<Medication> {
    const entity: MedicationEntity = {id: crypto.randomUUID(), ...request};
    return this.dbService
      .add(STORE_NAME, entity)
      .pipe(
        take(1),
        mergeMap(result => this.findById(result.id)));
  }

  private mapEntityToMedication(entity: MedicationEntity): Observable<Medication> {
    const {id, name, typeId} = entity;
    return this.medicationTypeService
      .findById(typeId)
      .pipe(
        take(1),
        map(type => ({id, name, type})))
  }
}
