import { useWindowDimensions } from "react-native";

export function useIsWide(): boolean {
  const { width, height } = useWindowDimensions();
  const isWide = width / height > 0.8;

  return isWide;
}
