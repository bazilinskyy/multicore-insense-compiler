package uk.ac.stand.cs.insense.compiler.symbols;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
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
public class SymbolTable  implements ISymbolTable {
	
	private Map<String,STEntry> symbolMap;
	private ISymbolTable parent;
	private int scope_level;
	private int context;	// This keeps track of the "purpose" of the new Symbol table
							// specifies if the symbol table is for a BEHAVIOUR,  COMPONENT, CONSTRUCTOR or PROC
	
	static int disambiguate_name = 0;
	
	
	public SymbolTable( ISymbolTable parent, int scope_level, int context ){
		symbolMap = new Hashtable<String,STEntry>();
		this.parent = parent;
		this.scope_level = scope_level;
		this.context = context;
		disambiguate_name++;
	}
	
	public static ISymbolTable newScope( int scope_level, int context ){
		return new SymbolTable( null, scope_level, context );
	}
	
	public boolean hasParent(){
		return parent != null;
	}
	
	public boolean declare(String identifier, ITypeRep type, Boolean isType ) {
		if ( symbolMap.get( identifier ) == null ){
			symbolMap.put( identifier , new STEntry( identifier, type, isType, scope_level, context, disambiguate_name ) );
            return true;
		}
		else {
			return false;
		}
	}
	
	public AnyProjectSTEntry declareAnyProject(String identifier, ITypeRep type, Boolean isType ) {
		if ( symbolMap.get( identifier ) == null ){
			AnyProjectSTEntry entry = new AnyProjectSTEntry( identifier, type, isType, this.scope_level, this.context, disambiguate_name );
			symbolMap.put( identifier , entry );
            return entry;
		}
		else {
			return null;
		}
	}
	
	public STEntry lookup(String identifier) {
		STEntry val = symbolMap.get( identifier );
		 if (val == null && parent != null ) {
			 return parent.lookup( identifier );
		 }
		 else if ( val != null ){
			 return val;
		 }
		 else {
			return null;
		 }
		
	}

	public ISymbolTable getParentScope() {
		return parent;
	}
	
	public int getScopeLevel() {
		return scope_level;
	}
	
	public int getContext() {
		return context;
	}
	
	public String toString(){
		Set keys = symbolMap.keySet();
		Iterator it = keys.iterator();
		StringBuffer buf = new StringBuffer();
		buf.append( "--------SymbolTable--------\n");
		while ( it.hasNext() ){
			String key = (String) it.next();
			buf.append( key + " = " + symbolMap.get( key ) + "\n" );
		}
		buf.append("----------------------------\n");
		return buf.toString();
	}

	public boolean is_declared_locally(String Identifier) {
		return symbolMap.containsKey(Identifier);
	}

	/*
	 * @see uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable#enterScope(uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable)
	 */
	public ISymbolTable enterScope(ISymbolTable parent) {
		return new SymbolTable( parent,parent.getScopeLevel() + 1, parent.getContext() );
	}

	/*
	 * @see uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable#enterScope(uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable, int)
	 */
	public ISymbolTable enterScope(ISymbolTable parent, int context) {
		return new SymbolTable( parent, parent.getScopeLevel() + 1, context );
	}
	
	public List<STEntry> getLocations(){
		List<STEntry> list = new ArrayList<STEntry>();
		for( String s : symbolMap.keySet()){
			list.add(symbolMap.get(s));
		}
		return list;
	}
	
}
