import AsyncStorage from "@react-native-async-storage/async-storage";

// --- Интерфейсы ---

export interface User {
  id: number; // На основе документации, user id
  email: string;
  username: string;
  // firstName и lastName могут быть добавлены, если они нужны в клиентском User
}

export interface Credentials {
  email: string;
  password: string;
}

// Интерфейс для данных регистрации
// Соответствует Телу запроса в POST /auth/register
export interface SignUpData extends Credentials {
  username: string;
  firstName: string;
  lastName: string;
}

// Интерфейс ответа API для входа и регистрации
// Использует поля из "Ответ" для POST /auth/register и POST /auth/login
interface AuthResponse {
  accessToken: string;
  tokenType: "Bearer";
  userId: number; // 'id' пользователя
  username: string;
  email: string;
}

// --- Сервис API ---

// Установите базовый URL вашего бэкенда
// Используем "http://localhost:8080/api" из документации
const API_BASE_URL = "http://localhost:8080/api";

export const authService = {
  /**
   * Выполняет вход пользователя. Соответствует POST /api/auth/login
   */
  async signIn(credentials: Credentials): Promise<User> {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(credentials),
    });

    if (!response.ok) {
      // Пытаемся получить сообщение об ошибке, если оно есть
      try {
        const errorData = await response.json();
        throw new Error(
          errorData.message || "Ошибка входа. Проверьте учетные данные."
        );
      } catch {
        throw new Error(`Ошибка входа. Статус: ${response.status}`);
      }
    }

    // 1. Получаем данные из ответа
    const responseData: AuthResponse = await response.json();

    console.log("ploho" + responseData.accessToken);

    // 2. Сохраняем токен (accessToken)
    await AsyncStorage.setItem("authToken", responseData.accessToken);

    // 3. Возвращаем данные пользователя, очищенные от токена
    return {
      id: responseData.userId,
      email: responseData.email,
      username: responseData.username,
    };
  },

  /**
   * Выполняет регистрацию нового пользователя. Соответствует POST /api/auth/register
   */
  async signUp(signUpData: SignUpData): Promise<User> {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
      body: JSON.stringify(signUpData),
    });

    if (!response.ok) {
      try {
        const errorData = await response.json();
        throw new Error(errorData.message || "Ошибка регистрации.");
      } catch {
        throw new Error(`Ошибка регистрации. Статус: ${response.status}`);
      }
    }

    // 1. Получаем данные из ответа
    const responseData: AuthResponse = await response.json();

    // 2. Сохраняем токен (accessToken)
    await AsyncStorage.setItem("authToken", responseData.accessToken);

    // 3. Возвращаем данные пользователя
    return {
      id: responseData.userId,
      email: responseData.email,
      username: responseData.username,
    };
  },

  /**
   * Выполняет выход пользователя. Соответствует POST /api/auth/logout
   */
  async signOut(): Promise<void> {
    // 1. Получаем токен для запроса на выход
    const authToken = await AsyncStorage.getItem("authToken");

    // 2. Очистка токена/сессии на стороне клиента (первый приоритет)
    await AsyncStorage.removeItem("authToken");

    // 3. Запрос к API для инвалидации сессии/токена на бэкенде
    try {
      if (authToken) {
        await fetch(`${API_BASE_URL}/auth/logout`, {
          method: "POST",
          headers: {
            Authorization: `Bearer ${authToken}`,
            "Content-Type": "application/json",
          },
        });
      }
      // Ошибки при logout часто игнорируются, так как клиентская очистка уже прошла
    } catch (e) {
      console.error("Ошибка при запросе /api/auth/logout:", e);
    }
  },
};
