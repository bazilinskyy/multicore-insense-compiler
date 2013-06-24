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
public class Symbol implements ISymbol {
	protected String stringRep;
    
    public Symbol(String stringRep) {
        this.stringRep = stringRep;
    }
    
    /* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.symbols.ISymbol#equals(java.lang.String)
	 */
    public boolean equals( String s ) { 
        return s.equals(stringRep);
    } 
    
    /* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.symbols.ISymbol#equals(uk.ac.stand.cs.insense.compiler.symbols.ISymbol)
	 */
    public boolean equals( ISymbol s ) { 
        return this == s;
    } 	
    
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.symbols.ISymbol#toString()
	 */
	public String toString() {
		return stringRep;
	}
}
