# Bomber Quest

## Table of Contents
1. Introduction
2. Project Structure

    2.1 Code Organization & Class Hierarchy 
    
    2.2 Classes Overview 
3. Running the Game 
   
    3.1 Prerequisites 
   
    3.2 Running Instructions 
4. Using the Game 
   
    4.1 Controls 

    4.2 Game Mechanics 

    4.3 HUD Elements 
5. Map File Format

    5.1 File Structure 

    5.2 Custom Map Creation
6. Game Rules 
7. Advanced Features 
8. Assets and Resources 
9. Credits 

## 1. Introduction 
**Bomber Quest** is a fun and engaging 2D top-down game that takes inspiration from the Bomberman video game series. 
The game challenges players to navigate through mazes filled with enemies, wall-obstacles and hidden power-ups. To win, 
players must defeat all enemies and destroy destructible walls to uncover the hidden exit, both with the strategic
use of bombs. 
 
Players can place these bombs strategically to destroy walls, reveal hidden power-ups and 
eliminate enemies. They can collect various power-ups that enhance their abilities, such as increasing the bomb blast radius 
or allowing them to place multiple bombs simultaneously. A countdown timer adds urgency, 
making the gameplay immersive and pushing players to think and act quickly in order to succeed.

With its combination of strategic gameplay, varied challenges, and retro-inspired visuals, Bomber Quest 
offers an addictive experience for fans of the classic Bomberman game series. 

## 2. Project Structure
### 2.1 Code Organization & Class Hierarchy 
The game is implemented using the video game development library libGDX and follows an object-oriented structure. 
The code is modular and follows a well-defined class inheritance structure.

***Key Hierarchies and Relationships:**

*Main Class*
* BomberQuestGame

*Game object classes*
* GameObject (Superclass, all objects in the game are inheriting from it)
  + Characters (Superclass)
    + Player 
    + Enemy 
  + MapPoints (Superclass)
    + Entrance 
    + Exit 
  + Power-ups (Superclass)
    * BlastRadiusPowerUp
    * ConcurrentBombPowerUp 
    * Freeze 
    * Invincible 
  * Wall (Superclass)
    * Destructible Wall
    * Indestructible Wall
  * Bomb 
  * Blast 
  * Flowers
####
*Gameplay-related classes*
* GameMap 
* GameTimer 
* ContactListenerGame 
* Tile 
####
*Screen-related classes*
* BaseScreen (Superclass, other screens are inheriting from BaseScreen)
  * MenuScreen
  * GameScreen
  * VictoryScreen
  * GameOverScreen
* HUD
####
*Visual-related classes*
* Animations 
* Drawable (Interface, all game objects are extending it)
* SpriteSheet (Enum)
* Textures
####
*Audio-related classes*
* Audio 
  * SoundEffect (Enum)
  * MusicTrack (Enum)


### 2.2 Classes Overview
Each class is documented with JavaDoc and additional comments for a clear understanding of the code. 
Below you can find a brief summary of the classes: 


#### BomberQuestGame
The `BomberQuestGame` class is the core class of the Bomber Quest game. The class manages the lifecycle of the game, game states, 
and resources. It controls transitions between screens, such as the main menu, gameplay and victory screen, and ensures efficient 
disposal of resources. The class supports single and multiplayer mode, different difficulty levels and provides methods to load 
a specific game map or the default game map. On top, methods for key features like audio management, pause and resume functionality, and a 
timer for tracking the gameplay duration, are implemented in this class. Overall, it provides a foundation for the game’s functionality 
and interactions.


#### GameObject
The `GameObject` class serves as the superclass for all entities on the game map. It defines a fixed position using x and y 
coordinates, which represent the object’s location within the game world. This class is designed to be extended by specific 
game object types, such as walls, power-ups and players, providing a consistent foundation for all game objects.


#### Character
The `Character` class serves as the abstract foundation for all characters in the game (players and enemies). It
defines shared behaviors, such as tracking death states, animations and positions through a Box2D hitbox. Subclasses
must implement their unique hitbox and visual appearance. Its structure provides a consistent framework for handling characters while
allowing customization in their specific classes.

