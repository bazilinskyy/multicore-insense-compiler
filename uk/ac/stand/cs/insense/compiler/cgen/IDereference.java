package uk.ac.stand.cs.insense.compiler.cgen;

import uk.ac.stand.cs.insense.compiler.symbols.STEntry;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

public interface IDereference extends ILocation {

	void fieldDereference(String the_name );
	
	void channelDereference(String the_name);
	
	void arrayDereference();

	void lengthDereference();

	void leftHandSide();

	void setDerefType( ITypeRep t );
}
