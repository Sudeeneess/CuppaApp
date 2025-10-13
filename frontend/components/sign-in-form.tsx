import { Button, TextInput, Checkbox } from "react-native-paper";
import { StyleSheet, View, Text } from "react-native";

export function SignInForm() {
  return (
    <View style={styles.container}>
      <TextInput label={"Email"} style={styles.textInput}></TextInput>
      <TextInput
        label={"Password"}
        secureTextEntry
        right={<TextInput.Icon icon="eye" />}
        style={styles.textInput}
      ></TextInput>
      <Checkbox.Item
        label="Stay signed in"
        status="unchecked"
        style={styles.checkBoxItem}
      />
      <Button mode="contained" style={styles.button}>
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
