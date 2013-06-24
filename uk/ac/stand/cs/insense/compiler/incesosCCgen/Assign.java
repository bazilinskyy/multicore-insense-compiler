package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import uk.ac.stand.cs.insense.compiler.cgen.IAssign;
import uk.ac.stand.cs.insense.compiler.symbols.STEntry;
import uk.ac.stand.cs.insense.compiler.types.*;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

public class Assign extends Location implements IAssign {

	int fromContext;
	
	public Assign( STEntry ste , int fromContext) {
		super(ste); 
		this.fromContext = fromContext;
	}

	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.cgen.ILocation#getTargetName()
	 */
	public String getTargetName() {
		if( ste != null ) {		// following compilation error
			return ste.contextualName(fromContext);
		}
		return "ERROR";
	}
	
	public void complete() {
		generateAssignment( ste , true, fromContext); // true means real assignment, causes potential copy of item on send
	}
	
}


