
# Programmer's Guide: Text Output

## Basic text output
### Print
Basic text output is performed using the `Print` or `Printr` function.

Format:
```
Print text-parameters ;
```

Or:
```
Print text-parameters
```

Where text-parameters is a list of parameters, separated by semicolons (`;`).

`Print` leaves the cursor positioned after the last character printed.

`Printr` will automatically move the cursor to the start of the next line after the text has been printed.
If the cursor reaches the bottom of the screen, the text will scroll up the screen to make room for the new line.

Example:
```
Printr "Hello ";
Printr "and welcome to ";
Printr "Basic4GL"
Print
Print "Have a nice day"
```

#### Traditional BASIC syntax
`Print` behaves slightly differently when "traditional BASIC" syntax is enabled, or "Basic4GL with traditional print" syntax is enabled.

You can enable the "traditional BASIC" syntax by placing a
```
language traditional
```

line at the top of your program, or
```
language traditional_print
```

for just the print command syntax.

In this mode, the `print` command will move the cursor to the next line if it does not end with a trailing semicolon (`;`)

For example:

```
language traditional
print "Line 1"
print "Line 2"
print "Line 3"
```

Will print:
> Line1
>
> Line2
>
> Line3

If the `print` command does end with a semicolon, then the cursor will remain on the same line. So:
```
language traditional
print "Welcome ";
print "to ";
print "Basic4GL"
```

Will print:
> Welcome to Basic4GL

Thus, the `printr` command is not required in this syntax (but it is still available for compatibility sake).

### Locate
Locate positions the text cursor on the screen.

Format:
```
Locate X-position, Y-position
```

The Basic4GL text cursor is invisible. It determines to where on the screen `Print` and `Printr` will write.

By default, the Basic4GL displays 40 characters across by 25 characters down.

> [!TIP]
>
> This can be changed using the `ResizeText()` function.

- The topmost row is row 0.
- The leftmost column is column 0.

Example:
```
Dim d#
While True
Cls
Locate Sin (d#) * 15 + 18, 10
Print "Hello"
Sleep (100)
d# = d# + 0.1
Wend
```

### CursorCol, CursorRow
`CursorCol()` returns the column the cursor is on.

`CursorRow()` returns the row the cursor is on.

- The topmost row is row 0.
- The leftmost column is column 0.

### Color
Sets the text colour.

Format:
```
Color (red, green, blue)
```

Where `red`, `green` and `blue` are integers between `0` and `255` inclusive indicating the intensity of their respective colour component.

Once the text colour is set, any text printed will be in that colour until the text colour is changed.

Example:
```
dim t
TextMode (TEXT_BUFFERED)
while true
for t = 1 to 10: color (rnd()%255, rnd()%255, rnd()%255): print chr$(rnd()%255): next
DrawText ()
wend
```

### Cls
`Cls` clears all text from the screen and repositions the cursor to the top left.

### ClearLine
`ClearLine ()` clears the current line (the one which the cursor is on).

Example:
```
dim i
SetTextScroll (false)
for i = 0 to 24: printr i: next
locate 0, 10
ClearLine ()		' Line 10 is cleared
```

### ClearRegion
Clears a rectangular region of the screen.

Format:
```
ClearRegion (x1, y1, x2, y2)
```

Where `x1`, `y1`, `x2`, `y2` are integers that define the top left column and row (`x1`, `y1`)
and the bottom right column and row (`x2`, `y2`) of the rectangular region to be cleared.

Example:
```
dim x, y
SetTextScroll (false)
TextMode (TEXT_BUFFERED)
for y = 1 to TextRows ()
for x = 1 to TextCols ()
print "#"
next
next
ClearRegion (5, 5, 35, 9)
locate 13, 7: print "Cleared region"
DrawText ()
```

### TextRows, TextCols and ResizeText
`TextRows ()` returns the number of text columns.

`TextCols ()` returns the number of text rows.

`ResizeText (x, y)` resizes the text display to `y` rows by `x` columns and clears the text.

Example:
```
dim i, a$
a$ = "Basic4GL"
i = 100
while i >= 4
ResizeText (i * 2 + 1, i + 1)
Locate (TextCols() - Len(a$)) / 2, TextRows() / 2
Print a$
Sleep (50)
i = i - 2
wend
```

