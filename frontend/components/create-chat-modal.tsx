import React, { useState, useEffect } from "react";
import { View, ScrollView, StyleSheet } from "react-native";
import {
  Modal,
  Portal,
  Text,
  TextInput,
  Button,
  Checkbox,
  List,
  Avatar,
  Searchbar,
  Divider,
} from "react-native-paper";
import { ChatType, CreateChatRequest } from "@/constants/types";

// MOCK DATA (В будущем заменить на API запрос друзей)
const MOCK_USERS = [
  { id: 1, name: "Alice Smith", avatar: "https://i.pravatar.cc/150?u=1" },
  { id: 2, name: "Bob Johnson", avatar: "https://i.pravatar.cc/150?u=2" },
  { id: 3, name: "Charlie Brown", avatar: "https://i.pravatar.cc/150?u=3" },
  { id: 4, name: "Diana Prince", avatar: "https://i.pravatar.cc/150?u=4" },
  { id: 5, name: "Evan Wright", avatar: "https://i.pravatar.cc/150?u=5" },
];

interface CreateChatModalProps {
  visible: boolean;
  type: ChatType; // Тип создаваемого чата
  onDismiss: () => void;
  onSubmit: (data: CreateChatRequest) => Promise<void>;
}

export const CreateChatModal: React.FC<CreateChatModalProps> = ({
  visible,
  type,
  onDismiss,
  onSubmit,
}) => {
  // Внутренние стейты формы (изолированы от остального приложения)
  const [step, setStep] = useState<1 | 2>(1);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedUsers, setSelectedUsers] = useState<number[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Сброс формы при открытии
  useEffect(() => {
    if (visible) {
      setStep(1);
      setName("");
      setDescription("");
      setSearchQuery("");
      setSelectedUsers([]);
      setIsSubmitting(false);
    }
  }, [visible]);

  const handleFinish = async (nameOverride?: string) => {
    const finalName = nameOverride || name;

    if (!finalName && type !== "PRIVATE") {
      alert("Пожалуйста, введите название");
      return;
    }

    setIsSubmitting(true);
    try {
      const data: CreateChatRequest = {
        name: finalName,
        type,
        description,
        // Логика макс. участников чисто для примера
        maxParticipants: type === "GROUP" ? 50 : 1000,
        // Здесь можно добавить avatarUrl, если будет загрузка
      };
      console.log(data);
      await onSubmit(data);
      onDismiss();
    } catch (e) {
      // Ошибку обработает родитель или сервис, здесь просто снимаем лоадер
      console.error(e);
    } finally {
      setIsSubmitting(false);
    }
  };

  // --- РЕНДЕР КОНТЕНТА ---

  const renderContent = () => {
    // Сценарий 1: Личные сообщения
    if (type === "PRIVATE") {
      const filteredUsers = MOCK_USERS.filter((u) =>
        u.name.toLowerCase().includes(searchQuery.toLowerCase())
      );

      return (
        <>
          <Text variant="headlineSmall" style={styles.title}>
            Новое сообщение
          </Text>
          <Searchbar
            placeholder="Поиск..."
            onChangeText={setSearchQuery}
            value={searchQuery}
            style={styles.input}
          />
          <ScrollView style={styles.userList}>
            {filteredUsers.map((user) => (
              <List.Item
                key={user.id}
                title={user.name}
                left={() => (
                  <Avatar.Image size={40} source={{ uri: user.avatar }} />
                )}
                right={() => (
                  <Button
                    mode="text"
                    loading={isSubmitting}
                    onPress={() => {
                      // 🔥 ВАЖНО:
                      // 1. Для ЛС имя чата часто не нужно (оно формируется из имен участников),
                      //    но если API требует, передадим имя юзера.
                      // 2. ГЛАВНОЕ: Передаем targetUserId

                      setIsSubmitting(true);
                      onSubmit({
                        type: "PRIVATE",
                        targetUserId: user.id, // ID из MOCK_USERS (1, 2, 3...)
                        name: user.name, // На всякий случай, если UI использует для оптимистичного обновления
                      })
                        .then(() => {
                          onDismiss();
                        })
                        .catch((e) => console.error(e))
                        .finally(() => setIsSubmitting(false));
                    }}
                  >
                    Написать
                  </Button>
                )}
              />
            ))}
            {/* ... */}
          </ScrollView>
          <Button style={styles.cancelButton} onPress={onDismiss}>
            Отмена
          </Button>
        </>
      );
    }
    // Сценарий 2: Группа/Сервер - Шаг 1 (Настройки)
    if (step === 1) {
      return (
        <>
          <Text variant="headlineSmall" style={styles.title}>
            {type === "GROUP" ? "Создать группу" : "Создать сервер"}
          </Text>
          <TextInput
            label="Название"
            value={name}
            onChangeText={setName}
            mode="outlined"
            style={styles.input}
          />
          <TextInput
            label="Описание (опционально)"
            value={description}
            onChangeText={setDescription}
            mode="outlined"
            multiline
            numberOfLines={3}
            style={styles.largeInput}
          />
          <View style={styles.buttonRow}>
            <Button onPress={onDismiss}>Отмена</Button>
            <Button mode="contained" onPress={() => setStep(2)}>
              Далее
            </Button>
          </View>
        </>
      );
    }

    // Сценарий 2: Группа/Сервер - Шаг 2 (Приглашения)
    if (step === 2) {
      const toggleUser = (id: number) => {
        if (selectedUsers.includes(id))
          setSelectedUsers((prev) => prev.filter((i) => i !== id));
        else setSelectedUsers((prev) => [...prev, id]);
      };

      return (
        <>
          <Text variant="headlineSmall" style={styles.title}>
            Пригласить участников
          </Text>
          <Text variant="bodySmall" style={styles.subtitle}>
            Выбрано: {selectedUsers.length}
          </Text>

          <ScrollView style={styles.userList}>
            {MOCK_USERS.map((user) => (
              <View key={user.id}>
                <List.Item
                  title={user.name}
                  left={() => (
                    <Avatar.Image size={40} source={{ uri: user.avatar }} />
                  )}
                  right={() => (
                    <Checkbox
                      status={
                        selectedUsers.includes(user.id)
                          ? "checked"
                          : "unchecked"
                      }
                      onPress={() => toggleUser(user.id)}
                    />
                  )}
                  onPress={() => toggleUser(user.id)}
                />
                <Divider />
              </View>
            ))}
          </ScrollView>

          <View style={styles.buttonRow}>
            <Button onPress={() => setStep(1)}>Назад</Button>
            <Button
              mode="contained"
              loading={isSubmitting}
              onPress={() => handleFinish()}
            >
              Создать
            </Button>
          </View>
        </>
      );
    }
  };

  return (
    <Portal>
      <Modal
        visible={visible}
        onDismiss={onDismiss}
        contentContainerStyle={styles.modalContainer}
      >
        {renderContent()}
      </Modal>
    </Portal>
  );
};

const styles = StyleSheet.create({
  modalContainer: {
    backgroundColor: "white",
    padding: 20,
    margin: 20,
    borderRadius: 8,
    alignSelf: "center",
    width: "90%",
    maxWidth: 500,
  },
  title: { marginBottom: 16 },
  subtitle: { marginBottom: 16, color: "gray" },
  input: { marginBottom: 12 },
  largeInput: { marginBottom: 20 },
  userList: { maxHeight: 300 },
  emptyText: { textAlign: "center", marginTop: 20, color: "gray" },
  buttonRow: {
    flexDirection: "row",
    justifyContent: "flex-end",
    gap: 10,
    marginTop: 20,
  },
  cancelButton: { marginTop: 16 },
});
