import { StatusBar } from "expo-status-bar";
import { StyleSheet, View } from "react-native";
import { SafeAreaProvider, SafeAreaView } from "react-native-safe-area-context";
import { Text, Button, PaperProvider, Surface } from "react-native-paper";
import { CustomLightTheme, CustomDarkTheme } from "./CustomThemes";
import MaterialCommunityIcon from "./IconProvider";

export default function App() {
  return (
    <PaperProvider
      settings={{ icon: MaterialCommunityIcon }}
      theme={CustomDarkTheme}
    >
      <SafeAreaProvider>
        <Surface style={styles.container}>
          <SafeAreaView style={styles.container}>
            <Surface style={styles.container}>
              <Surface style={styles.container2}>
                <Surface style={styles.container}>
                  <View></View>
                </Surface>
                <Surface style={styles.container} elevation={2}>
                  <Button icon="camera" mode="contained">
                    eshkere
                  </Button>
                </Surface>
                <Surface style={styles.container}>
                  <View></View>
                </Surface>
              </Surface>
              <StatusBar style="auto" />
            </Surface>
          </SafeAreaView>
        </Surface>
      </SafeAreaProvider>
    </PaperProvider>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: "column",
  },
  container2: {
    marginTop: "30%",
    flex: 1,
    flexDirection: "row",
  },
});