## Text scrolling
Advancing the cursor past the end of the line causes it to wrap around onto the next line.

Advancing the cursor past the end of the bottom-most line,
or performing a `Printr` on the bottom-most line causes the text to scroll up by one line.

Example 1:
```
Print glGetString (GL_EXTENSIONS)
```

Example 2:
```
dim d#
while true
locate sin(d#)*15+17, TextRows()-1
Printr "Hello"
Sleep (50)
d# = d# + 0.3
wend
```

Alternatively you can disable text scrolling with the `TextScroll` command.

### SetTextScroll
`SetTextScroll ()` enables or disables text scrolling when the cursor reaches the bottom of the text screen.

Format:
```
SetTextScroll (scroll)
```

Where `scroll` can equal `true` to enable text scrolling or `false` to disable it.

> [!IMPORTANT]
>
> Text scrolling is enabled by default.

Example:
```
SetTextScroll (false)
dim row
print "########################################"
for row = 2 to 24
print "#                                      #"
next
print "########################################"
```

### TextScroll
`TextScroll ()` returns `true` if text scrolling is enabled, or `false` if it isn't.

## Fonts
Basic4GL fonts are special transparent images, consisting of a 16 x 16 grid of characters.

You can set a new font by calling:

### Font (texture)

Where texture is an OpenGL texture handle (usually returned from `LoadTex()`).

Example:
```
printr "Normal font"
dim texture
texture = LoadTex("data\charset2.png")
Font (texture)
printr "charset2.png font"
```

To get the texture handle for the default font, call:
```
DefaultFont ()
```

Example:
```
dim texture
texture = LoadTex("data\charset2.png")
Font (texture)
printr "charset2.png font"
Font (DefaultFont ())
printr "Normal font"
```

## Text modes
Basic4GL has 3 different modes for rendering text on the screen. You choose one by executing the appropriate `TextMode()` call:

1. `TextMode (TEXT_SIMPLE)`
2. `TextMode (TEXT_BUFFERED)`
3. `TextMode (TEXT_OVERLAID)`

> [!IMPORTANT]
>
> The default mode is TEXT_SIMPLE.

In `TEXT_SIMPLE` mode, Basic4GL redraws the screen after each `Print`, `Printr`, `Cls` or `ResizeText()`.

This mode is easy to use, and the results are instant.

However, there are a number of situations where you may find it favourable to use `TEXT_BUFFERED`.

In `TEXT_BUFFERED` mode, Basic4GL does not update the screen until you call `DrawText ()`.
This has advantages if you are animating a large amount of text:

- Reduces flicker.
- The screen is only updated once all text has been drawn.
- Reduces screen resync delay.
- Depending on your video card and OpenGL settings, your OpenGL system may wait for vertical synchronization before every screen update.
- This can lead to unnecessarily slow animations in `TEXT_SIMPLE` mode, as Basic4GL must stop and wait for vertical resync after every `Print` statement. However, you must remember to call `DrawText()` or the user won't see any changes.

Example:
```
TextMode (TEXT_BUFFERED)
dim d#, t
while true
for t = 1 to 10
Locate sin(d#*t/19.0+t)*14+14,t*2+1
print " Thing "
next
DrawText ()
Sleep (10)
d# = d# + .1
wend
```

`TEXT_OVERLAID` mode is used to combine OpenGL graphics with text.
This mode is necessary if you wish to use OpenGL graphics commands and text at the same time.

This would cause problems in `TEXT_SIMPLE` or `TEXT_BUFFERED` mode, as both modes automatically clear the screen before rendering the text.

In `TEXT_OVERLAID` mode the `DrawText()` function will not clear the screen, or copy the result to the front buffer. It will simply render the current text transparently over the top of the current scene.
You must therefore manually clear the screen and swap it to the font buffer at the appropriate times.

The advantage of this mode is that it gives you a finer degree of control, and allows you to combine text and other graphics, such as OpenGL rendered objects.

