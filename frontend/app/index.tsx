import React from "react";
import { Link, Stack } from "expo-router";
import { StyleSheet } from "react-native";
import { Surface, Text } from "react-native-paper";

export default function Index() {
  return (
    <Surface style={styles.background}>
      <Stack.Screen options={{ headerShown: false }} />
      <Link
        href={{
          pathname: "./auth",
        }}
      >
        <Text> Go to Auth (if not authenticated) </Text>
      </Link>
      <Link
        href={{
          pathname: "./home",
        }}
      >
        <Text>Go to Home (for authenticated users) </Text>
      </Link>
    </Surface>
  );
}

const styles = StyleSheet.create({
  background: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
  },
});
