import {Component, inject, signal} from '@angular/core';
import {ContainerComponent} from '../../../shared/components/container/container.component';
import {HeroComponent} from '../../../shared/components/hero/hero.component';
import {HeroDescriptionDirective} from '../../../shared/components/hero/hero-description.directive';
import {HeroTitleDirective} from '../../../shared/components/hero/hero-title.directive';
import {ChatFormComponent} from '../../components/chat-form/chat-form.component';
import {ChatHistoryComponent} from '../../components/chat-history/chat-history.component';
import {ChatMessage} from '../../models/chat-message';
import {AssistantService} from '../../services/assistant.service';
import {AssistantRequest} from '../../models/assistant-request';
import {finalize, map} from 'rxjs';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'mediminder-assistant-overview-page',
  imports: [
    ContainerComponent,
    HeroComponent,
    HeroDescriptionDirective,
    HeroTitleDirective,
    ChatFormComponent,
    ChatHistoryComponent
  ],
  templateUrl: './assistant-overview-page.component.html',
  styleUrl: './assistant-overview-page.component.scss'
})
export class AssistantOverviewPageComponent {
  private readonly service = inject(AssistantService);
  private readonly toastr = inject(ToastrService);
  chatHistory = signal<ChatMessage[]>([{message: 'Hello, how can I help you?', role: 'ASSISTANT'}]);
  loading = signal(false);

  send(question: string) {
    const message: ChatMessage = {message: question, role: 'USER'};
    const request: AssistantRequest  = {question};
    this.chatHistory.update(history => [...history, message]);
    this.loading.set(true);
    this.service
      .ask(request)
      .pipe(
        map(({answer}) => ({message: answer, role: 'ASSISTANT'} as ChatMessage)),
        finalize(() => this.loading.set(false)))
      .subscribe({
        next: message => this.chatHistory.update(history => [...history, message]),
        error: () => this.toastr.error('Could not get a response from the assistant.'),
      });
  }
}
