; Standard IO routines

; PROCEDURE WRITECH writes a single character to the monitor
;     without a new-line. Character must be in dl register
;
writech    PROC      

           mov ah,02h    ; DOS write function
           int 21h
           ret
writech    ENDP 

;PROCEDURE WRITEINT calls writech to write a signed integer value
;    to standard output.  Integer must be in ax register
;    USES ax,bx,cx,dx registers
;
writeint   PROC       
           mov cx,1
           mov dx,0
loop_div:  mov bx,10
           div bx
           push dx
           mov dx,0
           cmp ax,0
           je loop_wr
           inc cx
           jmp loop_div
loop_wr:   pop dx
           add dl,'0'
           call writech
           dec cx
           cmp cx,0
           jne loop_wr
           ret
writeint   ENDP 

;PROCEDURE WRITESTR writes an ASCII$ string to standard output
;  requires the offset of the character string be in DX
;;
;;  usage:  mov DX, OFFSET desired_string
;;          call writestr
;;
writestr   PROC       ;writestr

           push  ax      ;save contents of ax register
           mov   ah, 09  ;DOS string write
           int   21h
           pop   ax
           ret
writestr   ENDP  ;writestr

;PROCEDURE WRITELN writes a newline to standard output
;
writeln    PROC      ; writeln
           push ax
           push dx
           mov ah,02h
           mov dl,13
           int 21h
           mov dl,10
           int 21h
           pop dx
	   pop ax
           ret
writeln    ENDP ;writeln

; PROCEDURE READCH  reads a single character from the keyboard
;     and returns it in the al register
;
readch     PROC       ;readch
           mov ah,01h    ; DOS read function
           int 21h
           PUSH AX
           MOV DL,' '
           CALL WRITELN
           POP AX
           RET
readch     ENDP ;readch

; PROCEDURE READINT reads an integer from the keyboard
;     valid range is -32768 to 32767 does no error checking
;     returns integer in bx register
readint    PROC       ;readint

           mov bx,0
           mov cx,10     ; base 10 multiplier

loop_read: mov ah,01h    ; DOS read char function
           int 21h

           cmp  al,' '
           je   end_read
           cmp  al,13
           je   end_read
           push ax
           mov  ax,bx
           imul cx
           mov  bx,ax
           pop  ax
           sub  al,'0'
           mov  ah,0
           add  bx,ax
           jmp loop_read
end_read:  call writeln
           ret
readint    ENDP ;readint

