import { Stack, Redirect } from "expo-router";
import { useAuth } from "@/hooks/use-auth";
import { ChatProvider } from "@/contexts/chat-context";

import { ParamListBase, StackNavigationState } from "@react-navigation/native";
import {
  createStackNavigator,
  StackNavigationEventMap,
  StackNavigationOptions,
} from "@react-navigation/stack";
import { withLayoutContext } from "expo-router";

const { Navigator } = createStackNavigator();

export const JsStack = withLayoutContext<
  StackNavigationOptions,
  typeof Navigator,
  StackNavigationState<ParamListBase>,
  StackNavigationEventMap
>(Navigator);

export default function AppLayout() {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Redirect href={"/(auth)"} />;
  }

  return (
    <ChatProvider>
      <JsStack
        screenOptions={{
          presentation: "card",
          animation: "reveal_from_bottom",
          gestureEnabled: true,
          headerTitleStyle: { userSelect: "none" },
        }}
      />
    </ChatProvider>
  );
}
