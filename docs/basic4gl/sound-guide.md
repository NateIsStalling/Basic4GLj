
# Programmer's Guide: Sound

## Sound System

### Playing Sound Effects

The following file formats are currently supported for playing sound effects using the `loadsound` and `playsound` functions:

| File Extension | Codec         | 
|----------------|---------------|
| wav            | CodecWav      |
| ogg            | CodecJOrbis   |
| xm             | CodecIBXM     |
| s3m            | CodecIBXM     |
| mod            | CodecIBXM     |

### Playing Music

Ogg Vorbis files are supported for playing music continuously using the `playmusic` function.

> [!IMPORTANT]
>
> Legacy Basic4GL for Windows versions use the Audiere sound library,
> which supports a number of different sound formats such as _.wav_, streamed music formats such as Ogg Vorbis,
> and "mod" formats like _.mod_, _.s3m_, _.xm_ and _.it_.
>
> If you experience issues loading any sound formats in Basic4GLj,
> please report any issues on the project's GitHub page at https://github.com/NateIsStalling/Basic4GLj/issues

> [!WARNING]
>
> _.mp3_ files are not currently supported by Basic4GLj
>
> (since legacy Basic4GL versions do not support _.mp3_ files in the standard Sound library)


### Sound System Licenses

Basic4GLj depends on a fork of Paulscode-SoundSystem to support sound codecs for LWJGL 3 which can be found here:

https://github.com/NateIsStalling/Paulscode-SoundSystem/tree/lwjgl3

Licenses for Paulscode-SoundSystem and related sound Codecs can be found in Basic4GLj's app module `dist` directory or in release packages under `/LICENSES/sound system`

## Sharing Standalone Programs

> [!IMPORTANT]
>
> Guides for sharing Basic4GLj programs are a work in progress.
>
> If you experience any issues sharing programs,
> please report any trouble you experience on the project's GitHub page at https://github.com/NateIsStalling/Basic4GLj/issues

If you use sound or music functions in your program, and you wish to share it as a standalone program,
be aware that you must also:

- manually add any sound files to the exported _.zip_ archive

which must be placed in the same folder as your exported _.jar_ file, depending on the file path used in your program.

Otherwise, your program will run silently.

> [!IMPORTANT]
>
> Having to manually add files to the exported _.zip_ archive should be fixed in future versions of Basic4GLj

## Sound functions

### LoadSound
Sounds are loaded as follows:
```
dim sound
...
sound = LoadSound (filename)
```

`Filename` must refer to a file of a supported sound format.

### PlaySound
Once the sound has been loaded, it can be played as follows:
```
PlaySound (sound)
```
or
```
PlaySound(sound, volume, looped)
```

Here sound is the sound handle that was returned from `LoadSound(...)`.
Volume is the sound volume, where `1 = full volume`, `0.5 = half volume`, etc.

> [!WARNING]
>
> You can also use values greater than `1`, but be warned that the sound may "clip" and become distorted.

Setting `looped` to `true` will cause the sound to play continuously in a loop.

> [!NOTE]
>
> If volume and looped are not specified they default to volume = 1 and looped = false.

`PlaySound(...)` returns the number of the "voice" that was chosen to play the sound.

This number is useful if you want to stop the sound later (especially for looped sounds like footsteps),  
as you can pass it to the `StopSoundVoice(...)` function.

> [!IMPORTANT]
>
> Basic4GL supports 10 voices, which defines the maximum number of sounds that can be played simultaneously.


### DeleteSound
`DeleteSound (sound)` deletes the sound from memory.

If you don't explicitly delete them, Basic4GL will automatically do so when your program finishes.

### StopSoundVoice
To stop a sound playing, use:
```
StopSoundVoice(voice)
```
`Voice` is the number of the voice you wish to stop playing.
This number is returned from `PlaySound(...)` when the sound was started.

### StopSounds
You can also stop all sounds with:
```
StopSounds()
```

## Music functions
These functions are used to stream in and play music files, such as Ogg Vorbis, or "mod" files (_.mod_, _.s3m_, _.xm_, _.it_, etc).

### PlayMusic
Start playing a music file with:
```
PlayMusic(filename)
```
or
```
PlayMusic(filename, volume, looped)
```

`Filename` must be a file of a supported music format. `Volume` and `looped` behave the same as with `PlaySound(...)`.

This will open the file and start playing it immediately.
Unlike regular sound files music files are "streamed". This means that the file is not loaded into memory all at once.

Instead, the file is loaded in continuously while the music is playing.

> [!IMPORTANT]
>
> Basic4GL supports playing one music file at a time only.
>
> If a music file is already playing, it will stop and the new file will play instead.

Example:
```
dim filename$
printr"Filename:": input filename$
PlayMusic(filename$)
if SoundError() <> "" then printr SoundError(): end endif
while MusicPlaying(): Sleep(100): wend
StopMusic
StopMusic() will stop music file from playing.
```

### MusicPlaying
`MusicPlaying()` returns true while the music file is playing.

## SetMusicVolume
To set the music volume while music is playing, use:

### SetMusicVolume(volume)

Where volume behaves the same as with `PlaySound()` or `PlayMusic()`.

## Sound and music errors
If a sound or music function fails, Basic4GL will store a description of the error, which can be retrieved with the `SoundError()` function.

### SoundError
`SoundError()` returns a text string describing the result of the last sound or music function call.

If the call was successful, `SoundError()` returns the empty string (`""`). Otherwise, it returns the text of the error message.

Example:
```
dim sound, i
sound = LoadSound("c:\windows\media\chimes.wav")
if SoundError() <> "" then
printr SoundError()
else
PlaySound(sound)
Sleep(2000)
endif
```

You can test whether the Basic4GL sound engine has initialised correctly by placing the following code at the top of your program.
```
if SoundError() <> "" then
print SoundError()
end
endif
```

If the sound engine has not initialised correctly (because an error occurred), it will print the message:

> Sound playback is not available; the sound engine failed to initialize.

and stop.

> [!IMPORTANT]
>
> The initialization error message is typically caused by issues with initializing LWJGL 3 or creating an OpenAL context.
>
> Please report any sound initialization issues on the Basic4GLj's GitHub page at https://github.com/NateIsStalling/Basic4GLj/issues

## Credits
Basic4GL, Copyright (C) 2003-2007 Tom Mulgrew

_Programmer's guide_

26-Jul-2008
Tom Mulgrew

Documentation modified for Sound System notes and Markdown formatting by Nathaniel Nielsen