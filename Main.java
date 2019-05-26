/////////////////
//	Greg Bowen //
//	Main.java  //
/////////////////
public class Main {

	public static void main(String[] args) {
		if(args.length==0) {
			System.err.printf("THERE ARE NO COMMAND LINE ARGUMENTS\n");
			System.exit(1);
		}
		new Evaluator(args);
	}
}