#### Player
The `Player` class represents a controllable character in the game and manages key gameplay elements for both single and multiplayer mode. 
Players can move, place bombs and use power-ups. 
The class manages the players interactions with walls, enemies, and power-ups, while dynamically
updating the player’s state each tick. Power-ups like bomb limit, blast radius, freeze and invincibility are
tracked and can be activated within this class. Movement and actions are responsive to user input, supporting dynamic gameplay. This class
is necessary for a player's interaction with the game world.

#### Enemy
The `Enemy` class represents opponents in the game. Their behaviors vary based on the chosen difficulty: Enemies can
move randomly (Easy), chase players by using pathfinding (Medium & Hard), and place bombs (Hard). They can become frozen (stop their movement completely), 
whenever the player activates a freeze power-up. The enemies dynamically adjust their movement direction and speed depending on obstacles and
the closeness to players. The class ensures that enemies are interactive and adaptable, making them a challenging element in the game.


#### MapPoints
The `MapPoints` class is an abstract class for key points on the game map, such as entrances and exits. It defines their
position and common behaviors, ensuring consistency among these objects. Each MapPoint is static and may interact with
the player or the game environment. Subclasses must implement the interact method to define specific behaviors.

#### Entrance
The `Entrance` class represents the starting point for a player on the game map. It is a static object with a fixed texture that
marks the player's initial starting position.

#### Exit
The `Exit` class represents the game's exit point, which can be locked or unlocked based on the game state. The exit plays
a key role in winning the game and changes its appearance depending on its state (locked or unlocked). It features a victory
sequence with music and animations when the player interacts with it. If the exit is locked, it remains inaccessible until all enemies are
defeated. 


#### PowerUps
The `PowerUps` class is an abstract base for all collectible items in the game that provide players with specific abilities
or benefits. Each power-up has a position on the game map and can apply an effect when collected. The class provides a standard
structure for creating power-ups, including methods for retrieving coordinates and applying effects. Subclasses must implement
their specific logic for interacting with players.

#### BlastRadiusPowerUp
The `BlastRadiusPowerUp` increases the blast radius of the player's bombs. When collected, it increments the radius by one,
up to a maximum of eight. The power-up also plays a sound effect when collected, giving immediate feedback to the player.

#### ConcurrentBombPowerUp
The `ConcurrentBombPowerUp`increases the player’s capacity to place multiple bombs at once, adding one to the limit 
with each power-up collected, up to a maximum of eight bombs. It also triggers a sound effect when it is collected.

#### Freeze
The `Freeze` class represents a power-up that temporarily stops enemy movement when it is activated. 
The pre-defined duration is fifteen seconds. The effect can only be triggered once per collected power-up by pressing “Shift”. This
power-up introduces a defensive option for dealing with challenging enemy situations. It spawns twice on the game map, 
hidden behind a destructible wall.

#### Invincible
The `Invincible` class represents a power-up that temporarily provides players with immunity to any damage when it is activated. 
The power-up allows players to navigate hazards or enemies unharmed.
The pre-defined duration is fifteen seconds. The effect can only be triggered once per collected power-up by pressing “Enter”. 
This power-up is essential for adding moments of high-risk, high-reward gameplay. It spawns twice on the game map,
hidden behind a destructible wall.


#### Wall
The `Wall` class is an abstract base class representing immovable objects on the game map. It provides the structure for
creating wall objects, including their physical representation using Box2D bodies and collision properties. Walls serve
as obstacles or boundaries in the game and are defined by their position and hitbox. The class ensures that each of 
the two types of walls (destructible and indestructible walls) has a visual representation through an abstract method, 
which must be implemented by the subclasses.

