# HyprDroid Linux Terminal Bridge

HyprDroid does not execute Linux commands inside its own Android process. When the user enters a command that is not an installed Android app name, it requests Termux's explicit `RUN_COMMAND` permission and starts a foreground Termux session. The command is run inside a Debian PRoot-Distro userland, not against Android's system shell.

## Device setup

1. Install the current Termux app from F-Droid or the official GitHub releases. Do not use an old Play Store build.
2. In Termux, install PRoot-Distro and the Debian userland:

   ```sh
   pkg update
   pkg install proot-distro
   proot-distro install debian
   ```

3. In Termux, enable external command requests by setting `allow-external-apps=true` in `~/.termux/termux.properties`, then run `termux-reload-settings` or restart Termux.
4. Install/update HyprDroid. The first Linux command requests `com.termux.permission.RUN_COMMAND`; grant it to HyprDroid in Android App Info if Android does not show a permission prompt.
5. Enter a Linux command such as `ls`, `uname -a`, or `apt update` in the HyprDroid command field. Commands that match an installed app name (for example `run Ayarlar`) continue to open that Android app instead. Type `term` to open an interactive Debian shell.

## Security and capability boundary

Termux runs under its own Android application UID and prompts Android to authorize external command requests. PRoot provides a userspace, chroot-like filesystem view without Android root privileges; it does not grant kernel root. Linux packages and files written inside Debian stay in the Termux-owned userland. A command that targets Android private data, changes Android system settings, controls Android's window manager, or needs real root/kernel features will not gain those privileges through this bridge. HyprDroid remains an Android HOME launcher and does not turn VoltageOS into a Linux/Hyprland desktop.

## Sources

- Termux `RUN_COMMAND` intent, required app permission and `allow-external-apps`: https://github.com/termux/termux-app/wiki/RUN_COMMAND-Intent
- Termux PRoot-Distro rootless Linux userlands, installation, and limitations: https://github.com/termux/proot-distro
- PRoot project description of userspace chroot/bind behavior: https://proot-me.github.io/
