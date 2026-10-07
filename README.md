# Lección 06: Introducción a KorGE — Segmentación de Bitmaps y Movimiento 2D

Demo práctica y didáctica para la clase sobre **KorGE Game Engine**, enfocada en:
1. **Carga de texturas (Bitmaps y Spritesheets)**.
2. **Segmentación de Bitmaps (`sliceWithSize` / `BmpSlice`)**: Cómo recortar partes específicas de una imagen en lugar de cargar múltiples archivos.
3. **Control y Movimiento de Sprites**: Captura de teclado, cálculo con `delta-time` (`dt`) y volteo horizontal (`scaleX`).
4. **Animación básica por alternancia de frames**: Cambio de slices al caminar vs reposo.

---

## 🚀 Cómo ejecutar la Demo

Desde la terminal o desde la configuración de ejecución de Android Studio / IntelliJ:

```bash
./gradlew :desktopApp:run
```

O para ejecutar los tests unitarios:

```bash
./gradlew :shared:jvmTest
```

---

## 🎮 Controles en Pantalla

| Tecla | Acción | Concepto Explicado |
|---|---|---|
| **Flechas (← ↑ → ↓)** o **W / A / S / D** | Mover al personaje | Actualización de posición (`x += dx * speed * dt`) |
| **1, 2, 3** | Cambiar personaje (Norman, Aldeano, Esqueleto) | Segmentación de diferentes áreas del mismo Bitmap |
| **Barra Espaciadora** | Sprint / Acelerar | Modificador dinámico de velocidad |
| **R** | Reiniciar posición al centro | Restablecer coordenadas iniciales |

---

## 🧠 Conceptos Clave para la Clase

### 1. ¿Qué es un Bitmap en KorGE?
Un `Bitmap` es una imagen rasterizada cargada en memoria de GPU como textura:
```kotlin
val bitmap: Bitmap = resourcesVfs["sprites.png"].readBitmap()
```

### 2. Segmentación del Bitmap (Slicing)
En videojuegos, los sprites se agrupan en un **Spritesheet (Atlas)** para optimizar la memoria y reducir las llamadas de dibujo (draw calls). Para usar un sprite individual, se "segmenta" un rectángulo:
```kotlin
// bitmap.sliceWithSize(x, y, width, height)
val idleFrame = bitmap.sliceWithSize(x = 16, y = 18, width = 16, height = 15)
val walkFrame = bitmap.sliceWithSize(x = 0, y = 18, width = 16, height = 15)
```

### 3. El Sprite en la Escena (`Image`)
Se crea una vista de tipo `Image` que renderiza el recorte actual:
```kotlin
val player = image(idleFrame) {
    anchor(0.5, 0.5) // Punto de pivote en el centro
    scale(4.0)       // Escalado 4x para pixel art nítido
    xy(275.0, 280.0)
}
```

### 4. Animación y Movimiento en el Game Loop (`addUpdater`)
En cada cuadro (`dt`), si hay movimiento, alternamos el slice asignado a `player.bitmap`:
```kotlin
addUpdater { dt ->
    val dtSeconds = dt.milliseconds / 1000.0
    player.x = player.x + (dx * speed * dtSeconds)
    player.y = player.y + (dy * speed * dtSeconds)

    if (isMoving) {
        val frame = (timer / 0.16).toInt() % 2
        player.bitmap = if (frame == 0) walkFrame else idleFrame
    } else {
        player.bitmap = idleFrame
    }
}
```