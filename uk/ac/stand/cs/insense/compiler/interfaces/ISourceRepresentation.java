package uk.ac.stand.cs.insense.compiler.interfaces;
/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:ajm@dcs.st-and.ac.uk"> Andrew J. McCarthy </a>
 */
public interface ISourceRepresentation {
	public char getNextChar();
	public char peek();
	public int lineNumber();
	public String currentLine();
	public void resetCurrentLine();
	
}
