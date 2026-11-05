package com.gangstar.rio.android

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.KeyEvent
import kotlin.math.cos
import kotlin.math.sin

class GangstarActivity : Activity() {
    private lateinit var gameView: GameSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameView = GameSurfaceView(this)
        setContentView(gameView)
    }

    override fun onResume() {
        super.onResume()
        gameView.resumeGame()
    }

    override fun onPause() {
        super.onPause()
        gameView.pauseGame()
    }
}

class GameSurfaceView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {
    private var gameThread: Thread? = null
    private var isRunning = false
    private var lastFrameTime = 0L
    private var fps = 0
    private var frameCount = 0

    private val gameState = GameState()
    private val input = InputHandler()
    private val camera = Camera()
    private val paint = Paint().apply {
        isAntiAlias = true
        textSize = 40f
    }

    init {
        holder.addCallback(this)
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        initializeGame()
        isRunning = true
        gameThread = Thread(this)
        gameThread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRunning = false
        gameThread?.join()
    }

    fun pauseGame() {
        gameState.isPaused = true
    }

    fun resumeGame() {
        gameState.isPaused = false
    }

    private fun initializeGame() {
        // Initialize world with NPCs and missions
        val world = gameState.world

        // Spawn player vehicle
        val startVehicle = Vehicle("car").apply {
            position = Vector2(2500f, 2500f)
        }
        world.entities.add(startVehicle)

        // Spawn NPCs
        repeat(20) {
            val npc = NPC("civilian").apply {
                position = Vector2(
                    (Math.random() * 5000).toFloat(),
                    (Math.random() * 5000).toFloat()
                )
                destination = Vector2(
                    (Math.random() * 5000).toFloat(),
                    (Math.random() * 5000).toFloat()
                )
                taskType = "walk"
            }
            world.entities.add(npc)
        }

        // Create missions
        val missions = listOf(
            Mission(1, MissionType.TAXI, Vector2(500f, 500f), Vector2(1000f, 1000f), 120f, 100),
            Mission(2, MissionType.DELIVERY, Vector2(1500f, 1500f), Vector2(3000f, 3000f), 180f, 200),
            Mission(3, MissionType.COP, Vector2(2000f, 2000f), Vector2(4000f, 4000f), 300f, 500)
        )

        gameState.world.player.acceptMission(missions.first())
    }

    override fun run() {
        var lastTime = System.currentTimeMillis()

        while (isRunning) {
            val currentTime = System.currentTimeMillis()
            val deltaTime = (currentTime - lastTime) / 1000f
            lastTime = currentTime

            // Update game
            input.update()
            gameState.update(deltaTime)
            updateCamera()

            // Render
            val canvas = holder.lockCanvas()
            if (canvas != null) {
                renderGame(canvas, deltaTime)
                holder.unlockCanvasAndPost(canvas)
            }

            // FPS calculation
            frameCount++
            if (currentTime - lastFrameTime > 1000) {
                fps = frameCount
                frameCount = 0
                lastFrameTime = currentTime
            }
        }
    }

    private fun updateCamera() {
        val player = gameState.world.player
        camera.update(player.position.x, player.position.y)
    }

    private fun renderGame(canvas: Canvas, deltaTime: Float) {
        // Clear background
        canvas.drawColor(Color.parseColor("#1a1a2e"))

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        canvas.save()
        // Center camera on player
        canvas.translate(width / 2f, height / 2f)
        canvas.translate(-camera.x, -camera.y)

        // Draw grid
        drawGrid(canvas)

        // Draw game world
        drawWorld(canvas)

        canvas.restore()

        // Draw HUD
        drawHUD(canvas, width, height)
    }

    private fun drawGrid(canvas: Canvas) {
        val gridSize = 500f
        paint.color = Color.parseColor("#333333")
        paint.strokeWidth = 1f

        for (x in -5000f..5000f step gridSize) {
            canvas.drawLine(x, -5000f, x, 5000f, paint)
        }
        for (y in -5000f..5000f step gridSize) {
            canvas.drawLine(-5000f, y, 5000f, y, paint)
        }
    }

