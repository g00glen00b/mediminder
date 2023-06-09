import {Component, OnInit} from '@angular/core';
import {AlertService} from "../../services/alert.service";
import {Observable} from "rxjs";
import {Alert} from "../../models/alert";

@Component({
  selector: 'mediminder-alert-list',
  templateUrl: './alert-list.component.html',
  styleUrls: ['./alert-list.component.scss']
})
export class AlertListComponent implements OnInit {
  alerts$!: Observable<Alert[]>;

  constructor(private alertService: AlertService) {
  }

  ngOnInit(): void {
    this.alerts$ = this.alertService.findAllAlerts();
  }
}
