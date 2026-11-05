package com.gangstar.rio.android

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

// Sprite & Asset Management System

class Sprite(
    val name: String,
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 64f,
    var height: Float = 64f,
    var rotation: Float = 0f
) {
    var alpha = 255
    var scaleX = 1f
    var scaleY = 1f
    var textureId = 0

    fun drawPixelArt(canvas: Canvas, paint: Paint) {
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(rotation)
        canvas.scale(scaleX, scaleY)

        paint.alpha = alpha
        when (name) {
            "player" -> drawPlayer(canvas, paint)
            "car" -> drawCar(canvas, paint)
            "npc" -> drawNPC(canvas, paint)
            else -> drawDefault(canvas, paint)
        }

        canvas.restore()
    }

    private fun drawPlayer(canvas: Canvas, paint: Paint) {
        // Player sprite (blue box with head)
        paint.color = Color.BLUE
        canvas.drawRect(-10f, -20f, 10f, 20f, paint)

        paint.color = Color.CYAN
        canvas.drawCircle(0f, -25f, 8f, paint)

        // Gun
        paint.color = Color.BLACK
        canvas.drawRect(10f, -5f, 20f, 5f, paint)
    }

    private fun drawCar(canvas: Canvas, paint: Paint) {
        // Car sprite (red rectangle with wheels)
        paint.color = Color.RED
        canvas.drawRect(-15f, -10f, 15f, 10f, paint)

        // Windows
        paint.color = Color.CYAN
        canvas.drawRect(-10f, -6f, 0f, -2f, paint)
        canvas.drawRect(2f, -6f, 10f, -2f, paint)

        // Wheels
        paint.color = Color.BLACK
        canvas.drawCircle(-12f, 12f, 4f, paint)
        canvas.drawCircle(12f, 12f, 4f, paint)

        // Headlights
        paint.color = Color.YELLOW
        canvas.drawRect(13f, -3f, 16f, 3f, paint)
    }

    private fun drawNPC(canvas: Canvas, paint: Paint) {
        // NPC sprite (yellow figure)
        paint.color = Color.YELLOW
        canvas.drawRect(-6f, -15f, 6f, 15f, paint)

        paint.color = Color.WHITE
        canvas.drawCircle(0f, -18f, 5f, paint)
    }

    private fun drawDefault(canvas: Canvas, paint: Paint) {
        paint.color = Color.GRAY
        canvas.drawRect(-width / 2, -height / 2, width / 2, height / 2, paint)
    }
}

// Visual Effects system
class ParticleEffect(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val lifetime: Float = 1f
) {
    var age = 0f
    var size = 5f
    var color = Color.WHITE

    fun update(deltaTime: Float) {
        age += deltaTime
        x += vx * deltaTime
        y += vy * deltaTime
        size *= 0.98f
    }

    fun isAlive(): Boolean = age < lifetime

    fun draw(canvas: Canvas, paint: Paint) {
        val alpha = ((1 - age / lifetime) * 255).toInt()
        paint.alpha = alpha
        paint.color = color

        canvas.drawCircle(x, y, size, paint)
    }
}

class ParticleSystem {
    private val particles = mutableListOf<ParticleEffect>()

    fun addExplosion(x: Float, y: Float, count: Int = 10) {
        repeat(count) {
            val angle = (Math.random() * 2 * Math.PI).toFloat()
            val speed = (200 + Math.random() * 300).toFloat()

            particles.add(ParticleEffect(
                x, y,
                kotlin.math.cos(angle.toDouble()).toFloat() * speed,
                kotlin.math.sin(angle.toDouble()).toFloat() * speed,
                lifetime = 0.5f
            ).apply {
                color = when {
                    Math.random() < 0.3 -> Color.RED
                    Math.random() < 0.6 -> Color.YELLOW
                    else -> Color.WHITE
                }
            })
        }
    }

    fun update(deltaTime: Float) {
        particles.forEach { it.update(deltaTime) }
        particles.removeAll { !it.isAlive() }
    }

    fun render(canvas: Canvas, paint: Paint) {
        particles.forEach { it.draw(canvas, paint) }
    }
}

