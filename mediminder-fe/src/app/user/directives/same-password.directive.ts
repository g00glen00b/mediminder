import {Directive} from '@angular/core';
import {FormGroup, NG_VALIDATORS, ValidationErrors, Validator} from '@angular/forms';

@Directive({
  selector: '[samePassword]',
  providers: [{
    provide: NG_VALIDATORS,
    useExisting: SamePasswordDirective,
    multi: true
  }]
})
export class SamePasswordDirective implements Validator {
    validate(control: FormGroup): ValidationErrors | null {
      const value1 = control.value.password;
      const value2 = control.value.repeatPassword;
      return value1 === value2 ? null : {samePassword: true};
    }
}
