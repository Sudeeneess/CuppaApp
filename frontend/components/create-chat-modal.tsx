// components/create-chat-modal.tsx
import React, { useState, useEffect, useCallback } from "react";
import { View, ScrollView, StyleSheet, FlatList } from "react-native";
import {
  Modal,
  Portal,
  Text,
  TextInput,
  Button,
  List,
  Avatar,
  Searchbar,
  ActivityIndicator,
  useTheme,
} from "react-native-paper";
import { ChatType, CreateChatRequest, UserSearchResult } from "@/constants/types";
import { UserService } from "@/services/user-service";
import { useAuth } from "@/hooks/use-auth";

interface CreateChatModalProps {
  visible: boolean;
  type: ChatType;
  onDismiss: () => void;
  onSubmit: (data: CreateChatRequest) => Promise<void>;
}

/**
 * Модальное окно для создания нового чата
 *
 * @component
 * @param {boolean} visible - Видимость модального окна
 * @param {ChatType} type - Тип создаваемого чата (PRIVATE/GROUP/PUBLIC)
 * @param {function} onDismiss - Callback для закрытия модального окна
 * @param {function} onSubmit - Callback для создания чата
 *
 * @description
 * Предоставляет интерфейс для создания чатов разных типов:
 * - PRIVATE: Поиск и выбор пользователя для личной переписки
 * - GROUP/PUBLIC: Создание группового чата с настройками названия и описания
 * Поддерживает поиск пользователей с дебаунсом для приватных чатов
 */
