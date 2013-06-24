package uk.ac.stand.cs.insense.compiler.cgen;

import uk.ac.stand.cs.insense.compiler.types.ITypeRep;
import uk.ac.stand.cs.insense.compiler.types.StructType;

public interface IStructDeclaration extends ICode {

	public abstract void set_type(StructType type);

	/**
	 * Adds type information for fields
	 * @param tr - the type to be added as a field
	 */
	public abstract void fieldType(ITypeRep tr);

	public abstract void fieldName(String s);


}