import {Pipe, PipeTransform} from '@angular/core';
import {isoDurationToInterval} from '../models/interval';

@Pipe({
  name: 'interval',
  standalone: true
})
export class IntervalPipe implements PipeTransform {

  transform(value?: string): string {
    if (value == undefined) return '';
    const interval = isoDurationToInterval(value);
    return `${interval.units} ${interval.type.toLowerCase()}`;
  }

}
