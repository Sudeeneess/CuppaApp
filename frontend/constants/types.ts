// types.ts

export type ChatType = "PRIVATE" | "GROUP" | "PUBLIC";

export interface ChatRoom {
  id: number;
  name: string;
  type: ChatType;
  avatarUrl?: string;
  description?: string;
  lastMessageText?: string;
  lastMessageAt?: string;
  createdAt: string;
  updatedAt: string;
  isActive: boolean;
  maxParticipants: number;

  // UI поля
  unreadCount?: number;
  isOnline?: boolean;
  lastSeen?: string;
}

export interface CreateChatRequest {
  type: ChatType;
  // Для Групп/Серверов
  name?: string;
  description?: string;
  avatarUrl?: string;
  maxParticipants?: number;

  // ТОЛЬКО Для ЛС (ID собеседника)
  targetUserId?: number;
}
