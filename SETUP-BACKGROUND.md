# CyberTrain — temporizador, notificaciones y alarma en segundo plano

## Configuración para CyberTrain v11

- La notificación de entrenamiento se publica inmediatamente como Foreground Service.
- La notificación de descanso se actualiza **cada segundo** en texto ligero; se mantiene el cronómetro nativo donde lo permite Android.
- La notificación de descanso **no incluye la imagen grande** para evitar bloquear SystemUI al actualizarla repetidamente.
- El estado del entrenamiento sigue guardado en DataStore.
- `restEndsAt` es la única fuente de verdad del cronómetro.
- `Handler` mantiene el temporizador en el Foreground Service.
- `AlarmManager` queda como respaldo para pantalla apagada/Doze/proceso recreado y **no se cancela** cuando Android destruye el servicio.
- `BootReceiver` restaura el servicio después de un reinicio y reprograma el descanso.
- La notificación es pública para pantalla bloqueada, sin `PublicVersion` genérico que reemplace el contenido. El nuevo canal es `active_workout_v11` (IMPORTANCE_DEFAULT, silencioso).
- El permiso de notificaciones se solicita antes de iniciar la rutina en Android 13+.
- La alarma exacta se detecta en Ajustes de avisos.
- El sonido GYM dura como máximo 7 segundos y usa por defecto 10% de volumen.
- No se solicita Audio Focus, por lo que CyberTrain no manda intencionalmente a la música a pausa.

## Permisos/configuración recomendada

### Notificaciones

Android 13+ requiere permiso de notificaciones.

Ajustes → Aplicaciones → CyberTrain → Notificaciones → Permitir.

En pantalla de bloqueo, selecciona mostrar notificaciones/contenido según la opción disponible en el teléfono.

### Alarmas exactas

Para Android 12+, permite:

Ajustes → Aplicaciones → Acceso especial → Alarmas y recordatorios → CyberTrain → Permitir.

La aplicación usa `setExactAndAllowWhileIdle()` cuando tiene este permiso.

### Batería

Para máxima confiabilidad en equipos que aplican restricciones agresivas:

Ajustes → Aplicaciones → CyberTrain → Batería → Sin restricciones / No optimizar.

Algunos fabricantes pueden tener un segundo control de inicio automático o ahorro de batería.

## Pantalla apagada y teléfono bloqueado

El temporizador está diseñado para continuar con la pantalla apagada y el teléfono bloqueado. El Foreground Service mantiene el estado, `PARTIAL_WAKE_LOCK` protege el CPU durante un descanso activo y `AlarmManager` actúa como respaldo.

## Teléfono físicamente apagado

Una aplicación Android normal no puede ejecutar código ni reproducir audio mientras el teléfono está completamente apagado. Android cancela las alarmas programadas al apagar el equipo.

Después de encenderse, `BootReceiver` intenta iniciar CyberTrain y recuperar el entrenamiento guardado. Si Android restringe ese inicio, al abrir la aplicación se reanuda el servicio. Si el descanso ya venció mientras el teléfono estaba apagado, CyberTrain lo reconcilia al arrancar y puede reproducir el aviso después de que Android permita ejecutar el servicio.

## Música

CyberTrain no solicita `AudioFocus` para la alarma. El aviso se reproduce con `MediaPlayer` y se limita a 7 segundos. El volumen por defecto es 10% del volumen interno del reproductor; el volumen del sistema del teléfono sigue aplicando.
