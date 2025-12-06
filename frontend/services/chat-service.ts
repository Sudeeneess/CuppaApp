// services/chat-service.ts
import AsyncStorage from "@react-native-async-storage/async-storage";
import { ChatRoom, CreateChatRequest } from "@/constants/types";

// ==================== КОНСТАНТЫ И ТИПЫ ====================

/**
 * Базовый URL API сервера
 */
const BASE_URL = "http://localhost:8080";
const API_BASE = `${BASE_URL}/api`;

/**
 * Ключи для хранения данных в AsyncStorage
 */
const STORAGE_KEYS = {
  AUTH_TOKEN: 'authToken',
  USER_ID: 'userId',
} as const;

/**
 * Ошибка API чатов
 */
class ChatServiceError extends Error {
  constructor(
    message: string,
    public statusCode?: number,
    public endpoint?: string
  ) {
    super(message);
    this.name = 'ChatServiceError';
  }
}

// ==================== ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ====================

/**
 * Получает токен аутентификации из хранилища
 */
const getAuthToken = async (): Promise<string> => {
  const token = await AsyncStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);
  if (!token) {
    throw new ChatServiceError('Требуется авторизация', 401);
  }
  return token;
};

/**
 * Получает ID текущего пользователя из хранилища
 */
const getCurrentUserId = async (): Promise<number> => {
  const userIdStr = await AsyncStorage.getItem(STORAGE_KEYS.USER_ID);
  if (!userIdStr) {
    throw new ChatServiceError('Идентификатор пользователя не найден', 401);
  }

  const userId = parseInt(userIdStr, 10);
  if (isNaN(userId)) {
    throw new ChatServiceError('Некорректный идентификатор пользователя', 400);
  }

  return userId;
};

/**
 * Выполняет HTTP запрос с авторизацией
 */
const fetchWithAuth = async <T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> => {
  const token = await getAuthToken();
  const url = `${API_BASE}${endpoint}`;

  const defaultHeaders = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  };

  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        ...defaultHeaders,
        ...options.headers,
      },
    });

    // Обработка неавторизованного доступа
    if (response.status === 401) {
      await AsyncStorage.removeItem(STORAGE_KEYS.AUTH_TOKEN);
      throw new ChatServiceError('Сессия истекла. Требуется повторная авторизация', 401, endpoint);
    }

    const responseText = await response.text();

    if (!response.ok) {
      let errorMessage = `Ошибка ${response.status}`;

      // Пытаемся извлечь сообщение об ошибке из ответа
      if (responseText) {
        try {
          const errorData = JSON.parse(responseText);
          errorMessage = errorData.message || errorData.error || errorMessage;
        } catch {
          errorMessage = `${errorMessage}: ${responseText.substring(0, 100)}`;
        }
      }

      throw new ChatServiceError(errorMessage, response.status, endpoint);
    }

    // Пустой ответ (например, для успешных операций без тела)
    if (!responseText) {
      return {} as T;
    }

    return JSON.parse(responseText) as T;
  } catch (error) {
    if (error instanceof ChatServiceError) {
      throw error;
    }

    // Ошибки сети
    if (error instanceof TypeError) {
      throw new ChatServiceError('Ошибка сети. Проверьте подключение к серверу', 0, endpoint);
    }

    throw new ChatServiceError('Неизвестная ошибка при выполнении запроса', 0, endpoint);
  }
};

// ==================== ВНУТРЕННИЕ ФУНКЦИИ ====================

/**
 * Валидирует данные для создания чата
 */
const validateCreateChatData = (data: CreateChatRequest): void => {
  if (data.type === 'PRIVATE') {
    if (!data.targetUserId || data.targetUserId <= 0) {
      throw new ChatServiceError('Некорректный ID пользователя для приватного чата', 400);
    }
  } else {
    if (!data.name?.trim()) {
      throw new ChatServiceError('Название чата обязательно для групповых и публичных чатов', 400);
    }

    if (data.name.trim().length < 1 || data.name.trim().length > 100) {
      throw new ChatServiceError('Название чата должно содержать от 1 до 100 символов', 400);
    }

    if (data.description && data.description.length > 500) {
      throw new ChatServiceError('Описание чата не должно превышать 500 символов', 400);
    }

    if (data.maxParticipants && (data.maxParticipants < 2 || data.maxParticipants > 10000)) {
      throw new ChatServiceError('Количество участников должно быть от 2 до 10000', 400);
    }
  }
};


