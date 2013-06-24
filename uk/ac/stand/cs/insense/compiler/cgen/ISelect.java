package uk.ac.stand.cs.insense.compiler.cgen;

import uk.ac.stand.cs.insense.compiler.symbols.STEntry;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

public interface ISelect extends ICode {

	void receiveArm(boolean acknowledge);

	void from(IDecl decl );

	void when();

	void selectExp(boolean acknowledge);

	void defaultArm();
	
}