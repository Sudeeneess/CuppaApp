// app/(app)/chat/[id].tsx - ПОЛНАЯ РЕАЛИЗАЦИЯ
import React, { useState, useEffect, useRef } from "react";
import {
  View,
  ScrollView,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
  FlatList,
  RefreshControl,
} from "react-native";
import {
  Text,
  Appbar,
  TextInput,
  Button,
  Avatar,
  Card,
  useTheme,
  ActivityIndicator,
  IconButton,
} from "react-native-paper";
import { useLocalSearchParams, useRouter } from "expo-router";
import { useAuth } from "@/hooks/use-auth";
import { ChatRoom, Message } from "@/constants/types";
import { MessageService } from "@/services/message-service";
import { ChatService } from "@/services/chat-service";

export default function ChatPage() {
  const { id } = useLocalSearchParams();
  const router = useRouter();
  const theme = useTheme();
  const { user } = useAuth();

  const [chat, setChat] = useState<ChatRoom | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [newMessage, setNewMessage] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isSending, setIsSending] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const scrollViewRef = useRef<ScrollView>(null);
  const flatListRef = useRef<FlatList>(null);
  const chatId = id ? parseInt(id as string) : null;

  /**
   * Загружает данные чата и сообщения
   */
  const loadChatData = async () => {
    if (!chatId || !user?.id) return;

    try {
      setIsLoading(true);
      setError(null);

      // 1. Загружаем информацию о чате
      const chatData = await ChatService.getChatById(chatId);
      setChat(chatData);

      // 2. Загружаем сообщения
      const messagesData = await MessageService.getChatMessages(chatId);
      setMessages(messagesData);

      // 3. Помечаем как прочитанные
      await MessageService.markAsRead(chatId);

    } catch (err: any) {
      console.error("Ошибка загрузки чата:", err);
      setError(err.message || "Не удалось загрузить чат");
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Обновление данных (pull-to-refresh)
   */
  const handleRefresh = async () => {
    setIsRefreshing(true);
    await loadChatData();
    setIsRefreshing(false);
  };

  useEffect(() => {
    loadChatData();
  }, [chatId, user?.id]);

  /**
   * Отправляет новое сообщение
   */
  const sendMessage = async () => {
    if (!newMessage.trim() || !user?.id || !chatId || isSending) return;

    try {
      setIsSending(true);

      const messageData = {
        content: newMessage.trim(),
        chatRoomId: chatId,
        senderId: user.id,
        messageType: "TEXT" as const,
      };

      const sentMessage = await MessageService.sendMessage(messageData);

      // Добавляем сообщение в список
      setMessages(prev => [...prev, sentMessage]);
      setNewMessage("");

      // Прокручиваем к последнему сообщению
      setTimeout(() => {
        flatListRef.current?.scrollToEnd({ animated: true });
      }, 100);

    } catch (err: any) {
      console.error("Ошибка отправки:", err);
      alert("Не удалось отправить сообщение: " + err.message);
    } finally {
      setIsSending(false);
    }
  };

  /**
   * Определяет название чата
   */
  const getChatName = (): string => {
    if (chat?.name) return chat.name;

    if (chat?.type === "PRIVATE" && chat.participants?.length > 0 && user?.id) {
      const otherParticipant = chat.participants.find(p => p.userId !== user.id);
      return otherParticipant?.userName || "Приватный чат";
    }

    return `Чат ${chat?.id || "неизвестный"}`;
  };

  /**
   * Форматирует время сообщения
   */
  const formatMessageTime = (dateString: string): string => {
    try {
      const date = new Date(dateString);
      const now = new Date();
      const diff = now.getTime() - date.getTime();

      // Если сегодня - показываем время
      if (diff < 24 * 60 * 60 * 1000) {
        return date.toLocaleTimeString([], {
          hour: "2-digit",
          minute: "2-digit"
        });
      }
      // Если вчера - показываем "вчера"
      else if (diff < 48 * 60 * 60 * 1000) {
        return "вчера";
      }
      // Иначе - дату
      else {
        return date.toLocaleDateString([], {
          day: "numeric",
          month: "short"
        });
      }
    } catch {
      return "";
    }
  };

  /**
   * Рендер одного сообщения
   */
  const renderMessage = ({ item }: { item: Message }) => {
    const isMyMessage = item.senderId === user?.id;
    const showAvatar = !isMyMessage &&
      (messages.findIndex(m => m.senderId === item.senderId) ===
       messages.indexOf(item));

    return (
      <View
        style={[
          styles.messageWrapper,
          isMyMessage ? styles.myMessageWrapper : styles.otherMessageWrapper,
        ]}
      >
        {showAvatar && !isMyMessage && (
          <Avatar.Text
            size={36}
            label={item.senderName?.charAt(0).toUpperCase() || "U"}
            style={styles.messageAvatar}
          />
        )}

        {!showAvatar && !isMyMessage && (
          <View style={styles.avatarSpacer} />
        )}

        <Card
          style={[
            styles.messageCard,
            isMyMessage ? styles.myMessageCard : styles.otherMessageCard,
          ]}
        >
          <Card.Content style={styles.messageContent}>
            {!isMyMessage && item.senderName && (
              <Text
                variant="labelSmall"
                style={styles.senderName}
                numberOfLines={1}
              >
                {item.senderName}
              </Text>
            )}
            <Text style={styles.messageText}>{item.content}</Text>
            <Text
              variant="labelSmall"
              style={styles.messageTime}
            >
              {formatMessageTime(item.sentAt)}
              {item.isEdited && " (ред.)"}
            </Text>
          </Card.Content>
        </Card>
      </View>
    );
  };

  if (isLoading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" />
        <Text style={{ marginTop: 12 }}>Загрузка чата...</Text>
      </View>
    );
  }

  if (error) {
    return (
      <View style={styles.center}>
        <Text variant="titleMedium">Ошибка</Text>
        <Text style={{ marginTop: 8, textAlign: "center" }}>{error}</Text>
        <Button
          mode="contained"
          onPress={loadChatData}
          style={{ marginTop: 16 }}
        >
          Повторить
        </Button>
        <Button
          mode="outlined"
          onPress={() => router.back()}
          style={{ marginTop: 8 }}
        >
          Назад
        </Button>
      </View>
    );
  }

  const chatName = getChatName();
  const participantCount = chat?.participants?.length || 0;

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === "ios" ? "padding" : "height"}
      keyboardVerticalOffset={Platform.OS === "ios" ? 90 : 0}
    >
      {/* Шапка чата */}
      <Appbar.Header>
        <Appbar.BackAction onPress={() => router.back()} />
        <Appbar.Content
          title={chatName}
          subtitle={
            chat?.type === "PRIVATE"
              ? "Приватный чат"
              : `${participantCount} участников`
          }
        />
      </Appbar.Header>

      {/* Список сообщений */}
      {messages.length === 0 ? (
        <View style={styles.center}>
          <Text style={styles.emptyText}>Пока нет сообщений</Text>
          <Text style={styles.emptySubtext}>Начните общение первым!</Text>
        </View>
      ) : (
        <FlatList
          ref={flatListRef}
          data={messages}
          renderItem={renderMessage}
          keyExtractor={(item) => item.id?.toString() || Math.random().toString()}
          contentContainerStyle={styles.messagesContent}
          showsVerticalScrollIndicator={false}
          refreshControl={
            <RefreshControl
              refreshing={isRefreshing}
              onRefresh={handleRefresh}
              colors={[theme.colors.primary]}
            />
          }
          inverted={false}
        />
      )}

      {/* Поле ввода сообщения */}
      <View style={styles.inputContainer}>
        <View style={styles.inputRow}>
          <TextInput
            mode="outlined"
            placeholder="Напишите сообщение..."
            value={newMessage}
            onChangeText={setNewMessage}
            style={styles.textInput}
            multiline
            maxLength={1000}
            numberOfLines={1}
            outlineStyle={{ borderRadius: 24 }}
            disabled={isSending}
          />
          <IconButton
            icon="send"
            mode="contained"
            size={24}
            onPress={sendMessage}
            disabled={!newMessage.trim() || isSending}
            loading={isSending}
            style={styles.sendButton}
          />
        </View>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#f5f5f5",
  },
  center: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    padding: 20,
  },
  messagesContent: {
    padding: 16,
    paddingBottom: 8,
  },
  messageWrapper: {
    flexDirection: "row",
    marginVertical: 4,
    alignItems: "flex-start",
  },
  myMessageWrapper: {
    justifyContent: "flex-end",
  },
  otherMessageWrapper: {
    justifyContent: "flex-start",
  },
  messageAvatar: {
    marginRight: 8,
    marginTop: 4,
  },
  avatarSpacer: {
    width: 44, // avatar width + margin
    marginRight: 8,
  },
  messageCard: {
    maxWidth: "75%",
    borderRadius: 18,
  },
  myMessageCard: {
    backgroundColor: "#e3f2fd",
    borderTopRightRadius: 4,
  },
  otherMessageCard: {
    backgroundColor: "white",
    borderTopLeftRadius: 4,
  },
  messageContent: {
    padding: 12,
    paddingBottom: 8,
  },
  senderName: {
    fontWeight: "600",
    marginBottom: 2,
    color: "#555",
  },
  messageText: {
    fontSize: 16,
    lineHeight: 20,
  },
  messageTime: {
    marginTop: 4,
    opacity: 0.6,
    alignSelf: "flex-end",
    fontSize: 11,
  },
  inputContainer: {
    padding: 12,
    backgroundColor: "white",
    borderTopWidth: StyleSheet.hairlineWidth,
    borderTopColor: "#e0e0e0",
  },
  inputRow: {
    flexDirection: "row",
    alignItems: "center",
  },
  textInput: {
    flex: 1,
    backgroundColor: "white",
    fontSize: 16,
    marginRight: 8,
  },
  sendButton: {
    margin: 0,
  },
  emptyText: {
    fontSize: 16,
    color: "#666",
    textAlign: "center",
  },
  emptySubtext: {
    fontSize: 14,
    color: "#999",
    textAlign: "center",
    marginTop: 4,
  },
});