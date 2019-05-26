///////////////////////
//	Greg Bowen	     //
//	Environment.java //
///////////////////////
class Environment {
	Environment(){
	}
	Lexeme setCar(Lexeme env, Lexeme value) {
		env.left = value;
		return env;
	}
	Lexeme setCdr(Lexeme env, Lexeme value) {
		env.right = value;
		return env;
	}
	Lexeme car(Lexeme env) {
		return env.left;
	}
	Lexeme cdr(Lexeme env){
		return env.right;
	}
	Lexeme cons(Lexeme env,Lexeme left,Lexeme right) {
		env.left=left;
		env.right=right;
		return env;
	}
	Lexeme cons(String envName,Lexeme left,Lexeme right) {
		return cons(new Lexeme(envName),left,right);
	}
	boolean sameVariable(Lexeme var1,Lexeme var2) {
		if(var1.strVal==null || var2.strVal==null)
			return false;
		if(var1.strVal.equals(var2.strVal))
			return true;
		return false;
	}
	Lexeme create(){
		return cons("ENV",cons("TAB",null,null),null);
	}
	Lexeme lookup(Lexeme variable, Lexeme env) {
		while (env != null){
			Lexeme table = car(env);
			Lexeme vars = car(table);
			Lexeme vals = cdr(table);
			while (vars != null){
				if (sameVariable(car(vars),variable)){
					return car(vals);
				}
				vars = cdr(vars);
				vals = cdr(vals);
			}
			env = cdr(env);
		}

		System.err.printf("\nFatal error in Environment: Variable.Undefined\n");
		System.err.printf("\t"+variable.type+": "+variable.strVal+"\n\tLine: "+variable.lineNumber+"\n");
		System.exit(1);

		return null;
	}
	Lexeme update(Lexeme variable,Lexeme env, Lexeme newValue) {
		while (env != null){
			Lexeme table = car(env);
			Lexeme vars = car(table);
			Lexeme vals = cdr(table);
			while (vars != null){
				if (sameVariable(car(vars),variable)){
					if(newValue.strVal != null)
						car(vals).updateValue(newValue.strVal);
					else if(newValue.intVal != null)
						car(vals).updateValue(newValue.intVal);
					else if(newValue.realVal != null)
						car(vals).updateValue(newValue.realVal);
					else if(newValue.boolVal != null)
						car(vals).updateValue(newValue.boolVal);
					return car(vals);
				}
				vars = cdr(vars);
				vals = cdr(vals);
			}
			env = cdr(env);
		}
		System.err.printf("\nFatal error in Environment: Variable.Undefined\n");
		System.err.printf("\t"+variable.type+": "+variable.strVal+" - Line: "+variable.lineNumber+"\n");
		System.exit(1);
		return null;
	}
	Lexeme insert(Lexeme variable,Lexeme value,Lexeme env){
		Lexeme table = car(env);
		setCar(table, cons("JOIN", variable, car(table)));
		setCdr(table, cons("JOIN", value, cdr(table)));
		setCar(env,table);
		return value;
	}
	Lexeme extend(Lexeme variables,Lexeme values,Lexeme env){
		return cons("ENV",cons("VALUES",variables,values),env);
	}
	void display(Lexeme env) {
		System.out.println("--------- Showing Environments ---------");
		int count = 1;
		while (env != null) {
			System.out.println("Level: "+count);

			Lexeme table = car(env);
			Lexeme vars  = car(table);
			Lexeme vals  = cdr(table);

			while (vars != null) {

				showLexemes(vars,vals,count);

				vars = cdr(vars);
				vals = cdr(vals);
			}
			env = cdr(env);//next environment
			++count;
		}
	}
	void showLexemes(Lexeme vars,Lexeme vals,int level){
		for(int i=0;i<level*2;++i)
			System.out.print(" ");
		System.out.println(vars.strVal+"="+vals.getValue());
	}
}