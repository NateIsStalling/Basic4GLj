# Programmer's Guide: Standard Functions

General purpose math, string, and timing functions.

## General purpose functions
These functions are used for general purpose operations, such as mathematics equations and string manipulation.

### abs
Abs(x) returns the absolute value of x.

### arraymax
`ArrayMax(array)` returns the index of the highest element of array.

Iterating elements `0..ArrayMax(array)` will therefore visit every element inside the array.
`ArrayMax` is a special function in that array can be any type, so long as it is an array.

### asc
`Asc(x)` takes a single string parameter `x`, and returns the ASCII value of the first character.

This is the opposite of the `chr$` function

### atn
`Atn(x)` returns the Arc Tangent value of `x`, in radians.

### atnd
`Atnd(x)` returns the Arc Tangent value of `x`, in degrees.

### atn2
`Atn2(x, y)` returns the Arc Tangent value of `x`, `y`, in radians.

### atn2d
`Atn2(x, y)` returns the Arc Tangent value of `x`, `y`, in degrees.

### beep
`Beep()` causes the computer to beep.

> [!IMPORTANT]
>
> `Beep()` does nothing in the current version of Basic4GLj

### chr$
`Chr$(x)` takes a single integer parameter `x`, and returns a string character whose ASCII value is `x`.

Example:
```
Printr Chr$(72)+Chr$(101)+Chr$(108)+Chr$(108)+Chr$(111)
```

### cos
`Cos(x)` returns the Cosine of `x`, where `x` is measured in radians.

### cosd
`Cosd(x)` returns the Cosine of `x`, where `x` is measured in degrees.

### exp
`Exp(x)` returns `e` raised to the power of `x`.

`Exp` is the inverse of `Log`.

### int
`Int(x)` casts a real valued x to an integer.

> [!IMPORTANT]
>
> The rounding is slightly different to the implicit type cast when a real value is assigned to an integer.
>
> `Int(x)` rounds `x` towards negative infinity, whereas implicit type casting always rounds towards `0`.

Example:
```
dim a#, i1, i2: a# = -5.1
i1 = a#
i2 = Int(a#)
printr "i1 = " + i1
printr "i2 = " + i2
```

### left$
`Left$(s,c)` returns a string containing the first `c` characters of `s`.
`s` is a string value, `c` is an integer value.

For example, `Left$("ABCDEFG", 3)` returns `ABC`

### lcase$
`LCase$ (x)` returns `x` converted to lowercase.

### len
`Len(x)` returns the length of the string `x` in characters.

### log
`Log(x)` returns the natural logarithm of `x`.

`Log` is the inverse of `Exp`.

### mid$
`Mid$(s,i,c)` returns a string containing `c` consecutive characters of string `s`, starting from the `i`th character.

For example, `Mid$("ABCDEFG", 4, 3)` returns `"DEF"`.

### performancecounter
`PerformanceCounter()` returns the number of milliseconds that have elapsed since the computer was turned on.

This function is very similar to `TickCount()`,
except `PerformanceCounter()` is accurate to _1 millisecond_ whereas `TickCount()` is only accurate to _10ms_.

Therefore, I strongly recommend using `PerformanceCounter()` for any timing operations.

The old `TickCount()` function is retained only for backwards compatibility with existing Basic4GL programs.

### pow
`Pow(x,y)` returns `x` raised to the power of `y`.

### right$
`Right$(s,c)` returns a string containing the last `c` characters of `s`.

For example, `Right$("ABCDEFG", 3)` returns `"EFG"`

### rnd
`Rnd()` returns a random integer value, between `0` and `RND_MAX`.
(`RND_MAX = 32767`, but could be different in future ports of Basic4GL to different platforms or operating systems.)

> [!IMPORTANT]
>
> The Basic4GL for Java port uses `RND_MAX = 32767` for random behavior compatibility with previous versions of Basic4GL.

To return a random number between `0` and `x-1` (inclusive), use:
```
Rnd() % x
```

To return a random number between `1` and `x` (inclusive), use:
```
Rnd() % x + 1
```

### sgn
`Sgn(x)` returns:

- `1`, if `x` is greater than `0`
- `0`, if `x` equals `0`
- `-1`, if `x` is less than `0`

### sin
`Sin(x)` returns the Sine of `x`, where `x` is measured in radians.

