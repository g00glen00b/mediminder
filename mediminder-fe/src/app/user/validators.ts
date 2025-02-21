import {AbstractControl, ValidatorFn} from '@angular/forms';

export function samePassword(field1: string, field2: string): ValidatorFn {
  return (group: AbstractControl) => {
    const value1: string = group.get(field1)!.value;
    const value2: string = group.get(field2)!.value;
    return value1 === value2 ? null : {samePassword: true};
  };
}
