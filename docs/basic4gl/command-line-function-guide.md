# Programmer's Guide: Command Line Functions

Basic4GL standalone programs can accept commands from the command line.
Command line arguments are entered after the program name when a program is run from the command line.

For example, if we built a standalone Java app called "CmdTest.jar", and ran it in a command prompt window with the command:

> java -XstartOnFirstThread -jar "CmdTest.jar" 1 banana 2 cucumber 3 "Tomato sandwich"

Then we have passed it 6 parameters:

1. `1`
2. `banana`
3. `2`
4. `cucumber`
5. `3`
6. `Tomato sandwich`

We can access these parameters with the `ArgCount` and `Arg` functions.


> [!IMPORTANT]
>
> `-XstartOnFirstThread` is required by LWJGL for the program window to display on Mac OS
> when running standalone apps from the command prompt.
>
> If your program does not start when run from the command prompt,
> try adding `-XstartOnFirstThread` to the Java arguments.

### ArgCount
`ArgCount()` returns the number of command line arguments.

### Arg
`Arg(index)` returns parameter number index as a text string, where `index` is `0` to return the first parameter.

`index` should be between `0` and `ArgCount() - 1`, otherwise `Arg(index)` returns a blank string.

## Setting command line arguments within Basic4GL

> [!IMPORTANT]
>
> Setting command line arguments within Basic4GLj is upcoming functionality for v0.7.0 and is not available in older versions

To set command line arguments for a program run inside Basic4GLj, open **Application** > **Project Settings**, select the **Program Arguments** tab, and enter one argument per line.

## Some examples

### Display all arguments:
```
dim i
printr ArgCount(); " argument(s) found"
for i = 0 to ArgCount() - 1
printr Arg(i)
next
```

### Compile and run another program:
```
dim prog
if ArgCount() = 0 then
printr "No program name!"
end
endif
prog = CompileFile(Arg(0), "__")
if CompilerError() <> "" then
printr CompilerError()
end
endif
Execute(prog)
if CompilerError() <> "" then
print CompilerError()
end
endif
```

## Credits
Basic4GL, Copyright (C) 2003-2007 Tom Mulgrew

_Programmer's guide_

26-Jul-2008
Tom Mulgrew

Documentation modified for Markdown formatting by Nathaniel Nielsen