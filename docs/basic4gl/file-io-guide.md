# Programmer's Guide: File I/O

## A note on security
Basic4GL programs can only read and write files from the directory where the Basic4GL program was saved
(or any subdirectory thereof).

This is for security, and is intended to protect people new to programming
when trying out example programs from the internet and other sources.
For all we know, the person who wrote the program might think that overwriting files in the Windows system directory is a hilarious practical joke.
This way the potential damage is restricted to a small subfolder, and the people can download and run Basic4GL programs with confidence.

Obviously this means that if you distribute Basic4GL programs that use File I/O,
you will have to ensure that the files read/written to end up in the appropriate directory so that they can be reached.

> [!NOTE]
>
> This security restriction applies to the general purpose File I/O routines described below.
>
> Other functions that load data from disk are not subject to these restrictions,
> in particular the image and texture loading functions can load any file they want.

> [!IMPORTANT]
>
> You can switch off these safety features by unchecking _"Safe mode"_ in the settings screen.
>
> If you do this, then you will need to make sure any program you run in Basic4GLj will not damage your computer.
>
> Also, standalone programs created with Basic4GLj ALWAYS run with _"safe mode"_ switched _OFF_.

## Opening files
`OpenFileRead` and `OpenFileWrite`

Files are opened like so:

(For writing):
```
dim file
...
file = OpenFileWrite ("Files/filename.ext")

(For reading):

dim file
...
file = OpenFileRead ("Files/filename.ext")
```

Where `filename.ext` is the filename and extension that is to be opened.

`file` is an integer variable that will store the file handle.

This is a number that Basic4GL generates to identify the file that was just opened,
and will be passed to other file routines to read data from or write data to the file.

> [!IMPORTANT]
>
> If a file is opened for writing, it replaces any file that was there previously.
>
> If no file exists, one is created.

## Error handling

### FileError
If a file I/O routine fails, the Basic4GL program simply keeps running,
without performing the particular file operation that it attempted.

You can test whether the operation succeeded by calling the `FileError ()` function.
This is updated after every file operation.
If the operation succeeded, it will be set to an error message, describing what went wrong.

For example:
```
dim file
file = OpenFileRead ("c:\autoexec.bat")
if FileError () <> "" then print FileError (): end endif
' Carry on...
```

## Closing the file

### CloseFile
It is good practice to close the file once you've finished with it as follows:
```
CloseFile (file)
```

> [!NOTE]
>
> If you forget, or your program stops for any reason before it can close the file,
> Basic4GL will close it automatically, the next time you run a Basic4GL program or when you close down Basic4GL.

## File reading routines
> [!IMPORTANT]
>
> The file must have been opened with `OpenFileRead` for these routines to work correctly.

### ReadLine
`ReadLine(file)` reads a line from a text `file` and returns it as a string.

The lines are separated by carriage return and/or newline characters.

### ReadText
`ReadText(file, skipEOL)` skips over whitespace (_spaces_, _tabs_ e.t.c) until it finds some text.
It then returns all the consecutive text at that point until a whitespace character has been reached, as a string.

`SkipEOL` is a boolean (`true`/`false`) parameter.

If `SkipEOL` is `true`, then `ReadText` will skip over any end-of-line characters it finds in the file.
If `SkipEOL` is `false`, it will stop at the end-of-line and return a blank string.

This can be used to break up a text files into words.

### ReadChar
`ReadChar(file)` reads a single character from the `file` and returns it as a string.

### ReadByte
`ReadByte(file)` reads a single binary byte from the `file` and returns it as an integer.

### ReadWord
`ReadWord(file)` reads a two byte "word" from the `file` and returns it as an integer.

### ReadInt
`ReadInt(file)` reads a four byte integer from the `file` and returns it as an integer.

### ReadFloat
`ReadFloat(file)` reads four bytes as a four byte floating point number and returns it as a real.