#### DestructibleWall
The `DestructibleWall` class extends the abstract Wall class, representing walls that can be destroyed by bomb explosions. 
These walls add dynamic gameplay elements by potentially hiding power-ups or the map’s exit beneath them, 
rewarding strategic bomb placement. The class overrides the visual representation method to use a specific texture for 
destructible walls, ensuring they are visually distinct. Also, it includes functionality to efficiently remove 
its Box2D body from the game world upon destruction, freeing up resources and ensuring smooth gameplay. 

#### IndestructibleWall
The `IndestructibleWall` class, a subclass of the abstract Wall class, represents permanent, unbreakable obstacles in the game. 
These walls define the static and fixed boundaries of the map, providing structure and challenging the player’s navigation. 
The class overrides the visual representation method to use a unique texture, making it visually distinct.


#### Blast
The `Blast` class represents the explosion effect triggered by a bomb. It propagates within the bomb’s radius, interacting
with the game environment by destroying destructible walls and eliminating enemies. Featuring 
dynamic animations and timed removal, the class ensures visually engaging explosions while maintaining efficient resource 
management. Its behavior reinforces immersive gameplay interactions.

#### Bomb
The `Bomb` class represents an explosive object placed by a player or an enemy within the game. 
It includes a countdown mechanism, exploding after the set duration of 3 seconds. Upon detonation, 
the bomb generates a blast that propagates outward in all four cardinal directions, interacting with the game environment 
(walls, enemies, and power-ups). The class supports animations during the bomb’s active state, as well as explosion effects, 
including sounds and visuals, for an immersive experience. The blast radius is adjustable, capped at a maximum of eight tiles. 
Additionally, the bomb’s timer can be paused and resumed, aligning its behavior with game state dynamics (e.g. if game is in paused-state).

#### Flowers
The `Flowers` class represents decorative elements on the game map. These static objects have no hitbox or interactions 
and are purely aesthetic, serving as floor decorations to enhance the visual appeal of the game environment.


#### GameMap
The `GameMap` class manages the game's map, including its objects, physics simulation and game logic. It initializes the map 
based on a provided .properties file and dynamically creates objects such as players, enemies, power-ups and walls. It also handles game 
state updates, like tracking and removing bombs, blasts and defeated enemies. Additionally, the class ensures smooth physics and rendering. 
The class supports the multiplayer mode by creating multiple entrances and by managing multiple players. 
Additionally, it coordinates game mechanics like power-up collection, enemy freezing and determining if players have reached the 
exit. Overall, it acts as a central hub for map-related functionality in the game.

#### GameTimer
The `GameTimer` class tracks the remaining time for the game and manages its pause. It updates the timer each frame when not paused, 
ensuring time-sensitive events are accurately managed. The class supports the pausing and resuming of the timer. It provides methods 
to reset the timer and query the remaining time, always ensuring the value is non-negative.

#### ContactListenerGame
The `ContactListenerGame` class manages collisions between different game objects, such as players, enemies, blasts and walls.
It processes events like a player colliding with an enemy or blast, triggering the game-over logic (only if the player has not currently activated the
invincible power-up). Destructible walls are removed when they are hit by blasts, while a collision between an enemy and a blast
triggers the death of the enemy, unless the blast originated from an enemy bomb. The class also disables certain physical responses,
like kickbacks during collisions, for smoother gameplay. By centralizing collision handling, it ensures consistent and efficient
interactions between game objects.

#### Tile
The `Tile` class is a utility for the A* pathfinding algorithm, used primarily in the Enemy class for navigation. 
Each tile represents a position on the game map and includes essential data for pathfinding, such as coordinates, costs,
and references to previous tiles in the path. It tracks the cost from the start tile, the estimated 
cost to the target, and the total cost. This class facilitates efficient pathfinding by 
enabling step-by-step navigation and path reconstruction.


#### BaseScreen 
The `BaseScreen` class serves as a foundational superclass for all game screens, providing shared functionality to 
streamline screen management. It includes essential features such as camera setup, viewport handling, and resource cleanup, 
offering a consistent framework for extending screens like a specific menu and game over screen. The class provides 
default methods for rendering, resizing and handling visibility changes, ensuring uniform behavior across different 
screens. The class simplifies the development of new screens and maintains a cohesive structure throughout the game.

