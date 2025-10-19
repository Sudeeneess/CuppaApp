import { Stack, Redirect } from "expo-router";
import { useAuth } from "@/hooks/use-auth";

export default function AppLayout() {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Redirect href={"/(auth)"} />;
  }

  return <Stack screenOptions={{}} />;
}
