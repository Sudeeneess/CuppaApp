import { Link, Stack } from "expo-router";
import { View, Text, StyleSheet } from "react-native";

export default function SignIn() {
  return (
    <View style={styles.container}>
      <Stack.Screen />
      <Link
        href={{
          pathname: "./home",
        }}
      >
        Sign-in
      </Link>
      <Text> </Text>
      <Link
        href={{
          pathname: "./sign-up",
        }}
      >
        Go to Sign-up
      </Link>
      <Link
        href={{
          pathname: "./password-recovery",
        }}
      >
        Go to Password recovery
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
