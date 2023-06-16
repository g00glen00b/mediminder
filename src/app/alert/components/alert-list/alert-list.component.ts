import {Component, DestroyRef, inject, OnInit} from '@angular/core';
import {AlertService} from "../../services/alert.service";
import {Observable} from "rxjs";
import {Alert} from "../../models/alert";
import { AlertComponent } from '../alert/alert.component';
import { NgFor, AsyncPipe } from '@angular/common';
import {takeUntilDestroyed} from "@angular/core/rxjs-interop";

@Component({
  selector: 'mediminder-alert-list',
  templateUrl: './alert-list.component.html',
  styleUrls: ['./alert-list.component.scss'],
  standalone: true,
  imports: [
    NgFor,
    AlertComponent,
    AsyncPipe
  ]
})
export class AlertListComponent implements OnInit {
  alerts$!: Observable<Alert[]>;
  private alertService = inject(AlertService);
  private destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.alerts$ = this.alertService.findAllAlerts().pipe(takeUntilDestroyed(this.destroyRef));
  }
}
