// chat-context.tsx
import React, {
  createContext,
  useState,
  useEffect,
  ReactNode,
  useCallback,
} from "react";
import { ChatRoom, CreateChatRequest } from "@/constants/types";
import { ChatService } from "@/services/chat-service";

interface ChatContextType {
  chats: ChatRoom[];
  isLoading: boolean;
  error: string | null;
  refreshChats: () => Promise<void>;
  createChat: (data: CreateChatRequest) => Promise<void>;
}

export const ChatContext = createContext<ChatContextType | undefined>(
  undefined
);

export const ChatProvider = ({ children }: { children: ReactNode }) => {
  const [chats, setChats] = useState<ChatRoom[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refreshChats = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await ChatService.getChatRooms();
      setChats(data);
      console.log(data);
    } catch (err) {
      setError("Не удалось загрузить список чатов");
    } finally {
      setIsLoading(false);
    }
  }, []);

  const createChat = async (data: CreateChatRequest) => {
    try {
      await ChatService.createChat(data);
      // После создания обновляем список
      await refreshChats();
    } catch (e) {
      console.error(e);
      throw e;
    }
  };

  useEffect(() => {
    refreshChats();
  }, [refreshChats]);

  return (
    <ChatContext.Provider
      value={{ chats, isLoading, error, refreshChats, createChat }}
    >
      {children}
    </ChatContext.Provider>
  );
};
