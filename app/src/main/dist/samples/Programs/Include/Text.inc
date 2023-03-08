' Text handling functions/subs
' Courtesy of djlinux.

' Find position of substring inside string
function InStr(s$, sub$)
    dim i, sLen, subLen
    sLen = len(s$)
    subLen = len(sub$)
    for i = 1 to sLen - subLen + 1
        if mid$(s$, i, subLen) = sub$ then
            return i
        endif
    next
    
    return 0
end function
 
' Trim whitespace from left/right of string
function RTRIM$(i$) 
  while right$(i$,1)=" " 
    i$=left$(i$,len(i$)-1)
  wend
  return i$
end function   

function LTRIM$(i$) 
  while left$(i$,1)=" " 
    i$=right$(i$,len(i$)-1)
  wend
  return i$
end function

function TRIM$(i$) 
  return LTRIM$(RTRIM$(i$))
end function