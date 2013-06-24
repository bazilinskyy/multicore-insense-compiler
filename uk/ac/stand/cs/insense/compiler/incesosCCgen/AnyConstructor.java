package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import uk.ac.stand.cs.insense.compiler.cgen.IAnyConstructor;
import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.types.*;

public class AnyConstructor extends TypeMarshaller implements ICode, IAnyConstructor {

	private ITypeRep tr;
	private String expr;
	private boolean need_serialization = true;
	private ICode call_stack;
	
	public AnyConstructor(ICode call_stack) {
		super();
		this.call_stack = call_stack;
		tr = UnknownType.TYPE;
		expr = "";
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IAnyConstructor#valueType(uk.ac.stand.cs.insense.compiler.types.ITypeRep)
	 */
	public void valueType( ITypeRep type ) {
		tr = type;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IAnyConstructor#append(java.lang.String)
	 */
	public void append( String s ) {
		expr += s;
	}

	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IAnyConstructor#complete()
	 */
	public void complete() {
		if(need_serialization){     // if we may need seialization for this type
			addSerializer( tr );	// generate a serializer
		}
		
		String constructor = "";
		
		if( tr.equals( BooleanType.TYPE ) )
			constructor = "Construct_BoolAnyType0( ";
		else if( tr.equals( IntegerType.TYPE ) || tr instanceof EnumType )
			constructor = "Construct_IntAnyType0( ";
		else if( tr.equals( UnsignedIntegerType.TYPE ) )
			constructor = "Construct_UnsignedIntAnyType0( ";
		else if( tr.equals( RealType.TYPE ) )
			constructor = "Construct_RealAnyType0( ";
		else if( tr.equals( ByteType.TYPE ) )
			constructor =  "Construct_ByteAnyType0( ";
		else if( tr.equals( UnknownType.TYPE) )
			throw new RuntimeException("don't recognise type: " + tr.getClass().toString() );
		else
			 constructor = "Construct_PointerAnyType0( ";
		
		super.append( constructor + potential_copy_function_call(tr, expr) + COMMA + DQUOTE + tr.toStringRep() + DQUOTE + SPACE + RRB_ );
		// TODO JL Space Tracking
		call_stack.track_call_space(MSP430Sizes.anyTypeConstructCallOverhead(tr));
	}

	public void setNeed_serialization(boolean need_serialization) {
		this.need_serialization = need_serialization;
	}
	
	
	
}
