package uk.ac.stand.cs.insense.compiler.cgen;

import java.util.List;

import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

public interface IConstructorCall extends ICode {

	public abstract void setDisambiguatorAndParameters(int index, List<ITypeRep> paramTypes);

	public abstract void parameter();

}