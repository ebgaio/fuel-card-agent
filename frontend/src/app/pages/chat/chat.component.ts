import { Component, signal, ViewChild, ElementRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { DatePipe } from '@angular/common';
import { AgentService } from '../../core/services/agent.service';
import { ChatMessage } from '../../core/models/chat.model';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [
    FormsModule,
    MatCardModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    DatePipe,
  ],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.scss',
})
export class ChatComponent {
  @ViewChild('chatContainer') chatContainer!: ElementRef;

  messages = signal<ChatMessage[]>([]);
  userInput = '';
  isLoading = signal(false);

  constructor(private agentService: AgentService) {}

  sendMessage(): void {
    const msg = this.userInput.trim();
    if (!msg || this.isLoading()) return;

    const userMessage: ChatMessage = {
      role: 'user',
      content: msg,
      timestamp: new Date(),
    };

    this.messages.update((msgs) => [...msgs, userMessage]);
    this.userInput = '';
    this.isLoading.set(true);
    this.scrollToBottom();

    this.agentService.chat(msg).subscribe({
      next: (response) => {
        const agentMessage: ChatMessage = {
          role: 'agent',
          content: response.response,
          timestamp: new Date(),
        };
        this.messages.update((msgs) => [...msgs, agentMessage]);
        this.isLoading.set(false);
        this.scrollToBottom();
      },
      error: (err) => {
        const errorMessage: ChatMessage = {
          role: 'agent',
          content:
            'Desculpe, ocorreu um erro ao processar sua mensagem. Verifique se o backend está rodando.',
          timestamp: new Date(),
        };
        this.messages.update((msgs) => [...msgs, errorMessage]);
        this.isLoading.set(false);
        this.scrollToBottom();
      },
    });
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      if (this.chatContainer) {
        const el = this.chatContainer.nativeElement;
        el.scrollTop = el.scrollHeight;
      }
    }, 50);
  }

  getSuggestions(): string[] {
    return [
      'Quais clientes possuem cartão de posto?',
      'Mostre as estatísticas dos cartões',
      'Busque o cliente com nome Silva',
      'Liste todos os cartões de crédito',
    ];
  }

  useSuggestion(suggestion: string): void {
    this.userInput = suggestion;
    this.sendMessage();
  }
}
