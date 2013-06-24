package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.util.ArrayList;
import java.util.List;

import uk.ac.stand.cs.insense.compiler.cgen.IAnyProject;
import uk.ac.stand.cs.insense.compiler.cgen.IDeclarationContainer;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.symbols.AnyProjectSTEntry;
import uk.ac.stand.cs.insense.compiler.symbols.STEntry;
import uk.ac.stand.cs.insense.compiler.types.*;

public class AnyProject extends UnionDeclaration implements IAnyProject {

	private boolean inDefault;
	private String defaultCode;
	private String arm_name;

	private boolean first_arm;
	
	private List<String> armCodes;
	//private List<AnyProjectSTEntry> choiceEntry;
	private List<STEntry> choiceEntry;
	private IDeclarationContainer declContainer;
	//private STEntry ste;
	private String subject_name;
	private int fromContext;
	
	private ISymbolTable arm_table;

	
	//public AnyProject( STEntry ste,  IDeclarationContainer declarationContainer , int fromContext ) {
	public AnyProject( String subject_name,  IDeclarationContainer declarationContainer , int fromContext ) {
		super();
		//this.ste = ste;
		this.subject_name = subject_name;
		inDefault = false;
		first_arm = true;
		defaultCode = null;
		arm_name = "";
		//choiceEntry = new ArrayList<AnyProjectSTEntry>();
		choiceEntry = new ArrayList<STEntry>();
		declContainer = declarationContainer;
		armCodes = new ArrayList<String>();
		fromContext = fromContext;
	}
	
	// TODO JL Track Space
	private void remove_stack_elements(ISymbolTable table){
        for(STEntry e : table.getLocations()){
        	//System.err.println("project - remove stack element " + e.getName());
    		Cgen.get_instance().findEnclosingDelcarationContainer().track_remove_stack_element(e.getType());
        }
	}

	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IAnyProject#append(java.lang.String)
	 */
	public void append( String s ) {
		if( inDefault ) {
			defaultCode = s;
		} else {
			armCodes.add( s );
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IAnyProject#complete()
	 */
	public void complete() {
		super.complete();
		// TODO JL Space Tracking
		// at end of project block, remove local decls from stack usage
		remove_stack_elements(arm_table);

		super.append( tab(1) + "//any project start" + NEWLINE );
		
		// Declare a component/constructor decl for the union
		// We need to fake this up as it doesnt have an insense type
		//ITypeRep as_decl_type = new CType( unionStructName() );
		String as_decl_name = choiceEntry.get(0).baseName();
		//STEntry faked_decl = new STEntry( as_decl_name, as_decl_type,false,choiceEntry.get(0).getScope_level(),choiceEntry.get(0).getContext(), choiceEntry.get(0).getDisambiguator() );
		//declContainer.addLocation( new Decl( faked_decl ) );

		
		// TODO JL Space Tracking
		// calculate overhead for project isEqual call, union declaration, ...
		int project_overhead_bytes = MSP430Sizes.anyTypeProjectOverhead(armTypes);
		//System.err.println("project overhead: " + project_overhead_bytes);
		Cgen.get_instance().findEnclosingDelcarationContainer().track_call_space(project_overhead_bytes);
		int index = 0;
		for( String the_block  : armCodes ) { // generate an if testing union and the corresponding code block
			if( index != 0 ) {
				super.append( tab(1) + ELSE_ );
			} else {
				super.append(tab(1));
			}
			
			//String subject_name = ste == null ? "error" : ste.contextualName(fromContext);

			super.append( IF + LRB_ );
			super.append( functionCall("anyTypeIsEqual", subject_name, DQUOTE + armTypes.get(index).toStringRep() + DQUOTE ) );
			super.append( RRB_ + LCB_ + NEWLINE );
			//super.append( TAB + insenseTypeToCTypeName(as_decl_type) + SPACE + as_decl_name + SEMI + NEWLINE);
			//super.append( TAB + insenseTypeToCTypeName(choiceEntry.get(index).getType()) + SPACE + choiceEntry.get(index).contextualName(fromContext) + SPACE + EQUALS_ + SPACE + cast( armTypes.get(index)) );
			super.append( tab(2) + insenseTypeToCTypeName(choiceEntry.get(index).getType()) + SPACE + choiceEntry.get(index).contextualName(fromContext) + SPACE + EQUALS_ + SPACE );
			super.append( functionCall( selector( armTypes.get(index) ), subject_name ) + SEMI + NEWLINE ); 
			super.append( tab(1) + the_block );
			super.append( tab(1) + RCB_ + NEWLINE );
			
			index++;
		}
		if( defaultCode != null ) {
			if( index != 0 ) {
				super.append( tab(1) + ELSE_ + LCB_ + NEWLINE );
				super.append( tab(1) + defaultCode );
				super.append( NEWLINE + tab(1) + RCB_ + SPACE );
			}
		}
		super.append( NEWLINE + tab(1) + "//any project end" + NEWLINE );
		
	}
	
	private String selector( ITypeRep tr ) {
		addDeSerializer( tr );
		if( tr.equals( BooleanType.TYPE ) )
			return "anyTypeGetBoolValue";
		else if( tr.equals( IntegerType.TYPE ) || tr instanceof EnumType )
			return "anyTypeGetIntValue";
		else if( tr.equals( UnsignedIntegerType.TYPE ) )
			return "anyTypeGetUnsignedIntValue";
		else if( tr.equals( RealType.TYPE ) )
			return "anyTypeGetRealValue";
		else if( tr.equals( ByteType.TYPE ) )
			return  "anyTypeGetByteValue";
		else if( tr.equals( UnknownType.TYPE) ) {
			throw new RuntimeException("don't recognise type: " + tr.getClass().toString() );
		}
		else
			return "anyTypeGetPointerValue";
	}

	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IAnyProject#choiceArm(uk.ac.stand.cs.insense.compiler.types.ITypeRep)
	 */
	public void choiceArm( STEntry entry, ISymbolTable arm_table ) {
		choiceEntry.add(entry);
		arm_name = entry.getName();
		armTypes.add( entry.getType() );
		// TODO JL Space Tracking
		// add artificial decl for projection variable onto stack
		Cgen.get_instance().findEnclosingDelcarationContainer().track_add_stack_element(entry.getType());
		// TODO JL Space Tracking
		// at end of project block, remove local decls from stack usage
		if(!first_arm){
			remove_stack_elements(arm_table);
		}
		first_arm = false;
		this.arm_table = arm_table;
		potentiallyGenerateGenerators(entry.getType());
	}

	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IAnyProject#defaultArm()
	 */
	public void defaultArm(ISymbolTable arm_table) {
		// TODO JL Space Tracking
		// at end of project block, remove local decls from stack usage
		if(!first_arm){
			remove_stack_elements(this.arm_table);
		}
		this.arm_table = arm_table;
		inDefault = true;
	}
}
