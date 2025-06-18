import {inject, Injectable} from '@angular/core';
import {from, mergeMap, Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {SubscriptionConfiguration} from '../models/subscription-configuration';
import {environment} from '../../../environment/environment';
import {SwPush} from '@angular/service-worker';

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private readonly httpClient = inject(HttpClient);
  private readonly swPush = inject(SwPush);

  subscribe() {
    return this.findConfiguration().pipe(
      mergeMap(configuration => from(this.swPush.requestSubscription({
        serverPublicKey: configuration.publicKey
      }))),
      mergeMap(subscription => this.subscribeCall(subscription))
    );
  }

  private findConfiguration(): Observable<SubscriptionConfiguration> {
    return this.httpClient.get<SubscriptionConfiguration>(`./api/notification/subscription/configuration`);
  }

  private subscribeCall(request: PushSubscription): Observable<void> {
    const body = request.toJSON();
    return this.httpClient.post<void>(`./api/notification/subscription`, body);
  }
}
