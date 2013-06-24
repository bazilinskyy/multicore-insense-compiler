package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import uk.ac.stand.cs.insense.compiler.types.ArrayType;
import uk.ac.stand.cs.insense.compiler.types.BooleanType;
import uk.ac.stand.cs.insense.compiler.types.ByteType;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;
import uk.ac.stand.cs.insense.compiler.types.IntegerType;
import uk.ac.stand.cs.insense.compiler.types.RealType;
import uk.ac.stand.cs.insense.compiler.types.StructType;

/**
 * @author al
 * Helper class for array dereferencing
 */
public abstract class ArrayOps extends Code {

	private static String construct_name = "Construct_Array";
	protected static String loc_function = "array_loc";
	
	private static String selector_pntr = VOIDSTAR_;
	private static String selector_int = "int";
	private static String selector_bool = "bool";	
	private static String selector_real = "float";
	private static String selector_byte = "uint8_t";
	private static String selector_array = "IArrayPNTR";

	
	public static String type_selector( ITypeRep referend_type ) {
		String c_array_type = selector_pntr;
		if( referend_type.equals( IntegerType.TYPE ) ) {
			c_array_type = selector_int;
		} else 	if( referend_type.equals( BooleanType.TYPE ) ) {
			c_array_type = selector_bool;
		} else if( referend_type.equals( RealType.TYPE ) ) {
			c_array_type = selector_real;
		} else if( referend_type.equals( ByteType.TYPE ) ) {
			c_array_type = selector_byte;
		} else if ( referend_type instanceof StructType ) {
			c_array_type = insenseTypeToCTypeName(referend_type);
		} else if ( referend_type instanceof ArrayType ) {
			c_array_type = selector_array;
		}
		return c_array_type;
	}
	
	public static String array_deref_function( ITypeRep referend_type ) {
		return STAR + LRB + type_selector( referend_type ) + STAR + RRB  + loc_function ;
	}
	
	public static String array_lhs_deref_function(ITypeRep referend_type) {
		return array_deref_function(referend_type);
	}
	
	public static String array_constructor_name(ITypeRep type) {
		return construct_name ;
	}

	public static String paramName( int i ) { return "p" + i; }
	
	public static String loopName( int i ) { return "iter" + i; }
}
