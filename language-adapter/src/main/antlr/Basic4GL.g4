/**
 * Basic4GL lexer grammar.
 *
 * Consumed by LanguageSupportTokenMaker (RSyntaxTextArea highlighting) and
 * Basic4GLLanguageSupport (SymbolIndexer).
 *
 * The reference implementation is authoritative for every rule here:
 *   Parser.java             character-level tokenization
 *   TomBasicCompiler.java   reserved words, operators, punctuation
 *   Preprocessor.java       include and #plugin directives
 *
 * To add a keyword: add it to reservedWords in TomBasicCompiler and add a rule
 * to the reserved-words block below, above IDENTIFIER. Neither consumer needs
 * changing. Regenerate with: antlr4 Basic4GL.g4
 */
lexer grammar Basic4GL;

// ===========================================================================
// Preprocessor directives
// ===========================================================================
//
// The two directives do not share a syntax, and neither reaches Parser.java.
//
//   include <path>   Preprocessor.java:141
//                    No '#'. Must start at column 0 (the line is not trimmed).
//                    The 8th character must be a space, not a tab.
//                    Case-insensitive. The path is unquoted and runs to end of
//                    line, so it may contain spaces, '/' and '\'.
//
//   #plugin <name>   Preprocessor.java:190
//                    '#' required. The line is trimmed first, so leading
//                    whitespace is allowed. Any whitespace may follow.
//                    The value may be wrapped in " " or ' '.
//
// A failed predicate disables the rule, so input falls through to INCLUDE_KW
// or INVALID_SYMBOL. RSyntaxTextArea lexes one line at a time, so
// _tokenStartCharPositionInLine is always accurate.

INCLUDE_DIR : {_tokenStartCharPositionInLine == 0}? I N C L U D E ' ' -> pushMode(INCLUDE_PATH_MODE) ;

// Leading whitespace is absorbed into the token because the line is trimmed
// before the directive is matched.
// An empty value is not a directive (Preprocessor.java:198), which a lexer
// cannot express without lookahead. PLUGIN_DIR with no following PLUGIN_VALUE
// is the malformed case.
PLUGIN_DIR  : {_tokenStartCharPositionInLine == 0}? [ \t]* '#' P L U G I N [ \t]+ -> pushMode(PLUGIN_VALUE_MODE) ;

// ===========================================================================
// Comments
// ===========================================================================
//
// An unquoted apostrophe comments out the rest of the line: Parser.peekChar
// (line 57) returns the end-of-line sentinel when it sees one, so the parser
// cannot see past it. Inside a string literal it is an ordinary character.
// Comments are not tokens in the reference; this rule exists so the
// highlighter has something to paint.
//
// There is no 'rem' comment form.

COMMENT : '\'' ~[\r\n]* ;

// ===========================================================================
// Reserved words - TomBasicCompiler.java:270-322
// ===========================================================================
//
// All 52 entries, in declaration order. Every keyword must precede IDENTIFIER;
// order among the keywords themselves does not matter, since ANTLR takes the
// longest match.

DIM_KW                : D I M ;
GOTO_KW               : G O T O ;
IF_KW                 : I F ;
THEN_KW               : T H E N ;
ELSEIF_KW             : E L S E I F ;
ELSE_KW               : E L S E ;
ENDIF_KW              : E N D I F ;
END_KW                : E N D ;
GOSUB_KW              : G O S U B ;
RETURN_KW             : R E T U R N ;
FOR_KW                : F O R ;
TO_KW                 : T O ;
STEP_KW               : S T E P ;
NEXT_KW               : N E X T ;
WHILE_KW              : W H I L E ;
WEND_KW               : W E N D ;
RUN_KW                : R U N ;
STRUC_KW              : S T R U C ;
ENDSTRUC_KW           : E N D S T R U C ;
CONST_KW              : C O N S T ;
ALLOC_KW              : A L L O C ;
NULL_KW               : N U L L ;

