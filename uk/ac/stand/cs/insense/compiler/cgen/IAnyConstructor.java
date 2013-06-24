package uk.ac.stand.cs.insense.compiler.cgen;

import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

public interface IAnyConstructor extends ICode {

	public abstract void valueType(ITypeRep type);
	public void setNeed_serialization(boolean need_serialization);


}