		.DATA
delay_counter 		.FILL #0
		.DATA
wave_counter 		.FILL #1
		.DATA
cooldown_counter 		.FILL #0
		.DATA
cursorImage 		.FILL #0
		.FILL #0
		.FILL #24
		.FILL #60
		.FILL #60
		.FILL #24
		.FILL #0
		.FILL #0
;;;;;;;;;;;;;;;;;;;;;;;;;;;;printnum;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
printnum
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	ADD R6, R6, #-13	;; allocate stack space for local variables
	;; function body
	LDR R7, R5, #3
	CONST R3, #0
	CMP R7, R3
	BRnp L6_mc
	LEA R7, L8_mc
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR lc4_puts
	ADD R6, R6, #1	;; free space for arguments
	JMP L5_mc
L6_mc
	LDR R7, R5, #3
	CONST R3, #0
	CMP R7, R3
	BRzp L10_mc
	LDR R7, R5, #3
	NOT R7,R7
	ADD R7,R7,#1
	STR R7, R5, #-13
	JMP L11_mc
L10_mc
	LDR R7, R5, #3
	STR R7, R5, #-13
L11_mc
	LDR R7, R5, #-13
	STR R7, R5, #-1
	LDR R7, R5, #-1
	CONST R3, #0
	CMP R7, R3
	BRzp L12_mc
	LEA R7, L14_mc
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR lc4_puts
	ADD R6, R6, #1	;; free space for arguments
	JMP L5_mc
L12_mc
	ADD R7, R5, #-12
	ADD R7, R7, #10
	STR R7, R5, #-2
	LDR R7, R5, #-2
	ADD R7, R7, #-1
	STR R7, R5, #-2
	CONST R3, #0
	STR R3, R7, #0
	JMP L16_mc
L15_mc
	LDR R7, R5, #-2
	ADD R7, R7, #-1
	STR R7, R5, #-2
	LDR R3, R5, #-1
	CONST R2, #10
	MOD R3, R3, R2
	CONST R2, #48
	ADD R3, R3, R2
	STR R3, R7, #0
	LDR R7, R5, #-1
	CONST R3, #10
	DIV R7, R7, R3
	STR R7, R5, #-1
L16_mc
	LDR R7, R5, #-1
	CONST R3, #0
	CMP R7, R3
	BRnp L15_mc
	LDR R7, R5, #3
	CONST R3, #0
	CMP R7, R3
	BRzp L18_mc
	LDR R7, R5, #-2
	ADD R7, R7, #-1
	STR R7, R5, #-2
	CONST R3, #45
	STR R3, R7, #0
L18_mc
	LDR R7, R5, #-2
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR lc4_puts
	ADD R6, R6, #1	;; free space for arguments
L5_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;endl;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
endl
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	;; function body
	LEA R7, L21_mc
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR lc4_puts
	ADD R6, R6, #1	;; free space for arguments
L20_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;UpdateLine;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
UpdateLine
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	;; function body
	ADD R7, R5, #3
	LDR R7, R7, #3
	ADD R3, R5, #3
	LDR R3, R3, #5
	CMP R7, R3
	BRnp L23_mc
	ADD R7, R5, #3
	LDR R7, R7, #4
	ADD R3, R5, #3
	LDR R3, R3, #6
	CMP R7, R3
	BRnp L23_mc
	JMP L22_mc
L23_mc
	LDR R7, R5, #18
	ADD R6, R6, #-1
	STR R7, R6, #0
	ADD R7, R5, #3
	LDR R7, R7, #4
	ADD R6, R6, #-1
	STR R7, R6, #0
	ADD R7, R5, #3
	LDR R7, R7, #3
	ADD R6, R6, #-1
	STR R7, R6, #0
	ADD R7, R5, #3
	LDR R7, R7, #2
	ADD R6, R6, #-1
	STR R7, R6, #0
	ADD R7, R5, #3
	LDR R7, R7, #1
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR lc4_draw_line
	ADD R6, R6, #5	;; free space for arguments
L22_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;DrawCursor;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
DrawCursor
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	;; function body
	LEA R7, cursorImage
	ADD R6, R6, #-1
	STR R7, R6, #0
	CONST R7, #255
	HICONST R7, #255
	ADD R6, R6, #-1
	STR R7, R6, #0
	LEA R7, cursor
	LDR R3, R7, #1
	ADD R6, R6, #-1
	STR R3, R6, #0
	LDR R7, R7, #0
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR lc4_draw_sprite
	ADD R6, R6, #4	;; free space for arguments
L25_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;DrawMissileCommand;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
DrawMissileCommand
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	;; function body
	LEA R7, missileCommand
	LDR R7, R7, #1
	CONST R3, #8
	CMP R7, R3
	BRnp L27_mc
	LEA R7, missileCommand
	CONST R3, #66
	STR R3, R7, #3
	LEA R7, missileCommand
	CONST R3, #165
	STR R3, R7, #4
	LEA R7, missileCommand
	CONST R3, #66
	STR R3, R7, #5
	LEA R7, missileCommand
	CONST R3, #165
	STR R3, R7, #6
	LEA R7, missileCommand
	CONST R3, #66
	STR R3, R7, #7
	LEA R7, missileCommand
	CONST R3, #165
	STR R3, R7, #8
	LEA R7, missileCommand
	CONST R3, #66
	STR R3, R7, #9
	LEA R7, missileCommand
	CONST R3, #165
	STR R3, R7, #10
	JMP L28_mc
L27_mc
	LEA R7, missileCommand
	LDR R7, R7, #1
	CONST R3, #7
	CMP R7, R3
	BRnp L29_mc
	LEA R7, missileCommand
	CONST R3, #2
	STR R3, R7, #3
	LEA R7, missileCommand
	CONST R3, #5
	STR R3, R7, #4
	LEA R7, missileCommand
	CONST R3, #66
	STR R3, R7, #5
	LEA R7, missileCommand
	CONST R3, #165
	STR R3, R7, #6
	LEA R7, missileCommand
	CONST R3, #66
	STR R3, R7, #7
	LEA R7, missileCommand
	CONST R3, #165
	STR R3, R7, #8
	LEA R7, missileCommand
	CONST R3, #66
	STR R3, R7, #9
	LEA R7, missileCommand
	CONST R3, #165
	STR R3, R7, #10
	JMP L30_mc
