package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.util.ArrayList;
import java.util.List;

import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.cgen.IStructConstructor;
import uk.ac.stand.cs.insense.compiler.types.StructType;

public class StructConstructor extends StructDeclaration implements ICode, IStructConstructor {

	StructType type;
	
	public StructConstructor( StructType type  ) {
		super( type );
		super.append( "construct_" + struct_name() + LRB);
		this.type = type;
	}
	
	public StructConstructor() {
		super();
		//this.initialisers = new ArrayList<String>();

	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IStructConstructor#append(java.lang.String)
	 */
	public void append(String s) {
		super.append( s );
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IStructConstructor#complete()
	 */
	public void complete() {	
		super.complete();
		Cgen.get_instance().addIncludeToCurrentContext( include_headers() );
		super.append( RRB );
		// TODO JL Space Tracking
		Cgen.get_instance().findEnclosingDelcarationContainer().track_call_space(MSP430Sizes.constructStruct_CallOverhead(type));
	}	
}
