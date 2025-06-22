import {Component, DestroyRef, inject, signal} from '@angular/core';
import {NotificationService} from '../../services/notification.service';
import {defaultPageRequest} from '../../../shared/models/page-request';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {mergeMap} from 'rxjs';
import {emptyPage} from '../../../shared/models/page';
import {Notification, NotificationType} from '../../models/notification';
import {AlertComponent} from '../../../shared/components/alert/alert.component';

@Component({
  selector: 'mediminder-notification-overview-page',
  imports: [
    AlertComponent
  ],
  templateUrl: './notification-overview-page.component.html',
  styleUrl: './notification-overview-page.component.scss'
})
export class NotificationOverviewPageComponent {
  private readonly service = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);
  infoNotificationTypes: NotificationType[] = ['CABINET_ENTRY_ALMOST_EXPIRED', 'DOCUMENT_ALMOST_EXPIRED', 'DOCUMENT_ALMOST_EXPIRED', 'INTAKE_EVENT', 'SCHEDULE_ALMOST_OUT_OF_DOSES'];
  warningNotificationTypes: NotificationType[] = ['CABINET_ENTRY_EXPIRED', 'DOCUMENT_EXPIRED', 'SCHEDULE_OUT_OF_DOSES'];
  pageRequest = signal(defaultPageRequest(['title,asc']))
  notifications = toSignal(toObservable(this.pageRequest).pipe(
    takeUntilDestroyed(this.destroyRef),
    mergeMap(pageRequest => this.service.findAll(pageRequest))
  ), {initialValue: emptyPage<Notification>()});

  delete(notification: Notification) {
    this.service.delete(notification.id).subscribe(() => this.pageRequest.set({...this.pageRequest()}));
  }
}
