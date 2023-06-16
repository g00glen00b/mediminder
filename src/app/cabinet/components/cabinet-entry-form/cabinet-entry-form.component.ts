import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators, FormsModule, ReactiveFormsModule } from "@angular/forms";
import {combineLatest, filter, map, Observable, startWith} from "rxjs";
import {Medication} from "../../../medication/models/medication";
import {MedicationService} from "../../../medication/services/medication.service";
import {MedicationTypeService} from "../../../medication/services/medication-type.service";
import {MedicationType} from "../../../medication/models/medication-type";
import {CreateCabinetEntry} from "../../models/create-cabinet-entry";
import {CabinetEntry} from "../../models/cabinet-entry";
import {ToastrService} from "ngx-toastr";
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { NgFor, NgIf, AsyncPipe } from '@angular/common';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';

@Component({
    selector: 'mediminder-cabinet-entry-form',
    templateUrl: './cabinet-entry-form.component.html',
    styleUrls: ['./cabinet-entry-form.component.scss'],
    standalone: true,
    imports: [FormsModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatAutocompleteModule, NgFor, MatOptionModule, NgIf, MatSelectModule, MatDatepickerModule, MatButtonModule, AsyncPipe]
})
export class CabinetEntryFormComponent implements OnInit, OnChanges {
  @Input()
  entry: CabinetEntry | null = null;
  @Input()
  submitLabel: string = 'Create';
  @Input()
  medicationReadOnly: boolean = false;
  @Output()
  submit: EventEmitter<CreateCabinetEntry> = new EventEmitter<CreateCabinetEntry>();
  @Output()
  cancel: EventEmitter<void> = new EventEmitter<void>();
  form!: FormGroup;
  filteredMedication$!: Observable<Medication[]>;
  medicationTypes$!: Observable<MedicationType[]>;
  unitType: string = '';
  minimumInitialUnits: number = 0;

  constructor(
    private formBuilder: FormBuilder,
    private medicationService: MedicationService,
    private medicationTypeService: MedicationTypeService,
    private toastrService: ToastrService) {
  }

  ngOnInit(): void {
    this.initializeFormGroup();
    this.initializeMedicationNamesObservable();
    this.initializeMedicationTypesObservable();
    this.initializeMedicationType();
    this.initializeUnitType();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.form != null) {
      if (this.medicationReadOnly) {
        this.form.get('medicationName')!.disable();
        this.form.get('medicationType')!.disable();
      } else {
        this.form.get('medicationName')!.enable();
        this.form.get('medicationType')!.enable();
      }
      if (this.entry != null) {
        this.form.get('medicationName')!.setValue(this.entry.medication);
        this.form.get('units')!.setValue(this.entry.units);
        this.form.get('initialUnits')!.setValue(this.entry.initialUnits);
        this.form.get('expiryDate')!.setValue(this.entry.expiryDate);
      } else {
        this.form.get('medicationName')!.setValue('');
        this.form.get('units')!.setValue(0);
        this.form.get('initialUnits')!.setValue(0);
        this.form.get('expiryDate')!.setValue(new Date());
      }
    }
  }

  getMedicationName(value: Medication | string | null): string {
    if (value == null) return '';
    if (typeof value === 'string') return value;
    return value.name;
  }

  submitForm(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    if (!this.form.valid) {
      this.toastrService.error('The form is incomplete');
    } else {
      const medication: Medication | string = this.form.get('medicationName')!.value;
      const medicationName: string = typeof medication === 'string' ? medication : medication.name;
      const typeId: string = this.form.get('medicationType')!.value.id;
      const units: number = this.form.get('units')!.value;
      const initialUnits: number = this.form.get('initialUnits')!.value;
      const expiryDate: Date = this.form.get('expiryDate')!.value;
      this.submit.emit({medicationName, expiryDate, units, initialUnits, typeId});
    }
  }

  updateInitialUnits(): void {
    const initialUnitsControl = this.form.get('initialUnits')!;
    const units = this.form.get('units')!.value;
    this.minimumInitialUnits = units;
    if (!initialUnitsControl.dirty) {
      initialUnitsControl.setValue(units);
    }
  }

  compareMedicationTypes(type1: MedicationType | null, type2: MedicationType | null): boolean {
    return type1 != null && type2 != null && type1.id === type2.id;
  }

  getMedicationTypeId(index: number, medicationType: MedicationType | null): string | null {
    return medicationType == null ? null : medicationType.id;
  }

  private initializeMedicationTypesObservable() {
    this.medicationTypes$ = this.medicationTypeService.findAll();
  }

  private initializeMedicationNamesObservable() {
    this.filteredMedication$ = combineLatest([
      this.medicationService.findAll(),
      this.form.get('medicationName')!.valueChanges.pipe(startWith('')),
    ]).pipe(
      map(([results, value]) => [results, this.getMedicationName(value)] as [Medication[], string]),
      map(([results, name]) => this.filterMedicationByPartialName(results, name)),
    );
  }

  private initializeMedicationType() {
    const control = this.form.get('medicationType')!;
    this.form.get('medicationName')!.valueChanges.subscribe(medication => {
      if (medication == null || typeof medication === 'string') {
        control.setValue(null);
        control.enable();
      } else {
        control.setValue(medication.type);
        control.disable();
      }
    });
  }

  private initializeUnitType() {
    this.form.get('medicationType')!.valueChanges.subscribe(type => {
      if (type == null) {
        this.unitType = '';
      } else {
        const {unit} = type as MedicationType;
        this.unitType = unit;
      }
    });
  }

  private initializeFormGroup() {
    this.form = this.formBuilder.group({
      medicationName: new FormControl('', [Validators.required, Validators.maxLength(128)]),
      medicationType: new FormControl(null, [Validators.required]),
      units: new FormControl(0, [Validators.required]),
      initialUnits: new FormControl(0, [Validators.required]),
      expiryDate: new FormControl(new Date()),
    });
  }

  private filterMedicationByPartialName(medication: Medication[], partialName: string): Medication[] {
    const lowerPartialName = partialName.toLowerCase();
    return medication.filter(({name}) => {
      const lowerName = name.toLowerCase();
      return lowerName.includes(lowerPartialName);
    });
  }
}
