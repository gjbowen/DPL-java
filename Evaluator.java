//////////////////////
//	Greg Bowen      //
//	Evaluator.java  //
//////////////////////
class Evaluator {
	Environment environment;
	static Parser parser;
	Evaluator(String args[]) {
		parser = new Parser(args[0]);
		environment=new Environment();  
		Lexeme env=environment.create();
		Lexeme tree = parser.parse();
		evalCommandLineArgs(tree,env,args);

		eval(tree, env);
	}
	void evalCommandLineArgs(Lexeme tree,Lexeme env,String[] args) {
		Lexeme argArray = new Lexeme("ARRAY");
		argArray.initArray();
		for(int i=0;i<args.length;++i) {
			argArray.array.add(new Lexeme("STRING",null,args[i]));	
		}
		environment.insert(new Lexeme("ARGS",null,"args"), argArray,env);
	}
	Lexeme evalProg(Lexeme tree,Lexeme env){
		while (tree != null){
			if(tree.left!=null)
				eval(tree.left, env);
			tree = tree.right;
		}
		return null;
	}
	Lexeme evalFuncCall(Lexeme tree,Lexeme env){
		Lexeme closure = eval(getFuncCallName(tree),env); // lookup the 'closure' from env
		Lexeme args = getFuncCallArgs(tree);// get args from func call
		Lexeme evaluated_Args = evalArgs(args, env);// evaluate args from func call
		Lexeme params = getClosureParams(closure);// get params from func def
		Lexeme func_body = getClosureBody(closure);// get body of func 
		Lexeme static_env = getClosureEnvironment(closure);// get static env from env
		Lexeme x_tended_env = environment.extend(params, evaluated_Args, static_env);
		return eval(func_body,x_tended_env);		
	}
	Lexeme evalFuncDef(Lexeme tree, Lexeme env) {
		Lexeme closure = 
				cons("CLOSURE",
						env,
						cons("JOIN",
								getFuncDefParams(tree),
								cons("JOIN",getFuncDefBody(tree),null))
						);

		environment.insert(getFuncDefName(tree),closure,env);
		return closure;
	}

	Lexeme evalArgs(Lexeme args,Lexeme env){
		if (args==null)
			return null;
		if (args.left==null)
			return null;
		else 
			return cons("JOIN",eval(args.left,env),evalArgs(cdr(args),env));
	}

	// FUNC CALL
	Lexeme getFuncCallName(Lexeme tree) {
		// gets closure from the environment 
		return tree.left;
	}
	Lexeme getFuncCallArgs(Lexeme tree) {
		return tree.right;
	}
	// FUNC DEF
	Lexeme getFuncDefName(Lexeme tree) {
		return tree.left;
	}
	Lexeme getFuncDefParams(Lexeme tree) {
		//par.prettyPrint(tree.right.left);
		return tree.right.left;
	}
	Lexeme getFuncDefBody(Lexeme tree) {
		return tree.right.right;
	}
	// CLOSURE GETTERS ////////////////////////////
	Lexeme getClosureParams(Lexeme closure) {
		return closure.right.left;
	}
	Lexeme getClosureBody(Lexeme closure) {
		return closure.right.right.left;
	}
	Lexeme getClosureEnvironment(Lexeme closure) {
		return closure.left;
	}

	//FUNC BLOCK
	Lexeme evalBlock(Lexeme tree,Lexeme env){
		Lexeme result=null;
		while (tree != null){
			result = eval(tree.left, env);
			tree = tree.right;
		}
		return result;
	}
	// Variable methods
	Lexeme evalAssign(Lexeme tree, Lexeme env) {
		Lexeme value = eval(tree.right,env);
		if (tree.left.type.equals("DOT")){
			Lexeme object = eval(tree.left.left,env);
			return environment.update(tree.right.left,object,value);
		}
		else {
			return environment.update(tree.left,env,value);
		}

		//return environment.update(tree.left,env, value);
	}
	Lexeme evalVarDef(Lexeme tree, Lexeme env) {
		Lexeme variable = tree.left.left; 
		Lexeme value =null;
		if(tree.left.right!=null&&tree.left.right.type.equals("OBRACK")){
			value = new Lexeme("ARRAY");
			value.initArray();
			tree =tree.left.right.right;
			while(tree!=null) {
				if(tree.type.equals("CBRACK"))
					break;
				value.array.add(tree);
				tree=tree.right;
			}
		}
		else {
			variable=tree.left.left;
			value = eval(tree.left.right,env);	
		}
		return environment.insert(variable, value,env);
	}