Example:
```
TextMode (TEXT_OVERLAID)
locate 12, 12: print "This is a square"

dim a#
while true
glClear (GL_DEPTH_BUFFER_BIT or GL_COLOR_BUFFER_BIT)
glLoadIdentity ()
glTranslatef (0, 0, -2)
glRotatef (a#, 0, 0, 1)
glBegin (GL_QUADS)
glColor3f (1, 0, 0): glVertex2f ( 1, 1)
glColor3f (0, 1, 0): glVertex2f (-1, 1)
glColor3f (0, 0, 1): glVertex2f (-1,-1)
glColor3f (1, 1, 1): glVertex2f ( 1,-1)
glEnd ()
DrawText ()
SwapBuffers ()
a# = a# + 0.3
wend
```

### DrawText

Format:
```
DrawText()
DrawText(flags)
```
The `DrawText` command is used to draw text and/or sprites. The default (no parameter) version draws all text and sprites that are on the screen.

Alternatively you can control what it draws by passing it a bitmask composed of one or more of the following flags:

| Flag                 | Description                                   |
|----------------------|-----------------------------------------------|
| DRAW_TEXT            | Draw text                                     |
| DRAW_SPRITES_BEHIND  | Draw all sprites behind the text              |
| DRAW_SPRITES_INFRONT | Draw all sprites infront of the text          |
| DRAW_SPRITES         | Draw all sprites behind or infront of the text |

Example:
```
TextMode(TEXT_OVERLAID)
glDisable(GL_DEPTH_TEST)

' Create some bouncing balls
const ballcount = 100
dim tex = LoadTex("data/ball.png")
dim sprites(ballcount), i

for i = 1 to ballcount
  sprites(i) = NewSprite(tex)
  if rnd()%2 then SprSetZOrder(-1) endif
  SprSetPos(rnd() % 640, rnd() % 480)
  if rnd()%2 then SprSetXVel(1) else SprSetXVel(-1) endif
  if rnd()%2 then SprSetYVel(1) else SprSetYVel(-1) endif
next

do
  ' Clear the screen background            
  glBegin(GL_QUADS)
  glColor3f(.5, 0, 0)
  glVertex3f(-10,  10, -5)
  glVertex3f( 10,  10, -5)
  glColor3f(0, 0, .5)
  glVertex3f( 10, -10, -5)
  glVertex3f(-10, -10, -5)
  glEnd()
  
  ' Draw behind sprites and small text
  ResizeText(80, 50)
  locate 35, 20: print "Small text"
  DrawText(DRAW_SPRITES_BEHIND or DRAW_TEXT)

  ' Draw large text and infront sprites
  ResizeText(20, 12)
  locate 6, 7: print "Big text"
  DrawText(DRAW_TEXT or DRAW_SPRITES_INFRONT)
  ' Show completed frame
  SwapBuffers()
             
  ' Animate bouncing balls
  while SyncTimer(10)
    AnimateSprites()
    for i = 1 to ballcount
      BindSprite(sprites(i))
      if SprX() < 0 or SprX() > 640 then
        SprSetXVel(-SprXVel())
      endif
      if SprY() < 0 or SprY() > 480 then
        SprSetYVel(-SprYVel())
      endif
    next
  wend
loop
```

## Reading from the screen

### CharAt$
`CharAt$(x, y)` returns the character at column `x` and row `y`.

Example:
```
TextMode(TEXT_BUFFERED)

dim d#, t, x, y, crash: crash = false: x = TextCols()/2

while not crash
  for t = 1 to 5: locate sin(d#+t)*15+15,t*2+2: print" Thing! ": next
  
  y=y-1
  
  if y<0 then
    y = TextRows()-1: cls
  else
    if ScanKeyDown(VK_LEFT) and x > 2 then x = x - 1 endif
    if ScanKeyDown(VK_RIGHT) and x < 36 then x = x + 1 endif
  
    crash = CharAt$(x,y)<>" "
    locate x, y: print"X"
  endif
  
  DrawText()
  
  WaitTimer (80)
  d# = d#+0.06
wend
```

## Credits
Basic4GL, Copyright (C) 2003-2007 Tom Mulgrew

_Programmer's guide_

26-Jul-2008
Tom Mulgrew

Documentation modified for Markdown formatting by Nathaniel Nielsen