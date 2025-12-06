// components/chat-card.tsx
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
import { useAuth } from "@/hooks/use-auth";

interface ChatCardProps {
  chat: ChatRoom | "ADD_BUTTON";
  isWide: boolean;
  onCreatePress?: (type: ChatType) => void;
}

/**
 * Карточка чата для отображения в списке или сетке
 *
 * @component
 * @param {ChatRoom | 'ADD_BUTTON'} chat - Объект чата или специальное значение для кнопки создания
 * @param {boolean} isWide - Режим отображения (широкий для сетки, узкий для списка)
 * @param {function} onCreatePress - Callback для создания нового чата
 *
 * @description
 * Отображает информацию о чате: аватар, название, последнее сообщение, статус онлайн.
 * В режиме ADD_BUTTON показывает интерактивную кнопку для создания нового чата.
 * Адаптируется под разные размеры экрана (широкий/узкий режим).
 */
export const ChatCard: React.FC<ChatCardProps> = ({
  chat,
  isWide,
  onCreatePress,
}) => {
  const theme = useTheme();
  const router = useRouter();
  const { user } = useAuth();

  // Состояние для кнопки создания чата
  const [isAddExpanded, setIsAddExpanded] = useState(false);
  const [newChatType, setNewChatType] = useState<ChatType>("PRIVATE");

  // --- КНОПКА СОЗДАНИЯ НОВОГО ЧАТА ---
  if (chat === "ADD_BUTTON") {
    return isAddExpanded ? (
      <CreateChatForm
        isWide={isWide}
        chatType={newChatType}
        onChatTypeChange={setNewChatType}
        onSubmit={() => onCreatePress?.(newChatType)}
        onCancel={() => {
          setIsAddExpanded(false);
          setNewChatType("PRIVATE");
        }}
      />
    ) : (
      <AddChatButton
        isWide={isWide}
        theme={theme}
        onPress={() => setIsAddExpanded(true)}
      />
    );
  }

  // --- КАРТОЧКА СУЩЕСТВУЮЩЕГО ЧАТА ---
  return <ExistingChatCard chat={chat} isWide={isWide} theme={theme} user={user} />;
};

// --- КОМПОНЕНТЫ-СУБКОМПОНЕНТЫ ---

/**
 * Кнопка для добавления нового чата (свернутое состояние)
 */
interface AddChatButtonProps {
  isWide: boolean;
  theme: any;
  onPress: () => void;
}

const AddChatButton: React.FC<AddChatButtonProps> = ({ isWide, theme, onPress }) => {
  if (isWide) {
    return (
      <Card style={[styles.wideCard, styles.addCard]} onPress={onPress}>
        <View style={styles.centerContent}>
          <Text variant="displayMedium" style={{ color: theme.colors.primary }}>
            +
          </Text>
          <Text variant="bodyLarge">Новый чат</Text>
        </View>
      </Card>
    );
  }

  return (
    <TouchableOpacity onPress={onPress}>
      <Surface style={styles.narrowCard} elevation={1}>
        <View
          style={[
            styles.avatarContainer,
            styles.addAvatar,
            { backgroundColor: theme.colors.surfaceVariant },
          ]}
        >
          <Text variant="headlineMedium" style={{ color: theme.colors.primary }}>
            +
          </Text>
        </View>
        <View style={styles.narrowContent}>
          <Text variant="titleMedium">Создать новый чат</Text>
        </View>
      </Surface>
    </TouchableOpacity>
  );
};

/**
 * Форма для создания нового чата (развернутое состояние)
 */
interface CreateChatFormProps {
  isWide: boolean;
  chatType: ChatType;
  onChatTypeChange: (type: ChatType) => void;
  onSubmit: () => void;
  onCancel: () => void;
}

