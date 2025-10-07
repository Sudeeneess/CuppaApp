import { Link, Stack } from "expo-router";
import { View, StyleSheet } from "react-native";

export default function Index() {
  return (
    <View style={styles.container}>
      <Stack.Screen />
      <Link
        href={{
          pathname: "./sign-in",
        }}
      >
        Go to Log-in
      </Link>
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
