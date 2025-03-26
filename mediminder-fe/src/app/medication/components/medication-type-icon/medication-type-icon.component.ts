import {Component, inject, input} from '@angular/core';
import {MedicationType} from '../../models/medication-type';
import {NgSwitch, NgSwitchCase, NgSwitchDefault} from '@angular/common';
import {MatIconModule, MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {Color} from '../../../shared/models/color';

@Component({
  selector: 'mediminder-medication-type-icon',
  imports: [
    NgSwitch,
    MatIconModule,
    NgSwitchCase,
    NgSwitchDefault,
  ],
  templateUrl: './medication-type-icon.component.html',
  standalone: true,
  styleUrl: './medication-type-icon.component.scss'
})
export class MedicationTypeIconComponent {
  private readonly iconRegistry = inject(MatIconRegistry);
  private readonly domSanitizer = inject(DomSanitizer);
  medicationType = input.required<MedicationType>();
  color = input.required<Color>();

  constructor() {
    const pillsIcon = this.domSanitizer.bypassSecurityTrustResourceUrl('healthicons/medications/pills_2.svg');
    const patchIcon = this.domSanitizer.bypassSecurityTrustResourceUrl('healthicons/contraceptives/contraceptive_patch.svg');
    const implantIcon = this.domSanitizer.bypassSecurityTrustResourceUrl('healthicons/contraceptives/implant.svg');
    const bottleIcon = this.domSanitizer.bypassSecurityTrustResourceUrl('healthicons/devices/medicine_bottle.svg');
    const inhalerIcon = this.domSanitizer.bypassSecurityTrustResourceUrl('healthicons/devices/asthma_inhaler.svg');
    const syringeIcon = this.domSanitizer.bypassSecurityTrustResourceUrl('healthicons/devices/syringe.svg');
    this.iconRegistry.addSvgIcon('medications-pills-2', pillsIcon);
    this.iconRegistry.addSvgIcon('contraceptives-contraceptive-patch', patchIcon);
    this.iconRegistry.addSvgIcon('contraceptives-implant', implantIcon);
    this.iconRegistry.addSvgIcon('devices-medicine-bottle', bottleIcon);
    this.iconRegistry.addSvgIcon('devices-asthma-inhaler', inhalerIcon);
    this.iconRegistry.addSvgIcon('devices-syringe', syringeIcon);
  }
}
