import {Component, input} from '@angular/core';
import {MedicationType} from '../../models/medication-type';
import {MedicationTypeIconComponent} from '../medication-type-icon/medication-type-icon.component';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';

@Component({
  selector: 'mediminder-medication-type-picker',
  imports: [
    MedicationTypeIconComponent
  ],
  templateUrl: './medication-type-picker.component.html',
  styleUrl: './medication-type-picker.component.scss',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      multi: true,
      useExisting: MedicationTypePickerComponent,
    }
  ]
})
export class MedicationTypePickerComponent implements ControlValueAccessor {
  isDisabled = false;
  isTouched = false;
  onTouched = () => {};
  onChange = (value: MedicationType) => {};
  types = input.required<MedicationType[]>();
  selected?: MedicationType;

  onSelect(type: MedicationType) {
    this.markAsTouched();
    if (!this.isDisabled) {
      this.selected = type;
      this.onChange(type);
    }
  }

  writeValue(obj: any): void {
    this.selected = obj;
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    this.isDisabled = isDisabled;
  }

  markAsTouched() {
    if (!this.isTouched) {
      this.isTouched = true;
      this.onTouched();
    }
  }
}
