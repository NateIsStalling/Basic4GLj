# Programmer's Guide: Keyboard, Mouse, and Joystick Input

## Keyboard input

### Input
Reads a text string from the keyboard.

Format:
```
Input variable
Input "prompt"; variable
Input "prompt", variable
```

`Input` will pause the program and wait until the user types in some text and hits enter. The text will be displayed on the screen as the user types.
If a prompt is given, it will be displayed on the screen. The first format (with the semicolon) automatically displays a question mark after the prompt.

The second format (with the comma) simply displays the prompt and nothing else.

Once the user has hit enter, the program will continue, and variable will contain the resulting text or number that the user entered.

Examples:

```
dim name$
input "What is your name"; name$
print "Hello " + name$
dim number
input "Please enter a number: ", number
print "The square root of " + number + " is " + sqrt (number)
```
> [!NOTE]
>
> Be aware that Basic4GL's implementation of "input" is not as complete as other BASICs.
> Basic4GL does not support inputting multiple variables with the same input command.
> Also, Basic4GL will not prompt the user to "Redo from start"
> if the text he/she entered cannot be converted into the destination variable type.
> Instead it will simply set the destination variable to 0.

> [!NOTE]
>
> There is an older `Input$()` function that has the syntax:
>
> `variable = Input$()`
>
> This is an old syntax, and kept only for backwards compatibility with older Basic4GL programs.

## Key state

### KeyDown and ScanKeyDown
Determines whether a key is currently pressed or released.

Format:
```
KeyDown(character)
```
```
ScanKeyDown(scan-code)
```
`KeyDown` takes the first character of the string argument passed to it.

`ScanKeyDown` takes a numeric virtual key code, often a `VK_x` constant (such as `VK_UP` e.t.c.)

> [!TIP]
>
> Click "Help|Functions and Constants list..." then the "Constants" tab for a list.

Both functions return `true` (`-1`) if the key is being pressed or `false` (`0`) if otherwise.

> [!NOTE]
>
> KeyDown("") will always return false.

#### Example 1:
```
ResizeText (5, 1)
while true
locate 0, 0
if KeyDown ("A") then print "Down"
else print " Up "
endif
wend
```

#### Example 2:
```
dim a#
while true
glClear (GL_DEPTH_BUFFER_BIT or GL_COLOR_BUFFER_BIT)
glLoadIdentity ()
glTranslatef (0, 0, -5)
glRotatef (a#, 0, 0, 1)
glBegin (GL_TRIANGLES)
glVertex2f ( 0, 1.5)
glVertex2f (-1,-1)
glVertex2f ( 1,-1)
glEnd ()
SwapBuffers ()
while SyncTimer (10)
if ScanKeyDown (VK_LEFT) then a# = a# + 3: endif
if ScanKeyDown (VK_RIGHT) then a# = a# - 3: endif
wend
wend
```

## Buffered input

### Inkey$ and InScanKey
Format:
```
Inkey$ ()
InScanKey ()
```

Basic4GL buffers characters and raw scan codes typed into the output window.

`Inkey$ ()` returns characters typed as single character strings.
If no characters are buffered, `Inkey$ ()` will return an empty string.

`InScanKey ()` returns scan codes as integers. If no scan codes are buffered, `InScanKey ()` returns `0`.

Example:
```
while true: print Inkey$ (): wend
```

### ClearKeys
Format:
```
ClearKeys ()
```

`ClearKeys ()` clears the keyboard buffer,
throwing away any keypresses that have yet to be handled by `Inkey$ ()` or `InScanKey ()`.

`ClearKeys ()` is equivalent to the following code:

```
While Inkey$() <> "": wend
While InScanKey() <> 0: wend
```

## Mouse input
The following functions can be used to read the mouse.

### Mouse_X, Mouse_Y
These functions return the position of the mouse in relation to the OpenGL window (if in windowed mode),
or the screen (fullscreen mode).

`Mouse_X()` returns the `X` (horizontal) position.
`Mouse_Y()` returns the `Y` (vertical) position.

Both functions return a real value between `0` (far left, or top) and `1` (far right, or bottom).

#### Example 1:
```
print Mouse_X () + ", " + Mouse_Y (): run
```

