import {Injectable} from '@angular/core';
import {MedicationType} from "../models/medication-type";
import {NgxIndexedDBService} from "ngx-indexed-db";
import {CreateMedicationType} from "../models/create-medication-type";
import {filter, from, mergeMap, Observable, take, toArray} from "rxjs";
import {concatIfEmpty} from "rxjs-etc/operators";

const TYPES: CreateMedicationType[] = [
  {name: 'capsules', unit: 'capsules', individual: true},
  {name: 'tablets', unit: 'tablets', individual: true},
  {name: 'injections', unit: 'ml', individual: false},
  {name: 'patches', unit: 'patches', individual: true},
];

const STORE_NAME = 'medicationType';

@Injectable({
  providedIn: 'root'
})
export class MedicationTypeService {

  constructor(private dbService: NgxIndexedDBService) {
    from(TYPES)
      .pipe(
        mergeMap(request => this.findOrCreate(request)),
        toArray())
      .subscribe();
  }

  findAll(): Observable<MedicationType[]> {
    return this.dbService.getAll<MedicationType>(STORE_NAME).pipe(take(1));
  }

  findOrCreate(request: CreateMedicationType): Observable<MedicationType> {
    return this.findByName(request.name).pipe(concatIfEmpty(this.create(request)));
  }

  private create(request: CreateMedicationType): Observable<MedicationType> {
    const type: MedicationType = {id: crypto.randomUUID(), ...request};
    return this.dbService.add(STORE_NAME, type).pipe(take(1));
  }

  findByName(name: string): Observable<MedicationType> {
    return this.dbService
      .getByIndex<MedicationType>(STORE_NAME, 'name', name)
      .pipe(
        filter(type => type != undefined),
        take(1));
  }

  findById(id: string): Observable<MedicationType> {
    return this.dbService
      .getByID<MedicationType>(STORE_NAME, id)
      .pipe(
        take(1),
        filter(type => type != undefined));
  }
}
