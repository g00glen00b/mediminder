import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
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

  findAll(pageRequest: PageRequest, medicationId?: string): Observable<Page<CabinetEntry>> {
    let params = pageRequestToHttpParams(pageRequest);
    if (medicationId != null) params = params.set('medicationId', medicationId);
    return this.httpClient.get<Page<CabinetEntry>>(`./api/cabinet`, {params});
  }

  findById(id: string): Observable<CabinetEntry> {
    return this.httpClient.get<CabinetEntry>(`./api/cabinet/${id}`);
  }

  create(request: CreateCabinetEntryRequest): Observable<CabinetEntry> {
    return this.httpClient.post<CabinetEntry>(`./api/cabinet`, request);
  }

  delete(id: string): Observable<void> {
    return this.httpClient.delete<void>(`./api/cabinet/${id}`);
  }

  update(id: string, request: UpdateCabinetEntryRequest): Observable<CabinetEntry> {
    return this.httpClient.put<CabinetEntry>(`./api/cabinet/${id}`, request);
  }
}
