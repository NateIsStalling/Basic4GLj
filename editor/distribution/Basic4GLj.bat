::Run Basic4GLj on Windows; requires Java be installed and in your path
::-Djava.library.path=native/ is needed for lwjgl library to work properly
::Log console output for debugging
>output.log (
    java -jar -Djava.library.path=native/ Basic4GLj.jar
)