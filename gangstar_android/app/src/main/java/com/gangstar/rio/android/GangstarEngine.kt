package com.gangstar.rio.android

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Game engine core for Gangstar Rio Android port

data class Vector2(var x: Float, var y: Float) {
    fun distance(other: Vector2): Float {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt(dx * dx + dy * dy)
    }

    fun normalize(): Vector2 {
        val len = sqrt(x * x + y * y)
        if (len > 0) {
            return Vector2(x / len, y / len)
        }
        return this
    }
}

// Game World - Open World Map
class GameWorld {
    var width: Float = 5000f
    var height: Float = 5000f
    val entities = mutableListOf<GameEntity>()
    val player: Player = Player()

    init {
        entities.add(player)
    }

    fun update(deltaTime: Float) {
        entities.forEach { it.update(deltaTime) }
        checkCollisions()
    }

    fun render() {
        entities.forEach { it.render() }
    }

    private fun checkCollisions() {
        for (i in entities.indices) {
            for (j in (i + 1) until entities.size) {
                val a = entities[i]
                val b = entities[j]
                if (a.position.distance(b.position) < a.radius + b.radius) {
                    a.onCollision(b)
                    b.onCollision(a)
                }
            }
        }
    }
}

// Base game entity
open class GameEntity {
    var position = Vector2(0f, 0f)
    var velocity = Vector2(0f, 0f)
    var radius = 32f
    var health = 100
    open var textureId = 0

    open fun update(deltaTime: Float) {
        position.x += velocity.x * deltaTime
        position.y += velocity.y * deltaTime
    }

    open fun render() {
        // Render sprite at position
    }

    open fun onCollision(other: GameEntity) {
    }
}

// Player character
class Player : GameEntity() {
    var cash = 0
    var armor = 0
    var weapons = mutableListOf<Weapon>()
    var currentMission: Mission? = null
    var missionProgress = 0f

    init {
        health = 100
        position = Vector2(2500f, 2500f) // Center of world
        radius = 20f
    }

    fun acceptMission(mission: Mission) {
        currentMission = mission
        missionProgress = 0f
    }

    fun completeMission() {
        currentMission?.let {
            cash += it.reward
            currentMission = null
        }
    }
}

// Mission system
enum class MissionType {
    TAXI, AMBULANCE, COP, DELIVERY, ROBBERY
}

data class Mission(
    val id: Int,
    val type: MissionType,
    val startLocation: Vector2,
    val targetLocation: Vector2,
    val timeLimit: Float,
    val reward: Int
) {
    var elapsedTime = 0f
    var isCompleted = false

    fun update(deltaTime: Float) {
        elapsedTime += deltaTime
        if (elapsedTime >= timeLimit) {
            isCompleted = true
        }
    }
}

// Vehicle system
class Vehicle(val type: String = "car") : GameEntity() {
    var speed = 500f
    var rotation = 0f
    var steering = 0f
    var isDriven = false
    var driver: Player? = null

    init {
        radius = 40f
        health = 200
    }

    override fun update(deltaTime: Float) {
        if (isDriven && driver != null) {
            // Vehicle physics
            val accelerationX = cos(rotation.toDouble()).toFloat() * speed
            val accelerationY = sin(rotation.toDouble()).toFloat() * speed

            velocity.x += accelerationX * steering * deltaTime
            velocity.y += accelerationY * steering * deltaTime

            // Apply friction
            velocity.x *= 0.98f
            velocity.y *= 0.98f
        }

        super.update(deltaTime)
    }
}

// Weapon system
class Weapon(val name: String, val damage: Int = 20) {
    var ammo = 100

    fun fire(): Boolean {
        return if (ammo > 0) {
            ammo--
            true
        } else false
    }
}

// NPC system
class NPC(val type: String = "civilian") : GameEntity() {
    var speed = 100f
    var destination = Vector2(0f, 0f)
    var taskType = "walk"

    init {
        radius = 18f
        health = 50
    }

    override fun update(deltaTime: Float) {
        when (taskType) {
            "walk" -> {
                val direction = destination.copy()
                direction.x -= position.x
                direction.y -= position.y
                val dist = sqrt(direction.x * direction.x + direction.y * direction.y)

                if (dist > 0) {
                    velocity.x = (direction.x / dist) * speed
                    velocity.y = (direction.y / dist) * speed
                }
            }
            "idle" -> {
                velocity.x = 0f
                velocity.y = 0f
            }
        }

        super.update(deltaTime)
    }
}

// Game state manager
class GameState {
    var currentScene = "menu"
    var isPaused = false
    val world = GameWorld()
    var gameTime = 0f

    fun update(deltaTime: Float) {
        gameTime += deltaTime
        if (!isPaused) {
            world.update(deltaTime)
        }
    }
}

// Input handler
class InputHandler {
    var moveX = 0f
    var moveY = 0f
    var isFiring = false
    var isAccelerating = false

    fun update() {
        // Will be implemented with Android input handling
    }
}

// Camera system (top-down view with player focus)
class Camera {
    var x = 0f
    var y = 0f
    var zoom = 1f

    fun update(targetX: Float, targetY: Float) {
        // Smooth camera follow
        x += (targetX - x) * 0.1f
        y += (targetY - y) * 0.1f
    }
}