	//dispatcher for simple operations
	Lexeme evalSimpleOp(Lexeme tree,Lexeme env){
		if(tree.type.equals("PLUS")) 
			return evalPlus(tree,env);
		else if(tree.type.equals("MINUS")) 
			return evalMinus(tree,env);
		else if(tree.type.equals("TIMES")) 
			return evalMultiply(tree,env);
		else if(tree.type.equals("DIVIDE")) 
			return evalDivide(tree,env);
		else if(tree.type.equals("POWER"))
			return evalPowerOf(tree,env);
		return null;
	}
	Lexeme evalPowerOf(Lexeme tree, Lexeme env) {
		Lexeme left = eval(tree.left,env);
		Lexeme right = eval(tree.right,env);
		Double result=null;

		if (left.type.equals("INTEGER") && right.type.equals("INTEGER"))
			result = Math.pow(left.intVal,right.intVal);
		else if (left.type.equals("INTEGER") && right.type.equals("REAL"))
			result = Math.pow(left.intVal,right.realVal);
		else if (left.type.equals("REAL") && right.type.equals("INTEGER"))
			result = Math.pow(left.realVal,right.intVal);
		else if (left.type.equals("REAL") && right.type.equals("REAL"))
			result = Math.pow(left.realVal,right.realVal);
		return newRealLexeme(convertToFloat(result));
	}
	// thank you, StackOverflow
	Float  convertToFloat(Double doubleValue) {
		return doubleValue == null ? null : doubleValue.floatValue();
	}

