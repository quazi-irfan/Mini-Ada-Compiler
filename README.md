# Mini-Ada to x86 Compiler
This is a mini-Ada to x86 compiler. Our compiled program will run on 16 bit architecture(i.e. Intel 8086/8088).
Our version of Ada supports only integer and string(limited) data type, and allows assignment and IO statement. 
It supports expressions, with addition, multiplication and unary negation operator, in an assignment statement,
If all operators have same precedence in an expression, it uses right to left associativity.
In an IO statement, it supports integer input/output, and string output.

## Backend
We have to run the following command to compile our compiler,
```bash
javac adac.java
```
Now our compiler is ready to compile ada source file.

Example Hello.ada file contains,
```Ada
-- Ada Hello World Program
procedure main is
begin
  putln("Hello World");
end main;
```

To compile Hello.ada we have to run the following command,
```bash
$ java adac Hello.ada
Parsing successful. Output at Hello.tac
TAC to x86 translation sucessful. Output at Hello.asm
```

Our compiler would would create Hello.tac and Hello.asm file. Hello.TAC
([Three Address Code](https://en.wikipedia.org/wiki/Three-address_code)) file contains our intermediate representation, 
and Hello.ASM contains the x86 instructions. 

Contant of the Hello.TAC file,
```
PROC    _MAIN   
wrs     _s0     
wrln    
ENDP    _MAIN   
START   PROC    _MAIN   
```
Contant of the Hello.ASM file,
```asm
	.model small
	.586
	.stack 100h
	.data
_s0    db      "Hello World","$"
	.code
	include io.asm

	  ;PROC    _MAIN   
_MAIN	proc
	  push bp
	  mov bp, sp
	  sub sp, 0

	  ;wrs     _s0     
	  mov dx, offset _s0
	  call writestr

	  ;wrln    
	  call writeln

	  ;ENDP    _MAIN   
	  add sp, 0
	  pop bp
	  ret 0
_MAIN	ENDP

	  ;START   PROC    _MAIN   
main	PROC
	  mov ax, @data
	  mov ds, ax
	  call _MAIN
	  mov ah, 4ch
	  int 21h
main	ENDP
	  END main

```
(Note: that all user defined identifiers has an underscore prefix added to them in both TAC and ASM file.)

## Frontend
Now we will use Microsoft Macro Assembler [MASM32](http://www.masm32.com) to translate our x86 instructions to object code. The files `ml.exe` and `link16.exe` are found under `<masm32>/bin`

Since [IO.asm](https://github.com/iamcreasy/Mini-Ada-Compiler/blob/master/io.asm) contains the IO macros we will need copy IO.asm in the same direcoty as Hello.ASM

The following command to translate our x86 instruction to machine code will create `Hello.obj`
```
ml /c Hello.asm
```

The following intruction to statically link the content of IO.ASM with Hello.OBJ will create final 16bit executable file `Hello.exe`
```
link16 Hello.obj;
```

## Running the executable
Since Hello.exe is 16 bit application, we will use [DosBox](https://www.dosbox.com) to emulate a sandboxed 16 bit environment.

Copy the file to Dosbox directory, and run the file. We will see the following output,
```
C:\>hello.exe
Hello World
```

# More Ada Examples

## Example 1 : Prints the result of an expression
TwoNum.ada contains,
```Ada
-- Print the result of adding 1 and 2
procedure TwoNum is
 a,b,c:integer;
begin
  a:= 1;
  b:= 2;
  c:= a + b;
  putln("Summation of 1 and 2 is ", c);  
end TwoNum;
```

After compilation, TwoNum.tac contains
```
PROC    _TWONUM 
_t0     =       1       
_A      =       _t0     
_t1     =       2       
_B      =       _t1     
_t2     =       _A      +       _B      
_C      =       _t2     
wrs     _s0     
wri     _C      
wrln    
ENDP    _TWONUM 
START   PROC    _TWONUM 
```

TwoNum.asm
```asm
		.model small
		.586
		.stack 100h
		.data
_s0     db      "Summation of 1 and 2 is ","$"
_t0     dw      ?       
_t1     dw      ?       
_t2     dw      ?       
_A      dw      ?       
_B      dw      ?       
_C      dw      ?       
		.code
		include io.asm

		;PROC    _TWONUM 
_TWONUM		proc
		push bp
		mov bp, sp
		sub sp, 12

		;_t0     =       1       
		mov ax, 1
		mov _t0 , ax

		;_A      =       _t0     
		mov ax, _t0
		mov _A , ax

		;_t1     =       2       
		mov ax, 2
		mov _t1 , ax

		;_B      =       _t1     
		mov ax, _t1
		mov _B , ax

		;_t2     =       _A      +       _B      
		mov ax, _A
		add ax, _B
		mov _t2 , ax

		;_C      =       _t2     
		mov ax, _t2
		mov _C , ax

		;wrs     _s0     
		mov dx, offset _s0
		call writestr

		;wri     _C      
		mov ax, _C
		call writeint

		;wrln    
		call writeln

		;ENDP    _TWONUM 
		add sp, 12
		pop bp
		ret 0
_TWONUM		ENDP

		;START   PROC    _TWONUM 
main		PROC
		mov ax, @data
		mov ds, ax
		call _TWONUM
		mov ah, 4ch
		int 21h
main		ENDP
		END main
```

Doxbox Output,
```
C:/>TwoNum
Summation of 1 and 2 is 3
```
## Example 2 : Take two numbers input from the user and perform some calculation on them

Three.ada contains,
```Ada
-- procedure three take two number input from the user
-- procedure test perform some calculation on them, and passes back the result
procedure three is
 a, b, c:integer;
 procedure test(in a,b:integer; out c:integer) is
 	e : integer;
  begin
	e := a * 10 + b;
	c := e; 
 end test;
begin
  put("Enter the first number ");
  get(a);
  put("Enter the second number ");
  get(b);
  test(a, b, c);
  put("The result of " , a ," * 10 + ", b, " is : ", c );
end three;
```

After compilation, three.tac contains
```
PROC    _TEST   
_bp-6   =       10      
_bp-4   =       _bp+8   *       _bp-6   
_bp-8   =       _bp-4   +       _bp+6   
_bp-2   =       _bp-8   
@_bp+4  =       _bp-2   
ENDP    _TEST   
PROC    _THREE  
wrs     _s0     
rdi     _A      
wrs     _s1     
rdi     _B      
push    _A      
push    _B      
push    @_C     
call    _TEST   
wrs     _s2     
wri     _A      
wrs     _s3     
wri     _B      
wrs     _s4     
wri     _C      
ENDP    _THREE  
START   PROC    _THREE  
```

and three.asm contains,

```asm
		.model small
		.586
		.stack 100h
		.data
_s0     db      "Enter the first number ","$"
_s1     db      "Enter the second number ","$"
_s2     db      "The result of ","$"
_s3     db      " * 10 + ","$"
_s4     db      " is : ","$"
_A      dw      ?       
_B      dw      ?       
_C      dw      ?       
		.code
		include io.asm

		;PROC    _TEST   
_TEST		proc
		push bp
		mov bp, sp
		sub sp, 8

		;_bp-6   =       10      
		mov ax, 10
		mov [bp-6] , ax

		;_bp-4   =       _bp+8   *       _bp-6   
		mov ax, [bp+8]
		mov bx, [bp-6]
		imul bx
		mov [bp-4], ax

		;_bp-8   =       _bp-4   +       _bp+6   
		mov ax, [bp-4]
		add ax, [bp+6]
		mov [bp-8] , ax

		;_bp-2   =       _bp-8   
		mov ax, [bp-8]
		mov [bp-2] , ax

		;@_bp+4  =       _bp-2   
		mov ax, [bp-2]
		mov bx, [bp+4]
		mov [bx], ax

		;ENDP    _TEST   
		add sp, 8
		pop bp
		ret 6
_TEST		ENDP

		;PROC    _THREE  
_THREE		proc
		push bp
		mov bp, sp
		sub sp, 6

		;wrs     _s0     
		mov dx, offset _s0
		call writestr

		;rdi     _A      
		call readint
		mov ax, bx
		mov _A , ax

		;wrs     _s1     
		mov dx, offset _s1
		call writestr

		;rdi     _B      
		call readint
		mov ax, bx
		mov _B , ax

		;push    _A      
		mov ax, _A
		push ax

		;push    _B      
		mov ax, _B
		push ax

		;push    @_C     
		mov ax, offset _C
		push ax

		;call    _TEST   
		call _TEST

		;wrs     _s2     
		mov dx, offset _s2
		call writestr

		;wri     _A      
		mov ax, _A
		call writeint

		;wrs     _s3     
		mov dx, offset _s3
		call writestr

		;wri     _B      
		mov ax, _B
		call writeint

		;wrs     _s4     
		mov dx, offset _s4
		call writestr

		;wri     _C      
		mov ax, _C
		call writeint

		;ENDP    _THREE  
		add sp, 6
		pop bp
		ret 0
_THREE		ENDP

		;START   PROC    _THREE  
main		PROC
		mov ax, @data
		mov ds, ax
		call _THREE
		mov ah, 4ch
		int 21h
main		ENDP
		END main
```

Output,
```
C:/>three
Enter the first number 5

Enter the second number 3

The result of 5 * 10 + 3 is : 53
```
