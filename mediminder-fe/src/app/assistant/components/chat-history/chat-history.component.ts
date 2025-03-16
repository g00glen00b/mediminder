import {Component, ElementRef, input, OnChanges, SimpleChanges, ViewChild} from '@angular/core';
import {ChatMessage} from '../../models/chat-message';
import {TypingProgressComponent} from '../../../shared/components/typing-progress/typing-progress.component';
import {AlertComponent} from '../../../shared/components/alert/alert.component';

@Component({
  selector: 'mediminder-chat-history',
  imports: [
    TypingProgressComponent,
    AlertComponent
  ],
  templateUrl: './chat-history.component.html',
  styleUrl: './chat-history.component.scss'
})
export class ChatHistoryComponent implements OnChanges {
  history = input.required<ChatMessage[]>();
  loading = input(false);
  @ViewChild('historyContainer')
  private historyContainer!: ElementRef;

  ngOnChanges() {;
    if (this.historyContainer != null) {
      const nativeElement =  this.historyContainer.nativeElement;
      setTimeout(() => nativeElement.scrollTop = nativeElement.scrollHeight, 0);
    }
  }
}
