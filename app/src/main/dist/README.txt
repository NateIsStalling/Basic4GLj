	Basic4GLj 0.7.0 alpha
	Build Date: June 2026
================================================
	Basic4GLj	by Nathaniel Nielsen	(c) 2026
	Basic4GL 	by Tom Mulgrew			(c) 2016
	License:	New BSD; see LICENSES folder
	Requires:	Java 17
	
	Site:		https://github.com/NateIsStalling/Basic4GLj
	Bluesky:    @nateisstalling.bsky.social

================================================
	Usage Notes
================================================

	Java 17 must be installed and in your system path. If you are using the installer distribution of Basic4GLj, Java 17 is embedded and does not need to be installed separately.

	The bin folder contains scripts to launch Basic4GLj on Windows and MacOS/Linux systems.

	Sample programs are included from the original Basic4GL - please report any issues running these programs at https://github.com/NateIsStalling/Basic4GLj/issues.
	
	Exporting applications generates a zip file containing: a JAR file with your program, .bat and .sh scripts for launching the program, and any embedded resources.

	Exported applications require Java 17 to be installed and in the system path to run.
	

================================================
	Change Log
================================================

[v0.7.0-alpha] June 6, 2026
The compiler and debugger update:
    - All original sample programs should now be supported; please report any issues https://github.com/NateIsStalling/Basic4GLj/issues
    - Implemented compiler library for runtime compilation of Basic4GL code
    - Updated compiler to support Basic4GL 2.6.4 syntax and functions
    - Updated project settings look and feel
    - Added Program Arguments in project settings for commandline input
    - Added JVM Settings in project settings for advanced debugging
    - Added View Virtual Machine debugging option
    - Added support to embed assets with exported programs
    - Added progress status for exporting programs
    - Added Fullscreen support for running programs
    - Added gluLookAt support
    - Added missing VK key codes
    - Added support for accessing files in AppData folder; creates "Basic4GL" folder in AppData
    - Added support to pause program with Pause key while debugging
    - Fixed misc. issues with Mouse and Keyboard input for sample programs
    - Fixed issues with file IO functions

[v0.6.0-alpha] March 19, 2025
The network engine update:
    - Implemented network engine library
    - Added "Open Recent" to File menu and "Recent Files" to default tab
    - Added macOS arm64 builds to Releases
    - macOS release builds are now notarized

[v0.5.1-alpha] July 6, 2024
Bug fix update:
    - Fixed issues loading textures with TexStripFrames and LoadTexStrip

[v0.5.0-alpha] February 1, 2024
Sound engine and installer update:
    - Implemented sound library
    - Java 17 now required
    - OpenJDK 17 now embedded for installer distributions of Basic4GLj
    - Added Find/Replace editor options
    - Added support for "Sandbox Mode" runtime setting
    - Added support for commandline arguments
    - Added "Program Version" project configuration option for commandline programs

[v0.4.1-alpha] April 1, 2023
LWJGL update and bug fixes:
    - Updated LWJGL to stable version 3.3.1
    - Added LWJGL natives for macOS arm64, Linux arm32, Linux arm64, Windows x86, and Windows arm64
    - Added Mac shortcut keys
    - Misc bug fixes

[v0.4.0-alpha] March 13, 2023
Mac OS support and open source update:
    - Revived project (Hooray!)
    - Added Mac OS support
    - Java 8 now required
    - Made Basic4GLj open source

[v0.3.0-alpha] November 11, 2016
LWJGL and OpenGL update:
    - Replaced JOGL extensions with LWJGL library implementation
    - Implemented OpenGL, sprite, text, input, and trig function libraries
    - Added sample programs from the Windows version of Basic4GL
    - Fixed some bugs, added more
		
[0.2.2-alpha] April 13, 2015
Debugger update:
    - Added debugging tools: breakpoints, watchlist, and callstack
    - Implemented "include" keyword (Ctrl+Click opens included files in the editor)
    - Tabs implemented
		
[v0.2.1-alpha] April 13, 2015
Standalone application export and project settings update:
    - Projects can be exported as standalone Java applications
    - Settings for projects implemented
    - Function list window added
    - Fixes to syntax highlighting
    - Removed unnecessary library files
		
[v0.2.0-alpha] January 24, 2015
The UI update:
    - UI changes and fixes
    - Improved text editing
    - Bookmarking code implemented
    - Fixed issue with VM error handling and reporting
    - Added license info
		
[v0.1.2-alpha] January 10, 2015
Bug fixes and code cleanup:
    - code cleanup

[v0.1.1-alpha] January 8, 2015
"Initial" release:
    - Fixed issue with function calls
    - Fixed issue with parsing hex constants
    - Print function doesn't require parenthesis
    - misc small fixes
		
================================================
	Compatibility Notes
================================================

    Some example programs require "glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_BLEND)" to be added their original source inorder to display output; this is considered a bug since it inverts texture colors and will hopefully become unecessary in future versions of the Basic4GLj IDE - the applications will function normally with or without the added glTexEnvi in earlier versions of the Windows Basic4GL. 
	
	The following virtualkey constants available in the Windows version of Basic4GL are not currently supported:
		VK_LBUTTON, VK_RBUTTON, VK_CANCEL, VK_MBUTTON, VK_CLEAR, VK_KANA, VK_HANGEUL, VK_HANGUL, VK_JUNJA, VK_FINAL, VK_HANJA, VK_KANJI, VK_CONVERT, VK_NONCONVERT, VK_ACCEPT, VK_MODECHANGE, VK_SELECT, VK_EXECUTE, VK_SNAPSHOT, VK_HELP, VK_SEPARATOR, VK_PROCESSKEY, VK_ATTN, VK_CRSEL, VK_EXSEL, VK_EREOF, VK_PLAY, VK_ZOOM

	Integer values of the virtualkey constants may differ from the Windows version; the supported virtualkeys have been mapped to the key constants provided by GLFW where possible.
	In addition, all documented GLFW key constants are available for use in Basic4GLj.
		see: http://www.glfw.org/docs/latest/group__keys.html

	OpenGL GLU constants are unavailable, they are unsupported by the current version of LWJGL that is used by Basic4GLj. GLU functions available in previous versions of Basic4GL are available with modifications.
		gluOrtho2D is mapped to glOrtho
		gluPerspective uses glFrustrum implementation
		gluLookAt uses glMultMatrixf with glTranslated implementation
	