// UI System
class UIElement(
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float,
    val text: String = ""
) {
    var backgroundColor = Color.parseColor("#333333")
    var textColor = Color.WHITE
    var isVisible = true

    fun draw(canvas: Canvas, paint: Paint) {
        if (!isVisible) return

        // Background
        paint.color = backgroundColor
        canvas.drawRect(x, y, x + width, y + height, paint)

        // Border
        paint.color = Color.WHITE
        paint.strokeWidth = 2f
        canvas.drawRect(x, y, x + width, y + height, paint)

        // Text
        if (text.isNotEmpty()) {
            paint.color = textColor
            paint.textSize = 24f
            canvas.drawText(text, x + 10, y + height / 2 + 10, paint)
        }
    }
}

class UIPanel(
    x: Float,
    y: Float,
    width: Float,
    height: Float
) : UIElement(x, y, width, height) {
    private val elements = mutableListOf<UIElement>()

    fun addElement(element: UIElement) {
        elements.add(element)
    }

    fun draw(canvas: Canvas, paint: Paint) {
        super.draw(canvas, paint)
        elements.forEach { it.draw(canvas, paint) }
    }
}

// HUD Manager
class HUDManager {
    private val panels = mutableListOf<UIPanel>()
    private var healthPanel: UIElement? = null
    private var cashPanel: UIElement? = null
    private var minimap: UIElement? = null

    init {
        createPanels()
    }

    private fun createPanels() {
        // Health panel (top-left)
        healthPanel = UIElement(10f, 10f, 150f, 50f, "Health: 100")

        // Cash panel (top-right)
        cashPanel = UIElement(250f, 10f, 150f, 50f, "Cash: $0")

        // Minimap (bottom-right)
        minimap = UIElement(0f, 0f, 120f, 120f).apply {
            backgroundColor = Color.parseColor("#1a1a2e")
        }
    }

    fun update(player: Player) {
        healthPanel?.text = "Health: ${player.health}"
        cashPanel?.text = "Cash: \$${player.cash}"
    }

    fun draw(canvas: Canvas, paint: Paint, screenWidth: Float, screenHeight: Float) {
        healthPanel?.apply {
            x = 10f
            y = 10f
            draw(canvas, paint)
        }

        cashPanel?.apply {
            x = screenWidth - 160f
            y = 10f
            draw(canvas, paint)
        }

        minimap?.apply {
            x = screenWidth - 130f
            y = screenHeight - 130f
            draw(canvas, paint)
        }
    }
}

// Animation System
class Animation(
    val frames: List<Sprite>,
    val duration: Float = 1f
) {
    var isPlaying = true
    var currentFrame = 0
    var elapsedTime = 0f

    fun update(deltaTime: Float) {
        if (!isPlaying) return

        elapsedTime += deltaTime
        val frameTime = duration / frames.size

        if (elapsedTime >= frameTime) {
            currentFrame = (currentFrame + 1) % frames.size
            elapsedTime = 0f

            if (currentFrame == 0 && frames.size > 0) {
                // Loop completed
            }
        }
    }

    fun getCurrentFrame(): Sprite? = frames.getOrNull(currentFrame)

    fun draw(canvas: Canvas, paint: Paint) {
        getCurrentFrame()?.drawPixelArt(canvas, paint)
    }
}

// Sprite Factory
object SpriteFactory {
    fun createPlayerSprite(): Sprite = Sprite("player", 0f, 0f, 20f, 40f)
    fun createCarSprite(): Sprite = Sprite("car", 0f, 0f, 30f, 20f)
    fun createNPCSprite(): Sprite = Sprite("npc", 0f, 0f, 12f, 30f)

    fun createExplosionAnimation(): Animation {
        return Animation(
            listOf(
                Sprite("explosion1", 0f, 0f, 20f, 20f),
                Sprite("explosion2", 0f, 0f, 25f, 25f),
                Sprite("explosion3", 0f, 0f, 30f, 30f)
            ),
            duration = 0.3f
        )
    }
}
