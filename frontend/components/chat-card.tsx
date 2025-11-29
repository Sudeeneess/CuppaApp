/*import * as React from "react";
import {
  View,
  FlatList,
  StyleSheet,
  TouchableOpacity,
  Image,
} from "react-native";
import { Card, Avatar, Text, useTheme, Badge } from "react-native-paper";

export interface ChatData {
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

const SquareAvatar = ({ avatarUrl }: ChatData) => (
  <Image
    source={{ uri: avatarUrl }}
    style={{ borderRadius: 12, width: 72, height: 72 }}
  />
);

export function ChatCard({ item, onPress }: ChatCardProps) {
  const theme = useTheme();
  const isUnread = item.unreadCount > 0;

  return (
    <TouchableOpacity
      onPress={() => onPress(item)}
      activeOpacity={0.7}
      style={{ marginVertical: 8 }}
    >
      <Card mode="elevated" style={{ alignContent: "flex-start" }}>
        <Card.Title
          title={item.name}
          subtitle={"fdfs"}
          left={() => SquareAvatar(item)}
          right={() => <Badge> 67 </Badge>}
          style={{ paddingLeft: 0 }}
          titleStyle={{ paddingLeft: 32 }}
          subtitleStyle={{ paddingLeft: 32 }}
        />
      </Card>
    </TouchableOpacity>
  );
}

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
*/

// chat-card.tsx
import React, { useState } from "react";
import { View, StyleSheet, Image, TouchableOpacity } from "react-native";
import {
  Text,
  Surface,
  Badge,
  useTheme,
  SegmentedButtons,
  Button,
  IconButton,
  Card,
} from "react-native-paper";
import { useRouter } from "expo-router";
import { ChatRoom, ChatType } from "@/constants/types";

interface ChatCardProps {
  chat: ChatRoom | "ADD_BUTTON";
  isWide: boolean;
  // Колбэк для вызова модалки создания (передает выбранный тип)
  onCreatePress?: (type: ChatType) => void;
}

