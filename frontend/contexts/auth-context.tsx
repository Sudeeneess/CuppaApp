// contexts/auth-context.tsx
import { ReactNode, createContext, useState, useEffect, useCallback } from "react";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { useRouter } from "expo-router";
import { authService, User } from "@/services/auth-service";

// ==================== ТИПЫ И ИНТЕРФЕЙСЫ ====================

/**
 * Данные для входа пользователя
 */
export interface LoginCredentials {
  email: string;
  password: string;
}

/**
 * Данные для регистрации пользователя
 */
export interface RegisterData {
  email: string;
  password: string;
  username: string;
  firstName: string;
  lastName: string;
}

/**
 * Контекст аутентификации предоставляет методы для управления сессией пользователя
 */
export interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  signIn: (credentials: LoginCredentials) => Promise<void>;
  signUp: (data: RegisterData) => Promise<void>;
  signOut: () => Promise<void>;
  clearAuth: () => Promise<void>;
}

/**
 * Ключи для хранения данных аутентификации в AsyncStorage
 */
const STORAGE_KEYS = {
  AUTH_TOKEN: 'authToken',
  USER_ID: 'userId',
  USERNAME: 'username',
  EMAIL: 'email',
} as const;

// ==================== СОЗДАНИЕ КОНТЕКСТА ====================

/**
 * Контекст аутентификации
 */
export const AuthContext = createContext<AuthContextType | undefined>(undefined);

// ==================== ПРОВАЙДЕР АУТЕНТИФИКАЦИИ ====================

/**
 * Провайдер аутентификации для управления пользовательской сессией
 *
 * @component
 * @param {Object} props - Свойства компонента
 * @param {ReactNode} props.children - Дочерние компоненты
 *
 * @description
 * Обеспечивает глобальное состояние аутентификации, управление токенами
 * и методами входа/выхода. Автоматически восстанавливает сессию при запуске приложения.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const router = useRouter();

  // Состояние аутентификации
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  /**
   * Восстанавливает сессию пользователя из AsyncStorage
   */
  const restoreSession = useCallback(async () => {
    setIsLoading(true);

    try {
      const [token, userIdStr, username, email] = await Promise.all([
        AsyncStorage.getItem(STORAGE_KEYS.AUTH_TOKEN),
        AsyncStorage.getItem(STORAGE_KEYS.USER_ID),
        AsyncStorage.getItem(STORAGE_KEYS.USERNAME),
        AsyncStorage.getItem(STORAGE_KEYS.EMAIL),
      ]);

      // Проверяем наличие всех необходимых данных
      if (token && userIdStr && username && email) {
        const userId = parseInt(userIdStr, 10);

        if (!isNaN(userId)) {
          setUser({
            id: userId,
            username,
            email,
          });
          console.log(`Сессия восстановлена для пользователя: ${username}`);
        } else {
          console.warn('Некорректный ID пользователя в хранилище');
          await clearStoredAuth();
        }
      }
    } catch (error) {
      console.error('Ошибка восстановления сессии:', error);
    } finally {
      setIsLoading(false);
    }
  }, []);

  /**
   * Сохраняет данные аутентификации в AsyncStorage
   */
  const storeAuthData = useCallback(async (userData: User, token: string) => {
    try {
      await Promise.all([
        AsyncStorage.setItem(STORAGE_KEYS.AUTH_TOKEN, token),
        AsyncStorage.setItem(STORAGE_KEYS.USER_ID, userData.id.toString()),
        AsyncStorage.setItem(STORAGE_KEYS.USERNAME, userData.username),
        AsyncStorage.setItem(STORAGE_KEYS.EMAIL, userData.email),
      ]);
    } catch (error) {
      console.error('Ошибка сохранения данных аутентификации:', error);
      throw new Error('Не удалось сохранить данные сессии');
    }
  }, []);

  /**
   * Очищает данные аутентификации из AsyncStorage
   */
  const clearStoredAuth = useCallback(async () => {
    try {
      await Promise.all(
        Object.values(STORAGE_KEYS).map(key =>
          AsyncStorage.removeItem(key)
        )
      );
    } catch (error) {
      console.error('Ошибка очистки данных аутентификации:', error);
    }
  }, []);

  // Восстановление сессии при монтировании
  useEffect(() => {
    restoreSession();
  }, [restoreSession]);

  /**
   * Авторизация пользователя
   */
  const signIn = useCallback(async (credentials: LoginCredentials) => {
    try {
      setIsLoading(true);

      const userData = await authService.signIn(credentials);

      // Предполагаем, что authService.signIn возвращает токен вместе с userData
      // Если нет, нужно получить токен отдельно
      const token = await AsyncStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);

      if (!token) {
        throw new Error('Токен аутентификации не получен');
      }

      await storeAuthData(userData, token);
      setUser(userData);

      console.log(`Пользователь ${userData.username} успешно авторизован`);
      router.replace("/(app)");
    } catch (error) {
      console.error('Ошибка авторизации:', error);
      await clearStoredAuth();
      throw error;
    } finally {
      setIsLoading(false);
    }
  }, [router, storeAuthData, clearStoredAuth]);

  /**
   * Регистрация нового пользователя
   */
  const signUp = useCallback(async (data: RegisterData) => {
    try {
      setIsLoading(true);

      const userData = await authService.signUp(data);

      // Получаем токен после успешной регистрации
      const token = await AsyncStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);

      if (!token) {
        throw new Error('Токен аутентификации не получен');
      }

      await storeAuthData(userData, token);
      setUser(userData);

      console.log(`Пользователь ${userData.username} успешно зарегистрирован`);
      router.replace("/(app)");
    } catch (error) {
      console.error('Ошибка регистрации:', error);
      await clearStoredAuth();
      throw error;
    } finally {
      setIsLoading(false);
    }
  }, [router, storeAuthData, clearStoredAuth]);

  /**
   * Выход из системы
   */
  const signOut = useCallback(async () => {
    try {
      setIsLoading(true);

      await authService.signOut();
      await clearStoredAuth();
      setUser(null);

      console.log('Пользователь успешно вышел из системы');
      router.replace("/(auth)");
    } catch (error) {
      console.error('Ошибка выхода из системы:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  }, [router, clearStoredAuth]);

  /**
   * Очистка данных аутентификации без вызова API
   */
  const clearAuth = useCallback(async () => {
    try {
      await clearStoredAuth();
      setUser(null);
      console.log('Данные аутентификации очищены');
    } catch (error) {
      console.error('Ошибка очистки аутентификации:', error);
      throw error;
    }
  }, [clearStoredAuth]);

  // Значение контекста
  const contextValue: AuthContextType = {
    user,
    isLoading,
    isAuthenticated: !!user,
    signIn,
    signUp,
    signOut,
    clearAuth,
  };

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
}