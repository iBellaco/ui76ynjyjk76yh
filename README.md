# 📱 KWGT ChatGPT Monitor Widget & Token Tracker

[![Android Build](https://github.com/aistudio/kwgt-chatgpt-widget/actions/workflows/build-apk.yml/badge.svg)](https://github.com/aistudio/kwgt-chatgpt-widget/actions/workflows/build-apk.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-green.svg)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**KWGT ChatGPT** es una aplicación y Widget minimalista para la pantalla de inicio de Android que muestra la hora en tiempo real, el monitoreo del límite de 5 horas y consumo semanal de ChatGPT, gestión de código de autorización de OpenAI y registro de historial de tokens consumidos.

---

## 🚀 Características Principales

- ⏰ **Reloj Minimalista en Tiempo Real**: Visualización de hora y fecha con actualización automática cada minuto.
- ⏳ **Ventana de Consumo de 5 Horas**: Medidor visual del límite de peticiones y tokens en ventanas deslizantes de 5 horas.
- 📊 **Consumo Semanal de Tokens**: Barra de progreso y cálculo acumulado de tokens usados en la semana.
- 🔐 **Código de Autorización OpenAI**:
  - Muestra el código de autorización activo para sesión de OpenAI / ChatGPT.
  - Botón directo de **1 toque para COPIAR CÓDIGO** con confirmación.
  - Generador de nuevos códigos de autorización.
  - Enlaces directos para **Abrir OpenAI** (`platform.openai.com/usage` y `chatgpt.com`).
- 📜 **Historial de Tokens con Room**:
  - Base de datos local persistente con historial de prompts y modelo usado (`gpt-4o`, `o3-mini`, `gpt-4o-mini`).
  - Filtros por periodo (*5 Horas*, *Hoy*, *Todos*).
  - Calculador y simulador de tokens integrado.
- 🎨 **Soporte KWGT Kustom**:
  - Transmisión de variables en tiempo real a la app KWGT Kustom Widget (`$br(kwgt, chatgpt_5h_reqs)$`, `$br(kwgt, chatgpt_weekly_tokens)$`, `$br(kwgt, chatgpt_auth_code)$`).
  - Varios temas visuales (*KWGT Dark Minimal*, *Cyberpunk Mint*, *OLED Black*, *Glassmorphism*).

---

## 📱 Cómo Añadir el Widget a la Pantalla de Inicio

1. Mantén presionada cualquier área vacía en la pantalla de inicio de tu dispositivo Android.
2. Selecciona **Widgets**.
3. Busca **ChatGPT Monitor KWGT**.
4. Arrastra el widget a tu pantalla de inicio y redimensiónalo a tu gusto.

---

## 🛠️ Compilación y Compilador Local

Para compilar el proyecto manualmente con Gradle:

```bash
# Compilar APK de Depuración
gradle :app:assembleDebug

# Ejecutar Pruebas Unitarias
gradle :app:testDebugUnitTest
```

---

## 📦 Estructura del Proyecto

```text
/app
 ├── src/main/java/com/example
 │    ├── data/               # Room Database, Repositorio y Preferencias
 │    ├── ui/                 # Jetpack Compose UI (Screens, Theme, ViewModel)
 │    ├── widget/             # AppWidgetProvider y RemoteViews XML
 │    └── MainActivity.kt
 ├── src/main/res/
 │    ├── layout/             # Layout XML del Widget RemoteViews
 │    ├── xml/                # Provider Info de AppWidget
 │    └── values/             # Colores, Temas y Recursos
```

---

## 📄 Licencia

Este proyecto está bajo la Licencia **MIT**. Consulta el archivo [LICENSE](LICENSE) para más detalles.
