import {inject, Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable} from 'rxjs';
import {Medication} from '../models/medication';
import {CreateMedicationRequest} from '../models/create-medication-request';
import {environment} from '../../../environment/environment';
import {PageRequest, pageRequestToHttpParams} from '../../shared/models/page-request';
import {Page} from '../../shared/models/page';
import {UpdateMedicationRequest} from '../models/update-medication-request';

@Injectable({
  providedIn: 'root'
})
export class MedicationService {
  private readonly httpClient = inject(HttpClient);

  create(request: CreateMedicationRequest): Observable<Medication> {
    return this.httpClient.post<Medication>(`./api/medication`, request);
  }

  findAll(search: string = '', pageRequest: PageRequest): Observable<Page<Medication>> {
    const params = pageRequestToHttpParams(pageRequest).set('search', search);
    return this.httpClient.get<Page<Medication>>(`./api/medication`, {params});
  }

  delete(id: string): Observable<void> {
    return this.httpClient.delete<void>(`./api/medication/${id}`);
  }

  update(id: string, request: UpdateMedicationRequest): Observable<Medication> {
    return this.httpClient.put<Medication>(`./api/medication/${id}`, request);
  }

  findById(id: string): Observable<Medication> {
    return this.httpClient.get<Medication>(`./api/medication/${id}`);
  }
}
