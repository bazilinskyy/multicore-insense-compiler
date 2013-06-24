package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.types.AnyType;
import uk.ac.stand.cs.insense.compiler.types.ArrayType;
import uk.ac.stand.cs.insense.compiler.types.BooleanType;
import uk.ac.stand.cs.insense.compiler.types.ByteType;
import uk.ac.stand.cs.insense.compiler.types.CType;
import uk.ac.stand.cs.insense.compiler.types.ChannelType;
import uk.ac.stand.cs.insense.compiler.types.ComponentType;
import uk.ac.stand.cs.insense.compiler.types.EnumType;
import uk.ac.stand.cs.insense.compiler.types.FunctionType;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;
import uk.ac.stand.cs.insense.compiler.types.IntegerType;
import uk.ac.stand.cs.insense.compiler.types.InterfaceType;
import uk.ac.stand.cs.insense.compiler.types.RealType;
import uk.ac.stand.cs.insense.compiler.types.StringType;
import uk.ac.stand.cs.insense.compiler.types.StructType;
import uk.ac.stand.cs.insense.compiler.types.UnknownType;
import uk.ac.stand.cs.insense.compiler.types.UnsignedIntegerType;
import uk.ac.stand.cs.insense.compiler.types.VoidType;
import uk.ac.standrews.cs.nds.util.Diagnostic;

public abstract class Code extends SpaceTracker implements ICode {

	// CONVENTION
	// All names with trailing underscore have a space after the String that they declare

	private static final String[] STUPID = new String[0];
	public static final String FILENAMESTART = "Insense_";
	public static final String FILENAMEHEADERTAIL = ".h";
	public static final String FILENAMEIMPLTAIL = ".c";
	public static final String FILENAMEPROPERTIESTAIL = ".cdf";
	public static final String IFNDEF_ = "#ifndef ";
	public static final String DEFINE_ = "#define ";
	public static final String ENDIF_ = "#endif ";
	public static final String HASH_INCLUDE_ = "#include ";
	public static final String LCB_ = "{ ";
	public static final String RCB_ = "} ";
	public static final String LRB = "(";
	public static final String RRB = ")";
	public static final String LRB_ = "( ";
	public static final String RRB_ = ") ";
	public static final String GENERATED_FROM = "// Generated from: ";
	public static final String IMPL = "impl";
	public static final String C_COMMENT_CLOSE_ = "*/ ";
	public static final String C_COMMENT_OPEN_ = "/* ";
	public static final String SLASHSLASH = "//";
	public static final String UNDERBAR = "_";
	public static final String TAB = "\t";
	public static final String SPACE = " ";
	public static final String DOT = ".";
	public static final String ARROW = "->";
	public static final String EQUALS = "=";
	public static final String EQUALSEQUALS_ = "== ";
	public static final String EQUALS_ = "= ";
	public static final String PLUS_EQUALS_ = "+= ";
	public static final String MINUS_EQUALS_ = "-= ";
	public static final String PLUS_ = "+ ";
	public static final String PLUS_PLUS_ = "++ ";
	public static final String NOT_EQUALS_ = "!= ";
	public static final String LT_ = "< ";
	public static final String LE_ = "<= ";
	public static final String GT_ = "> ";
	public static final String GE_ = ">= ";

	public static final String NOT_ = "! ";
	public static final String DQUOTE = "\"";
	public static final String SQUOTE = "'";
	public static final String SEMI = ";";
	public static final String COLON = ":";
	public static final String COMMA = ",";
	public static final String NEWLINE = "\n";
	public static final String AMPERSAND = "&";
	public static final String STAR = "*";
	public static final String STAR_ = "* ";
	public static final String ANDAND_ = "&& ";
	public static final String OROR_ = "|| ";

	public static final String CHAR_ = "char ";
	public static final String BOOLEAN_ = "bool ";
	public static final String INTEGER_ = "int ";
	public static final String UNSIGNED_INTEGER_ = "unsigned ";
	public static final String FLOAT_ = "float ";
	public static final String STRING_ = "StringPNTR ";
	public static final String BYTE_ = "uint8_t ";
	public static final String UNKNOWN_ = "unknown "; // only used in error cases

	// public static final String CHANNEL_ = "Channel "; // TODO JL remove, not needed for post asynch impl
	public static final String CHANNEL_ = "chan_id ";

	public static final String ANYTYPEPNTR_ = "AnyTypePNTR ";

