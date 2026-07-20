# Basic4GL Language Guide
This document is aimed at experienced programmers and describes the basic syntax of Basic4GL programs.

This document focuses on the language itself, and as such does not go into the individual functions and constants,
or how they are intended to be used.

## Basic4GL Overview
Basic4GL is designed to combine a simple, safe and easy to understand programming language based on traditional BASIC with the OpenGL graphics library,
so that programmers can experiment and learn OpenGL and beginning programmers can learn about programming in general.

The downside is that Basic4GL cannot compete with programs compiled to native machine code (e.g. from a C++ compiler).
But this was never the intention.

Basic4GL compiles programs to byte code, which it runs on a virtual machine.
This makes Basic4GL a safe language to experiment in as the virtual machine protects the programs from writing
to invalid addresses or jumping to uninitialised code,
and handles cleaning up resources such as OpenGL textures automatically.

In addition, the Basic4GL virtual machine automatically handles certain setup tasks
such as creating an OpenGL capable window (and initialising OpenGL state), handling windows messages,
and buffering keyboard input.

Basic4GL programs do not need to initialise OpenGL windows, link to libraries, include header files or declare function prototypes.
This means you can cut through all the paperwork and get straight to the code that does the actual work.

The following examples are complete Basic4GL programs.

#### Example 1, A "Hello world" program:

```
print "Hello world!"
```

#### Example 2, drawing a square in OpenGL:

```
glTranslatef (0, 0, -5)
glBegin (GL_QUADS)
glVertex2f ( 1, 1): glVertex2f (-1, 1): glVertex2f (-1,-1): glVertex2f ( 1,-1)
glEnd ()
SwapBuffers ()
```

## BASIC Language Syntax
As of Basic4GL language version 2.3.2, Basic4GL supports a new "traditional BASIC" syntax.

> [!NOTE]
> Basic4GLj is based on Basic4GL 2.5

This syntax is intended to be more compatible with other BASIC compilers,
to make porting code between them and Basic4GL a little bit easier,
and to make programming in Basic4GL a little easier for people who are used to other BASIC compilers.

The new syntax must be explicitly enabled, otherwise Basic4GL will simply use the standard Basic4GL syntax.
You do this by placing the following command at the top of your program:

```
language traditional
```

Basic4GL also accepts:

```
language basic4gl
```

