package uk.ac.stand.cs.insense.compiler.cgen;

public interface ICode extends ISpaceTracker {

	public void append( String s );
	public void complete();
	public String toString();
	public void reset( String s );
	public String pop();
	
}
