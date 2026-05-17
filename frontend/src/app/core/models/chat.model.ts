export interface ChatMessage {
  role: 'user' | 'agent';
  content: string;
  timestamp: Date;
}

export interface ChatResponse {
  message: string;
  response: string;
}
