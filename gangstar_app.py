"""
GANGSTAR RIO - Python Flet App
Buildable to APK with Flet
"""

import flet as ft
import random
import threading
import time

class GangstarRioGame:
    def __init__(self, page: ft.Page):
        self.page = page
        self.page.title = "Gangstar Rio"
        self.page.theme_mode = ft.ThemeMode.DARK

        # Game state
        self.cash = 0
        self.health = 100
        self.wanted = 0
        self.score = 0
        self.position = (2500, 2500)
        self.game_running = True

        # UI Elements
        self.cash_text = ft.Text(f"💰 Cash: ${self.cash}", size=20, color=ft.colors.GREEN)
        self.health_text = ft.Text(f"❤️ Health: {self.health}", size=20, color=ft.colors.RED)
        self.wanted_text = ft.Text(f"⭐ Wanted: {self.wanted}", size=20, color=ft.colors.ORANGE)
        self.score_text = ft.Text(f"📈 Score: {self.score}", size=20, color=ft.colors.BLUE)
        self.position_text = ft.Text(f"📍 Position: {self.position}", size=18)
        self.status_text = ft.Text("Status: Waiting for commands...", size=16, color=ft.colors.CYAN)
        self.log_text = ft.Text("Game Log:\n", size=14, color=ft.colors.WHITE70)

        self.setup_ui()
        self.start_game_loop()

    def setup_ui(self):
        """Create UI layout"""

        # Stats panel
        stats = ft.Column([
            ft.Text("🎮 GANGSTAR RIO", size=32, weight="bold", color=ft.colors.RED),
            ft.Divider(),
            self.cash_text,
            self.health_text,
            self.wanted_text,
            self.score_text,
            self.position_text,
            ft.Divider(),
            self.status_text,
        ])

        # Action buttons
        buttons = ft.Row([
            ft.ElevatedButton(
                "🚶 Walk",
                on_click=self.on_walk,
                width=100,
                bgcolor=ft.colors.BLUE,
            ),
            ft.ElevatedButton(
                "🎯 Mission",
                on_click=self.on_mission,
                width=100,
                bgcolor=ft.colors.GREEN,
            ),
            ft.ElevatedButton(
                "🚗 Vehicle",
                on_click=self.on_vehicle,
                width=100,
                bgcolor=ft.colors.AMBER,
            ),
            ft.ElevatedButton(
                "💥 Combat",
                on_click=self.on_combat,
                width=100,
                bgcolor=ft.colors.RED,
            ),
        ], wrap=True)

        # Game log
        log_container = ft.Container(
            self.log_text,
            bgcolor=ft.colors.BLACK26,
            padding=10,
            border_radius=10,
            height=200,
        )

        # Main layout
        main_column = ft.Column([
            stats,
            ft.Divider(),
            buttons,
            ft.Divider(),
            ft.Text("Action Log:", size=16, weight="bold"),
            log_container,
        ], scroll="auto")

        self.page.add(main_column)

    def log(self, message: str):
        """Add message to game log"""
        self.log_text.value += f"{message}\n"
        self.page.update()

    def update_stats(self):
        """Update UI stats"""
        self.cash_text.value = f"💰 Cash: ${self.cash}"
        self.health_text.value = f"❤️ Health: {self.health}"
        self.wanted_text.value = f"⭐ Wanted: {'★' * self.wanted}"
        self.score_text.value = f"📈 Score: {self.score}"
        self.position_text.value = f"📍 Position: {self.position}"
        self.page.update()

    def on_walk(self, e):
        """Walk action"""
        dx = random.randint(-500, 500)
        dy = random.randint(-500, 500)
        self.position = (self.position[0] + dx, self.position[1] + dy)
        self.score += 10
        self.log(f"✓ Walked to {self.position}")
        self.status_text.value = f"Status: Walking to {self.position}"
        self.update_stats()

    def on_mission(self, e):
        """Mission action"""
        missions = ["🚕 Taxi", "📦 Delivery", "🚔 Cop Chase", "💰 Robbery"]
        mission = random.choice(missions)
        reward = random.randint(100, 500)

        self.log(f"🎯 Mission: {mission} - Reward: ${reward}")
        self.status_text.value = f"Status: On mission {mission}"

        if random.random() > 0.3:
            self.cash += reward
            self.score += 50
            self.log(f"✓ MISSION COMPLETE! +${reward}")
        else:
            self.wanted = min(5, self.wanted + 1)
            self.log(f"✗ Mission Failed! Wanted: +1")

        self.update_stats()

    def on_vehicle(self, e):
        """Vehicle action"""
        self.log(f"🚗 Found vehicle nearby")
        self.log(f"   Vehicle: Car | Speed: 180km/h | Health: 200")
        self.score += 30
        self.log(f"✓ Vehicle entered!")
        self.status_text.value = "Status: Driving vehicle"
        self.update_stats()

    def on_combat(self, e):
        """Combat action"""
        if self.wanted > 0:
            damage = random.randint(10, 30)
            self.log(f"💥 COMBAT ENGAGED!")
            self.log(f"   Fired weapon - Damage: {damage}HP")
            self.score += 25

            if random.random() < 0.2:
                self.health -= random.randint(5, 15)
                self.log(f"   ❤️ Hit by enemy! Health: {self.health}")
        else:
            self.log(f"⚠️ No enemies in area")
            self.status_text.value = "Status: No combat available"

        self.update_stats()

    def start_game_loop(self):
        """Game loop in background"""
        def game_loop():
            while self.game_running:
                # Random events
                if random.random() < 0.1 and self.wanted > 0:
                    self.wanted = max(0, self.wanted - 1)
                    self.log(f"⭐ Evaded police - Wanted: {self.wanted}")
                    self.update_stats()

                time.sleep(2)

        thread = threading.Thread(target=game_loop, daemon=True)
        thread.start()

def main(page: ft.Page):
    """Main app entry"""
    game = GangstarRioGame(page)

if __name__ == "__main__":
    ft.app(target=main)
