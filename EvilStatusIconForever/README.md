Evil Status Icon Forever
========================

This is a GNOME 3.2 - 3.6 shell extensions that let users have good old applet-like notification area on the top bar.


How to Install
--------------

 1. Enable it at https://extensions.gnome.org/extension/99/evial-status-icon-forerver/

How to Use
-----------

After install and enable this extension, you should have a file under `~/.local/share/gnome-shell/extensions/EvilStatusIconForever@bone.twbbs.org.tw/extension.js`.

Open this file using your favorite text editor, you should have seen the following code section, just add the application into ``notification`` array to make its notification show on top bar.

You may use `removeStatusIcon` array to control which built-in icon should be hidden.

**!! NOTE !!**

You have to restart GNOME (Alt-F2 and enter r) to make it work after install and change settings!!

    /*****************************************************
     * Statuc Icon Settings
     ****************************************************/
    
    //
    // Add application you want it shows thier notification status
    // icon on top bar to the following list.
    //
    // You may use top/htop to find out the name of application.
    //
    
    var notification = [
        'deadbeef',     // Deadbeef Music Player
        'pidgin',       // Pidgin IM Client
        'gcin',         // GCIN Chinese Input Method
        'hime'          // HIME Imput Method Editor
    ]
    
    
    // Add which built-in status icon you want to remove in the
    // following list.
    //
    
    var removeStatusIcon = [
        'a11y',         // Accessibility
        // 'volume',
        // 'battery',
        // 'keyboard',
        // 'bluetooth',
        // 'network'
    ]


How to Find Application Name
=============================

  0. This may borken your GNOME, BE CAREFUL AND BACKUP FIRST!!!!
  1. You need root premission to do this.
  2. Edit `/usr/share/gnome-shell/js/ui/statusIconDispatcher.js`
  3. Move to line 48, you should see a function called `_onTrayIconAdded` which look like the following:

        _onTrayIconAdded: function(o, icon) {
            let wmClass = (icon.wm_class || 'unknown').toLowerCase();
            let role = STANDARD_TRAY_ICON_IMPLEMENTATIONS[wmClass];
            if (role)
                this.emit('status-icon-added', icon, role);
            else
                this.emit('message-icon-added', icon);
        },  

  4. Add `global.log("wmClass[] = " + wmClass);` after the line of `let role = ...`, now this function should look like the following:

        _onTrayIconAdded: function(o, icon) {
            let wmClass = (icon.wm_class || 'unknown').toLowerCase();
            let role = STANDARD_TRAY_ICON_IMPLEMENTATIONS[wmClass];
            global.log("wmClass[] = " + wmClass);
            if (role)
                this.emit('status-icon-added', icon, role);
            else
                this.emit('message-icon-added', icon);
        },  

  5. Restart GNOME 3 by press `Alt + F2` and `r` and hit enter.
  6. Start GNOME console by press `Alt + F2` and `lg` and hit enter.
  7. Switch to `Errors` tab, now you should see message like the following:

        wmClass[] = deadbeef
        wmClass[] = pidgin

  8. The above is all tray icon you have now, pick up those tray icon you want it appears on top bar, and put thier name into `notification` array.
  9. Restart GNOME, now you should see them on top bar. Goold Luck! :)

        
