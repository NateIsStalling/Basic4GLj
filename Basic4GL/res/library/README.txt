Use this folder to store additional resources for built-in Basic4GL libraries such as the Standard or DesktopGL library. 
Separate resources into folders based on which libraries use them, using the name of a library's parent folder as 
the name for its respective resource folder.
- ie: The resource folder for com.basic4gl.lib.targets.desktopgl.DesktopGL.java would be /res/desktopgl/

//TODO for 0.2.0
Implement a API for libraries that includes the following functions:
getResource(Class<?> c, String path)		Return a file on the given class' classpath
getImageResource(Class<?> c, String path)	Return an image on the given class' classpath
getLibResource(Class<?> c, String path)		Return a file from the /res/library/<?>/ directory

