package org.alicebot.server.sql.pool;
	
	public class Debug {
	protected static boolean doDebug = false;
	public static void main(String argv[]){
		Debug.setDebug(true);
		Debug.println("works !");
		Debug.println(23);
		Debug.println(23.4);
	}
	public static void print(){
		if (doDebug){System.out.println();}
	}
	public static void print(double o){
		if (doDebug){System.out.print(o);}
	}
	public static void print(float o){
		if (doDebug){System.out.print(o);}
	}
	public static void print(int o){
		if (doDebug){System.out.print(o);}
	}
	public static void print(long o){
		if (doDebug){System.out.print(o);}
	}
	public static void print(Object o){
		if (doDebug){System.out.print(o);}
	}
	public static void println(){
		if (doDebug){System.out.println();}
	}
	public static void println(double o){
		if (doDebug){System.out.println(o);}
	}
	public static void println(float o){
		if (doDebug){System.out.println(o);}
	}
	public static void println(int o){
		if (doDebug){System.out.println(o);}
	}
	public static void println(long o){
		if (doDebug){System.out.println(o);}
	}
	public static void println(Object o){
		if (doDebug){System.out.println(o);}
	}
	public static void println(boolean o){
		if (doDebug){System.out.println(o);}
	}
	public static void setDebug( String debug ){
		if(debug.equals("true")){
			doDebug = true;
		}
		if(debug.equals("1")){
			doDebug = true;
		}
	}
	public static void setDebug( boolean debug ){
			doDebug = debug;
	}
}