	//start for operations
	Lexeme evalPlus(Lexeme tree,Lexeme env){		
		Lexeme left = eval(tree.left ,env);
		Lexeme right = eval(tree.right,env);

		if(right.type.equals("ARRAY")) {
			String array=right.arrayToString();
			right=new Lexeme("STRING");
			right.strVal=array;
		}
		if(left.type.equals("ARRAY")) {
			String array=right.arrayToString();
			left=new Lexeme("STRING");
			left.strVal=array;
		}

		if (left.type.equals("INTEGER") && right.type.equals("INTEGER"))
			return newIntegerLexeme(left.intVal + right.intVal);
		else if (left.type.equals("INTEGER") && right.type.equals("REAL"))
			return newRealLexeme(left.intVal + right.realVal);
		else if (left.type.equals("REAL") && right.type.equals("INTEGER"))
			return newRealLexeme(left.realVal + right.intVal);
		else if (left.type.equals("REAL") && right.type.equals("REAL"))
			return newRealLexeme(left.realVal + right.realVal);
		// STRING OPERATIONS //
		else if (left.type.equals("REAL") && right.type.equals("STRING"))
			return newStringLexeme(left.realVal.toString() + right.strVal);
		else if (left.type.equals("INTEGER") && right.type.equals("STRING"))
			return newStringLexeme(left.intVal.toString() + right.strVal);
		else if (left.type.equals("STRING") && right.type.equals("REAL"))
			return newStringLexeme(left.strVal + right.realVal.toString());
		else if (left.type.equals("STRING") && right.type.equals("INTEGER"))
			return newStringLexeme(left.strVal + right.intVal.toString());
		else if (left.type.equals("STRING") && right.type.equals("STRING"))
			return newStringLexeme(left.strVal.concat(right.strVal));
		else if (left.type.equals("STRING") && right.type.equals("NULL")) 
			return newStringLexeme(left.strVal);
		else if (left.type.equals("NULL") && right.type.equals("STRING"))
			return newStringLexeme(right.strVal);
		return null;
	}
	Lexeme evalMinus(Lexeme tree, Lexeme env) {
		Lexeme left = eval(tree.left,env);
		Lexeme right = eval(tree.right,env);
		if (left.type.equals("INTEGER") && right.type.equals("INTEGER"))
			return newIntegerLexeme(left.intVal - right.intVal);
		else if (left.type.equals("INTEGER") && right.type.equals("REAL"))
			return newRealLexeme(left.intVal - right.realVal);
		else if (left.type.equals("REAL") && right.type.equals("INTEGER"))
			return newRealLexeme(left.realVal - right.intVal);
		else if (left.type.equals("REAL") && right.type.equals("REAL"))
			return newRealLexeme(left.realVal - right.realVal);
		return null;
	}
	Lexeme evalMultiply(Lexeme tree,Lexeme env){
		Lexeme left = eval(tree.left,env);
		Lexeme right = eval(tree.right,env);
		if (left.type.equals("INTEGER") && right.type.equals("INTEGER"))
			return newIntegerLexeme(left.intVal * right.intVal);
		else if (left.type.equals("INTEGER") && right.type.equals("REAL"))
			return newRealLexeme(left.intVal * right.realVal);
		else if (left.type.equals("REAL") && right.type.equals("INTEGER"))
			return newRealLexeme(left.realVal * right.intVal);
		else if (left.type.equals("REAL") && right.type.equals("REAL"))
			return newRealLexeme(left.realVal * right.realVal);
		return null;
	}
	Lexeme evalDivide(Lexeme tree,Lexeme env){
		Lexeme left = eval(tree.left,env);
		Lexeme right = eval(tree.right,env);
		if (left.type.equals("INTEGER") && right.type.equals("INTEGER"))
			return newIntegerLexeme(left.intVal / right.intVal);
		else if (left.type.equals("INTEGER") && right.type.equals("REAL"))
			return newRealLexeme(left.intVal / right.realVal);
		else if (left.type.equals("REAL") && right.type.equals("INTEGER"))
			return newRealLexeme(left.realVal / right.intVal);
		else if (left.type.equals("REAL") && right.type.equals("REAL"))
			return newRealLexeme(left.realVal / right.realVal);
		return null;
	}
	//dispatcher for comparisons
	Lexeme evalCompare(Lexeme tree,Lexeme env){
		Lexeme eq = tree.left;
		Lexeme optAnd = tree.right;

		Lexeme leftExpr = eval(eq.left,env);
		Lexeme rightExpr = eval(eq.right,env);
		Lexeme result = null;

		if(eq.type.equals("EQUAL"))
			result = evalCompareEqual(leftExpr,rightExpr);
		else if(eq.type.equals("NOTEQUAL"))
			result = evalCompareNotEqual(leftExpr,rightExpr);
		else if(eq.type.equals("GREATERTHAN"))
			result = evalCompareGreaterThan(leftExpr,rightExpr);
		else if(eq.type.equals("GREATERTHANEQUAL"))
			result = evalCompareGreaterThanEqual(leftExpr,rightExpr);
		else if(eq.type.equals("LESSTHAN"))
			result = evalCompareLessThan(leftExpr,rightExpr);
		else if(eq.type.equals("LESSTHANEQUAL"))
			result = evalCompareLessThanEqual(leftExpr,rightExpr);

		if(optAnd!=null) 
			if(optAnd.type.equals("AND") && result.type.equals("TRUE"))
				result = evalCompare(optAnd.left,env);
			else if(optAnd.type.equals("OR") && result.type.equals("FALSE"))
				result = evalCompare(optAnd.left,env);
		return result;
	}	
	//start for comparisons
	Lexeme evalCompareEqual(Lexeme left,Lexeme right) {
		if(left.type.equals("NULL")&&right.type.equals("NULL"))
			return newTrueLexeme();
		if(left.type.equals("STRING")&&right.type.equals("STRING"))
			if(left.strVal.equals(right.strVal))
				return newTrueLexeme();
		if (left.type.equals("INTEGER") && right.type.equals("INTEGER"))
			if(left.intVal==right.intVal)
				return newTrueLexeme();
		if (left.type.equals("INTEGER") && right.type.equals("REAL"))
			if(left.intVal.floatValue()==right.realVal)
				return newTrueLexeme();
		if (left.type.equals("REAL") && right.type.equals("INTEGER"))
			if(left.realVal==right.intVal.floatValue())
				return newTrueLexeme();		
		if (left.type.equals("REAL") && right.type.equals("REAL"))
			if(left.realVal==right.realVal)
				return newTrueLexeme();
		if(left.type.equals("NULL")&&right.type.equals("NULL"))
			return newTrueLexeme();
		return newFalseLexeme();
	}
	Lexeme evalCompareNotEqual(Lexeme left,Lexeme right) {
		Lexeme result = evalCompareEqual(left,right);
		if(result.type.equals("TRUE")) 
			return newFalseLexeme();
		else 
			return newTrueLexeme();
	}
	Lexeme evalCompareGreaterThan(Lexeme left,Lexeme right) {
		if(left.type.equals("STRING")&&right.type.equals("STRING"))
			return newFalseLexeme();
		if(left.type.equals("INTEGER") && right.type.equals("INTEGER"))
			if(left.intVal>right.intVal)
				return newTrueLexeme();
		if(left.type.equals("INTEGER") && right.type.equals("REAL"))
			if(left.intVal.floatValue()>right.realVal)
				return newTrueLexeme();
		if(left.type.equals("REAL") && right.type.equals("INTEGER"))
			if(left.realVal>right.intVal.floatValue())
				return newTrueLexeme();		
		if(left.type.equals("REAL") && right.type.equals("REAL"))
			if(left.realVal>right.realVal)
				return newTrueLexeme();		
		return newFalseLexeme();
	}
	Lexeme evalCompareGreaterThanEqual(Lexeme left,Lexeme right) {
		if(left.type.equals("STRING") && right.type.equals("STRING"))
			return newFalseLexeme();
		if(left.type.equals("INTEGER") && right.type.equals("INTEGER"))
			if(left.intVal>=right.intVal)
				return newTrueLexeme();
		if(left.type.equals("INTEGER") && right.type.equals("REAL"))
			if(left.intVal.floatValue()>=right.realVal)
				return newTrueLexeme();
		if(left.type.equals("REAL") && right.type.equals("INTEGER"))
			if(left.realVal>=right.intVal.floatValue())
				return newTrueLexeme();		
		if(left.type.equals("REAL") && right.type.equals("REAL"))
			if(left.realVal>=right.realVal)
				return newTrueLexeme();	
		return newFalseLexeme();
	}
	Lexeme evalCompareLessThan(Lexeme left,Lexeme right) {
		if(evalCompareGreaterThanEqual(left,right).type.equals("TRUE"))
			return newFalseLexeme();
		else
			return newTrueLexeme();		
	}
	Lexeme evalCompareLessThanEqual(Lexeme left,Lexeme right) {
		if(evalCompareGreaterThan(left,right).type.equals("TRUE"))
			return newFalseLexeme();
		else
			return newTrueLexeme();
	}

