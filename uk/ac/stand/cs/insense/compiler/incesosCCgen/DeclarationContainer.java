package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.cgen.IDecl;
import uk.ac.stand.cs.insense.compiler.cgen.IDeclarationContainer;
import uk.ac.stand.cs.insense.compiler.cgen.IFunction;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.types.ArrayType;
import uk.ac.stand.cs.insense.compiler.types.CType;
import uk.ac.stand.cs.insense.compiler.types.ChannelType;
import uk.ac.stand.cs.insense.compiler.types.ComponentType;
import uk.ac.stand.cs.insense.compiler.types.FunctionType;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;
import uk.ac.stand.cs.insense.compiler.types.StructType;
import uk.ac.standrews.cs.nds.util.Diagnostic;

/**
 * @author Alan Dearle
 */
public abstract class DeclarationContainer extends Code implements ICode, IDeclarationContainer {
	//protected HashMap<String,IDecl> locations;
	// don't really need a map and map does not appear to be able to hold multiple entries with the same key
	// which is necessary as all component, constructor and behaviour decls are recorded in the component
	// TODO JL may have same duplicate problem with other maps and may not need a map, check
	protected ArrayList<IDecl> locations;
	protected HashMap<String,FunctionType> functionTypes;
	protected HashMap<String,IFunction> functionBodies;
	protected StringBuffer hoistedCode; 
	
	public DeclarationContainer() {
		//this.locations = new HashMap<String,IDecl>();
		this.locations = new ArrayList<IDecl>();
		this.functionTypes = new HashMap<String,FunctionType>();
		this.functionBodies = new HashMap<String,IFunction>();
		this.hoistedCode = new StringBuffer();
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IDeclarationContainer#addLocation(uk.ac.stand.cs.insense.compiler.Ccgen.Location)
	 */
	public void addLocation( IDecl l ) {
		//locations.put( l.getBaseName(),l );
		locations.add( l );
	}
	

	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IDeclarationContainer#addFunction(uk.ac.stand.cs.insense.compiler.Ccgen.Function)
	 */
	public void addFunction(IFunction fb) {
		String name = fb.getName();
		FunctionType fn = fb.getFt();
		functionTypes.put( name,fn );
		functionBodies.put( name,fb );
	}
	
	protected String locationInitialisers() {
		StringBuffer sb = new StringBuffer();
		sb.append(GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE);
		//Set<String> ks = locations.keySet();
		//for( String s : ks ) {
		// JL test
		for( IDecl l : locations ) {
			ITypeRep tr = l.getType();
			if(l.getScopeLevel() == ISymbolTable.COMPONENT){
				if(tr.isPointerType()){
					sb.append(TAB + functionCall("DAL_assign", AMPERSAND + l.getSymbolTableEntry().contextualName(ISymbolTable.UNKNOWN_CONTEXT) , l.getInitialiser()) + SEMI + NEWLINE);
				}
				else {
					sb.append( TAB + l.getSymbolTableEntry().contextualName(ISymbolTable.UNKNOWN_CONTEXT) + SPACE + EQUALS_ + l.getInitialiser() + SEMI + NEWLINE);
				}
				if(tr instanceof ComponentType){
					sb.append(TAB + functionCall("component_yield") + SEMI + NEWLINE);
				}
			}
		}
		return sb.toString();
	}
	
	protected String locationDecls() {
		StringBuffer sb = new StringBuffer();
		sb.append(GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE);
		//Set<String> ks = locations.keySet();
		//for( String s : ks ) {
		// JL test
		for( IDecl l : locations ) {
			ITypeRep tr = l.getType();
			boolean component_decl = this instanceof Component;
			//boolean procedure_decl = this instanceof Procedure;
			boolean is_not_component_level_channel = !component_decl || !(tr instanceof ChannelType) || (tr instanceof ChannelType && l.getSymbolTableEntry().getScope_level() > ISymbolTable.COMPONENT) ;
			if(is_not_component_level_channel){ // component level channels are added to component's interface in impl to simplify dereferencing
				sb.append( TAB + insenseTypeToCTypeName( tr ) + SPACE + l.getSymbolTableEntry().getName() + SEMI );
			}
		}
		return sb.toString();
	}
	
	/**
	 * Writes the declarations for local variables to the stream
	 * @param ps - the stream on which the decls are written
	 */
	protected void printLocationDecls( PrintStream ps ) {
		ps.println( GENERATED_FROM + Diagnostic.getMethodInCallChain() );
		ps.print( locationDecls() );
	}

	public void addHoistedCode(String s) {
		hoistedCode.append( s );
	}

	protected void printHoistedCode(PrintStream ps) {
		ps.println(generateHoistedCode());
	}

	protected String generateHoistedCode() {
		return hoistedCode.toString();
	}

	protected static boolean requiresCopying(ITypeRep thisType) {
		return thisType instanceof StructType ||  thisType instanceof ArrayType ;
	}

	protected void printCopyMacros(PrintStream ps) {
		//Set<String> ks = locations.keySet();
		//for( String s : ks ) {
		for( IDecl l : locations ) {
			if(requiresCopying(l.getType())){
				ps.println( GENERATED_FROM + Diagnostic.getMethodInCallChain() );
				
				ps.print( "#define POTENTIALLY_COPY_" + l.getSymbolTableEntry().getName() + SPACE );
				if( l.getSymbolTableEntry().isSent() &&  l.getSymbolTableEntry().isAssignedTo() ) {
					if(l.getType() instanceof StructType){
						ps.print("copy_" + ((StructType)l.getType()).getName());
					}
					else if (l.getType() instanceof ArrayType){
						ps.print(generate_unique_copy_function_name(l.getType()));
					}
					else{
						ps.print("// Don't have copy function for " +l.getType());
					}
					ps.print(LRB + l.getSymbolTableEntry().contextualName(ISymbolTable.COMPONENT) + RRB + NEWLINE);
				}
				else
					ps.print(LRB + l.getSymbolTableEntry().contextualName(ISymbolTable.COMPONENT) + RRB + TAB + "// no need to copy ;-)"+ NEWLINE);
				ps.print(NEWLINE);
			}
		}
	}

	public ArrayList<IDecl> getLocations() {
		return locations;
	}

}