L29_mc
	LEA R7, missileCommand
	LDR R7, R7, #1
	CONST R3, #6
	CMP R7, R3
	BRnp L31_mc
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #3
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #4
	LEA R7, missileCommand
	CONST R3, #66
	STR R3, R7, #5
	LEA R7, missileCommand
	CONST R3, #165
	STR R3, R7, #6
	LEA R7, missileCommand
	CONST R3, #66
	STR R3, R7, #7
	LEA R7, missileCommand
	CONST R3, #165
	STR R3, R7, #8
	LEA R7, missileCommand
	CONST R3, #66
	STR R3, R7, #9
	LEA R7, missileCommand
	CONST R3, #165
	STR R3, R7, #10
	JMP L32_mc
L31_mc
	LEA R7, missileCommand
	LDR R7, R7, #1
	CONST R3, #5
	CMP R7, R3
	BRnp L33_mc
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #3
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #4
	LEA R7, missileCommand
	CONST R3, #2
	STR R3, R7, #5
	LEA R7, missileCommand
	CONST R3, #5
	STR R3, R7, #6
	LEA R7, missileCommand
	CONST R3, #66
	STR R3, R7, #7
	LEA R7, missileCommand
	CONST R3, #165
	STR R3, R7, #8
	LEA R7, missileCommand
	CONST R3, #66
	STR R3, R7, #9
	LEA R7, missileCommand
	CONST R3, #165
	STR R3, R7, #10
	JMP L34_mc
L33_mc
	LEA R7, missileCommand
	LDR R7, R7, #1
	CONST R3, #4
	CMP R7, R3
	BRnp L35_mc
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #3
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #4
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #5
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #6
	LEA R7, missileCommand
	CONST R3, #66
	STR R3, R7, #7
	LEA R7, missileCommand
	CONST R3, #165
	STR R3, R7, #8
	LEA R7, missileCommand
	CONST R3, #66
	STR R3, R7, #9
	LEA R7, missileCommand
	CONST R3, #165
	STR R3, R7, #10
	JMP L36_mc
L35_mc
	LEA R7, missileCommand
	LDR R7, R7, #1
	CONST R3, #3
	CMP R7, R3
	BRnp L37_mc
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #3
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #4
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #5
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #6
	LEA R7, missileCommand
	CONST R3, #2
	STR R3, R7, #7
	LEA R7, missileCommand
	CONST R3, #5
	STR R3, R7, #8
	LEA R7, missileCommand
	CONST R3, #66
	STR R3, R7, #9
	LEA R7, missileCommand
	CONST R3, #165
	STR R3, R7, #10
	JMP L38_mc
L37_mc
	LEA R7, missileCommand
	LDR R7, R7, #1
	CONST R3, #2
	CMP R7, R3
	BRnp L39_mc
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #3
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #4
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #5
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #6
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #7
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #8
	LEA R7, missileCommand
	CONST R3, #66
	STR R3, R7, #9
	LEA R7, missileCommand
	CONST R3, #165
	STR R3, R7, #10
	JMP L40_mc
L39_mc
	LEA R7, missileCommand
	LDR R7, R7, #1
	CONST R3, #1
	CMP R7, R3
	BRnp L41_mc
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #3
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #4
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #5
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #6
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #7
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #8
	LEA R7, missileCommand
	CONST R3, #2
	STR R3, R7, #9
	LEA R7, missileCommand
	CONST R3, #5
	STR R3, R7, #10
	JMP L42_mc
L41_mc
	LEA R7, missileCommand
	LDR R7, R7, #1
	CONST R3, #0
	CMP R7, R3
	BRnp L43_mc
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #3
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #4
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #5
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #6
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #7
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #8
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #9
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #10
L43_mc
L42_mc
L40_mc
L38_mc
L36_mc
L34_mc
L32_mc
L30_mc
L28_mc
	LEA R7, missileCommand
	ADD R3, R7, #3
	ADD R6, R6, #-1
	STR R3, R6, #0
	CONST R3, #255
	HICONST R3, #255
	ADD R6, R6, #-1
	STR R3, R6, #0
	CONST R3, #116
	ADD R6, R6, #-1
	STR R3, R6, #0
	LDR R7, R7, #2
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR lc4_draw_sprite
	ADD R6, R6, #4	;; free space for arguments
L26_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;DrawCities;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
DrawCities
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	ADD R6, R6, #-1	;; allocate stack space for local variables
	;; function body
	CONST R7, #0
	STR R7, R5, #-1
	CONST R7, #0
	STR R7, R5, #-1
L46_mc
	CONST R7, #10
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, cities
	ADD R7, R7, R3
	ADD R3, R7, #2
	ADD R6, R6, #-1
	STR R3, R6, #0
	CONST R3, #0
	HICONST R3, #124
	ADD R6, R6, #-1
	STR R3, R6, #0
	CONST R3, #116
	ADD R6, R6, #-1
	STR R3, R6, #0
	LDR R7, R7, #1
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR lc4_draw_sprite
	ADD R6, R6, #4	;; free space for arguments
L47_mc
	LDR R7, R5, #-1
	ADD R7, R7, #1
	STR R7, R5, #-1
	LDR R7, R5, #-1
	CONST R3, #2
	CMP R7, R3
	BRn L46_mc
L45_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;DrawMeteors;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
DrawMeteors
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	ADD R6, R6, #-1	;; allocate stack space for local variables
	;; function body
	CONST R7, #0
	STR R7, R5, #-1
	CONST R7, #0
	STR R7, R5, #-1
L51_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	LDR R7, R7, #0
	CONST R3, #0
	CMP R7, R3
	BRnp L55_mc
	CONST R7, #240
	HICONST R7, #127
	ADD R6, R6, #-1
	STR R7, R6, #0
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	LDR R3, R7, #4
	ADD R6, R6, #-1
	STR R3, R6, #0
	LDR R3, R7, #3
	ADD R6, R6, #-1
	STR R3, R6, #0
	LDR R3, R7, #2
	ADD R6, R6, #-1
	STR R3, R6, #0
	LDR R7, R7, #1
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR lc4_draw_line
	ADD R6, R6, #5	;; free space for arguments