	Lexeme evalIncrementOne(Lexeme tree, Lexeme env) {
		Lexeme id = eval(tree.left, env);
		if (id.type.equals("INTEGER")) {
			return new Lexeme("INTEGER",0,++id.intVal);
		} 
		return new Lexeme("REAL",0,++id.realVal);
	}
	Lexeme evalDecrementOne(Lexeme tree, Lexeme env) {
		Lexeme id = eval(tree.left, env);
		if (id.type.equals("INTEGER")) {
			return new Lexeme("INTEGER",0,--id.intVal);
		} 
		return new Lexeme("REAL",0,--id.realVal);
	}

	Lexeme eval(Lexeme tree,Lexeme env){
		//		System.out.println("eval.. "+env.type);
		switch (tree.type){
		case "INTEGER": 
			return tree;
		case "REAL": 
			return tree;
		case "STRING": 
			return tree;
		case "TRUE": 
			return tree;
		case "FALSE": 
			return tree;
		case "NULL": 
			return tree;
		case "DOT":
			return evalDot(tree,env);
		case "THIS":
			return env;
		case "READ":
			return read(tree,env);
		case "CLOSE":
			return close(tree,env);
		case "OPEN":
			return evalOpenFileForReading(tree,env);
		case "EOF":
			return evalEOF(tree,env);
		case "NEXT":
			return evalGetNext(tree,env);
		case "ARRAY_GET": //array
			return getArrayValue(tree,env);
		case "ARRAY_SET":
			setArrayValue(tree,env);
			break;
		case "PRINT":
			evalPrint(tree,env);
			break;
		case "PRINTLN":
			evalPrintLn(tree,env);
			break;
		case "RETURN":
			return evalReturn(tree,env);
		case "ID": 
			return getID(tree,env);
		case "VAR_DEF": 
			return evalVarDef(tree,env);
		case "LAMBDA": 
			return evalLambda(tree,env);
		case "PLUS": 
			return evalSimpleOp(tree,env);
		case "MINUS":
			return evalSimpleOp(tree,env);
		case "TIMES":
			return evalSimpleOp(tree,env);
		case "DIVIDED":
			return evalSimpleOp(tree,env);
		case "POWER":
			return evalSimpleOp(tree,env);
		case "EQUAL": 
			return evalCompare(tree,env);
		case "NOTEQUAL": 
			return evalCompare(tree,env);
		case "GREATERTHAN": 
			return evalCompare(tree,env);
		case "GREATERTHANEQUAL": 
			return evalCompare(tree,env);
		case "LESSTHAN": 
			return evalCompare(tree,env);
		case "LESSTHANEQUAL": 
			return evalCompare(tree,env);
		case "PROG": 
			return evalProg(tree,env);
		case "FUNCTION": 
			return evalFuncDef(tree,env);
		case "FUNC_CALL": 
			return evalFuncCall(tree,env);
		case "BLOCK": 
			return evalBlock(tree,env);
		case "ASSIGN":
			return evalAssign(tree,env);
		case "INCREMENTONE":
			return evalIncrementOne(tree,env);
		case "DECREMENTONE":
			return evalDecrementOne(tree,env);
		case "WHILE":
			return evalWhile(tree,env);
		case "IF":
			return evalIf(tree,env);
		default: {
			System.err.printf("Fatal error in Evaluator: Bad.Expression\n");
			System.err.printf("\t"+tree.type+" - Line: "+tree.lineNumber);
			System.exit(1);
		}
		}
		return null;
	}
	Lexeme close(Lexeme tree, Lexeme env) {
		Lexeme fileToClose =eval(tree.left,env);
		fileToClose.closeFile();
		return newTrueLexeme();
	}

