/*
 * Created on 13 Sep 2006 at 11:54:44.
 */
package uk.ac.stand.cs.insense.compiler.symbols;

import uk.ac.stand.cs.insense.compiler.interfaces.ISymbol;


/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:ajm@dcs.st-and.ac.uk"> Alan Dearle </a>
 */

public class StringSymbol extends Symbol {
    
    public static final ISymbol STRING = new StringSymbol();
	
	private StringSymbol() {
		super( "string literal" );
	}
	
	public StringSymbol( String the_string ) {
        super( the_string );
    }
    
    public boolean equals( ISymbol s ) {
        return s instanceof StringSymbol;
    }
    
	public String toString() {
		return "\"" + stringRep + "\"";
	}
}