L55_mc
L52_mc
	LDR R7, R5, #-1
	ADD R7, R7, #1
	STR R7, R5, #-1
	LDR R7, R5, #-1
	CONST R3, #3
	CMP R7, R3
	BRn L51_mc
L50_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;DrawMissiles;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
DrawMissiles
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	ADD R6, R6, #-1	;; allocate stack space for local variables
	;; function body
	CONST R7, #0
	STR R7, R5, #-1
	CONST R7, #0
	STR R7, R5, #-1
L58_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	LDR R7, R7, #0
	CONST R3, #0
	CMP R7, R3
	BRnp L62_mc
	CONST R7, #0
	HICONST R7, #51
	ADD R6, R6, #-1
	STR R7, R6, #0
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	LDR R3, R7, #4
	ADD R6, R6, #-1
	STR R3, R6, #0
	LDR R3, R7, #3
	ADD R6, R6, #-1
	STR R3, R6, #0
	LDR R3, R7, #2
	ADD R6, R6, #-1
	STR R3, R6, #0
	LDR R7, R7, #1
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR lc4_draw_line
	ADD R6, R6, #5	;; free space for arguments
L62_mc
L59_mc
	LDR R7, R5, #-1
	ADD R7, R7, #1
	STR R7, R5, #-1
	LDR R7, R5, #-1
	CONST R3, #3
	CMP R7, R3
	BRn L58_mc
L57_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;Redraw;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
Redraw
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	;; function body
	JSR lc4_reset_vmem
	ADD R6, R6, #0	;; free space for arguments
	JSR DrawCursor
	ADD R6, R6, #0	;; free space for arguments
	JSR DrawMissileCommand
	ADD R6, R6, #0	;; free space for arguments
	JSR DrawCities
	ADD R6, R6, #0	;; free space for arguments
	JSR DrawMissiles
	ADD R6, R6, #0	;; free space for arguments
	JSR DrawMeteors
	ADD R6, R6, #0	;; free space for arguments
	JSR lc4_blt_vmem
	ADD R6, R6, #0	;; free space for arguments
L64_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;reset;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
reset
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	;; function body
	JSR lc4_reset_vmem
	ADD R6, R6, #0	;; free space for arguments
	JSR lc4_blt_vmem
	ADD R6, R6, #0	;; free space for arguments
L65_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;ResetGame;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
ResetGame
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	ADD R6, R6, #-1	;; allocate stack space for local variables
	;; function body
	CONST R7, #0
	STR R7, R5, #-1
	CONST R7, #0
	STR R7, R5, #-1
L67_mc
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #0
	CONST R3, #8
	STR R3, R7, #1
	LEA R7, missileCommand
	CONST R3, #60
	STR R3, R7, #2
L68_mc
	LDR R7, R5, #-1
	ADD R7, R7, #1
	STR R7, R5, #-1
	LDR R7, R5, #-1
	CONST R3, #1
	CMP R7, R3
	BRn L67_mc
	LEA R7, cities
	CONST R3, #20
	STR R3, R7, #1
	LEA R7, cities
	CONST R3, #100
	STR R3, R7, #11
	CONST R7, #0
	STR R7, R5, #-1
L71_mc
	CONST R7, #10
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, cities
	ADD R7, R7, R3
	CONST R3, #0
	STR R3, R7, #0
	CONST R7, #10
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, cities
	ADD R7, R7, R3
	CONST R3, #24
	STR R3, R7, #2
	CONST R7, #10
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, cities
	ADD R7, R7, R3
	CONST R3, #24
	STR R3, R7, #3
	CONST R7, #10
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, cities
	ADD R7, R7, R3
	CONST R3, #59
	STR R3, R7, #4
	CONST R7, #10
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, cities
	ADD R7, R7, R3
	CONST R3, #59
	STR R3, R7, #5
	CONST R7, #10
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, cities
	ADD R7, R7, R3
	CONST R3, #125
	STR R3, R7, #6
	CONST R7, #10
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, cities
	ADD R7, R7, R3
	CONST R3, #125
	STR R3, R7, #7
	CONST R7, #10
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, cities
	ADD R7, R7, R3
	CONST R3, #255
	STR R3, R7, #8
	CONST R7, #10
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, cities
	ADD R7, R7, R3
	CONST R3, #255
	STR R3, R7, #9
L72_mc
	LDR R7, R5, #-1
	ADD R7, R7, #1
	STR R7, R5, #-1
	LDR R7, R5, #-1
	CONST R3, #2
	CMP R7, R3
	BRn L71_mc
	CONST R7, #0
	STR R7, R5, #-1
L75_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	CONST R3, #1
	STR R3, R7, #0
L76_mc
	LDR R7, R5, #-1
	ADD R7, R7, #1
	STR R7, R5, #-1
	LDR R7, R5, #-1
	CONST R3, #3
	CMP R7, R3
	BRn L75_mc
	CONST R7, #0
	STR R7, R5, #-1
L79_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	CONST R3, #1
	STR R3, R7, #0
L80_mc
	LDR R7, R5, #-1
	ADD R7, R7, #1
	STR R7, R5, #-1
	LDR R7, R5, #-1
	CONST R3, #3
	CMP R7, R3
	BRn L79_mc
	LEA R7, targets
	CONST R3, #20
	STR R3, R7, #0
	CONST R3, #60
	STR R3, R7, #1
	LEA R7, targets
	CONST R3, #100
	STR R3, R7, #2
	LEA R7, wave_counter
	CONST R3, #1
	STR R3, R7, #0
	LEA R7, meteors_per_wave
	CONST R3, #5
	STR R3, R7, #0
	LEA R3, meteors_left_per_wave
	LDR R7, R7, #0
	STR R7, R3, #0