Which will switch the compiler to the standard Basic4GL syntax. (Although it's not really necessary, as this is the default syntax anyway.)

And also:
```
language traditional_print
```

Which is a tradeoff between the standard Basic4GL syntax, except with a more traditional `print` command syntax.

Syntax differences
The differences between the "Traditional BASIC" and old "Basic4GL" syntax are listed below:

"Traditional" BASIC	Basic4GL
Functions have round brackets only if they return a value.
Examples:

```
a = rnd()%5
sleep 1000
locate 10, 12
glVertex3f -5, 12, 2
print sqrt(2)
```

All functions have round brackets except for `cls`, `print`, `printr` and `locate`

Examples:

```
a = rnd()%5
sleep(1000)
locate 10, 12
glVertex3f(-5, 12, 2)
print sqrt(2)
```

If a `print` command ends with a semicolon (`;`) the cursor will remain on the same line.
Otherwise, the cursor will move to the next line.

Example:

```
print "============"
print " Tom's game"
print "============"
print
print "Please enter your name:";
```

The cursor always remains on the same line after `print`.
To have the cursor move to the next line, use the `printr` command instead.

Example:

```
printr "============"
printr " Tom's game"
printr "============"
printr
print "Please enter your name:"
```

When dividing two integers, they will automatically be converted to floating point first.

Examples:

```
print "5 goes into 12"; int(5/12); "times"
print "10/8 = "; 10/8
a = 3: b = 4: c# = a / b
```

When dividing two integers, integer division is used, and the remainder is discarded.

Examples:

```
printr "5 goes into 12 "; 5/12; " times"
printr "10/8 = "; 10.0 / 8
a = 3: b = 4: c# = (a * 1.0) / b
```

## Syntax documentation
The syntax documented in these help files is the standard Basic4GL syntax.

When other syntaxes differ, the differences will be described in their corresponding **Compatibility with other BASICs** section.

## "Include" files

Basic4GL supports a very simple `include` mechanism.
You can include a file in your main program with:
```
include filename.ext
```

Where `filename.ext` is the filename and extension of the file you wish to include.

> [!IMPORTANT]
>
> "include" must be on its own line, with no leading spaces before the `include` keyword.

When keyed in correctly the line will become highlighted, and `filename.ext` will be displayed as an underlined hyperlink (which you can click to open up the include file).

Basic4GL will compile your file as if all the lines of `filename.ext` had been cut and pasted in at the point of the include.

> [!IMPORTANT]
>
> "include" is not supported by the runtime compilation functions ("Compile()" and "CompileFile()").

## Basic language features

### Comments

Comments are designated with a single quote.

All text from the quote to the end of the line are ignored by the compiler.
```
' Program starts here
dim a 	'Declare a variable
a = 5 	'Initialise to a value
print a 'Print it to screen
```

Is equivalent to:

```
dim a
a = 5
print a
```

### Case insensitivity

Basic4GL is a case insensitive language. This applies to all keywords and variable names, and infact anything except the contents of string constants.

The following lines are all equivalent:

```
GLVERTEX2F (X, Y)
glVertex2f (x, y)
glvertex2f(X, Y)
```

The following lines are not equivalent:

```
print "HELLO WORLD"
print "Hello World"
print "hello world"
```

(because the "Hello world"s are quoted strings).

### Separating instructions
Instructions are separated by colons `:` or new-lines.

The following code sample:

```
dim a$: a$ = "Hello": print a$
```

Is equivalent to:

```
dim a$
a$ = "Hello"
print a$
```

## Variables and data types

Basic4GL supports only 3 basic data types (although they can be combined into structures which are described further on).

| Data Type | Description                    |
|-----------|--------------------------------|
| Integer   | A 32 bit signed integer.       |
| Real      | A 32 bit floating point value. |
| String    | A character string.            |

Variables are declared and allocated explicitly with the `Dim` instruction.
Attempting to use a variable without declaring it with `Dim` will result in a compiler error.

A naming convention is used to designate the type of each variable, as follows:

String variables are post-fixed with a `$` character, for example:

```
Dim a$
a$ = "Hello world"
```

Real variables are post-fixed with a `#` character, for example:

```
Dim value#
value = 1.2345
```

Integer variables are not post-fixed, for example:

```
Dim index
index = 10
```

### Declaring variables (with Dim)

All variables must be declared with `Dim` before use.

The format is:

```
Dim variable [, variable [, ...]]
```

For example:

```
Dim a
Dim name$
Dim a, b, c
Dim xOffset#, yOffset#
Dim ages(20)
Dim a, b, c, name$, xOffset#, yOffset#, ages(20)
```

`Dim` is both a declaration to the compiler that the keyword is to be treated as a variable, and an executed instruction.
Therefore, the `Dim` instruction must appear before the variable is used.

This program:
```
a = 5
Dim a
```
Results in a compiler error, because the compiler encounters `a` in an expression before it is declared with `Dim`.

This program:

```
goto Skip
Dim a
Skip:
a = 5
```

Compiles successfully but results in a run time error, as it attempts to write to `a` before the `Dim` instruction has executed,
and therefore no storage space has yet been allocated for it.

The correct example is (of course):

```
Dim a
a = 5
```

#### Compatibility with other BASICs
Basic4GL also supports the syntax:

```
Dim variable as type
```

Where type can be one of:

- `integer`
- `string`
- `single`
- `double`

> [!NOTE]
>
> Basic4GL has only one floating point type which is a single precision float (ie a `single`). The `double` keyword is still accepted for compatibility, but Basic4GL still allocates a single precision floating point number.

### Allocating variable storage
Storage space is allocated when the `Dim` instruction has been executed.
In addition, Basic4GL automatically initialises the data as follows:

- Integers and reals are initialised to `0`.
- Strings are initialised to the empty string `""`.

### Re-Dimming a variable
Attempting to `Dim` the same variable twice results in a runtime error.
There is currently no way to re-dim a variable. However, this may be included in a future version of Basic4GL.

### Array variables
Basic4GL supports single and multi-dimensional arrays. These are "Dim"med by specifying the array variable name, followed by a number in round brackets.

Basic4GL will allocate elements from indices `0`, through to and including the value specified in the brackets.

Examples:

```
Dim a$(10)
Dim size#(12)
const MaxThings = 12
Dim ThingHeight# (MaxThings), ThingWidth#(MaxThings)
dim count: count = 10
Dim array(count), bigArray (count * 10)
```

For arrays of more than one dimension, each dimension is specified in its own pair of brackets.

Examples:

```
Dim matrix#(3)(3)
matrix#(2)(3) = 1
const width = 20, height = 15
Dim grid(width)(height)
```

Is mentioned, Basic4GL allocates elements from indices `0`, through to and including the value specified in the brackets.

For example:
```
Dim a(3)
```

Will allocate four integers, named `a(0)`, `a(1)`, `a(2)` and `a(3)`, and set their values to `0`.

Basic4GL arrays are sized at runtime. You can use any (expression that can be cast to an integer) to specify the number of elements.

> [!CAUTION]
>
> However, keep in mind that Basic4GL will stop with a runtime error if you attempt to allocate array:
>- With an array size of less than 0, OR
>- That uses more memory than the Basic4GL memory limit.

Basic4GL arrays can be copied by specifying the array name without any brackets or indices. The target array must be the same size as the copied array, otherwise a runtime error will result.

Examples:
```
Dim a$(4), b$(4)
...
b$ = a$ ' Copy entire array from a$ to b$
```

Likewise, some functions accept arrays as parameters, or return them as results:
```
Dim matrix#(3)(3)
matrix# = MatrixTranslate (-.5, -.5, -2)
glLoadMatrixf (matrix#)
glBegin (GL_TRIANGLES)
glVertex2f (0, 0): glVertex2f (1, 0): glVertex2f (0, 1)
glEnd ()
SwapBuffers ()
```

If you specify just one dimension of a 2D array, the result is a 1D array,
which can be assigned to/from variables or passed to to/functions like any other 1D array of the same type.

Example:
```
dim vectors# (12)(3), temp#(3)
temp# = vectors# (4)
```

Likewise, specifying N dimensions of an M dimension array results in a (M - N) dimension array.

#### Compatibility with other BASICs

Basic4GL also supports the syntax:

```
Dim variable(dimension [,dimension [...]])
```

For multidimension arrays.

E.g.
```
dim grid(20, 10)
grid (3, 7) = 12
```

Is exactly equivalent to:
```
dim grid(20)(10)
grid (3)(7) = 12
```

**Why not automatically allocate variables?**

Early designs of Basic4GL were intended to allocate variables automatically the first time they were encountered.
However, Basic4GL is case-insensitive, and OpenGL uses long constants for bitmasks and flags.

Therefore, mistyping (or miss-spelling) a constant in an OpenGL function call such as:

```
glClear (GL_DEPTH_BUFER_BIT) ' Missing an "F" in "BUFFER"
```

Would have resulted in a code that still compiles, but instead of passing the value of `GL_DEPTH_BUFFER_BIT` into the function, Basic4GL would have created a new variable called `GL_DEPTH_BUFER_BIT`, initialised the value to `0`, and then passed `0` into the function.
This type of error can be very confusing and frustrating, especially when learning a library such as OpenGL.

Therefore, variables must be explicitly declared with `Dim`.

### Converting between data types
You can convert a variable, or an expression value to a different type, simply by assigning it to a variable of that type, providing the conversion type is one of the ones below:

- Integer -> Real
- Real -> Integer
- Integer -> String
- Real -> String

Certain expression operators such as `+`, `-`, `*`, `/` can also result in an automatic conversion of either the left or right operand to match the other, using the following rules:

- If one operand is a string, the other operand is converted to a string before the operation is performed.
- If one operand is a real and the other is an integer, the integer is converted to a real before the operation is performed.

### Literal constants
To use a literal integer in a Basic4GL program, simply specify the integer value.

Examples:

```
Dim a: a = 5
Dim a: a = -5
```

Likewise, to use a literal real:

```
Dim a#: a# = 3.14159265
```

Literal integers can also be specified in hexadecimal using the `0x` prefix.

Examples:

```
Dim a: a = 0xff
Dim a: a = -0xff
```

To use a literal string, simply encase the string in double quotes.

For example:

```
Dim helloString$: helloString$ = "Hello world!"
```

Basic4GL does not support literal prefix notations, such as `\n` for newline in C/C++.
You can however use the Chr$() function to achieve the same effect, for example:

```
Dim a$: a$ = "Bob says " + Chr$(34) + "Hello!" + Chr$ (34)
Print a$
```

Will output:

> Bob says "Hello!"

### Named constants
Basic4GL also has a number of named constants, such as `M_PI` and `GL_CULL_FACE`.

> [!TIP]
>
> For a complete list, click "Help > Function and Constant list..." and click the "Constants" tab.

> [!NOTE]
>
> Two commonly used constants are `true` and `false`, which evaluate to `-1` and `0` respectively.

You can add constants using the `Const` instruction.

The format is:
```
Const name = value [, name = value [, ...]]
```

Where:

`name` is the name of the constant, and follows the same naming conventions as standard variables, (including `#` and `$` suffixes for real and string constants respectively).
`value` is a literal constant, another named constant, or a constant expression (defined below)
For example:

```
const Things = 20
const Max = 100, Min = 1
const StepCount = 360, StepSize# = 2 * m_pi / StepCount
const major = 3, minor = 7, version$ = major + "." + minor
```

### Constant expressions
Certain instructions require constant expressions, such as the `const` instruction (described above), and the `step` part of the `for..next` instruction.
These expressions must always evaluate to the same value and Basic4GL must be able to calculate this value at the time the program is compiled.

An expression must satisfy these criteria to be considered "constant" by Basic4GL:

- The expression must contain only literal constants or named constants.
- These constants can only be combined with the standard operators:
  `+`, `-`, `*`, `/`, `%`, `=`, `<>`, `>`, `>=`, `<`, `<=`, `or`, `and`, `not`.

Examples:

```
-12
22.4
m_pi
m_pi / 180
true and not false
"banana"
"banana " + "split"
"Pi = " + m_pi
```

Are all valid constant expressions

Expressions are not considered constant if they contain variables or functions. This holds even for expressions that (to a human) are obviously constant.
For example:

```
sqrt (2)
length (vec3 (1, 1, 1))
```

Are not valid constant expressions in Basic4GL, even though it is clear to us that they will always evaluate to the same value.

### Structures

Structures are used to group related information together into a single "data structure".
The format is as follows:

```
Struc strucname

dim field [, field [,...]]
[dim field [,[field [,...]]]
[...]

EndStruc
```

Example:

```
struc SPlayer
dim pos#(1), vel#(1)
dim dir#, lives, score, deadCounter, inGame
dim leftKey, rightKey, thrustKey, shootKey
dim wasShooting
endstruc
```

This defines a data storage format. You can now allocate variables of the new structure type by using a special format of the `Dim` instruction:

```
Dim strucname variablename
```

Examples:

```
Dim SPlayer player
const maxPlayers = 10
Dim SPlayer players (maxPlayers)
```

Each variable now stores all the information described in the structure. You can access these individual fields using the `.` operator as follows:

```
structurename.fieldname
```

For example:

```
player.pos#(0) = 12.3
players (4).score = players (4).score + 10
i = 3
print players (i).lives
```

You can also assign variables of the same structure type to one another. This will copy all the fields from one variable to the other.

Example:

```
player (7) = player (6)
```

#### Compatibility with other BASICs

Basic4GL also supports the syntax:
```
Type typename

variable as type [, variable as type [...]]
[...]

End type
```

E.g.
```
struc SpaceMartian
dim name$
dim x#, y#
dim health(4)
endstruc
```

Is equivalent to:
```
type SpaceMartian
name as string
x, y as single
health(4) as integer
end type
```

(Except that in the first example the field names now have `$` and `#` post-fixes.)

### Arrays inside structures
Structures can contain arrays. Unlike regular arrays, the size of an array in a structure must be fixed at compile time.
This means that the array size must be either a numeric constant, or a named constant, or a constant expression.

For example:
```
struc STest: dim a(10): endstruc
const size = 20
struc STest2: dim array$(size): endstruc
```

Will work.

However, this example:
```
dim size: size = 20
struc STest2: dim array$(size): endstruc
```

Will cause a compile time error, because size is now a variable and is not fixed at compile time.
(Even though it's obvious to a human that it will always be 20!)

### Pointers

Basic4GL has a pointer syntax which is vaguely similar to C++'s `reference` type, but a lot more simplified.

#### Declaring pointers
Pointers are declared by prefixing a `&` character before the variable name in the `Dim` statement.
The syntax is then the same as "Dim"ming a regular variable, except that array dimensions must be specified with `()` (i.e with no number in the brackets).

So whereas:
```
Dim i, r#, a$, array#(10), SomeStructure s, matrix#(3)(3)
```

Declares and allocates:
- An integer named "i"
- A real named "r#"
- A string named "a$"
- An array of reals named "array#"
- A structure of type "SomeStructure" named "s"
- A 2D array of reals named "matrix#"

```
Dim &pi, &pr#, &pa$, &parray#(), SomeStructure &ps, &pmatrix#()()
```
Declares:
- An pointer to an integer named "pi"
- A pointer to a real named "pr#"
- A pointer to a string named "pa$"
- A pointer to an array named "parray#"
- A pointer to a structure of type "SomeStructure" named "ps"
- A pointer to a 2D array of reals named "pmatrix#"

#### Setting pointers

Pointer variables are initially unset. Attempting to read or write to the data of an unset pointer results in a runtime error. To do anything useful you need to point them to a variable, otherwise known as "set"ting them.

Pointers are set using this syntax:
```
&pointer = &variable
```

Examples:
```
Dim a$, &ptr$
a$ = "Hello world"
&ptr$ = &a$
print ptr$
Dim array(10), &element, i
for i = 1 to 10: &element = &array(i): element = i: next
dim matrix#(3)(3), &basisVector#(), axis, i
matrix# = MatrixIdentity ()
print "Axis? (0-3): ": axis = Val (input$ ()) ' Enter 4 to crash!
&basisVector# = &matrix# (axis)
for i = 0 to 3: print basisVector# (i) + " ": next
```

#### Accessing pointer data

Once a pointer is set, it can be accessed like any other variable, i.e. read, assigned to, passed to functions e.t.c.
The actual data read from or written to will be that of the variable that it is pointing to.
```
Dim a, b, &ptr
&ptr = &a
a = 5 ' a is 5, b is 0
b = ptr ' a is 5, b is 5
ptr = b + 1 ' a is 6, b is 5
print "a = " + a + ", b = " + b
```

#### Un-setting pointers

You can "un-set" a pointer by assigning it the special constant `null`, as follows:
```
Dim val, &ptr
&ptr = &val ' Pointer now set
&ptr = null ' Pointer now un-set
```

You can also compare a pointer to `null`.

```
if &ptr = null then

    ...

endif
if &ptr <> null then

    ...

endif
```

### Mixing structures, arrays and pointers
You can mix structures, arrays and pointers mostly in any way you wish.
There are a few limitations to keep in mind however:

You cannot allocate an array of pointers, as:
```
Dim &ptrs()
```
will allocate a pointer to an array.

If you really need an array of pointers you can use the following workaround:

```
struc SPtr: dim &ptr: endstruc
dim SPtr array (100)
```

Then you can set the pointers using:
```
&array (5).ptr = &var
```
(or similar.)

## Allocating data
Basic4GL supports a very simple memory allocation scheme. Memory once allocated is permanent (until the program finishes).
There is no concept of freeing a block of allocated memory! (Note: While this has some obvious limitations, it does prevent a large number pointer related bugs. Keep in mind that Basic4GL was never intended to be the next C++...)

Data is allocated as follows:
```
alloc pointername [, arraysize [, arraysize [...]]]
```

Where `pointername` is the name of a Basic4GL pointer variable DIMmed earlier.

Examples:
```
dim &ptri
alloc ptri ' Allocate an integer
dim &ptrr#
alloc ptrr# ' Allocate a real numer
dim &ptrs$
alloc ptrs$ ' Allocate a string
struc SPlayer: dim x, y, z: endstruc
dim SPlayer &ptrplayer
alloc ptrplayer ' Allocate a player structure
```
Basic4GL allocates a variable of the type that `pointername` points to, and then points `pointername` to the new variable.

To allocate an array, add a comma, and list the dimension sizes separated by commas.

Examples:

```
dim &ptrarray () ' Array size is not specified here!
alloc ptrarray, 100 ' Specified here instead!
dim &ptrMatrix#()()
alloc ptrMatrix, 3, 3
```

As with DIMming arrays, specifiying N as the array size will actually create N+1 elements: 0 through to N inclusive.
Also, the array size is calculated at runtime, and is subject to the same rules as DIMming an array
(size must be at least 0 e.t.c).

## Expressions

### Operators

Basic4GL evaluates infix expressions with full operator precedence.

In most loosely to most tightly bound order:

| Operator | Description                                                                                                          | Example                 |
|----------|----------------------------------------------------------------------------------------------------------------------|-------------------------|
| or       | 	Bitwise or                                                                                                          | 	a# < 0 or a# > 1000    |
| and      | 	Bitwise and                                                                                                         | 	a# >= 0 and a# <= 1000 |
| xor      | 	Bitwise exclusive or                                                                                                | 	a = a xor 255          |
| lor      | 	Bitwise lazy or                                                                                                     | 	a# < 0 lor a# > 1000   |
| land     | 	Bitwise lazy and                                                                                                    | 	a# >= 0 and a# <= 1000 |
| not      | 	Bitwise not                                                                                                         | 	not a# = 5             |
| =        | 	Test for equal <br> _= can also be used to compare pointers of the same type, or to compare pointers to null._      | 	a# = 5                 |
| <>       | 	Test for not equal <br> _<> can also be used to compare pointers of the same type, or to compare pointers to null._ | a# <> 5                 |
| \>       | 	Test for greater than	a > 10                                                                                        |                         |
| \>=      | 	Test for greater or equal	a# >= 0                                                                                   |                         |
| <        | 	Test for less than	a# < 9.5                                                                                         |                         |
| <=       | Test for less or equal	a <= 1000                                                                                     |                         |
| +        | 	Add numeric values, or concatenate strings                                                                          |                         |
| -        | 	Subtract                                                                                                            |                         |
| *        | 	Multiply                                                                                                            |                         |	 
| /        | 	Divide	                                                                                                             |                         |
| %        | 	Remainder                                                                                                           |                         |
| -        | (with single operand)	Negate	                                                                                        | a * -b                  |

Notes:

`+` and `-` have equal precedence (except when minus is used to negate a single value).
The comparison operators: `=`, `<>`, `>`, `>=`, `<`, `<=` all have equal precedence.
Operators with equal precedence are evaluated from left to right.

You can force Basic4GL to evaluate expressions in a different order by enclosing parts of them in round brackets. For example:

```
(5 + 10) / 5
```

Will add 5 to 10, then divide the result by 5 (giving 3), whereas:

```
5 + 10 / 5
```

Will divide first, then add, and the resulting value will be 7.



> [!TIP]
>
> Operators generally operate on standard integer, real and to a lesser extent string types.
> However certain operators have been extended to work with 1D and 2D arrays of real numbers for vector and matrix functions.
>
> These are explained in the **Programmer's Guide**.

Also, the `=` and `<>` operators can also be used to compare pointers to each other, or to compare pointers to `null`.

### Expression operands

An expression operand can be any of the following:

- A variable. E.g. `a$`
- An array variable. E.g. `x# (index)`
- A literal constant. E.g. `3.14159265`
- A named constant. E.g. `M_PI`
- A function result. E.g. `Sqrt (2)`

### Boolean values and expressions
Basic4GL stores boolean values as integers, where `0` is `false` and anything non `0` is `true`.

The comparison operators `<`, `<=`, `=`, `>=`, `>`, and `<>` all evaluate to `-1` if the comparison is `true` or `0` if it is `false`.

The `and` and `or` operators perform a bitwise "and" or "or" of the respective operands.

Effectively this means that `and` and `or` can be used in both boolean expressions and bit manipulation.

Boolean example:
```
If a < 0 or a > 10 Then Print "Out of range": Endif
```

Bitwise example:
```
glClear (GL_DEPTH_BUFFER_BIT or GL_COLOR_BUFFER_BIT)
```

### Lazy evaluation
Basic4GL supports lazy evaluation through the `land` and `lor` operators.
Here "lazy" means that Basic4GL will stop evaluating a boolean (`true`/`false`) expression as soon as it knows what the result will be.

For example, the expression:
```
age# < 15 land not accompanied_by_adult
```

will not even evaluate `not accompanied_by_adult` if `age#` were set to `42` (for example),
because Basic4GL already knows that `age# < 15` evaluates to `false` and therefore the whole expression will evaluate to `false`.

Besides the lazy behaviour, `land` is exactly equivalent to `and` and `lor` is exactly equivalent to `or`.

Proper use of lazy evaluation can make your programs more efficient,
and can be useful in situations where evaluating all of the expression may produce undesirable results.

For example:
```
if i >= 0 and i <= 10 and array(i) = searchValue then
```
could halt your program with an "Array index out of range" error if `i` happened to be `11` (assuming `array` is a 0..10 element array).
Whereas:
```
if i >= 0 land i <= 10 land array(i) = searchValue then
```
will not halt your program, because `array(i)` is only ever evaluated if `i >= 0` and `i <= 10` have already evaluated to true.

## Flow control

### Goto

Jumps directly to a new position in the source code.

Format:
```
Goto labelName
```

Where `labelName` is a Basic4GL label declared as the first identifier on a line, followed by a colon.
Basic4GL will jump straight to the offset of the `labelName` label, and continue execution.

For example:
```
Loop:
Print "Hello "
Goto Loop
```

Creates an infinite loop, where "Hello" is printed again and again.

### Gosub
Calls a subroutine.

Format:
```
Gosub labelName
```

Where `labelName` is a Basic4GL label, declared exactly the same way as with the `Goto` instruction.

The subroutine should directly follow the `labelName` label, and be terminated with a `Return` instruction.
When `Return` executes, Basic4GL will jump to the instruction immediately after the `Gosub` instruction.

Example:
```
Dim name$: name$ = "Bob"
locate 10, 10: gosub Name
locate 20, 4:  gosub Name
locate 3, 15:  gosub Name
locate 30, 20: gosub Name
end

Name:
print name$
Return
```

To encounter a `Return` instruction, without a corresponding `Gosub` is a runtime error.
A `Gosub` without a `Return` will not cause a runtime error, but will waste stack space.
If too many `Gosub`s are without `Return`s will eventually cause a "stack overflow" runtime error

### If .. Then .. Elseif .. Else .. Endif
Executes a block of code conditionally.

Format:
```
If expression Then
If block
Endif
```

Or:
```
If expression Then
If block
Else
Else block
Endif
```

Basic4GL evaluates `expression`. It must evaluate to an integer (usually the result of a boolean expression).
If the expression evalutes to true (non zero), then the `If block` instructions are executed.
Otherwise the `Else block` instructions are executed if present.

Example 1:
```
If lives < 1 then
Print "Game Over"
End
Endif
```

Example 2:
```
If score > highscore Then
Print "New high score!"
highscore = score
Else
Print "Better luck next time."
Endif
```

Basic4GL also supports the `Elseif` keyword, which is equivalent to an `else` followed by an `if`, but removes the need for an extra `endif` at the end of the `if` structure.
Thus:
```
if expression1 then
...
elseif expression2 then
...
endif
```

Is equivalent to:
```
if expression then
...
else
if expression2 then
...
endif
endif
```

Any number of `endif` sections can be placed after the initial `if`. You cannot place an `endif` after the `else` section however.

Example 3:
```
dim a
for a = 0 to 10
if     a = 0  then printr "Zero"
elseif a = 1  then printr "One"
elseif a = 2  then printr "Two"
elseif a = 3  then printr "Three"
elseif a = 4  then printr "Four"
elseif a = 5  then printr "Five"
elseif a = 6  then printr "Six"
elseif a = 7  then printr "Seven"
elseif a = 8  then printr "Eight"
elseif a = 9  then printr "Nine"
elseif a = 10 then printr "Ten"
else               
printr "???"
endif
next
```

Example 4:
```
dim score
print "Enter score (0-100): "
score = Val (Input$ ())
print "Your grade is: "
if     score < 20 then printr "F"
elseif score < 30 then printr "E"
elseif score < 50 then printr "D"
elseif score < 70 then printr "C"
elseif score < 90 then printr "B"
else                   printr "A"
endif
```

#### Compatibility with other BASICs

Basic4GL also supports the syntax:
```
If condition Then
ifblock
end if
```

The `if` must follow immediately after the `end`, otherwise it will be interpreted as an `end` program instruction.

### While .. Wend
Executes a code block repeatedly while an expression is true.

Format:
```
While expression
Code block
Wend
```
This creates a conditional loop. Basic4GL evalutes `expression`, which again must evaluate to an integer (and is usually a boolean expression).
If the expression evaluates to false (zero), then Basic4GL will jump straight to the instruction following the `Wend`, and continue.
If the expression evaluates to true Basic4GL will execute the code block, then re-evaluate the expression.
Basic4GL will continue executing the code block until the expression evaluates to false.

Example:
```
While lives > 0
' Do gameplay
...
Wend
' Game over
...
```

### For .. next
Used to create loops with a loop counter variable.

Format:
```
For variable = begin-value To end-value
Code block
Next
```

Or:
```
For variable = begin-value To end-value step step-constant
Code block
Next
```
This creates a loop, where `variable` counts from `begin-value` to `end-value`.
`Variable` must be a numeric type (integer or real), and cannot be an array element or structure field.
`Step-constant` must be a constant expression (integer or real).
If no `step` is given the step-constant defaults to `1`.

Basic4GL will count either upwards or downwards depending on whether the step-constant is positive or negative.
If step-constant is positive, the `for..next` construct is exactly equivalent to:
```
variable = begin-value
While variable <= end-value
Code block
variable = variable + step-constant
Wend
```

If step-constant is negative, it is equivalent to:
```
variable = begin-value
While variable >= end-value
Code block
variable = variable + step-constant
Wend
```

And if step-constant is zero, it is equivalent to:
```
variable = begin-value
While variable <> end-value
Code block
Wend
```

Example 1:
```
Dim index
For index = 1 to 10
Printr "Index = " + index
Next
```

Example 2:
```
Dim count: count = 10
Dim squared(count), index
For index = 0 to count
squared (index) = index * index
Next
```

Example 3:
```
dim angle#
glTranslatef (0, 0, -3)
glBegin (GL_LINE_LOOP)
for angle# = 0 to 2 * m_pi step 2 * m_pi / 360
glVertex2f (sin (angle#), cos (angle#))
next
glEnd ()
SwapBuffers ()
```

Example 4:
```
dim count
for count = 10 to 1 step -1
cls: locate 20, 12: printr count
Sleep (1000)
next
cls: locate 15, 12: print "Blast off!!"
```

### Do .. loop
Also used to execute a code block a number of times.

Format:
```
do
Code block
loop
```

Or:
```
do while condition
Code block
loop
```

Or:
```
do until condition
Code block
loop
```

Or:
```
do
Code block
loop while condition
```

Or:
```
do
Code block
loop until condition
```

## Functions and subroutines
User defined functions and subroutines are created with the `function` and `sub` keywords respectively.
They are blocks of code that are "called", much like when you `gosub` to a label. At this point the computer executes the code inside the function/subroutine and then resumes executing from the instruction after the one that called the function/subroutine.

> [!TIP]
>
> You are strongly recommended to use functions/subroutines instead of gosub/return, as it is generally considered to be better programming practice.

Functions/subroutines introduce a number of features not supported by gosub/return:

- Local variables - Prevent two unrelated parts of code from interfering with each other by modifying each others' variables.
- Parameters - Provide a convenient and less error prone (than using global variables) way to pass data to a routine.
- Return values - Provide a convenient and less error prone (than using global variables) way to pass data back from a routine.
- Better encapsulation - A function/subroutine can only be executed by calling it explicitly. You do not have to setup gotos to "jump around" the routine to prevent it from executing when it shouldn't.

### Sub/End Sub
To create a subroutine, use `Sub` and `End Sub`

Format:
```
Sub name([param[, param[,...]]])

...

End Sub
```

Where name is the name of the subroutine, and must not have already been used for a variable, function, other subroutine etc.
param are optional parameters that will be passed to the subroutine, and can be used inside it like variables.

Examples:
```
sub MySubroutine()
print "Hello"
end sub
sub PrintAt(x, y, text$)
locate x, y
print text$
end sub
```

The format for parameters is the same as when DIMming a variable.
You can specify integer, real or string (`%`, `#` and `$` suffixes), structures and pointers.

Array parameters are specified by suffixing the variable with empty brackets `()`.

> [!NOTE]
>
> You do not specify the array size for array parameters.

To specify a 2D or 3D array, use `()` and `()()` respectively (and so on).

For example:
```
sub PrintTextArray(array$())
dim i
for i = 0 to arraymax(array$)
printr array$(i)
next
end sub
dim a$(3)
a$(0) = "This"
a$(1) = "is"
a$(2) = "a"
a$(3) = "test"
PrintTextArray(a$)
```

### Return (from subroutine)

Program control returns from a subroutine as soon as its last instruction has executed.
Alternatively you can return immediately from a subroutine with the `return` command.

Format:
```
Return
```

### Calling a subroutine
Subroutines are called the same way as Basic4GL built-in routines and functions.

Format:
```
name([value1[,value2[,...]]])
```
Local variables
To declare a local variable, simply declare it with dim inside the body of the subroutine.

Example:
```
sub DrawStars(count)
dim i               ' This is a local variable
for i = 1 to count
print "*"
next
printr
end sub

dim i                   ' This is a global variable
i = 3
DrawStars(20)
print i
```

Local variables can only be accessed inside the subroutine that they are DIMmed.
Their memory is reclaimed as soon as the subroutine finishes.

An important feature of local variables is that if a variable of the same name is DIMmed in two different subroutines,
(or if one is DIMmed outside any subroutine), they are treated as two completely different variables,
each with its own separate storage.

This is very useful for temporary variables (like `for..next` loop counters),
as the variable is guaranteed not to be overwritten by another subroutine that your subroutine may call.

### Function/end function
To create a function, use `function` and `end function`.

Format:
```
Function name([param[, param[, ...]]])

...

End Function
```

Where name is the name of the function, and must not have already been used for a variable, function,
other subroutine etc.
`param` are optional parameters that will be passed to the function, and can be used inside it like variables.

`name` also determines the "return type" of the function (what kind of value it returns),
and can be treated much like a variable in a DIM, in that you can suffix it with (`%`, `#`, `$`) to return an integer,
real or string respectively, or precede it with a structure name to return a structure.

To declare a function that returns an array, suffix the declaration with a pair of empty brackets.

A function must explicitly return a value with the `return` keyword.

### Return (from function)
A function must return a value to the caller with the `return` keyword.

Format:
```
Return expression
```

Where expression is the expression that will be evaluated, and whose result will be sent back to the caller.

Examples:
```
function AddTwoNumbers(n1, n2)
return n1 + n2
end function
function SumArray(array())
dim sum, i
for i = 0 to arraymax(array)
sum = sum + array(i)
next     
return sum
end function
```

### Calling a function
A function can be called exactly the same way as a subroutine.
However, a function can also be called within an expression, and its result used as part of the expression in the same way as a constant or variable.

Example 1:
```
function Reverse$(s$)
  dim result$, i
  for i = 1 to len(s$)
    result$ = result$ + mid$(s$, len(s$) - i + 1, 1)
  next
  return result$
end function

print Reverse$("?efil laer eht siht sI")
```

Example 2:
```
function Random(min, max)
  return rnd() % (max - min + 1) + min
end function

dim dice(5), i
for i = 1 to 5: dice(i) = Random(1, 6): next
for i = 1 to 5: print dice(i); " ";: next
```

Example 3:
```
function UpdateChar$(c$, delta)
  dim a
  a = asc(c$)
  a = a + delta
  if a > 255 then a = a - 256 endif
  if a < 0 then a = a + 256 endif
  return chr$(a)
end function

function UpdateWord$(w$, delta)
  dim result$, i
  for i = 1 to len(w$)
    result$ = result$ + UpdateChar$(mid$(w$, i, 1), delta)
  next
  return result$
end function

dim word$, encoded$, decoded$
input "Word"; word$
encoded$ = UpdateWord$(word$, 1)
printr "Encoded: "; encoded$
decoded$ = UpdateWord$(encoded$, -1)
printr "Decoded: "; decoded$
```

### Declare
You can "forward declare" a function or subroutine with the `declare` keyword.

Format:
```
Declare sub name([param[, param[, ...]]])
```

Or:
```
Declare function name([param[, param[, ...]]])
```

"Forward declaring" a function/subroutine allows the compiler to compile calls to the function/subroutine
before it has compiled the function body.

### Function restrictions
Be aware that there are a couple of restrictions on what can be placed inside a function or subroutine:

- You cannot define a label inside a function/subroutine.
- You cannot use the `goto` or `gosub` commands inside a function/subroutine.

## Program data
Basic4GL provides the standard `Data`, `Read` and `Reset` mechanism for entering data directly into programs.
This is basically a shorthand way of hard-coding data into programs and is typically used to initialise arrays.

The actual data stored is a list of values. Each value is either a string or a number (int or real).

### Data
To specify the data elements, use `Data`.

Format:
```
Data element [, element [, ...]]
```

Examples:
```
Dim 12.4, -3.4, 12, 0, 44
Dim My age, 20, My height, 156
Dim "A long time ago, in a galaxy far away, yada yada yada"
```

If the data element can be parsed as a number, it will be stored as such. Otherwise, it will be stored as a string.

Strings can either be quoted (enclosed in double quotes) or unquoted. Quoted strings can contain commas (`,`), colons (`:`) and single quotes (`'`).
Unquoted strings cannot contain these characters, because:

- Comma starts a new data element
- Colon starts a new instruction
- Single starts a program comment

So it is best to quote strings if you are unsure.

### Read
In order to do something with the data, you need to read it into variables, using `Read`.

Format:
```
Read variable [, variable [, ...]]
```

Variable must be a simple variable type, either a string, integer or real.
(In other words, you can't read a structure or an array with a single read statement,
although you can write code to read each element individually).

Read copies an element of data into the variable, and then moves the data position along one.
> [!CAUTION]
>
> If there is no data, or the program has run out of data, you will get an "Out of DATA" runtime error.

> [!CAUTION]
>
> Attempting to read a string value into a number variable (integer or real) will also generate a runtime error.

Example 1:
```
data age, 22, height, 175, shoesize, 12
dim name$(3), value(3), i
for i = 1 to 3
read name$(i), value(i)
next
for i = 1 to 3
printr name$(i) + "=" + value(i)
next
```

### Reset
`Reset` tells Basic4GL where to start loading data from.

Format:
```
Reset labelname
```

Where `labelname` is a Basic4GL program label.

The next `Read` will begin reading data from the first `Data` statement after `labelname`.
```
ThisData:
  data 1, 2, 3, 4, 5

ThatData:
  data cat, dog, fish, mouse, horse

dim a$, i
printr "1) This data"
printr "2) That data"
print "Please press 1 or 2"

while a$ <> "1" and a$ <> "2"
  a$ = Inkey$ ()
wend

if a$ = "1" then
  reset ThisData
else            
  reset ThatData
endif

printr
for i = 1 to 5
  read a$
  printr a$
next
```

## External functions
Basic4GL supports a number of external functions.

> [!TIP]
>
> You can see a full list by selecting "Help|Function and Constant list..." and selecting the "Functions" tab.
> This lists all the external functions Basic4GL recognises,
> along with their return types (if they return a value), and parameter types.

External functions are called with the following format:
```
FunctionName ([param [, param [, ...]])
```

Examples:
```
Beep()
glClear (GL_COLOR_BUFFER_BIT)
glVertex3f (-2.5, 10, 0)
```

A small number of functions do not require their arguments to be enclosed in brackets (mainly for historical reasons.)
These functions are: `Cls`, `Print`, `Printr` and `Locate`.
For example:
```
Cls
Locate 17, 12
Print "Hello"
```

### Traditional BASIC syntax
When "traditional BASIC" syntax is used, functions that do not return a value must **not** have their parameters enclosed in brackets.

Some examples:
```
sleep 1000
glBegin GL_TRIANGLES
SprSetX x#
glVertex3f 10, 4, 2
```

Functions which do return a value must still have their parameters enclosed in brackets (or have empty brackets if there are no parameters)

Examples are:
```
a = rnd() % 10
texture = LoadTexture(filename$)
a$ = inkey$()
```

Some functions return a value, which can be assigned to a variable, used in an expression or as a parameter to another external function.

Examples:
```
print Sqrt (2)
if ScanKeyDown (VK_UP) then ...
locate (TextCols()-Len(a$))/2, TextRows()/2: Print a$
```

## Runtime compilation

> [!WARNING]
>
> Basic4GLj does not currently support Basic4GL's Runtime compilation feature in the current release version, 0.5.0;
> Runtime compilation support is planned for future Basic4GLj project milestones.

Basic4GL code can also be compiled and executed at runtime. The source can be a file on disk, or a text string in memory.
The runtime compile is the same as the compile time compiler, and accepts all the same code. The only restriction is that you cannot use "include" within runtime compiled code.

The main commands are `Comp` and `Exec` to compile and execute respectively.
(Actually `Comp` is a function, but it's so closely associated with the `Exec` command that I've included it here.)

There is also support for calling functions in runtime-compiled code, using the `Runtime` keyword.

### Comp
`Comp` compiles a text string and return a handle that can be used to execute the compiled code at runtime.

Format:
```
Comp(codetext)
```

Where `codetext` is a text string, or an array of text strings, containing code to be compiled at runtime.

If the text compiled successfully, `Comp` returns a non-zero integer handle to identify the compiled code.
If the compiler encountered an error, `Comp` returns zero, and the error description can be retrieved with `CompilerError()`, `CompilerErrorLine()` and `CompilerErrorCol()`.

Example 1:
```
dim code1, code2
code1 = Comp("printr " + chr$(34) + "Ding" + chr$(34))
code2 = Comp("printr " + chr$(34) + "Dong" + chr$(34))
exec code1
exec code2
exec code1
exec code2
```

Example 2:
```
dim prog$(10), code
prog$(0) = "dim x, y"
prog$(1) = "for y = 1 to 10"
prog$(2) = "for x = 1 to y"
prog$(3) = "print " + chr$(34) + "*" + chr$(34) + ";"
prog$(4) = "next"
prog$(5) = "printr"
prog$(6) = "next"
Compile(prog$)
exec
```

### CompFile
`CompFile` compiles a file on disk and return a handle that can be used to execute the compiled code at runtime.

Format:
```
CompFile(filename)
```
Where filename is the filename as a text string.

If the file was read and compiled successfully, `CompFile` returns a non-zero integer handle to identify the compiled code.
If the compiler encountered an error, `CompFile` returns zero, and the error description can be retrieved with `CompilerError()`, `CompilerErrorLine()` and `CompilerErrorCol()`.

### Exec
`Exec` executes runtime-compiled code.

Format:
```
Exec
```
```
Exec handle
```
Where `handle` is an integer handle returned from a successful call to `Comp` or `CompFile`.

If no handle is supplied, Exec executes the last code compiled (or bound if `BindCode` has been executed.)

> [!CAUTION]
>
> Be warned that any runtime errors will halt your program.

### Functions/subs inside compiled code
Normally you cannot have two functions or subs with the same name.

However, Basic4GL will allow this if the functions/subs are in different compiled code blocks,
or if one is in the main program and the other(s) in compiled code blocks.

Basic4GL applies "scoping" logic to determine which function/sub is to be called as follows:

- If calling code is in the main program, the function/sub is assumed to be in the main program
- If calling code is runtime-compiled, the function/sub is first assumed to be in the runtime-compiled code then in the main program (if not found in the runtime code)

The scoping logic only applies to functions and subs, however.
Other things like global variables, labels etc are not scoped this way.

Example:
```
' Subroutines in main code
sub Sub1(): printr "Main 1": end sub
sub Sub2(): printr "Main 2": end sub

' Subroutines in compiled code
Comp("sub Sub1(): printr " + chr$(34) + "Runtime 1" + chr$(34) + ": end sub: Sub1(): Sub2()")
' Execute compiled code
exec
printr                 
' Call main subroutines
Sub1()
Sub2()
```

## Calling functions/subs in runtime code
Runtime compiled code can call functions/subs in the main program easily.
Calling runtime-compiled functions/subs from your main program requires you declare the function with "runtime" first.

### Runtime

The `Runtime` keyword is used to declare a function or sub that can be implemented either:
- In the main program
- In one or more sections of runtime compiled code
- Or, all of the above

The syntax is much the same as the `Declare` keyword.

Format:
```
Runtime Sub prototype
```
```
Runtime Function prototype
```

Where prototype defines the function/sub, its parameters and return type (if applicable).

Examples:
```
runtime sub MySub()
runtime sub MoveBadGuy(SBadGuy& badguy)
runtime function CalcY#(x#)
```

Once declared with `runtime`, the sub/function can be called from your main program.
Basic4GL will check at runtime to see if the function/sub being called has been implemented,
checking the current runtime-compiled code first, then the main program.
If the function/sub is found, Basic4GL calls it.
Otherwise, a runtime error results, and your program stops.

As with `exec`, the `current` runtime-compiled code is the last code that was compiled with `Comp`, or bound with `BindCode`.

Example:
```
runtime sub MySub()
sub MySub()
printr "Main program"
end sub

' Will call MySub() in main program
MySub()                                    
dim code
code = Comp("sub MySub(): printr " + chr$(34) + "Runtime compiled code" + chr$(34) + ": end sub")

' Will call MySub() in runtime code
MySub()            
' Will call MySub() in main program
bindcode 0
MySub()                
' Will call MySub() in runtime code
bindcode code
MySub()
```

### BindCode
The `BindCode` command is used to make runtime-compiled code current.
This affects the `Exec` command (when called without a parameter), and where Basic4GL looks for `Runtime` functions.

Format:
```
BindCode 0
```
```
BindCode handle
```
Where `handle` is an integer handle returned from a successful call to `Comp` or `CompFile`.

`BindCode 0` has special meaning. No runtime-compiled code is considered bound.
Any `runtime` functions called must therefore be implemented in the main program itself.
`Exec` without a parameter will cause a runtime exception.

## Other Basic4GL instructions
There are two more Basic4GL instructions that have yet to be discussed.

### End
Causes Basic4GL to stop executing the program.

### Run
Causes Basic4GL to deallocate all variables, reset OpenGL, deallocate any resources (such as OpenGL textures),
clear the Gosub-Return stack and begin executing the program again from the top.

The program will begin executing again as if you had just clicked Run in the Basic4GL editor.

## Credits
Basic4GL, Copyright (C) 2003-2007 Tom Mulgrew

Language guide

16-Feb-2008
Tom Mulgrew

Documentation modified for Markdown (.md) formatting by Nathaniel Nielsen