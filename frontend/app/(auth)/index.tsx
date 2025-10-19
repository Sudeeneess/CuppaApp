import { useState } from "react";
import { Stack } from "expo-router";
import { StyleSheet, Text, ScrollView, View } from "react-native";
import { Surface, SegmentedButtons } from "react-native-paper";
import { SignInForm } from "@/components/sign-in-form";
import { SignUpForm } from "@/components/sign-up-form";
import { useIsWide } from "@/hooks/use-is-wide";

export default function Auth() {
  const isWide = useIsWide();

  type AuthMode = "signIn" | "signUp";
  const [authMode, setAuthMode] = useState<AuthMode>("signIn");

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
        <ScrollView contentContainerStyle={styles.contentContainer}>
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
          <View style={styles.content}>
            {authMode === "signIn" ? <SignInForm /> : <SignUpForm />}
          </View>
        </ScrollView>
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
    maxHeight: "100%",
    alignItems: "stretch",
    justifyContent: "center",
  },
  contentContainer: {
    minHeight: "100%",
    paddingTop: 48,
    justifyContent: "center",
    alignItems: "center",
  },
  content: {
    width: "80%",
    marginBottom: 32,
  },
});
