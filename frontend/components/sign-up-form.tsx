import { useState } from "react";
import { Button, TextInput, Checkbox } from "react-native-paper";
import { StyleSheet, View, Text } from "react-native";
import { useAuth } from "@/hooks/use-auth";

export function SignUpForm() {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const { signUp } = useAuth();

  return (
    <View>
      <View style={{ flexDirection: "row" }}>
        <TextInput
          label={"First name"}
          style={[styles.textInput, { marginRight: 8 }]}
          onChangeText={setFirstName}
        ></TextInput>
        <TextInput
          label={"Last name"}
          style={styles.textInput}
          onChangeText={setLastName}
        ></TextInput>
      </View>
      <TextInput
        label={"Username"}
        style={styles.textInput}
        onChangeText={setUsername}
      ></TextInput>
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
        onPress={async () =>
          signUp({ firstName, lastName, username, email, password })
        }
      >
        <Text> Sign Up </Text>
      </Button>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {},
  button: {
    marginVertical: 8,
  },
  textInput: {
    marginBottom: 8,
    flex: 1,
  },
  checkBoxItem: {
    marginVertical: 8,
  },
});
