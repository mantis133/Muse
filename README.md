# Muse

## What is Muse?
Muse is and android application for playing local music files that are located in the applications data files. 

Muse is my first attempt at an Android/mobile application without using a dedicated tutorial, I fully expect the codebase to not be at the level of someone who fully understands all the code that is being written and will likely be full of notes to remind me of the functionality of any specific section of code.

Despite the innate limits my current knowledge as detailed above, Muse is an application I have wanted to make for some years at this point after each music application that I have used for local offline play just not having all the features I have wanted or just plain having annoying performance issues, *cough* Youtube Music *cough*.

## Features
- Foreground and background playback of music files.
- Playlist support through a subset of the extended M3U file format.
- Shuffling and un-shuffling of playlists.
- Looping, both single song and full playlist.
- Song selection within the loaded playlist.

## Whats Next?
- Moving the application away from being constructed using `Activity`s for each screen to using `Fragment`s.
- Move the UI to using Jetpack Compose for most of the design instead of XML and kotlin. 
- Updating UI to a much nicer look.
- Fixing various known issues that are causing crashes and unintended behavior, some of which are currently suspected of being caused by the way state is being stored.

## How to Build
1. Use `git clone` to create a local copy of the repository.
2. open up in Android Studio.
3. The project can then be built/loaded onto an android device just as normal.