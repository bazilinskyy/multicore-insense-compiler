package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.cgen.IConditional;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.symbols.STEntry;

public class Conditional extends Code implements ICode, IConditional {
	
	
	// TODO JL Space Tracking
	// keep symbol tables for then and else blocks so as to be able to 
	// remove stack space resulting from local decls in these blocks
	ISymbolTable then_table;
	ISymbolTable else_table;
	
	public Conditional() {
		super();
		then_table = null;
		else_table = null;
		append( IF );
		append( LRB_ );
	}	
	
	
	
	// TODO JL Space Tracking
	private void remove_stack_elements(ISymbolTable table){
        for(STEntry e : table.getLocations()){
    		Cgen.get_instance().findEnclosingDelcarationContainer().track_remove_stack_element(e.getType());
        }
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IConditional#thenBranch()
	 */
	public void thenBranch(ISymbolTable then_table) {
		this.then_table = then_table;
		append( RRB_ );
		append( LCB_ );
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IConditional#elseBranch()
	 */
	public void elseBranch(ISymbolTable else_table) {
		append(SEMI + NEWLINE);
		remove_stack_elements(then_table);
		append( RCB_ );
		append( ELSE_ );
		append( LCB_ );
	}	
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IConditional#complete()
	 */
	public void complete() {
		// TODO JL Space Tracking
		append(SEMI + NEWLINE);
		if(else_table == null){
			remove_stack_elements(then_table);
		} else {
			remove_stack_elements(else_table);
		}
		append( RCB_ );	
	}

}
