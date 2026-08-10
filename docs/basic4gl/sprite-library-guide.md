

# Sprite library guide
This document details Basic4GL's integrated sprite library and various routines.

## Sprite routines
Basic4GL's contains an integrated sprite library, designed to simplify the process of writing 2D sprite based games and applications.
Internally sprites are drawn using OpenGL similar to Basic4GL's text mechanism. However - as with text - no OpenGL experience is needed to use the sprite routines.

Basic4GL sprites support:

- Scaling & rotation
- Colours & transparency
- Animated sprites
- Z order
- Tile maps (a 2D grid of tiles)
- Parallax scrolling

#### A simple example of a Basic4GL sprite program:

```
dim texture, sprite
texture = LoadTex("data\ball.png")
sprite = NewSprite (texture)
SprSetVel (vec2 (2, 2))
SprSetPos (100, 100)
locate 13, 12: print "Bouncing ball"

while true
  AnimateSprites ()
  if SprLeft () < 0 or SprRight () > SpriteAreaWidth () then
    SprSetXVel (-SprXVel ())
  endif
  if SprTop () < 0 or SprBottom () > SpriteAreaHeight () then
    SprSetYVel (-SprYVel ())
  endif
wend
```

> [!TIP]
>
> There are also some larger examples supplied with Basic4GL.
> See AsteroidDemo2.gb (and compare it to AsteroidDemo.gb), and CavernDemo.gb.

## Integration with the text system
The Basic4GL sprite engine is built on top of the Basic4GL text engine, and shares some its mechanism and functions.
The sprites and text are redrawn at the same time, which by default is whenever either the text on the screen changes, or a change is made to a sprite.

Also, if you switch the text mode to buffered mode (using `TextMode (TEXT_BUFFERED)`),
sprites are automatically switched to buffered mode also, and will only be drawn when `DrawText ()` is called.

You may think it strange to have text commands controlling when sprites are drawn, and you're probably right!
The reason is text support was implemented first, and so the functions were named `TextMode()` and `DrawText()`, instead of (maybe) `TextAndSpriteMode()` and `DrawTextAndSprites()`.
When sprite support was added, I chose not to rename the functions, in order to maintain backward compatibility with existing Basic4GL code.

See the `TextMode()` and `DrawText()` definitions (in the **"Text Output"** section) for more information.

## Loading textures
> [!WARNING]
>
> The following commands are deprecated as of Basic4GL language version 2.5.6:
> - LoadTexture
> - LoadMipmapTexture
> - LoadImageStrip
> - LoadMipmapImageStrip
> - ImageStripFrames
>
> These commands are still available, so that old Basic4GL programs will still compile,
> however you are advised to use the `LoadTex`, `LoadTexStrip` and `TexStripFrames` commands instead.

> [!NOTE]
>
> Basic4GLj currently supports functions deprecated in Basic4GL language version 2.5.6.

Basic4GL sprites are drawn using OpenGL textures. So in order to display a sprite, you must first load the texture into OpenGL and then assign it to the sprite.

### LoadTex()
The easiest way to load a single OpenGL texture is with the `LoadTex()` function.

For example:
```
' Load texture
dim texture
texture = LoadTex("data\star.bmp")          ' Load texture and return handle

' Create sprite
dim sprite                                  
sprite = NewSprite (texture)                ' Create sprite, and assign texture
SprSetPos (320, 240)
```

`LoadTex()` loads a texture into OpenGL, and returns the OpenGL texture "name" (an integer that identifies the texture).

You can then pass that "name" to a sprite, in order to create a sprite that displays that texture.

### TexStripFrames and LoadTexStrip
Basic4GL also supports animated sprites.

These require multiple OpenGL textures (one texture for each animation frame) which are passed to the sprite as an array of OpenGL texture "name"s.
You could achieve this by having multiple image files and loading them all-in-one by one.
This is a bit clumsy however, so Basic4GL supports the concept of "image strips".

An image strip is a single image, that contains multiple subimages.

> [!TIP]
>
> Have a look at the "explode.png" image in the "Programs\Data" folder if you need an example.

Basic4GL provides routines to load such an image, chop it up into the separate subimages and upload them into OpenGL as separate textures.

`LoadTexStrip()` will do all of the above and return an array of OpenGL texture handles that can be passed to a Basic4GL sprite.

Format:
```
LoadTexStrip(filename [, frameWidth, frameHeight])
```

The `frameWidth` and `frameHeight` are optional. If not specified, Basic4GL will use the width or height of the image (whatever is smaller).
This means that if all the frames are on one row in the image file, and they are square, you do not need to specify what size the frames are.
Frame widths and heights will usually be powers of 2 (1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, ...), so that they can be loaded into an OpenGL texture.

