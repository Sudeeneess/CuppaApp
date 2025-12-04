import * as theme from "../constants/app-theme";
import { useColorScheme } from "react-native";
import { MD3DarkTheme, MD3LightTheme } from "react-native-paper";

export function useAppTheme() {
  const colorScheme = useColorScheme();

  const appTheme =
    colorScheme === "dark"
      ? { ...MD3DarkTheme, colors: theme.dark }
      : { ...MD3LightTheme, colors: theme.light };

  return appTheme;
}