#### Example 2:
```
ResizeText (80, 50)
dim x, y, char$
while true
if not Mouse_Button (MOUSE_LBUTTON) then
locate x, y: print char$
endif
x = Mouse_X () * TextCols ()
y = Mouse_Y () * TextRows ()
char$ = CharAt$ (x, y)
locate x, y: print "X"
wend
```

### Mouse_Button
`Mouse_Button (index)` returns `true` if button `index` is being pressed, or `false` if it isn't.

The left mouse button is index `0`, the right is index `1` and the middle is index `2`.

Alternatively you can use the following constants:

| Button              | Constant       |
|---------------------|----------------|
| Left mouse button   | MOUSE_LBUTTON  |
| Right mouse button  | MOUSE_RBUTTON  |
| Middle mouse button | MOUSE_MBUTTON  |

Example:
```
dim i               
print "Press the mouse buttons!"
while true
  locate 0, 2
  for i = 0 to 2: printr Mouse_Button (i) + " ": next
wend
```

### Mouse_Wheel
`Mouse_Wheel()` returns how many notches the mouse wheel has turned since the last time `Mouse_Wheel()` was called
(or the program started).

For example:
```
dim i
print "Turn the mouse wheel!"
while true
i = i + Mouse_Wheel ()
locate 0, 2: print i + "    "
wend
```

### Mouse_XD(), Mouse_YD()
These functions return how far the mouse has moved since the last time `Mouse_XD()` or `Mouse_YD()` was called
(respectively).

`Mouse_XD()` returns the `X` (horizontal) distance.

`Mouse_YD()` returns the `Y` (vertical) distance.

These functions are useful for first-person shooter type movement, where the mouse is used to turn the player,
instead of controlling a pointer on the screen.

> [!WARNING]
>
> `Mouse_XD()` and `Mouse_YD()` work internally by positioning the mouse pointer in the middle of the window
> and measuring how far the mouse moves from that position.
>
> This means that using `Mouse_X()` or `Mouse_Y()` will produce unexpected results,
> and it is recommended you stick to one method or the other.

Example:
```
dim x#, z#
while true
  glClear (GL_DEPTH_BUFFER_BIT or GL_COLOR_BUFFER_BIT)
  glLoadIdentity ()
  glTranslatef (0, 0, -4)
  glRotatef (z#, 0, 0, 1)
  glRotatef (x#, 1, 0, 0)
  glBegin (GL_TRIANGLES)
  glVertex2f (0, 1)
  glVertex2f (-.5, -1)
  glVertex2f ( .5, -1)
  glEnd ()
  SwapBuffers ()
  z# = z# - Mouse_XD () * 100
  x# = x# + Mouse_YD () * 100
wend
```

## Joystick input
> [!NOTE]
>
> A big thanks to Tyler Bingham for implementing the joystick support for the original Basic4GL!

Basic4GL supports input from a single joystick. If more than one joystick is attached to a PC,
Basic4GL will use whatever one the operating system says is first.

The following functions can be used to read the joystick.

### Joy_Keys
`Joy_Keys()` takes a snapshot of the joystick and generates appropriate keypresses.

Arrow keys are generated for stick movement, and space bar and control (_Ctrl_) keypresses are generated for joystick buttons `0` and `1` respectively.

The keypresses can then be detected with the keyboard input functions:
- `InScanKey ()`
- `KeyDown (...)`
- `ScanKeyDown ()`

> [!NOTE]
>
> Inkey$() is not affected by Joy_Keys().

This effectively provides a simple and easy way of incorporating joystick and keyboard support into a program.

Example:
```
dim x, y
x = TextCols () / 2
y = TextRows () / 2
while true
  Joy_Keys ()
  if not ScanKeyDown (VK_SPACE) then
    locate x, y: print " "
  endif
  if ScanKeyDown (VK_LEFT)    and x > 0 then                  x = x - 1 endif
  if ScanKeyDown (VK_RIGHT)   and x < TextCols () - 1 then    x = x + 1 endif
  if ScanKeyDown (VK_UP)      and y > 0 then                  y = y - 1 endif
  if ScanKeyDown (VK_DOWN)    and y < TextRows () - 1 then    y = y + 1 endif
  locate x, y: print "X"
  Sleep (30)
wend
```