#### GameOverScreen
The `GameOverScreen` class represents the screen when the player loses the game. It provides options to retry the current
map, load a new game, or return to the main menu. Featuring a “Game Over” message and responsive navigation buttons, 
the screen integrates sound effects and background music for an immersive experience. It ensures smooth transitions by 
updating HUD elements and synchronizing game states.

#### GameScreen 
The `GameScreen` class manages the primary gameplay screen, handling rendering, game logic, and user interaction. It 
incorporates essential gameplay elements like the map, HUD, and camera control. The screen ensures smooth gameplay by 
updating map states, processing user input, and rendering game elements, including animations and critical visual effects 
such as a red frame when the timer is low. Camera behavior adapts to player movements or switches to a full-map view in 
multiplayer mode. The class also supports seamless transitions through pause, resizing, and HUD updates, maintaining a 
responsive and immersive gaming experience.

#### MenuScreen 
The `MenuScreen` class represents the main menu screen, providing options for navigating the game. 
It extends the BaseScreen class and manages UI components, including buttons for starting a game, continuing a paused 
game, loading a new map, enabling multiplayer, adjusting difficulty, resetting high scores, toggling sound, and exiting 
the game. The menu features a visually centered background image and dynamically adjusts its layout based on screen size. 
It also integrates background music and supports smooth transitions to other screens, ensuring an intuitive and immersive 
user experience.

#### VictoryScreen 
The `VictoryScreen` class represents the screen that is displayed when a player wins the game. It showcases the player’s 
remaining time, compares it against saved high scores, and updates the high score if surpassed. The screen provides 
options to return to the main menu and features celebratory elements like victory music and sound effects for new high 
scores. High scores are stored locally using preferences, with methods to reset or validate scores against a maximum 
allowed time. The screen ensures a smooth and engaging post-victory experience while managing resources efficiently.

#### HUD 
The `Hud` class manages the in-game Heads-Up Display (HUD), providing vital information such as the number of remaining enemies, 
power-up and exit states, and the game timer. It utilizes a dedicated camera to ensure the HUD remains static and unaffected by 
gameplay movements. The HUD dynamically updates each frame, reflecting changes like collected power-ups, exit lock status,
and enemy count. Its layout and font scale adapt to various screen sizes for a consistent visual experience. The HUD also
includes a secondary display (top right) for special power-ups (Invincible and Freeze), showing collected counts and an additional 
red timer during activation.

#### Drawable 
The `Drawable` interface establishes a standard for objects that can be rendered on the screen. It defines methods to 
retrieve an object’s current visual representation (TextureRegion) and its position within the game world grid. 
This abstraction ensures that game elements, such as characters, bombs, and power-ups, can be treated consistently during rendering. 
By unifying the rendering logic, the interface simplifies drawing operations and enhances flexibility in handling visual elements.

#### SpriteSheet
The `SpriteSheet` class manages and provides access to the game’s sprite sheets, which are grids of images used for textures 
and animations. Each sprite sheet is associated with a file and a grid size (e.g., 16x16 pixels). 
A helper method allows retrieval of specific images using grid-based coordinates. This class streamlines the organization 
and use of graphical assets.

#### Textures 
The `Textures` class centralizes all static image references used in Bomber Quest, such as walls, entrances, exits, 
and power-ups. Each texture is defined as a constant, derived from specific coordinates in a SpriteSheet. This approach 
prevents redundant loading, improves performance, and ensures consistency across the game. By organizing textures in a 
single location, the class simplifies reuse, enhances code readability, and supports maintainable asset management.

#### Animations
The `Animations` class centralizes all animated sequences, covering key elements like player movement, enemy actions,
bomb explosions, and environmental effects. By defining animations as constants with frame sequences sourced from
sprite sheets, the class ensures efficient reuse and maintainability. Each animation includes timing parameters for consistent playback.


