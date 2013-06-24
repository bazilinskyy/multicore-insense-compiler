package uk.ac.stand.cs.insense.compiler.cgen;

import uk.ac.stand.cs.insense.compiler.symbols.STEntry;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

public interface IDecl extends ILocation {

	public abstract ITypeRep getType();

	public abstract String getInitialiser();
	
	public abstract int getScopeLevel();
	
	public abstract String getBaseName();
	
	public abstract void addSymbolTableEntry(STEntry entry);
	
	public abstract STEntry getSymbolTableEntry();

	public abstract void complete();

}