import { Injectable } from '@angular/core';
import {Observable, Subject} from "rxjs";
import {MediminderEvent} from "../models/mediminder-event";

@Injectable({
  providedIn: 'root'
})
export class MediminderEventService {
  private events$$: Subject<MediminderEvent> = new Subject<MediminderEvent>();

  publish(event: MediminderEvent): void {
    this.events$$.next(event);
  }

  events(): Observable<MediminderEvent> {
    return this.events$$;
  }
}
