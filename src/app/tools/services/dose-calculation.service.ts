import {Injectable} from '@angular/core';
import {CabinetService} from "../../cabinet/services/cabinet.service";
import {IntakeService} from "../../intake/services/intake.service";
import {MedicationService} from "../../medication/services/medication.service";
import {combineLatest, from, map, mergeMap, Observable, toArray} from "rxjs";
import {DoseMatch} from "../models/dose-match";
import {MIDNIGHT} from "../../shared/utils/date-fns-utils";
import {set} from "date-fns";
import {TotalIntakeDose} from "../../intake/models/total-intake-dose";
import {TotalAvailableDose} from "../../cabinet/models/total-available-dose";
import {DoseMatchTuple} from "../models/dose-match-tuple";

@Injectable({
  providedIn: 'root'
})
export class DoseCalculationService {

  constructor(
    private cabinetService: CabinetService,
    private intakeService: IntakeService,
    private medicationService: MedicationService) { }

  findUntil(date: Date): Observable<DoseMatch[]> {
    const dateAtMidnight: Date = set(date, MIDNIGHT);
    return combineLatest([
      this.intakeService.findTotalIntakeDosesUntil(dateAtMidnight),
      this.cabinetService.findAllAvailableDoses()
    ]).pipe(
      map(([totalIntakeDoses, totalAvailableDoses]) => this.findDoseMatcheTuples(totalIntakeDoses, totalAvailableDoses)),
      mergeMap(doseMatches => from(doseMatches)),
      mergeMap(doseMatch => this.mapTupleToDoseMatch(doseMatch)),
      toArray());
  }

  private findDoseMatcheTuples(totalIntakeDoses: TotalIntakeDose[], totalAvailableDoses: TotalAvailableDose[]): DoseMatchTuple[] {
    return totalIntakeDoses.map(intakeDose => {
      const {medicationId} = intakeDose;
      const emptyAvailableDose: TotalAvailableDose = {medicationId, totalAvailableDose: 0, averageInitialDose: 0};
      const foundAvailableDose: TotalAvailableDose | undefined = totalAvailableDoses.find(({medicationId: availableMedicationId}) => availableMedicationId === medicationId);
      return {required: intakeDose, available: foundAvailableDose || emptyAvailableDose};
    });
  }

  private mapTupleToDoseMatch(tuple: DoseMatchTuple): Observable<DoseMatch> {
    const {available: {medicationId, totalAvailableDose: availableDoses, averageInitialDose}, required: {totalDose: requiredDoses}} = tuple;
    return this.medicationService
      .findById(medicationId)
      .pipe(map(medication => ({availableDoses, requiredDoses, averageInitialDose, medication})));
  }

}