/**
 * Создает приватный чат
 */
const createPrivateChat = async (data: CreateChatRequest, currentUserId: number): Promise<ChatRoom> => {
  if (!data.targetUserId) {
    throw new ChatServiceError('Для создания приватного чата необходим ID собеседника', 400);
  }

  const endpoint = `/chat-rooms/private?user1Id=${currentUserId}&user2Id=${data.targetUserId}&createdBy=${currentUserId}`;

  return await fetchWithAuth<ChatRoom>(endpoint, {
    method: 'POST',
  });
};

/**
 * Создает групповой или публичный чат
 */
const createGroupChat = async (data: CreateChatRequest): Promise<ChatRoom> => {
  const requestBody = {
    name: data.name!.trim(), // Проверено в validateCreateChatData
    type: data.type,
    description: data.description || '',
    avatarUrl: data.avatarUrl || '',
    maxParticipants: data.maxParticipants || (data.type === 'GROUP' ? 50 : 1000),
  };

  return await fetchWithAuth<ChatRoom>('/chat-rooms', {
    method: 'POST',
    body: JSON.stringify(requestBody),
  });
};

// ==================== СЕРВИС ЧАТОВ ====================

/**
 * Сервис для работы с чатами
 *
 * @service
 * @description
 * Предоставляет методы для получения списка чатов и создания новых чатов.
 * Поддерживает различные типы чатов (PRIVATE, GROUP, PUBLIC) с разными
 * параметрами создания.
 */
export const ChatService = {
  /**
   * Получает список чатов текущего пользователя
   *
   * @returns {Promise<ChatRoom[]>} Список чатов пользователя
   * @throws {ChatServiceError} При ошибке загрузки
   */
  async getChatRooms(): Promise<ChatRoom[]> {
    try {
      const chatRooms = await fetchWithAuth<ChatRoom[]>('/chat-rooms', {
        method: 'GET',
      });

      if (!Array.isArray(chatRooms)) {
        console.warn('API вернул не массив чатов, преобразуем в массив');
        return chatRooms ? [chatRooms] : [];
      }

      console.log(`Загружено ${chatRooms.length} чатов`);
      return chatRooms;
    } catch (error) {
      console.error('Ошибка загрузки чатов:', error);

      // Возвращаем пустой массив для некритичных ошибок
      if (error instanceof ChatServiceError && error.statusCode !== 401) {
        console.warn('Возвращаем пустой массив из-за ошибки:', error.message);
        return [];
      }

      throw error;
    }
  },

  /**
   * Получает чат по его ID
   *
   * @param {number} chatId - ID чата для загрузки
   * @returns {Promise<ChatRoom>} Данные чата
   * @throws {ChatServiceError} При ошибке загрузки
   */
  async getChatById(chatId: number): Promise<ChatRoom> {
    try {
      return await fetchWithAuth<ChatRoom>(`/chat-rooms/${chatId}`, {
        method: 'GET',
      });
    } catch (error) {
      console.error('Ошибка загрузки чата по ID:', error);
      throw error;
    }
  },

  /**
   * Создает новый чат
   *
   * @param {CreateChatRequest} data - Данные для создания чата
   * @returns {Promise<ChatRoom>} Созданный чат
   * @throws {ChatServiceError} При ошибке создания
   */
  async createChat(data: CreateChatRequest): Promise<ChatRoom> {
    try {
      // Валидация входных данных
      validateCreateChatData(data);

      const currentUserId = await getCurrentUserId();

      if (data.type === 'PRIVATE') {
        return await createPrivateChat(data, currentUserId);
      } else {
        return await createGroupChat(data);
      }
    } catch (error) {
      console.error('Ошибка создания чата:', error);
      throw error;
    }
  },
};

// Экспорт класса ошибок для использования в других модулях
export { ChatServiceError };