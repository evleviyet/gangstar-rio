package com.gangstar.rio.android

import kotlin.math.sqrt

// Police System
class PoliceUnit(x: Float, y: Float) : GameEntity() {
    var targetPlayer: Player? = null
    var patrolRoute = mutableListOf<Vector2>()
    var patrolIndex = 0
    var alertLevel = 0f // 0-100

    init {
        position = Vector2(x, y)
        health = 80
        radius = 18f
        textureId = 2
    }

    override fun update(deltaTime: Float) {
        if (targetPlayer != null) {
            // Chase mode
            val distance = position.distance(targetPlayer!!.position)
            if (distance < 50f) {
                // Engage
                velocity = Vector2(
                    (targetPlayer!!.position.x - position.x) * 2,
                    (targetPlayer!!.position.y - position.y) * 2
                )
            }
        } else {
            // Patrol mode
            if (patrolRoute.isNotEmpty()) {
                val target = patrolRoute[patrolIndex]
                val distance = position.distance(target)

                if (distance < 50f) {
                    patrolIndex = (patrolIndex + 1) % patrolRoute.size
                } else {
                    velocity = Vector2(
                        (target.x - position.x) * 0.5f,
                        (target.y - position.y) * 0.5f
                    )
                }
            }
        }

        velocity.x *= 0.95f
        velocity.y *= 0.95f
        super.update(deltaTime)
    }

    fun engage(player: Player) {
        targetPlayer = player
        alertLevel = 100f
    }

    fun disengage() {
        targetPlayer = null
        alertLevel = 0f
    }
}

class PoliceStation {
    var units = mutableListOf<PoliceUnit>()
    var wantedLevel = 0 // 0-5 stars

    fun spawnPatrol(x: Float, y: Float): PoliceUnit {
        val unit = PoliceUnit(x, y)
        units.add(unit)
        return unit
    }

    fun increaseWanted() {
        wantedLevel = minOf(5, wantedLevel + 1)
    }

    fun decreaseWanted() {
        wantedLevel = maxOf(0, wantedLevel - 1)
    }

    fun update(deltaTime: Float, playerPosition: Vector2) {
        units.forEach { unit ->
            unit.alertLevel -= 0.1f * deltaTime
            if (unit.alertLevel < 0) unit.alertLevel = 0f
        }
    }
}

// Gang Territory System
class GangTerritory(
    val id: Int,
    val centerX: Float,
    val centerY: Float,
    val radius: Float = 300f
) {
    var ownerGang = "neutral"
    var controlPoints = 0f // 0-100
    var members = mutableListOf<NPC>()
    var isUnderAttack = false

    fun contains(x: Float, y: Float): Boolean {
        val dx = x - centerX
        val dy = y - centerY
        return sqrt(dx * dx + dy * dy) < radius
    }

    fun update(deltaTime: Float) {
        // Territory control dynamics
        if (isUnderAttack) {
            controlPoints = maxOf(0f, controlPoints - 1f * deltaTime)
        } else {
            controlPoints = minOf(100f, controlPoints + 0.5f * deltaTime)
        }
    }

    fun draw(canvas: android.graphics.Canvas, paint: android.graphics.Paint, cameraX: Float, cameraY: Float) {
        val screenX = centerX - cameraX
        val screenY = centerY - cameraY

        paint.color = when (ownerGang) {
            "player" -> android.graphics.Color.BLUE
            "rival" -> android.graphics.Color.RED
            else -> android.graphics.Color.GRAY
        }
        paint.alpha = (100 + controlPoints * 1.5).toInt()

        canvas.drawCircle(screenX, screenY, radius, paint)

        // Control indicator
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 20f
        canvas.drawText("${"%.0f".format(controlPoints)}%", screenX - 20, screenY, paint)
    }
}

class GangSystem {
    private val territories = mutableListOf<GangTerritory>()
    val playerGang = Gang("Player", android.graphics.Color.BLUE)
    val rivalGang = Gang("Rivals", android.graphics.Color.RED)

    init {
        initializeTerritories()
    }

    private fun initializeTerritories() {
        repeat(5) { i ->
            territories.add(GangTerritory(
                i,
                (i % 3) * 1500f + 500f,
                (i / 3) * 1500f + 500f,
                300f
            ))
        }
    }

