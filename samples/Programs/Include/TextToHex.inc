' Text to hexidecimal and other conversions.
' Courtesy of djlinux.

include Include\Text.inc

function HOB2D(basis,val$)
  dim n, i
  for i=1 to len(val$)
    n=n*basis
    n=n + (InStr("0123456789ABCDEFG",mid$(val$,i,1))-1)
  next
  return n
end function
   
' Convert string to number.
' Format is determined by prefix, e.g 0x => Hex
function Value(val$)
  dim basis
  val$=ucase$(trim$(val$))
  if     left$(val$,2)="&H" then
    basis=16:' basic hex
    val$=right$(val$,len(val$)-2)
  elseif left$(val$,2)="0X" then
    basis=16:' C hex
    val$=right$(val$,len(val$)-2)
  elseif left$(val$,1)="H"  then
    basis=16:' old hex
    val$=right$(val$,len(val$)-1)
  elseif left$(val$,1)="$"  then
    basis=16:' asm hex
    val$=right$(val$,len(val$)-1)
  elseif right$(val$,1)="H" then
    basis= 16:' other hex
    val$=left$(val$,len(val$)-1)
  elseif left$(val$,2)="&O" then
    basis= 8' basic oct
    val$=right$(val$,len(val$)-2)
  elseif left$(val$,2)="&B" then
    basis= 2:' basic bin
    val$=right$(val$,len(val$)-2)
  else  
    basis=10' basic dec
  end if
  if basis then
     return HOB2D(basis,val$)
  end if
end function

function D2HOB$(basis,int32)
  dim r$
  do
    r$=mid$("0123456789ABCDEF",(int32 % basis)+1,1)+r$
    int32=int32/basis
  loop until not int32
  return r$
end function  
          
' Convert number to decimal string
function Dec$(int32)
  return D2HOB$(10,int32)
end function
                           
' Convert number to hexidecimal string
function Hex$(int32)
  return "&H"+D2HOB$(16,int32)
end function
            
' Convert number to octal string
function Oct$(int32)  
  return "&O"+D2HOB$(8,int32)
end function

' Convert number to binary string
function Bin$(int32)  
  return "&B"+D2HOB$(2,int32)
end function