import {Component, input} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import {MedicationType} from '../../models/medication-type';
import {Color, ColorOption, colorOptions} from '../../../shared/models/color';
import {MedicationTypeIconComponent} from '../medication-type-icon/medication-type-icon.component';

@Component({
  selector: 'mediminder-medication-color-picker',
  imports: [
    MedicationTypeIconComponent
  ],
  templateUrl: './medication-color-picker.component.html',
  styleUrl: './medication-color-picker.component.scss',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      multi: true,
      useExisting: MedicationColorPickerComponent,
    }
  ]
})
export class MedicationColorPickerComponent implements ControlValueAccessor {
  isDisabled = false;
  isTouched = false;
  onTouched = () => {};
  onChange = (value: Color) => {};
  colors = colorOptions();
  type = input.required<MedicationType>();
  selected?: Color;

  onSelect(color: ColorOption) {
    this.markAsTouched();
    if (!this.isDisabled) {
      this.selected = color.color;
      this.onChange(color.color);
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
