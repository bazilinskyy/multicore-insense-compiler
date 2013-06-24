package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import uk.ac.stand.cs.insense.compiler.cgen.ISwitch;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

public class Switch extends Code implements ISwitch {
	
	boolean first = true;
	private String unique_id = "";
	
	public Switch() {
		super();
	}

	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.cgen.ISwitch#switchMatchType(uk.ac.stand.cs.insense.compiler.types.ITypeRep)
	 * By the time this is called we have already generated the code for the swich expression
	 */
	public void switchMatchType( ITypeRep matchType ) {
		String swtichexp = super.pop();	// the code for the expression
		// makes a decl e.g. int unique_id = swtichexp;
		super.reset( insenseTypeToCTypeName( matchType ) + SPACE + get_unique_id() + EQUALS + swtichexp + SEMI + NEWLINE );
		
	}
	
	public void defaultArm() {
		if( ! first ) {
			append( SEMI + SPACE + RCB_ + ELSE_ );
		} 
		first = false;
		append( LCB_ );
	}

	public void switchArm() {
		if( ! first ) {
			append( SEMI + SPACE + RCB_ + ELSE_ );
		} 
		first = false;
		append( IF );
		append( LRB_ );
		append( get_unique_id() + EQUALSEQUALS_ );
	}
	
	public void switchExp() {
		append( RRB_ );
		append( LCB_ );
	}

	private String get_unique_id() {
		if( unique_id.equals( "" ) ) {
			unique_id = generate_unique_id();
		}
		return unique_id;
	}

	public void complete() {
		append( SEMI );
		append( RCB_ );
	}
}
