import { MD3LightTheme, MD3DarkTheme } from "react-native-paper";

// --- ЕДИНЫЙ ЦВЕТОВОЙ СЛОВАРЬ (для удобства) ---
const COLORS = {
  // Базовые оттенки
  SAGE_PRIMARY: "#7A9D96", // Шалфей (Основной акцент)
  OLIVE_WARM: "#A2A882", // Тёплый Оливковый
  TAUPE_LIGHT: "#E0DAD4", // Светлый Тауп
  CHARCOAL: "#383A3C", // Тёмный Уголь
  OFF_WHITE: "#F1F0E8", // Очень светлый теплый

  // Оттенки для тем (Светлая)
  LIGHT_OUTER_BG: "#EAE7E3",
  LIGHT_INNER_BG: "#FAF7F2",
  LIGHT_OUTLINE: "#C3C1BC",

  // Оттенки для тем (Тёмная)
  DARK_OUTER_BG: "#1E2325",
  DARK_INNER_BG: "#282D30",
  DARK_OUTLINE: "#3E4245",
  DARK_USER_MSG: "#45524B",
  DARK_OTHER_MSG: "#363C40",
};

// --- СВЕТЛАЯ ТЕМА (CustomLightTheme) ---
export const CustomLightTheme = {
  ...MD3LightTheme,
  dark: false,
  roundness: 8,

  colors: {
    ...MD3LightTheme.colors,

    // 1. ПЕРЕОПРЕДЕЛЕНИЕ ГЛАВНЫХ ЦВЕТОВ
    primary: COLORS.SAGE_PRIMARY,
    onPrimary: "#FFFFFF", // Белый текст на кнопках

    background: COLORS.LIGHT_OUTER_BG, // Внешний фон
    onBackground: COLORS.CHARCOAL,

    surface: COLORS.LIGHT_INNER_BG, // Фон элементов (Область чата)
    onSurface: COLORS.CHARCOAL,

    // 2. УДАЛЕНИЕ СИНИХ/ФИОЛЕТОВЫХ ОТТЕНКОВ
    // secondary/tertiary (для второстепенных кнопок, иконок)
    secondary: COLORS.OLIVE_WARM,
    onSecondary: COLORS.CHARCOAL,
    secondaryContainer: COLORS.TAUPE_LIGHT,

    tertiary: COLORS.TAUPE_LIGHT, // Тройной акцент (редко используется)
    onTertiary: COLORS.CHARCOAL,

    // surfaceVariant (для полей ввода, разделителей, второстепенных карточек)
    surfaceVariant: COLORS.TAUPE_LIGHT,
    onSurfaceVariant: COLORS.CHARCOAL,

    // outline (Границы и разделители)
    outline: COLORS.LIGHT_OUTLINE,

    // error (Ошибки)
    error: "#D16666",

    // Остальные цвета (могут быть не явно переопределены, но должны быть тут)
    // elevation, scrim, inversePrimary и т.д. - их оставим по умолчанию MD3
  },

  // ПОЛЬЗОВАТЕЛЬСКИЕ ЦВЕТА
  customColors: {
    userMessage: COLORS.OLIVE_WARM,
    otherMessage: COLORS.TAUPE_LIGHT,
  },
};

// --- ТЁМНАЯ ТЕМА (CustomDarkTheme) ---
export const CustomDarkTheme = {
  ...MD3DarkTheme,
  dark: true,
  roundness: 8,

  colors: {
    ...MD3DarkTheme.colors,

    // 1. ПЕРЕОПРЕДЕЛЕНИЕ ГЛАВНЫХ ЦВЕТОВ
    primary: COLORS.SAGE_PRIMARY, // Используем более яркий акцент на темном фоне
    onPrimary: COLORS.DARK_OUTER_BG, // Тёмный текст на светлом акценте

    background: COLORS.DARK_OUTER_BG,
    onBackground: COLORS.OFF_WHITE,

    surface: COLORS.DARK_INNER_BG,
    onSurface: COLORS.OFF_WHITE,

    // 2. УДАЛЕНИЕ СИНИХ/ФИОЛЕТОВЫХ ОТТЕНКОВ
    // secondary/tertiary
    secondary: COLORS.SAGE_PRIMARY,
    onSecondary: COLORS.DARK_INNER_BG,
    secondaryContainer: COLORS.DARK_USER_MSG,

    tertiary: COLORS.DARK_OTHER_MSG,
    onTertiary: COLORS.OFF_WHITE,

    // surfaceVariant
    surfaceVariant: COLORS.DARK_OTHER_MSG,
    onSurfaceVariant: COLORS.OFF_WHITE,

    // outline
    outline: COLORS.DARK_OUTLINE,

    // error
    error: "#FFB4AB",
  },

  // ПОЛЬЗОВАТЕЛЬСКИЕ ЦВЕТА
  customColors: {
    userMessage: COLORS.DARK_USER_MSG,
    otherMessage: COLORS.DARK_OTHER_MSG,
  },
};
