# Plasmacore

About     | Current Release
----------|-----------------------
Version   | v0.9.0
Date      | May 13, 2018
Platforms | macOS
Targets   | macOS, iOS, Linux, Web


## Notes
- Requires a Mac with Xcode and the iOS SDK.
- Requires the Rogue language to be installed:
    - https://github.com/AbePralle/Rogue


## Bootstrap Command

To bootstrap a new Plasmacore-based project, open a Terminal in your new project folder and copy and paste the following command:

    curl -O https://raw.githubusercontent.com/AbePralle/Plasmacore/master/Bootstrap.rogue && rogo --build=Bootstrap.rogue

The command will fetch a bootstrap makefile which in turn will `git clone` the latest Plasmacore repo in a temporary folder and copy all the files into the current folder.


## Documentation and Resources

There is some Rogue documentation here: [https://github.com/AbePralle/Rogue/wiki](https://github.com/AbePralle/Rogue/wiki)

There is no Plasmacore documentation yet.  You can manually browse the `Libraries/Rogue/Plasmacore` files.

A sample Plasmacore game project is available here: [https://github.com/AbePralle/PlasmacoreDemos](https://github.com/AbePralle/PlasmacoreDemos)


## Starting a New Project

1.  Run the bootstrap command or manually clone the Plasmacore repo and copy everything except the `.git` folder into your project folder.
2.  At the command line run e.g. `rogo ios`.  The first build will take a while as intermediate files are compiled.
3.  Open `Platforms/iOS/iOS-Project.xcodeproj` in Xcode and run on the simulator or a device.  You should see a blue screen.
4.  Edit `Source/Main.rogue` and add more game code.
5.  Either run `rogo ios` again or just compile and run in Xcode again as a build phase automatically runs `rogo ios`.  If you get an error in Xcode and you can't tell what it is, run `rogo ios` on the command line and you will see the compiler error message.
6.  Add images to Assets/Images and load them by name - `Image("img.png")`, `Font("SomeFont.png")`, etc.


## Updating an Existing Project

`rogo update` will update your current project to the latest version of Plasmacore (via `git` and `rsync`) without touching any game-specific files.


## License
Plasmacore is released into the Public Domain under the terms of the [Unlicense](http://unlicense.org/).

