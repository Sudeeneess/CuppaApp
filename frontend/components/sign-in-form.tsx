import { useState } from "react";
import { Button, TextInput, Checkbox } from "react-native-paper";
import { StyleSheet, View, Text } from "react-native";
import { useAuth } from "@/hooks/use-auth";

export function SignInForm() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const { signIn } = useAuth();

  return (
    <View style={styles.container}>
      <TextInput
        label={"Email"}
        style={styles.textInput}
        onChangeText={setEmail}
      ></TextInput>
      <TextInput
        label={"Password"}
        secureTextEntry
        right={<TextInput.Icon icon="eye" />}
        style={styles.textInput}
        onChangeText={setPassword}
      ></TextInput>
      <Checkbox.Item
        label="Stay signed in"
        status="unchecked"
        style={styles.checkBoxItem}
      />
      <Button
        mode="contained"
        style={styles.button}
        onPress={async () => signIn({ email, password })}
      >
        <Text> Sign In </Text>
      </Button>
      <Button mode="text" style={styles.button}>
        <Text> Forgot Password? </Text>
      </Button>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: "100%",
    height: "auto",
    flexDirection: "column",
    alignItems: "stretch",
    justifyContent: "flex-start",
  },
  button: {
    marginVertical: 8,
  },
  textInput: {
    marginBottom: 8,
  },
  checkBoxItem: {
    marginVertical: 8,
  },
});