### ReadDouble
`ReadDouble(file)` reads eight bytes as an eight byte floating point number and returns it as a real.

### ReadReal
`ReadReal(file)` is a synonym for `ReadFloat(file)` in the current version of Basic4GL and Basic4GLj.

> [!NOTE]
>
> Basic4GL's "real" type is equivalent to a "float" in C.

## File writing routines
> [!IMPORTANT]
>
> The file must have been opened with `OpenFileWrite` for these routines to work correctly.

### WriteLine
`WriteLine (file, text)` writes text to the file and automatically appends a carriage return/newline pair.

`text` is a string value.

### WriteString
`WriteString (file, text)` writes text to the file.

> [!IMPORTANT]
>
> `WriteString` does not append a carriage return or linefeed to text output.
> A zero byte string terminator is NOT appended either..

`text` is a string value.

### WriteChar
`WriteChar (file, text)` writes the first character of `text` to the `file` as a single character.

`text` is a string value.

### WriteByte
`WriteByte (file, intval)` writes `intval` to the `file` as a single byte value.

`intval` is an integer value.

### WriteWord
`WriteWord (file, intval)` writes `intval` to the `file` as a two byte "word" value.

`intval` is an integer value.

### WriteInt
`WriteInt (file, intval)` writes `intval` to the `file` as a four byte integer value.

`intval` is an integer value.

### WriteFloat
`WriteFloat (file, realval)` writes `realval` to the `file` as a four byte floating point value.

`realval` is a real value.

### WriteDouble
`WriteDouble (file, realval)` writes `realval` to the file as an eight byte floating point value.

`realval` is a real value.

### WriteReal
`WriteReal (file, realval)` is a synonym for `WriteFloat (file, realval)`

## Other file I/O routines

### EndOfFile
`EndOfFile (file)` applies to files opened for reading, and returns `true` if we have reached the end of the file.

### Seek
`Seek (file, offset)` applies to files opened for reading,
and attempts to reposition the reading position to offset `bytes` from the beginning of the `file`.

## Deleting a file

### DeleteFile
`DeleteFile(filename)` will delete a file.

> [!IMPORTANT]
>
> This routine is only available when _"Safe mode"_ is switched OFF.

If the delete routine succeeds, `DeleteFile()` returns `true`.

Otherwise, `DeleteFile()` returns `false`, and `FileError()` can be used to retrieve the text of the error.

## Directory listing routines

### FindFirstFile
`FindFirstFile(mask)` returns the filename of the first file that matches the text string `mask`.

Example:
```
dim filename$
filename$ = FindFirstFile("*.gb")
print filename$
```

Example 2:
```
print FindFirstFile("files\*.*")
```

> [!IMPORTANT]
>
> Directory listing is subject to the same restrictions as general file access.
>
> That is, the directory must be the same directory as where the Basic4GL program is saved, or a subdirectory.

If no matching file is found, `FindFirstFile` returns an empty string (`""`).

### FindNextFile
`FindNextFile()` returns the filename of the next matching file in the directory.

This function uses the same mask as was passed to `FindFirstFile`,
and therefore will only work after a successful `FindFirstFile` call.

`FindNextFile` will keep returning the next filename until there are no more matching files,
at which point it returns an empty string (`""`).

Example:
```
dim filename$
filename$ = FindFirstFile("*.gb")
while filename$ <> ""
printr filename$
filename$ = FindNextFile()
wend
FindClose()
```

### FindClose
`FindClose()` will free resources after a `FindFirstFile..FindNextFile` directory search.

> [!NOTE]
>
> It is not strictly required as Basic4GL will do this for you automatically when the program finishes.
> However, it is good practice.

## Credits
Basic4GL, Copyright (C) 2003-2007 Tom Mulgrew

_Programmer's guide_

26-Jul-2008
Tom Mulgrew

Documentation modified for Markdown formatting by Nathaniel Nielsen
