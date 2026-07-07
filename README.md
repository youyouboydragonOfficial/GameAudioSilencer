# Game Audio Silencer

Beta Android app for muting selected apps while music keeps playing.

Android does not expose normal per-app volume control to ordinary apps. This beta uses the system app-ops `PLAY_AUDIO` switch through `su` on rooted devices:

```text
cmd appops set <package> PLAY_AUDIO ignore
cmd appops set <package> PLAY_AUDIO allow
```

## Features

- Beta badge and first-run tutorial.
- Large safe top padding so the title avoids punch-hole cameras and display cutouts.
- Installed app picker with search.
- Root-powered per-app mute/unmute.
- GitHub Releases update check and APK download from inside the app.

## Notes

- Root is required for real per-app audio blocking in this beta.
- Non-root devices can still use the app UI, tutorial, and update checker, but cannot apply app-level audio blocking.
- Release APKs are distributed from GitHub Releases.