### Joy_X, Joy_Y
`Joy_X()` returns the `X` (horizontal) position.

`Joy_Y()` returns the `Y` (vertical) position.

Both functions return a value from `-32768` (far left, or top) to `32767` (far right or bottom).

`0` is the centre of each axis. (If you have a stable, properly calibrated digital joystick.)

Example:
```
print Joy_X () + ", " + Joy_Y (): run
```

### Joy_Button
`Joy_Button(index)` returns `true` if button index is currently being pressed, or `false` if isn't.

The first joystick button is index `0`. The second is index `1` e.t.c

Example:
```
dim i
for i = 0 to 9
  if joy_button (i) then print i: else print " ": endif
next
run
```

### Joy_Left, Joy_Right, Joy_Up, Joy_Down

`Joy_Left ()` returns `true` if the joystick is more than `100` units to the left. (This is equivalent to: `Joy_X () < -100`)

`Joy_Right ()` returns `true` if the joystick is more than `100` units to the right. (This is equivalent to: `Joy_X () > 100`)

`Joy_Up ()` returns `true` if the joystick is more than `100` units upwards. (This is equivalent to: `Joy_Y () < -100`)

`Joy_Down ()` returns `true` if the joystick is more than `100` units downwards. (This is equivalent to: `Joy_Y () > 100`)

### Joy_0, ..., Joy_9
There are also explicit functions for each joystick button from `0` through to `9`.

`Joy_0()` returns true if the first joystick button is being pressed. (This is equivalent to: `Joy_Button(0)`).

...

`Joy_9()` returns true if the 10th joystick button is being pressed. (This is equivalent to: `Joy_Button(9)`).

## Joystick polling
To "poll" the joystick means to take a snapshot of its current state, including the readings of the `X` and `Y` axis
and whether each button is up or down at the time of the poll.

Basic4GL automatically polls the joystick whenever one of the joystick functions is called,
so you don't have to tell it to explicitly.

For example:
```
while true: printr Joy_X() + " " + Joy_Y () + " " + Joy_0() + " " + Joy_1 (): wend
```

> [!TIP]
>
> Polling takes time (at least on older analogue joysticks).
>
> You may want to explicitly tell Basic4GL when to poll the joystick, in order to make the program run faster.
>
> It is more efficient to poll the joystick once, and then act on the `X` axis, `Y` axis and button data captured in that poll
> than to poll the joystick for each axis and button that you read.

### UpdateJoystick
`UpdateJoystick ()` polls the joystick and takes a snapshot of the `X` and `Y` axis and the state of all the buttons.

Any `Joy_?` calls will now return the data captured at the time of the `UpdateJoystick()` call.

For example:
```
while true: UpdateJoystick (): printr Joy_X() + " " + Joy_Y () + " " + Joy_0() + " " + Joy_1 (): wend
```

Now instead of reading the joystick 4 times each time around the loop, we are only reading it once.
This runs significantly faster than the previous example on my PC
(although my PC has an older analogue joystick attached to it.. I can't comment on digital joysticks.)

As soon as you call `UpdateJoystick()`, Basic4GL switches to explicit joystick updates,
and stays that way until your program finishes executing.

Therefore, you must keep calling `UpdateJoystick()` at the appropriate times to ensure the joystick data is up-to-date.

If you don't, the joystick will appear frozen, for example:
```
UpdateJoystick ()
while true: printr Joy_X() + " " + Joy_Y () + " " + Joy_0() + " " + Joy_1 (): wend
```

Here we have moved the `UpdateJoystick()` call out of the main loop, so it is only called once at the start of the program.

Because we don't ever call it again, each joystick functions will simply return the same value each time,
i.e. the state of the joystick at the start of the program when `UpdateJoystick()` was called.

So manual polling can be faster, but you must do it right!


## Credits
Basic4GL, Copyright (C) 2003-2007 Tom Mulgrew

_Programmer's guide_

26-Jul-2008
Tom Mulgrew

Documentation modified for Markdown formatting by Nathaniel Nielsen