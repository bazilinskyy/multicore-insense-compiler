package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.cgen.IDecl;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.symbols.STEntry;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

public class Decl extends Location implements ICode, IDecl {
	
	private String name; // only used when we dont have an STE set - always a transient condition
	String initialiser;
	private int scopeLevel;
	
	public Decl( String name , int scopeLevel ) {
		super(null);
		this.name = name;
		this.scopeLevel = scopeLevel;
		this.initialiser = "0";
	}
	
	public Decl( STEntry entry ) {
		super(entry);
		this.name = entry.getName();
	}	
	
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.ILocation#getType()
	 */
	public ITypeRep getType() {
		return ste.getType();
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.ILocation#getType()
	 */
	public String getBaseName() {
		if( ste != null ) {
			return ste.getName(); 
		} else {
			return name ;
		}
	}	

	public int getScopeLevel(){
		return this.scopeLevel;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.ILocation#addSymbolTableEntry(uk.ac.stand.cs.insense.compiler.symbols.STEntry)
	 */
	public void addSymbolTableEntry(STEntry entry) {
		this.ste = entry;
		this.name = entry.getName();
		// TODO JL Space Tracking
		track_add_stack_element( entry.getType() );	// Keep track of space
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.ILocation#getInitialiser()
	 */
 
	 public String getInitialiser() {
		return initialiser;
	}
	
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.ILocation#complete()
	 */
	public void complete() {
		 
		initialiser = super.toString();
		if(ste != null){
			generateAssignment( ste , false , ste.getScope_level()); // false means not real assignment, avoids copy of item on send
		}
		// TODO JL Space Tracking
		Cgen.get_instance().findEnclosingDelcarationContainer().track_add_stack_byte( this.get_maximal_stack_usage() );
	}

	public STEntry getSymbolTableEntry() {
		return ste;
	}
}