If they are not, Basic4GL will automatically scale each frame to a suitable size before loading it into an OpenGL texture.
The scaling routine is primative and works by either dropping or duplicating rows or columns.

> [!TIP]
>
> For better results, use an image editor (Gimp, Photoshop etc) to scale the image beforehand so that the frames sizes are a power of 2.
> These programs have more sophisticated image scaling algorithms, and the result will generally look better.

Example:
```
dim sprite
sprite = NewSprite(LoadTexStrip("data\explode.png"))
SprSetPos (320, 240)
SprSetAnimSpeed (1)

while true
  WaitTimer (100)
  AnimateSprites ()
wend
```

Example 2:
```
dim sprite
sprite = NewSprite(LoadTexStrip("data\spacetiles.png", 32, 32))
SprSetPos (320, 240)
SprSetAnimSpeed (1)

while true
  WaitTimer (100)
  AnimateSprites ()
wend
```

To calculate the number of frames in an image strip, use `TexStripFrames()`.

Format:
```
TexStripFrames(filename [, frameWidth, frameHeight])
```

This returns the number of images in the image strip (and is useful if you want to assign them to an array, e.g. to be shared between multiple sprites).

```
' Load images
dim &explodeFrames ()
alloc explodeFrames, TexStripFrames("data\explode.png") - 1
explodeFrames = LoadTexStrip("data\explode.png")

' Create sprites
dim sprites (10), i
for i = 1 to 10
  sprites (i) = NewSprite (explodeFrames)
  SprSetPos (rnd () % int (SpriteAreaWidth ()), rnd () % int (SpriteAreaHeight ()))
  SprSetScale (5)
  SprSetAnimSpeed (rnd () % 10 * .1 + .5)
next

' Main loop
while true
WaitTimer (100)
AnimateSprites ()
wend
```

## Advanced texture loading options
There are also a number of options that affect the way Basic4GL handles images when they are loaded.

By default Basic4GL:

- Does not treat any one colour as transparent (images that contain transparency information, like PNG files, are still treated correctly however)
- Automatically generates "mipmap" textures for each texture it loads
- Removes blank frames from the end of texture strips
- Sets up each texture to use linear filtering
- Basic4GL has a number of routines that change this behaviour. These routines affect all subsequent LoadTex and LoadTexStrip calls until the program ends.

### SetTexTransparentCol
`SetTexTransparentCol` specifies a colour to treat as transparent.
Basic4GL will replace all pixels of this colour with transparent pixels when it loads the texture.

Format:
```
SetTexTransparentCol(red, green, blue)
SetTexTransparentCol(colour)
```

Where `red`, `green`, and `blue` are integers representing the intensity of the corresponding colour component. `0` = minimum, `255` = maximum.
Or colour is an integer calculated as: `colour = red * 65536 + green * 256 + blue`

Example:
```
' Replace black with transparent pixels
SetTexTransparentCol(0, 0, 0)

' Load textures
dim textures(TexStripFrames("data\spacetiles.png") - 1) = LoadTexStrip("data\spacetiles.png")

' Build tile map
data 0, 0, 0, 1, 0
data 0, 1, 5, 0, 2
data 2, 3, 0, 0, 4
data 0, 0, 1, 2, 0
data 1, 4, 3, 0, 0

dim tiles(4)(4), x, y

for y = 0 to 4: for x = 0 to 4: read tiles(x)(y): next: next

' Create tile map sprite
dim tile = NewTileMap(textures)
SprSetSolid(false)
SprSetTiles(tiles)

' Animate over a blue background
glClearColor(0, .1, .3, 1)

while true
  SprSetPos(SprPos() + vec2(2, 1))
wend
```

### SetTexNoTransparentCol
`SetTexNoTransparentCol()` sets Basic4GL back to its original behaviour, where no colour is treated as transparent.

### SetTexIgnoreBlankFrames
By default `LoadTexStrip()` will automatically detect blank frames at the end of the texture strip and remove them.
A frame is considered blank if all its pixels are fully transparent,
or if all its pixels match the current transparent colour (if one is set).

You can switch this behaviour off with:
```
SetTexIgnoreBlankFrames(false)
```

In this case blank frames will be loaded in and stored in the texture array.

To re-enable this behaviour, use:
```
SetTexIgnoreBlankFrames(true)
```

### SetTexMipmap
By default, whenever Basic4GL loads a texture with `LoadTex` or `LoadTexStrip`, it will create corresponding mipmap textures.

These are smaller versions of the texture that will automatically be used when the texture is squeezed into a smaller number of pixels.
This will usually look better than trying to draw the original texture scaled down, which can introduce visual artifacts such as "moire patterns".
However, the mipmap textures do take up a third more texture memory than a non mipmap texture on its own.

