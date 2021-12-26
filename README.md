# GTOHelper
An online poker HUD/tracker (PokerTracker4) -> poker GTO solver (PioSolver) automation tool

The initial purpose of the tool was to alleviate the painful and repetitive process of manually inputting and solving for hands that you wanted to review from your last grind session. While the functionality of scraping data from PokerTracker4's local database, packaging the hands, internally queueing the hands to be solved, and executing the hands in the solver via it's API has been completed, there's still a lot more potential for cool features to be completed that I unfortunately don't have the time/energy to complete.

Eg.
- Find your average distance from GTO between your play and the solver for each hand in a bulk Position vs. Position computation. This would quantify where you're playing the poorest ~ rather than subjectively guessing where you're underperforming.
- Analyze passivity/aggressiveness imbalances in certain situations by calculating confidence intervals around given solver action frequencies (eg, let's say solver high frequency checks on paired boards when OOP. Well, with some sample size we can deduce if you tend to check sufficiently).
- Filter out hands vs casual players to reduce non-gto hands from sample sizes
- I could go on... Sufficed to say, I'm happy to review and merge in (or give contributor permissions) to anyone who wants to add to this tool. 

![Sessions](https://github.com/Mister-Kitty/Mister-Kitty.github.io/blob/3cbb8b91d2556ee92580313fcacdf9e3d0fb5d52/img/GTOHelper/Session%20Tab.png)

![Position v position](https://github.com/Mister-Kitty/Mister-Kitty.github.io/blob/3cbb8b91d2556ee92580313fcacdf9e3d0fb5d52/img/GTOHelper/Position%20v%20Position.png)

![Work Queue](https://github.com/Mister-Kitty/Mister-Kitty.github.io/blob/3cbb8b91d2556ee92580313fcacdf9e3d0fb5d52/img/GTOHelper/Work%20Queue.png)

# Step by step "super easy, barely an inconvenience" compilation instructions
1. Download and install IntelliJ Idea (Community Edition). It's a free Java development environment.
2. After opening IntelliJ, click 'Get from VCS' (Version Control System) and paste this code repository's URL (https://github.com/Mister-Kitty/GTOHelper.git). Clone. Wait for download.
3. On the top toolbar, go to 'File > Project Structure' and go to the Project tab. Click on the SDK dropdown and pick a verion 15 or later. If you don't have it installed, you can download version 17 here https://www.oracle.com/java/technologies/downloads/#jdk17-windows. Make sure the 'Language Level' matches and press Apply.
4.   - Note - The following steps need only be followed if IntelliJ doesn't successfully automatically fill them in -
5. Then go to the Modules tab on the same Project Structure page. Click and expand src>main>java (shown in blue below) and click the blue Sources button to the right of "Mark as". Fill in with the appropriatly for Tests and Resources. Use the picture below as a guide. Note that you won't have the orange 'out' folder. Don't worry about it.
![Modules](https://github.com/Mister-Kitty/Mister-Kitty.github.io/blob/281df6abb17c884792bc74e13c4d57b46d9fc368/img/GTOHelper/Modules.png)
6. Then go to the Global Libraries tab on the same Project Structure page. Press + (new global library), click Java, then click on the "lib" directory in the project. Click Ok. Click Ok again when it asks to select module. Then, press + again. click Java again. This time, expand the lib folder, the javafx-sdk folder, and click the inner lib folder therein. Click Ok again. Note that I need to apologize for this step. It's embarassing that I don't have a build framework. But when I started this prototype I thought I wouldn't need more than 1 dependancy, and so was sloppy.
7. On the top toolbar go to 'Run > Edit Configurations...', and 'Add New Configuration > Application' Fill in the Java SDK (I'm using 15 in the image), select the Main method, click 'modify options' and add 'add VM options'. Copy-paste the following to the VM options: --module-path ".\lib\javafx-sdk-15.0.1\lib" --add-modules javafx.controls,javafx.fxml,javafx.base,javafx.web

# Dude, where's my .jar?
Q: Can't you provide me with an .exe or something? I don't want to do the above steps.
A: I'm really sorry. I picked JavaFX for GUI because IntelliJ had an integration that required no additional steps and no internal Java/JavaFX linking/building knowledge. The functionality was rug-pulled after Java 8, and I simply don't have the time to figure it out. It's not simple at all. If you know a Java developer, I'd be happy to have them submit the code and I'll merge it in for everyone.
Q: Exception on GTOHelperModel.java's rootLoader.load() function
A: Make sure the Resource folder is marked back on step 5
Q: I get an error about JUnit when compiling...
A: Ensure you have GTOHelper\lib in Global Libraries as per section 6. It needs the testing framework.

# Etcetera
I hope you find this useful. I spent a *load* of time on this earlier in 2021, and got entirely burned out on this project because of how horrible PokerTracker4 was to work with. Amazing, there's a lot of hand data that you simply can't get from PT4. There were times I had to literally code an internal "replay" of the hand to find out trivial information. The last straw that caused me to gave up was when I found out that there is no saved hand data about anything 5bet+, meaning I would have to scrape all 5bet+ pot data fresh from the hand history .txt itself to be able to provide full functionality for deep stacked games. 
