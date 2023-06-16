import {CabinetEntry} from "../../cabinet/models/cabinet-entry";
import {isPast, subDays} from "date-fns";

export const EXPIRY_WARNING_DAYS = 7;
export const EXPIRY_ERROR_DAYS = 0;

export function isSoonExpired(entry: CabinetEntry): boolean {
  return isPast(subDays(entry.expiryDate, EXPIRY_WARNING_DAYS));
}

export function isExpired(entry: CabinetEntry): boolean {
  return isPast(subDays(entry.expiryDate, EXPIRY_ERROR_DAYS));
}
