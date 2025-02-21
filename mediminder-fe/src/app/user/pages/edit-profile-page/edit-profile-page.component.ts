import {Component, inject} from '@angular/core';
import {UserService} from '../../services/user.service';
import {AlertComponent} from '../../../shared/components/alert/alert.component';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {ErrorResponse} from '../../../shared/models/error-response';
import {ProfileFormComponent} from '../../components/profile-form/profile-form.component';
import {PasswordFormComponent} from '../../components/password-form/password-form.component';
import {toSignal} from '@angular/core/rxjs-interop';
import {UpdateUserRequest} from '../../models/update-user-request';
import {ToastrService} from 'ngx-toastr';
import {UpdateCredentialsRequest} from '../../models/update-credentials-request';
import {MatButton} from '@angular/material/button';
import {SubscriptionService} from '../../services/subscription.service';

@Component({
  selector: 'mediminder-edit-profile-page',
  imports: [
    AlertComponent,
    ContainerComponent,
    HeroComponent,
    HeroDescriptionDirective,
    HeroTitleDirective,
    ProfileFormComponent,
    PasswordFormComponent,
    MatButton
  ],
  templateUrl: './edit-profile-page.component.html',
  styleUrl: './edit-profile-page.component.scss'
})
export class EditProfilePageComponent {
  private readonly service = inject(UserService);
  private readonly subscriptionService = inject(SubscriptionService);
  private readonly toastr = inject(ToastrService);
  error?: ErrorResponse;
  user = toSignal(this.service.findCurrentUser());

  submitProfileForm(request: UpdateUserRequest) {
    this.error = undefined;
    this.service.update(request).subscribe({
      next: () => this.toastr.success('Successfully updated profile'),
      error: response => this.error = response.error,
    });
  }

  submitCredentialsForm(request: UpdateCredentialsRequest) {
    this.error = undefined;
    this.service.updateCredentials(request).subscribe({
      next: () => this.toastr.success('Successfully updated credentials'),
      error: response => this.error = response.error,
    });
  }

  enablePushNotifications(): void {
    this.subscriptionService.subscribe().subscribe({
      next: () => this.toastr.success('Successfully enabled push notifications'),
      error: error => this.toastr.error(error || 'Could not enable push notifications'),
      complete: () => console.log('Complete'),
    })
  }
}