	public static final String ZERO = "0";
	public static final String NULL = "NULL";
	public static final String NULL_ = NULL + SPACE;
	public static final String VOID_ = "void ";
	public static final String PROCESS = "process";
	public static final String STATIC_ = "static ";
	public static final String BEHAVIOURCAST = "( *behaviour )";
	public static final String BEHAVIOUR_ = "behaviour_";
	public static final String THIS = "this";
	public static final String THIS_ = "this ";
	public static final String STARTHIS_ = "*this ";
	public static final String PNTR_ = "PNTR ";
	public static final String FUNCS = "funcs";
	public static final String FUNCS_ = FUNCS + SPACE;
	public static final String FUNCSSTRUCT_ = "FuncsStruct ";
	public static final String FUNCSPNTR = "FuncsPNTR";
	public static final String VTBL = "VTBL";
	public static final String VTBL_GLOBAL_ = "_vtbl_global ";
	public static final String FUNCSPNTR_ = FUNCSPNTR + SPACE;
	public static final String SSTRUCT_ = "Struct ";
	public static final String END = "end";

	public static final String TRUE_ = "1 ";
	public static final String FALSE_ = "0 ";

	public static final String IF = "if";
	public static final String WHILE = "while";
	public static final String THEN_ = "then ";
	public static final String ELSE_ = "else ";
	public static final String FOR = "for";
	public static final String RETURN_ = "return ";
	public static final String GOTO_ = "goto ";

	public static final String STRUCT_ = "struct ";
	public static final String UNION_ = "union ";
	public static final String ENUM_ = "enum ";
	public static final String EXTERN_ = "extern ";
	public static final String TYPEDEF_ = "typedef ";
	public static final String VOIDSTAR_ = "void* ";
	public static final String INTSTAR_ = "int* ";
	public static final String CONSTRUCT = "Construct";

	protected static final String PROCEND = "end";

	public static final String TAB2 = "\t\t";
	public static final String TAB3 = "\t\t\t";
	public static final String TAB4 = "\t\t\t\t";
	protected static final String PROC_SUFFIX = "proc";

	private static long last_time_in_millis;
	List<String> fragments;

	public Code() {
		super();
		fragments = new ArrayList<String>();
	}

	@Override
	public void append(String s) {
		fragments.add(s);
	}

