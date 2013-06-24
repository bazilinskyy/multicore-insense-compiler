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

public class IntegerSymbol extends Symbol {
	
	public static IntegerSymbol INTEGER = new IntegerSymbol();
	
	private IntegerSymbol() {
		super( "integer literal" );
	}
	
    public IntegerSymbol( int the_int ) {
        super( Integer.toString( the_int ) );
    }
    
    public boolean equals( ISymbol s ) {
        return s instanceof IntegerSymbol;
    }   
}