You can disable creation of mipmap textures with:
```
SetTexMipmap(false)
```

To re-enable it, use:
```
SetTexMipmap(true)
```

Example:
```
' Create a sprite with a mipmapped texture
SetTexMipmap(true)
dim sprite1 = NewSprite(LoadTex("data/cube.bmp"))
SprSetPos(160, 240)
SprSetSpin(1)

' Create a sprite with the same texture, no mipmapping
SetTexMipmap(false)
dim sprite2 = NewSprite(LoadTex("data/cube.bmp"))
SprSetPos(480, 240)
SprSetSpin(1)

locate 7, 10: print "Mipmap"
locate 25, 10: print "No mipmap"

' Spin textures to show difference
while true
  AnimateSprites()
  WaitTimer(50)
wend
```

### SetTexLinearFilter
Linear filtering controls how a texture is drawn when it is magnified.
- When linear filtering is enabled (the default) texture pixels are interpolated making magnified textures appear smooth.
- When linear filtering is disabled, no interpolation takes place, and magnified texture pixels appear as distinct rectangles.

To disable linear filtering, use:
```
SetTexLinearFilter(false)
```

To re-enable it, use:
```
SetTexLinearFilter(true)
```

Example:
```
' Create a sprite with linear filtering
SetTexLinearFilter(true)
dim sprite1 = NewSprite(LoadTex("data/asteroid.png"))
SprSetSize(300, 300)
SprSetPos(160, 240)
SprSetSpin(1)
' Create a sprite with no linear filtering
SetTexLinearFilter(false)
dim sprite2 = NewSprite(LoadTex("data/asteroid.png"))
SprSetSize(300, 300)
SprSetPos(480, 240)
SprSetSpin(1)

locate 4, 1: print "Linear filter"
locate 22, 1: print "No linear filter"

' Spin textures to show difference
while true
  AnimateSprites()
  WaitTimer(50)
wend
```

Note, loading a texture with linear filtering has exactly the same effect as configuring the texture with OpenGL commands:
```
glBindTexture(GL_TEXTURE_2D, texture)
glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
```

Loading a texture without linear filtering has exactly the same effect as configuring it with:
```
glBindTexture(GL_TEXTURE_2D, texture)
glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
```

## Creating sprites and deleting sprites
Sprites can be created with `NewSprite()` or `NewTileMap()`.
For now, we will only concentrate on regular sprites (and the `NewSprite()` function).
Tile maps will be explained in the tile map section further below.

### NewSprite
`NewSprite()` creates a single sprite and returns a handle to it.

Format:
```
NewSprite ()
NewSprite (texture)
NewSprite (textureArray)
```
Where:
- `texture` is an OpenGL texture.
- `textureArray` is an array of OpenGL textures.

All 3 different formats return a sprite handle. This is a number that identifies the sprite, and is used to access it and manipulate it.
- If texture is specified, then the texture is loaded into the sprite.
- If textures is specified, then the array of textures is loaded into the sprite.
- If nothing is specified, then no textures are associated with the sprite, and you will need to call one of the texture setting functions to add some.

### DeleteSprite
(Applies to sprites and tile maps.)

DeleteSprite can be used to free a sprite once you have finished with it.

Format:
```
DeleteSprite (spriteHandle)
```
Where `spriteHandle` is a sprite handle returned from `NewSprite()` (or `NewTileMap ()`).

### ClearSprites
`ClearSprite` will delete all sprites and tile-maps from the screen.

Format:
```
ClearSprites()
```

## Binding sprites
To manipulate a sprite, you must first "bind" it. This works much in the same way as OpenGL texture binding.
Once bound, all sprite functions operate on that sprite until the binding is changed to another sprite.

`NewSprite()` and `NewTileMap()` automatically bind the sprite they have just created.

Otherwise, you need to call `BindSprite()` to set the currently bound sprite.

### BindSprite
(Applies to sprites and tile maps.)

Sets the currently bound sprite. All sprite changes from there on will be applied to the sprite until another sprite is bound.

Format:
```
BindSprite (spriteHandle)
```

Where `spriteHandle` is a sprite handle returned from `NewSprite()` (or `NewTileMap ()`).

## Setting the sprite texture

### SprSetTexture, SprSetTextures
(Applies to sprites and tile maps.)

Loads a texture or set of textures into a sprite.

Format:
```
SprSetTexture (texture)
SprSetTextures (textureArray)
```
Where:
- `texture` is an OpenGL texture.
- `textureArray` is an array of OpenGL textures.

The `texture` or `textureArray` will completely replace any textures already in the sprite.

### SprAddTexture, SprAddTextures
(Applies to sprites and tile maps.)

Adds a texture or textures to a sprite.

Format:
```
SprAddTexture (texture)
SprAddTextures (textureArray)
```

