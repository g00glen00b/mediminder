import {Injectable} from '@angular/core';
import {CabinetService} from "../../cabinet/services/cabinet.service";
import {combineLatest, filter, from, map, mergeMap, Observable, toArray} from "rxjs";
import {Alert} from "../models/alert";
import {CabinetEntry} from "../../cabinet/models/cabinet-entry";
import {isExpired, isSoonExpired} from "../utils/alert-utils";
import {addDays, format, set} from "date-fns";
import {MIDNIGHT} from "../../shared/utils/date-fns-utils";
import {compareByField} from "../../shared/utils/compare-utils";
import {DoseCalculationService} from "../../tools/services/dose-calculation.service";
import {DoseMatch} from "../../tools/models/dose-match";

@Injectable({
  providedIn: 'root'
})
export class AlertService {

  constructor(
    private cabinetService: CabinetService,
    private doseCalculationService: DoseCalculationService) { }

  findAllAlerts(): Observable<Alert[]> {
    return combineLatest([
      this.findExpiryAlerts(),
      this.findUnitAlerts()
    ]).pipe(
      map(([alerts1, alerts2]) => [...alerts1, ...alerts2]),
      map(alerts => alerts.sort(compareByField(alert => alert.type))));
  }

  findExpiryAlerts(): Observable<Alert[]> {
    return this.cabinetService
      .findAll({field: 'name', direction: 'asc'})
      .pipe(
        mergeMap(entries => from(entries)),
        filter(entry => entry.units > 0),
        map(entry => this.findExpiryAlert(entry)),
        filter(alert => alert != null),
        map(alert => alert as Alert),
        toArray());
  }

  findUnitAlerts(): Observable<Alert[]> {
    const dateToCheck: Date = set(addDays(new Date(), 7), MIDNIGHT);
    return this.doseCalculationService
      .findUntil(dateToCheck)
      .pipe(
        mergeMap(matches => from(matches)),
        map(match => this.findDoseMatchAlert(match)),
        filter(alert => alert != null),
        map(alert => alert as Alert),
        toArray());
  }

  private findDoseMatchAlert(doseMatch: DoseMatch): Alert | null {
    const difference: number = doseMatch.availableDoses - doseMatch.requiredDoses;
    if (doseMatch.availableDoses === 0) {
      return {
        type: 'error',
        title: `You're out of ${doseMatch.medication.name}`,
        text: `You have no more ${doseMatch.medication.name} available in your cabinet. Please go to the nearest pharmacy.`
      };
    } else if (difference < 0) {
      return {
        type: 'warning',
        title: `You're running out of ${doseMatch.medication.name}`,
        text: `Your ${doseMatch.medication.name} will run out in less than a week. Please go to the nearest pharmacy.`
      };
    } else {
      return null;
    }
  }

  private findExpiryAlert(entry: CabinetEntry): Alert | null {
    if (isExpired(entry)) {
      return {
        type: 'error',
        title: `${entry.medication.name} expired`,
        text: `${entry.medication.name} with expiry date ${format(entry.expiryDate, 'PPP')} expired`
      };
    } else if (isSoonExpired(entry)) {
      return {
        type: 'warning',
        title: `${entry.medication.name} expires soon`,
        text: `${entry.medication.name} with expiry date ${format(entry.expiryDate, 'PPP')} expires soon`
      };
    } else {
      return null;
    }
  }
}
