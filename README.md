
<div align="center">

# ⚡ CyberTrain

### 🏋️ Entrena con precisión. Supera tus límites.

**Aplicación Android para gestionar entrenamientos de fuerza e hipertrofia.**

![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Development-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack-Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Estado](https://img.shields.io/badge/Estado-En%20desarrollo-FFB300?style=for-the-badge)

</div>

---

## 📱 ¿Qué es CyberTrain?

**CyberTrain** es una aplicación móvil desarrollada de forma nativa para Android utilizando **Kotlin y Jetpack Compose**.

Su objetivo es facilitar la planificación, ejecución y seguimiento de entrenamientos de fuerza e hipertrofia mediante una interfaz moderna de estilo **cyberpunk**.

La aplicación integra el método de entrenamiento **5/3/1**, rutinas personalizables, calculadoras de fuerza, temporizadores de descanso e historial de entrenamientos.

---

## ✨ Funcionalidades principales

- 🏋️ **Entrenamientos de fuerza e hipertrofia:** diferentes modalidades de entrenamiento.
- 📈 **Método 5/3/1:** cálculo automático de cargas y configuración manual de pesos.
- ⚙️ **Rutinas personalizables:** creación y edición de ejercicios y entrenamientos.
- ⏱️ **Temporizador de descanso:** control del tiempo entre series.
- 🔔 **Notificaciones:** avisos durante las sesiones mediante servicios de Android.
- 📊 **Historial:** seguimiento de entrenamientos realizados.
- 🧮 **Calculadoras:** estimación de repetición máxima (1RM), porcentajes y distribución de discos.
- 💾 **Persistencia local:** almacenamiento de rutinas, configuraciones y sesiones.
- 🌃 **Diseño cyberpunk:** interfaz futurista con elementos visuales personalizados.

---

## 🛠️ Tecnologías utilizadas

| Tecnología | Uso |
| :--- | :--- |
| **Kotlin** | Lenguaje principal |
| **Jetpack Compose** | Desarrollo de interfaces |
| **Material 3** | Componentes visuales |
| **Navigation Compose** | Navegación |
| **DataStore** | Almacenamiento local |
| **Coroutines y Flow** | Programación asíncrona |
| **Kotlinx Serialization** | Serialización de datos |
| **Foreground Service** | Servicio de entrenamiento |
| **AlarmManager** | Gestión de alarmas |
| **JUnit** | Pruebas unitarias |
| **Gradle** | Gestión de dependencias |

---

## 📂 Arquitectura del proyecto

```text
app/src/main/java/com/tecnosuper/neon531/
│
├── data/
│   ├── Models.kt
│   └── WorkoutStore.kt
│
├── domain/
│   ├── FiveThreeOne.kt
│   ├── OneRepMax.kt
│   ├── PlateCalculator.kt
│   └── RoutineFactory.kt
│
├── service/
│   ├── WorkoutService.kt
│   ├── RestTimerReceiver.kt
│   └── BootReceiver.kt
│
├── ui/
│   ├── screens/
│   ├── components/
│   └── theme/
│
└── MainActivity.kt
```

---

## 🚀 Instalación

1. Descarga o clona este repositorio.
2. Abre el proyecto en **Android Studio**.
3. Configura **JDK 17** y Android SDK 35.
4. Sincroniza las dependencias de Gradle.
5. Ejecuta la aplicación en un dispositivo Android 8.0 o superior.

> **Nota:** es posible que necesites generar el Gradle Wrapper 8.9 antes de compilar el proyecto.

---

## 📸 Capturas de pantalla

Próximamente se agregarán capturas de:

- Pantalla principal.
- Gestión de rutinas.
- Entrenamiento activo.
- Método 5/3/1.
- Historial y calculadoras.

---

## 🧪 Estado del proyecto

**🚧 Versión 11 — En desarrollo**

Actualmente se están realizando mejoras y validaciones relacionadas con:

- Estabilidad del temporizador.
- Actualización de notificaciones.
- Funcionamiento con pantalla bloqueada.
- Recuperación de sesiones.

Las correcciones recientes todavía requieren validación en dispositivos Android reales.

---

## 🎯 Próximas mejoras

- [ ] Validación en diferentes dispositivos Android.
- [ ] Pruebas de integración y de interfaz.
- [ ] Capturas y demostración de la aplicación.
- [ ] Publicación de una versión instalable estable.

---

## 👨‍💻 Sobre el proyecto

Desarrollé **CyberTrain** como parte de mi portafolio personal de desarrollo de software.

Este proyecto me ha permitido aplicar y fortalecer conocimientos en:

- Desarrollo móvil nativo con Kotlin.
- Arquitectura y organización de aplicaciones.
- Diseño de interfaces con Jetpack Compose.
- Implementación de lógica de negocio.
- Persistencia y gestión de datos locales.
- Manejo de servicios y notificaciones de Android.

---

<div align="center">

### ⚡ CyberTrain

**Constancia • Fuerza • Progreso**

</div>
