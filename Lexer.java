//////////////////
//	Greg Bowen	//
//	Lexer.java	//
//////////////////
import java.io.FileInputStream;
import java.io.IOException;
class Lexer {
	String filename;
	boolean doneReading;
	FileInputStream fis;
	char ch;
	int lineNumber=0;
	char pushBackCh;
	boolean chHasBeenPushed;

	Lexer(String f){
		filename = f;
		try {
			fis = new FileInputStream(filename);
			doneReading=false;
			lineNumber=1;
			chHasBeenPushed=false;
		} catch (IOException e) {
			if(e.getClass().getName().equals("java.io.FileNotFoundException"))
				fatal("File.Not.Found - "+filename);
			e.printStackTrace();
			fatal(e.getMessage());
		}	
	}
	void fatal(String message) {//custom Fatal function
		System.err.printf("\nFatal error in Lexer: "+message+"\n");
		System.err.printf("\tLine: "+lineNumber+"\n");
		System.exit(0);
	}
	void readChar() {
		try {
			if(!doneReading) { //prevent additional file reads
				if (chHasBeenPushed){
					chHasBeenPushed = false;
					ch = pushBackCh;
				}
				else if (fis.available() > 0) {
					ch = (char) fis.read();
					if(ch=='\t'||ch=='\r') //treat CR and tabs as a space
						ch = ' ';
				}
				else { //done reading
					doneReading=true;
					closeFile();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			fatal(e.getMessage());
		}
	}
	void closeFile() {
		try {
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
			fatal(e.getMessage());
		}
	}
	void pushBack() {
		if(chHasBeenPushed) 
			fatal("Too.Many.Pushbacks");
		pushBackCh=ch;
		chHasBeenPushed=true;
	}
	void skipWhiteSpace() {
		while((ch==' '||ch=='\n')&&!doneReading) {
			if(ch=='\n')
				++lineNumber;
			readChar();
		}

		if(ch=='#') {//go to end of line if comment
			while(ch!='\n')
				readChar();
			skipWhiteSpace(); //read the first character of the next lineNumber
		}
	}
	Lexeme lexVariableOrKeyword(){
		String token="";
		while (isLetter(ch) || isDigit(ch)){
			token = token + ch; //grow the token string
			readChar();
		}
		pushBack();
		//token holds either a variable or a keyword, so figure it out
		if (token.equals("break")) return new Lexeme("BREAK",lineNumber);
		else if (token.equals("case")) return new Lexeme("CASE",lineNumber);
		else if (token.equals("null")) return new Lexeme("NULL",lineNumber);
		else if (token.equals("default")) return new Lexeme("DEFAULT",lineNumber);
		else if (token.equals("this")) return new Lexeme("THIS",lineNumber);
		else if (token.equals("else")) return new Lexeme("ELSE",lineNumber);
		else if (token.equals("false")) return new Lexeme("FALSE",lineNumber,false);
		else if (token.equals("function")) return new Lexeme("FUNCTION",lineNumber);
		else if (token.equals("if")) return new Lexeme("IF",lineNumber);
		else if (token.equals("lambda")) return new Lexeme("LAMBDA",lineNumber);
		else if (token.equals("read")) return new Lexeme("READ",lineNumber);
		else if (token.equals("close")) return new Lexeme("CLOSE",lineNumber);
		else if (token.equals("open")) return new Lexeme("OPEN",lineNumber);
		else if (token.toLowerCase().equals("eof")) return new Lexeme("EOF",lineNumber,"EOF");
		else if (token.equals("next")) return new Lexeme("NEXT",lineNumber);
		else if (token.equals("print")) return new Lexeme("PRINT",lineNumber);
		else if (token.equals("println")) return new Lexeme("PRINTLN",lineNumber);
		else if (token.equals("return")) return new Lexeme("RETURN",lineNumber);
		else if (token.equals("switch")) return new Lexeme("SWITCH",lineNumber);
		else if (token.equals("true")) return new Lexeme("TRUE",lineNumber,true);
		else if (token.equals("var")) return new Lexeme("VAR_DEF",lineNumber);
		else if (token.equals("while")) return new Lexeme("WHILE",lineNumber);
		else { //must be a variable!
			return new Lexeme("ID",lineNumber,token);
		}
	}
	Lexeme lex() {
		readChar();
		skipWhiteSpace();
		if(doneReading) return new Lexeme("STRING",lineNumber,"EOF");
		switch(ch) {
		case '{': return new Lexeme("OBRACE",lineNumber);
		case '}': return new Lexeme("CBRACE",lineNumber);
		case '(': return new Lexeme("OPAREN",lineNumber);
		case ')': return new Lexeme("CPAREN",lineNumber);
		case '[': return new Lexeme("OBRACK",lineNumber);
		case ']': return new Lexeme("CBRACK",lineNumber);
		case ';': return new Lexeme("SEMI",lineNumber);
		case ',': return new Lexeme("COMMA",lineNumber);
		case '.': return new Lexeme("DOT",lineNumber);
		case ':': return new Lexeme("COLON",lineNumber);
		case '*': return new Lexeme("TIMES",lineNumber);
		case '/': return new Lexeme("DIVIDE",lineNumber);
		case '^': return new Lexeme("POWER",lineNumber);
		//check for multi valid outcomes in remaining cases 
		case '=': return equalChar();
		case '<': return lessThanChar();
		case '>': return greaterThanChar();
		case '+': return plusChar();
		case '-': return minusChar();
		default:	
			// multi-character tokens (only numbers, 
			// variables/keywords, and strings) 
			if (isDigit(ch))
				return lexNumber();  
			else if (isLetter(ch))				
				return lexVariableOrKeyword();
			else if (ch == '\"')
				return lexString(); 
			// remaining else ifs must have ONE valid lexeme
			else if (ch=='!') {	 		
				readChar();
				if(ch=='=') return new Lexeme("NOTEQUAL",lineNumber);
			}
			else if (ch=='|') {			
				readChar();
				if(ch=='|') return new Lexeme("OR",lineNumber);
			}
			else if (ch=='&') {			
				readChar();
				if(ch=='&') return new Lexeme("AND",lineNumber);
			}
			System.err.printf("\nFatal error in Evaluator: Bad.Lexeme\n");
			System.err.printf("\t"+ch+" - Line: "+lineNumber+"\n");
			System.exit(1);
		}
		return null;
	}
	Lexeme plusChar(){
		readChar();
		if(ch=='=')
			return new Lexeme("INCREMENT",lineNumber);
		else if(ch=='+')
			return new Lexeme("INCREMENTONE",lineNumber);
		pushBack(); //just a  +
		return new Lexeme("PLUS",lineNumber);
	}
	Lexeme equalChar(){
		readChar();
		if(ch=='=')
			return new Lexeme("EQUAL",lineNumber);
		pushBack(); //just a  =
		return new Lexeme("ASSIGN",lineNumber);
	}	
	Lexeme notEqualChar(){

		pushBack(); //just a  =
		fatal("Bad.Lexeme - "+Character.toString(ch)); 
		return new Lexeme("UNKNOWN",lineNumber,Character.toString(ch));
	}
	Lexeme lessThanChar(){
		readChar();
		if(ch=='=')
			return new Lexeme("LESSTHANEQUAL",lineNumber);
		pushBack(); //just a <
		return new Lexeme("LESSTHAN",lineNumber);
	}
	Lexeme greaterThanChar(){
		readChar();
		if(ch=='=')
			return new Lexeme("GREATERTHANEQUAL",lineNumber);
		pushBack(); //just a  >
		return new Lexeme("GREATERTHAN",lineNumber);
	}
	Lexeme minusChar(){
		readChar();
		if(ch=='=')
			return new Lexeme("DECREMENT",lineNumber);
		else if(ch=='-')
			return new Lexeme("DECREMENTONE",lineNumber);
		pushBack(); //just a  -
		return new Lexeme("MINUS",lineNumber);
	}
	Lexeme lexString () {
		String token="";
		readChar();
		while(ch!='"') {
			token+=ch;
			readChar();
		} 
		return new Lexeme("STRING",lineNumber,token);
	}
	Lexeme lexNumber () {
		String token=""+ch;
		readChar();
		boolean realNumber=false;
		while(!doneReading&&(Character.isDigit(ch)||ch=='.')) {
			if(ch=='.')
				realNumber=true;

			token+=ch;
			readChar();
		}
		pushBack();
		if(realNumber)
			return new Lexeme("REAL",lineNumber,Float.parseFloat(token));
		else
			return new Lexeme("INTEGER",lineNumber,Integer.parseInt(token));
	}
	boolean isDigit(char c) {	
		return Character.isDigit(c);
	}
	boolean isLetter(char c) {	
		return Character.isLetter(c);
	}
}