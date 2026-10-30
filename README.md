CyberTrain 🏋️‍♂️
Aplicación Android para planificar, ejecutar y registrar entrenamientos de fuerza e hipertrofia. Combina el método 5/3/1, rutinas personalizables y herramientas de cálculo en una interfaz de estilo cyberpunk desarrollada con Kotlin y Jetpack Compose.
Estado del proyecto: versión 11, en desarrollo y validación en dispositivos. El código fuente incluye mejoras para temporizadores y notificaciones en segundo plano; su comportamiento en pantalla bloqueada aún requiere pruebas en equipos reales.

🎯 Objetivo
Crear una aplicación de entrenamiento que permita organizar rutinas, calcular cargas de trabajo, controlar descansos entre series y consultar el historial de sesiones, sin depender de servicios en la nube.
✨ Funcionalidades
- Dos modalidades de entrenamiento: fuerza e hipertrofia.
- Método 5/3/1: cálculo automático de series y cargas por semana, con opción de ingresar pesos manuales.
- Rutinas editables: agregar, modificar, duplicar, reordenar y eliminar ejercicios.
- Seguimiento de series: registrar progreso y descansos durante una sesión.
- Temporizador de descanso: contador basado en una hora de finalización absoluta, con notificaciones de entrenamiento, Foreground Service y alarmas de respaldo mediante AlarmManager.
- Historial: consulta de sesiones finalizadas y volumen registrado.
- Herramientas de entrenamiento: estimación de 1RM (Epley y Brzycki), porcentajes de carga y calculadora de discos.
- Almacenamiento local: conservación de rutinas, configuración y sesión activa mediante DataStore.
- Diseño cyberpunk: interfaz con elementos visuales neón, creada con Jetpack Compose y Material 3.
🛠️ Tecnologías
Tecnología	Uso en el proyecto
Kotlin	Lenguaje principal
Jetpack Compose / Material 3	Interfaz de usuario declarativa
Navigation Compose	Navegación entre pantallas
DataStore Preferences	Persistencia local
Kotlin Coroutines / Flow	Procesamiento asíncrono y estado reactivo
Kotlinx Serialization	Serialización de datos
Foreground Service / AlarmManager / WakeLock	Temporizador y avisos de descanso
JUnit	Pruebas unitarias de cálculos y rutinas
Gradle Kotlin DSL	Configuración y compilación


📂 Organización del código
app/src/main/java/com/tecnosuper/neon531/
├── data/       # Modelos y almacenamiento local
├── domain/     # Reglas 5/3/1, 1RM, rutinas y cálculo de discos
├── service/    # Servicio de entrenamiento, temporizador y receptores
└── ui/         # Pantallas, componentes, navegación y tema visual
🚀 Ejecutar el proyecto
1. Clona el repositorio y ábrelo en Android Studio.
2. Configura JDK 17 y el SDK de Android 35.
3. Sincroniza las dependencias de Gradle.
4. Ejecuta la app en un emulador o dispositivo con Android 8.0 (API 26) o superior.
5. Para probar las alertas, autoriza las notificaciones y revisa los permisos de alarmas y las opciones de pantalla bloqueada del dispositivo.
Nota sobre la compilación: la versión fuente publicada originalmente no contiene gradlew, gradlew.bat ni gradle-wrapper.jar, aunque sí incluye la configuración para Gradle 8.9. Si no cuentas con el wrapper, genéralo con Gradle instalado o desde Android Studio antes de compilar.
🧪 Pruebas y estado
El repositorio contiene pruebas unitarias de 5/3/1, estimación de 1RM y configuración de rutinas manuales. Las últimas modificaciones del temporizador se revisaron de forma estática, pero aún no han sido compiladas ni probadas en un dispositivo físico. El proyecto no debe considerarse una versión estable de producción hasta completar esas pruebas.
📸 Capturas de pantalla
Pendiente: agregar imágenes reales de la pantalla principal, la rutina activa, el modo 5/3/1, el historial y las herramientas de cálculo.
🔜 Próximas mejoras
- Validar temporizador, alarma y notificaciones en diferentes fabricantes y versiones de Android.
- Ampliar las pruebas instrumentadas y de interfaz.
- Incorporar capturas de pantalla y una demostración del funcionamiento.
- Preparar una versión instalable de demostración cuando la compilación esté validada.
📌 Sobre el desarrollo
Este proyecto reúne prácticas de desarrollo móvil nativo, diseño de interfaces, lógica de negocio, almacenamiento local y gestión de procesos en segundo plano. Es parte de mi portafolio de desarrollo de software y continúa en evolución.
