import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {PageRequest, pageRequestToHttpParams} from '../../shared/models/page-request';
import {Observable} from 'rxjs';
import {Page} from '../../shared/models/page';
import {environment} from '../../../environment/environment';
import {Notification} from '../models/notification';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly httpClient = inject(HttpClient);

  findAll(pageRequest: PageRequest): Observable<Page<Notification>> {
    const params = pageRequestToHttpParams(pageRequest);
    return this.httpClient.get<Page<Notification>>(`${environment.apiUrl}/notification`, {params});
  }

  delete(id: string): Observable<void> {
    return this.httpClient.delete<void>(`${environment.apiUrl}/notification/${id}`);
  }
}
