///////////////////
//  Greg Bowen   //
//  Parser.java	 //
///////////////////
public class Parser {
	Lexeme current;
	Lexer lexer;

	Parser(String file){
		lexer = new Lexer(file);
	}
	static int count=1;
	static void prettyPrint(Lexeme tree) {
		if(tree!=null) {
			System.out.print  ("TYPE: "+tree.type);
			System.out.print  ("    VALUE: "+tree.getValue());
			System.out.println("    LINE: "+tree.lineNumber);
			System.out.println("left node: "+tree.car()+"\tright node: "+tree.cdr());
			if(tree.car()!=null) {
				++count;
				System.out.println("going to left node..\n");
				prettyPrint(tree.car());
				System.out.println("back up to parent node..");
			}
			if(tree.cdr()!=null) {
				++count;
				System.out.println("going to right node..\n");
				prettyPrint(tree.cdr());
				System.out.println("back up to parent node..");
			}
		}
	}
	//////////////////////////////////////////////////////////////
	Lexeme program(){
		Lexeme tree = new Lexeme("PROG");
		if(statementPending()) {
			tree.setCar(statement());
		}
		if (programPending())
			tree.setCdr(program());
		return tree;
	}
	boolean programPending(){
		return statementPending();
	}
	//////////////////////////////////////////////////////////////
	boolean funcDefPending(){
		return check("FUNCTION");
	}
	Lexeme funcDef(){
		Lexeme tree = cons(match("FUNCTION"),match("ID"),null);
		Lexeme body = new Lexeme("FUNC_GLUE");
		match("OPAREN");
		body.setCar(paramList()); 
		match("CPAREN");

		body.setCdr(block());

		tree.setCdr(body);;
		return tree;
	}

	//////////////////////////////////////////////////////////////
	boolean paramListPending(){
		return check("ID");
	}
	Lexeme paramList(){
		Lexeme paramTree= cons("PARAMS",null,null);
		if(paramListPending()) {
			//paramTree=match("ID");
			paramTree.setCar(match("ID"));
		}
		if(check("COMMA")) {
			match("COMMA");//more!
			paramTree.setCdr(paramList());	
		}
		return paramTree;
	}
	//////////////////////////////////////////////////////////////
	Lexeme block() {
		match("OBRACE");
		Lexeme tree = statementList();
		match("CBRACE");
		return tree;
	}
	//////////////////////////////////////////////////////////////
	boolean statementPending() {
		return thisPending()			||
				closePending()			||
				lambdaStatementPending()||
				funcDefPending()		||
				printStatementPending()	||
				printLnStatementPending()||	
				whileLoopPending()		||
				ifStatementPending()	||
				returnStatementPending()||
				switchStatementPending()||
				incrementOnePending()	||
				decrementOnePending()	||
				optBreakPending()		||
				varDefExprPending()		||
				varExprPending();
	}
	boolean thisPending() {
		if(check("THIS"))
			return true;
		return false;
	}
	Lexeme statement() {
		if(thisPending())
			return thisStatement();
		if(closePending())
			return close();
		if(lambdaStatementPending())
			return lambdaStatement();
		else if(funcDefPending())
			return funcDef();
		else if(printStatementPending())
			return printStatement();
		else if(printLnStatementPending())
			return printLnStatement();
		else if(whileLoopPending())
			return whileLoop();
		else if(ifStatementPending())
			return ifStatement();
		else if(returnStatementPending())
			return returnStatement();
		else if(switchStatementPending())
			return switchStatement();
		else if(incrementOnePending())
			return incrementOne();
		else if(decrementOnePending())
			return decrementOne();
		else if(optBreakPending())
			return optBreak();
		else if(varDefExprPending())
			return varDefExpr();
		else if(varExprPending())
			return varExpr();
		return null;
	}
	boolean closePending() {
		return check("CLOSE");
	}
	Lexeme close() {
		Lexeme tree =cons(match("CLOSE"),null,null);
		match("OPAREN");
		tree.setCar(expr());
		match("CPAREN");
		match("SEMI");
		return tree;
	}
	private Lexeme thisStatement() {
		Lexeme tree = match("THIS");
		match("SEMI");
		return tree;
	}
	Lexeme statementList() {
		if(statementPending())
			return cons("BLOCK",statement(),statementList());
		return null;
	}

