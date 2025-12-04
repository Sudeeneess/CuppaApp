import { app, BrowserWindow } from "electron";
import isDev from "electron-is-dev";

let mainWindow: BrowserWindow | null;

function createWindow() {
  // 1. Создаем нативное окно браузера
  mainWindow = new BrowserWindow({
    // Опции окна
    width: 1200,
    height: 800,
    minWidth: 400,
    minHeight: 700,
    webPreferences: {
      // Ключевой параметр для безопасности:
      // Включает Node.js API в Renderer Process (если это необходимо)
      // В современных приложениях рекомендуется использовать Context Isolation
      // для повышения безопасности.
      nodeIntegration: false,
      contextIsolation: true,

      // Путь к preload-скрипту, если вы используете изоляцию контекста
      // preload: path.join(__dirname, 'preload.js'),
    },
  });

  // 2. Загружаем ваш контент

  // В разработке: загружаем адрес, который выдает ваш Webpack Dev Server (например, Expo Web)

  if (isDev) {
    // В режиме разработки Expo или Webpack Dev Server
    // Убедитесь, что этот порт совпадает с вашим веб-сборщиком
    mainWindow.loadURL("http://localhost:3000");
    mainWindow.webContents.openDevTools(); // Открываем DevTools для удобства
  } else {
    // В продакшене: загружаем скомпилированный HTML-файл
    mainWindow.loadFile("index.html");
  }

  // 3. Обработка закрытия окна
  mainWindow.on("closed", () => {
    mainWindow = null;
  });
}

// Запуск окна, когда приложение готово
app.on("ready", createWindow);

// Закрытие приложения, когда все окна закрыты (кроме macOS)
app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    app.quit();
  }
});

app.on("activate", () => {
  // На macOS обычно повторно создают окно в приложении,
  // после нажатия на док-иконку, когда нет открытых окон
  if (mainWindow === null) {
    createWindow();
  }
});