const CreateChatForm: React.FC<CreateChatFormProps> = ({
  isWide,
  chatType,
  onChatTypeChange,
  onSubmit,
  onCancel,
}) => {
  const theme = useTheme();
  const buttonLabels = {
    PRIVATE: "Написать",
    GROUP: "Создать",
    PUBLIC: "Создать",
  };

  if (isWide) {
    return (
      <Card style={[styles.wideCard, { justifyContent: "center" }]}>
        <Card.Content style={{ gap: 10 }}>
          <SegmentedButtons
            value={chatType}
            onValueChange={onChatTypeChange}
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
            icon={chatType === "PRIVATE" ? "send" : "plus"}
            onPress={onSubmit}
          >
            {buttonLabels[chatType]}
          </Button>
          <Button mode="outlined" onPress={onCancel}>
            Отменить
          </Button>
        </Card.Content>
      </Card>
    );
  }

  return (
    <Surface style={[styles.narrowCard, { paddingVertical: 8 }]} elevation={2}>
      <View style={styles.compactFormRow}>
        <View style={{ flex: 1 }}>
          <SegmentedButtons
            value={chatType}
            onValueChange={onChatTypeChange}
            density="small"
            buttons={[
              { value: "PRIVATE", label: "ЛС" },
              { value: "GROUP", label: "Гр" },
              { value: "PUBLIC", label: "Срв" },
            ]}
          />
        </View>
        <IconButton
          icon={chatType === "PRIVATE" ? "send" : "plus"}
          mode="contained"
          containerColor={theme.colors.primary}
          iconColor={theme.colors.onPrimary}
          size={20}
          onPress={onSubmit}
        />
        <IconButton icon="arrow-left" size={20} onPress={onCancel} />
      </View>
    </Surface>
  );
};

/**
 * Карточка существующего чата
 */
interface ExistingChatCardProps {
  chat: ChatRoom;
  isWide: boolean;
  theme: any;
  user: { id: number } | null;
}

const ExistingChatCard: React.FC<ExistingChatCardProps> = ({ chat, isWide, theme, user }) => {
  const router = useRouter();

  const participants = chat.participants || [];
  const isOnline = chat.isOnline ?? false;
  const hasUnread = (chat.unreadCount || 0) > 0;

  const displayName = getChatDisplayName(chat, participants, user?.id);
  const lastMessageTime = formatTime(chat.lastMessageAt);
  const backgroundColor = hasUnread ? theme.colors.primaryContainer : theme.colors.surface;
  const textColor = hasUnread ? theme.colors.onPrimaryContainer : theme.colors.onSurface;

  const handlePress = () => {
    router.push(`./chat/${chat.id}`);
  };

  return isWide ? (
    <WideChatCard
      chat={chat}
      displayName={displayName}
      isOnline={isOnline}
      hasUnread={hasUnread}
      backgroundColor={backgroundColor}
      textColor={textColor}
      lastMessageTime={lastMessageTime}
      onPress={handlePress}
    />
  ) : (
    <NarrowChatCard
      chat={chat}
      displayName={displayName}
      isOnline={isOnline}
      hasUnread={hasUnread}
      backgroundColor={backgroundColor}
      textColor={textColor}
      lastMessageTime={lastMessageTime}
      onPress={handlePress}
      theme={theme}
    />
  );
};

/**
 * Широкий вариант карточки чата (для сетки)
 */
interface WideChatCardProps {
  chat: ChatRoom;
  displayName: string;
  isOnline: boolean;
  hasUnread: boolean;
  backgroundColor: string;
  textColor: string;
  lastMessageTime: string;
  onPress: () => void;
}