export const CreateChatModal: React.FC<CreateChatModalProps> = ({
  visible,
  type,
  onDismiss,
  onSubmit,
}) => {
  const theme = useTheme();
  const { user } = useAuth();

  // Состояние для приватных чатов
  const [searchQuery, setSearchQuery] = useState("");
  const [users, setUsers] = useState<UserSearchResult[]>([]);
  const [loadingUsers, setLoadingUsers] = useState(false);
  const [searchError, setSearchError] = useState<string | null>(null);

  // Состояние для групповых чатов
  const [step, setStep] = useState<1 | 2>(1);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Сброс состояния при закрытии
  useEffect(() => {
    if (!visible) {
      resetState();
    }
  }, [visible]);

  const resetState = () => {
    setSearchQuery("");
    setUsers([]);
    setSearchError(null);
    setStep(1);
    setName("");
    setDescription("");
    setIsSubmitting(false);
  };

  /**
   * Загружает список пользователей с фильтрацией
   */
  const loadUsers = useCallback(async (search = "") => {
    if (!user?.id) return;

    setLoadingUsers(true);
    setSearchError(null);

    try {
      let userList = await UserService.getAllUsers();

      // Фильтруем текущего пользователя
      userList = userList.filter(userItem => userItem.id !== user.id);

      // Применяем поисковый фильтр
      if (search.trim()) {
        const query = search.toLowerCase();
        userList = userList.filter(userItem =>
          userItem.username.toLowerCase().includes(query) ||
          userItem.email.toLowerCase().includes(query) ||
          (userItem.firstName && userItem.firstName.toLowerCase().includes(query)) ||
          (userItem.lastName && userItem.lastName.toLowerCase().includes(query))
        );
      }

      setUsers(userList);

      if (userList.length === 0 && search) {
        setSearchError(`Пользователь "${search}" не найден`);
      }
    } catch (err: any) {
      console.error("Ошибка загрузки пользователей:", err);
      setSearchError("Не удалось загрузить список пользователей");
      setUsers([]);
    } finally {
      setLoadingUsers(false);
    }
  }, [user?.id]);

  /**
   * Обработчик поиска с дебаунсом
   */
  const handleSearch = useCallback((query: string) => {
    setSearchQuery(query);

    if (query.trim()) {
      const timer = setTimeout(() => loadUsers(query), 500);
      return () => clearTimeout(timer);
    } else {
      loadUsers();
    }
  }, [loadUsers]);

  /**
   * Создает приватный чат с выбранным пользователем
   */
  const createPrivateChat = async (targetUserId: number) => {
    if (!user?.id) return;

    setIsSubmitting(true);
    try {
      await onSubmit({
        type: "PRIVATE",
        targetUserId,
      });
      onDismiss();
    } catch (error: any) {
      console.error("Ошибка создания приватного чата:", error);
      alert(error.message || "Не удалось создать чат");
    } finally {
      setIsSubmitting(false);
    }
  };

  /**
   * Создает групповой или публичный чат
   */
  const createGroupChat = async () => {
    setIsSubmitting(true);
    try {
      await onSubmit({
        name: name.trim(),
        type,
        description: description.trim(),
        maxParticipants: type === "GROUP" ? 50 : 1000,
      });
      onDismiss();
    } catch (error: any) {
      console.error("Ошибка создания группового чата:", error);
      alert(error.message || "Не удалось создать чат");
    } finally {
      setIsSubmitting(false);
    }
  };

  /**
   * Рендер элемента списка пользователей
   */
  const renderUserItem = ({ item: user }: { item: UserSearchResult }) => (
    <List.Item
      title={user.username}
      description={getUserDescription(user)}
      left={() => (
        <Avatar.Image
          size={48}
          source={{
            uri: user.avatarUrl || `https://i.pravatar.cc/150?u=${user.id}`
          }}
          style={styles.avatar}
        />
      )}
      right={() => (
        <Button
          mode="contained"
          loading={isSubmitting}
          onPress={() => createPrivateChat(user.id)}
          style={styles.actionButton}
        >
          Написать
        </Button>
      )}
      style={styles.listItem}
    />
  );

  // --- РЕНДЕР ДЛЯ ПРИВАТНЫХ ЧАТОВ ---
  if (type === "PRIVATE") {
    return (
      <Portal>
        <Modal
          visible={visible}
          onDismiss={onDismiss}
          contentContainerStyle={[
            styles.modalContainer,
            { backgroundColor: theme.colors.surface }
          ]}
        >
          <Text variant="headlineSmall" style={styles.title}>
            Новое сообщение
          </Text>
          <Text variant="bodyMedium" style={styles.subtitle}>
            Найдите пользователя для личной переписки
          </Text>

          <Searchbar
            placeholder="Имя, никнейм или email..."
            onChangeText={handleSearch}
            value={searchQuery}
            style={styles.searchInput}
            icon="account-search"
          />

          {loadingUsers ? (
            <View style={styles.center}>
              <ActivityIndicator size="large" />
              <Text style={{ marginTop: 16, color: theme.colors.onSurfaceVariant }}>
                Поиск пользователей...
              </Text>
            </View>
          ) : searchError ? (
            <View style={styles.center}>
              <Text style={{ color: theme.colors.error, textAlign: "center" }}>
                {searchError}
              </Text>
              <Button
                mode="outlined"
                onPress={() => loadUsers()}
                style={{ marginTop: 16 }}
              >
                Обновить
              </Button>
            </View>
          ) : (
            <FlatList
              data={users}
              renderItem={renderUserItem}
              keyExtractor={(item) => item.id.toString()}
              style={styles.userList}
              ListEmptyComponent={
                <View style={styles.center}>
                  <Text style={styles.emptyText}>
                    {searchQuery
                      ? "Пользователи не найдены"
                      : "Начните поиск пользователей"}
                  </Text>
                </View>
              }
              showsVerticalScrollIndicator={false}
            />
          )}

          <Button
            mode="outlined"
            onPress={onDismiss}
            style={styles.cancelButton}
          >
            Отмена
          </Button>
        </Modal>
      </Portal>
    );
  }

  // --- РЕНДЕР ДЛЯ ГРУППОВЫХ И ПУБЛИЧНЫХ ЧАТОВ ---
  const modalTitle = type === "GROUP" ? "Создать группу" : "Создать сервер";

  if (step === 1) {
    return (
      <Portal>
        <Modal
          visible={visible}
          onDismiss={onDismiss}
          contentContainerStyle={[
            styles.modalContainer,
            { backgroundColor: theme.colors.surface }
          ]}
        >
          <Text variant="headlineSmall" style={styles.title}>
            {modalTitle}
          </Text>

          <TextInput
            label="Название"
            value={name}
            onChangeText={setName}
            mode="outlined"
            style={styles.input}
            placeholder="Введите название чата"
            maxLength={50}
          />

          <TextInput
            label="Описание (необязательно)"
            value={description}
            onChangeText={setDescription}
            mode="outlined"
            multiline
            numberOfLines={3}
            style={styles.largeInput}
            placeholder="Опишите назначение чата"
            maxLength={200}
          />

          <View style={styles.buttonRow}>
            <Button onPress={onDismiss}>Отмена</Button>
            <Button
              mode="contained"
              onPress={() => setStep(2)}
              disabled={!name.trim()}
            >
              Далее
            </Button>
          </View>
        </Modal>
      </Portal>
    );
  }

  return (
    <Portal>
      <Modal
        visible={visible}
        onDismiss={onDismiss}
        contentContainerStyle={[
          styles.modalContainer,
          { backgroundColor: theme.colors.surface }
        ]}
      >
        <Text variant="headlineSmall" style={styles.title}>
          Пригласить участников
        </Text>

        <Text variant="bodyMedium" style={styles.subtitle}>
          Вы можете добавить участников позже через меню чата
        </Text>

        <View style={[styles.center, { marginVertical: 20 }]}>
          <Avatar.Icon
            size={64}
            icon="account-group"
            style={{ backgroundColor: theme.colors.primaryContainer }}
          />
          <Text style={styles.emptyText}>
            Приглашения участников
          </Text>
          <Text style={styles.emptySubtext}>
            После создания чата вы сможете пригласить участников
            через меню настроек
          </Text>
        </View>

        <View style={styles.buttonRow}>
          <Button onPress={() => setStep(1)}>Назад</Button>
          <Button
            mode="contained"
            loading={isSubmitting}
            onPress={createGroupChat}
            disabled={!name.trim()}
          >
            Создать
          </Button>
        </View>
      </Modal>
    </Portal>
  );
};

