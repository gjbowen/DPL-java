
# define a makefile variable for the java & compiler
JCC = javac
JR  = java

default: Main.class Evaluator.class Environment.class Parser.class Lexer.class Lexeme.class Scanner.class

# Compile java files
Main.class: ./Main.java
	$(JCC) ./Main.java
Lexer.class: ./Lexer.java
	$(JCC) ./Lexer.java
Lexeme.class: ./Lexeme.java
	$(JCC) ./Lexeme.java
Parser.class: ./Parser.java
	$(JCC) ./Parser.java
Environment.class: ./Environment.java
	$(JCC) ./Environment.java
Evaluator.class: ./Evaluator.java
	$(JCC) ./Evaluator.java
Scanner.class: ./Scanner.java
	$(JCC) ./Scanner.java
# run all
run : error1 error2 error3 error4 error5 arrays conditionals recursion iteration functions lambda objects
runx : error1x error2x error3x error4x error5x arraysx conditionalsx recursionx iterationx functionsx lambdax objectsx

# Error 1
error1 : 
	@cat error1.mylang
	@echo
error1x : ./Main.class 
	$(JR) -cp ./ Main error1.mylang
# Error 2
error2 : 
	@cat error2.mylang
	@echo
error2x : ./Main.class 
	$(JR) -cp ./ Main error2.mylang
# Error 3
error3 : 
	@cat error3.mylang
	@echo
error3x : ./Main.class 
	$(JR) -cp ./ Main error3.mylang
# Error4
error4 : 
	@cat error4.mylang
	@echo
error4x : ./Main.class 
	$(JR) -cp ./ Main error4.mylang
# Error 5
error5 : 
	@cat error5.mylang
	@echo
error5x : ./Main.class 
	$(JR) -cp ./ Main error5.mylang
# Arrays
arrays : 
	@cat arrays.mylang
	@echo
arraysx : ./Main.class 
	$(JR) -cp ./ Main arrays.mylang
# Conditionals
conditionals : 
	@cat conditionals.mylang
	@echo
conditionalsx : ./Main.class 
	$(JR) -cp ./ Main conditionals.mylang
# Recursion
recursion : 
	@cat recursion.mylang
	@echo
recursionx : ./Main.class 
	$(JR) -cp ./ Main recursion.mylang
# Interation
iteration : 
	@cat iteration.mylang
	@echo
iterationx : ./Main.class 
	$(JR) -cp ./ Main iteration.mylang
# Functions
functions : 
	@cat functions.mylang
	@echo
functionsx : ./Main.class 
	$(JR) -cp ./ Main functions.mylang
# Lambda
lambda : 
	@cat lambda.mylang
	@echo
lambdax : ./Main.class 
	$(JR) -cp ./ Main lambda.mylang
# Objects
objects : 
	@cat objects.mylang
	@echo
objectsx : ./Main.class 
	$(JR) -cp ./ Main objects.mylang

# Problem
problem : 
	@cat problem.mylang
	@echo
problemx : ./Main.class 
	$(JR) -cp ./ Main problem.mylang


# Removes all .class files, so that the next make rebuilds them
#
clean: 
	$(RM) *.class