L66_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;abs;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
abs
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	;; function body
	LDR R7, R5, #3
	CONST R3, #0
	CMP R7, R3
	BRzp L84_mc
	LDR R7, R5, #3
	NOT R7,R7
	ADD R7,R7,#1
	JMP L83_mc
L84_mc
	LDR R7, R5, #3
L83_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;BlowUpMeteor;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
BlowUpMeteor
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	;; function body
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	CONST R3, #1
	STR R3, R7, #0
	LEA R7, meteors_left_per_wave
	LDR R3, R7, #0
	ADD R3, R3, #-1
	STR R3, R7, #0
L86_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;CheckForMeteorCollsion;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
CheckForMeteorCollsion
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	;; function body
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	LDR R7, R7, #5
	CONST R3, #20
	CMP R7, R3
	BRnp L88_mc
	LEA R7, cities
	CONST R3, #1
	STR R3, R7, #0
	CONST R3, #0
	STR R3, R7, #2
	LEA R7, cities
	CONST R3, #0
	STR R3, R7, #3
	LEA R7, cities
	CONST R3, #0
	STR R3, R7, #4
	LEA R7, cities
	CONST R3, #0
	STR R3, R7, #5
	LEA R7, cities
	CONST R3, #127
	STR R3, R7, #6
	LEA R7, cities
	CONST R3, #207
	STR R3, R7, #7
	LEA R7, cities
	CONST R3, #255
	STR R3, R7, #8
	LEA R7, cities
	CONST R3, #255
	STR R3, R7, #9
	JMP L89_mc
L88_mc
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	LDR R7, R7, #5
	CONST R3, #100
	CMP R7, R3
	BRnp L90_mc
	LEA R7, cities
	CONST R3, #1
	STR R3, R7, #10
	LEA R7, cities
	CONST R3, #0
	STR R3, R7, #12
	LEA R7, cities
	CONST R3, #0
	STR R3, R7, #13
	LEA R7, cities
	CONST R3, #0
	STR R3, R7, #14
	LEA R7, cities
	CONST R3, #0
	STR R3, R7, #15
	LEA R7, cities
	CONST R3, #127
	STR R3, R7, #16
	LEA R7, cities
	CONST R3, #207
	STR R3, R7, #17
	LEA R7, cities
	CONST R3, #255
	STR R3, R7, #18
	LEA R7, cities
	CONST R3, #255
	STR R3, R7, #19
	JMP L91_mc
L90_mc
	LEA R7, missileCommand
	CONST R3, #1
	STR R3, R7, #0
	CONST R3, #0
	STR R3, R7, #1
L91_mc
L89_mc
	LDR R7, R5, #3
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR BlowUpMeteor
	ADD R6, R6, #1	;; free space for arguments
L87_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;BlowUpMissile;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
BlowUpMissile
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	ADD R6, R6, #-1	;; allocate stack space for local variables
	;; function body
	CONST R7, #0
	STR R7, R5, #-1
	CONST R7, #0
	STR R7, R5, #-1
L93_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	LDR R7, R7, #0
	CONST R3, #0
	CMP R7, R3
	BRnp L97_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R3, R7, R3
	LEA R2, meteors
	ADD R3, R3, R2
	LDR R3, R3, #3
	LDR R2, R5, #3
	MUL R7, R7, R2
	LEA R2, missiles
	ADD R7, R7, R2
	LDR R7, R7, #5
	SUB R7, R3, R7
	MUL R7, R7, R7
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR abs
	LDR R7, R6, #-1	;; grab return value
	ADD R6, R6, #1	;; free space for arguments
	CONST R3, #15
	LDR R2, R5, #-1
	MUL R2, R3, R2
	LEA R1, meteors
	ADD R2, R2, R1
	LDR R2, R2, #4
	LDR R1, R5, #3
	MUL R3, R3, R1
	LEA R1, missiles
	ADD R3, R3, R1
	LDR R3, R3, #6
	SUB R3, R2, R3
	MUL R3, R3, R3
	ADD R7, R7, R3
	CONST R3, #250
	CMPU R7, R3
	BRzp L99_mc
	LDR R7, R5, #-1
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR BlowUpMeteor
	ADD R6, R6, #1	;; free space for arguments
L99_mc
L97_mc
L94_mc
	LDR R7, R5, #-1
	ADD R7, R7, #1
	STR R7, R5, #-1
	LDR R7, R5, #-1
	CONST R3, #3
	CMP R7, R3
	BRn L93_mc
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	CONST R3, #1
	STR R3, R7, #0
L92_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;UpdateProjectiles;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
UpdateProjectiles
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	ADD R6, R6, #-1	;; allocate stack space for local variables
	;; function body
	CONST R7, #0
	STR R7, R5, #-1
	CONST R7, #0
	STR R7, R5, #-1
L102_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	LDR R7, R7, #0
	CONST R3, #0
	CMP R7, R3
	BRnp L106_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	ADD R3, R7, #14
	LDR R2, R3, #0
	LDR R7, R7, #13
	ADD R7, R2, R7
	STR R7, R3, #0
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	LDR R3, R7, #14
	LDR R7, R7, #12
	CMP R3, R7
	BRn L108_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	ADD R3, R7, #14
	LDR R2, R3, #0
	LDR R7, R7, #12
	SUB R7, R2, R7
	STR R7, R3, #0
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	ADD R3, R7, #3
	LDR R2, R3, #0
	LDR R7, R7, #7
	ADD R7, R2, R7
	STR R7, R3, #0
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	ADD R3, R7, #4
	LDR R2, R3, #0
	LDR R7, R7, #8
	ADD R7, R2, R7
	STR R7, R3, #0
	JMP L109_mc
L108_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	ADD R3, R7, #3
	LDR R2, R3, #0
	LDR R7, R7, #9
	ADD R7, R2, R7
	STR R7, R3, #0
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	ADD R3, R7, #4
	LDR R2, R3, #0
	LDR R7, R7, #10
	ADD R7, R2, R7
	STR R7, R3, #0
L109_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	LDR R3, R7, #11
	LDR R7, R7, #12
	CMP R3, R7
	BRnz L110_mc
	LDR R7, R5, #-1
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR BlowUpMissile
	ADD R6, R6, #1	;; free space for arguments