// Switches tokenization for the rest of the statement. See DATA_MODE.
DATA_KW               : D A T A -> pushMode(DATA_MODE) ;

READ_KW               : R E A D ;
RESET_KW              : R E S E T ;
TYPE_KW               : T Y P E ;                          // QBasic/FreeBASIC synonym for 'struc'
AS_KW                 : A S ;

// The complete set of type names. compileAs accepts only these four or a
// struct name; there is no 'int'.
INTEGER_T             : I N T E G E R ;
SINGLE_T              : S I N G L E ;
DOUBLE_T              : D O U B L E ;
STRING_T              : S T R I N G ;

// 'language' and its four arguments - compileLanguage, TomBasicCompiler.java:5272
LANGUAGE_KW           : L A N G U A G E ;
TRADITIONAL_KW        : T R A D I T I O N A L ;
BASIC4GL_KW           : B A S I C '4' G L ;
TRADITIONAL_PRINT_KW  : T R A D I T I O N A L '_' P R I N T ;
TRADITIONAL_SUFFIX_KW : T R A D I T I O N A L '_' S U F F I X ;

INPUT_KW              : I N P U T ;
DO_KW                 : D O ;
LOOP_KW               : L O O P ;
UNTIL_KW              : U N T I L ;
FUNCTION_KW           : F U N C T I O N ;
SUB_KW                : S U B ;
ENDFUNCTION_KW        : E N D F U N C T I O N ;
ENDSUB_KW             : E N D S U B ;
DECLARE_KW            : D E C L A R E ;
RUNTIME_KW            : R U N T I M E ;
BINDCODE_KW           : B I N D C O D E ;
EXEC_KW               : E X E C ;

// Reached only when INCLUDE_DIR's column-0 predicate fails; the preprocessor
// strips real include lines before the compiler runs.
INCLUDE_KW            : I N C L U D E ;

// Special-cased in compileLoad (TomBasicCompiler.java:2772) because the plugin
// mechanism cannot express an any-type parameter.
ARRAYMAX_KW           : A R R A Y M A X ;

BEGINCODEBLOCK_KW     : B E G I N C O D E B L O C K ;
ENDCODEBLOCK_KW       : E N D C O D E B L O C K ;

// ===========================================================================
// Statement-position built-ins
// ===========================================================================
//
// Dispatched as statements by compileInstruction (TomBasicCompiler.java:1241)
// but not reserved words - they resolve to library functions via
// findFunction2. Separate token types let the highlighter style them as
// statements while the symbol layer still treats them as functions.

PRINT_KW  : P R I N T ;
PRINTR_KW : P R I N T R ;

// ===========================================================================
// Word operators - TomBasicCompiler.java:232-241
// ===========================================================================
//
// Not reserved words. getToken leaves them as CTT_TEXT and the expression
// compiler resolves them through binaryOperators / unaryOperators. Separate
// token types keep the SymbolIndexer from treating them as identifiers.
//
// Modulo is '%', not 'mod'.

XOR_OP  : X O R ;         // binary, binding 10 (looser than and/or, per QBasic)
OR_OP   : O R ;           // binary, binding 11
AND_OP  : A N D ;         // binary, binding 12
LOR_OP  : L O R ;         // binary, lazy, binding 11
LAND_OP : L A N D ;       // binary, lazy, binding 12
NOT_OP  : N O T ;         // unary, binding 20

// ===========================================================================
// Literals
// ===========================================================================
//
// 'true' and 'false' are not tokens. They are library-registered constants
// (TomBasicCompiler.java:635) resolved by getToken, and can be shadowed.

