/**
 * Basic4GL lexer grammar.
 *
 * Single source of truth for the Basic4GL language token vocabulary.  The
 * generated Basic4GLLexer is consumed by:
 *
 *   1. LanguageSupportTokenMaker  – drives RSyntaxTextArea syntax highlighting
 *   2. Basic4GLLanguageSupport    – drives SymbolIndexer (user functions, labels, variables)
 *
 * Adding a new keyword here automatically gives it correct highlighting and
 * correct exclusion from label/symbol heuristics – no other file needs touching.
 *
 * The grammar is intentionally a lexer-only grammar; a full parser grammar can be
 * layered on top later when AST-level features (e.g. go-to-definition, refactoring)
 * are desired.
 */
lexer grammar Basic4GL;

// ---------------------------------------------------------------------------
// Preprocessor directive  (must precede HASH so '#include' wins max-munch)
// ---------------------------------------------------------------------------

INCLUDE_DIR : '#' I N C L U D E [ \t]* ;

// ---------------------------------------------------------------------------
// Comments
// ---------------------------------------------------------------------------

COMMENT     : '\'' ~[\r\n]* ;

// 'rem' followed by optional whitespace+rest-of-line is a comment.
// Max-munch ensures "remember_var" → IDENTIFIER (longer match wins).
REM_COMMENT : R E M ([ \t] ~[\r\n]*)? ;

// ---------------------------------------------------------------------------
// Keywords  (case-insensitive via letter fragments at the bottom)
// Longer alternatives are listed before shorter ones with the same prefix so
// that ANTLR's max-munch selects the correct rule without ambiguity.
// ---------------------------------------------------------------------------

ELSEIF_KW   : E L S E I F ;
ENDIF_KW    : E N D I F ;
ENDSTRUC_KW : E N D S T R U C ;
FUNCTION_KW : F U N C T I O N ;
GOSUB_KW    : G O S U B ;
GOTO_KW     : G O T O ;
INTEGER_T   : I N T E G E R ;
ALLOC_KW    : A L L O C ;
CONST_KW    : C O N S T ;
DATA_KW     : D A T A ;
DOUBLE_T    : D O U B L E ;
SINGLE_T    : S I N G L E ;
STRING_T    : S T R I N G ;
RESET_KW    : R E S E T ;
RETURN_KW   : R E T U R N ;
STRUC_KW    : S T R U C ;
FALSE_KW    : F A L S E ;
WHILE_KW    : W H I L E ;
WEND_KW     : W E N D ;
AS_KW       : A S ;
DIM_KW      : D I M ;
ELSE_KW     : E L S E ;
END_KW      : E N D ;
FOR_KW      : F O R ;
INT_T       : I N T ;
NEXT_KW     : N E X T ;
NULL_KW     : N U L L ;
READ_KW     : R E A D ;
RUN_KW      : R U N ;
STEP_KW     : S T E P ;
SUB_KW      : S U B ;
THEN_KW     : T H E N ;
TO_KW       : T O ;
TRUE_KW     : T R U E ;
TYPE_KW     : T Y P E ;
AND_KW      : A N D ;
MOD_KW      : M O D ;
NOT_KW      : N O T ;
XOR_KW      : X O R ;
IF_KW       : I F ;
OR_KW       : O R ;

// ---------------------------------------------------------------------------
// Literals
// ---------------------------------------------------------------------------

STRING_LIT  : '"' ~["\r\n]* '"' ;
HEX_LIT     : '0' X [0-9A-Fa-f]+ ;
FLOAT_LIT   : [0-9]+ '.' [0-9]* | '.' [0-9]+ ;
INT_LIT     : [0-9]+ ;

// ---------------------------------------------------------------------------
// Identifiers (catch-all after keywords – order matters)
// ---------------------------------------------------------------------------

IDENTIFIER  : [a-zA-Z_][a-zA-Z_0-9]* ;

// ---------------------------------------------------------------------------
// Operators and punctuation  (multi-char operators before their prefixes)
// ---------------------------------------------------------------------------

LTE         : '<=' ;
GTE         : '>=' ;
NEQ         : '<>' ;
COLON       : ':' ;
LPAREN      : '(' ;
RPAREN      : ')' ;
LBRACKET    : '[' ;
RBRACKET    : ']' ;
COMMA       : ',' ;
DOT         : '.' ;
SEMICOLON   : ';' ;
EQ          : '=' ;
LT          : '<' ;
GT          : '>' ;
PLUS        : '+' ;
MINUS       : '-' ;
STAR        : '*' ;
SLASH       : '/' ;
BACKSLASH   : '\\' ;
CARET       : '^' ;
AT          : '@' ;
BANG        : '!' ;
TILDE       : '~' ;
PERCENT     : '%' ;
PIPE        : '|' ;
HASH        : '#' ;

// ---------------------------------------------------------------------------
// Whitespace
// ---------------------------------------------------------------------------

NEWLINE     : '\r'? '\n' ;
WS          : [ \t]+ ;

// Catch-all so the lexer never hard-errors on unknown characters
UNKNOWN     : . ;

// ---------------------------------------------------------------------------
// Case-insensitive character fragments
// ---------------------------------------------------------------------------

fragment A : [aA] ; fragment B : [bB] ; fragment C : [cC] ;
fragment D : [dD] ; fragment E : [eE] ; fragment F : [fF] ;
fragment G : [gG] ; fragment H : [hH] ; fragment I : [iI] ;
fragment J : [jJ] ; fragment K : [kK] ; fragment L : [lL] ;
fragment M : [mM] ; fragment N : [nN] ; fragment O : [oO] ;
fragment P : [pP] ; fragment Q : [qQ] ; fragment R : [rR] ;
fragment S : [sS] ; fragment T : [tT] ; fragment U : [uU] ;
fragment V : [vV] ; fragment W : [wW] ; fragment X : [xX] ;
fragment Y : [yY] ; fragment Z : [zZ] ;