L110_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	ADD R7, R7, #11
	LDR R3, R7, #0
	ADD R3, R3, #1
	STR R3, R7, #0
L106_mc
L103_mc
	LDR R7, R5, #-1
	ADD R7, R7, #1
	STR R7, R5, #-1
	LDR R7, R5, #-1
	CONST R3, #3
	CMP R7, R3
	BRn L102_mc
	LEA R7, delay_counter
	LDR R7, R7, #0
	CONST R3, #5
	CMP R7, R3
	BRnp L112_mc
	CONST R7, #0
	STR R7, R5, #-1
L114_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	LDR R7, R7, #0
	CONST R3, #0
	CMP R7, R3
	BRnp L118_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	ADD R3, R7, #14
	LDR R2, R3, #0
	LDR R7, R7, #13
	ADD R7, R2, R7
	STR R7, R3, #0
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	LDR R3, R7, #14
	LDR R7, R7, #12
	CMP R3, R7
	BRn L120_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	ADD R3, R7, #14
	LDR R2, R3, #0
	LDR R7, R7, #12
	SUB R7, R2, R7
	STR R7, R3, #0
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	ADD R3, R7, #3
	LDR R2, R3, #0
	LDR R7, R7, #7
	ADD R7, R2, R7
	STR R7, R3, #0
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	ADD R3, R7, #4
	LDR R2, R3, #0
	LDR R7, R7, #8
	ADD R7, R2, R7
	STR R7, R3, #0
	JMP L121_mc
L120_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	ADD R3, R7, #3
	LDR R2, R3, #0
	LDR R7, R7, #9
	ADD R7, R2, R7
	STR R7, R3, #0
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	ADD R3, R7, #4
	LDR R2, R3, #0
	LDR R7, R7, #10
	ADD R7, R2, R7
	STR R7, R3, #0
L121_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	LDR R3, R7, #11
	LDR R7, R7, #12
	CMP R3, R7
	BRnz L122_mc
	LDR R7, R5, #-1
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR CheckForMeteorCollsion
	ADD R6, R6, #1	;; free space for arguments
L122_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	ADD R7, R7, #11
	LDR R3, R7, #0
	ADD R3, R3, #1
	STR R3, R7, #0
L118_mc
L115_mc
	LDR R7, R5, #-1
	ADD R7, R7, #1
	STR R7, R5, #-1
	LDR R7, R5, #-1
	CONST R3, #3
	CMP R7, R3
	BRn L114_mc
	LEA R7, delay_counter
	CONST R3, #0
	STR R3, R7, #0
	JMP L113_mc
L112_mc
	LEA R7, delay_counter
	LDR R3, R7, #0
	ADD R3, R3, #1
	STR R3, R7, #0
L113_mc
L101_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;CalculateMissileMovement;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
CalculateMissileMovement
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	ADD R6, R6, #-6	;; allocate stack space for local variables
	;; function body
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	LDR R3, R7, #5
	LDR R7, R7, #1
	SUB R7, R3, R7
	STR R7, R5, #-1
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	LDR R3, R7, #6
	LDR R7, R7, #2
	SUB R7, R3, R7
	STR R7, R5, #-2
	CONST R7, #0
	STR R7, R5, #-4
	CONST R7, #0
	STR R7, R5, #-5
	CONST R7, #0
	STR R7, R5, #-3
	CONST R7, #0
	STR R7, R5, #-6
	LDR R7, R5, #-1
	CONST R3, #0
	CMP R7, R3
	BRzp L125_mc
	CONST R7, #-1
	STR R7, R5, #-4
L125_mc
	LDR R7, R5, #-1
	CONST R3, #0
	CMP R7, R3
	BRnz L127_mc
	CONST R7, #1
	STR R7, R5, #-4
L127_mc
	LDR R7, R5, #-2
	CONST R3, #0
	CMP R7, R3
	BRzp L129_mc
	CONST R7, #-1
	STR R7, R5, #-5
L129_mc
	LDR R7, R5, #-2
	CONST R3, #0
	CMP R7, R3
	BRnz L131_mc
	CONST R7, #1
	STR R7, R5, #-5
L131_mc
	LDR R7, R5, #-1
	CONST R3, #0
	CMP R7, R3
	BRzp L133_mc
	CONST R7, #-1
	STR R7, R5, #-3
L133_mc
	LDR R7, R5, #-1
	CONST R3, #0
	CMP R7, R3
	BRnz L135_mc
	CONST R7, #1
	STR R7, R5, #-3
L135_mc
	LDR R7, R5, #-1
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR abs
	LDR R7, R6, #-1	;; grab return value
	ADD R6, R6, #1	;; free space for arguments
	CONST R3, #15
	LDR R2, R5, #3
	MUL R3, R3, R2
	LEA R2, missiles
	ADD R3, R3, R2
	STR R7, R3, #12
	LDR R7, R5, #-2
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR abs
	LDR R7, R6, #-1	;; grab return value
	ADD R6, R6, #1	;; free space for arguments
	CONST R3, #15
	LDR R2, R5, #3
	MUL R3, R3, R2
	LEA R2, missiles
	ADD R3, R3, R2
	STR R7, R3, #13
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	LDR R3, R7, #12
	LDR R7, R7, #13
	CMP R3, R7
	BRp L137_mc
	LDR R7, R5, #-2
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR abs
	LDR R7, R6, #-1	;; grab return value
	ADD R6, R6, #1	;; free space for arguments
	CONST R3, #15
	LDR R2, R5, #3
	MUL R3, R3, R2
	LEA R2, missiles
	ADD R3, R3, R2
	STR R7, R3, #12
	LDR R7, R5, #-1
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR abs
	LDR R7, R6, #-1	;; grab return value
	ADD R6, R6, #1	;; free space for arguments
	CONST R3, #15
	LDR R2, R5, #3
	MUL R3, R3, R2
	LEA R2, missiles
	ADD R3, R3, R2
	STR R7, R3, #13
	LDR R7, R5, #-2
	CONST R3, #0
	CMP R7, R3
	BRzp L139_mc
	CONST R7, #-1
	STR R7, R5, #-6