#### MusicTrack 
The `MusicTrack` class manages background music for different parts of the game, including the menu, gameplay, victory, 
and game-over screens. Each track is represented as an enum constant, loaded once for efficiency, and assigned a 
default volume. Tracks are set to loop by default and can be played or stopped as needed. This centralized approach 
simplifies the integration of music with game events, ensures consistent audio levels, and allows smooth transitions between tracks.

#### SoundEffect 
The `SoundEffect` class centralizes the management of sound effects in the game, such as explosions, power-up collection,
and victory sounds. Each sound is defined as an enum constant, loaded once for efficiency, and associated with a predefined volume. 
Sound effects can overlap when played and be stopped when necessary. A global default volume modifier allows for consistent 
adjustment of all sound effects. By consolidating sound management, the class enhances audio efficiency and provides 
flexibility for dynamic in-game events.


## 3. Running the Game 
### 3.1 Prerequisites 
* Java 8 or higher
* libGDX framework
* Gradle build system


### 3.2 Running Instructions 
1. Clone the repository: git clone <repository-url>
2. Open the project in IntelliJ IDEA or any Gradle-compatible IDE.
3. Configure the appropriate run configuration based on your OS.
4. Launch the game via the Play Button on the top right in the IntelliJ bar.

## 4. Using the Game 
### 4.1 Controls 
* **Arrow Keys:** Move the player. 
* **Space:** Place a bomb. 
* **Enter:** Activate the invincibility power-up (if collected). 
* **Shift:** Activate the freeze power-up (if collected). 
* **Escape:** Open the game menu. 
* **Multiplayer:**
  * **W/A/S/D:** Move the second player. 
  * **F:** Place a bomb with the second player. 

### 4.2 Game Mechanics 
* **Player Interaction**: Players navigate the map, avoid enemies, and strategically place bombs.
* **Bombs**: Bombs explode after 3 seconds, destroying destructible walls and defeating enemies.
* **Power-Ups**: Enhance player abilities (e.g. increase bomb radius or allow multiple bomb placements).
* **Enemies**: Intelligent movement with pathfinding and bomb placement on higher difficulties. On easy difficulty, the enemies move randomly.

### 4.3 HUD 
#### Main HUD
* Bomb Blast Radius
* Concurrent Bomb Limit
* Countdown Timer
* Remaining Enemies
* Exit Unlocked Indicator
#### Second HUD (Top Right Corner)
- Collected Invincible Power-Ups
- Collected Freeze Power-Ups
- Timer for Special Power-Ups

## 5. Map File Format

### 5.1 File Structure 
Maps are defined in `.properties` files. Each line specifies a coordinate and the object type: `X,Y=Value`
* **0**: Indestructible Wall
* **1**: Destructible Wall
* **2**: Entrance
* **3**: Enemy
* **4**: Exit
* **5**: Concurrent bomb power-up (hidden under a destructible wall)
* **6**: Blast radius power-up (hidden under a destructible wall)

### 5.2 Custom Map Creation
**Load an existing map**
1. Click on **"Load New Map"** on the menu screen. 
2. Your local file chooser will open. 
3. Select the map you want to play.

**Create your own map**
1. Create a `.properties` file.
2. Define coordinates and objects using the format mentioned above. 
3. Save the file and load it in-game via the menu button "Load Map".


## 6. Game Rules
1. Defeat all enemies to unlock the exit. 
2. Destroy destructible walls to reveal power-ups and the exit. 
3. Avoid enemy contact and contact with blasts. 
4. Complete the level before the timer runs out. The timer is set to four minutes. 


## 7. Advanced Features 
* **Intelligent Enemy Movement**: Enemies use A* pathfinding to chase the player when the game difficulty is set to either "Medium"
or "Hard" in the game menu. This makes gameplay more exciting and challenging since the enemies actively pursue the player 
rather than moving randomly. They also have a detection radius of 5 tiles, meaning they only start chasing the player when 
close enough. This adds an element of strategy as the player needs to carefully plan the moves to avoid getting caught, as the 
enemies will take the shortest path to reach the player. 
On “Easy” mode, enemies still walk randomly, which keeps things simple and less stressful. But for higher difficulties, it made sense 
to make enemies smarter to match the challenge level. With A* pathfinding, enemies feel more responsive and dynamic, creating a 
sense of tension and urgency.

