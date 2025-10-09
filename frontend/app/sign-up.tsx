import { Link, Stack } from "expo-router";
import { View, StyleSheet } from "react-native";

export default function SignUp() {
  return (
    <View style={styles.container}>
      <Stack.Screen />
      <Link
        href={{
          pathname: "./sign-in",
        }}
      >
        Sign-up
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