	Lexeme argsList(){
		Lexeme argTree = cons("ARGS",expr(),null);
		if(check("COMMA")) {
			match("COMMA");
			argTree.setCdr(argsList());
		}
		return argTree;
	}
	Lexeme expr() {
		Lexeme uTree=unary();
		if(check("DOT")) {
			uTree=cons(match("DOT"),uTree,match("ID"));
		}
		else if(operatorPending()){ // +  -  *  /  ^
			Lexeme oTree=operator();
			Lexeme eTree=expr();
			return cons(oTree,uTree,eTree);
		}
		else if(check("OBRACK")) {
			match("OBRACK");
			uTree = cons("ARRAY_GET",uTree,expr());
			match("CBRACK");
		}
		else if(check("NEXT")) {
			uTree =cons(match("NEXT"),null,null);
			match("OPAREN");
			uTree.setCar(expr());
			match("CPAREN");
		}

		else if(check("OPEN")) {
			uTree =cons(match("OPEN"),null,null);
			match("OPAREN");
			uTree.setCar(expr());
			match("CPAREN");
		}
		else if(check("READ")) {
			uTree =cons(match("READ"),null,null);
			match("OPAREN");
			uTree.setCar(expr());
			match("CPAREN");
		}
		else if(check("EOF")){
			uTree =cons(match("EOF"),null,null);
			match("OPAREN");
			uTree.setCar(expr());
			match("CPAREN");
		}
		if(equalityPending()){ // <  > == <=
			Lexeme eqTree = equality();
			eqTree.setCar(uTree);
			eqTree.setCdr(expr());
			if(andPending())
				return cons("condBlock",eqTree,cons(match("AND"),expr(),null));
			else if(orPending())
				return cons("condBlock",eqTree,cons(match("OR"),expr(),null));
			else
				return cons("condBlock",eqTree,null);
		}
		else if(check("OPAREN")){ //for function calls 
			match("OPAREN"); //ID OPAREN EXPR EXPR CPAREN SEMI
			uTree=cons("FUNC_CALL",uTree,argsList());
			match("CPAREN");		
		} 		
		return uTree;
	}
	boolean unaryPending() {
		return check("INTEGER")||
				check("REAL")||
				check("TRUE")||
				check("FALSE")|| 
				check("AND")||
				check("OR")||
				check("STRING")||
				check("NULL")||
				check("ID");
	}
	Lexeme unary() { 
		if(unaryPending())
			return advance();
		return null;
	}
	//////////////////////////////////////////////////////////////
	boolean varDefExprPending(){ 
		return check("VAR_DEF"); 
	}
	boolean varExprPending(){ 
		return check("ID"); 
	}
	Lexeme varExpr(){ 
		Lexeme tree = cons(match("ID"),null,null);
		if (check("OPAREN")){ //function call
			match("OPAREN"); 
			tree=cons("FUNC_CALL",tree,null);
			tree.setCdr(argsList());
			match("CPAREN"); 
		}

		else if(check("OBRACK")) {  //array set
			match("OBRACK");
			tree.setCar(expr());
			match("CBRACK");			
			Lexeme temp = tree;
			tree = cons("ARRAY_SET",temp,null);
			tree.lineNumber=temp.lineNumber;
			match("ASSIGN");
			tree.setCdr(expr());
		}
		else if(check("ASSIGN")) {
			tree = cons(match("ASSIGN"),tree,null);
			if(arrayExprPending()) { //array inititalize
				tree.setCdr(arrayExpr());
			}else { 
				tree.setCdr(expr());
			}
		}
		else if(incrementPending()){
			tree.setCar(match("INCREMENT"));
			tree.setCdr(unary());
		}
		else if(decrementPending()){
			tree.setCar(match("DECREMENT"));
			tree.setCdr(unary());
		}
		match("SEMI");
		return tree;
	}
	Lexeme varDefExpr(){ 
		Lexeme tree;
		Lexeme temp;
		temp  = match("VAR_DEF");
		Lexeme temp2 = match("ID");
		tree = cons(match("ASSIGN"),temp2,null);
		if(arrayExprPending())
			tree.setCdr(arrayExpr());
		else {
			tree.setCdr(expr());
		}
		//prettyPrint(cons(temp,tree,null));
		match("SEMI");
		return cons(temp,tree,null);
	}
	//////////////////////////////////////////////////////////////
	boolean arrayExprPending() {
		return check("OBRACK");
	}
	Lexeme arrayExpr() {
		if(check("OBRACK")) {//start
			return cons(match("OBRACK"),null,arrayExpr());
		}
		else if(unaryPending()) {
			return cons(match(current.type),null,arrayExpr());
		}
		else if(check("COMMA")) { //go again?
			match("COMMA");//burn the comman.go again.
			return arrayExpr();
		}
		else if(check("CBRACK")) { //end array!
			return match("CBRACK");
		}
		else
			fatal("INVALID ARRAY ASSIGNMENT");
		return null;
	}

