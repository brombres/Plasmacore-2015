//
//  AppDelegate.swift
//  Project-macOS
//
//  Created by Abe Pralle on 11/27/16.
//  Copyright © 2019 AuthorName. All rights reserved.
//

import Cocoa

@NSApplicationMain
class AppDelegate: NSObject, NSApplicationDelegate {

  @IBOutlet weak var window: NSWindow!

  func applicationWillFinishLaunching(_ notification: Notification)
  {
    Plasmacore.setMessageListener(type:"ping",once:true) { (m:PlasmacoreMessage) in
    //  m.reply().set(name:"pong",value:true)
      PlasmacoreMessage( type:"pong" ).set( name:"sup", value:"dawg" ).send()
    }
  }

  func applicationDidFinishLaunching(_ aNotification: Notification) {
    // Insert code here to initialize your application
    Plasmacore.start()
  }

  func applicationWillTerminate(_ aNotification: Notification) {
    // Insert code here to tear down your application
    Plasmacore.save()
    Plasmacore.stop()
  }

  func applicationShouldTerminateAfterLastWindowClosed(_ sender: NSApplication) -> Bool
  {
    return true
  }
    
  func application(_ sender: NSApplication, openFile filename: String) -> Bool {
    Plasmacore.start();
    
    if let result = PlasmacoreMessage( type:"Application.on_open_file" ).set( name:"filepath", value:filename ).send()
    {
      return result.getLogical( name:"success" )
    }
    else
    {
      return false;
    }
  }
}

