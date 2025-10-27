# 🎮 Arkanoid Game - Java Edition

Game Arkanoid cổ điển được viết bằng Java với Swing.

## 📋 Yêu cầu hệ thống

- **Java JDK**: 8 trở lên
- **Maven**: 3.6+ (để build project)

## 🚀 Cách chạy game

### Lần đầu tiên sau khi clone:

1. **Build project:**
   ```bash
   mvn clean package -DskipTests
   ```

2. **Chạy game:**
   - **Windows**: Double click `start-game.bat`
   - **Hoặc dùng Maven**:
     ```bash
     java -jar target/arkanoid-game-1.0-SNAPSHOT.jar
     ```

### Lần sau (nếu đã build):

- Chỉ cần chạy `start-game.bat` (Windows)
- Script tự động kiểm tra JAR, nếu chưa có sẽ build

## 🎯 Tính năng

### Gameplay:
- ⚡ 5 levels với độ khó tăng dần
- 🎨 Level 4-5 có gạch tái sinh (biến mất 10s rồi xuất hiện lại)
- 🎮 Điều khiển: Mũi tên trái/phải hoặc chuột
- 🔫 PowerUps: Laser, Enlarge, Catch, Mega Ball, Twin Ball, v.v.

### UI:
- 🎹 Điều khiển menu bằng phím mũi tên + Enter
- 🔊 Điều chỉnh âm lượng trong menu
- 🏆 Bảng xếp hạng high score
- 💾 Save/Resume game

### Đặc biệt:
- 🟪 Gạch tái sinh (màu hồng): Biến mất 10s khi bị đánh
- 🟠 Bóng cam: PowerUp Incandescent - xuyên thấu gạch
- 🎨 Sprite chính thức: Ball, Paddle, Bricks

## ⌨️ Điều khiển

**Menu:**
- `↑/↓`: Chọn menu
- `Enter`: Xác nhận
- `ESC`: Thoát

**Trong game:**
- `←/→` hoặc `Chuột`: Di chuyển paddle
- `Space`: Bắt đầu/Thả bóng
- `Space` (khi có Laser): Bắn laser
- `ESC`: Pause game

## 📁 Cấu trúc project

```
Arkanoid/
├── src/main/java/com/arkanoid/
│   ├── game/
│   │   ├── entity/      # Ball, Brick, Paddle, PowerUp
│   │   ├── game/        # GamePanel, Level, Updater, Renderer
│   │   └── ui/          # Menu, Dialog, HighScore
│   └── util/            # FontManager, HighScoreManager
├── src/main/resources/
│   ├── Image/           # Sprites
│   ├── Sound/           # Âm thanh
│   └── font/            # Custom font
├── start-game.bat       # Script chạy game (Windows)
├── rebuild-game.bat     # Script build lại
└── pom.xml              # Maven config
```

## 🔧 Development

**Build lại khi sửa code:**
```bash
mvn clean package -DskipTests
```

**Hoặc dùng script:**
```bash
rebuild-game.bat
```

**Chạy test:**
```bash
mvn test
```

## 🎨 Credits

- Game gốc: Taito Corporation
- Font: ByteBounce
- Sprites: Arkanoid assets

## 📝 License

Educational project - Free to use and modify

---

**Chúc chơi vui vẻ!** 🎮✨
