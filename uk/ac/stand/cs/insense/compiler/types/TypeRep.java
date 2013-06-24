package uk.ac.stand.cs.insense.compiler.types;

/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:ajm@dcs.st-and.ac.uk"> Andrew J. McCarthy </a>
 * @author <a href="mailto:al@dcs.st-and.ac.uk"> Alan Dearle </a>
 */
public abstract class TypeRep implements ITypeRep {
	
	
    /* (non-Javadoc)
	 * These names are used in the creation of reps used at runtime to represent types
	 * e.g. type type of an array of integers is represented as "[i"
	 */
	protected static String DELIMITER = "_";
	protected static String ANY_REP = "a";
	protected static String ARRAY_REP = "A";
	protected static String BOOLEAN_REP = "b";
	protected static String BYTE_REP = "8";
	protected static String CHANNEL_REP_IN = "I";
	protected static String CHANNEL_REP_OUT = "O";
	protected static String COMPONENT_REP = "C";
	protected static String ENUM_REP = "e";
	protected static String FUNCTION_REP = "f";
	protected static String INTEGER_REP = "i";
	protected static String UNSIGNED_REP = "u";
	protected static String INTERFACE_REP = "I";
	protected static String REAL_REP = "r";
	protected static String STRING_REP = "s";
	protected static String STRUCT_REP = "S";
	protected static String VOID_REP = "v";
	
	protected static int RELATIVE_UNUSEDTYPE_SIZE = 0;
	protected static int RELATIVE_BYTE_SIZE = 1;
	protected static int RELATIVE_INT_SIZE = 2;
	protected static int RELATIVE_ENUM_SIZE = RELATIVE_INT_SIZE;
	protected static int RELATIVE_BOOL_SIZE = RELATIVE_INT_SIZE;
	protected static int RELATIVE_REAL_SIZE = 4;
	protected static int RELATIVE_POINTER_SIZE = 2;
	

}
   