	Lexeme evalEOF(Lexeme tree, Lexeme env) {
		Lexeme currentToken = eval(tree.left,env);
		if(currentToken.strVal!=null&&currentToken.strVal.equals("EOF")) {
			return newFalseLexeme();//other lexeme will be null		
		}
		return currentToken;
	}
	Lexeme evalOpenFileForReading(Lexeme tree,Lexeme env){
		String fileName = tree.left.getValue();
		Lexeme file = cons("FILE_POINTER",null,null);
		file.open(fileName);
		environment.insert(tree.left, file, env);
		return file;
	}
	Lexeme evalGetNext(Lexeme tree,Lexeme env){
		Lexeme nextValue = eval(tree.left,env).getNext();
		environment.update(tree.left, env, nextValue);
		return 	nextValue;
	}

	Lexeme evalCloseFile(Lexeme tree,Lexeme env){
		Lexeme filePointer = car(tree);
		filePointer.fp.close();	//implementation language for closing a file
		return newTrueLexeme(); //gotta return something
	}

	Lexeme evalDot(Lexeme tree,Lexeme env){
		Lexeme object = eval(tree.left, env);
		return eval(tree.right, object); // objects == environments!
	}

	Lexeme evalLambda(Lexeme tree,Lexeme env){
		Lexeme lamndaClosure = cons("LAMBDA_CLOSURE",env,tree);
		return lamndaClosure;
	}

	Lexeme getArrayValue(Lexeme tree, Lexeme env) {
		Lexeme foundArray=environment.lookup(tree.left, env);
		int index=eval(tree.right,env).intVal;
		if(foundArray.array.size()<=index) {
			System.err.printf("\nFatal error in Evaluator: IndexOutOfBoundsException\n");
			System.err.printf("\tType: "+tree.right.type+"\n");
			System.err.printf("\tIndex: "+index+"\n");
			System.err.printf("\tLine: "+tree.right.lineNumber+"\n");
			System.exit(1);
		}

		return foundArray.array.get(index);
	}
	void setArrayValue(Lexeme tree, Lexeme env) {
		int index ;
		if(tree.left.left.type.equals("ID")) {
			index=environment.lookup(tree.left.left,env).intVal;
		}
		else{
			index=tree.left.left.intVal;
		}
		Lexeme newValue = tree.right;

		Lexeme oldValue = environment.lookup(tree.left, env);

		if(oldValue.array.size()<=index) {
			System.err.printf("\nFatal error in Evaluator: IndexOutOfBoundsException\n");
			System.err.printf("\t"+tree.type+" - Line: "+tree.lineNumber+"\n");
			System.exit(1);
		}
		if(oldValue.array!=null)
			oldValue.array.set(index, newValue);
	}

