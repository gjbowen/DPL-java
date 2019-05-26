//////////////////////
//	Greg Bowen		//
//	Scanner.java	//
//////////////////////
class Scanner {
	Lexeme token; 
	Lexer lexer;
	String fileName;
	Scanner(String file){
		fileName = file;
		lexer = new Lexer(fileName);
	}

	Lexeme getNext(){
		token=lexer.lex();
		//System.out.println("scanner says: "+token.type);
		return token; 
	}
	void close() {
		lexer.closeFile();
	}
	boolean eof(){
		if(token.type.equals("EOF"))
			return true;
		return false;
	}
}