' Miscellaneous functions/subroutines

' Integer increment/decrement
sub Inc(&v): v = v + 1: end sub
sub Dec(&v): v = v - 1: end sub
sub IncBy(&v, by): v = v + by: end sub
sub DecBy(&v, by): v = v - by: end sub
                             
' Floating point increment/decrement
sub IncF(&v#): v# = v# + 1: end sub
sub DecF(&v#): v# = v# - 1: end sub
sub IncByF(&v#, by#): v# = v# + by#: end sub
sub DecByF(&v#, by#): v# = v# - by#: end sub
