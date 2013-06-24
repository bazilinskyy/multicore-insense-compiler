package uk.ac.stand.cs.insense.compiler.interfaces;

import java.util.List;

import uk.ac.stand.cs.insense.compiler.symbols.AnyProjectSTEntry;
import uk.ac.stand.cs.insense.compiler.symbols.STEntry;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:ajm@dcs.st-and.ac.uk"> Andrew J. McCarthy </a>
 */
public interface ISymbolTable {
	
	public static int GLOBAL = 0;
	public static int COMPONENT = 1;
	public static int BEHAVIOUR = 2;
	public static int FUNCTION = 3;
	public static int CONSTRUCTOR_PARAMETERS = 4;
	public static int CONSTRUCTOR = 5;
	public static int PROJECT = 6;
	public static int UNKNOWN_CONTEXT = 7;
	
	public abstract STEntry lookup(String identifier);

	public abstract ISymbolTable getParentScope();
	
	public int getScopeLevel();
	
	public int getContext();
	
	public abstract ISymbolTable enterScope( ISymbolTable parent, int context );
	
	public abstract ISymbolTable enterScope( ISymbolTable parent );
	
	public abstract boolean declare(String identifier, ITypeRep type, Boolean isType );
	
	public abstract AnyProjectSTEntry declareAnyProject(String identifier, ITypeRep type, Boolean isType );
	
	public abstract boolean is_declared_locally(String Identifier);
	
	public abstract List<STEntry> getLocations();
}