// No escape sequences. A doubled "" does not escape a quote; the first '"'
// ends the literal. Strings cannot span lines. An apostrophe inside a string
// is an ordinary character.
STRING_LIT : '"' ~["\r\n]* '"' ;

// Reaching end of line inside a string raises "Unterminated string".
UNTERMINATED_STRING : '"' ~["\r\n]* ;

// Prefix is '0x', case-insensitive on the 'x', with digits 0-9 and a-f.
// A bare '0x' is rejected by the parser; here it degrades to INT_LIT plus
// IDENTIFIER.
HEX_LIT : '0' X [0-9a-fA-F]+ ;

// Leading-dot (.5) and trailing-dot (5.) forms are both valid. Only one
// decimal point is consumed, so "1.2.3" lexes as "1.2" then ".3". There is no
// exponent notation: "1e5" lexes as "1" then "e5".
FLOAT_LIT : [0-9]+ '.' [0-9]* | '.' [0-9]+ ;
INT_LIT   : [0-9]+ ;

// ===========================================================================
// Identifiers  (must follow every keyword and word operator)
// ===========================================================================
//
// At most one trailing suffix character, from exactly three (Parser.java:252):
//   $ -> string    # -> real    % -> integer
// No suffix means the type is resolved by getDefaultDataBasicValType.
// '!' is not a suffix.
//
// '%' is both a suffix and the modulo operator, so "a%b" lexes as two
// identifiers. Modulo needs whitespace on at least the left: "a % b".
//
// '&' is always a separate token, never part of a name.

IDENTIFIER : [a-zA-Z_][a-zA-Z_0-9]* [$%#]? ;

// ===========================================================================
// Comparison operators
// ===========================================================================
//
// Parser.java:260 tests only the first character of the token, so a symbol
// beginning with '<', '=' or '>' absorbs every following '<', '=' or '>'.
// These are runs, not a fixed set of digraphs: "=>", "==", "<=>" and "===="
// each form one token, which then fails the operator lookup.
//
// The three valid digraphs are declared first so they win the equal-length tie
// against INVALID_COMPARISON.

NEQ : '<>' ;   // binding 30
GTE : '>=' ;   // binding 30
LTE : '<=' ;   // binding 30

INVALID_COMPARISON : [<=>][<=>]+ ;

EQ : '=' ;     // binding 30; also assignment
GT : '>' ;     // binding 30
LT : '<' ;     // binding 30

// ===========================================================================
// Arithmetic operators - TomBasicCompiler.java:246-252
// ===========================================================================

PLUS    : '+' ;   // binding 40
MINUS   : '-' ;   // binding 40; also unary negation, binding 50
STAR    : '*' ;   // binding 41
SLASH   : '/' ;   // binding 42
PERCENT : '%' ;   // modulo, binding 43

// ===========================================================================
// Punctuation
// ===========================================================================
//
// The complete set. Basic4GL has no '[' or ']'; parentheses serve as both call
// arguments and array subscripts.

LPAREN    : '(' ;
RPAREN    : ')' ;
COMMA     : ',' ;
COLON     : ':' ;   // statement separator; also label terminator
SEMICOLON : ';' ;   // print item separator, suppresses newline
AMPERSAND : '&' ;   // pointer declarator / address-of
DOT       : '.' ;   // struct field access

// ===========================================================================
// Whitespace
// ===========================================================================

// Parser skips while (c <= ' '), so all control characters count as
// whitespace, not just space and tab.
WS : [\u0001-\u0009\u000B\u000C\u000E-\u0020]+ ;

// Significant; surfaces as CTT_EOL.
NEWLINE : ('\r' '\n'? | '\n') ;

// ===========================================================================
// Symbols the compiler rejects
// ===========================================================================
//
// Unrecognised characters do lex - Parser makes each a single-character
// CTT_SYMBOL - but nothing in the compiler accepts them:
//
//   [ ] \ ^ @ ~ | ? { }   no corresponding operator or construct
//   ! $ #                 valid only as identifier suffixes ('#' also opens '#plugin')
//
// Must be the last rule in the default mode.

INVALID_SYMBOL : . ;

// ===========================================================================
// Case-insensitive character fragments
// ===========================================================================
//
// getToken lowercases every CTT_TEXT token unless the compiler was constructed
// with caseSensitive = true. Reserved words are stored lowercase.

fragment A : [aA] ; fragment B : [bB] ; fragment C : [cC] ;
fragment D : [dD] ; fragment E : [eE] ; fragment F : [fF] ;
fragment G : [gG] ; fragment H : [hH] ; fragment I : [iI] ;
fragment J : [jJ] ; fragment K : [kK] ; fragment L : [lL] ;
fragment M : [mM] ; fragment N : [nN] ; fragment O : [oO] ;
fragment P : [pP] ; fragment Q : [qQ] ; fragment R : [rR] ;
fragment S : [sS] ; fragment T : [tT] ; fragment U : [uU] ;
fragment V : [vV] ; fragment W : [wW] ; fragment X : [xX] ;
fragment Y : [yY] ; fragment Z : [zZ] ;

// ===========================================================================
// Mode: DATA statement elements
// ===========================================================================
//
// compileData reads elements with getToken(false, true), taking the dataMode
// branch of Parser.nextToken. Commas and colons separate; everything else is
// element text with trailing whitespace trimmed. A bare word in a DATA
// statement is a string constant, not an identifier.
//
// Quoted strings still work, because the '"' branch is tested before the
// dataMode branch. An apostrophe still ends the line. A colon ends the
// statement and pops the mode.
//
// Numeric typing is inferred from element content, which is a semantic
// concern, so all unquoted elements share one token type.

mode DATA_MODE;

DATA_WS      : [\u0001-\u0009\u000B\u000C\u000E-\u0020]+ -> type(WS) ;
DATA_COMMENT : '\'' ~[\r\n]*                             -> type(COMMENT) ;
DATA_STRING  : '"' ~["\r\n]* '"'                         -> type(STRING_LIT) ;
DATA_UNTERM  : '"' ~["\r\n]*                             -> type(UNTERMINATED_STRING) ;
DATA_COMMA   : ','                                       -> type(COMMA) ;
DATA_COLON   : ':'                                       -> type(COLON), popMode ;
DATA_ELEMENT : ~[ \t,:"'\r\n] ~[,:"'\r\n]* ;
DATA_NEWLINE : ('\r' '\n'? | '\n')                       -> type(NEWLINE), popMode ;

// ===========================================================================
// Mode: rest of an 'include' line
// ===========================================================================
//
// The path is unquoted and runs to end of line (Preprocessor.java:147), so it
// may contain spaces, '/', '\' and '.'. No comment form applies: an apostrophe
// here is part of the filename, since Parser never sees this line.

mode INCLUDE_PATH_MODE;

INC_WS       : [ \t]+            -> type(WS) ;
INCLUDE_PATH : ~[ \t\r\n] ~[\r\n]* ;
INC_NEWLINE  : ('\r' '\n'? | '\n') -> type(NEWLINE), popMode ;

// ===========================================================================
// Mode: rest of a '#plugin' line
// ===========================================================================
//
// The value is trimmed, then unwrapped if it both starts and ends with a
// matching quote (Preprocessor.java:464). The single-quote form is why this
// needs its own mode: the preprocessor consumes the line before Parser runs,
// so an apostrophe here is a quote character rather than a comment.
//
// Matching is greedy, to the last quote on the line, because the reference
// tests only the first and last character of the trimmed value. So
// '"a" b "c"' is one quoted value, not two.
//
// PLUGIN_VALUE_STRING is first so a fully quoted value wins the equal-length
// tie. A value with trailing text ("foo.dll" extra) matches the longer
// PLUGIN_VALUE instead, mirroring the reference refusal to strip quotes there.

mode PLUGIN_VALUE_MODE;

PLG_WS              : [ \t]+ -> type(WS) ;
PLUGIN_VALUE_STRING : '"' ~[\r\n]* '"' | '\'' ~[\r\n]* '\'' ;
PLUGIN_VALUE        : ~[ \t\r\n] ~[\r\n]* ;
PLG_NEWLINE         : ('\r' '\n'? | '\n') -> type(NEWLINE), popMode ;