* **Enemies That Place Bombs**: On hard difficulty, every two seconds a random enemy drops a bomb. This adds an exciting 
twist to the game as players have to navigate carefully to avoid not only their own bombs but also the bombs placed by their opponents. 
Accordingly, this creates a more intense and tactical gaming experience.


* **Dynamic Difficulty**: The user can pick how tough the game is! Players can tailor the game’s challenge to their skill 
level by selecting from three difficulty options: Easy, Medium, and Hard. Each difficulty level adjusts enemy behavior 
and gameplay features to provide an experience suited for both beginners and more experienced players. The difficulty can 
be chosen directly on the menu screen, ensuring that everyone can enjoy a customized and engaging gameplay experience.


* **Multiplayer Mode**: Play with a friend! This mode allows two players to play on the same keyboard, making it a fun and 
social experience. One player controls their character using the WASD keys, while the other one uses the arrow keys. 
The entire map is visible on the screen during multiplayer mode, ensuring both players can strategize and interact with each 
other effectively. You can activate the multiplayer mode easily by selecting the option on the menu screen.


* **Additional Power-Ups**: By adding new power-ups, players are given interesting and powerful tools to overcome challenges, 
which can be especially helpful when choosing difficulty "Medium" or "Hard".
  * **Freeze**: With this power-up the user can stop all enemies temporarily — perfect for getting out of tough game situations. 
  Bombs originated from enemies have a blast radius of 2 tiles to make the challenge even more difficult. 
  The power-up can be activated by pressing "Shift". Players can use the freeze-time to reposition, gather resources, or 
  escape danger.
  * **Invincible**: For a short time the player can not take any damage, giving the player the possibility to play more brave.  
    These power-ups make the gameplay more dynamic and give you exciting tools to turn the tide and let players take risks 
  they would usually avoid, like running through bomb zones or directly tackling aggressive enemies. The power-up can be activated 
  by pressing "Enter". 


* **Second HUD**: There is an extra HUD in the top right corner, used to track the special power-ups. This makes it easy to 
track the amount of collected special power-ups, so the player can plan his moves. During the time one of these 
power-ups is activated, a red timer is visible. 


* **Highscores**: The user can see the best scores for each map and even reset them in the main menu if wanted. Competing for the top best highscore makes 
the game replayable and adds a challenge for players to improve. Highscores are based on the highest remaining time and are 
stored locally on the computer. If a new highscore is set, a special sound effect occurs.

  
* **Mute Button**: The user can mute and unmute sound effects and background music. The button for this is displayed on 
the menu screen. This is making the game more flexible for personal preferences. 


* **Red Frame**: When the game is about to end (the last 30 seconds), a blinking red frame appears around the game window. It adds urgency and 
makes the final moments of the game more intense and exciting.


* **Background Picture for Menu Screen**: The menu screen has a vibrant background image, making the game feel more inviting 
right from the start. It’s a small touch that creates a better first impression.


## 8. Assets and Resources 
* **Graphics**: The in-game graphics come exclusively from the provided textures (Basic Tiles Spritesheet, Original Bomberman 
Spritesheet and Things Spritesheet). The menu background image was created using AI (ChatGPT).
* **Sound Effects & Music**: Open-source assets sourced from OpenGameArt.


## 9. Credits
* Developed by: Amelie Oheim, Luca Birkholz
* Special thanks to the libGDX framework and the open-source community for providing invaluable tools and resources. 
  * Please note that references to specific sources are included within the code (in method-level or class-level comments) for further reading.
* AI was occasionally used for guidance on game construction principles, concept explanations, and assistance with bug fixing.

