import {Component, DestroyRef, inject} from '@angular/core';
import {MatProgressBar} from '@angular/material/progress-bar';
import {takeUntilDestroyed, toSignal} from '@angular/core/rxjs-interop';
import {HttpRequestService} from '../../services/http-request.service';

@Component({
  selector: 'mediminder-http-progressbar',
  imports: [
    MatProgressBar
  ],
  templateUrl: './http-progressbar.component.html',
  styleUrl: './http-progressbar.component.scss'
})
export class HttpProgressbarComponent {
  private readonly destroyRef = inject(DestroyRef);
  private readonly httpRequestService = inject(HttpRequestService);
  isRequestPending = toSignal(this.httpRequestService
    .isRequestPending()
    .pipe(takeUntilDestroyed(this.destroyRef)), {initialValue: false});
}
