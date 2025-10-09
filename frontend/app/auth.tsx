import React from "react";
import { Stack } from "expo-router";
import { StyleSheet, Text } from "react-native";
import { Surface, SegmentedButtons } from "react-native-paper";
import { SignInForm } from "../components/sign-in-form";
import { SignUpForm } from "../components/sign-up-form";
import { useIsWide } from "../hooks/use-is-wide";
import { SafeAreaView } from "react-native-safe-area-context";

export default function Auth() {
  const isWide = useIsWide();

  type AuthMode = "signIn" | "signUp";
  const [authMode, setAuthMode] = React.useState<AuthMode>("signIn");

  return (
    <Surface style={styles.background}>
      <Stack.Screen options={{ headerShown: false }} />
      <Surface
        elevation={2}
        style={[
          styles.surface,
          {
            maxWidth: isWide ? "50%" : "100%",
            minHeight: isWide ? 0 : "100%",
            minWidth: isWide ? 0 : "100%",
          },
        ]}
      >
        <SafeAreaView style={styles.contentContainer}>
          <Text style={styles.content}> logotip.mp3 </Text>
          <SegmentedButtons
            value={authMode}
            onValueChange={(value) => setAuthMode(value as AuthMode)}
            buttons={[
              { value: "signIn", label: "Sign In" },
              { value: "signUp", label: "Sign Up" },
            ]}
            style={styles.content}
          />
          {authMode === "signIn" ? <SignInForm /> : <SignUpForm />}
        </SafeAreaView>
      </Surface>
    </Surface>
  );
}

const styles = StyleSheet.create({
  background: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
  },
  surface: {
    width: 544,
    height: "auto",
    alignItems: "center",
    justifyContent: "center",
  },
  contentContainer: {
    width: "80%",
    alignItems: "center",
    justifyContent: "flex-start",
    paddingTop: 64,
    paddingBottom: 256,
  },
  content: {
    width: "100%",
    marginBottom: 32,
  },
});
