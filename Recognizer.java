///////////////////////
//	Greg Bowen       //
//	Recognizer.java	 //
///////////////////////
public class Recognizer {
	String fileName;
	Lexeme current;
	Lexer lexer;
	Recognizer(String file){
		fileName=file;
		lexer = new Lexer(fileName);
	}
	void program(){
		//if(fdefPending())
		funcDef();
		if (programPending())
			program();
		else if(check("ID"))
			program();
	}
	boolean programPending(){
		return funcDefPending();
	}
	//////////////////////////////////////////////////////////////
	boolean funcDefPending(){
		return check("FUNCTION");
	}
	void funcDef(){
		match("FUNCTION");
		match("ID");
		match("OPAREN");
		optParamList(); 
		match("CPAREN");
		block();
	}
	//////////////////////////////////////////////////////////////
	boolean optParamListPending(){
		return paramListPending();
	}
	void optParamList(){
		if(optParamListPending()) {
			paramList();
		}
	}
	//////////////////////////////////////////////////////////////
	boolean paramListPending(){
		return check("ID");
	}
	void paramList(){
		if(paramListPending()) {
			match("ID");
			if(check("COMMA"))
				match("COMMA");//more!
		}
		if(paramListPending())
			paramList();	
	}
	//////////////////////////////////////////////////////////////
	void block() {
		match("OBRACE");
		optStatementList();
		match("CBRACE");
	}
	//////////////////////////////////////////////////////////////
	void optStatementList() {
		if(lambdaStatementPending()) {
			lambdaStatement();
			optStatementList();
		}
		else if(funcDefPending()) {
			funcDef();
			optStatementList();
		}
		else if(printStatementPending()) {
			printStatement();
			optStatementList();
		}
		else if(whileLoopPending()) {
			whileLoop();
			optStatementList();
		}
		else if(ifStatementPending()) {
			ifStatement();
			optStatementList();
		}
		else if(returnStatementPending()) {
			returnStatement();
			optStatementList();

		}
		else if(switchStatementPending()) {
			switchStatement();
			optStatementList();
		}
		else if(check("ID")){ //assign variable
			varExpr(); 
			optStatementList();
		} 
		else if(incrementOnePending()){
			incrementOne();
			optStatementList();
		} 
		else if(decrementOnePending()){
			decrementOne();
			optStatementList();
		} 
		else if(check("VAR")){
			match("VAR");
			varExpr(); 
			optStatementList();
		} 
	}
	void expr() {
		unary();
		if(operatorPending()){
			operator();
			expr();
		}
		else if(equalityPending()){
			equality();
			expr();
		}
		else if(check("OPAREN")){
			match("OPAREN"); 
			expr(); 
			match("CPAREN"); 
		} 		
		else if(check("COMMA")){//for function calls
			match("COMMA"); 
			expr(); 
		}
	}
	void unary() { 
		if(check("INTEGER"))
			match("INTEGER");
		else if(check("REAL"))
			match("REAL");
		else if(check("TRUE"))
			match("TRUE");
		else if(check("FALSE")) 
			match("FALSE");
		else if(check("STRING"))
			match("STRING");
		else if(check("ID"))
			match("ID"); 
		else if(check("OPAREN")){
			match("OPAREN"); 
			expr(); 
			match("CPAREN"); 
		}
	}
	//////////////////////////////////////////////////////////////
	boolean varExprPending(){ 
		return check("ID"); 
	}
	void varExpr(){ 
		match("ID"); 
		if (check("OPAREN")){ //function call assignment
			match("OPAREN"); 
			paramList();
			match("CPAREN"); 
		}
		else if(check("ASSIGN")) {//
			match("ASSIGN");
			if(arrayExprPending()) {//array
				match("OBRACK");
				arrayExpr();
				match("CBRACK");
			}
			else
				expr();
		}
		else if(incrementPending()){
			increment();
		}
		else if(decrementPending()){
			decrement();
		}
		match("SEMI");
	}
	//////////////////////////////////////////////////////////////
	boolean arrayExprPending() {
		return check("OBRACK");
	}
	void arrayExpr() {
		if(check("INTEGER")||
				check("REAL")||
				check("TRUE")||
				check("FALSE")||
				check("STRING")||check("ID")) {
			match(current.type);
			if(check("COMMA")) { //go again?
				match("COMMA");
				arrayExpr();
			}
		}
		else
			fatal("INVALID ARRAY ASSIGNMENT");
	}
	//////////////////////////////////////////////////////////////
	boolean operatorPending() {
		return check("PLUS")		||
				check("MINUS")		||
				check("TIMES")	||
				check("DIVIDE")		||
				check("POWER");
	}
	void operator() {
		match(current.type);
	}
	//////////////////////////////////////////////////////////////
	boolean equalityPending() {
		return check("AND")				||
				check("OR")				||
				check("EQUAL")			||
				check("NOTEQUAL")		||
				check("GREATERTHAN")		||
				check("GREATERTHANEQUAL")	||
				check("LESSTHAN")			||
				check("LESSTHANEQUAL");
	}
	void equality() {
		match(current.type);
	}
	//////////////////////////////////////////////////////////////
	boolean lambdaStatementPending() {
		return check("LAMBDA");
	}
	void lambdaStatement() {
		match("LAMBDA");
		match("OPAREN");
		optParamList();
		match("CPAREN");
		block();
	}
	//////////////////////////////////////////////////////////////
	boolean printStatementPending() {
		return check("PRINT");
	}
	void printStatement() {
		match("PRINT");
		match("OPAREN");
		expr();
		match("CPAREN");
		match("SEMI");
	}	
	//////////////////////////////////////////////////////////////
	boolean returnStatementPending() {
		return check("RETURN");
	}
	void returnStatement() {
		match("RETURN");
		expr();
		match("SEMI");
	}
	//////////////////////////////////////////////////////////////
	boolean whileLoopPending() {
		return check("WHILE");
	}
	void whileLoop() {
		match("WHILE");
		match("OPAREN");
		expr();
		match("CPAREN");
		block();
	}
	//////////////////////////////////////////////////////////////
	boolean ifStatementPending() {
		return check("IF");
	}
	void ifStatement() {
		match("IF");
		match("OPAREN");
		expr();
		match("CPAREN");
		block();
		optElse();
	}
	boolean elseStatementPending() {
		return check("ELSE");
	}
	void optElse() {
		if(elseStatementPending()) {
			match("ELSE");
			if(ifStatementPending()) {//aka elif or else if
				ifStatement();
			}
			else
				block();
		}
	}
	//////////////////////////////////////////////////////////////
	boolean switchStatementPending() {
		return check("SWITCH");
	}
	void switchStatement() {
		match("SWITCH");
		match("OPAREN");
		expr();
		match("CPAREN");
		switchBlock();
	}
	void switchBlock(){
		match("OBRACE");
		optCase();
		optDefault();
		match("CBRACE");
	}
	//////////////////////////////////
	boolean casePending() {
		return check("CASE");
	}
	void optCase() {
		if(casePending()) {
			match("CASE");
			expr();
			match("COLON");
			optStatementList();
			optBreak();
			optCase();
		}
	}
	//////////////////////////////////
	boolean optDefaultPending() {
		return check("DEFAULT");
	}
	void optDefault() {
		if(optDefaultPending()) {
			match("DEFAULT");
			expr();
			match("COLON");
			optStatementList();
			optBreak();
		}
	}
	//////////////////////////////////
	boolean optBreakPending() {
		return check("BREAK");
	}
	void optBreak() {
		if(optBreakPending()) {
			match("BREAK");
			match("SEMI");
		}
	}
	//////////////////////////////////////////////////////////////
	boolean incrementPending() {
		return check("INCREMENT");
	}
	void increment() {
		match("INCREMENT");
		expr();
	}
	////////////////////////////////
	boolean decrementPending() {
		return check("DECREMENT");
	}
	void decrement() {
		match("DECREMENT");
		expr();
	}
	//////////////////////////////////////////////////////////////
	boolean incrementOnePending() {
		return check("INCREMENTONE");
	}
	void incrementOne() {
		match("INCREMENTONE");
		match("ID");
		match("SEMI");
	}
	////////////////////////////////
	boolean decrementOnePending() {
		return check("DECREMENTONE");
	}
	void decrementOne() {
		match("DECREMENTONE");
		match("ID");
		match("SEMI");
	}
	//////////////////////////////////////////////////////////////
	//essentially the driver for Parser
	void parse() {
		current = lexer.lex();
		program();
	}
	//first helper function in the Parser/Parser
	void advance(){
		current = lexer.lex(); //get the next	
//		System.out.print("\tadvance() to: "+current.getValue());
	}
	//like advance but forces the current lexeme to be matched
	void match(String type) { 
		matchNoAdvance(type); //MAY be optional. unknown..
		advance(); 
	}
	//check whether or not the current lexeme is of the given type
	boolean check(String type) {
		return current.type.equals(type); 
	}
	//verify it matches, if not, throw the program
	void matchNoAdvance(String type){
		if (!check(type))
			fatal("syntax error. expected: "+type+" "
					+ "actual type: "+current.type);
	}
	void fatal(String message) {//custom Fatal function
		System.err.printf("Fatal error in Recognizer: "+message+"\n");
		System.err.printf("\tLine: "+current.lineNumber);
		System.exit(1);
	}
	public static void main(String[] args) {
		Recognizer rec = new Recognizer("test1.mylang");
		rec.parse();

		System.out.println("done!");
	}
	
}