L139_mc
	LDR R7, R5, #-2
	CONST R3, #0
	CMP R7, R3
	BRnz L141_mc
	CONST R7, #1
	STR R7, R5, #-6
L141_mc
	CONST R7, #0
	STR R7, R5, #-3
L137_mc
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	LDR R3, R5, #-4
	STR R3, R7, #7
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	LDR R3, R5, #-5
	STR R3, R7, #8
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	LDR R3, R5, #-3
	STR R3, R7, #9
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	LDR R3, R5, #-6
	STR R3, R7, #10
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	LDR R3, R7, #12
	SRA R3, R3, #1
	STR R3, R7, #14
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	CONST R3, #0
	STR R3, R7, #11
L124_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;CalculateMeteorMovement;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
CalculateMeteorMovement
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	ADD R6, R6, #-6	;; allocate stack space for local variables
	;; function body
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	LDR R3, R7, #5
	LDR R7, R7, #1
	SUB R7, R3, R7
	STR R7, R5, #-1
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	LDR R3, R7, #6
	LDR R7, R7, #2
	SUB R7, R3, R7
	STR R7, R5, #-2
	CONST R7, #0
	STR R7, R5, #-4
	CONST R7, #0
	STR R7, R5, #-5
	CONST R7, #0
	STR R7, R5, #-3
	CONST R7, #0
	STR R7, R5, #-6
	LDR R7, R5, #-1
	CONST R3, #0
	CMP R7, R3
	BRzp L144_mc
	CONST R7, #-1
	STR R7, R5, #-4
L144_mc
	LDR R7, R5, #-1
	CONST R3, #0
	CMP R7, R3
	BRnz L146_mc
	CONST R7, #1
	STR R7, R5, #-4
L146_mc
	LDR R7, R5, #-2
	CONST R3, #0
	CMP R7, R3
	BRzp L148_mc
	CONST R7, #-1
	STR R7, R5, #-5
L148_mc
	LDR R7, R5, #-2
	CONST R3, #0
	CMP R7, R3
	BRnz L150_mc
	CONST R7, #1
	STR R7, R5, #-5
L150_mc
	LDR R7, R5, #-1
	CONST R3, #0
	CMP R7, R3
	BRzp L152_mc
	CONST R7, #-1
	STR R7, R5, #-3
L152_mc
	LDR R7, R5, #-1
	CONST R3, #0
	CMP R7, R3
	BRnz L154_mc
	CONST R7, #1
	STR R7, R5, #-3
L154_mc
	LDR R7, R5, #-1
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR abs
	LDR R7, R6, #-1	;; grab return value
	ADD R6, R6, #1	;; free space for arguments
	CONST R3, #15
	LDR R2, R5, #3
	MUL R3, R3, R2
	LEA R2, meteors
	ADD R3, R3, R2
	STR R7, R3, #12
	LDR R7, R5, #-2
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR abs
	LDR R7, R6, #-1	;; grab return value
	ADD R6, R6, #1	;; free space for arguments
	CONST R3, #15
	LDR R2, R5, #3
	MUL R3, R3, R2
	LEA R2, meteors
	ADD R3, R3, R2
	STR R7, R3, #13
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	LDR R3, R7, #12
	LDR R7, R7, #13
	CMP R3, R7
	BRp L156_mc
	LDR R7, R5, #-2
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR abs
	LDR R7, R6, #-1	;; grab return value
	ADD R6, R6, #1	;; free space for arguments
	CONST R3, #15
	LDR R2, R5, #3
	MUL R3, R3, R2
	LEA R2, meteors
	ADD R3, R3, R2
	STR R7, R3, #12
	LDR R7, R5, #-1
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR abs
	LDR R7, R6, #-1	;; grab return value
	ADD R6, R6, #1	;; free space for arguments
	CONST R3, #15
	LDR R2, R5, #3
	MUL R3, R3, R2
	LEA R2, meteors
	ADD R3, R3, R2
	STR R7, R3, #13
	LDR R7, R5, #-2
	CONST R3, #0
	CMP R7, R3
	BRzp L158_mc
	CONST R7, #-1
	STR R7, R5, #-6
L158_mc
	LDR R7, R5, #-2
	CONST R3, #0
	CMP R7, R3
	BRnz L160_mc
	CONST R7, #1
	STR R7, R5, #-6
L160_mc
	CONST R7, #0
	STR R7, R5, #-3
L156_mc
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	LDR R3, R5, #-4
	STR R3, R7, #7
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	LDR R3, R5, #-5
	STR R3, R7, #8
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	LDR R3, R5, #-3
	STR R3, R7, #9
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	LDR R3, R5, #-6
	STR R3, R7, #10
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	LDR R3, R7, #12
	SRA R3, R3, #1
	STR R3, R7, #14
	CONST R7, #15
	LDR R3, R5, #3
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	CONST R3, #0
	STR R3, R7, #11
L143_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;CreateNewMissile;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
CreateNewMissile
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	ADD R6, R6, #-1	;; allocate stack space for local variables
	;; function body
	CONST R7, #0
	STR R7, R5, #-1
	CONST R7, #0
	STR R7, R5, #-1
L163_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	LDR R7, R7, #0
	CONST R3, #1
	CMP R7, R3
	BRnp L167_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	CONST R3, #0
	STR R3, R7, #0
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	LEA R3, missileCommand
	LDR R3, R3, #2
	ADD R3, R3, #4
	STR R3, R7, #1
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	CONST R3, #120
	STR R3, R7, #2
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	LDR R3, R7, #1
	STR R3, R7, #3
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	LDR R3, R7, #2
	STR R3, R7, #4
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	LEA R3, cursor
	LDR R3, R3, #0
	STR R3, R7, #5
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, missiles
	ADD R7, R7, R3
	LEA R3, cursor
	LDR R3, R3, #1
	STR R3, R7, #6
	LDR R7, R5, #-1
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR CalculateMissileMovement
	ADD R6, R6, #1	;; free space for arguments
	LEA R7, missileCommand
	ADD R7, R7, #1
	LDR R3, R7, #0
	ADD R3, R3, #-1
	STR R3, R7, #0
	JMP L162_mc
