package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.cgen.IDeclarationContainer;
import uk.ac.stand.cs.insense.compiler.cgen.IForLoop;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.symbols.STEntry;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;
import uk.ac.stand.cs.insense.compiler.types.IntegerType;

public class ForLoop extends Code implements ICode, IForLoop { 
	private String lastCode;
	private String startValue;
	private String endValue;
	private String increment = "1";
	private boolean negative = false;
	private boolean specified_increment = false;
	private IDeclarationContainer declContainer;
	private STEntry entry;
	private int fromContext;
	
	private ISymbolTable for_table;
	
	public ForLoop(ISymbolTable for_table) {
		super();
		this.for_table = for_table;
		this.declContainer = Cgen.get_instance().findEnclosingDelcarationContainer();
		this.lastCode = "";
		this.endValue = "";
		this.startValue = "";
		// TODO JL Space Tracking
		declContainer.track_add_stack_element(new IntegerType()); // for iterator
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IForLoop#append(java.lang.String)
	 */
	public void append( String s ) {
		lastCode += s;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IForLoop#finalValue()
	 */
	public void finalValue() {
		startValue = lastCode;
		lastCode = "";
	}	

	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IForLoop#increment()
	 */
	public void increment() {
		endValue = lastCode;
		lastCode = "";
		specified_increment = true;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IForLoop#body()
	 */
	public void body() {
		if(specified_increment){
			increment = lastCode;
		} else {
			endValue = lastCode;
		}
		lastCode = "";		
	}
	
	public void negativeIncrement() {
		negative = true;
		
	}
	
	public void addDecl(STEntry entry) {
		this.entry = entry;
		// following not needed as loop variables are now kept 
		// on the stack rather than in the component
		//declContainer.addLocation( new Decl( entry ) );
	}
	
	// TODO JL Track Space
	private void remove_stack_elements(ISymbolTable table){
        for(STEntry e : table.getLocations()){
        	//System.err.println("project - remove stack element " + e.getName());
    		declContainer.track_remove_stack_element(e.getType());
        }
	}

	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IForLoop#complete()
	 */
	public void complete() {
		String controlVariable = entry.contextualName(fromContext);
		StringBuffer sb = new StringBuffer();
		sb.append(TAB + LCB_ + "// start for loop" + NEWLINE);
		sb.append(TAB + insenseTypeToCTypeName(entry.getType()) + SPACE + entry.getName() + SEMI + NEWLINE);
		sb.append( TAB + FOR + LRB_ + controlVariable + SPACE + EQUALS + SPACE + startValue + SEMI + SPACE );
		if( negative ) {
			sb.append( controlVariable + SPACE + GE_ + endValue + SPACE + SEMI + SPACE);
			sb.append( controlVariable + SPACE + MINUS_EQUALS_ + increment + SPACE + RRB_ );	
		} else {
			sb.append( controlVariable + SPACE + LE_ + endValue + SPACE + SEMI + SPACE );
			sb.append( controlVariable + SPACE + PLUS_EQUALS_ + increment + SPACE + RRB_ );
		}
		sb.append( LCB_ + NEWLINE );
		sb.append( lastCode  ) ; // body
		sb.append( TAB + RCB_ + NEWLINE);
		sb.append( TAB + RCB_ + "// end for loop"+ NEWLINE);
		super.append( sb.toString() );
		// TODO JL Space Tracking
		remove_stack_elements(for_table);
	}
}

	