### sind
`Sind(x)` returns the Sine of `x`, where `x` is measured in degrees.

### sqr
`Sqr(x)` returns the square root of `x`.

(Actually the square root of the absolute value of `x`.)

### sqrt
`Sqrt(x)` is exactly the same as `Sqr(x)`

### str$
`Str$(x)` converts an integer value `x` into a string representation of `x`.

For example, `Str$(-13.4)` returns `"-13.4"`.

### tan
`Tan(x)` returns the Tangent of `x`, where `x` is measured in radians.

### tand
`Tand(x)` returns the Tangent of `x`, where `x` is measured in degrees.

### tanh
`Tanh(x)` returns the Hyperbolic Tangent of `x`, where `x` is measured in radians.

### tickcount
`TickCount()` returns the number of milliseconds that have elapsed since the computer was turned on.

> [!NOTE]
>
> This function is only accurate to about 10ms. I strongly advise using `PerformanceCounter()` instead.

### ucase$
`UCase$ (x)` returns `x` converted to uppercase.

### val
`Val(x)` converts a string `x` into a numeric value.
If `x` cannot be converted into a number, then `Val(x)` returns `0`.

For example, `Val("27.2")` returns `27.2`.

`Val` is the opposite of `Str$`.

## Timing
### Sleep
Pauses execution for a number of milliseconds.

Format:
```
Sleep (milliseconds)
```

> [!NOTE]
>
> The application is completely unresponsive while sleeping.
>
> Therefore, Basic4GL will not sleep for more than 5000 milliseconds (5 seconds) at a time.

To sleep for more than 5 seconds, use a loop.

For example:
```
Dim i
For i = 1 to 60: Sleep (1000): Next
Will pause for 60 seconds, but still give the user the opportunity to break out of the program if he/she wishes.
```

## WaitTimer, SyncTimer and ResetTimer

### WaitTimer

This function is similar to `Sleep`, and indeed has the same format:
```
WaitTimer (milliseconds)
```

The difference is that `WaitTimer` waits until `milliseconds` milliseconds has elapsed from the previous `WaitTimer` call.

This difference is significant if `WaitTimer` is used inside an animation loop,
with other code that may take some time to execute (such as rendering a frame).

For example:
```
While true
Draw a frame
WaitTimer (100)
Wend
```

If _Draw a frame_ were to take `40` milliseconds, then `WaitTimer` will pause for only `60` milliseconds,
ensuring that the loop is correctly iterated `10` times a second.

Even simple animations can potentially take up to the resync period of the monitor (anything from 1/100th to 1/50th of a second),
if the user's graphics card is configured to wait for retrace before drawing.

### SyncTimer
`SyncTimer` returns true if you need to update the internal state of the application to catch up to the clock.

This can be used to force an animation to update internally so many times per second, regardless of a PC's rendering speed, and is intended to be used as follows:
```
While main-loop-condition
Render scene
While SyncTimer (delay)
Update state
Wend
```

For example, if delay was `10` milliseconds, then Update state will execute `100` times per second,
regardless of whether the computer is capable of rendering `20` or `100` frames per second.

Example:
```
dim x, y, a#, b#
while true
glClear (GL_DEPTH_BUFFER_BIT or GL_COLOR_BUFFER_BIT)
glLoadIdentity ()
glTranslatef (0, 0, -16)
glRotatef (a#, 0, 0, 1)
for y = -5 to 5: for x = -5 to 5
glPushMatrix ()
glTranslatef (x * 3, y * 3, 0)
glRotatef ((x + y) * 60 + b#, 1, 0, 0)
glBegin (GL_QUADS)
glColor3f (1, 0, 0): glVertex2f ( 1, 1)
glColor3f (0, 1, 0): glVertex2f (-1, 1)
glColor3f (0, 0, 1): glVertex2f (-1,-1)
glColor3f (1, 1, 1): glVertex2f ( 1,-1)
glEnd ()
glPopMatrix ()
Next: Next
SwapBuffers ()
while SyncTimer (10)
a# = a# + 0.9: b# = b# + 3.6
wend
wend
```

## Credits
Basic4GL, Copyright (C) 2003-2007 Tom Mulgrew

_Programmer's guide_

26-Jul-2008
Tom Mulgrew

Documentation modified for Markdown formatting by Nathaniel Nielsen