    private fun drawWorld(canvas: Canvas) {
        val world = gameState.world

        // Draw player
        paint.color = Color.BLUE
        val player = world.player
        canvas.drawCircle(player.position.x, player.position.y, 20f, paint)
        canvas.drawRect(
            player.position.x - 10f,
            player.position.y - 25f,
            player.position.x + 10f,
            player.position.y - 15f,
            paint
        )

        // Draw NPCs
        paint.color = Color.YELLOW
        world.entities.filterIsInstance<NPC>().forEach { npc ->
            canvas.drawCircle(npc.position.x, npc.position.y, npc.radius, paint)
        }

        // Draw vehicles
        paint.color = Color.RED
        world.entities.filterIsInstance<Vehicle>().forEach { vehicle ->
            drawVehicle(canvas, vehicle)
        }

        // Draw current mission target
        player.currentMission?.let { mission ->
            paint.color = Color.GREEN
            canvas.drawCircle(mission.targetLocation.x, mission.targetLocation.y, 30f, paint)
            paint.color = Color.WHITE
            paint.textSize = 24f
            canvas.drawText("TARGET", mission.targetLocation.x - 30f, mission.targetLocation.y, paint)
        }
    }

    private fun drawVehicle(canvas: Canvas, vehicle: Vehicle) {
        val length = 40f
        val width = 25f

        // Draw vehicle rectangle
        val cos = cos((vehicle.rotation.toDouble())).toFloat()
        val sin = sin((vehicle.rotation.toDouble())).toFloat()

        val corners = arrayOf(
            Vector2(length / 2, width / 2),
            Vector2(length / 2, -width / 2),
            Vector2(-length / 2, -width / 2),
            Vector2(-length / 2, width / 2)
        )

        val rotatedCorners = corners.map { corner ->
            Vector2(
                vehicle.position.x + corner.x * cos - corner.y * sin,
                vehicle.position.y + corner.x * sin + corner.y * cos
            )
        }

        for (i in 0..3) {
            val from = rotatedCorners[i]
            val to = rotatedCorners[(i + 1) % 4]
            canvas.drawLine(from.x, from.y, to.x, to.y, paint)
        }
    }

    private fun drawHUD(canvas: Canvas, width: Float, height: Float) {
        paint.color = Color.WHITE
        paint.textSize = 32f

        val player = gameState.world.player
        var yPos = 50f

        canvas.drawText("Cash: \$${player.cash}", 20f, yPos, paint)
        yPos += 40f
        canvas.drawText("Health: ${player.health}", 20f, yPos, paint)
        yPos += 40f
        canvas.drawText("FPS: $fps", 20f, yPos, paint)
        yPos += 40f

        player.currentMission?.let { mission ->
            canvas.drawText("Mission: ${mission.type}", 20f, yPos, paint)
            yPos += 40f
            canvas.drawText("Time: ${String.format("%.1f", mission.timeLimit - mission.elapsedTime)}s", 20f, yPos, paint)
        }

        // Distance to objective
        player.currentMission?.let { mission ->
            val distance = player.position.distance(mission.targetLocation)
            canvas.drawText("Distance: ${distance.toInt()}m", 20f, height - 50f, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val centerX = width / 2f
                val centerY = height / 2f
                val deltaX = event.x - centerX
                val deltaY = event.y - centerY

                input.moveX = deltaX / (width / 2f)
                input.moveY = deltaY / (height / 2f)
            }
            MotionEvent.ACTION_UP -> {
                input.moveX = 0f
                input.moveY = 0f
            }
        }
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> input.moveY = -1f
            KeyEvent.KEYCODE_DPAD_DOWN -> input.moveY = 1f
            KeyEvent.KEYCODE_DPAD_LEFT -> input.moveX = -1f
            KeyEvent.KEYCODE_DPAD_RIGHT -> input.moveX = 1f
            KeyEvent.KEYCODE_SPACE -> input.isFiring = true
        }
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> input.moveY = 0f
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT -> input.moveX = 0f
            KeyEvent.KEYCODE_SPACE -> input.isFiring = false
        }
        return true
    }
}
