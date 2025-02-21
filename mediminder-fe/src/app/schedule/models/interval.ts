import {Duration} from 'date-fns';
import {parseAndNormalizeDuration} from '../../shared/utils/date-fns-utils';

export const intervalTypes= ['Day(s)', 'Week(s)', 'Month(s)'] as const;
export const defaultInterval: Interval = {units: 1, type: 'Day(s)'} as const;

export type IntervalType = typeof intervalTypes[number];
export interface Interval {
  units: number;
  type: IntervalType;
}

export function isoDurationToInterval(isoDuration: string): Interval {
  const interval: Duration = parseAndNormalizeDuration(isoDuration);
  const {days, weeks, months} = interval;
  if (days != null && days > 0) return {units: days, type: 'Day(s)'};
  if (weeks != null && weeks > 0) return {units: weeks, type: 'Week(s)'};
  if (months != null && months > 0) return {units: months, type: 'Month(s)'};
  throw new Error(`Cannot convert ${isoDuration}`);
}

export function intervalToIsoDuration(interval: Interval): Duration {
  switch (interval.type) {
    case 'Day(s)': return {days: interval.units};
    case 'Week(s)': return {days: interval.units * 7};
    case 'Month(s)': return {months: interval.units};
    default: return {};
  }
}
