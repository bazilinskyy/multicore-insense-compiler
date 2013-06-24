package uk.ac.stand.cs.insense.compiler.cgen;

import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

public interface IArrayConstructor extends ICode {

	public void initialiser();
	
	public void type( ITypeRep basetype, ITypeRep arrayType );
}