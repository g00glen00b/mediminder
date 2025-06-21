import {Component, inject} from '@angular/core';
import {UserService} from '../../services/user.service';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {ErrorResponse} from '../../../shared/models/error-response';
import {ProfileFormComponent} from '../../components/profile-form/profile-form.component';
import {toSignal} from '@angular/core/rxjs-interop';
import {UpdateUserRequest} from '../../models/update-user-request';
import {ToastrService} from 'ngx-toastr';
import {MatButton} from '@angular/material/button';
import {SubscriptionService} from '../../services/subscription.service';
import {ConfirmationService} from '../../../shared/services/confirmation.service';
import {ConfirmationDialogData} from '../../../shared/models/confirmation-dialog-data';
import {mergeMap} from 'rxjs';
import {Router} from '@angular/router';
import {environment} from '../../../../environment/environment';
import {AuthService} from '@auth0/auth0-angular';

@Component({
  selector: 'mediminder-edit-profile-page',
  imports: [
    AlertComponent,
    ContainerComponent,
    HeroComponent,
    HeroDescriptionDirective,
    HeroTitleDirective,
    ProfileFormComponent,
    MatButton
  ],
  templateUrl: './edit-profile-page.component.html',
  styleUrl: './edit-profile-page.component.scss'
})
export class EditProfilePageComponent {
  private readonly authService = inject(AuthService);
  private readonly service = inject(UserService);
  private readonly subscriptionService = inject(SubscriptionService);
  private readonly router = inject(Router);
  private readonly toastr = inject(ToastrService);
  private readonly confirmationService = inject(ConfirmationService);
  error?: ErrorResponse;
  user = toSignal(this.service.findCurrentUser());

  submitProfileForm(request: UpdateUserRequest) {
    this.error = undefined;
    this.service.update(request).subscribe({
      next: () => this.toastr.success('Successfully updated profile'),
      error: response => this.error = response.error,
    });
  }

  enablePushNotifications(): void {
    this.subscriptionService.subscribe().subscribe({
      next: () => this.toastr.success('Successfully enabled push notifications'),
      error: error => this.toastr.error(error || 'Could not enable push notifications'),
    })
  }

  deleteAccount() {
    const data: ConfirmationDialogData = {
      content: 'Are you sure you want to delete your account? This action cannot be undone.',
      title: 'Delete Account',
      okLabel: 'Yes',
      cancelLabel: 'No',
      type: 'error',
    };
    this.confirmationService
      .show(data)
      .pipe(mergeMap(() => this.service.delete()))
      .subscribe({
        next: () => {
          this.toastr.success('Your account has been deleted successfully.');
          this.router.navigate(['/user', 'login']);
        },
        error: response => this.toastr.error(response.error.detail),
      });
  }

  logout() {
    this.authService.logout();
  }
}
