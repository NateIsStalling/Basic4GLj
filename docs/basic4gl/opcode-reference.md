The following OpCodes are used by Basic4GL's virtual machine and are defined in [OpCode.java](https://github.com/NateIsStalling/Basic4GLj/blob/main/runtime/src/main/java/com/basic4gl/runtime/types/OpCode.java)

## General

| Mnemonic | OpCode | Description |
| --- | --- | --- |
| NOP | 0x00 | No operation |
| END | 0x01 | End program |
| LOAD_CONST | 0x02 | Load constant into reg |
| LOAD_VAR | 0x03 | Load address of variable into reg |
| LOAD_LOCAL_VAR | 0x04 | Load address of local variable into reg |
| DEREF | 0x05 | Dereference reg (load value at [reg] into reg) |
| ADD_CONST | 0x06 | Add constant to reg. Used to address into a structure |
| ARRAY_INDEX | 0x07 | IN: reg = array index, reg2 = array address. OUT: reg = element address. |
| PUSH | 0x08 | Push reg to stack |
| POP | 0x09 | Pop stack into reg2 |
| SAVE | 0x0A | Save int, real or string in reg into [reg2] |
| COPY | 0x0B | Copy structure at [reg2] into [reg]. Instruction value points to data type. |
| DECLARE | 0x0C | Allocate a variable |
| DECLARE_LOCAL | 0x0D | Allocate a local variable |
| TIMESHARE | 0x0E | Perform a timesharing break |
| FREE_TEMP | 0x0F | Free temporary data |
| ALLOC | 0x10 | Allocate variable memory |
| DATA_READ | 0x11 | Read program data into data at [reg]. Instruction contains target data type. |
| DATA_RESET | 0x12 | Reset program data pointer |
| SAVE_PARAM | 0x13 | Save int, real or string in reg into parameter in pending stack frame. Instruction contains data type. Parameter # is however many parameters  have been set since *CREATE_USER_FRAME* was executed.|
| SAVE_PARAM_PTR | 0x14 | Save pointer to data in [reg] into parameter pointer. |
| COPY_USER_STACK | 0x15 | Copy data at [reg] to top of user stack. reg is then adjusted to point to the data in the user stack. |
| MOVE_TEMP | 0x16 | Free temp data and move data at [reg] into temp data.  (Also handles if [reg] points to temp data). |
| CHECK_PTR | 0x17 |  Check pointer scope before saving to variable.  (Ptr is in reg, variable is [reg2]) |
| CHECK_PTRS | 0x18 | Check all pointers in block at [reg] before copying to [reg2]. Instruction contains data type. |
| REG_DESTRUCTOR | 0x19 | Register string destruction block at [reg] (will be temp or stack depending on reg) |


## Flow control

| Mnemonic | OpCode | Description |
| --- | --- | --- |
| JUMP | 0x40 | Unconditional jump |
| JUMP_TRUE | 0x41 | Jump if reg <> 0 |
| JUMP_FALSE | 0x42 | Jump if reg == 0 |
| CALL_FUNC | 0x43 | Call external function |
| CALL_OPERATOR_FUNC | 0x44 | Call external operator function |
| CALL_DLL | 0x45 | Call DLL function |
| CALL | 0x46 | Call VM function |
| CREATE_USER_FRAME | 0x47 | Create user stack frame in preparation for a call |
| CALL_USER_FUNC | 0x48 | Call user defined function |
| RETURN | 0x49 | Return from VM function |
| RETURN_USER_FUNC | 0x4A | Return from user defined function |
| NO_VALUE_RETURNED | 0x4B | Generates a runtime error if executed |

### 0x4C - 0x50 added after version 2.5.0

| Mnemonic | OpCode | Description |
| --- | --- | --- |
| BINDCODE | 0x4C | Bind a runtime code block to be executed |
| EXEC | 0x4D | Execute runtime code block |
| CREATE_RUNTIME_FRAME | 0x4F | Create a stack frame to call a function/sub in runtime code block |
| END_CALLBACK | 0x50 | End callback initiated by built-in function or DLL function, and return control to that function. |


## Operations

### Mathematical

| Mnemonic | OpCode | Description |
| --- | --- | --- |
| OP_NEG | 0x60 |  |
| OP_PLUS | 0x61 | Doubles as string concatenation |
| OP_MINUS | 0x62 |  |
| OP_TIMES | 0x63 |  |
| OP_DIV | 0x64 |  |
| OP_MOD | 0x65 |  |

### Logical

| Mnemonic | OpCode | Description |
| --- | --- | --- |
| OP_NOT | 0x80 |  |
| OP_EQUAL | 0x81 |  |
| OP_NOT_EQUAL | 0x82 |  |
| OP_GREATER | 0x83 |  |
| OP_GREATER_EQUAL | 0x84 |  |
| OP_LESS | 0x85 |  |
| OP_LESS_EQUAL | 0x86 |  |
| OP_AND | 0x87 |  |
| OP_OR | 0x88 |  |
| OP_XOR | 0x89 |  |

### Conversion

| Mnemonic | OpCode | Description |
| --- | --- | --- |
| CONV_INT_REAL | 0xA0 | Convert integer in reg to real |
| CONV_INT_STRING | 0xA1 | Convert integer in reg to string |
| CONV_REAL_STRING | 0xA2 | Convert real in reg to string |
| CONV_REAL_INT | 0xA3 |  |
| CONV_INT_REAL2 | 0xA4 | Convert integer in reg2 to real |
| CONV_INT_STRING2 | 0xA5 | Convert integer in reg2 to string |
| CONV_REAL_STRING2 | 0xA6 | Convert real in reg2 to string |
| CONV_REAL_INT2 | 0xA7 |  |


## Misc Routine

| Mnemonic | OpCode | Description |
| --- | --- | --- |
| RUN | 0xC0 | Restart program. Re-initializes variables, display, state e.t.c |


## Debugging

| Mnemonic | OpCode | Description |
| --- | --- | --- |
| BREAKPT | 0xE0 | Breakpoint |