	//////////////////////////////////////////////////////////////
	boolean operatorPending() {
		return check("PLUS")		||
				check("MINUS")		||
				check("TIMES")		||
				check("DIVIDE")		||
				check("POWER");
	}
	Lexeme operator() {
		return advance();
	}
	//////////////////////////////////////////////////////////////
	boolean compoundPending() {
		return check("AND")||
				check("OR");
	}
	Lexeme compound() {
		return match(current.type);
	}
	//////////////////////////////////////////////////////////////
	boolean equalityPending() {
		return check("EQUAL")				||
				check("NOTEQUAL")			||
				check("GREATERTHAN")		||
				check("GREATERTHANEQUAL")	||
				check("LESSTHAN")			||
				check("LESSTHANEQUAL");
	}
	Lexeme equality() {
		return match(current.type);
	}
	//////////////////////////////////////////////////////////////
	boolean lambdaStatementPending() {
		return check("LAMBDA");
	}
	Lexeme lambdaStatement() {
		Lexeme tree = match("LAMBDA");
		match("OPAREN");
		tree.setCar(paramList());
		match("CPAREN");
		tree.setCdr(block());
		return tree;
	}
	//////////////////////////////////////////////////////////////
	boolean printStatementPending() {
		return check("PRINT");
	}
	Lexeme printStatement() {
		Lexeme tree=cons(match("PRINT"),null,null);
		match("OPAREN");
		tree.setCar(expr());
		match("CPAREN");
		match("SEMI");
		return tree;
	}
	//////////////////////////////////////////////////////////////
	boolean printLnStatementPending() {
		return check("PRINTLN");
	}
	Lexeme printLnStatement() {
		Lexeme tree=cons(match("PRINTLN"),null,null);
		match("OPAREN");
		tree.setCar(expr());
		match("CPAREN");
		match("SEMI");
		return tree;
	}
	//////////////////////////////////////////////////////////////
	boolean returnStatementPending() {
		return check("RETURN");
	}
	Lexeme returnStatement() {
		Lexeme tree=cons(match("RETURN"),expr(),match("SEMI"));
		return tree;
	}
	//////////////////////////////////////////////////////////////
	boolean whileLoopPending() {
		return check("WHILE");
	}
	Lexeme whileLoop() {
		Lexeme tree = cons(match("WHILE"),null,null);
		match("OPAREN");
		if(check("EOF")) {
			tree.setCar(match("EOF"));
			match("OPAREN");
			tree.left.setCar(expr());
			match("CPAREN");
		}
		else
			tree.setCar(expr());//conditions
		match("CPAREN");
		tree.setCdr(block());
		return tree;
	}
	//////////////////////////////////////////////////////////////
	boolean ifStatementPending() {
		return check("IF");
	}
	Lexeme ifStatement() {
		Lexeme tree =  cons(match("IF"),null,null);
		Lexeme subTree =  cons("ifBody",null,null);
		match("OPAREN");
		subTree.setCar(expr());
		match("CPAREN");
		subTree.setCdr(block());
		tree.setCar(subTree);

		tree.setCdr(optElse());
		//prettyPrint(tree);
		return tree;
	}
	boolean elseStatementPending() {
		return check("ELSE");
	}
	Lexeme optElse() {
		if(elseStatementPending()) {
			Lexeme tree=cons("optElse",match("ELSE"),null);
			if(ifStatementPending())//aka "elif" or "else if"
				tree = ifStatement();
			else
				tree.setCar(block());
			return tree;
		}
		return null;
	}

