// hooks/use-chat.ts
import { useContext } from "react";
import { ChatContext } from "@/contexts/chat-context";

/**
 * Хук для доступа к контексту чатов
 *
 * @hook
 * @returns {ChatContextType} Объект контекста чатов
 * @throws {Error} Если хук используется вне ChatProvider
 *
 * @description
 * Предоставляет доступ к глобальному состоянию чатов, методам загрузки
 * и создания чатов. Включает проверку на использование вне провайдера
 * для предотвращения ошибок времени выполнения.
 *
 * @example
 * // Использование в компоненте
 * const { chats, loadingState, refreshChats, createChat } = useChat();
 *
 * useEffect(() => {
 *   if (loadingState === 'idle') {
 *     refreshChats();
 *   }
 * }, [loadingState, refreshChats]);
 *
 * const handleCreateChat = async () => {
 *   try {
 *     await createChat({
 *       type: 'PRIVATE',
 *       targetUserId: 123,
 *     });
 *   } catch (error) {
 *     console.error('Не удалось создать чат:', error);
 *   }
 * };
 */
export const useChat = () => {
  // Получаем контекст чатов
  const context = useContext(ChatContext);

  // Проверяем, что хук используется внутри ChatProvider
  if (!context) {
    const errorMessage = [
      'Ошибка: useChat вызван вне ChatProvider.',
      'Убедитесь, что ваш компонент обернут в ChatProvider.',
      'Пример правильной структуры:',
      '',
      '<ChatProvider>',
      '  <YourComponent />',
      '</ChatProvider>',
    ].join('\n');

    throw new Error(errorMessage);
  }

  // Логирование для отладки (только в dev режиме)
  if (__DEV__) {
    console.debug('useChat: текущее состояние', {
      chatsCount: context.chats.length,
      loadingState: context.loadingState,
      hasError: !!context.error,
      hasRefresh: typeof context.refreshChats === 'function',
      hasCreate: typeof context.createChat === 'function',
      hasGetById: typeof context.getChatById === 'function',
    });
  }

  return context;
};

/**
 * Хук для работы с конкретным чатом по ID
 *
 * @hook
 * @param {number} chatId - ID чата для поиска
 * @returns {ChatRoom | undefined} Найденный чат или undefined
 *
 * @description
 * Вспомогательный хук для удобного поиска чата по ID.
 * Использует метод getChatById из контекста чатов.
 *
 * @example
 * const chat = useChatById(123);
 * if (chat) {
 *   console.log('Найден чат:', chat.name);
 * } else {
 *   console.log('Чат не найден');
 * }
 */
export const useChatById = (chatId: number) => {
  const { getChatById } = useChat();
  return getChatById(chatId);
};

/**
 * Хук для проверки состояния загрузки чатов
 *
 * @hook
 * @returns {Object} Объект с флагами состояний загрузки
 * @returns {boolean} isIdle - Начальное состояние
 * @returns {boolean} isLoading - Идет загрузка
 * @returns {boolean} isSuccess - Загрузка успешна
 * @returns {boolean} isError - Произошла ошибка
 *
 * @description
 * Предоставляет удобные флаги для проверки состояния загрузки чатов
 * в компонентах UI.
 *
 * @example
 * const { isLoading, isSuccess, isError } = useChatLoading();
 *
 * if (isLoading) return <LoadingSpinner />;
 * if (isError) return <ErrorMessage />;
 * if (isSuccess) return <ChatList />;
 */
export const useChatLoading = () => {
  const { loadingState } = useChat();

  return {
    isIdle: loadingState === 'idle',
    isLoading: loadingState === 'loading',
    isSuccess: loadingState === 'succeeded',
    isError: loadingState === 'failed',
  };
};