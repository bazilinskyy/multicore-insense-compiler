package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.cgen.ILocation;
import uk.ac.stand.cs.insense.compiler.symbols.STEntry;
import uk.ac.stand.cs.insense.compiler.types.AnyType;
import uk.ac.stand.cs.insense.compiler.types.ArrayType;
import uk.ac.stand.cs.insense.compiler.types.ChannelType;
import uk.ac.stand.cs.insense.compiler.types.ComponentType;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;
import uk.ac.stand.cs.insense.compiler.types.InterfaceType;
import uk.ac.stand.cs.insense.compiler.types.StringType;
import uk.ac.stand.cs.insense.compiler.types.StructType;
import uk.ac.standrews.cs.nds.util.ErrorHandling;

public abstract class Location extends Code implements ILocation {

	protected boolean reordered;
	protected STEntry ste = null;
	
	public Location(STEntry ste ) {
		this.ste = ste; // Caution this is called when null when the ste is not initialised 
	}

	public boolean isPointerAssignment() {
		if(ste != null) {
			ITypeRep thisType = ste.getType();
			return thisType.isPointerType();
		}
		else{
			ErrorHandling.error( "Cannot establish type of variable " + ste );
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense;.compiler.cgen.ILocation#reordered()
	 */
	public boolean reordered() {
		return reordered;
	}
	
	public void generateAssignment( STEntry ste , boolean isAssignmentTo, int fromContext) {
		// ensure compiler knows that this symbol table entry is assigned to
		// and that this is used in send to decided whether to copy or not
		if(isAssignmentTo){
			ste.setAssignedTo(true);
		}
		// Deal with the special cases of how things are declared.

		String initialiser = super.toString();
		if( ste != null ) {	// may have in error cases.
			StringBuffer sb1 = new StringBuffer();
			sb1.append(TAB);
			if( reordered() ) {
				sb1.append( initialiser );
			} 
			else {
				// JL added for garbage collection stuff
				if (isPointerAssignment())
					sb1.append("DAL_assign("+ AMPERSAND );
				sb1.append( ste.contextualName(fromContext));
				
				sb1.append( SPACE );
				if(isPointerAssignment()){
					sb1.append( COMMA );
				}
				else {
					sb1.append( EQUALS );
				}
				sb1.append( SPACE );

				sb1.append( initialiser );
				if(isPointerAssignment()){
					sb1.append( RRB );
				}
			}
			if(ste.getType() instanceof ComponentType){
				sb1.append(SEMI + NEWLINE + TAB + functionCall("component_yield") + SEMI + NEWLINE);
			}
			reset( sb1.toString() );
			if(ste.getType() instanceof StructType){
				StructValue sv = new StructValue(((StructType) ste.getType()));
				sv.complete();
			}
			// TODO JL Space Tracking
			if(isPointerAssignment()){
				Cgen.get_instance().findEnclosingDelcarationContainer().track_call_space(MSP430Sizes.dalAssignCallOverHead(ste.getType()));
			}
		}
	}

	
	public void performReordering( ICode context, ITypeRep rhsType , int fromContext) {
		String last = context.pop();
		if(ste == null){
			context.append("/* ERRROR JL 2008-08-11, ste is null when re-ordering is performed */");
		}
		else{
			if(rhsType.isPointerType()){
				context.append( functionCall("DAL_assign", AMPERSAND + ste.contextualName(fromContext), last) );
			}
			else{
				context.append( ste.contextualName(fromContext) + SPACE + EQUALS_ + last );
			}
		}
		context.append( SEMI );		
		reordered = true;
	}

}
