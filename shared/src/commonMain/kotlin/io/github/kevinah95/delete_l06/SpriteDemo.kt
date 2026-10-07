package io.github.kevinah95.delete_l06

import korlibs.event.Key
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.Bitmap32
import korlibs.image.bitmap.BmpSlice
import korlibs.image.bitmap.sliceWithSize
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.Korge
import korlibs.korge.input.keys
import korlibs.korge.view.*
import korlibs.math.geom.ScaleMode
import korlibs.math.geom.Size
import korlibs.render.GameWindow

import korlibs.time.*

/**
 * Datos de configuración de un personaje en el spritesheet.
 * Permite enseñar cómo recortar (segmentar) diferentes partes de la misma textura.
 */
data class CharacterSegment(
    val name: String,
    val description: String,
    val idleX: Int,
    val idleY: Int,
    val walkX: Int,
    val walkY: Int,
    val width: Int,
    val height: Int
)

suspend fun launchSpriteDemo() {
    Korge(
        virtualSize = Size(800, 600),
        windowSize = Size(800, 600),
        title = "Lección 06: Segmentación de Bitmaps y Movimiento con KorGE",
        backgroundColor = Colors["#1e1e2e"],
        quality = GameWindow.Quality.QUALITY,
        scaleMode = ScaleMode.SHOW_ALL
    ) {
        // Cargar el bitmap del spritesheet
        val bitmap: Bitmap = loadSpritesheet()

        // Definición de personajes segmentados desde el mismo spritesheet
        val characters = listOf(
            CharacterSegment(
                name = "Norman (Nigromante)",
                description = "Frame 1 (Brazos abajo) y Frame 2 (Brazos arriba)",
                idleX = 16, idleY = 18,
                walkX = 0, walkY = 18,
                width = 16, height = 15
            ),
            CharacterSegment(
                name = "Aldeano",
                description = "Variante 1 y Variante 2 del pueblo",
                idleX = 32, idleY = 18,
                walkX = 46, walkY = 18,
                width = 14, height = 15
            ),
            CharacterSegment(
                name = "Esqueleto",
                description = "Minion no-muerto con espada",
                idleX = 11, idleY = 33,
                walkX = 0, walkY = 33,
                width = 13, height = 15
            )
        )

        var selectedCharIndex = 0

        // Función auxiliar de segmentación
        fun getSlice(x: Int, y: Int, w: Int, h: Int): BmpSlice {
            val safeX = x.coerceIn(0, bitmap.width - 1)
            val safeY = y.coerceIn(0, bitmap.height - 1)
            val safeW = w.coerceIn(1, bitmap.width - safeX)
            val safeH = h.coerceIn(1, bitmap.height - safeY)
            return bitmap.sliceWithSize(safeX, safeY, safeW, safeH)
        }

        // --- ENCABEZADO DIDÁCTICO ---
        solidRect(800.0, 70.0, Colors["#181825"])
        text("LECCIÓN 06: KORGE & SEGMENTACIÓN DE BITMAPS", textSize = 22.0, color = Colors["#f9e2af"]) {
            xy(20.0, 15.0)
        }
        text("Aprende cómo recortar una textura (spritesheet) y mover un Sprite en pantalla", textSize = 13.0, color = Colors["#a6adc8"]) {
            xy(20.0, 44.0)
        }

        // --- ÁREA DE JUEGO / ESCENARIO ---
        val arena = solidRect(510.0, 390.0, Colors["#11111b"]) {
            xy(20.0, 85.0)
        }

        // Sombra y Sprite del Jugador en el escenario
        val spriteScale = 4.0
        val shadow = solidRect(14.0 * spriteScale, 4.0 * spriteScale, Colors["#000000"].withAd(0.35)) {
            anchor(0.5, 0.5)
            xy(275.0, 310.0)
        }

        val initialChar = characters[selectedCharIndex]
        var currentSlice = getSlice(initialChar.idleX, initialChar.idleY, initialChar.width, initialChar.height)
        val player = image(currentSlice) {
            anchor(0.5, 0.5)
            scale(spriteScale)
            xy(275.0, 280.0)
            smoothing = false
        }

        // --- PANEL DERECHO: INSPECTOR DE LA TEXTURA / SPRITESHEET ---
        val panelX = 550.0
        solidRect(230.0, 390.0, Colors["#181825"]) {
            xy(panelX, 85.0)
        }
        text("INSPECTOR DE BITMAP", textSize = 14.0, color = Colors["#cba6f7"]) {
            xy(panelX + 15.0, 95.0)
        }
        text("Textura original completa:", textSize = 11.0, color = Colors["#9399b2"]) {
            xy(panelX + 15.0, 115.0)
        }

        // Renderizado del spritesheet original en el inspector (escala 1:1 pixel-perfect)
        val sheetScale = 1.0
        val sheetView = image(bitmap) {
            xy(panelX + 15.0, 135.0)
            scale(sheetScale)
            smoothing = false
        }

        // Marco de resaltado que muestra visualmente el corte (slice) sobre la textura
        val highlightBorder = solidRect(16.0 * sheetScale, 15.0 * sheetScale, Colors["#f38ba8"].withAd(0.45)) {
            xy(panelX + 15.0 + initialChar.idleX * sheetScale, 135.0 + initialChar.idleY * sheetScale)
        }

        text("Recuadro rojo = Slice actual", textSize = 10.0, color = Colors["#f38ba8"]) {
            xy(panelX + 15.0, 260.0)
        }

        // --- PANEL INFERIOR: HUD DINÁMICO Y CONTROLES ---
        solidRect(760.0, 105.0, Colors["#181825"]) {
            xy(20.0, 485.0)
        }

        val infoTitle = text("DATOS EN TIEMPO REAL:", textSize = 12.0, color = Colors["#89b4fa"]) {
            xy(35.0, 495.0)
        }
        val infoSlice = text("", textSize = 12.0, color = Colors["#cdd6f4"]) {
            xy(35.0, 515.0)
        }
        val infoPos = text("", textSize = 12.0, color = Colors["#a6e3a1"]) {
            xy(35.0, 535.0)
        }
        val infoControls = text(
            "Controles: [Flechas / WASD] Mover  |  [1, 2, 3] Cambiar Personaje  |  [Espacio] Sprint  |  [R] Reset",
            textSize = 12.0,
            color = Colors["#f9e2af"]
        ) {
            xy(35.0, 565.0)
        }

        // --- LÓGICA DE ENTRADA Y MOVIMIENTO ---
        val pressedKeys = mutableSetOf<Key>()
        keys {
            down { keyEvent ->
                pressedKeys.add(keyEvent.key)
                when (keyEvent.key) {
                    Key.N1, Key.NUMPAD1 -> selectedCharIndex = 0
                    Key.N2, Key.NUMPAD2 -> selectedCharIndex = 1
                    Key.N3, Key.NUMPAD3 -> selectedCharIndex = 2
                    Key.R -> {
                        player.xy(275.0, 280.0)
                    }
                    else -> Unit
                }
            }
            up { keyEvent ->
                pressedKeys.remove(keyEvent.key)
            }
        }

        var animTimer = 0.0
        var isMoving = false
        var currentDirectionX = 1.0

        // Bucle de actualización (Update Loop a 60 FPS)
        addUpdater { dt ->
            val currentChar = characters[selectedCharIndex]

            // 1. Detectar dirección de movimiento
            var dx = 0.0
            var dy = 0.0
            if (Key.LEFT in pressedKeys || Key.A in pressedKeys) dx -= 1.0
            if (Key.RIGHT in pressedKeys || Key.D in pressedKeys) dx += 1.0
            if (Key.UP in pressedKeys || Key.W in pressedKeys) dy -= 1.0
            if (Key.DOWN in pressedKeys || Key.S in pressedKeys) dy += 1.0

            isMoving = (dx != 0.0 || dy != 0.0)

            // Multiplicador de velocidad (Sprint con barra espaciadora)
            val speed = if (Key.SPACE in pressedKeys) 220.0 else 120.0

            // 2. Aplicar movimiento con delta-time
            val dtSeconds = dt.milliseconds / 1000.0
            player.x = player.x + (dx * speed * dtSeconds)
            player.y = player.y + (dy * speed * dtSeconds)

            // Limitar al personaje dentro del área de juego
            val margin = 35.0
            player.x = player.x.coerceIn(arena.x + margin, arena.x + arena.width - margin)
            player.y = player.y.coerceIn(arena.y + margin, arena.y + arena.height - margin)

            // Actualizar la posición de la sombra
            shadow.x = player.x
            shadow.y = player.y + (currentChar.height * spriteScale) / 2.0 - 2.0

            // 3. Volteo horizontal (Flip horizontal por escala)
            if (dx > 0) currentDirectionX = 1.0
            else if (dx < 0) currentDirectionX = -1.0
            player.scaleX = spriteScale * currentDirectionX

            // 4. SEGMENTACIÓN: Alternar entre cuadros del Bitmap según el movimiento
            val (activeSliceX, activeSliceY) = if (isMoving) {
                animTimer = animTimer + dtSeconds
                val frameStep = (animTimer / 0.16).toInt() % 2
                if (frameStep == 0) currentChar.walkX to currentChar.walkY
                else currentChar.idleX to currentChar.idleY
            } else {
                animTimer = 0.0
                currentChar.idleX to currentChar.idleY
            }

            // Aplicar el nuevo recorte al sprite
            currentSlice = getSlice(activeSliceX, activeSliceY, currentChar.width, currentChar.height)
            player.bitmap = currentSlice

            // 5. Actualizar el marco indicador en el inspector del spritesheet
            highlightBorder.xy(
                panelX + 15.0 + activeSliceX * sheetScale,
                135.0 + activeSliceY * sheetScale
            )
            highlightBorder.scaledWidth = currentChar.width * sheetScale
            highlightBorder.scaledHeight = currentChar.height * sheetScale

            // 6. Actualizar textos didácticos en pantalla
            infoSlice.text = "Segmento activo: sliceWithSize(x = $activeSliceX, y = $activeSliceY, w = ${currentChar.width}, h = ${currentChar.height}) | [${currentChar.name}]"
            val stateText = if (isMoving) "CAMINANDO (alternando frames)" else "EN REPOSO (IDLE)"
            infoPos.text = "Posición: (x = ${player.x.toInt()}, y = ${player.y.toInt()})  |  Estado: $stateText  |  Mirando: ${if (currentDirectionX > 0) "Derecha" else "Izquierda"}"
        }
    }
}