	@Override
	public String pop() {
		if (fragments.size() > 0) {
			return fragments.remove(fragments.size() - 1);
		} else {
			return "";
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (String s : fragments) {
			sb.append(s);
		}
		return sb.toString();
	}

	@Override
	public void reset(String s) {
		fragments = new ArrayList<String>();
		append(s);
	}

	@Override
	public void complete() {
	}

	// Naming Utility code

	public String constructor_name(String name, int i) {
		return CONSTRUCT + UNDERBAR + name + i;
	}

	public String data_pntr_name(String name) {
		return name + PNTR_;
	}

	public String data_struct_name(String name) {
		return name + SSTRUCT_;
	}

	public String funcs_pntr_name(String name) {
		return name + FUNCSPNTR_;
	}

	public String vtbl_global_name(String name) {
		return name + VTBL_GLOBAL_;
	}

	// TODO NOT NEEDED INCEOS?
	// public String vtbl_decl( String s ) {
	// return funcs_pntr_name( s ) + SPACE + vtbl_global_name( s ) + SEMI;
	// }
	//
	// public String vtbl_constructor_name( String name ) {
	// return CONSTRUCT + UNDERBAR + name + UNDERBAR + VTBL;
	// }

	public String funcs_struct_name(String name) {
		return name + FUNCSSTRUCT_;
	}

	public String funcs_name(String name) {
		return name + FUNCS_;
	}

	public static String header_name(String name) {
		return name.toUpperCase() + "_H_";
	}

	/**
	 * Converts Insense types to C types
	 * 
	 * @param tr
	 *            - the Insense type to be converted
	 * @return the string representation of the equivalent C type
	 */
	public static String insenseTypeToCTypeName(ITypeRep tr) {
		if (tr.equals(BooleanType.TYPE))
			return BOOLEAN_;
		if (tr.equals(IntegerType.TYPE))
			return INTEGER_;
		if (tr.equals(UnsignedIntegerType.TYPE))
			return UNSIGNED_INTEGER_;
		if (tr.equals(RealType.TYPE))
			return FLOAT_;
		if (tr.equals(StringType.TYPE))
			return STRING_;
		if (tr.equals(ByteType.TYPE))
			return BYTE_;
		if (tr instanceof UnknownType)
			return UNKNOWN_;
		if (tr instanceof StructType) {
			// TODO need a construct here as in Enum below
			return structPntrName((StructType) tr);
		}
		if (tr.equals(AnyType.TYPE))
			return ANYTYPEPNTR_;
		if (tr instanceof ChannelType)
			return CHANNEL_;
		if (tr instanceof ArrayType)
			return arrayName((ArrayType) tr);
		if (tr instanceof ComponentType)
			return componentName((ComponentType) tr);
		if (tr instanceof EnumType) {
			EnumDeclaration.generate((EnumType) tr); // forces code gen for includes etc.
			return ((EnumType) tr).getName() + SPACE;
		}
		if (tr instanceof VoidType) {
			// this is only encountered in erroneous programs
			return VOID_;
		}
		if (tr instanceof CType) {
			return tr.toHumanReadableString();
		}
		if (tr instanceof FunctionType)
			return tr.toHumanReadableString();
		if (tr instanceof InterfaceType)
			return VOIDSTAR_;
		// if( tr instanceof FunctionType )
		// return TODO implement function type in insenseTypeToCTypeName
		throw new RuntimeException("don't recognise type: " + tr.getClass().toString());
	}

	public static String cast(ITypeRep tr) {
		return LRB_ + insenseTypeToCTypeName(tr) + RRB_;
	}

	/**
	 * @return a unique constructor name for given type
	 */
	protected static String generate_unique_constructor_name(ITypeRep tr) {
		return "Construct_" + tr.toStringRep();
	}

	/**
	 * @return a unique constructor name for given type
	 */
	protected static String generate_unique_copy_function_name(ITypeRep tr) {
		String copy_func = "";
		if (tr instanceof ChannelType) {
			copy_func = "channel_duplicate";
		} else {
			copy_func = "copy_";
			if (tr instanceof StructType) {
				copy_func += "struct_";
			}
			copy_func += tr.toStringRep();
		}
		return copy_func;
	}

	/**
	 * 
	 * @param thisType
	 * @param data_name
	 * @return the data_name or copied data_name if the type requires copying
	 */

	protected static String potential_copy_function_call(ITypeRep thisType, String data_name) {
		if (requiresCopying(thisType)) {
			return functionCall(generate_unique_copy_function_name(thisType), data_name);
		}
		return data_name;
	}

	/**
	 * @param thisType
	 * @return whether the type requires copying when sent over a channel
	 */
	protected static boolean requiresCopying(ITypeRep thisType) {
		return thisType instanceof StructType || thisType instanceof ArrayType || thisType instanceof ChannelType;
	}

	/**
	 * @return a unique id generated from current time
	 */
	protected String generate_unique_id() {
		long time_in_millis;
		do {
			time_in_millis = System.currentTimeMillis();
		} while (time_in_millis == last_time_in_millis);
		last_time_in_millis = time_in_millis;
		return "uid_" + time_in_millis + SPACE;
	}

	public static void potentiallyGenerateGenerators(ITypeRep type) {
		if (type instanceof ArrayType) {
			ArrayType array_type = (ArrayType) type;
			if (!ArrayConstructor.generatedConstructorAlready(array_type)) {
				ArrayConstructor.generate_generator_function(array_type);
			}
			if (!ArrayConstructor.generatedCopyfunctionAlready(array_type)) {
				ArrayConstructor.generate_copy_function(array_type);
			}
		}
	}

	/**
	 * Strips a name back to a basename - i.e. takes a->b and gives b
	 * 
	 * @param s
	 *            the string to be stripped
	 * @return the stripped string
	 */
	protected static String baseName(String s) {
		int position = s.lastIndexOf("->");
		if (position == -1) { // not found
			return s;
		} else {
			if (s.length() < position + 2) { // no tail after the ->
				throw new RuntimeException("no -> found in basename: " + s);
			} else {
				return s.substring(position + 2);
			}
		}
	}

	protected static String componentName(ComponentType type) {
		return type.getName() + PNTR_;
	}

	protected static String arrayName(ArrayType tr) {
		// return STAR + insenseTypeToCTypeName( tr.getArray_type() );
		return "IArrayPNTR ";
	}

	protected static String structPntrName(StructType type) {
		return type.getName() + UNDERBAR + PNTR_;
	}

	// protected static String channelIncludeName( ITypeRep type ) {
	// if( type instanceof ChannelType ) {
	// // JL we now have single HalfChannel type rather than IntChannel BoolChannel etc.
	// return HALFCHANNEL_INCLUDE;
	// } else {
	// throw new RuntimeException("unrecognised type:" + type);
	// }
	// }

	protected static String channelConstructorName(ITypeRep type) {
		if (type instanceof ChannelType) {
			String direction = "";
			ChannelType ctype = (ChannelType) type;
			if (ctype.getDirection() == ChannelType.OUT) {
				direction = "CHAN_OUT";
			} else {
				direction = "CHAN_IN";
			}
			ITypeRep payload_type = ctype.getChannel_type();
			String type_size = functionCall("sizeof", insenseTypeToCTypeName(payload_type));
			String contains_pointers = payload_type.isPointerType() ? "true" : "false";
			// return functionCall( "channel_create", direction ); // TODO JL remove, not needed for post asynch impl
			return functionCall("channel_create", direction, type_size, contains_pointers);
		} else {
			throw new RuntimeException("Didn't find expected channel type, found:" + type);
		}
	}

	// Generative methods

	/**
	 * Returns the parameter signaure list for function
	 * 
	 * @param ft
	 *            - the function whose parameter list is being generated
	 */
	public String paramSignatures(FunctionType ft, List<String> names) {
		StringBuffer sb = new StringBuffer();
		sb.append(LRB_);
		int i = 0;
		for (ITypeRep param : ft.getArgs()) {
			if (i != 0)
				sb.append(COMMA);
			sb.append(insenseTypeToCTypeName(param) + names.get(i));
			i++;
		}
		sb.append(SPACE + RRB_);
		return sb.toString();
	}

	protected String genMallocSizeofAssign(String type, String lhs, String typename, boolean contains_pointers) {
		return genMallocAssign(type, lhs, functionCall("sizeof", typename), contains_pointers);
	}

	protected String genMallocSizeofAssign(String type, String lhs, String typename) {
		return genMallocAssign(type, lhs, functionCall("sizeof", typename), false);
	}

	protected static String genMallocAssign(String type, String lhs, String size, boolean contains_pointers) {
		StringBuffer sb = new StringBuffer();
		sb.append(GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE);
		sb.append(TAB + type + SPACE + lhs + EQUALS + LRB_ + type + RRB_);
		sb.append(functionCall("DAL_alloc", size, (contains_pointers ? "true" : "false")) + SEMI + NEWLINE);
		sb.append(TAB + IF + LRB_ + lhs + EQUALSEQUALS_ + NULL_ + RRB_ + LCB_ + NEWLINE);
		// TODO find better way to define the actual file_name
		sb.append(TAB + TAB + "char * file_name = \"file\";" + NEWLINE); // Added this line to avoid file_name not found errors.
		sb.append(TAB + TAB + "DAL_error(OUT_OF_MEMORY_ERROR);" + NEWLINE);
		sb.append(TAB + TAB + RETURN_ + NULL + SEMI + NEWLINE);
		sb.append(TAB + RCB_ + NEWLINE);
		return sb.toString();
	}

	// print methods

	public static String functionCall(String functionName, List<String> params) {
		return functionCall(functionName, params.toArray(STUPID));
	}

	public static String functionCall(String functionName, String... params) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (String s : params) {
			if (first) {
				sb.append(s);
				first = false;
			} else {
				sb.append(COMMA);
				sb.append(s);
			}
		}
		return functionName + LRB_ + sb + SPACE + RRB_;
	}

