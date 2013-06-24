package uk.ac.stand.cs.insense.compiler.cgen;

import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

public interface IUnionDeclaration extends ICode {

	/**
	 * Adds type information for branches
	 * @param tr - the type to be added as a brach
	 */
	public abstract void armType(ITypeRep tr);

	public abstract String unionName();

	public abstract String unionTypeName();

	public abstract String unionStructName();

}