L167_mc
L164_mc
	LDR R7, R5, #-1
	ADD R7, R7, #1
	STR R7, R5, #-1
	LDR R7, R5, #-1
	CONST R3, #3
	CMP R7, R3
	BRn L163_mc
L162_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;CreateNewMeteors;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
CreateNewMeteors
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	ADD R6, R6, #-1	;; allocate stack space for local variables
	;; function body
	CONST R7, #0
	STR R7, R5, #-1
	CONST R7, #0
	STR R7, R5, #-1
L170_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	LDR R7, R7, #0
	CONST R3, #1
	CMP R7, R3
	BRnp L174_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	CONST R3, #0
	STR R3, R7, #0
	JSR rand16
	LDR R7, R6, #-1	;; grab return value
	ADD R6, R6, #0	;; free space for arguments
	CONST R3, #15
	LDR R2, R5, #-1
	MUL R3, R3, R2
	LEA R2, meteors
	ADD R3, R3, R2
	STR R7, R3, #1
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	CONST R3, #0
	STR R3, R7, #2
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	LDR R3, R7, #1
	STR R3, R7, #3
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	LDR R3, R7, #2
	STR R3, R7, #4
	JSR rand16
	LDR R7, R6, #-1	;; grab return value
	ADD R6, R6, #0	;; free space for arguments
	CONST R3, #15
	LDR R2, R5, #-1
	MUL R3, R3, R2
	LEA R2, meteors
	ADD R3, R3, R2
	CONST R2, #3
	MUL R7, R2, R7
	CONST R2, #128
	DIV R7, R7, R2
	LEA R2, targets
	ADD R7, R7, R2
	LDR R7, R7, #0
	STR R7, R3, #5
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	CONST R3, #116
	STR R3, R7, #6
	LDR R7, R5, #-1
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR CalculateMeteorMovement
	ADD R6, R6, #1	;; free space for arguments
L174_mc
L171_mc
	LDR R7, R5, #-1
	ADD R7, R7, #1
	STR R7, R5, #-1
	LDR R7, R5, #-1
	CONST R3, #3
	CMP R7, R3
	BRn L170_mc
L169_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;ExecuteInput;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
ExecuteInput
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	ADD R6, R6, #-1	;; allocate stack space for local variables
	;; function body
	LDR R7, R5, #3
	CONST R3, #97
	CMP R7, R3
	BRnp L177_mc
	LEA R7, cursor
	LDR R3, R7, #0
	ADD R3, R3, #-1
	STR R3, R7, #0
	LDR R7, R7, #0
	CONST R3, #0
	CMP R7, R3
	BRzp L178_mc
	LEA R7, cursor
	CONST R3, #0
	STR R3, R7, #0
	JMP L178_mc
L177_mc
	LDR R7, R5, #3
	CONST R3, #100
	CMP R7, R3
	BRnp L181_mc
	LEA R7, cursor
	LDR R3, R7, #0
	ADD R3, R3, #1
	STR R3, R7, #0
	LDR R7, R7, #0
	CONST R3, #128
	CMP R7, R3
	BRnp L182_mc
	LEA R7, cursor
	CONST R3, #127
	STR R3, R7, #0
	JMP L182_mc
L181_mc
	LDR R7, R5, #3
	CONST R3, #119
	CMP R7, R3
	BRnp L185_mc
	LEA R7, cursor
	ADD R7, R7, #1
	LDR R3, R7, #0
	ADD R3, R3, #-1
	STR R3, R7, #0
	LEA R7, cursor
	LDR R7, R7, #1
	CONST R3, #0
	CMP R7, R3
	BRzp L186_mc
	LEA R7, cursor
	CONST R3, #0
	STR R3, R7, #1
	JMP L186_mc
L185_mc
	LDR R7, R5, #3
	CONST R3, #115
	CMP R7, R3
	BRnp L189_mc
	LEA R7, cursor
	ADD R7, R7, #1
	LDR R3, R7, #0
	ADD R3, R3, #1
	STR R3, R7, #0
	LEA R7, cursor
	LDR R7, R7, #1
	CONST R3, #124
	CMP R7, R3
	BRnp L190_mc
	LEA R7, cursor
	CONST R3, #123
	STR R3, R7, #1
	JMP L190_mc
L189_mc
	LDR R7, R5, #3
	CONST R3, #114
	CMP R7, R3
	BRnp L193_mc
	LEA R7, missileCommand
	STR R7, R5, #-1
	CONST R3, #0
	LDR R2, R7, #0
	CMP R2, R3
	BRnp L193_mc
	LDR R7, R5, #-1
	LDR R7, R7, #1
	CMP R7, R3
	BRnz L193_mc
	LEA R7, cooldown_counter
	LDR R7, R7, #0
	CONST R3, #5
	CMP R7, R3
	BRnz L195_mc
	JSR CreateNewMissile
	ADD R6, R6, #0	;; free space for arguments
L195_mc
L193_mc
L190_mc
L186_mc
L182_mc
L178_mc
	LEA R7, cooldown_counter
	LDR R3, R7, #0
	ADD R3, R3, #1
	STR R3, R7, #0
L176_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;rand16;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
rand16
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	ADD R6, R6, #-1	;; allocate stack space for local variables
	;; function body
	JSR lc4_lfsr
	LDR R7, R6, #-1	;; grab return value
	ADD R6, R6, #0	;; free space for arguments
	STR R7, R5, #-1
	JSR lc4_lfsr
	LDR R7, R6, #-1	;; grab return value
	ADD R6, R6, #0	;; free space for arguments
	STR R7, R5, #-1
	JSR lc4_lfsr
	LDR R7, R6, #-1	;; grab return value
	ADD R6, R6, #0	;; free space for arguments
	STR R7, R5, #-1
	JSR lc4_lfsr
	LDR R7, R6, #-1	;; grab return value
	ADD R6, R6, #0	;; free space for arguments
	STR R7, R5, #-1
	JSR lc4_lfsr
	LDR R7, R6, #-1	;; grab return value
	ADD R6, R6, #0	;; free space for arguments
	STR R7, R5, #-1
	JSR lc4_lfsr
	LDR R7, R6, #-1	;; grab return value
	ADD R6, R6, #0	;; free space for arguments
	STR R7, R5, #-1
	JSR lc4_lfsr
	LDR R7, R6, #-1	;; grab return value
	ADD R6, R6, #0	;; free space for arguments
	STR R7, R5, #-1
	LDR R7, R5, #-1
	CONST R3, #127
	AND R7, R7, R3