Where:
- `texture` is an OpenGL texture.
- `textureArray` is an array of OpenGL textures.

The `texture` or textures in `textureArray` are added to the end of any existing textures in the sprite.

## Sprite properties
You animate and move sprites around by setting various sprite properties, such as their position, angle, colour e.t.c.
For each property there is usually one or more functions to set the property, and one or more to get (read) the property from the sprite.

The "Set" functions always operate on the bound sprite.

The "Get" functions usually come in 2 forms. One that operates on the bound sprite, and one that is passed a sprite handle to operate on.
(The second form is useful if you want to copy properties from one sprite to another, or set one sprite's properties dependent on those of another sprite.)

### SprSetPos, SprSetX, SprSetY, SprPos, SprX, SprY
(Applies to sprites and tile maps.)

These functions are used to move sprites around.

Format:
```
SprSetPos (vector)
SprSetPos (x, y)
SprSetX (x)
SprSetY(y)
```
```
SprPos () / SprPos (spriteHandle)
SprX () / SprX (spriteHandle)
SprY () / SprY (spriteHandle)
```

Where:
- `vector` is a real numbered 2D vector. (e.g `dim vec#(1)` ).
- `x` is a real number representing the sprite's horizontal position.
- `y` is a real number representing the sprite's vertical position.

By default, the top left corner of the screen is at x = 0, y = 0, and the bottom right is at x = 640, y = 480.

> [!NOTE]
>
> Increasing `y` corresponds to the down direction,
> unlike most OpenGL configurations in which increasing `y` corresponds to up.

You can change the dimensions of the sprite area by calling `ResizeSpriteArea()` (which works much the same as `ResizeText()`).

The position that the sprite appears in is also dependent on the "sprite camera". (See the `SprCamera` functions for more information.)

### SprSetZOrder, SprZOrder
(Applies to sprites and tile maps.)

Format:
```
SprSetZOrder (zOrder)
```
```
SprZOrder () / SprZOrder (spriteHandle)
```
Where:
- `zOrder` is a real number.

The Z order is a real valued number, stored with each sprite which determines whether it will appear in front or behind other sprites.
Smaller values (or more negative values) appear in-front of larger values.

Also, any sprite with a Z order of 0 or greater will appear behind any text on the screen,
and any sprite with a Z order less than 0 will appear in-front of the text.

When parallax scrolling is switched on for a sprite or tile map (using `SprSetParallax()`),
the Z order affects whether it appears far away and scrolls slowly (positive Z order values)
or close and scrolls quickly (negative Z order values).

If a sprite has a Z order of 0, switching on parallax scrolling will have no effect.

### SprSetSize, SprSetXSize, SprSetYSize, SprXSize, SprYSize
(Applies to sprites and tile maps.)

These functions set the size of the sprite, or in the case of tilemaps, the size of each tile.

Format:
```
SprSetSize (vector)
SprSetSize (xSize, ySize)
SprSetXSize (xSize)
SprSetYSize (ySize)
```
```
SprXSize () / SprXSize (spriteHandle)
SprYSize () / SprYSize (spriteHandle)
```
> [!NOTE]
>
> The default sprite (and tile) size is 32 x 32.

### SprSetXCentre, SprSetYCentre, SprXCentre, SprYCentre
(Applies to sprites and tile maps.)

Format:
```
SprSetXCentre (xCentre)
SprSetYCentre (yCentre)
```
```
SprXCentre () / SprXCentre (spriteHandle)
SprYCentre () / SprYCentre (spriteHandle)
```
Used to specify the centre of the sprite. This is the point that is lined up to the sprite's position, and is also where the sprite rotates around.
By default, this is (.5, .5) for regular sprites (corresponding to the centre of the sprite) and (0, 0) for tile maps (corresponding to the top left corner).

A value of 1 corresponds to a single sprite with or height, or a single tile width or height (for tile maps).

### SprSetScale, SprScale
(Applies to sprites and tile maps.)

Format:
```
SprSetScale (scaleFactor)
```
```
SprScale () / SprScale (spriteHandle)
```
Where:
- `scaleFactor` is a real number.

Used to scale a sprite. Alternatively you could just multiply the X and Y size to achieve exactly the same effect.
The default scale is 1.

### SprSetXFlip, SprSetYFlip, SprXFlip, SprYFlip
> [!IMPORTANT]
>
> Applies to sprites only!

Format:
```
SprSetXFlip (xFlip)
SprSetYFlip (yFlip)
```
```
SprXFlip () / SprXFlip (spriteHandle)
SprYFlip () / SprYFlip (spriteHandle)
```

Where:
- `xFlip` and `yFlip` are integer values. If the value is true, the sprite is flipped in that direction.

### SprLeft, SprTop, SprRight, SprBottom
> [!IMPORTANT]
>
> Applies to sprites only!

These are readonly functions that return the effective position of the edges of the sprite, taking into account position, size, scaling and sprite's centre position.

Format:
```
SprLeft () / SprLeft (spriteHandle)
SprRight () / SprRight (spriteHandle)
SprTop () / SprTop (spriteHandle)
SprBottom () / SprBottom (spriteHandle)
```

These functions all return a real value, corresponding to the horizontal coordinate (`SprLeft` & `SprRight`)
or vertical coordinate (`SprTop` & `SprBottom`) of the corresponding edge of the sprite.

These functions are useful for primitive collision detection, or bouncing sprites against the edge of the screen.

### SprSetAngle, SprAngle
(Applies to sprites and tile maps.)

Used to rotate sprites.

Format:
```
SprSetAngle (angle)
```
```
SprAngle () / SprAngle (spriteHandle)
```
Where:
- `angle` is a real number specifying the number degrees to rotate the sprite by in the anticlockwise direction.

### SprSetVisible, SprVisible
(Applies to sprites and tile maps.)

Used to show / hide a sprite.

Format:
```
SprSetVisible (visible)
```
```
SprVisible () / SprVisible (spriteHandle)
```

Where:
- `Visible` is an integer value, set to `true` to show the sprite or `false` to hide it.

(By default sprites are made visible when created.)

### SprSetFrame, SprFrame
> [!IMPORTANT]
>
> Applies to sprites only!

Once multiple textures are loaded into a sprite, you can animate it by setting the sprite frame.

Format:
```
SprSetFrame (frameNo)
```
```
SprFrame () / SprFrame (spriteHandle)
```

Where:
- `frameNo` is the frame number to display. The first loaded frame is 0.

### SprSetColor, SprColor
(Applies to sprites and tile maps.)

Used to set the colour of the sprite, and (optionally) it's transparency.

Format:
```
SprSetColor (colourVec)
SprSetColor (red, green, blue)
SprSetColor (red, green, blue, alpha)
```
```
SprColor () / SprColor (spriteHandle)
```

Where:
- `colourVec` is a 3D or 4D vector (array) of real values.
- `red`, `green`, `blue` are real values representing the red, green and blue intensities of the colour respectively (`0` = no intensity, `1` = full intensity)
- `alpha` is a real value representing how transparent the object is (`0` = fully transparent / invisible, `1` = fully solid).

If the alpha component isn't specified, it defaults to `1`.

If the sprite is set to solid (`SprSetSolid()`) then it will always be drawn solid, and alpha has no effect.

Tile maps, by default are solid. Regular sprites are transparent.

### SprSetAlpha, SprAlpha
(Applies to sprites and tile maps.)

Used to set just the alpha component of the sprite's colour.

Format:
```
SprSetAlpha (alpha)
```
```
SprAlpha () / SprAlpha (spriteHandle)
```

Where:
- `alpha` is a real value representing how transparent the object is (`0` = fully transparent / invisible, `1` = fully solid).

See `SprSetColor()` above for more info on alpha and sprite transparency.

### SprSetBlendFunc
(Applies to sprites and tile maps.)

Allows you to change the blending mode of a sprite. This is an advanced function, used to enable special types of transparency.

Format:
```
SprSetBlendFunc(sfactor,dfactor)
```

Where `sfactor` and `dfactor` are OpenGL blending function constants for the source and destination pixels respectively.

The function accepts the same constants as the OpenGL `glBlendFunc` function.

The default blending function is `sfactor = GL_SRC_ALPHA`, `dfactor = GL_ONE_MINUS_SRC_ALPHA`.

Example:
```
dim stars(10), i, tex
tex = LoadTex("data/star.bmp")
for i = 1 to 10
  stars(i) = NewSprite(tex)
  SprSetSize(500, 500)
  SprSetColor((rnd() % 1001) / 1000.0, (rnd() % 1001) / 1000.0, (rnd() % 1001) / 1000.0)
  SprSetPos(rnd() % int(SpriteAreaWidth()), rnd() % int(SpriteAreaHeight()))
  SprSetVel(((rnd() % 2001) - 1000) / 1000.0, ((rnd() % 2001) - 1000) / 1000.0)
  SprSetBlendFunc(GL_SRC_ALPHA, GL_ONE)
next

do
  AnimateSprites()
  for i = 1 to 10
    BindSprite(stars(i))
    if SprX() < 0 or SprX() > SpriteAreaWidth() then
      SprSetXVel(-SprXVel())
    endif
    if SprY() < 0 or SprY() > SpriteAreaHeight() then
      SprSetYVel(-SprYVel())
    endif
   next
loop
```

### SprSetSolid, SprSolid
(Applies to sprites and tile maps.)

Specifies whether a sprite is solid or transparent.

Format:
```
SprSetSolid (solid)
```
```
SprSolid () / SprSolid (spriteHandle)
```

Where:
- `solid` is an integer value. `True` = solid, `false` = transparent.

Solid sprites and tiles are drawn as solid rectangular blocks. Transparent ones use the transparency information stored inside the textures (if any) plus the alpha value.

By default tile maps are solid, and regular sprites are transparent.

### SprSetParallax, SprParallax
(Applies to sprites and tile maps.)

Format:
```
SprSetParallax (parallax)
```
```
SprParallax () / SprParallax (sprHandle)
```

Where:
- `parallax` is an integer value. `True` = parallax scrolling, `false` = regular scrolling.

Parallax scrolling uses the Z order information to create a parallax scrolling effect.
Positive Z order values correspond to further away objects, which are displayed as smaller, and scroll slower.
Negative Z order values correspond to closer objects, which are displayed as larger and scroll faster.

## Sprite animation
You can animate sprites by updating the above properties manually.
However, to simplify things, there are also a number of properties that you can set on a sprite in order to have it animate automatically.

These include:
- Velocity
- Spin
- Animation speed (animating through frames)

These properties can only be set on regular sprites! They do not apply to tile maps.

Once set, these properties will automatically update the sprite every time `AnimateSprites()` is called.

### AnimateSprites

Format:
```
AnimateSprites()
```

Updates each sprite, adding velocity to position, spin to angle and animation speed to frame number.

### AnimateSpriteFrames()

Format:
```
AnimateSpriteFrames()
```

Like updates each sprite, adding animation speed to frame number.
Similar to AnimateSprites, but updates only the animation frames. Sprites do not move or rotate.

### SprSetVel, SprSetXVel, SprSetYVel, SprVel, SprXVel, SprYVel
> [!IMPORTANT]
>
> Applies to sprites only!

Format:
```
SprSetVel (vector)
SprSetVel (x, y)
SprSetXVel (x)
SprSetYVel (y)
```
```
SprVel () / SprVel (spriteHandle)
SprXVel () / SprXVel (spriteHandle)
SprYVel () / SprYVel (spriteHandle)
```

Where:
- `vector` is a 2D real valued vector.
- `x` is a real value representing the X component of the velocity.
- `y` is a real value representing the Y component of the velocity.

A sprite's velocity is added to its position every time `AnimateSprites()` is called, to make the sprite move.

The default velocity is (0, 0).

### SprSetSpin, SprSpin
> [!IMPORTANT]
>
> Applies to sprites only!

Format:
```
SprSetSpin (spin)
```
```
SprSpin () / SprSpin (spriteHandle)
```
Where:
- `spin` is a real number value.

A sprite's spin is added to its angle every time `AnimateSprites()` is called, to make a sprite spin.

### SprSetAnimSpeed, SprAnimSpeed
> [!IMPORTANT]
>
> Applies to sprites only!

Format:
```
SprSetAnimSpeed (speed)
```
```
SprAnimSpeed () / SprAnimSpeed (spriteHandle)
```

Where:
- `speed` is a real number value.

A sprite's animation speed is added to it's frame every time `AnimateSprites()` is called, to make a sprite animate through its frames.

A sprite can be set to animate once, or animate in a loop (`SprSetAnimLoop()`).

### SprSetAnimLoop, SprAnimLoop
> [!IMPORTANT]
>
> Applies to sprites only!

Format:
```
SprSetAnimLoop (loop)
```
```
SprAnimLoop () / SprAnimLoop (spriteHandle)
```
Where:
- `loop` is an integer value. `True` = sprite animates in a loop. `False` = sprite animates once then stops.

By default, sprites animate in a loop.

### SprAnimDone
> [!IMPORTANT]
>
> Applies to sprites only!

Format:
```
SprAnimDone () / SprAnimDone (spriteHandle)
```

`SprAnimDone ()` returns true if a sprite has completed its animation (i.e. has reached the last frame.)
(Obviously this does not apply to looped animations, as they never finish!)

Tile map routines
> [!TIP]
> See CavernDemo.gb for a simple demo of tile maps in action.

In addition to regular sprites, Basic4GL supports a special type of sprite called a "Tile map".

A tile map is a 2D grid. Each grid contains a number, which indicates the index of the image that is to be displayed at that point in the grid.
Tile maps are an efficient way of representing large images, often many times higher and wider than the actual screen size.
They are typically used for backgrounds in 2D games like platformers and side scrollers.

Tile maps are implemented as a special kind of sprite, so many of the functions that operate on general sprites
will also work on tile maps (e.g setting position, size, scale, colour, angle, transparency).
Although not all of them though, so be aware that there are functions that will only operate on regular sprites (and not tile maps), and vice-versa.
The type of sprites that a function will operate on is displayed in red underneath each function description.

## Creating tile maps

### NewTileMap
Tile maps are created with `NewTileMap()`. This has the same format as `NewSprite()`.

Format:
```
NewTileMap ()
NewTileMap (texture)
NewTileMap (textureArray)
```
Where:
- `texture` is an OpenGL texture.
- `textureArray` is an array of OpenGL textures.

Each of the 3 functions returns a handle to the new tile map object, that can be used to manipulate it later.

Like regular sprites, tile maps need to be given textures to indicate what is to be displayed.
You can either pass these textures to the `NewTileMap()` function,
or add them later with the `SprSetTexture(s)` or `SprAddTexture(s)` functions.

When a tile map is created, it is automatically "bound" (see `BindSprite()` for more info.)

## Setting up tiles
Once a tile map has been created and has some textures, you need to specify the tiles.
This is the actual 2D grid of numbers, where each number indicates which texture is to be displayed at that point,
where 0 indicates the first texture loaded.

You setup the grid as a 2D array of integers, and then pass the array to the tile map using SprSetTiles().

### SprSetTiles
> [!IMPORTANT]
>
> Applies to tile maps only!

Format:
```
SprSetTiles (tilesArray) / SprSetTiles (spriteHandle, tilesArray)
```

Where:
- `tilesArray` is a 2D array of integers.

This loads the tiles into the tile map. Once the tile map has textures and tiles, it is ready to display.

Example:
```
data 1, 1, 1, 1
data 1, 0, 0, 1
data 1, 0, 0, 1
data 1, 1, 1, 1

' Read in tiles
dim tiles(3)(3), x, y, tileMap
for y = 0 to 3
for x = 0 to 3
read tiles (x)(y)
next
next

' Create tilemap
tileMap = NewTileMap (LoadTexStrip("data\cavernTiles.png", 32, 32))
SprSetTiles (tiles)
```

Note: By default tilemaps repeat infinitely in the horizontal and vertical directions.

You can now move the tilemap around just like any regular sprite with the standard sprite routines.

### SprXTiles, SprYTiles

> [!IMPORTANT]
>
> Applies to tile maps only!

Occasionally it is useful to know the number of tiles a tile map has across or down. To achieve this you can use the `SprXTiles` or `SprYTiles` functions.

Format:
```
SprXTiles () / SprXTiles (spriteHandle)
SprYTiles () / SprYTiles (spriteHandle)
```

- `SprXTiles()` returns the number of tiles across (horizontally).
- `SprYTiles()` returns the number of tiles down (vertically).

### SprSetXRepeat, SprSetYRepeat, SprXRepeat, SprYRepeat

> [!IMPORTANT]
>
> Applies to tile maps only!

Format:
```
SprSetXRepeat (xRepeat)
SprSetYRepeat (yRepeat)
```
```
SprXRepeat () / SprXRepeat (spriteHandle)
SprYRepeat () / SprYRepeat (spriteHandle)
```

Where:
- `xRepeat` and `yRepeat` are integer values, set to `true` to repeat the tile map infinitely along the given dimension, and false to disable repeating.

(By default X and Y repeat are on, when the tile map is created.)

## Copying sprites
You can copy on sprite to another using `CopySprite()`. The target sprite will then become an identical copy of the original.

### CopySprite
(Applies to sprites and tile maps.)

Format:
```
CopySprite (spriteHandle)
```

All the properties of the `spriteHandle` sprite are copied to the currently bound sprite, making the two completely identical.

> [!NOTE]
>
> While you can copy a regular sprite to a regular sprite or a tile map to another tile map,
> you cannot copy a tile map to a sprite or vice-versa.

Distinguishing Sprite types
You can distinguish a regular sprite from a tile map using the SprType() function.

### SprType
(Applies to sprites and tile maps.)

Format:
```
SprType () / SprType (spriteHandle)
```

This returns an integer constant, which will be:

- `SPR_SPRITE` if the sprite is a regular sprite
- `SPR_TILEMAP` is the sprite is a tile map
- `SPR_INVALID` if the sprite handle does not correspond to a regular sprite or tile map.

## Setting the sprite area size
You can resize the sprite area, just like you can resize the text area.

The default sprite area size is 640 x 480, but you can pretty much set it to anything you want.
All sprites and tile maps will be scaled accordingly.

### ResizeSpriteArea
Format:
```
ResizeSpriteArea (width, height)
```

Where:
- `width` and `height` are real numbers corresponding to the width and height of the sprite area respectively.

### SpriteAreaWidth, SpriteAreaHeight
Format:
```
SpriteAreaWidth()
```
```
SpriteAreaHeight()
```

These functions return the respective width and height of the sprite area (as set by the most recent `ResizeSpriteArea()` call).

Useful for finding the screen boundaries.

## The Sprite Camera
Basic4GL has the concept of a sprite "camera". You can use this to create scrolling games,
by setting up your sprites to move around in an area bigger than the screen size, and moving the camera around it.

This is also an easy way to setup parallax scrolling.

By enabling parallax mode (`SprSetParallax()`) and setting the Z order to a positive number (`SprSetZOrder()`),
you can cause background sprites and tile maps to appear smaller, and move slower in response to camera movement.

Example:
```
TextMode (TEXT_BUFFERED)

data  1, 1, 1, 1
data -1,-1,-1, 1
data -1,-1,-1, 1
data -1,-1,-1, 1

' Read in tiles
dim tiles(3)(3), x, y, tileMap, tileMap2, tileMap3
dim textures (TexStripFrames ("data\cavernTiles.png", 32, 32) - 1)
for y = 0 to 3
for x = 0 to 3
read tiles (x)(y)
next
next

' Load textures
textures = LoadTexStrip("data\cavernTiles.png", 32, 32)

' Create tilemaps
tileMap = NewTileMap (textures)
SprSetTiles (tiles)

tileMap2 = NewTileMap (textures)
SprSetTiles (tiles)
SprSetColor (.75, .5, .5)
SprSetZOrder (100)
SprSetParallax (true)
SprSetAngle (45)

tileMap3 = NewTileMap (textures)
SprSetTiles (tiles)
SprSetColor (.25, .25, .5)
SprSetZOrder (200)
SprSetParallax (true)

while true
while SyncTimer (10)
SprCameraSetPos (SprCameraPos () + vec2 (2, .5))
wend
DrawText ()
wend
```

### SprCameraSetPos, SprCameraSetX, SprCameraSetY, SprCameraPos, SprCameraX, SprCameraY
Used to move the sprite camera around. All sprites and tilemaps are drawn in relation to the sprite camera.

Format:
```
SprCameraSetPos (positionVector)
SprCameraSetX (xPosition)
SprCameraSetY (yPosition)
```

```
SprCameraPos ()
SprCameraX ()
SprCameraY ()
```

Where:
- `positionVector` is a 2D vector of real values (e.g dim camVec# (1)).
- `xPosition` and `yPosition` are real values representing the camera's X and Y position respectively.

> [!NOTE]
> The default camera position is (0, 0).

### SprCameraSetAngle, SprCameraAngle
As well as moving the camera, you can also rotate it.
All sprites and tile maps are then drawn rotated in relation to the rotation of the camera.

Format:
```
SprCameraSetAngle (angle)
```
```
SprCameraAngle ()
```

Where:
- `angle` is a real number value representing the new camera angle in degrees.

Positive angles rotate the camera anticlockwise, and give the effect of rotating all the sprites and tile maps clockwise.

### SprCameraSetFov, SprCameraFov
You can also set the field of view used for the parallax scrolling effect.

Format:
```
SprCameraSetFov (fov)
```
```
SprCameraFov ()
```

Where:
- `fov` is a real number value representing the new camera field-of-view in degrees.

The field-of-view must be at least 1 degree wide and no more than 175 degrees.

Altering the camera's field of view has no effect on sprites or tile maps which are not in parallax mode.

### SprCameraSetZ, SprCameraZ
Format:
```
SprCameraSetZ (zPosition)
```
```
SprCameraZ ()
```

Where:
- `zPosition` is a real number value representing the Z position of the camera.

This can be used to set the sprite camera's Z position. It affects parallax sprites and tile maps only.
Positive values of zPosition make the camera appear to move forward. Negative values of zPosition make the camera appear to move backwards.

Example:
```
TextMode (TEXT_BUFFERED)

data 1, -1, -1, -1
data 1, -1, -1, -1
data 1, -1, -1, -1
data 1, -1, -1, -1
dim tiles(3)(3), x, y
for y = 0 to 3: for x = 0 to 3: read tiles (x)(y): next: next

dim textures(TexStripFrames("data\cavernTiles.png", 32, 32) - 1)
textures = LoadTexStrip("data\cavernTiles.png", 32, 32)

const numLayers = 5
dim layers(numLayers), i
for i = 1 to numLayers
layers(i) = NewTileMap (textures)
SprSetTiles (tiles)
SprSetYRepeat (false)
SprSetZOrder (i * 100)
SprSetY (240 - 64)
SprSetSolid (false)
SprSetParallax (true)
next

while true
SprCameraSetZ (SprCameraZ () + 1)
DrawText ()
wend
```

## Credits
Basic4GL, Copyright (C) 2003 Tom Mulgrew

26-Jul-2008
Tom Mulgrew

Documentation modified for Markdown formatting by Nathaniel Nielsen