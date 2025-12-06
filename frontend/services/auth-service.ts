// services/auth-service.ts
import AsyncStorage from "@react-native-async-storage/async-storage";
import { User, LoginCredentials, RegisterData } from "@/constants/types";

// ==================== КОНСТАНТЫ И ТИПЫ ====================

/**
 * Базовый URL API сервера
 */
const API_BASE_URL = "http://localhost:8080/api";

/**
 * Ключи для хранения данных в AsyncStorage
 */
const STORAGE_KEYS = {
  AUTH_TOKEN: 'authToken',
  USER_ID: 'userId',
  USERNAME: 'username',
  EMAIL: 'email',
} as const;

/**
 * Ответ сервера на успешную аутентификацию
 */
interface AuthResponse {
  accessToken: string;
  tokenType: "Bearer";
  userId: number;
  username: string;
  email: string;
}

/**
 * Ошибка аутентификации с дополнительной информацией
 */
class AuthError extends Error {
  constructor(
    message: string,
    public statusCode?: number,
    public responseText?: string
  ) {
    super(message);
    this.name = 'AuthError';
  }
}

// ==================== ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ====================

/**
 * Сохраняет данные аутентификации в AsyncStorage
 */
const storeAuthData = async (responseData: AuthResponse): Promise<void> => {
  try {
    await Promise.all([
      AsyncStorage.setItem(STORAGE_KEYS.AUTH_TOKEN, responseData.accessToken),
      AsyncStorage.setItem(STORAGE_KEYS.USER_ID, responseData.userId.toString()),
      AsyncStorage.setItem(STORAGE_KEYS.USERNAME, responseData.username),
      AsyncStorage.setItem(STORAGE_KEYS.EMAIL, responseData.email),
    ]);
  } catch (error) {
    console.error('Ошибка сохранения данных аутентификации:', error);
    throw new AuthError('Не удалось сохранить данные сессии');
  }
};

/**
 * Очищает данные аутентификации из AsyncStorage
 */
const clearAuthData = async (): Promise<void> => {
  try {
    await Promise.all(
      Object.values(STORAGE_KEYS).map(key =>
        AsyncStorage.removeItem(key)
      )
    );
  } catch (error) {
    console.error('Ошибка очистки данных аутентификации:', error);
    throw new AuthError('Не удалось завершить сессию');
  }
};

/**
 * Выполняет HTTP запрос с обработкой ошибок
 */
const fetchWithAuth = async <T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> => {
  const url = `${API_BASE_URL}${endpoint}`;
  const defaultHeaders = {
    'Content-Type': 'application/json',
  };

  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        ...defaultHeaders,
        ...options.headers,
      },
    });

    const responseText = await response.text();

    if (!response.ok) {
      console.error(`HTTP ${response.status}: ${responseText}`);

      // Пытаемся распарсить JSON ошибки
      let errorMessage = `Ошибка ${response.status}`;
      try {
        const errorData = JSON.parse(responseText);
        errorMessage = errorData.message || errorMessage;
      } catch {
        // Если не JSON, используем текст ответа
        if (responseText) {
          errorMessage = `${errorMessage}: ${responseText}`;
        }
      }

      throw new AuthError(errorMessage, response.status, responseText);
    }

    // Пустой ответ (например, при logout)
    if (!responseText) {
      return {} as T;
    }

    return JSON.parse(responseText) as T;
  } catch (error) {
    if (error instanceof AuthError) {
      throw error;
    }

    console.error('Ошибка сети при выполнении запроса:', error);
    throw new AuthError('Ошибка сети. Проверьте подключение к интернету');
  }
};

// ==================== СЕРВИС АУТЕНТИФИКАЦИИ ====================

/**
 * Сервис для управления аутентификацией пользователя
 *
 * @service
 * @description
 * Предоставляет методы для входа, регистрации и выхода пользователя.
 * Управляет хранением токенов и данных пользователя в AsyncStorage.
 */
