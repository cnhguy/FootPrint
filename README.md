[![Build Status](https://travis-ci.com/cnhguy/FootPrint.svg?branch=master)](https://travis-ci.com/cnhguy/FootPrint)

# FootPrint: Variable History Viewer


# User Manual

## About:

FootPrint is a user-friendly, lightweight, and simple plugin for IntelliJ that allows Java developers to view the history of their variables. FootPrint is great for developers who often find themselves overstepping in the regular IntelliJ debugger. Instead of having to restart the debugging process, users can use FootPrint to view their variables’ histories and the line numbers at which values were changed. After starting FootPrint, users can simply select one or more variables to monitor. At any point in the debugging process, they can click to expand on a variable to view the previous values that it was assigned to, as well as which line these values were updated. Furthermore, by studying their variable’s history, a developer can better understand what is going on in a program. For example, if a method has unexpected side effects on other variables of interest, FootPrint would allow a user to easily see that.

## Example: 
 
Consider this scenario:
```
Line 1: int sum = 0

Line 2: for (int i = 0; i < 6; i++) {

Line 3:    sum += i; 
    
Line 4: }

Line 5: System.out.println(sum); 
```
 
The user wants to track how the variables `sum` and `i` change throughout the for loop. The user first sets debugging breakpoints as normal and start a debugging session by clicking on the FootPrint icon. Upon hitting the first breakpoint, he or she will pick from the variables that show up on the debugging variables window (in this case, `sum` and `i`) and add them to FootPrint for tracking (see more in the instructions section). The user then step through the program while FootPrint records the different values that `sum` and `i` were previously assigned (along with line numbers where the change occurred). The histories can be accessed anytime throughout the debugging process. FootPrint will create the following output for their histories:
 
	History:
 
		sum:
		line    value
		 1       0
		 3       1
		 3       3
		 3       6
		 3       10
		 15      3
		 
		i:
		line    value
		 2       0
		 2       1
		 2       2
		 2       3
		 2       4
		 2       5
		 
## Installation:

1) In Intellij, navigate to `Settings` and select `Preferences.` 
2) Select the `Plugins` option
3) Select `Install JetBrains plugin` and search for FootPrint
4) Select `Install` and confirm. After restarting Intellij, you should now see the FootPrint icon in the upper right hand toolbar next to the Debug button.
 
## Instructions:

1) Set breakpoints and click the FootPrint icon, found in the upper right next to the regular IntelliJ Debugger icon. This should start a debugging session for most recently executed program
2) Select the variables you wish to monitor from the variables window (simply right-click them and select `Add to FootPrint` or type their names in the input box of the FootPrint window).
3) Step through your program as normal
4) Add more variables if you wish at any time in the debugging process
5) In a separate pane to the left of the debugger window, you should see a list of your selected variables. Clicking on a the name of one of these variables at any point in the debugging process will display their value history along with which line the value was updated from the start of the program up to whichever line you are currently at in the debugger.
6) When you are finished, you can simply exit FootPrint by clicking the `X` in the upper right hand corner of the FootPrint pane.

Congrats! You have successfully used FootPrint.

# Developer Manual

## Dependencies:

* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)


## Cloning The Repository:

1) On the repository page, select `Clone or download` and copy the link provided
2) On the Intellij start page, select `Checkout from Version Control` and `Git`
3) Paste the URL from step 1 into the URL field and choose your desired directory and hit `Clone`
4) Select `use gradle 'wrapper' task configuration` and continue (the other default settings can be kept if desired)

Congrats! You have cloned the repository.

## Build and Test:

FootPrint uses Gradle as its build system. You can trigger a build by running `./gradlew build` from the root directory.

This command will build the project and run any tests in the `/src/test/` folder.

If you wish to test FootPrint in the IDE, you can select the `Gradle` option in IntelliJ located on the far right and double click `runIde`. This will launch a separate IntelliJ window where you will see the FootPrint icon in the upper right hand corner. Here, you may test the functionality of FootPrint.