	Lexeme getID(Lexeme tree, Lexeme env) {
		//		par.prettyPrint(tree);
		return environment.lookup(tree,env);
	}

	Lexeme evalIf(Lexeme tree, Lexeme env) {
		Lexeme condBlock = tree.left.left;
		Lexeme result = evalCompare(condBlock,env);
		//par.prettyPrint(tree);
		if(result==null) {
			System.err.printf("\nFatal error in Evaluator: result==null\n");
			System.exit(1);
		}

		if(result.type.equals("TRUE")) {
			return evalBlock(tree.left.right,env);
		}
		else if (tree.right!=null){
			Lexeme optif = tree.right;
			return evalElse(optif,env);
		}
		return null;
	}
	Lexeme evalElse(Lexeme tree,Lexeme env) {
		if(tree.type.equals("IF")) {
			return evalIf(tree,env);
		}else { 
			return evalBlock(tree,env);
		}
	}

	Lexeme read(Lexeme tree,Lexeme env) {
		Lexeme file = tree.left;
		String fileName = eval(file,env).strVal;

		Lexeme value = new Lexeme("ARRAY");
		value.initArray();

		Scanner scanner = new Scanner(fileName);
		scanner.getNext();

		Lexeme integers = getIntegers(new Lexeme("VALUES"),scanner);
		while(integers!=null) {

			value.array.add(integers);
			integers=integers.right;
		}
		return value;
	}

	Lexeme getIntegers(Lexeme temp,Scanner scanner) {
		Lexeme tree = null;
		if(scanner.token.type.equals("INTEGER")) {
			tree = scanner.token;
			scanner.getNext();
			tree.setCdr(getIntegers(temp,scanner));
			return tree;
		}
		else if(scanner.token.type.equals("STRING"))
			return new Lexeme("NULL");
		else {
			System.err.printf("\nFatal error in Evaluator: Non-Integer.Read\n");
			System.err.printf("\tTYPE: "+scanner.token.type+"\n");
			System.err.printf("\tFile: "+scanner.fileName+"\n");
			System.err.printf("\tLine: "+scanner.token.lineNumber+"\n");
			System.exit(1);
			return null;
		}
	}

	Lexeme evalWhile(Lexeme tree, Lexeme env) {
		Lexeme condBlock = tree.left;
		Lexeme block = tree.right;
		Lexeme result = null;
		if(condBlock.type.equals("EOF"))
			result = evalEOF(condBlock,env);
		else
			result = evalCompare(condBlock, env);
		while(true) {

			if(result.type.equals("FALSE"))
				break;
			evalBlock(block,env);

			if(condBlock.type.equals("EOF"))
				result = evalEOF(condBlock,env);
			else
				result = evalCompare(condBlock, env);
		}
		return result;
	}
	void evalPrint(Lexeme tree, Lexeme env) {
		if(tree.left==null) //print blank line
			System.out.print("");
		else {
			System.out.print(eval(tree.left,env).getValue());
		}
	}
	void evalPrintLn(Lexeme tree, Lexeme env) {
		if(tree.left==null) //print blank line
			System.out.println();
		else {
			System.out.println(eval(tree.left,env).getValue());
		}
	}

	Lexeme evalReturn(Lexeme tree, Lexeme env) {
		//par.prettyPrint(tree);
		return eval(tree.left,env);
	}
	Lexeme newIntegerLexeme(Integer i) {
		return new Lexeme("INTEGER",null,i);
	}
	Lexeme newRealLexeme(Float f) {
		return new Lexeme("REAL",null,f);
	}
	Lexeme newStringLexeme(String s) {
		return new Lexeme("STRING",null,s);
	}
	Lexeme newTrueLexeme() {
		return new Lexeme("TRUE",null,true);
	}
	Lexeme newFalseLexeme() {
		return new Lexeme("FALSE",null,false);
	}
	Lexeme car(Lexeme tree) {
		return tree.left;
	}
	Lexeme cdr(Lexeme tree) {
		return tree.right;
	}
	Lexeme cons(Lexeme tree,Lexeme left,Lexeme right) {
		tree.left=left;
		tree.right=right;
		return tree;
	}
	Lexeme cons(String tree,Lexeme left,Lexeme right) {
		return cons(new Lexeme(tree),left,right);
	}
}
