# FootPrint


# User Manual

## About:

FootPrint is a user-friendly, lightweight, and simple plugin for IntelliJ that allows Java developers to view the history of their variables. FootPrint is great for developers who often find themselves overstepping in the regular IntelliJ debugger. Instead of having to restart the debugging process, users can use FootPrint to view their variables’ histories. After starting FootPrint, users can simply select one or more variables to monitor via watchpoints. At any point in the debugging process, they can click to expand on a watched variable to view the previous values that it was assigned to, as well as which line these values were updated. Unlike standard watchpoints, a user can choose when to view their variables instead of being paused each time a variable updates, which can be especially inconvenient when dealing with multiple variables of interest. Furthermore, by studying their variable’s history, a developer can better understand what is going on in a program. For example, if a method has unexpected side effects on other variables of interest, FootPrint would allow a user to easily see that.

## Example: 
 
Consider this scenario:
```
Line 1: int sum = 0                    // set watchpoint

Line 2: for (int i = 0; i < 6; i++) {  // set watchpoint

Line 3:    sum += i; 
    
Line 4: }

Line 5: System.out.println(sum); 
```
 
The user wants to see how the variables sum  and i change throughout the for loop. He or she will set a watch point on sum and i to track the changes that will happen to them. Alternatively, a user could proceed to any point in the program after line 5 and would still be able to look back at the histories of sum and i anytime. FootPrint records the different values that sum and i were previously assigned to create the following output for their histories:
 
	History:
 
		sum → 0 → 1 → 3 → 6 → 10 → 15
		line → 1 →  3 → 3 → 3 → 3 →   3
 
		i      →  0 → 1  → 2 → 3 → 4 → 5
		line →  2 →  2 → 2 →  2 → 2 → 2 
 
## Instructions:

1) Click the FootPrint icon, found in the upper right next to the regular Intellij Debugger icon. This should start the debugger for most recently executed program
2) Select the variables you wish to monitor (simply click them in your code). FootPrint supports local variables and fields.
3) Set any desired breakpoints as normal
4) Run the debugger as normal
5) In a separate pane in the debugger window, you should see a list of your selected variables. Clicking on a the name of one of these variables at any point in the debugging process will display their value history along with which line the value was updated from the start of the program up to whichever line you are currently at in the debugger.
6) When you are finished, you can simply exit FootPrint by clicking the on the FootPrint pane.

Congrats! You have successfully used FootPrint.
