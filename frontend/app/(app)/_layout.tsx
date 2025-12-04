import { Stack, Redirect } from "expo-router";
import { useAuth } from "@/hooks/use-auth";
import { ChatProvider } from "@/contexts/chat-context";

export default function AppLayout() {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Redirect href={"/(auth)"} />;
  }

  return (
    <ChatProvider>
      <Stack screenOptions={{}} />
    </ChatProvider>
  );
}