L197_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;PrepareForNextWave;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
PrepareForNextWave
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	ADD R6, R6, #-1	;; allocate stack space for local variables
	;; function body
	LEA R7, meteors_left_per_wave
	LDR R7, R7, #0
	CONST R3, #0
	CMP R7, R3
	BRp L199_mc
	CONST R7, #0
	STR R7, R5, #-1
	CONST R7, #0
	STR R7, R5, #-1
L201_mc
	CONST R7, #15
	LDR R3, R5, #-1
	MUL R7, R7, R3
	LEA R3, meteors
	ADD R7, R7, R3
	CONST R3, #1
	STR R3, R7, #0
L202_mc
	LDR R7, R5, #-1
	ADD R7, R7, #1
	STR R7, R5, #-1
	LDR R7, R5, #-1
	CONST R3, #3
	CMP R7, R3
	BRn L201_mc
	LEA R7, meteors_per_wave
	LDR R3, R7, #0
	ADD R3, R3, #5
	STR R3, R7, #0
	LEA R3, meteors_left_per_wave
	LDR R7, R7, #0
	STR R7, R3, #0
	LEA R7, wave_counter
	LDR R3, R7, #0
	ADD R3, R3, #1
	STR R3, R7, #0
	LEA R7, L205_mc
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR lc4_puts
	ADD R6, R6, #1	;; free space for arguments
	LEA R7, wave_counter
	LDR R7, R7, #0
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR printnum
	ADD R6, R6, #1	;; free space for arguments
	LEA R7, L206_mc
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR lc4_puts
	ADD R6, R6, #1	;; free space for arguments
	LEA R7, missileCommand
	CONST R3, #8
	STR R3, R7, #1
	LEA R7, missileCommand
	CONST R3, #0
	STR R3, R7, #0
L199_mc
L198_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

;;;;;;;;;;;;;;;;;;;;;;;;;;;;main;;;;;;;;;;;;;;;;;;;;;;;;;;;;
		.CODE
		.FALIGN
main
	;; prologue
	STR R7, R6, #-2	;; save return address
	STR R5, R6, #-3	;; save base pointer
	ADD R6, R6, #-3
	ADD R5, R6, #0
	ADD R6, R6, #-1	;; allocate stack space for local variables
	;; function body
	LEA R7, L208_mc
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR lc4_puts
	ADD R6, R6, #1	;; free space for arguments
	JSR ResetGame
	ADD R6, R6, #0	;; free space for arguments
	JMP L210_mc
L209_mc
	LEA R7, L212_mc
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR lc4_puts
	ADD R6, R6, #1	;; free space for arguments
	LEA R7, L213_mc
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR lc4_puts
	ADD R6, R6, #1	;; free space for arguments
	JMP L215_mc
L214_mc
	LEA R7, cities
	STR R7, R5, #-1
	CONST R3, #0
	LDR R2, R7, #0
	CMP R2, R3
	BRz L217_mc
	LDR R7, R5, #-1
	LDR R7, R7, #10
	CMP R7, R3
	BRz L217_mc
	JMP L216_mc
L217_mc
	JSR Redraw
	ADD R6, R6, #0	;; free space for arguments
	CONST R7, #10
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR lc4_getc_timer
	LDR R7, R6, #-1	;; grab return value
	ADD R6, R6, #1	;; free space for arguments
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR ExecuteInput
	ADD R6, R6, #1	;; free space for arguments
	JSR CreateNewMeteors
	ADD R6, R6, #0	;; free space for arguments
	JSR UpdateProjectiles
	ADD R6, R6, #0	;; free space for arguments
	JSR PrepareForNextWave
	ADD R6, R6, #0	;; free space for arguments
L215_mc
	JMP L214_mc
L216_mc
	LEA R7, L219_mc
	ADD R6, R6, #-1
	STR R7, R6, #0
	JSR lc4_puts
	ADD R6, R6, #1	;; free space for arguments
	JSR ResetGame
	ADD R6, R6, #0	;; free space for arguments
L210_mc
	JMP L209_mc
	CONST R7, #0
L207_mc
	;; epilogue
	ADD R6, R5, #0	;; pop locals off stack
	ADD R6, R6, #3	;; free space for return address, base pointer, and return value
	STR R7, R6, #-1	;; store return value
	LDR R5, R6, #-3	;; restore base pointer
	LDR R7, R6, #-2	;; restore return address
	RET

		.DATA
cursor 		.BLKW 2
		.DATA
meteors 		.BLKW 45
		.DATA
missiles 		.BLKW 45
		.DATA
missileCommand 		.BLKW 11
		.DATA
cities 		.BLKW 20
		.DATA
targets 		.BLKW 3
		.DATA
meteors_per_wave 		.BLKW 1
		.DATA
meteors_left_per_wave 		.BLKW 1
		.DATA
L219_mc 		.STRINGZ "---GAME OVER---\n"
		.DATA
L213_mc 		.STRINGZ "Press w, a, s and d to move the cursor\n"
		.DATA
L212_mc 		.STRINGZ "Press r to shoot a missile\n"
		.DATA
L208_mc 		.STRINGZ "Welcome to the Missile Command!\n"
		.DATA
L206_mc 		.STRINGZ "\n"
		.DATA
L205_mc 		.STRINGZ "Current wave: "
		.DATA
L21_mc 		.STRINGZ "\n"
		.DATA
L14_mc 		.STRINGZ "-32768"
		.DATA
L8_mc 		.STRINGZ "0"
