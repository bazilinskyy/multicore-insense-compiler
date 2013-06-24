package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.util.ArrayList;
import java.util.List;

import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.cgen.IStructConstructor;
import uk.ac.stand.cs.insense.compiler.types.StructType;

public class StructValue extends StructDeclaration implements ICode, IStructConstructor {

	public StructValue( StructType type  ) {
		super( type );
	}
	
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IStructConstructor#complete()
	 */
	public void complete() {	
		super.complete();
		Cgen.get_instance().addIncludeToCurrentContext( include_headers() );
	}
	
}