export const authService = {
  /**
   * Авторизация пользователя
   *
   * @param {LoginCredentials} credentials - Данные для входа
   * @returns {Promise<User>} Данные авторизованного пользователя
   * @throws {AuthError} При ошибке аутентификации
   */
  async signIn(credentials: LoginCredentials): Promise<User> {
    console.log('Попытка авторизации пользователя:', credentials.email);

    try {
      const responseData = await fetchWithAuth<AuthResponse>('/auth/login', {
        method: 'POST',
        body: JSON.stringify(credentials),
      });

      // Сохраняем данные аутентификации
      await storeAuthData(responseData);

      console.log('Авторизация успешна:', {
        userId: responseData.userId,
        username: responseData.username,
      });

      return {
        id: responseData.userId,
        email: responseData.email,
        username: responseData.username,
      };
    } catch (error) {
      console.error('Ошибка авторизации:', error);

      // Очищаем данные при ошибке
      try {
        await clearAuthData();
      } catch {
        // Игнорируем ошибки очистки
      }

      throw error;
    }
  },

  /**
   * Регистрация нового пользователя
   *
   * @param {RegisterData} signUpData - Данные для регистрации
   * @returns {Promise<User>} Данные зарегистрированного пользователя
   * @throws {AuthError} При ошибке регистрации
   */
  async signUp(signUpData: RegisterData): Promise<User> {
    console.log('Попытка регистрации пользователя:', signUpData.username);

    try {
      // Преобразуем имена полей к формату ожидаемому бэкендом
      const requestData = {
        email: signUpData.email,
        password: signUpData.password,
        username: signUpData.username,
        first_name: signUpData.firstName,
        last_name: signUpData.lastName,
      };

      const responseData = await fetchWithAuth<AuthResponse>('/auth/register', {
        method: 'POST',
        body: JSON.stringify(requestData),
      });

      // Сохраняем данные аутентификации
      await storeAuthData(responseData);

      console.log('Регистрация успешна:', {
        userId: responseData.userId,
        username: responseData.username,
      });

      return {
        id: responseData.userId,
        email: responseData.email,
        username: responseData.username,
      };
    } catch (error) {
      console.error('Ошибка регистрации:', error);

      // Очищаем данные при ошибке
      try {
        await clearAuthData();
      } catch {
        // Игнорируем ошибки очистки
      }

      throw error;
    }
  },

  /**
   * Выход из системы
   *
   * @returns {Promise<void>}
   * @throws {AuthError} При ошибке выхода
   */
  async signOut(): Promise<void> {
    console.log('Завершение сессии пользователя');

    try {
      // Отправляем запрос на сервер для инвалидации токена
      try {
        await fetchWithAuth('/auth/logout', {
          method: 'POST',
        });
      } catch (error) {
        // Логируем, но не прерываем процесс выхода
        console.warn('Ошибка при вызове logout API:', error);
      }

      // Очищаем локальные данные
      await clearAuthData();

      console.log('Сессия успешно завершена');
    } catch (error) {
      console.error('Ошибка при выходе из системы:', error);
      throw error;
    }
  },

  /**
   * Получает текущий токен аутентификации
   *
   * @returns {Promise<string | null>} Токен или null если не авторизован
   */
  async getToken(): Promise<string | null> {
    try {
      return await AsyncStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);
    } catch (error) {
      console.error('Ошибка получения токена:', error);
      return null;
    }
  },

  /**
   * Проверяет, авторизован ли пользователь
   *
   * @returns {Promise<boolean>} true если пользователь авторизован
   */
  async isAuthenticated(): Promise<boolean> {
    try {
      const token = await this.getToken();
      return !!token;
    } catch (error) {
      console.error('Ошибка проверки авторизации:', error);
      return false;
    }
  },
};

// Экспорт типов для использования в других модулях
export type { LoginCredentials, RegisterData };
export { AuthError };