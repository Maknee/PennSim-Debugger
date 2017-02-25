PennSim Debugger
======
**Pennsim Debugger** is a better version of the old Pennsim. Provides additional functionality on top of the original Pennsim. 

Makes Pennsim not look like a cis120 java swing project. :)

#### Screenshot
![Screenshot software](https://i.gyazo.com/7bbce59d5c6d622986243d060529875c.png)
======

## Features:
* Christmas Syntax Highlighting! (Eyes won't fry as easily)
* Access to entering line numbers (No more scrolling using that pesky scrollbar)

![Screenshot linenumber](https://i.gyazo.com/06bd4ef2ce8b3d3104739d7913f5eaf0.png)

* Memory Dump (Easier to view the values while running the program at the same time) 

![Screenshot memorydump](https://i.gyazo.com/d41c6b598a9a1d8db19dabeec0a23a9a.png)

* Generation of a flow chart of your program (See how program branches)

![Screenshot flowchart](https://i.gyazo.com/26aa335554c3f6bd76bbd3e517e9c7ab.png)

* Breakpoints panel (Keep track of breakpoints)

![Screenshot breakpoint](https://i.gyazo.com/3306f7c6fa5319d77b8baf0caf877119.png)

* A stack of the program (Keeps track of JSR calls and return addresses)

![Screenshot stack](https://i.gyazo.com/b69fa4492a0da310f18a30f8d615d17a.png)

* DataPath Chart (View which control signals are set for the current instruction)

![Screenshot datapath](https://i.gyazo.com/5b0547e4e9f4c10cf2ffd027d0336b8c.png)

## Download
* [Version 0.1](https://github.com/Maknee/PennSim-Debugger)

## Usage
By default the extra functionality is disabled.

* Enable breakpoints panel and stack information by pressing "Additional Information Output"
* Enable syntax highlighting by pressing "Christmas Syntax Highlighting"

Can be found at lower right of the screen:
![Screenshot Buttons](https://i.gyazo.com/614b594d5628eb2d18eab04c667ed250.png)

## Contributors
Henry Zhu

### Contributors on GitHub
* [Contributors](https://github.com/Maknee/PennSim-Debugger/graphs/contributors)

### Third party libraries
* JAVA SWING :))))))

## License 
* see [LICENSE](https://github.com/Maknee/PennSim-Debugger/LICENSE.md)

## Version 
* Version 0.1

## Contact
* e-mail: henryzhu@seas.upenn.edu

# Known Bugs
* Generating the flow chart may screw up register values. Make sure to reset the program after generating the flow chart.
