package uk.ac.stand.cs.insense.compiler.cgen;

import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

public interface ISwitch extends ICode {

	void defaultArm();

	void switchArm();

	void switchExp();

	void switchMatchType(ITypeRep matchType);


}