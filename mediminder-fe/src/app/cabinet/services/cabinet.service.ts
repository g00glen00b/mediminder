import {inject, Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {CreateCabinetEntryRequest} from '../models/create-cabinet-entry-request';
import {Observable} from 'rxjs';
import {CabinetEntry} from '../models/cabinet-entry';
import {environment} from '../../../environment/environment';
import {PageRequest, pageRequestToHttpParams} from '../../shared/models/page-request';
import {Page} from '../../shared/models/page';
import {UpdateCabinetEntryRequest} from '../models/update-cabinet-entry-request';

@Injectable({
  providedIn: 'root'
})
export class CabinetService {
  private readonly httpClient = inject(HttpClient);

  findAll(pageRequest: PageRequest): Observable<Page<CabinetEntry>> {
    const params = pageRequestToHttpParams(pageRequest);
    return this.httpClient.get<Page<CabinetEntry>>(`${environment.apiUrl}/cabinet`, {params});
  }

  findById(id: string): Observable<CabinetEntry> {
    return this.httpClient.get<CabinetEntry>(`${environment.apiUrl}/cabinet/${id}`);
  }

  create(request: CreateCabinetEntryRequest): Observable<CabinetEntry> {
    return this.httpClient.post<CabinetEntry>(`${environment.apiUrl}/cabinet`, request);
  }

  delete(id: string): Observable<void> {
    return this.httpClient.delete<void>(`${environment.apiUrl}/cabinet/${id}`);
  }

  update(id: string, request: UpdateCabinetEntryRequest): Observable<CabinetEntry> {
    return this.httpClient.put<CabinetEntry>(`${environment.apiUrl}/cabinet/${id}`, request);
  }
}
