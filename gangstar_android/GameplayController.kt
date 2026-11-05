package com.gangstar.rio.android

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.atan2

class GameplayController(val gameState: GameState, val input: InputHandler) {
    private var currentVehicle: Vehicle? = null
    private var isInVehicle = false
    private val missionManager = MissionManager()

    init {
        initializeMissions()
    }

    fun update(deltaTime: Float) {
        val player = gameState.world.player

        // Handle player input
        if (input.moveX != 0f || input.moveY != 0f) {
            if (isInVehicle && currentVehicle != null) {
                updateVehicleControls(currentVehicle!!, deltaTime)
            } else {
                updatePlayerMovement(player, deltaTime)
            }
        }

        // Handle firing
        if (input.isFiring) {
            player.weapons.firstOrNull()?.let {
                if (it.fire()) {
                    // Fire weapon
                }
            }
        }

        // Update current mission
        player.currentMission?.let { mission ->
            mission.update(deltaTime)
            checkMissionProgress(player, mission)
        }

        // Check for vehicle entry
        checkVehicleEntry(player)
    }

    private fun updatePlayerMovement(player: Player, deltaTime: Float) {
        val speed = 200f // pixels/second
        val moveLength = kotlin.math.sqrt(input.moveX * input.moveX + input.moveY * input.moveY)

        if (moveLength > 0) {
            val normalizedX = input.moveX / moveLength
            val normalizedY = input.moveY / moveLength

            player.velocity.x = normalizedX * speed
            player.velocity.y = normalizedY * speed
        } else {
            player.velocity.x = 0f
            player.velocity.y = 0f
        }
    }

    private fun updateVehicleControls(vehicle: Vehicle, deltaTime: Float) {
        // Vehicle steering and acceleration
        vehicle.steering = kotlin.math.max(-1f, kotlin.math.min(1f, input.moveX))

        val acceleration = 500f
        val maxSpeed = 800f

        val velocityLength = kotlin.math.sqrt(vehicle.velocity.x * vehicle.velocity.x + vehicle.velocity.y * vehicle.velocity.y)

        if (input.moveY > 0 && velocityLength < maxSpeed) {
            // Accelerate forward
            val moveLength = kotlin.math.sqrt(input.moveX * input.moveX + input.moveY * input.moveY)
            if (moveLength > 0) {
                vehicle.velocity.x += (input.moveX / moveLength) * acceleration * deltaTime
                vehicle.velocity.y += (input.moveY / moveLength) * acceleration * deltaTime
            }
        } else if (input.moveY < 0) {
            // Reverse
            vehicle.velocity.x *= 0.95f
            vehicle.velocity.y *= 0.95f
        }

        // Update rotation based on velocity direction
        if (velocityLength > 0) {
            vehicle.rotation = atan2(vehicle.velocity.y.toDouble(), vehicle.velocity.x.toDouble()).toFloat()
        }

        // Apply friction
        vehicle.velocity.x *= 0.98f
        vehicle.velocity.y *= 0.98f
    }

    private fun checkVehicleEntry(player: Player) {
        gameState.world.entities.filterIsInstance<Vehicle>().forEach { vehicle ->
            if (player.position.distance(vehicle.position) < 50f && !isInVehicle) {
                // Enter vehicle
                enterVehicle(vehicle, player)
            }
        }
    }

    private fun enterVehicle(vehicle: Vehicle, player: Player) {
        isInVehicle = true
        currentVehicle = vehicle
        vehicle.isDriven = true
        vehicle.driver = player

        // Snap player position to vehicle
        player.position = vehicle.position.copy()
    }

    fun exitVehicle(player: Player) {
        currentVehicle?.let {
            it.isDriven = false
            it.driver = null
            isInVehicle = false

            // Eject player slightly away from vehicle
            val angle = kotlin.math.random() * 2 * Math.PI
            player.position.x += kotlin.math.cos(angle).toFloat() * 50f
            player.position.y += kotlin.math.sin(angle).toFloat() * 50f
        }
        currentVehicle = null
    }

    private fun checkMissionProgress(player: Player, mission: Mission) {
        val distanceToTarget = player.position.distance(mission.targetLocation)

        if (distanceToTarget < 100f) {
            // Mission complete
            player.completeMission()

            // Assign next mission
            val nextMissions = missionManager.getMissionsNear(player.position)
            nextMissions.firstOrNull()?.let { player.acceptMission(it) }
        }
    }

    private fun initializeMissions() {
        // Initialize mission generator
        missionManager.generateRandomMissions(20)
    }
}

class MissionManager {
    private val missions = mutableListOf<Mission>()
    private var missionIdCounter = 0

    fun generateRandomMissions(count: Int) {
        val types = listOf(MissionType.TAXI, MissionType.DELIVERY, MissionType.COP, MissionType.ROBBERY)

        repeat(count) {
            val type = types.random()
            val start = Vector2(
                (kotlin.math.random() * 5000).toFloat(),
                (kotlin.math.random() * 5000).toFloat()
            )
            val target = Vector2(
                (kotlin.math.random() * 5000).toFloat(),
                (kotlin.math.random() * 5000).toFloat()
            )
            val timeLimit = when (type) {
                MissionType.TAXI -> 120f
                MissionType.DELIVERY -> 180f
                MissionType.COP -> 300f
                MissionType.ROBBERY -> 250f
                else -> 100f
            }
            val reward = (100..1000).random()

            missions.add(Mission(missionIdCounter++, type, start, target, timeLimit, reward))
        }
    }

    fun getMissionsNear(position: Vector2, radius: Float = 500f): List<Mission> {
        return missions.filter { position.distance(it.startLocation) < radius }
    }

    fun getAllMissions(): List<Mission> = missions.toList()
}

// Combat system
class CombatSystem(val gameState: GameState) {
    fun fireWeapon(shooter: GameEntity, weapon: Weapon, direction: Vector2) {
        if (!weapon.fire()) return

        // Create projectile
        val projectile = Projectile(shooter, weapon.damage)
        projectile.position = shooter.position.copy()
        projectile.velocity = direction

        gameState.world.entities.add(projectile)
    }

    fun takeDamage(entity: GameEntity, damage: Int) {
        entity.health -= damage
        if (entity.health <= 0) {
            // Entity destroyed
            gameState.world.entities.remove(entity)
        }
    }
}

class Projectile(val shooter: GameEntity, val damage: Int) : GameEntity() {
    var lifeTime = 5f
    private var elapsedTime = 0f

    init {
        radius = 5f
        velocity = Vector2(400f, 0f) // Default direction
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        elapsedTime += deltaTime

        if (elapsedTime > lifeTime) {
            // Projectile expired
        }
    }

    override fun onCollision(other: GameEntity) {
        if (other != shooter) {
            // Deal damage
            other.health -= damage
        }
    }
}
