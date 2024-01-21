	Basic4GLj alpha 0.4.1
	Build Date: April 2023
================================================
	Basic4GLj	by Nathaniel Nielsen	(c) 2023
	Basic4GL 	by Tom Mulgrew			(c) 2010
	License:	New BSD; see LICENSES folder
	Requires:	Java 17
	
	Site:		https://github.com/NateIsStalling/Basic4GLj
	Twitter:	@NateIsStalling

================================================
	Usage Notes
================================================

	The bin folder contains scripts to launch Basic4GLj on Windows and MacOS/Linux systems.

	Example programs are included from the original Basic4GL. They may work, they may not, or they might work unexpectedly. Have fun.
	
	Exporting applications generates a launcher batch script.
	
	Java 17 must be installed and in your system path.
	
	Paths of images resources and files loaded in user-made applications are relative to the current directory of the jar; this is a compatibility issue currently since earlier versions of Basic4GL find resources relative to the source code file
	Resources will need to be manually added to exported applications at this time; simply copy the files to the directory where the output jar is located, maintaining their path relative to the jar.

================================================
	Change Log
================================================
0.5.0   - Implemented sound library
        - Added support for "Sandbox Mode" runtime setting
        - Added support for commandline arguments
        - Added "Program Version" project configuration option for commandline programs
        - Java 17 now embedded for installer distributions of Basic4GLj

0.4.1   - Updated LWJGL to stable version 3.3.1
        - Added LWJGL natives for macOS arm64, Linux arm32, Linux arm64, Windows x86, and Windows arm64
        - Added Mac shortcut keys
        - Misc bug fixes

0.4.0   - Revived project (Hooray!)
        - Added Mac OS support
        - Java 8 now required
        - Made Basic4GLj open source

0.3.0	- Replaced JOGL extensions with LWJGL library implementation
		- Implemented OpenGL, sprite, text, input, and trig function libraries
		- Added sample programs from the Windows version of Basic4GL
		- Fixed some bugs, added more
		
0.2.2	- Added debugging tools: breakpoints, watchlist, and callstack
		- Implemented "include" keyword (Ctrl+Click opens included files in the editor)
		- Tabs implemented
		
0.2.1	- Projects can be exported as standalone Java applications
		- Settings for projects implemented
		- Function list window added
		- Fixes to syntax highlighting
		- Removed unnecessary library files
		
0.2.0	- UI changes and fixes
		- Improved text editing
		- Bookmarking code implemented
		- Fixed issue with VM error handling and reporting
		- Added license info
		
0.1.2	- code cleanup

0.1.1	- Fixed issue with function calls
		- Fixed issue with parsing hex constants
		- Print function doesn't require parenthesis
		- misc small fixes
		
================================================
	Compatibility Notes
================================================

    Some example programs require "glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_BLEND)" to be added their original source inorder to display output; this is considered a bug since it inverts texture colors and will hopefully become unecessary in future versions of the Basic4GLj IDE - the applications will function normally with or without the added glTexEnvi in earlier versions of the Windows Basic4GL. 
	
	The following virtualkey constants available in the Windows version of Basic4GL are not currently supported:
		VK_LBUTTON, VK_RBUTTON, VK_CANCEL, VK_MBUTTON, VK_CLEAR, VK_RETURN, VK_SHIFT, VK_CONTROL, VK_MENU, VK_PAUSE, VK_KANA, VK_HANGEUL, VK_HANGUL, VK_JUNJA, VK_FINAL, VK_HANJA, VK_KANJI, VK_CONVERT, VK_NONCONVERT, VK_ACCEPT, VK_MODECHANGE, VK_SELECT, VK_PRINT, VK_EXECUTE, VK_SNAPSHOT, VK_HELP, VK_LWIN, VK_RWIN, VK_APPS, VK_SEPARATOR, VK_LMENU, VK_RMENU, VK_PROCESSKEY, VK_ATTN, VK_CRSEL, VK_EXSEL, VK_EREOF, VK_PLAY, VK_ZOOM

	Integer values of the virtualkey constants may differ from the Windows version; the supported virtualkeys have been mapped to the key constants provided by GLFW where possible. In addition, all documented GLFW key constants are available for use in Basic4GLj.
		see: http://www.glfw.org/docs/latest/group__keys.html
	
	VK_RETURN has not been mapped to a GLFW constant because GLFW has separate constants for the enter key and the keypad/numpad enter key, where VK_RETURN would recognize either. 
	GLFW_KEY_ENTER and GLFW_KEY_KP_ENTER are available for usage in place of VK_RETURN.
		
	OpenGL GLU constants are unavailable, they are unsupported by the current version of LWJGL that is used by Basic4GLj. GLU functions available in previous versions of Basic4GL are available with modifications.
		gluOrtho2D is mapped to glOrtho
		gluPerspective uses glFrustrum implementation
		gluLookAt will currently throw an UnsupportedOperationException if called
	