// --- ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ---

/**
 * Формирует описание пользователя для отображения
 */
const getUserDescription = (user: UserSearchResult): string => {
  const parts = [];
  if (user.firstName || user.lastName) {
    parts.push(`${user.firstName || ''} ${user.lastName || ''}`.trim());
  }
  if (user.email) {
    parts.push(user.email);
  }
  return parts.join(' • ') || "Пользователь";
};

// --- СТИЛИ ---

const styles = StyleSheet.create({
  modalContainer: {
    padding: 24,
    margin: 20,
    borderRadius: 16,
    alignSelf: "center",
    width: "90%",
    maxWidth: 500,
    maxHeight: "80%",
  },
  title: {
    marginBottom: 12,
    fontWeight: "600",
  },
  subtitle: {
    marginBottom: 20,
    opacity: 0.7,
  },
  searchInput: {
    marginBottom: 16,
  },
  input: {
    marginBottom: 16,
  },
  largeInput: {
    marginBottom: 24,
  },
  userList: {
    maxHeight: 300,
    marginBottom: 16,
  },
  listItem: {
    paddingVertical: 8,
  },
  avatar: {
    marginRight: 12,
  },
  actionButton: {
    alignSelf: "center",
  },
  emptyText: {
    textAlign: "center",
    marginTop: 16,
    fontSize: 16,
    fontWeight: "500",
    opacity: 0.8,
  },
  emptySubtext: {
    textAlign: "center",
    marginTop: 8,
    fontSize: 14,
    opacity: 0.6,
    paddingHorizontal: 20,
  },
  buttonRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginTop: 8,
  },
  cancelButton: {
    marginTop: 16,
    alignSelf: "center",
  },
  center: {
    alignItems: "center",
    justifyContent: "center",
    padding: 20,
  },
});