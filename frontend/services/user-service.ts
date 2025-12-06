// services/user-service.ts
import AsyncStorage from "@react-native-async-storage/async-storage";
import { UserSearchResult } from "@/constants/types";

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
} as const;

/**
 * Ошибка сервиса пользователей
 */
class UserServiceError extends Error {
  constructor(
    message: string,
    public statusCode?: number,
    public endpoint?: string
  ) {
    super(message);
    this.name = 'UserServiceError';
  }
}

// ==================== ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ====================

/**
 * Получает токен аутентификации из хранилища
 */
const getAuthToken = async (): Promise<string> => {
  const token = await AsyncStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);
  if (!token) {
    throw new UserServiceError('Требуется авторизация', 401);
  }
  return token;
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
      throw new UserServiceError('Сессия истекла. Требуется повторная авторизация', 401, endpoint);
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

      throw new UserServiceError(errorMessage, response.status, endpoint);
    }

    // Пустой ответ
    if (!responseText) {
      return [] as T;
    }

    return JSON.parse(responseText) as T;
  } catch (error) {
    if (error instanceof UserServiceError) {
      throw error;
    }

    // Ошибки сети
    if (error instanceof TypeError) {
      throw new UserServiceError('Ошибка сети. Проверьте подключение к серверу', 0, endpoint);
    }

    throw new UserServiceError('Неизвестная ошибка при выполнении запроса', 0, endpoint);
  }
};

// ==================== СЕРВИС ПОЛЬЗОВАТЕЛЕЙ ====================

/**
 * Сервис для работы с пользователями
 *
 * @service
 * @description
 * Предоставляет методы для поиска и получения информации о пользователях.
 * Используется для создания приватных чатов и поиска собеседников.
 */
export const UserService = {
  /**
   * Получает список всех пользователей системы
   *
   * @returns {Promise<UserSearchResult[]>} Список пользователей
   * @throws {UserServiceError} При ошибке загрузки
   *
   * @description
   * Возвращает список всех зарегистрированных пользователей системы,
   * исключая текущего пользователя. Используется для выбора собеседника
   * при создании приватного чата.
   */
  async getAllUsers(): Promise<UserSearchResult[]> {
    try {
      const users = await fetchWithAuth<UserSearchResult[]>('/users', {
        method: 'GET',
      });

      if (!Array.isArray(users)) {
        console.warn('API вернул не массив пользователей, преобразуем в массив');
        return users ? [users] : [];
      }

      console.log(`Загружено ${users.length} пользователей`);
      return users;
    } catch (error) {
      console.error('Ошибка загрузки пользователей:', error);

      // Для некритичных ошибок возвращаем пустой массив
      if (error instanceof UserServiceError && error.statusCode !== 401) {
        console.warn('Возвращаем пустой массив пользователей из-за ошибки:', error.message);
        return [];
      }

      throw error;
    }
  },

  /**
   * Ищет пользователей по запросу
   *
   * @param {string} query - Поисковый запрос
   * @returns {Promise<UserSearchResult[]>} Отфильтрованный список пользователей
   * @throws {UserServiceError} При ошибке поиска
   *
   * @description
   * Выполняет поиск пользователей по имени, username или email.
   * Если запрос пустой, возвращает всех пользователей.
   */
  async searchUsers(query: string): Promise<UserSearchResult[]> {
    try {
      // Валидация запроса
      const searchQuery = query.trim();

      if (!searchQuery) {
        return await this.getAllUsers();
      }

      // Получаем всех пользователей и фильтруем локально
      // В будущем можно заменить на серверный поиск
      const allUsers = await this.getAllUsers();

      const filteredUsers = allUsers.filter(user => {
        const searchLower = searchQuery.toLowerCase();

        return (
          user.username.toLowerCase().includes(searchLower) ||
          user.email.toLowerCase().includes(searchLower) ||
          (user.firstName && user.firstName.toLowerCase().includes(searchLower)) ||
          (user.lastName && user.lastName.toLowerCase().includes(searchLower))
        );
      });

      console.log(`Найдено ${filteredUsers.length} пользователей по запросу: "${searchQuery}"`);
      return filteredUsers;
    } catch (error) {
      console.error('Ошибка поиска пользователей:', error);
      throw error;
    }
  },

  /**
   * Получает информацию о конкретном пользователе по ID
   *
   * @param {number} userId - ID пользователя
   * @returns {Promise<UserSearchResult>} Информация о пользователе
   * @throws {UserServiceError} При ошибке загрузки
   *
   * @description
   * Возвращает подробную информацию о пользователе по его ID.
   * Может использоваться для отображения профиля собеседника.
   */
  async getUserById(userId: number): Promise<UserSearchResult> {
    try {
      if (!userId || userId <= 0) {
        throw new UserServiceError('Некорректный ID пользователя', 400);
      }

      // Предполагаем, что API имеет endpoint для получения пользователя по ID
      // Если нет - фильтруем из общего списка
      const allUsers = await this.getAllUsers();
      const user = allUsers.find(u => u.id === userId);

      if (!user) {
        throw new UserServiceError(`Пользователь с ID ${userId} не найден`, 404);
      }

      return user;
    } catch (error) {
      console.error('Ошибка получения пользователя по ID:', error);
      throw error;
    }
  },

  /**
   * Получает информацию о текущем пользователе
   *
   * @returns {Promise<UserSearchResult>} Информация о текущем пользователе
   * @throws {UserServiceError} При ошибке загрузки
   */
  async getCurrentUser(): Promise<UserSearchResult> {
    try {
      // Получаем ID текущего пользователя из AsyncStorage
      const userIdStr = await AsyncStorage.getItem('userId');
      if (!userIdStr) {
        throw new UserServiceError('Идентификатор текущего пользователя не найден', 401);
      }

      const userId = parseInt(userIdStr, 10);
      if (isNaN(userId)) {
        throw new UserServiceError('Некорректный идентификатор пользователя', 400);
      }

      // Используем общий метод для получения пользователя по ID
      return await this.getUserById(userId);
    } catch (error) {
      console.error('Ошибка получения текущего пользователя:', error);
      throw error;
    }
  },

  /**
   * Фильтрует список пользователей, исключая текущего
   *
   * @param {UserSearchResult[]} users - Исходный список пользователей
   * @param {number} currentUserId - ID текущего пользователя
   * @returns {UserSearchResult[]} Отфильтрованный список
   */
  filterOutCurrentUser(users: UserSearchResult[], currentUserId: number): UserSearchResult[] {
    return users.filter(user => user.id !== currentUserId);
  },
};

// Экспорт класса ошибок для использования в других модулях
export { UserServiceError };