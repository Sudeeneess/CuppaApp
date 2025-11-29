import { Stack, useLocalSearchParams } from "expo-router";
import { View, Text, StyleSheet } from "react-native";

export default function Chat() {
  const { id } = useLocalSearchParams();
  return (
    <View style={styles.container}>
      <Stack.Screen />
      <Text> {id} </Text>
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
