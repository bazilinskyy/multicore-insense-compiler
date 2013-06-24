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

public class ByteSymbol extends Symbol {
    
	public static ByteSymbol BYTE = new ByteSymbol();
	
	private ByteSymbol() {
		super( "byte literal" );
	}

	public ByteSymbol( int the_int ) {
        super( Integer.toString( the_int ) );
    }
    
    public boolean equals( ISymbol s ) {
        return s instanceof ByteSymbol;
    }   
}
