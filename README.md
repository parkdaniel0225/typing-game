# Death by Spell Check

## Short Description

Death by Spell Check is an interactive Java-based typing game designed to improve typing speed and accuracy through engaging gameplay. Players must correctly type/click words within a limited time while managing lives, progressing through difficulty levels, and utilizing power-ups.
The system combines real-time input, scoring (WPM and accuracy), and word definitions to create an engaging learning experience.
---

## Required Libraries, Tools, and Third-Party Resources
This project uses standard Java libraries only. No external `.jar` libraries are required.
The lib folder is only for Junit test

### Required Software
- Windows, Mac OS, Linux
- Java Development Kit (JDK) 8 or higher  
  Recommended version: JDK 19
- IDE: IntelliJ IDEA, Eclipse, Netbeans

### Third-Party Resources Used at Runtime
- dictionaryapi.dev  
  Used to fetch word definitions during gameplay
- URL of words list: https://raw.githubusercontent.com/first20hours/google-10000-english/master/google-10000-english-no-swears.txt 
  Used to download the word list used by the game

### Important Note
An internet connection may be required the first time the game runs so that it can download the word list and fetch definitions.


## Project Files

Important source files (in src) include:
- Run_MainMenuScreen.java
- Home.java
- GameplayScreen.java
- ControlScreen.java
- GameSettings.java
- GameTutorial.java
- HighScoresScreen.java
- PlayerScreen.java
- SessionRecord.java
- WordDictionary.java
- WordFilter.java
- DefinitionLookup.java
- Fullscreen.java
- GameButton.java
- LetterCircleButton.java

Important data files:
- accounts.json
- sessions.json
- game_settings.json (run time, no write to local)

---

## Detailed Build Instructions for a Modern Windows System

These instructions are written for a user who may be unfamiliar with Java tools.


### Setup Instructions (IDE Example: IntelliJ IDEA)
#### 1. Unzip and Open
* Extract the downloaded `.zip` file into a dedicated folder on your computer.
* Open IntelliJ IDEA and select **Open**.
* Navigate to and select the root folder of the project (the folder containing `src`, `lib`, and `README.md`).

#### 2. Run the Game
* In the Project navigation tree on the left, open `src`.
* Find the file named **`Run_MainMenuScreen.java`**.
* Right-click `Run_MainMenuScreen.java` and select **Run 'Run_MainMenuScreen.main()'**.

#### May need to configure project structure
IntelliJ needs to be told where your source code is located to compile it correctly:
* Go to **File > Project Structure** (or press `Ctrl+Alt+Shift+S`).
* **Project Settings:** Ensure the **Project SDK** is set to JDK 17 or higher.
* **Modules:** * Click on **Modules** in the left sidebar.
    * Select your project in the center list.
    * In the **Sources** tab, find the **`src`** folder in the file tree.
    * Right-click the `src` folder and select **Sources** (the folder icon should turn blue).
    * Click **OK**.

### User Guide
1. Press any key then Register/Login to your account using a username and password
2. Click the Tutorial to get an idea on how to play the game
3. Click start game and choose difficulty level from Easy, Medium and Hard
4. After selecting the difficulty level click start game
5. There will be a 3 second count-down and then type/click the words that are shown on the screen to form correct words.
6. On the screen you can also see the difficulty level, scores, lives and power-ups.
7. You will start with 3 lives but each mistake will reduce one life from that. You will also have the option to pause the game as well.
8. After all the lives are used on the game over screen you can see the score, WPM, accuracy, the words you could not solve and the meaning of them.
9. You will have options to play again or change difficulty or even to go back to menu.

### Parental Controls
1. Start the game by running Run_MainMenuScreen.java
2. Press any key to begin the game.
3. Click on register
4. Create a username and password
5. Click on the I am a parent/teacher box
6. Create the account
7. Login to your account using that username and password
8. Click on the parent/teacher controls where you can edit starting lives, reset session history, reset an account password and even delete an account. 
9. There is NO PIN required

### Important Notes of TA
1. Main class to run: Run_MainMenuScreen.java
2. No external libraries are required
3. Works entirely with standard java
4. JSON files are automatically created
5. Program will run offline

The end.