/**
 * Carga el spritesheet desde los recursos o genera uno proceduralmente si no está disponible.
 */
suspend fun loadSpritesheet(): Bitmap {
    return try {
        resourcesVfs["sprites.png"].readBitmap()
    } catch (_: Throwable) {
        createFallbackSpritesheet()
    }
}

/**
 * Generador procedural de respaldo para asegurar que la demo funcione siempre.
 */
fun createFallbackSpritesheet(): Bitmap32 {
    val bmp = Bitmap32(160, 80)
    // Fondo transparente con cuadrícula tenue
    for (x in 0 until 160) {
        for (y in 0 until 80) {
            if ((x % 16 == 0) || (y % 16 == 0)) {
                bmp[x, y] = Colors["#2a2a3e"]
            }
        }
    }

    // Dibujar un personaje tipo Norman simple en x=16, y=18 (16x15)
    fun drawBox(ox: Int, oy: Int, w: Int, h: Int, color: RGBA) {
        for (i in 0 until w) {
            for (j in 0 until h) {
                bmp[ox + i, oy + j] = color
            }
        }
    }

    // Frame Idle
    drawBox(16, 18, 16, 15, Colors["#45475a"])
    drawBox(18, 20, 12, 10, Colors["#89b4fa"])
    drawBox(20, 22, 3, 3, Colors["#11111b"])
    drawBox(25, 22, 3, 3, Colors["#11111b"])

    // Frame Walk
    drawBox(0, 18, 16, 15, Colors["#45475a"])
    drawBox(2, 20, 12, 10, Colors["#a6e3a1"])
    drawBox(4, 22, 3, 3, Colors["#11111b"])
    drawBox(9, 22, 3, 3, Colors["#11111b"])

    // Aldeano en x=32, y=18 y x=46, y=18
    drawBox(32, 18, 14, 15, Colors["#fab387"])
    drawBox(46, 18, 12, 15, Colors["#f9e2af"])

    // Esqueleto en x=11, y=33
    drawBox(11, 33, 13, 15, Colors["#cdd6f4"])
    drawBox(0, 33, 11, 20, Colors["#bac2de"])

    return bmp
}
