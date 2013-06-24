package uk.ac.stand.cs.insense.compiler.cgen;

import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.symbols.AnyProjectSTEntry;
import uk.ac.stand.cs.insense.compiler.symbols.STEntry;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

public interface IAnyProject extends ICode {

	public abstract void defaultArm(ISymbolTable arm_table);

	//public abstract void choiceArm(AnyProjectSTEntry entry, ISymbolTable arm_table);
	public abstract void choiceArm(STEntry entry, ISymbolTable arm_table);

}