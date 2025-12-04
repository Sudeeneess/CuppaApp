import { ReactNode, createContext, useState, useEffect } from "react";
import { authService, User } from "@/services/auth-service";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { useRouter } from "expo-router";

type SignInFunction = (
  ...args: Parameters<typeof authService.signIn>
) => Promise<void>;
type SignUpFunction = (
  ...args: Parameters<typeof authService.signUp>
) => Promise<void>;
type SignOutFunction = typeof authService.signOut;

export const AuthContext = createContext<{
  user: User | null;
  isAuthenticated: boolean;
  signIn: SignInFunction;
  signUp: SignUpFunction;
  signOut: SignOutFunction;
} | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);

  const router = useRouter();

  // Здесь может быть логика проверки токена из AsyncStorage
  useEffect(() => {
    const checkAuthStatus = async () => {
      const token = await AsyncStorage.getItem("authToken");
      if (token) {
        // В идеале - сделать API-запрос для получения данных пользователя по токену
        setUser({ id: 1, username: "StoredUser", email: "stored@example.com" });
      }
    };
    checkAuthStatus();
    // В реальном приложении здесь будет логика проверки токена
  }, []);

  // Методы, вызывающие скрипты из сервиса
  const signIn: SignInFunction = async (credentials) => {
    try {
      const userData = await authService.signIn(credentials);
      setUser(userData);
      router.replace("/(app)");
    } catch (error) {
      // Обработка ошибки
      console.error("Login failed:", error);
      throw error; // Проброс ошибки для компонента
    } finally {
    }
  };

  const signUp: SignUpFunction = async (data) => {
    try {
      const userData = await authService.signUp(data);
      setUser(userData);
      router.replace("/(app)");
    } catch (error) {
      console.error("Registration failed:", error);
      throw error;
    } finally {
    }
  };

  const signOut = async () => {
    await authService.signOut();
    setUser(null);
    router.replace("/(auth)");
  };

  return (
    <AuthContext.Provider
      value={{ user, isAuthenticated: !!user, signIn, signUp, signOut }}
    >
      {children}
    </AuthContext.Provider>
  );
}
