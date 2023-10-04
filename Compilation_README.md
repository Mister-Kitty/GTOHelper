# These are !OUTDATED! compilation instructions
1. Download and install IntelliJ Idea (Community Edition). It's a free Java development environment.
2. After opening IntelliJ, click 'Get from VCS' (Version Control System) and paste this code repository's URL (https://github.com/Mister-Kitty/GTOHelper.git). Clone. Wait for download.
3. On the top toolbar, go to 'File > Project Structure' and go to the Project tab. Click on the SDK dropdown and pick a verion 15 or later. If you don't have it installed, you can download version 17 here https://www.oracle.com/java/technologies/downloads/#jdk17-windows. Make sure the 'Language Level' matches and press Apply.

   - Note - The following steps need only be followed if IntelliJ doesn't successfully automatically fill them in -

4. Then go to the Modules tab on the same Project Structure page. Click and expand src>main>java (shown in blue below) and click the blue Sources button to the right of "Mark as". Fill in with the appropriatly for Tests and Resources. Use the picture below as a guide. Note that you won't have the orange 'out' folder. Don't worry about it.
![Modules](https://github.com/Mister-Kitty/Mister-Kitty.github.io/blob/281df6abb17c884792bc74e13c4d57b46d9fc368/img/GTOHelper/Modules.png)
5. Then go to the Global Libraries tab on the same Project Structure page. Press + (new global library), click Java, then click on the "lib" directory in the project. Click Ok. Click Ok again when it asks to select module. Then, press + again. click Java again. This time, expand the lib folder, the javafx-sdk folder, and click the inner lib folder therein. Click Ok again.
  Note that I need to apologize for this step. It's embarassing that I don't have a build framework. But when I started this prototype I thought I wouldn't need more than 1 dependancy, and so was sloppy.
7. On the top toolbar go to 'Run > Edit Configurations...', and 'Add New Configuration > Application' Fill in the Java SDK (I'm using 15 in the image), select the Main method, click 'modify options' and add 'add VM options'. Copy-paste the following to the VM options: --module-path ".\lib\javafx-sdk-15.0.1\lib" --add-modules javafx.controls,javafx.fxml,javafx.base,javafx.web
![RunConf](https://github.com/Mister-Kitty/Mister-Kitty.github.io/blob/e9745acc763c122d86a1ee2e957a82c790dbbe93/img/GTOHelper/Run%20Config.png)

# Q and A
Q: Exception on GTOHelperModel.java's rootLoader.load() function

A: Make sure the Resource folder is marked back on step 5

Q: I get an error about JUnit when compiling...

A: Ensure you have GTOHelper\lib in Global Libraries as per section 6. It needs the testing framework.

Q: On the "Run\Debug Configuration" screen (step 7), there are no drop-down options for the "main method" and it won't accept anything I paste.

A: This is because it fills this drop-down based on "main classes" found after you've run step 5 and labeled the "src" folder as where it should look to find these "main classes". Make sure