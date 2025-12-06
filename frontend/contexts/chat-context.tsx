// contexts/chat-context.tsx
import React, {
  createContext,
  useState,
  useEffect,
  ReactNode,
  useCallback,
  useRef,
} from "react";
import { ChatRoom, CreateChatRequest, LoadingState } from "@/constants/types";
import { ChatService } from "@/services/chat-service";

// ==================== ТИПЫ И ИНТЕРФЕЙСЫ ====================

/**
 * Контекст чатов предоставляет глобальный доступ к списку чатов пользователя
 */
export interface ChatContextType {
  chats: ChatRoom[];
  loadingState: LoadingState;
  error: string | null;
  refreshChats: () => Promise<void>;
  createChat: (data: CreateChatRequest) => Promise<ChatRoom>;
  getChatById: (chatId: number) => ChatRoom | undefined;
}

// ==================== СОЗДАНИЕ КОНТЕКСТА ====================

/**
 * Контекст чатов
 */
export const ChatContext = createContext<ChatContextType | undefined>(undefined);

// ==================== ПРОВАЙДЕР ЧАТОВ ====================

/**
 * Провайдер чатов для управления глобальным состоянием списка чатов
 *
 * @component
 * @param {Object} props - Свойства компонента
 * @param {ReactNode} props.children - Дочерние компоненты
 *
 * @description
 * Предоставляет глобальный доступ к списку чатов пользователя,
 * управляет загрузкой, обновлением и созданием чатов.
 * Автоматически загружает чаты при монтировании и обрабатывает ошибки.
 */
export const ChatProvider = ({ children }: { children: ReactNode }) => {
  // Состояние чатов
  const [chats, setChats] = useState<ChatRoom[]>([]);
  const [loadingState, setLoadingState] = useState<LoadingState>('idle');
  const [error, setError] = useState<string | null>(null);

  // Ref для предотвращения двойных загрузок
  const isFetchingRef = useRef(false);

  /**
   * Загружает список чатов пользователя
   */
  const refreshChats = useCallback(async () => {
    // Предотвращаем двойную загрузку
    if (isFetchingRef.current) {
      console.log('Загрузка чатов уже выполняется, пропускаем...');
      return;
    }

    try {
      isFetchingRef.current = true;
      setLoadingState('loading');
      setError(null);

      const chatList = await ChatService.getChatRooms();

      setChats(chatList);
      setLoadingState('succeeded');

      console.log(`Загружено ${chatList.length} чатов`);
    } catch (err: any) {
      console.error('Ошибка загрузки чатов:', err);

      setError(err.message || 'Не удалось загрузить список чатов');
      setLoadingState('failed');
      setChats([]);
    } finally {
      isFetchingRef.current = false;
    }
  }, []);

  /**
   * Создает новый чат и обновляет список
   */
  const createChat = useCallback(async (data: CreateChatRequest): Promise<ChatRoom> => {
    try {
      setLoadingState('loading');

      const newChat = await ChatService.createChat(data);

      // Добавляем новый чат в начало списка
      setChats(prev => [newChat, ...prev]);
      setLoadingState('succeeded');

      console.log(`Чат "${newChat.name || `ID: ${newChat.id}`}" успешно создан`);
      return newChat;
    } catch (err: any) {
      console.error('Ошибка создания чата:', err);

      setError(err.message || 'Не удалось создать чат');
      setLoadingState('failed');
      throw err;
    }
  }, []);

  /**
   * Находит чат по ID
   */
  const getChatById = useCallback((chatId: number): ChatRoom | undefined => {
    return chats.find(chat => chat.id === chatId);
  }, [chats]);

  /**
   * Загружает чаты при монтировании компонента
   */
  useEffect(() => {
    const initializeChats = async () => {
      if (loadingState === 'idle') {
        await refreshChats();
      }
    };

    initializeChats();
  }, [loadingState, refreshChats]);

  /**
   * Обработка ошибок загрузки
   */
  useEffect(() => {
    if (error) {
      console.warn('Ошибка в ChatProvider:', error);
      // Здесь можно добавить логику для уведомления пользователя
      // или автоматического повтора загрузки
    }
  }, [error]);

  /**
   * Логирование для отладки (только в dev режиме)
   */
  useEffect(() => {
    if (__DEV__ && chats.length > 0) {
      console.log('Текущие чаты:', {
        count: chats.length,
        chats: chats.map(chat => ({
          id: chat.id,
          name: chat.name || 'Без названия',
          type: chat.type,
          participants: chat.participants?.length || 0,
        }))
      });
    }
  }, [chats]);

  // Значение контекста
  const contextValue: ChatContextType = {
    chats,
    loadingState,
    error,
    refreshChats,
    createChat,
    getChatById,
  };

  return (
    <ChatContext.Provider value={contextValue}>
      {children}
    </ChatContext.Provider>
  );
};