const WideChatCard: React.FC<WideChatCardProps> = ({
  chat,
  displayName,
  isOnline,
  hasUnread,
  backgroundColor,
  textColor,
  lastMessageTime,
  onPress,
}) => {
  return (
    <Card
      style={[styles.wideCard, { backgroundColor }]}
      onPress={onPress}
      mode={hasUnread ? "contained" : "elevated"}
    >
      <View style={styles.wideCardInner}>
        <View style={{ alignSelf: "center", marginBottom: 12 }}>
          <ChatAvatar avatarUrl={chat.avatarUrl} isOnline={isOnline} />
        </View>

        <Text
          variant="titleMedium"
          style={{ textAlign: "center", color: textColor }}
          numberOfLines={1}
        >
          {displayName}
        </Text>

        <Text
          variant="bodySmall"
          style={{
            textAlign: "center",
            color: isOnline ? "#2196F3" : "#666",
            marginBottom: 8,
          }}
        >
          {isOnline ? "Онлайн" : "Не в сети"}
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
              {lastMessageTime}
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
};

/**
 * Узкий вариант карточки чата (для списка)
 */
interface NarrowChatCardProps {
  chat: ChatRoom;
  displayName: string;
  isOnline: boolean;
  hasUnread: boolean;
  backgroundColor: string;
  textColor: string;
  lastMessageTime: string;
  onPress: () => void;
  theme: any;
}

const NarrowChatCard: React.FC<NarrowChatCardProps> = ({
  chat,
  displayName,
  isOnline,
  hasUnread,
  backgroundColor,
  textColor,
  lastMessageTime,
  onPress,
  theme,
}) => {
  return (
    <TouchableOpacity onPress={onPress} activeOpacity={0.7}>
      <Surface
        style={[styles.narrowCard, { backgroundColor }]}
        elevation={0}
      >
        <View style={styles.avatarContainer}>
          <ChatAvatar avatarUrl={chat.avatarUrl} isOnline={isOnline} />
        </View>

        <View style={styles.narrowContent}>
          <Text
            variant="titleMedium"
            numberOfLines={1}
            style={{ color: textColor }}
          >
            {displayName}
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
            {lastMessageTime}
          </Text>
          <View style={{ marginTop: 4, alignItems: "flex-end" }}>
            {hasUnread ? (
              <Badge size={22}>{chat.unreadCount}</Badge>
            ) : (
              !isOnline && (
                <Text variant="labelSmall" style={{ color: "#666" }}>
                  Не в сети
                </Text>
              )
            )}
          </View>
        </View>
      </Surface>
      <View style={{ height: StyleSheet.hairlineWidth, backgroundColor: theme.colors.outlineVariant }} />
    </TouchableOpacity>
  );
};

/**
 * Аватар чата с индикатором онлайн-статуса
 */
interface ChatAvatarProps {
  avatarUrl?: string;
  isOnline: boolean;
}

const ChatAvatar: React.FC<ChatAvatarProps> = ({ avatarUrl, isOnline }) => {
  return (
    <View>
      <Image
        source={{
          uri: avatarUrl || "https://via.placeholder.com/150?text=Чат"
        }}
        style={[styles.avatar, { opacity: isOnline ? 1 : 0.6 }]}
        defaultSource={{ uri: "https://via.placeholder.com/150?text=Чат" }}
      />
      <View
        style={[
          styles.statusIndicator,
          { backgroundColor: isOnline ? "#2196F3" : "#666" },
        ]}
      />
    </View>
  );
};

// --- ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ---

/**
 * Определяет отображаемое название чата
 */
const getChatDisplayName = (
  chat: ChatRoom,
  participants: ChatRoom['participants'],
  currentUserId?: number
): string => {
  if (chat.name) return chat.name;

  if (chat.type === "PRIVATE" && participants.length > 0 && currentUserId) {
    const otherParticipant = participants.find(p => p.userId !== currentUserId);
    return otherParticipant?.userName || "Приватный чат";
  }

  if (chat.type === "GROUP" || chat.type === "PUBLIC") {
    return `Чат ${chat.id}`;
  }

  return `Чат ${chat.id}`;
};

/**
 * Форматирует время для отображения
 */
const formatTime = (dateString?: string): string => {
  if (!dateString) return "";

  try {
    return new Date(dateString).toLocaleTimeString([], {
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch {
    return "";
  }
};

// --- СТИЛИ ---

const styles = StyleSheet.create({
  // Общие стили
  avatar: {
    width: 56,
    height: 56,
    borderRadius: 12,
    backgroundColor: "#f0f0f0",
  },
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

  // Стили для узкого режима (список)
  narrowCard: {
    flexDirection: "row",
    padding: 12,
    alignItems: "center",
    minHeight: 72,
  },
  avatarContainer: {
    marginRight: 12,
  },
  narrowContent: {
    flex: 1,
    justifyContent: "center",
    minHeight: 48,
  },
  narrowRightMeta: {
    alignItems: "flex-end",
    marginLeft: 8,
    minWidth: 60,
  },

  // Стили для широкого режима (сетка)
  wideCard: {
    flex: 1,
    margin: 8,
    minHeight: 220,
    borderRadius: 16,
  },
  wideCardInner: {
    padding: 16,
    flex: 1,
    justifyContent: "space-between",
  },
  wideMessageRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginTop: 12,
    minHeight: 40,
  },

  // Стили для кнопки добавления
  addCard: {
    justifyContent: "center",
    alignItems: "center",
    borderStyle: "dashed",
    borderWidth: 2,
    borderColor: "#ddd",
    backgroundColor: "transparent",
  },
  addAvatar: {
    justifyContent: "center",
    alignItems: "center",
    width: 56,
    height: 56,
    borderRadius: 12,
  },
  centerContent: {
    alignItems: "center",
    justifyContent: "center",
    height: "100%",
    padding: 16,
  },

  // Стили для компактной формы
  compactFormRow: {
    flex: 1,
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
  },
});