export const ChatCard: React.FC<ChatCardProps> = ({
  chat,
  isWide,
  onCreatePress,
}) => {
  const theme = useTheme();
  const router = useRouter();

  // Состояние для карточки создания (расширена или нет)
  const [isAddExpanded, setIsAddExpanded] = useState(false);
  const [newChatType, setNewChatType] = useState<string>("PRIVATE");

  // --- ЛОГИКА КАРТОЧКИ СОЗДАНИЯ (ADD_BUTTON) ---
  if (chat === "ADD_BUTTON") {
    // Сброс состояния при сворачивании
    const handleCancel = () => {
      setIsAddExpanded(false);
      setNewChatType("PRIVATE");
    };

    const handleSubmit = () => {
      if (onCreatePress) {
        onCreatePress(newChatType as ChatType);
      }
      setIsAddExpanded(false);
    };

    // 1.1 Широкий режим + Расширенная форма
    if (isWide && isAddExpanded) {
      return (
        <Card style={[styles.wideCard, { justifyContent: "center" }]}>
          <Card.Content style={{ gap: 10 }}>
            <SegmentedButtons
              value={newChatType}
              onValueChange={setNewChatType}
              density="small"
              buttons={[
                { value: "PRIVATE", label: "ЛС" },
                { value: "GROUP", label: "Группа" },
                { value: "PUBLIC", label: "Сервер" },
              ]}
              style={{ marginBottom: 8 }}
            />
            <Button
              mode="contained"
              icon={newChatType === "PRIVATE" ? "send" : "plus"}
              onPress={handleSubmit}
            >
              {newChatType === "PRIVATE" ? "Написать" : "Создать"}
            </Button>
            <Button mode="outlined" onPress={handleCancel}>
              Отменить
            </Button>
          </Card.Content>
        </Card>
      );
    }

    // 1.2 Узкий режим + Расширенная форма
    if (!isWide && isAddExpanded) {
      return (
        <Surface
          style={[styles.narrowCard, { paddingVertical: 8 }]}
          elevation={2}
        >
          <View
            style={{
              flex: 1,
              flexDirection: "row",
              alignItems: "center",
              gap: 8,
            }}
          >
            <View style={{ flex: 1 }}>
              <SegmentedButtons
                value={newChatType}
                onValueChange={setNewChatType}
                density="small"
                buttons={[
                  { value: "PRIVATE", label: "ЛС" },
                  { value: "GROUP", label: "Гр" },
                  { value: "PUBLIC", label: "Срв" },
                ]}
              />
            </View>
            <IconButton
              icon={newChatType === "PRIVATE" ? "send" : "plus"}
              mode="contained"
              containerColor={theme.colors.primary}
              iconColor={theme.colors.onPrimary}
              size={20}
              onPress={handleSubmit}
            />
            <IconButton icon="arrow-left" size={20} onPress={handleCancel} />
          </View>
        </Surface>
      );
    }

    // 1.3 Свернутое состояние (Одинаковое логически, разное визуально)
    const handleExpand = () => setIsAddExpanded(true);

    if (isWide) {
      return (
        <Card style={[styles.wideCard, styles.addCard]} onPress={handleExpand}>
          <View style={styles.centerContent}>
            <Text
              variant="displayMedium"
              style={{ color: theme.colors.primary }}
            >
              +
            </Text>
            <Text variant="bodyLarge">Новый чат</Text>
          </View>
        </Card>
      );
    }

    return (
      <TouchableOpacity onPress={handleExpand}>
        <Surface style={styles.narrowCard} elevation={1}>
          <View
            style={[
              styles.avatarContainer,
              styles.addAvatar,
              { backgroundColor: theme.colors.surfaceVariant },
            ]}
          >
            <Text
              variant="headlineMedium"
              style={{ color: theme.colors.primary }}
            >
              +
            </Text>
          </View>
          <View style={styles.narrowContent}>
            <Text variant="titleMedium">Создать новый чат</Text>
          </View>
        </Surface>
      </TouchableOpacity>
    );
  }

  // --- ЛОГИКА ОБЫЧНОГО ЧАТА ---

  const isOnline = chat.isOnline ?? false;
  const hasUnread = (chat.unreadCount || 0) > 0;

  // Стилизация оффлайна и непрочитанных
  const cardOpacity = isOnline ? 1 : 0.6;
  // Если есть непрочитанные, фон чуть подкрашен (primaryContainer с прозрачностью или просто primaryContainer)
  // Для Paper лучше использовать theme colors.
  const backgroundColor = hasUnread
    ? theme.colors.primaryContainer // Можно сделать светлее, если слишком ярко: theme.colors.elevation.level2
    : theme.colors.surface;

  const textColor = hasUnread
    ? theme.colors.onPrimaryContainer
    : theme.colors.onSurface;

  const handlePress = () => {
    router.push(`./chat/${chat.id}`);
  };

  const AvatarComponent = () => (
    <View>
      <Image
        source={{ uri: chat.avatarUrl || "https://via.placeholder.com/150" }}
        style={[styles.avatar, { opacity: isOnline ? 1 : 0.5 }]} // Desaturate effect (fake) via opacity
      />
      {/* Если оффлайн, добавляем серый оверлей для эффекта "тусклости/desaturation" */}
      {!isOnline && (
        <View
          style={[
            styles.avatar,
            { position: "absolute", backgroundColor: "gray", opacity: 0.3 },
          ]}
        />
      )}

      {/* Индикатор */}
      <View
        style={[
          styles.statusIndicator,
          { backgroundColor: isOnline ? "#2196F3" : "gray" },
        ]}
      >
        {!isOnline &&
          !isWide &&
          // Для узкого режима в оффлайне можно вывести текст внутри кругляшка, если очень надо,
          // но места там мало (14px). Оставим просто серым кругляшком по ТЗ ("серый кругляшок с надписью" - текст не влезет в кругляшок 14px, текст будет рядом)
          null}
      </View>
    </View>
  );

  const formatTime = (date?: string) => {
    if (!date) return "";
    return new Date(date).toLocaleTimeString([], {
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  // 2.1 Широкий режим
  if (isWide) {
    return (
      <Card
        style={[styles.wideCard, { opacity: cardOpacity, backgroundColor }]}
        onPress={handlePress}
        mode={hasUnread ? "contained" : "elevated"}
      >
        <View style={styles.wideCardInner}>
          <View style={{ alignSelf: "center", marginBottom: 12 }}>
            <AvatarComponent />
          </View>

          <Text
            variant="titleMedium"
            style={{ textAlign: "center", color: textColor }}
            numberOfLines={1}
          >
            {chat.name}
          </Text>

          <Text
            variant="bodySmall"
            style={{
              textAlign: "center",
              color: isOnline ? "#2196F3" : theme.colors.onSurfaceDisabled,
              marginBottom: 8,
            }}
          >
            {isOnline ? "Онлайн" : chat.lastSeen || "Не в сети"}
          </Text>

          <View style={styles.wideMessageRow}>
            <Text
              variant="bodyMedium"
              numberOfLines={2}
              style={{ flex: 1, color: textColor, opacity: 0.8 }}
            >
              {chat.lastMessageText || "Нет сообщений"}
            </Text>
            <View style={{ alignItems: "flex-end", marginLeft: 8 }}>
              <Text variant="labelSmall" style={{ color: textColor }}>
                {formatTime(chat.lastMessageAt)}
              </Text>
              {hasUnread && (
                <Badge size={22} style={{ marginTop: 4 }}>
                  {chat.unreadCount}
                </Badge>
              )}
            </View>
          </View>
        </View>
      </Card>
    );
  }

  // 2.2 Узкий режим
  return (
    <TouchableOpacity onPress={handlePress} activeOpacity={0.7}>
      <Surface
        style={[styles.narrowCard, { opacity: cardOpacity, backgroundColor }]}
        elevation={0}
      >
        <View style={styles.avatarContainer}>
          <AvatarComponent />
        </View>

        <View style={styles.narrowContent}>
          <Text
            variant="titleMedium"
            numberOfLines={1}
            style={{ color: textColor }}
          >
            {chat.name}
          </Text>
          <Text
            variant="bodyMedium"
            numberOfLines={1}
            style={{ color: textColor, opacity: 0.7 }}
          >
            {chat.lastMessageText || "Нет сообщений"}
          </Text>
        </View>

        <View style={styles.narrowRightMeta}>
          <Text variant="labelSmall" style={{ color: textColor }}>
            {formatTime(chat.lastMessageAt)}
          </Text>
          <View style={{ marginTop: 4, alignItems: "flex-end" }}>
            {hasUnread ? (
              <Badge size={22}>{chat.unreadCount}</Badge>
            ) : (
              // Серый текст "5 ч." под временем, если оффлайн и нет непрочитанных
              !isOnline && (
                <Text variant="labelSmall" style={{ color: "gray" }}>
                  {chat.lastSeen}
                </Text>
              )
            )}
          </View>
        </View>
      </Surface>
      <View
        style={{ height: 1, backgroundColor: theme.colors.outlineVariant }}
      />
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  avatar: {
    width: 56,
    height: 56,
    borderRadius: 12,
    backgroundColor: "#e0e0e0",
  },
  addAvatar: { justifyContent: "center", alignItems: "center" },
  statusIndicator: {
    position: "absolute",
    bottom: -2,
    right: -2,
    width: 14,
    height: 14,
    borderRadius: 7,
    borderWidth: 2,
    borderColor: "white",
  },
  narrowCard: { flexDirection: "row", padding: 12, alignItems: "center" },
  avatarContainer: { marginRight: 12 },
  narrowContent: { flex: 1, justifyContent: "center" },
  narrowRightMeta: { alignItems: "flex-end", marginLeft: 8, minWidth: 50 },
  wideCard: { flex: 1, margin: 8, minHeight: 220 },
  addCard: {
    justifyContent: "center",
    alignItems: "center",
    borderStyle: "dashed",
    borderWidth: 2,
    borderColor: "#ccc",
    backgroundColor: "transparent",
  },
  centerContent: {
    alignItems: "center",
    justifyContent: "center",
    height: "100%",
  },
  wideCardInner: { padding: 16 },
  wideMessageRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginTop: 12,
  },
});
