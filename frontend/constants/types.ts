// constants/types.ts

/**
 * Типы данных для системы чатов
 * 
 * @module types
 * @description Определяет основные интерфейсы и типы для работы с чатами,
 * пользователями и сообщениями в приложении. Используется для обеспечения
 * типобезопасности и согласованности данных между компонентами.
 */

// ==================== ОСНОВНЫЕ ТИПЫ ====================

/**
 * Тип чата определяет его назначение и доступные функции
 */
export type ChatType = "PRIVATE" | "GROUP" | "PUBLIC";

/**
 * Тип сообщения определяет его формат и способ отображения
 */
export type MessageType = "TEXT" | "IMAGE" | "FILE";

/**
 * Роль участника в чате определяет его права доступа
 */
export type ParticipantRole = "MEMBER" | "ADMIN" | "MODERATOR" | "OWNER";

/**
 * Типы сообщений для WebSocket соединения
 */
export type WebSocketMessageType = 
  | 'CHAT_MESSAGE'     // Обычное сообщение чата
  | 'USER_JOINED'      // Пользователь присоединился к чату
  | 'USER_LEFT'        // Пользователь покинул чат
  | 'TYPING'           // Индикатор набора текста
  | 'MESSAGE_READ';    // Подтверждение прочтения

// ==================== ОСНОВНЫЕ ИНТЕРФЕЙСЫ ====================

/**
 * Пользователь системы
 * 
 * @interface User
 * @property {number} id - Уникальный идентификатор пользователя
 * @property {string} username - Уникальное имя пользователя
 * @property {string} email - Адрес электронной почты
 * @property {string} [firstName] - Имя пользователя
 * @property {string} [lastName] - Фамилия пользователя
 * @property {string} [avatarUrl] - URL аватара пользователя
 * @property {boolean} [isOnline] - Статус онлайн
 * @property {string} [lastSeen] - Время последней активности
 */
export interface User {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  avatarUrl?: string;
  isOnline?: boolean;
  lastSeen?: string;
}

/**
 * Участник чата
 * 
 * @interface ChatParticipant
 * @property {number} userId - ID пользователя
 * @property {string} userName - Имя пользователя для отображения
 * @property {string} [userAvatarUrl] - URL аватара пользователя
 * @property {ParticipantRole} [role] - Роль участника в чате
 * @property {string} [joinedAt] - Время присоединения к чату
 * @property {boolean} [isActive] - Активен ли участник в чате
 * @property {string} [lastReadAt] - Время прочтения последнего сообщения
 * @property {boolean} [isOnline] - Статус онлайн участника
 * @property {string} [lastSeen] - Время последней активности
 */
export interface ChatParticipant {
  userId: number;
  userName: string;
  userAvatarUrl?: string;
  role?: ParticipantRole;
  joinedAt?: string;
  isActive?: boolean;
  lastReadAt?: string;
  isOnline?: boolean;
  lastSeen?: string;
}

/**
 * Комната чата
 * 
 * @interface ChatRoom
 * @property {number} id - Уникальный идентификатор чата
 * @property {string} [name] - Название чата (обязательно для групп)
 * @property {ChatType} type - Тип чата
 * @property {string} [avatarUrl] - URL аватара чата
 * @property {string} [description] - Описание чата
 * @property {string} [lastMessageText] - Текст последнего сообщения
 * @property {string} [lastMessageAt] - Время последнего сообщения
 * @property {string} createdAt - Время создания чата
 * @property {string} updatedAt - Время последнего обновления
 * @property {boolean} isActive - Активен ли чат
 * @property {number} maxParticipants - Максимальное количество участников
 * @property {ChatParticipant[]} participants - Список участников
 * @property {number} [unreadCount] - Количество непрочитанных сообщений (UI)
 * @property {boolean} [isOnline] - Статус онлайн собеседника (UI)
 * @property {string} [lastSeen] - Время последней активности (UI)
 */
export interface ChatRoom {
  id: number;
  name?: string;
  type: ChatType;
  avatarUrl?: string;
  description?: string;
  lastMessageText?: string;
  lastMessageAt?: string;
  createdAt: string;
  updatedAt: string;
  isActive: boolean;
  maxParticipants: number;
  participants: ChatParticipant[];
  
  // Поля для UI (могут вычисляться на клиенте)
  unreadCount?: number;
  isOnline?: boolean;
  lastSeen?: string;
}

