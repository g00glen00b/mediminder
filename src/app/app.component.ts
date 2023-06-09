import {Component, OnInit, ViewChild} from '@angular/core';
import {NavigationEnd, Router} from "@angular/router";
import {filter} from "rxjs";
import {MatDrawer} from "@angular/material/sidenav";
import {MedicationTypeService} from "./medication/services/medication-type.service";
import {ToastrService} from "ngx-toastr";

@Component({
  selector: 'mediminder-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  @ViewChild(MatDrawer)
  drawer!: MatDrawer;

  constructor(private router: Router) {
  }

  ngOnInit(): void {
    this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd))
      .subscribe(() => this.drawer.toggle(false));
  }
}
