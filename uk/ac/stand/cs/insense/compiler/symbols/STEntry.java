/*
 * Created on 26-Jun-2006 at 13:36:21.
 */
package uk.ac.stand.cs.insense.compiler.symbols;

import uk.ac.stand.cs.insense.compiler.incesosCCgen.Code;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.types.ChannelType;
import uk.ac.stand.cs.insense.compiler.types.FunctionType;
import uk.ac.stand.cs.insense.compiler.types.EnumType;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:al@cs.st-andrews.ac.uk"> Alan Dearle </a>
 */
public class STEntry {
	
    private final String name;
    private final ITypeRep type;
    private final boolean isType;
    private final int scope_level;
    private boolean isAssignedTo;
    private boolean isSent;
	
    
    private final String GLOBAL_SUFFIX = "_glob";
    private final String COMPONENT_SUFFIX = "_comp";
    private final String BEHAVIOUR_SUFFIX = "_behav";
    private final String FUNCTION_SUFFIX= "_proc";

	private int context = 0;
	private int disambiguator = 0;
    
    /**
     * Creates a structure for declaring 
     * @param name - the name being declared
     * @param type1 - the type of declaration
     * @param isType - if this is a typedecl rather than a value decl
     * @param scope_level - the scope level at which the declaration is being made
     */
    public STEntry( String name, ITypeRep type, Boolean isType, int scope_level, int context, int diasambiguator ) {
        this.name = name;
        this.type = type;
        this.isType = isType;
        this.scope_level = scope_level;
        this.context = context;
        this.disambiguator = disambiguator;
        this.isAssignedTo = false;
        this.isSent = false;
    }    
    
//    public String contextualName(int fromContext) {
//    	String prefix = "";
//    	String param = "";
//    	
////    	if(context == ISymbolTable.CONSTRUCTOR_PARAMETERS && fromContext == ISymbolTable.CONSTRUCTOR){
////    		return baseName();
////    	}
////		if( getType() instanceof ChannelType && context == ISymbolTable.COMPONENT){
////			prefix += Code.STAR;
////		}
//    	if(context != ISymbolTable.GLOBAL){
//    		prefix += Code.THIS + Code.ARROW ;
//    		param += Code.THIS ;
////    		if( (fromContext == ISymbolTable.FUNCTION || fromContext == ISymbolTable.CONSTRUCTOR) && context == ISymbolTable.COMPONENT){
////    			prefix +=  "comp" + Code.ARROW;
////    			param += Code.ARROW + "comp" ;
////    		}
//    		if( getType() instanceof ChannelType && context == ISymbolTable.COMPONENT){ // components have impl
//    			return prefix + getName(); 
//    		}
////			if( getType() instanceof FunctionType ) 
////				prefix += Code.IMPL + Code.ARROW ;
//		}
//    	else{
//    		if(getType() instanceof ChannelType && context == ISymbolTable.COMPONENT){
//    			return prefix + getName();     			
//    		}
//    	}
//    	return prefix + getName();
//    }
    
    
    public String contextualName(int fromContext) {	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< TODO LOOK HERE FIRST IF NAMING PROBLEMS !!!!!
    	if( context == ISymbolTable.COMPONENT ) {
    		return Code.THIS + Code.ARROW + getName();
    	} else {
    		return getName();
    	}
    }
    
    public String baseName() { return name; }

	
	public String getName( String s ) {
		if (getType() instanceof EnumType)
			return s;
		else{
			switch( context ) {
				case ISymbolTable.GLOBAL:
					return s +  GLOBAL_SUFFIX;
				case ISymbolTable.COMPONENT:
					return s + COMPONENT_SUFFIX;
				case ISymbolTable.BEHAVIOUR:
					return s; //+ getScope_level() + "_" + disambiguator + BEHAVIOUR_SUFFIX;
				case ISymbolTable.FUNCTION:
					return s; // + getScope_level() + "_" + disambiguator + FUNCTION_SUFFIX;
				case ISymbolTable.CONSTRUCTOR:
					return s;
				case ISymbolTable.CONSTRUCTOR_PARAMETERS:
					return s;
				default:
					return s;
			}
		}
	}
	
	public String getName(){
		return getName( name );
	}
	
	public ITypeRep getType() {
		return type;
	}
	
	public int getContext() {
		return context;
	}

	public int getDisambiguator() {
		return disambiguator;
	}

	public boolean isType() {
		return isType;
	}

	public int getScope_level() {
		return scope_level;
	}

	public boolean isAssignedTo() {
		return isAssignedTo;
	}

	public boolean isSent() {
		return isSent;
	}

	public void setAssignedTo(boolean isAssignedTo) {
		this.isAssignedTo = isAssignedTo;
	}

	public void setSent(boolean isSent) {
		this.isSent = isSent;
	}


}
