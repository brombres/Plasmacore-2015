# Plasmacore

About     | &nbsp;
----------|-----------------------
Version   | 0.13.4
Date      | 2020-04-13
Platforms | macOS, Linux
Targets   | macOS, iOS, Android, Linux, Web


## Requirements
- The Rogue language must be installed separately from:
    - [https://github.com/AbePralle/Rogue](https://github.com/AbePralle/Rogue)
- macOS & iOS targets require Xcode.

### Compile Target Support
Host Platform | macOS   | Linux   |  iOS    | Android   | Web
--------------|---------|---------|---------|-----------|---------
macOS         | &#10003;|         | &#10003;| &#10003;  | &#10003;
Linux         |         | &#10003;|         | &#10003;* | &#10003;

&#42; Linux Android support is not well-tested.

## Bootstrap Command

To bootstrap a new Plasmacore-based project, install Rogue, open a Terminal in your new project folder, and copy and paste the following command:

    curl -O https://raw.githubusercontent.com/AbePralle/Plasmacore/master/Bootstrap.rogue && rogo --build=Bootstrap

The command will fetch a bootstrap build file which in turn will install and use Murphy McCauley's [https://github.com/MurphyMc/depz](depz) app to clone the latest Plasmacore repo as a subfolder and copy all relevant project files into the current folder.


## Documentation and Resources

There is some Rogue documentation here: [https://github.com/AbePralle/Rogue/wiki](https://github.com/AbePralle/Rogue/wiki)

There is no Plasmacore documentation yet.  You can manually browse the `Libraries/Rogue/Plasmacore` files.

A sample Plasmacore game project is available here: [https://github.com/AbePralle/PlasmacoreDemos](https://github.com/AbePralle/PlasmacoreDemos)


## Starting a New Project

1.  Run the bootstrap command.
2.  At the command line run e.g. `rogo ios`.  The first build will take a while as intermediate files are compiled.
3.  Open `Platforms/iOS/iOS-Project.xcodeproj` in Xcode and run on the simulator or a device.  You should see a blue screen.
4.  Edit `Source/Main.rogue` and add more game code.
5.  Either run `rogo ios` again or just compile and run in Xcode again as a build phase automatically runs `rogo ios`.  If you get an error in Xcode and you can't tell what it is, run `rogo ios` on the command line and you will see the compiler error message.
6.  Add images to Assets/Images and load them by name - `Image("img.png")`, `Font("SomeFont.png")`, etc.


## Updating an Existing Project

`rogo update` will update your current project to the latest version of Plasmacore (via `git`) without touching any game-specific files.


## License
Plasmacore is released under the terms of the [MIT License](https://en.wikipedia.org/wiki/MIT_License).

