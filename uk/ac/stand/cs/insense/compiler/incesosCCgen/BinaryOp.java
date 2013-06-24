package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import uk.ac.stand.cs.insense.compiler.cgen.IBinaryOp;
import uk.ac.stand.cs.insense.compiler.cgen.ICode;

public class BinaryOp extends Code implements ICode, IBinaryOp {

	public BinaryOp(String op) {
		super();
		super.append( op );
	}
}
