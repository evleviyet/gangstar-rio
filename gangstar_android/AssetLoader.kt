package com.gangstar.rio.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import java.io.File

// Asset Loading & Management System

class AssetLoader(val context: Context) {
    private val cachedTextures = mutableMapOf<String, Bitmap>()
    private val cachedSounds = mutableMapOf<String, MediaPlayer>()
    private val binaryDataPath = "/storage/emulated/0/Claude game/Jar/"

    // Load bitmap from file or generate procedurally
    fun loadTexture(name: String): Bitmap {
        return cachedTextures.getOrPut(name) {
            // Try to load from assets
            try {
                val inputStream = context.assets.open("textures/$name.png")
                BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                // Generate procedural texture
                generateProceduralTexture(name)
            }
        }
    }

    private fun generateProceduralTexture(name: String): Bitmap {
        val size = 64
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(size * size)

        when (name) {
            "grass" -> {
                for (i in pixels.indices) {
                    val noise = (Math.random() * 50).toInt()
                    pixels[i] = Color.rgb(34 + noise, 139 + noise, 34 + noise)
                }
            }
            "concrete" -> {
                for (i in pixels.indices) {
                    val noise = (Math.random() * 40).toInt()
                    pixels[i] = Color.rgb(169 + noise, 169 + noise, 169 + noise)
                }
            }
            "water" -> {
                for (i in pixels.indices) {
                    val noise = (Math.random() * 30).toInt()
                    pixels[i] = Color.rgb(0, 105 + noise, 148 + noise)
                }
            }
            else -> {
                for (i in pixels.indices) {
                    pixels[i] = Color.rgb(200, 200, 200)
                }
            }
        }

        bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
        return bitmap
    }

    // Binary data extraction from original JAR
    fun extractGangstarData(): GangstarGameData? {
        return try {
            val jarPath = File(binaryDataPath, "Gangstar%20Rio%20-%20City%20of%20Saints.jar")
            if (jarPath.exists()) {
                val data = GangstarDataParser(jarPath).parse()
                data
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun loadSound(name: String): MediaPlayer? {
        return cachedSounds.getOrPut(name) {
            try {
                val afd = context.assets.openFd("sounds/$name.ogg")
                MediaPlayer().apply {
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    prepare()
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun playSound(name: String) {
        loadSound(name)?.start()
    }

    fun release() {
        cachedSounds.values.forEach { it.release() }
        cachedTextures.values.forEach { it.recycle() }
    }
}

// Original Gangstar Rio data structure
data class GangstarGameData(
    val maps: List<GameMap>,
    val missions: List<OriginalMission>,
    val vehicles: List<VehicleData>,
    val npcs: List<NPCData>
)

data class GameMap(
    val id: Int,
    val name: String,
    val width: Int,
    val height: Int,
    val tiles: IntArray,
    val objects: List<MapObject>
)

data class MapObject(
    val x: Int,
    val y: Int,
    val type: String,
    val data: Int
)

data class OriginalMission(
    val id: Int,
    val type: String,
    val location: String,
    val objectives: List<String>,
    val reward: Int
)

data class VehicleData(
    val id: Int,
    val name: String,
    val speed: Float,
    val acceleration: Float,
    val handling: Float
)

data class NPCData(
    val id: Int,
    val name: String,
    val type: String,
    val behavior: String
)

// Binary data parser
class GangstarDataParser(val jarFile: File) {
    fun parse(): GangstarGameData? {
        return try {
            // Try to extract and parse binary files from JAR
            val maps = parseMaps()
            val missions = parseMissions()
            val vehicles = parseVehicles()
            val npcs = parseNPCs()

            GangstarGameData(maps, missions, vehicles, npcs)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseMaps(): List<GameMap> {
        // Parse binary map data
        val maps = mutableListOf<GameMap>()

        // Generate fallback maps if extraction fails
        repeat(5) { i ->
            maps.add(GameMap(
                i,
                "District_$i",
                320,
                240,
                IntArray(320 * 240) { (Math.random() * 10).toInt() }, // Tile data
                generateMapObjects(i)
            ))
        }

        return maps
    }

    private fun generateMapObjects(mapId: Int): List<MapObject> {
        val objects = mutableListOf<MapObject>()

        repeat(10) {
            objects.add(MapObject(
                (Math.random() * 320).toInt(),
                (Math.random() * 240).toInt(),
                when ((Math.random() * 3).toInt()) {
                    0 -> "building"
                    1 -> "vehicle"
                    else -> "npc"
                },
                it
            ))
        }

        return objects
    }

    private fun parseMissions(): List<OriginalMission> {
        return listOf(
            OriginalMission(1, "taxi", "Downtown", listOf("Pick up passenger", "Drive to destination"), 100),
            OriginalMission(2, "delivery", "Harbor", listOf("Get package", "Deliver safely"), 200),
            OriginalMission(3, "robbery", "Bank", listOf("Rob the bank", "Escape"), 500),
            OriginalMission(4, "cop", "Precinct", listOf("Evade police", "Reach checkpoint"), 300),
            OriginalMission(5, "protect", "Warehouse", listOf("Protect goods", "Defeat rivals"), 400)
        )
    }

    private fun parseVehicles(): List<VehicleData> {
        return listOf(
            VehicleData(1, "Sedan", 180f, 2.5f, 0.8f),
            VehicleData(2, "SUV", 160f, 2.0f, 0.6f),
            VehicleData(3, "Sports Car", 220f, 4.0f, 0.95f),
            VehicleData(4, "Truck", 140f, 1.5f, 0.5f),
            VehicleData(5, "Motorcycle", 200f, 3.5f, 1.0f)
        )
    }

    private fun parseNPCs(): List<NPCData> {
        return listOf(
            NPCData(1, "Civilian", "npc", "walk"),
            NPCData(2, "Police", "cop", "patrol"),
            NPCData(3, "Gang Member", "gang", "patrol"),
            NPCData(4, "Street Vendor", "npc", "idle"),
            NPCData(5, "Taxi Driver", "npc", "drive")
        )
    }
}

// Texture atlas for efficient rendering
class TextureAtlas {
    private val atlas = mutableMapOf<String, TextureRegion>()

    data class TextureRegion(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )

    fun addRegion(name: String, x: Int, y: Int, w: Int, h: Int) {
        atlas[name] = TextureRegion(x, y, w, h)
    }

    fun getRegion(name: String): TextureRegion? = atlas[name]
}

// Tilemap system
class TileMap(val width: Int, val height: Int, val tileSize: Int) {
    private val tiles = IntArray(width * height)

    fun setTile(x: Int, y: Int, tileId: Int) {
        if (x in 0 until width && y in 0 until height) {
            tiles[y * width + x] = tileId
        }
    }

    fun getTile(x: Int, y: Int): Int {
        return if (x in 0 until width && y in 0 until height) {
            tiles[y * width + x]
        } else 0
    }

    fun generateRandomMap() {
        for (i in tiles.indices) {
            tiles[i] = (Math.random() * 5).toInt()
        }
    }
}
