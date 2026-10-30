# CyberTrain v11 — corrección de temporizador y pantalla bloqueada

Proyecto Android (Kotlin / Jetpack Compose) actualizado a partir del ZIP **CyberTrain-v10-fixed(2).zip**. Se mantiene el mismo `applicationId` y la persistencia original en DataStore.

## Cambios realizados

- El contador de descanso está anclado a `restEndsAt`, una fecha/hora absoluta, no a un número que se decrementa en memoria.
- Mientras hay descanso, la notificación **actualiza también el título visible una vez por segundo**. Mantiene el cronómetro nativo como apoyo. Se evita volver a enviar una imagen pesada a Android SystemUI en cada actualización.
- Nuevo canal `active_workout_v11`: importancia DEFAULT con sonido desactivado, visibilidad PUBLIC. La notificación ya **no tiene un PublicVersion genérico** que pueda reemplazar el texto del contador en pantalla bloqueada.
- La destrucción del servicio elimina sus callbacks internos, pero **no cancela la alarma programada en AlarmManager**. El fin normal o la cancelación de un descanso sí cancela la alarma previa.
- Al volver a abrir la aplicación, `MainActivity` consulta DataStore y vuelve a iniciar el servicio si detecta una sesión sin finalizar.
- WakeLock acotado a la duración del descanso más un margen de 15 segundos, con máximo de 1 hora por adquisición.
- La pantalla de entrenamiento actualiza su contador cada segundo (antes cuatro veces por segundo).
- Eliminados ocho imports inválidos de `androidx.compose.foundation.layout.weight`, **sin eliminar el uso válido de `Modifier.weight`** en filas/columnas.
- Se mantienen intactas las funciones 5/3/1 de cálculo automático, pesos manuales, incrementos por levantamiento y edición de rutinas.
- Sonido GYM de hasta 7 segundos y volumen interno máximo del 10% se conservan sin cambios.

## Instalación y comprobación en dispositivo

1. Abrir esta carpeta como proyecto en Android Studio y sincronizar con Gradle. Es un **proyecto de código fuente**, no un APK ya compilado.
2. Instalar la nueva versión **sobre la anterior**, conservando los datos (no borrar datos ni desinstalar si deseas preservar las rutinas).
3. Permitir las **notificaciones** y configurar **mostrar en pantalla de bloqueo**, también para el canal *Entrenamiento activo*.
4. En **Avisos de descanso**, comprobar y, de ser posible, conceder el permiso de **Alarmas y recordatorios** (Android 12+).
5. Si el fabricante restringe apps en segundo plano, poner CyberTrain en modo de batería **Sin restricciones**.
6. Iniciar una rutina, terminar una serie con 30–60 s de descanso, bloquear la pantalla: el aviso debe mostrar `DESCANSO · 00:XX`, actualizándose con el paso del tiempo (salvo restricciones de SystemUI/fabricante), hasta la finalización.
7. Repetir el ensayo bloqueando y desbloqueando, quitando la app de recientes, y abriéndola de nuevo.

**Limitaciones:** ningún código de aplicación normal puede sonar con el teléfono físicamente apagado, ni anular una configuración del sistema que oculte notificaciones bloqueadas. Algunos fabricantes también pueden detener servicios o retrasar alarmas. En esta entrega no se ejecutó el proyecto en un teléfono físico ni se compiló un APK: son cambios y verificaciones estáticas del código fuente.

**Gradle:** el ZIP original traía `gradle/wrapper/gradle-wrapper.properties` pero no `gradlew`, `gradlew.bat` ni `gradle-wrapper.jar`. Se mantiene esa estructura. Abrir con Android Studio; si pide wrapper, generarlo localmente con Gradle 8.9 (`gradle wrapper --gradle-version 8.9`).