    fun conquerTerritory(territoryId: Int, playerPosition: Vector2): Boolean {
        val territory = territories.getOrNull(territoryId) ?: return false

        if (territory.contains(playerPosition.x, playerPosition.y)) {
            territory.ownerGang = "player"
            territory.controlPoints += 5f
            playerGang.reputation += 10

            if (territory.controlPoints >= 100f) {
                playerGang.territories++
                return true
            }
        }
        return false
    }

    fun getTerritoryAt(x: Float, y: Float): GangTerritory? {
        return territories.firstOrNull { it.contains(x, y) }
    }

    fun update(deltaTime: Float) {
        territories.forEach { it.update(deltaTime) }
    }

    fun draw(canvas: android.graphics.Canvas, paint: android.graphics.Paint, cameraX: Float, cameraY: Float) {
        territories.forEach { it.draw(canvas, paint, cameraX, cameraY) }
    }
}

// Gang Data
class Gang(
    val name: String,
    val color: Int
) {
    var reputation = 0
    var territories = 0
    var members = mutableListOf<NPC>()
    var treasury = 0

    fun addReputation(amount: Int) {
        reputation += amount
    }

    fun addMoney(amount: Int) {
        treasury += amount
    }
}

// Extended Police & Wanted System
class WantedSystem(val policeStation: PoliceStation) {
    private var wantedTimer = 0f

    fun onCrimeCommitted(crime: String, severity: Int) {
        when (crime) {
            "assault" -> policeStation.increaseWanted()
            "robbery" -> repeat(2) { policeStation.increaseWanted() }
            "murder" -> repeat(3) { policeStation.increaseWanted() }
        }
    }

    fun update(deltaTime: Float, playerSafe: Boolean) {
        wantedTimer += deltaTime

        if (playerSafe && wantedTimer > 30f) {
            policeStation.decreaseWanted()
            wantedTimer = 0f
        }
    }

    fun getWantedText(): String {
        return when (policeStation.wantedLevel) {
            0 -> "Wanted: None"
            1 -> "Wanted: ★"
            2 -> "Wanted: ★★"
            3 -> "Wanted: ★★★"
            4 -> "Wanted: ★★★★"
            5 -> "Wanted: ★★★★★"
            else -> "Wanted: EXTREME"
        }
    }
}

// Enhanced Combat System
class WeaponInventory {
    private val weapons = mutableListOf<Weapon>()
    var selectedIndex = 0

    init {
        weapons.add(Weapon("Pistol", 20))
        weapons.add(Weapon("Rifle", 40))
        weapons.add(Weapon("Shotgun", 60))
    }

    fun getCurrentWeapon(): Weapon? = weapons.getOrNull(selectedIndex)

    fun switchWeapon(index: Int) {
        if (index in weapons.indices) {
            selectedIndex = index
        }
    }

    fun addAmmo(amount: Int) {
        getCurrentWeapon()?.ammo = (getCurrentWeapon()?.ammo ?: 0) + amount
    }

    fun getAllWeapons(): List<Weapon> = weapons.toList()
}

// Skill System
class SkillSystem {
    var drivingSkill = 0f // 0-100
    var shootingSkill = 0f
    var stealthSkill = 0f

    fun improveSkill(skill: String, amount: Float) {
        when (skill) {
            "driving" -> drivingSkill = minOf(100f, drivingSkill + amount)
            "shooting" -> shootingSkill = minOf(100f, shootingSkill + amount)
            "stealth" -> stealthSkill = minOf(100f, stealthSkill + amount)
        }
    }

    fun getMultiplier(skill: String): Float {
        return (1f + when (skill) {
            "driving" -> drivingSkill / 200f
            "shooting" -> shootingSkill / 200f
            "stealth" -> stealthSkill / 200f
            else -> 0f
        })
    }
}

// Extended Player Class
class ExtendedPlayer(basePlayer: Player) : Player() {
    var inventory = WeaponInventory()
    var skills = SkillSystem()
    var reputation = 0
    var wantedLevel = 0

    init {
        position = basePlayer.position
        cash = basePlayer.cash
        health = basePlayer.health
        weapons = inventory.getAllWeapons().toMutableList()
    }

    fun commit(crime: String) {
        when (crime) {
            "robbery" -> {
                cash += (100..500).random()
                wantedLevel += 2
            }
            "carjacking" -> {
                cash += (50..200).random()
                wantedLevel += 1
            }
        }
    }

    fun improveDriving(amount: Float = 1f) {
        skills.improveSkill("driving", amount)
    }

    fun improveShooting(amount: Float = 1f) {
        skills.improveSkill("shooting", amount)
    }
}
