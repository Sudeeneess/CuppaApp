/*import { Link, Stack } from "expo-router";
import { View, StyleSheet, Text } from "react-native";
import { Button } from "react-native-paper";
import { useAuth } from "@/hooks/use-auth";

export default function Index() {
  const { signOut } = useAuth();

  return (
    <View style={styles.container}>
      <Stack.Screen options={{ headerShown: false }} />
      <Link
        href={{
          pathname: "./chat",
        }}
      >
        Go to Chat
      </Link>
      <Button mode="contained" onPress={signOut}>
        <Text> Sign Out </Text>
      </Button>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
  },
});
*/

/*
import * as React from "react";
import { View, FlatList, StyleSheet, TouchableOpacity } from "react-native";
import { Card, Avatar, Text, useTheme } from "react-native-paper";
import { ChatCard } from "@/components/chat-card";
import { useRouter } from "expo-router";
import { useChat } from "@/hooks/use-chat";


interface ChatData {
  id: string;
  name: string;
  lastMessage: string;
  time: string;
  unreadCount: number;
  avatarUrl: string;
}


interface ChatCardProps {
  item: ChatData;
  onPress: (item: ChatData) => void;
}

// 2. Моковые данные (с явным указанием типа)
// ----------------------------------------------------
const CHAT_DATA: ChatData[] = [
  {
    id: "1",
    name: "Abvrgl",
    lastMessage: "Да, отличная идея!",
    time: "18:30",
    unreadCount: 3,
    avatarUrl: "https://i.pravatar.cc/150?img=1",
  },
  {
    id: "2",
    name: "Максим",
    lastMessage: "Завтра встречаемся в 10.",
    time: "17:55",
    unreadCount: 0,
    avatarUrl:
      "https://preview.redd.it/how-would-the-meowl-walk-v0-rhz6zltl9qpf1.jpg?width=736&format=pjpg&auto=webp&s=86001c54b7fa7a37c5160ebf1613702768f1c273",
  },
  {
    id: "3",
    name: "Команда",
    lastMessage: "Обновлен отчет.",
    time: "Вчера",
    unreadCount: 1,
    avatarUrl: "https://i.pravatar.cc/150?img=28",
  },
];

// 4. Главный компонент Списка Чатов
// ----------------------------------------------------
export default function ChatListScreen() {
  const theme = useTheme();
  const router = useRouter();

  const chat = useChat();

  const handlePress = (chat: ChatData) => {
    console.log("Открыть чат:", chat.name);
  };

  return (
    <FlatList<ChatData> // Типизация FlatList для данных
      data={CHAT_DATA}
      keyExtractor={(item) => item.id}
      renderItem={({ item }) => (
        <ChatCard
          item={item}
          onPress={() => {
            router.push(`./chat/${item.id}`);
            console.log(chat.chatList);
          }}
        />
      )}
      contentContainerStyle={[
        styles.listContainer,
        { backgroundColor: theme.colors.background },
      ]}
    />
  );
}

// 4. Стили (без цветов)
const styles = StyleSheet.create({
  listContainer: {
    padding: 10,
    // Убрали жесткий цвет, он теперь в contentContainerStyle
  },
  card: {
    marginVertical: 6,
    borderRadius: 16,
    overflow: "visible",
  },
  cardContent: {
    flexDirection: "row",
    alignItems: "center",
    padding: 12,
    paddingRight: 20,
  },
  avatar: {
    marginRight: 15,
  },
  badge: {
    position: "absolute",
    top: 5,
    left: 55,
    borderRadius: 12,
    minWidth: 24,
    height: 24,
    justifyContent: "center",
    alignItems: "center",
    zIndex: 10,
    paddingHorizontal: 4,
  },
  badgeText: {
    fontSize: 12,
    fontWeight: "bold",
    // Убрали 'white', он теперь theme.colors.onError
  },
  textContainer: {
    flex: 1,
    justifyContent: "center",
  },
  headerRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 2,
  },
  name: {
    flexShrink: 1,
    marginRight: 10,
  },
  time: {
    // Убрали жесткий цвет, он теперь в style прописан
  },
  lastMessage: {
    fontSize: 14,
    // Убрали жесткий цвет
  },
});

// ... (export default ChatListScreen)

*/

// index.tsx
import React, { useState } from "react";
import { View, StyleSheet, FlatList, RefreshControl } from "react-native";
import { useTheme, Button } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";

import { useChat } from "@/hooks/use-chat";
import { ChatCard } from "@/components/chat-card";
import { CreateChatModal } from "@/components/create-chat-modal";
import { ChatType } from "@/constants/types";

import { useAuth } from "@/hooks/use-auth";
import { useIsWide } from "@/hooks/use-is-wide"; // Твой хук

export default function ChatListScreen() {
  const { chats, isLoading, refreshChats, createChat } = useChat();
  const { signOut } = useAuth();
  const isWide = useIsWide();
  const theme = useTheme();

  // ЕДИНСТВЕННЫЙ стейт для управления модалкой
  // null = закрыто, ChatType = открыто с определенным типом
  const [activeCreationType, setActiveCreationType] = useState<ChatType | null>(
    null
  );

  const dataWithAddButton = [...chats, "ADD_BUTTON" as const];

  return (
    <SafeAreaView
      style={[styles.container, { backgroundColor: theme.colors.background }]}
    >
      <FlatList
        key={isWide ? "grid" : "list"}
        data={dataWithAddButton}
        keyExtractor={(item) =>
          typeof item === "string" ? "add-btn" : item.id.toString()
        }
        numColumns={isWide ? 3 : 1}
        contentContainerStyle={styles.listContent}
        refreshControl={
          <RefreshControl refreshing={isLoading} onRefresh={refreshChats} />
        }
        renderItem={({ item }) => (
          <View
            style={isWide ? styles.gridItemWrapper : styles.listItemWrapper}
          >
            <ChatCard
              chat={item}
              isWide={isWide}
              // Просто меняем стейт на тип чата, который вернула карточка
              onCreatePress={(type) => setActiveCreationType(type)}
            />
          </View>
        )}
      />

      {/* Компонент модального окна стал полностью самостоятельным */}
      <CreateChatModal
        visible={!!activeCreationType} // true, если type не null
        type={activeCreationType || "PRIVATE"} // Фоллбек тип, чтобы TS не ругался
        onDismiss={() => setActiveCreationType(null)}
        onSubmit={createChat} // Передаем функцию из контекста
      />
      <Button onPress={signOut}>signOut</Button>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  listContent: { padding: 8, paddingBottom: 80 },
  gridItemWrapper: { flex: 1 / 3, maxWidth: "33.33%" },
  listItemWrapper: { flex: 1 },
});