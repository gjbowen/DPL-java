///////////////////
//  Greg Bowen	 //
//  Lexeme.java	 //
///////////////////
import java.util.ArrayList;

class Lexeme {
	String type;
	Integer lineNumber;

	String strVal;
	Integer intVal;
	Float realVal;
	Boolean boolVal;
	ArrayList<Lexeme> array;

	Lexeme left;
	Lexeme right;

	Scanner fp;

	void initArray() {
		array = new ArrayList<>();
	}
	void open(String file) {
		fp = new Scanner(file);
	}
	Lexeme getNext() {
		return fp.getNext();
	}
	void closeFile() {
		
		
	}
	
	String arrayToString() {
		StringBuilder str = new StringBuilder("");
		if(array!=null) {
			str.append("[");
			for(int i=0;i<array.size()-1;++i) {
				if(array.get(i).strVal!=null)
					str.append("\""+array.get(i).getValue()+"\"");
				else
					str.append(array.get(i).getValue());
				if(i!=array.size()-2)
					str.append(",");
			}
			str.append("]");
		}
		return str.toString();
	}

	Lexeme(String t){
		type=t;
	}
	Lexeme(String t,String str){
		type=t;
		strVal=str;
	}
	Lexeme(String t,int l){ 
		type=t;
		lineNumber=l;
	}
	Lexeme(String t,int l,String str){
		type=t;
		lineNumber=l;
		strVal=str;
	}
	Lexeme(String t,int l,Integer i){
		type=t;
		lineNumber=l;
		intVal=i;
	}
	Lexeme(String t,int l,Float d){
		type=t;
		lineNumber=l;
		realVal=d;
	}
	Lexeme(String t,int l,Boolean b){
		type=t;
		lineNumber=l;
		boolVal=b;
	}
	//used for new evaluated lexemes. Object will be nul
	Lexeme(String t, String object, Float f) {
		type = t;
		realVal = f;
	}
	Lexeme(String t, String object, Integer i) {
		type = t;
		intVal = i;
	}
	Lexeme(String t, String object, Boolean b) {
		type = t;
		boolVal = b;
	}
	Lexeme(String t, String object, String s) {
		type = t;
		strVal = s;
	}
	void setCar(Lexeme car) {
		this.left=car;
	}
	void setCdr(Lexeme cdr) {
		this.right = cdr;
	}
	Lexeme car() {
		return this.left;
	}
	Lexeme cdr() {
		return this.right;
	}
	String getValue() {
		if(strVal!=null)
			return strVal;
		else if(intVal!=null)
			return intVal.toString();
		else if(realVal!=null)
			return realVal.toString();
		else if(boolVal!=null)
			return boolVal.toString();
		else if(array!=null)
			return arrayToString();
		else
			return "null";
	}
	void updateValue(String s) {
		clearValues();
		strVal=s;
	}
	void updateValue(Integer i) {
		clearValues();
		intVal=i;
	}
	void updateValue(Float r) {
		clearValues();
		realVal=r;
	}
	void updateValue(Boolean b) {
		clearValues();
		boolVal=b;
	}
	void clearValues(){
		strVal=null;
		intVal=null;
		realVal=null;
		boolVal=null;
		array=null;
	}
}