/**
 * Запрос на создание нового чата
 * 
 * @interface CreateChatRequest
 * @property {ChatType} type - Тип создаваемого чата
 * @property {string} [name] - Название чата (обязательно для GROUP/PUBLIC)
 * @property {string} [description] - Описание чата
 * @property {string} [avatarUrl] - URL аватара чата
 * @property {number} [maxParticipants] - Максимальное количество участников
 * @property {number} [targetUserId] - ID пользователя для приватного чата
 */
export interface CreateChatRequest {
  type: ChatType;
  name?: string;
  description?: string;
  avatarUrl?: string;
  maxParticipants?: number;
  targetUserId?: number;
}

/**
 * Сообщение в чате
 * 
 * @interface Message
 * @property {number} id - Уникальный идентификатор сообщения
 * @property {string} content - Содержимое сообщения
 * @property {number} senderId - ID отправителя
 * @property {string} [senderName] - Имя отправителя
 * @property {string} [senderAvatarUrl] - URL аватара отправителя
 * @property {number} chatRoomId - ID чата
 * @property {MessageType} messageType - Тип сообщения
 * @property {string} sentAt - Время отправки
 * @property {number[]} readBy - Список ID пользователей, прочитавших сообщение
 * @property {boolean} [isEdited] - Было ли сообщение отредактировано
 * @property {string} [editedAt] - Время редактирования
 * @property {string} [deliveredAt] - Время доставки
 * @property {string} [readAt] - Время прочтения текущим пользователем
 */
export interface Message {
  id: number;
  content: string;
  senderId: number;
  senderName?: string;
  senderAvatarUrl?: string;
  chatRoomId: number;
  messageType: MessageType;
  sentAt: string;
  readBy: number[];
  isEdited?: boolean;
  editedAt?: string;
  deliveredAt?: string;
  readAt?: string;
}

// ==================== WEB SOCKET ТИПЫ ====================

/**
 * Сообщение для передачи через WebSocket
 * 
 * @interface WebSocketMessageDto
 * @property {WebSocketMessageType} type - Тип WebSocket сообщения
 * @property {number} chatRoomId - ID чата, к которому относится сообщение
 * @property {number} [senderId] - ID отправителя
 * @property {string} [senderName] - Имя отправителя
 * @property {string} [content] - Содержимое сообщения
 * @property {string} timestamp - Временная метка сообщения
 * @property {any} [payload] - Дополнительные данные в зависимости от типа
 */
export interface WebSocketMessageDto {
  type: WebSocketMessageType;
  chatRoomId: number;
  senderId?: number;
  senderName?: string;
  content?: string;
  timestamp: string;
  payload?: any;
}

/**
 * Событие набора текста (typing indicator)
 * 
 * @interface TypingEventDto
 * @property {number} userId - ID пользователя, который печатает
 * @property {boolean} isTyping - Флаг состояния набора текста
 */
export interface TypingEventDto {
  userId: number;
  isTyping: boolean;
}

/**
 * Событие прочтения сообщений
 * 
 * @interface MessageReadEventDto
 * @property {number} userId - ID пользователя, прочитавшего сообщения
 * @property {number[]} messageIds - Список ID прочитанных сообщений
 * @property {number} chatRoomId - ID чата
 */
export interface MessageReadEventDto {
  userId: number;
  messageIds: number[];
  chatRoomId: number;
}

// ==================== ВСПОМОГАТЕЛЬНЫЕ ТИПЫ ====================

/**
 * Результат поиска пользователей
 * 
 * @interface UserSearchResult
 * @extends User
 */
export interface UserSearchResult extends User {
  // Может содержать дополнительные поля для поиска
}

/**
 * Пагинированный ответ с чатами
 * 
 * @interface PagedChatRooms
 * @property {ChatRoom[]} content - Список чатов на текущей странице
 * @property {PageMetadata} page - Метаданные пагинации
 */
export interface PagedChatRooms {
  content: ChatRoom[];
  page: PageMetadata;
}

/**
 * Метаданные пагинации
 * 
 * @interface PageMetadata
 * @property {number} size - Количество элементов на странице
 * @property {number} number - Номер текущей страницы (начиная с 0)
 * @property {number} totalElements - Общее количество элементов
 * @property {number} totalPages - Общее количество страниц
 */

export interface PageMetadata {
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;
}
export interface SendMessageRequest {
  content: string;
  chatRoomId: number;
  senderId: number;
  messageType: MessageType;
  replyTo?: number;
  attachments?: string[];
}

export interface PagedMessages {
  content: Message[];
  page: PageMetadata;
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
}

/**
 * Состояние загрузки данных
 * 
 * @type LoadingState
 */
export type LoadingState = 'idle' | 'loading' | 'succeeded' | 'failed';