	protected static String tab(int n) {
		switch (n) {
			case 0:
				return "";
			case 1:
				return TAB;
			case 2:
				return TAB2;
			case 3:
				return TAB3;
			case 4:
				return TAB4;
			default: {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < n; i++) {
					sb.append(TAB);
				}
				return sb.toString();
			}
		}
	}

	protected void printMalloc(PrintStream ps, String type, String lhs, String rhs, boolean contains_pointers) {
		ps.print(genMallocSizeofAssign(type, lhs, rhs, contains_pointers));
	}

	protected void printMalloc(PrintStream ps, String type, String lhs, String rhs) {
		printMalloc(ps, type, lhs, rhs, false);
	}

	/**
	 * Method to read in an entire file containing C code for in-line send and receive
	 * This was used for in-line replication of send and receive C code,
	 * probably will not needed in future as we now use threaded
	 * calls to send and receive functions defined in the runtime.
	 * Method is commented out for now.
	 * 
	 * @param filename
	 *            the filename to read
	 * @return The content of the file as a String
	 */
	/*
	 * protected String readCodeFile(String filename){
	 * String result = null;
	 * File inf = new File(filename);
	 * try{
	 * int no_chars = (int) inf.length();
	 * char[] cbuf = new char[no_chars];
	 * FileReader in = new FileReader(inf);
	 * in.read(cbuf);
	 * in.close();
	 * result = new String(cbuf);
	 * }
	 * catch(FileNotFoundException e){
	 * System.err.println(e.getMessage());
	 * }
	 * catch(IOException e){
	 * System.err.println(e.getMessage());
	 * }
	 * return result;
	 * }
	 */

}