import { Pipe, PipeTransform } from '@angular/core';
import {ScheduleRecurrenceType} from "../models/schedule-recurrence-type";
import {RECURRENCE_TYPES, ScheduleRecurrenceTypeWrapper} from "../models/schedule-recurrence-type-wrapper";

@Pipe({
  name: 'scheduleRecurrenceType',
  standalone: true
})
export class ScheduleRecurrenceTypePipe implements PipeTransform {

  transform(value: ScheduleRecurrenceType): string {
    const type: ScheduleRecurrenceTypeWrapper | undefined = RECURRENCE_TYPES.find(({type}) => value === type);
    return type == null ? '' : type.label.toLowerCase();
  }

}
