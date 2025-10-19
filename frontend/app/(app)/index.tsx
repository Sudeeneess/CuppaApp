import { Link, Stack } from "expo-router";
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
