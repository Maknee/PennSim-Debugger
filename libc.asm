USER_STACK_ADDR .UCONST x7FFF
USER_STACK_SIZE .UCONST x1000
USER_HEAP_SIZE .UCONST x3000	
;;; Reserve space for heap and stack so that assembler will not try to
;;; place data in these regions
	
.DATA
.ADDR x4000
USER_HEAP	.BLKW x3000
.ADDR x7000			
USER_STACK	.BLKW x1000

.CODE
.ADDR x0000	
	.FALIGN
__start
	LC R6, USER_STACK_ADDR	; Init the Stack Pointer
	LEA R7, main		; Invoke the main routine
	JSRR R7
	TRAP x25		; HALT

;;; Wrappers for the traps.  Marshall the arguments from stack to 
;;; registers and return value from register to stack

	.FALIGN
lc4_puts
	;; prologue
	ADD R6, R6, #-2
	STR R5, R6, #0
	STR R7, R6, #1
	
	LEA R7, STACK_SAVER
	STR R6, R7, #0

	
	;; marshall arguments
	LDR R0, R6, #2

	TRAP x02

	;; epilogue
	LEA R7, STACK_SAVER
	LDR R6, R7, #0

	LDR R7, R6, #1
	LDR R5, R6, #0
	ADD R6, R6, #2
	RET

	.FALIGN
lc4_getc_timer
	ADD R6, R6, #-2	
	STR R5, R6, #0
	STR R7, R6, #1

	LDR R0, R6, #2

	LEA R7, STACK_SAVER
	STR R6, R7, #0

	TRAP x08
	
	LEA R7, STACK_SAVER
	LDR R6, R7, #0	

	LDR R7, R6, #1
	;; save TRAP return value on stack
	STR R0, R6, #1
	;; restore user base-pointer
	LDR R5, R6, #0
	ADD R6, R6, #2
	RET


	.FALIGN
lc4_draw_rect
	;; prologue
	ADD R6, R6, #-2
	STR R5, R6, #0
	STR R7, R6, #1
	;; marshall arguments
	LDR R0, R6, #2		; x
	LDR R1, R6, #3		; y
	LDR R2, R6, #4		; width
	LDR R3, R6, #5		; height
	LDR R4, R6, #6		; color

	LEA R7, STACK_SAVER
	STR R6, R7, #0
	
	TRAP x05
	
	LEA R7, STACK_SAVER
	LDR R6, R7, #0
	
	;; epilogue
	LDR R7, R6, #1
	LDR R5, R6, #0
	ADD R6, R6, #2
	RET

	.FALIGN
lc4_draw_sprite
	;; prologue
	ADD R6, R6, #-2
	STR R5, R6, #0
	STR R7, R6, #1
	;; marshall arguments
	LDR R0, R6, #2		; x
	LDR R1, R6, #3		; y
	LDR R2, R6, #4		; color
	LDR R3, R6, #5		; sprite
	
	LEA R7, STACK_SAVER
	STR R6, R7, #0
	
	TRAP x06
	
	LEA R7, STACK_SAVER
	LDR R6, R7, #0
	
	;; epilogue
	LDR R7, R6, #1
	LDR R5, R6, #0
	ADD R6, R6, #2
	RET
	

	.FALIGN
lc4_draw_line
	;; prologue
	ADD R6, R6, #-2
	STR R5, R6, #0
	STR R7, R6, #1
	;; marshall arguments
	LDR R0, R6, #2		; x0
	LDR R1, R6, #3		; y0
	LDR R2, R6, #4		; x1
	LDR R3, R6, #5		; x2
	LDR R4, R6, #6		; color
	
	LEA R7, STACK_SAVER
	STR R6, R7, #0
	
	TRAP x07
	
	LEA R7, STACK_SAVER
	LDR R6, R7, #0
	
	;; epilogue
	LDR R7, R6, #1
	LDR R5, R6, #0
	ADD R6, R6, #2
	RET

	.FALIGN	
lc4_halt
	;; prologue
	ADD R6, R6, #-2
	LDR R5, R6, #0
	STR R7, R6, #1
	;; no arguments
	TRAP x25
	;; epilogue
	LDR R7, R6, #1
	LDR R5, R6, #0
	ADD R6, R6, #2
	RET			

	.FALIGN
lc4_reset_vmem
	;; prologue
	ADD R6, R6, #-2
	STR R5, R6, #0
	STR R7, R6, #1
	;; no arguments
	TRAP x26
	;; epilogue
	LDR R5, R6, #0
	LDR R7, R6, #1
	ADD R6, R6, #2
	RET

	.FALIGN
lc4_blt_vmem
	;; prologue
	ADD R6, R6, #-2
	STR R5, R6, #0
	STR R7, R6, #1
	;; no arguments
	TRAP x27
	;; epilogue
	LDR R5, R6, #0
	LDR R7, R6, #1
	ADD R6, R6, #2
	RET

		
	.FALIGN
lc4_lfsr 
	;; R5 is the base pointer as well as the 
	;; TRAP return register.  If the trap returns
	;; a value, we have to save and restore the user's
	;; base-pointer
	ADD R6, R6, #-2	
	STR R5, R6, #0
	STR R7, R6, #1

	LEA R7, STACK_SAVER
	STR R6, R7, #0
	
	TRAP x0A
	
	LEA R7, STACK_SAVER
	LDR R6, R7, #0
	
	LDR R7, R6, #1
	;; save LFSR return value on stack
	STR R0, R6, #1
	;; restore user base-pointer
	LDR R5, R6, #0
	ADD R6, R6, #2
	RET
	
	
;;; Other library data will start at x2000
;;; This needs to be here so that subsequent files will have their user data
;;;  placed appropriately
.DATA
.ADDR x2000

;;;  We use this storage location to cache the Stack Pointer so that
;;;  we can restore the stack appropriately after a TRAP. It is only
;;;  needed for traps that overwrite R6
STACK_SAVER .FILL 0x0000
