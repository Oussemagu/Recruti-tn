import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

type ChatMessage = {
  role: 'user' | 'assistant';
  content: string;
};

type ChatbotResponse = {
  answer: string;
  suggestedUrls: string[];
};

@Component({
  selector: 'app-chatbot-widget',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chatbot-widget.component.html',
  styleUrl: './chatbot-widget.component.css'
})
export class ChatbotWidgetComponent {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  isOpen = signal(false);
  loading = signal(false);
  input = '';

  messages = signal<ChatMessage[]>([
    {
      role: 'assistant',
      content: 'Bonjour ! Je suis l assistant Recruti.tn. Je peux t aider sur les recruteurs, candidats, et la navigation dans l application.'
    }
  ]);

  quickUrls = signal<string[]>(['/home', '/auth/login', '/auth/register', '/candidat/offres', '/candidat/mes-offres', '/schedule-interview']);

  toggle(): void {
    this.isOpen.update((value) => !value);
  }

  goTo(url: string): void {
    this.router.navigateByUrl(url);
  }

  ask(): void {
    const message = this.input.trim();
    if (!message || this.loading()) {
      return;
    }

    this.messages.update((list) => [...list, { role: 'user', content: message }]);
    this.input = '';
    this.loading.set(true);

    this.http.post<ChatbotResponse>(`${environment.apiUrl}/api/chatbot/ask`, {
      message,
      currentPath: this.router.url
    }).subscribe({
      next: (response) => {
        this.messages.update((list) => [...list, {
          role: 'assistant',
          content: response?.answer || 'Je n ai pas pu generer une reponse.'
        }]);
        if (Array.isArray(response?.suggestedUrls) && response.suggestedUrls.length > 0) {
          this.quickUrls.set(response.suggestedUrls);
        }
        this.loading.set(false);
      },
      error: () => {
        this.messages.update((list) => [...list, {
          role: 'assistant',
          content: 'Le chatbot est temporairement indisponible.'
        }]);
        this.loading.set(false);
      }
    });
  }
}
