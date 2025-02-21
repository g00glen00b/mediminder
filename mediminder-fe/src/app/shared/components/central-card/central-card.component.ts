import {Component} from '@angular/core';
import {MatCard} from '@angular/material/card';

@Component({
  selector: 'mediminder-central-card',
  imports: [
    MatCard
  ],
  standalone: true,
  templateUrl: './central-card.component.html',
  styleUrl: './central-card.component.scss'
})
export class CentralCardComponent {

}