	Lexeme car(Lexeme tree) {
		return tree.left;
	}
	boolean andPending() {
		return check("AND");
	}
	boolean orPending() {
		return check("OR");
	}
	//////////////////////////////////////////////////////////////
	boolean switchStatementPending() {
		return check("SWITCH");
	}
	Lexeme switchStatement() {
		Lexeme tree = cons(match("SWITCH"),null,null);
		match("OPAREN");
		tree.setCar(expr());
		match("CPAREN");
		match("OBRACE");
		tree.setCdr(cases());
		match("CBRACE");
		return tree;
	}
	Lexeme cases() {
		if(casePending())
			return cons("CASE",optCase(),cases());
		else if(defaultPending())
			return cons("DEFAULT",optDefault(),null);//done with switch statement!
		else
			return null;
	}
	//////////////////////////////////
	boolean casePending() {
		return check("CASE");
	}
	Lexeme optCase() {
		Lexeme tree = cons(match("CASE"),expr(),null);
		match("COLON");
		tree.setCdr(statementList());
		return tree;
	}
	//////////////////////////////////
	boolean defaultPending() {
		return check("DEFAULT");
	}
	Lexeme optDefault() {
		Lexeme tree = cons(match("DEFAULT"),null,null);
		match("COLON");
		tree.setCdr(statementList());
		return tree;
	}
	//////////////////////////////////
	boolean optBreakPending() {
		return check("BREAK");
	}
	Lexeme optBreak() {
		Lexeme tree = match("BREAK");
		match("SEMI");
		return tree;
	}
	//////////////////////////////////////////////////////////////
	boolean incrementPending() {
		return check("INCREMENT");
	}
	////////////////////////////////
	boolean decrementPending() {
		return check("DECREMENT");
	}
	//////////////////////////////////////////////////////////////
	boolean incrementOnePending() {
		return check("INCREMENTONE");
	}
	Lexeme incrementOne() {
		Lexeme tree=cons(match("INCREMENTONE"),match("ID"),null);
		match("SEMI");
		return tree;
	}
	////////////////////////////////
	boolean decrementOnePending() {
		return check("DECREMENTONE");
	}
	Lexeme decrementOne() {
		Lexeme tree=cons(match("DECREMENTONE"),match("ID"),null);
		match("SEMI");
		return tree;
	}
	//////////////////////////////////////////////////////////////
	//essentially the driver for Parser
	Lexeme parse() {
		current = lexer.lex();
		Lexeme tree = program();
		return tree;
	}
	//first helper function in the Parser/Parser
	Lexeme advance(){
		Lexeme oldLexeme = current;
		current=lexer.lex(); //get the next	
		//System.out.println(current.type);
		return oldLexeme;
	}
	//like advance but forces the current lexeme to be matched
	Lexeme match(String type) { 
		if(type.equals("ANYTHING"))
			return advance();
		matchNoAdvance(type); //MAY be optional. unknown..
		return advance(); 
	}
	//check whether or not the current lexeme is of the given type
	boolean check(String type) {
		return current.type.equals(type); 
	}
	//verify it matches, if not, throw the program
	void matchNoAdvance(String type){
		if (!check(type))
			fatal("Syntax.Error\n\tExpected: "+type+"\n\t"
					+ "Actual: "+current.type);
	}
	void fatal(String message) {//custom Fatal function
		System.err.printf("\nFatal error in Parser: "+message+"\n");
		System.err.printf("\tLine: "+current.lineNumber+"\n");
		System.exit(1);
	}
	Lexeme cons(String type,Lexeme left,Lexeme right){
		return cons(new Lexeme(type),left,right);
	}
	Lexeme cons(Lexeme main,Lexeme left,Lexeme right){
		main.setCar(left);
		main.setCdr(right);
		return main;
	}
}
