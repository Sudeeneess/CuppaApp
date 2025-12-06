// services/message-service.ts
import AsyncStorage from "@react-native-async-storage/async-storage";
import { Message, SendMessageRequest, PagedMessages } from "@/constants/types";

const BASE_URL = "http://localhost:8080";
const API_BASE = `${BASE_URL}/api`;

/**
 * Сервис для работы с сообщениями
 */
export const MessageService = {
  /**
   * Получает сообщения чата с пагинацией
   */
  async getChatMessages(
    chatId: number,
    page = 0,
    size = 50
  ): Promise<Message[]> {
    try {
      const token = await AsyncStorage.getItem("authToken");
      if (!token) throw new Error("Требуется авторизация");

      const response = await fetch(
        `${API_BASE}/messages/chat/${chatId}?page=${page}&size=${size}`,
        {
          headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Ошибка загрузки сообщений: ${response.status} - ${errorText}`);
      }

      const data = await response.json();

      // Обработка разных форматов ответа
      if (Array.isArray(data)) {
        return data;
      } else if (data.content && Array.isArray(data.content)) {
        return data.content;
      } else {
        console.warn("Неизвестный формат ответа:", data);
        return [];
      }
    } catch (error) {
      console.error("Ошибка загрузки сообщений:", error);
      throw error;
    }
  },

  /**
   * Отправляет новое сообщение
   */
  async sendMessage(data: SendMessageRequest): Promise<Message> {
    try {
      const token = await AsyncStorage.getItem("authToken");
      if (!token) throw new Error("Требуется авторизация");

      const response = await fetch(`${API_BASE}/messages`, {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Ошибка отправки: ${response.status} - ${errorText}`);
      }

      return await response.json();
    } catch (error) {
      console.error("Ошибка отправки сообщения:", error);
      throw error;
    }
  },

  /**
   * Получает последнее сообщение в чате
   */
  async getLastMessage(chatId: number): Promise<Message | null> {
    try {
      const token = await AsyncStorage.getItem("authToken");
      if (!token) throw new Error("Требуется авторизация");

      const response = await fetch(
        `${API_BASE}/messages/chat/${chatId}/last`,
        {
          headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      if (!response.ok) {
        // Если нет сообщений, возвращаем null
        if (response.status === 404) {
          return null;
        }
        const errorText = await response.text();
        throw new Error(`Ошибка: ${response.status} - ${errorText}`);
      }

      return await response.json();
    } catch (error) {
      console.error("Ошибка получения последнего сообщения:", error);
      return null;
    }
  },

  /**
   * Помечает сообщения как прочитанные
   */
  async markAsRead(chatId: number): Promise<void> {
    try {
      const token = await AsyncStorage.getItem("authToken");
      if (!token) return;

      await fetch(
        `${API_BASE}/messages/chat/${chatId}/mark-as-read`,
        {
          method: "PUT",
          headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
    } catch (error) {
      console.error("Ошибка отметки прочитанных:", error);
    }
  },
};