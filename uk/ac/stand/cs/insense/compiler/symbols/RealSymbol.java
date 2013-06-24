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

public class RealSymbol extends Symbol {
	
	public static RealSymbol REAL = new RealSymbol();
	
	private RealSymbol() {
		super( "real literal" );
	}
	
    public RealSymbol( double the_real ) {
    	super( String.valueOf( the_real ) );
    }
    
    public boolean equals( ISymbol s ) {
        return s instanceof RealSymbol;
    }   
}
