// chat-service.ts
import AsyncStorage from "@react-native-async-storage/async-storage";
import { ChatRoom, CreateChatRequest } from "@/constants/types";

// Читаем URL
const BASE_URL = "http://localhost:8080";

// 🛑 ЗАГЛУШКА: ID текущего пользователя.
// API требует передать его явно в user1Id и createdBy.
// В реальности ты должен брать его из своего UserContext или декодировать из JWT.
const MOCK_CURRENT_USER_ID = 2;

const getHeaders = async () => {
  const token = await AsyncStorage.getItem("authToken");
  if (!token) throw new Error("No auth token found");
  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  };
};

export const ChatService = {
  async getChatRooms(): Promise<ChatRoom[]> {
    try {
      const headers = await getHeaders();
      console.log("ghkgjhg" + headers.Authorization);
      const response = await fetch(`${BASE_URL}/api/chat-rooms`, {
        method: "GET",
        headers,
      });
      if (!response.ok) throw new Error("Failed to fetch");
      return await response.json();
    } catch (e) {
      console.error(e);
      return [];
    }
  },

  async createChat(data: CreateChatRequest): Promise<ChatRoom> {
    try {
      const headers = await getHeaders();

      // === ВЕТКА 1: ЛИЧНЫЕ СООБЩЕНИЯ ===
      if (data.type === "PRIVATE") {
        if (!data.targetUserId) {
          throw new Error("Для создания ЛС необходим targetUserId");
        }

        // Формируем Query Parameters согласно скриншоту 1000049723.jpg
        const params = new URLSearchParams({
          user1Id: MOCK_CURRENT_USER_ID.toString(), // Я (текущий юзер)
          user2Id: data.targetUserId.toString(), // Собеседник
          createdBy: MOCK_CURRENT_USER_ID.toString(), // Создатель (я)
        });

        console.log(`[POST] /api/chat-rooms/private?${params.toString()}`);

        const response = await fetch(
          `${BASE_URL}/api/chat-rooms/private?${params.toString()}`,
          {
            method: "POST",
            headers,
            // Тело запроса пустое или отсутствует, так как данные в параметрах
          }
        );

        if (!response.ok) {
          const err = await response.text();
          throw new Error(`Ошибка создания ЛС: ${err}`);
        }
        return await response.json();
      }

      // === ВЕТКА 2: ГРУППЫ И СЕРВЕРА ===
      else {
        console.log(`[POST] /api/chat-rooms (Body JSON)`);

        const response = await fetch(`${BASE_URL}/api/chat-rooms`, {
          method: "POST",
          headers,
          body: JSON.stringify({
            name: data.name,
            type: data.type,
            description: data.description,
            maxParticipants: data.maxParticipants,
            avatarUrl: data.avatarUrl,
          }),
        });

        if (!response.ok) {
          const err = await response.text();
          throw new Error(`Ошибка создания Группы: ${err}`);
        }
        return await response.json();
      }
    } catch (error) {
      console.error("Error creating chat:", error);
      throw error;
    }
  },
};
