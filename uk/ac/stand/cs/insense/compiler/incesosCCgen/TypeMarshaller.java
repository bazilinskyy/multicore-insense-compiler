package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling.HeaderFile;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling.ImplFile;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling.OutputFile;
import uk.ac.stand.cs.insense.compiler.types.AnyType;
import uk.ac.stand.cs.insense.compiler.types.ArrayType;
import uk.ac.stand.cs.insense.compiler.types.BooleanType;
import uk.ac.stand.cs.insense.compiler.types.ByteType;
import uk.ac.stand.cs.insense.compiler.types.ChannelType;
import uk.ac.stand.cs.insense.compiler.types.EnumType;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;
import uk.ac.stand.cs.insense.compiler.types.IntegerType;
import uk.ac.stand.cs.insense.compiler.types.InterfaceType;
import uk.ac.stand.cs.insense.compiler.types.RealType;
import uk.ac.stand.cs.insense.compiler.types.StringType;
import uk.ac.stand.cs.insense.compiler.types.StructType;
import uk.ac.stand.cs.insense.compiler.types.UnsignedIntegerType;
import uk.ac.standrews.cs.nds.util.Diagnostic;
import uk.ac.standrews.cs.nds.util.ErrorHandling;

/**
 * @author al
 *         This code is responsible for generating marshalling and unmarshalling
 *         code for transmission on radio
 */
public class TypeMarshaller extends Code {

	// private static final String RADIO_CHECK_START = "\n#if defined(DALRADIO) || defined (DALINTERNODECHANNEL)\n";
	// private static final String RADIO_CHECK_END = "\n#endif /* DALRADIO */\n";
	private static final String MARSHALLER_H_ = "MARSHALLER_H_";
	// private static final String MEMCPY = "strncpy";
	private static final String MEMCPY = "memncpy";
	private static final String CHARSTARCAST = "(char *)";
	private static List<ITypeRep> encounteredTypesRequiringSerialization = new ArrayList<ITypeRep>();
	private static List<ITypeRep> encounteredTypesRequiringDeSerialization = new ArrayList<ITypeRep>();
	private static List<String> encounteredTypeStringsRequiringSerialization = new ArrayList<String>();
	private static List<String> encounteredTypeStringsRequiringDeSerialization = new ArrayList<String>();

	private static ArrayList<String> declaredLengthVars;

	private static String PARAMNAME = "p";
	private static String initializeSerializerFunctions = "initializeSerializerFunctions";
	private static String serialiserMap = "serialiserMap";
	private static String Construct_FunctionPair = "Construct_FunctionPair";
	private static String mem_block_name = "pntr";
	private static String result = "result";
	private static String used = "used";
	private static String MAX_SIZE = "128"; // maximum size of Rime payload

	private static StringBuffer externalIncludes = new StringBuffer();

	static int counter = 0; // for generating unique variables
	static int loop_counter = 0; // for generating unique variables
	private static String length_var_name = "length";
	private static int length_var_counter = 0;

	public TypeMarshaller() {
	}

	/**
	 * This is the entry point for adding serializers
	 * 
	 * @param type
	 *            - the type for which a serializer must be generated
	 */
	public void addSerializer(ITypeRep type) {
		if (!encounteredSerializerAlready(type)) {
			encounteredTypesRequiringSerialization.add(type);
			encounteredTypeStringsRequiringSerialization.add(type.toStringRep());
			if (!encounteredDeSerializerAlready(type)) { // add include (unless already included for de-serialiser
				if (type instanceof ArrayType) {
					ITypeRep base_type = ((ArrayType) type).getBaseType();
					if (base_type instanceof StructType) {
						externalIncludes.append("#include " + DQUOTE + StructDeclaration.struct_name(((StructType) base_type)) + ".h" + DQUOTE + NEWLINE);
					}
					if (base_type instanceof EnumType) {
						externalIncludes.append("#include " + DQUOTE + ((EnumType) type).getName() + ".h" + DQUOTE + NEWLINE);
					}
				}
			}
		}
	}

	/**
	 * This is the entry point for adding deserializers
	 * 
	 * @param type
	 *            - the type for which a deserializer must be generated
	 */
	public void addDeSerializer(ITypeRep type) {
		if (!encounteredDeSerializerAlready(type)) {
			encounteredTypesRequiringDeSerialization.add(type);
			encounteredTypeStringsRequiringDeSerialization.add(type.toStringRep());
			if (!encounteredSerializerAlready(type)) { // add include (unless already included for serialiser
				if (type instanceof ArrayType) {
					ITypeRep base_type = ((ArrayType) type).getBaseType();
					if (base_type instanceof StructType) {
						externalIncludes.append("#include " + DQUOTE + StructDeclaration.struct_name(((StructType) base_type)) + ".h" + DQUOTE + NEWLINE);
					}
					if (base_type instanceof EnumType) {
						externalIncludes.append("#include " + DQUOTE + ((EnumType) type).getName() + ".h" + DQUOTE + NEWLINE);
					}
				}
			}
		}
	}

	/**
	 * This is the entry point for the generation process itself
	 */
	public static void generateSerializersAndDeserializers() {
		generateSerializerFiles();
	}

	/********************************** Generative methods **********************************/

	private static String externalIncludes() {
		return externalIncludes.toString();
	}

	private static void check_space(StringBuffer sb, String extra) {
		sb.append(TAB + used + " += " + extra + SEMI + NEWLINE);
		sb.append(TAB + IF + LRB + used + SPACE + GT_ + MAX_SIZE + RRB + LCB_ + NEWLINE);
		sb.append(TAB + TAB + functionCall("DAL_error", "SERIALISATION_ERROR") + SEMI + NEWLINE);
		sb.append(TAB + TAB + "return NULL" + SEMI + NEWLINE);
		sb.append(TAB + RCB_ + NEWLINE);
	}

	private static String serializeValue(ITypeRep t, String from, boolean increment_pntr, boolean in_array, boolean in_struct) {
		// scalars
		StringBuffer sb = new StringBuffer();
		String memcpy_scalar_from_deref = CHARSTARCAST;
		if (in_array || in_struct) {
			memcpy_scalar_from_deref += AMPERSAND;
		}
		if (t instanceof BooleanType || t instanceof IntegerType || t instanceof UnsignedIntegerType || t instanceof EnumType) {
			check_space(sb, functionCall("sizeof", "int"));
			sb.append(TAB + functionCall(MEMCPY, CHARSTARCAST + mem_block_name, memcpy_scalar_from_deref + from, functionCall("sizeof", "int")) + SEMI
					+ NEWLINE);
			if (increment_pntr) {
				sb.append(TAB + mem_block_name + SPACE + EQUALS_ + LRB + CHARSTARCAST + mem_block_name + RRB + "+" + functionCall("sizeof", "int") + SEMI + TAB
						+ NEWLINE);
			}
		} else if (t instanceof ByteType) {
			check_space(sb, functionCall("sizeof", "char"));
			sb.append(TAB + functionCall(MEMCPY, CHARSTARCAST + mem_block_name, memcpy_scalar_from_deref + from, functionCall("sizeof", "char")) + SEMI
					+ NEWLINE);
			if (increment_pntr) {
				sb.append(TAB + mem_block_name + SPACE + EQUALS_ + LRB + CHARSTARCAST + mem_block_name + RRB + "+" + functionCall("sizeof", "char") + SEMI
						+ TAB + NEWLINE);
			}
		} else if (t instanceof RealType) {
			check_space(sb, functionCall("sizeof", "float"));
			sb.append(TAB + functionCall(MEMCPY, CHARSTARCAST + mem_block_name, memcpy_scalar_from_deref + from, functionCall("sizeof", "float")) + SEMI
					+ NEWLINE);
			if (increment_pntr) {
				sb.append(TAB + mem_block_name + SPACE + EQUALS_ + LRB + CHARSTARCAST + mem_block_name + RRB + "+" + functionCall("sizeof", "float") + SEMI
						+ TAB + NEWLINE);
			}
		} else
		// pointers
		if (t instanceof StringType) {
			check_space(sb, from + ARROW + "length + 1");
			sb.append(TAB + functionCall(MEMCPY, CHARSTARCAST + mem_block_name, from + ARROW + "data", from + ARROW + "length + 1") + SEMI + NEWLINE);
			if (increment_pntr) {
				sb.append(TAB + mem_block_name + SPACE + EQUALS_ + LRB + CHARSTARCAST + mem_block_name + RRB + "+" + from + ARROW + "length + 1" + SEMI + TAB
						+ NEWLINE);
			}

		} else if (t instanceof StructType) {
			structFieldSerialize(sb, (StructType) t, from);
		} else if (t instanceof ArrayType) {
			if (!in_array) {
				sb.append(serializeArrayLengths((ArrayType) t, from));
			}
			arraySerialize(sb, (ArrayType) t, from);
		} else if (t instanceof AnyType) { // TODO JL if we want to continue allowing ANYs in STRUCTs then we will have to add size field to all deserialisers
											// to permit the start of the next struct field to find in the serial data
			sb.append(TAB + "pntr" + EQUALS_ + functionCall("useRTSerialiseAnyType", "(AnyTypePNTR) " + from, "&used", "pntr") + SEMI + NEWLINE);
		} else if (t instanceof ChannelType) {
			sb.append("\n// Not implemented:" + t + "\n");
			// ErrorHandling.error( "Not implemented" );
			// TODO JL fix me: do we need channel over radio?
		} else if (t instanceof InterfaceType) {
			// ErrorHandling.error( "Not implemented" );
			// TODO JL fix me: do we need component/interface over radio?
		} else {
			// ErrorHandling.hardError( "Not implemented" );
		}

		// TODO finish this..... (see above)

		return sb.toString();
	}

	/**
	 * Serialize an array into a buffer for radio transmission
	 */
	private static String arraySerialize(StringBuffer sb, ArrayType t, String from) {
		ITypeRep tr = t.getArray_type();
		sb.append(TAB + "unsigned " + current_loop_var() + SEMI + NEWLINE);
		sb.append(TAB + FOR + SPACE + LRB + current_loop_var() + EQUALS + ZERO + SEMI + SPACE + current_loop_var() + "<" + from + ARROW + "length" + SEMI
				+ SPACE + current_loop_var() + PLUS_PLUS_ + RRB + LCB_ + NEWLINE);
		sb.append(serializeValue(tr, LRB + functionCall(ArrayOps.array_deref_function(tr), from, next_loop_var(), SPACE + AMPERSAND + "success") + RRB, true,
				true, false));
		sb.append(TAB + RCB_ + NEWLINE);
		return sb.toString();
	}

	/**
	 * Serialize the fields of a struct into a buffer for radio transmission
	 */
	private static String structFieldSerialize(StringBuffer sb, StructType t, String from) {
		int index = 0;
		for (ITypeRep tr : t.getFields()) {
			String fname = t.getFieldNames().get(index++);
			sb.append(serializeValue(tr, from + ARROW + fname, true, false, true));
		}
		return sb.toString();
	}

	private static String deserializeValue(ITypeRep t, String to, String from, boolean increment_pntr, boolean in_array) {
		// scalars
		StringBuffer sb = new StringBuffer();
		if (t instanceof BooleanType || t instanceof IntegerType || t instanceof UnsignedIntegerType || t instanceof EnumType) {
			if (!in_array)
				sb.append(TAB + insenseTypeToCTypeName(t) + to + SEMI + NEWLINE);
			sb.append(TAB + functionCall(MEMCPY, CHARSTARCAST + AMPERSAND + to, CHARSTARCAST + from, functionCall("sizeof", "int")) + SEMI + NEWLINE);
			if (increment_pntr) {
				sb.append(TAB + PARAMNAME + SPACE + EQUALS + LRB + CHARSTARCAST + PARAMNAME + RRB + "+" + functionCall("sizeof", "int") + SEMI + NEWLINE);
			}
		} else if (t instanceof ByteType) {
			if (!in_array)
				sb.append(TAB + insenseTypeToCTypeName(t) + to + SEMI + NEWLINE);
			sb.append(TAB + functionCall(MEMCPY, CHARSTARCAST + AMPERSAND + to, CHARSTARCAST + from, functionCall("sizeof", "char")) + SEMI + NEWLINE);
			if (increment_pntr) {
				sb.append(TAB + PARAMNAME + SPACE + EQUALS + LRB + CHARSTARCAST + PARAMNAME + RRB + "+" + functionCall("sizeof", "char") + SEMI + NEWLINE);
			}
		} else if (t instanceof RealType) {
			if (!in_array)
				sb.append(TAB + insenseTypeToCTypeName(t) + to + SEMI + NEWLINE);
			sb.append(TAB + functionCall(MEMCPY, CHARSTARCAST + AMPERSAND + to, CHARSTARCAST + from, functionCall("sizeof", "float")) + SEMI + NEWLINE);
			if (increment_pntr) {
				sb.append(TAB + PARAMNAME + SPACE + EQUALS + LRB + CHARSTARCAST + PARAMNAME + RRB + "+" + functionCall("sizeof", "float") + SEMI + NEWLINE);
			}
		} else
		// pointers
		if (t instanceof StringType) {
			if (!in_array)
				sb.append(TAB + insenseTypeToCTypeName(t) + to + SEMI + NEWLINE);
			if (to.contains("array_loc")) {
				sb.append(TAB + functionCall("DAL_assign", AMPERSAND + to, SPACE + functionCall("Construct_String0", from)) + SEMI + NEWLINE);
			} else {
				sb.append(TAB + to + EQUALS + functionCall("Construct_String1", from) + SEMI + NEWLINE);
			}
			if (increment_pntr) {
				sb.append(TAB + PARAMNAME + SPACE + EQUALS + LRB + CHARSTARCAST + PARAMNAME + RRB + "+" + functionCall("strlen", from) + PLUS_ + "1" + SEMI
						+ NEWLINE);
			}
		} else if (t instanceof StructType) {
			if (!in_array)
				sb.append(TAB + insenseTypeToCTypeName(t) + to + SEMI + NEWLINE);
			int count = get_count();
			structFieldDeSerialize(sb, (StructType) t, from);
			if (to.contains("array_loc")) {
				sb.append(TAB
						+ functionCall("DAL_assign", AMPERSAND + to,
								SPACE + functionCall(StructDeclaration.constructor_name((StructType) t), structActualParams(count, (StructType) t))) + SEMI
						+ NEWLINE);
			} else {
				sb.append(TAB + to + SPACE + EQUALS_
						+ functionCall(StructDeclaration.constructor_name((StructType) t), structActualParams(count, (StructType) t)) + SEMI + NEWLINE);
			}

		} else if (t instanceof ArrayType) {
			if (!in_array) {
				sb.append(deSerializeArrayLengths((ArrayType) t));
				sb.append(TAB + insenseTypeToCTypeName(t) + to + SPACE + EQUALS);
				sb.append(functionCall(generate_unique_constructor_name(t), arrayLengths((ArrayType) t), NULL) + SEMI + NEWLINE);
			}
			arrayDeSerialize(sb, (ArrayType) t, to, from);
		} else if (t instanceof AnyType) { // TODO JL if we want to continue allowing ANYs in STRUCTs then we will have to add size field to all deserialisers
											// to permit the start of the next struct field to find in the serial data
			if (to.contains("array_loc")) {
				sb.append(TAB + functionCall("DAL_assign", AMPERSAND + to, SPACE + functionCall("useRTDeserialiseToAnyType", from)) + SEMI + NEWLINE);
			} else {
				sb.append(TAB + insenseTypeToCTypeName(t) + to + SEMI + NEWLINE);
				sb.append(TAB + to + SPACE + EQUALS_ + functionCall("useRTDeserialiseToAnyType", from) + SEMI + NEWLINE);
			}
		} else if (t instanceof ChannelType) {
			// ErrorHandling.error( "Not implemented" + t);
			// TODO JL do we need channels over radio?
		} else if (t instanceof InterfaceType) {
			// ErrorHandling.error( "Not implemented" + t );
			// TODO JL do we need interface over radio
		} else {
			// ErrorHandling.hardError( "Not implemented" + t );
		}

		// TODO finish this..... (see above)

		return sb.toString();
	}

	static private String current_var() {
		return "var" + counter;
	}

	static private String next_var() {
		return "var" + counter++;
	}

	static private String current_loop_var() {
		return "count" + loop_counter;
	}

	static private String next_loop_var() {
		return "count" + loop_counter++;
	}

	static private int get_count() {
		return counter;
	}

	private static String structActualParams(int count, StructType t) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < t.getFields().size() - 1; i++) {
			sb.append("var" + count++ + COMMA);
		}
		if (t.getFields().size() > 0) {	// need the last var without a COMMA
			sb.append("var" + count++);
		}
		return sb.toString();
	}

	private static String arrayLengths(ArrayType t) {
		StringBuffer sb = new StringBuffer();
		int dims = t.getDimensionality();
		int i;
		for (i = 0; i < dims - 1; i++) {
			sb.append(" length" + i + COMMA);
		}
		sb.append(" length" + i);
		return sb.toString();
	}

	/**
	 * Serialize the fields of a struct into a buffer for radio transmission
	 */
	private static String structFieldDeSerialize(StringBuffer sb, StructType t, String from) {
		int index = 0;
		for (ITypeRep tr : t.getFields()) {
			String fname = t.getFieldNames().get(index++);
			sb.append(deserializeValue(tr, next_var(), from, true, false));
		}
		return sb.toString();
	}

	/**
	 * Deserialize an array into a buffer for radio transmission
	 */
	private static String arrayDeSerialize(StringBuffer sb, ArrayType t, String to, String from) {
		ITypeRep tr = t.getArray_type();
		sb.append(TAB + "unsigned " + current_loop_var() + SEMI + NEWLINE);
		sb.append(TAB + FOR + SPACE + LRB + current_loop_var() + EQUALS + ZERO + SEMI + SPACE + current_loop_var() + "<" + "length" + length_var_counter++
				+ SEMI + SPACE + current_loop_var() + PLUS_PLUS_ + RRB + LCB_ + NEWLINE);
		sb.append(deserializeValue(tr, LRB + functionCall(ArrayOps.array_deref_function(tr), to, next_loop_var(), SPACE + AMPERSAND + "success") + RRB, from,
				true, true));
		sb.append(TAB + RCB_ + NEWLINE);
		length_var_counter--;
		return sb.toString();
	}

	private static String serializerPrototype(ITypeRep t) {
		String param = insenseTypeToCTypeName(t);
		if (!t.isPointerType())
			param += STAR;
		param += PARAMNAME;
		return VOIDSTAR_ + serializerName(t) + LRB_ + param + COMMA + INTSTAR_ + "size" + SPACE + RRB_;
	}

	private static String deserializerPrototype(ITypeRep t) {
		return VOIDSTAR_ + deserializerName(t) + LRB_ + VOIDSTAR_ + PARAMNAME + SPACE + RRB_;
	}

	private static String initializeSerializerFunctionsPrototype() {
		return VOID_ + initializeSerializerFunctions + LRB + RRB;
	}

	private static String serializerName(ITypeRep t) {
		return "serialize_" + t.toStringRep();
	}

	private static String deserializerName(ITypeRep t) {
		return "deserialize_" + t.toStringRep();
	}

	private static String generateSerializerFileName() {
		return "marshaller.c";
	}

	private static String generateSerializerHeaderFileName() {
		return "marshaller.h";
	}

	private static String constructAny(ITypeRep t) {
		if (t instanceof BooleanType)
			return "Construct_BoolAnyType0";
		if (t instanceof IntegerType || t instanceof EnumType)
			return "Construct_IntAnyType0";
		if (t instanceof UnsignedIntegerType)
			return "Construct_UnsignedIntAnyType0";
		if (t instanceof RealType)
			return "Construct_RealAnyType0";
		if (t instanceof ByteType)
			return "Construct_ByteAnyType0";
		else
			return "Construct_PointerAnyType0";
	}

	/************************ Printing Methods ************************/

	private static void printIncludes(PrintStream out) {
		out.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		out.println("#include \"InsenseRuntime.h\"");
		out.println("#include \"FunctionPair.h\"");
		// out.println( "#include \"BSTMap.h\"" );
		// out.println("#include \"GlobalObjects.h\"");
		out.println("#include \"cstring.h\"");
		out.println("#include \"marshaller.h\"");
		out.println(externalIncludes());
		out.println();
	}

	private static void printLocalStatics(PrintStream out) {
		out.println("#ifndef DALSMALL");
		out.println("static char *file_name = \"marshaller\"; // used by DAL_error macro");
		out.println("#endif");
	}

	private static void printHeaderStart(PrintStream out) {
		out.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		// out.println ( RADIO_CHECK_START );
		out.println("#ifndef " + MARSHALLER_H_);
		out.println("#define " + MARSHALLER_H_);
		out.println();
		out.println("#include \"String.h\"");
	}

	private static void printHeaderIncludes(PrintStream out) {
		for (ITypeRep t : encounteredTypesRequiringSerialization) {
			printInclude(t, out);
		}
		for (ITypeRep t : encounteredTypesRequiringDeSerialization) {
			if (!encounteredTypesRequiringSerialization.contains(t)) {
				printInclude(t, out);
			}
		}
		out.println();
	}

	private static void printInclude(ITypeRep t, PrintStream out) {
		if (t instanceof StructType)
			out.print("#include " + DQUOTE + "struct_" + t.toStringRep() + ".h" + DQUOTE + NEWLINE);
		if (t instanceof EnumType)
			out.print("#include " + DQUOTE + ((EnumType) t).getName() + ".h" + DQUOTE + NEWLINE);

	}

	private static void printHeaderEnd(PrintStream out) {
		out.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		out.println("#endif" + TAB + "// MARSHALLER_H_");
		// out.println( RADIO_CHECK_END );
		out.println();
	}

	private static void printSerializers(PrintStream out) {
		for (ITypeRep t : encounteredTypesRequiringSerialization) {
			printSerializer(t, out);
		}
	}

	private static String lengthVarName(int i) {
		return length_var_name + i;
	}

	private static String serializeArrayLengths(ArrayType t, String from) {
		StringBuffer sb = new StringBuffer();
		String size = "sizeof(int)";
		String array = from;
		for (int i = 0; i < t.getDimensionality(); i++) {
			check_space(sb, size);
			String array_length = array + ARROW + "length";
			sb.append(TAB);
			if (!declaredLengthVars.contains(lengthVarName(i))) {
				declaredLengthVars.add(lengthVarName(i));
				sb.append("unsigned ");
			}
			sb.append(lengthVarName(i) + SPACE + EQUALS_ + array_length + SEMI + NEWLINE);
			sb.append(TAB + functionCall(MEMCPY, mem_block_name, LRB + CHARSTARCAST + AMPERSAND + lengthVarName(i) + RRB, size) + SEMI + NEWLINE);
			sb.append(TAB + mem_block_name + SPACE + EQUALS + LRB + CHARSTARCAST + mem_block_name + RRB + "+" + size + SEMI + TAB + NEWLINE);
			array = LRB + functionCall(ArrayOps.array_deref_function(t), array, "0", SPACE + AMPERSAND + "success") + RRB;
		}
		return sb.toString();
	}

	private static String deSerializeArrayLengths(ArrayType t) {
		StringBuffer sb = new StringBuffer();
		String size = "sizeof(int)";
		for (int i = 0; i < t.getDimensionality(); i++) {
			if (!declaredLengthVars.contains(lengthVarName(i))) {
				declaredLengthVars.add(lengthVarName(i));
				sb.append(TAB + "unsigned " + lengthVarName(i) + SEMI + NEWLINE);
			}
			sb.append(TAB + functionCall(MEMCPY, CHARSTARCAST + AMPERSAND + lengthVarName(i), SPACE + CHARSTARCAST + PARAMNAME, " sizeof(int)") + SEMI
					+ NEWLINE);
			sb.append(TAB + PARAMNAME + SPACE + EQUALS + LRB + CHARSTARCAST + PARAMNAME + RRB + "+sizeof(int)" + SEMI + TAB + NEWLINE);
		}
		return sb.toString();
	}

	private static void printSerializer(ITypeRep t, PrintStream out) {
		declaredLengthVars = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();
		sb.append(GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE);
		sb.append(serializerPrototype(t) + SPACE + LCB_ + NEWLINE);

		sb.append(genMallocAssign(VOIDSTAR_, mem_block_name, MAX_SIZE, false));
		sb.append(TAB + VOIDSTAR_ + result + EQUALS + mem_block_name + SEMI + NEWLINE);
		sb.append(TAB + INTEGER_ + used + EQUALS_ + ZERO + SEMI + NEWLINE);
		// copy the type trademark first
		String tm_size = Integer.toString(t.toStringRep().length() + 1);
		check_space(sb, tm_size);
		sb.append(TAB + functionCall(MEMCPY, mem_block_name, DQUOTE + t.toStringRep() + DQUOTE, tm_size) + SEMI + NEWLINE);
		sb.append(TAB + mem_block_name + SPACE + EQUALS + LRB + CHARSTARCAST + mem_block_name + RRB + "+" + tm_size + SEMI + TAB + NEWLINE);
		sb.append(serializeValue(t, PARAMNAME, false, false, false));
		sb.append(TAB + STAR + "size" + EQUALS + used + SEMI + NEWLINE);
		sb.append(TAB + RETURN_ + result + SEMI + NEWLINE);
		sb.append(RCB_ + NEWLINE);
		sb.append(NEWLINE);
		out.print(sb.toString());
	}

	private static void printDeSerializers(PrintStream out) {
		for (ITypeRep t : encounteredTypesRequiringDeSerialization) {
			printDeSerializer(t, out);
		}
	}

	private static void printDeSerializer(ITypeRep t, PrintStream out) {
		// TODO Auto-generated method stub
		declaredLengthVars = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();
		sb.append(GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE);
		sb.append(deserializerPrototype(t) + SPACE + LCB_ + NEWLINE);
		sb.append(TAB + "void *result;" + NEWLINE);

		if (t instanceof BooleanType ||	// all the scalars
				t instanceof IntegerType || t instanceof UnsignedIntegerType || t instanceof EnumType || t instanceof ByteType || t instanceof RealType) {
			// sb.append( TAB + insenseTypeToCTypeName(t) + "value = 0;" + NEWLINE );
			sb.append(deserializeValue(t, "value", PARAMNAME, false, false)); // type, to , from
			sb.append(TAB + "result = " + functionCall(constructAny(t), "value", DQUOTE + t.toStringRep() + DQUOTE) + SEMI + NEWLINE);
		} else if (t instanceof StringType) {	// Strings
			// sb.append( TAB + insenseTypeToCTypeName(t) + "value = 0;" + NEWLINE );
			sb.append(deserializeValue(t, "value", PARAMNAME, false, false)); // type, to , from
			sb.append(TAB + "result = " + functionCall(constructAny(t), "value", DQUOTE + t.toStringRep() + DQUOTE) + SEMI + NEWLINE);
		} else if (t instanceof StructType) {
			// sb.append( TAB + insenseTypeToCTypeName(t) + "value = 0;" + NEWLINE );
			sb.append(deserializeValue(t, "value", PARAMNAME, false, false)); // type, to , from
			sb.append(TAB + "result = " + functionCall(constructAny(t), "value", DQUOTE + t.toStringRep() + DQUOTE) + SEMI + NEWLINE);
		} else if (t instanceof ArrayType) {
			sb.append(deserializeValue(t, "value", PARAMNAME, false, false)); // type, to , from
			sb.append(TAB + "result = " + functionCall(constructAny(t), "value", DQUOTE + t.toStringRep() + DQUOTE) + SEMI + NEWLINE);

		} else if (t instanceof ChannelType) {
			sb.append("\n// Not implemented:" + t + "\n");
			// sb.append( deserializeValue( t, "value", PARAMNAME, false, false ) ); // type, to , from
			// sb.append( TAB + "result = " + functionCall( constructAny( t ), "value", DQUOTE + t.toStringRep() + DQUOTE ) + SEMI + NEWLINE );

		} else {
			ErrorHandling.hardError("Not implemented:" + t);

			// TODO finish this..... (see above)
		}

		sb.append(TAB + RETURN_ + result + SEMI + NEWLINE);
		sb.append(RCB_ + NEWLINE);
		sb.append(NEWLINE);
		out.print(sb.toString());
	}

	private static void printSerializerPrototypes(PrintStream out) {
		for (ITypeRep t : encounteredTypesRequiringSerialization) {
			printSerializerPrototype(t, out);
		}
	}

	private static void printSerializerPrototype(ITypeRep t, PrintStream out) {
		StringBuffer sb = new StringBuffer();
		sb.append(GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE);
		sb.append(EXTERN_ + serializerPrototype(t) + SEMI + NEWLINE);
		out.print(sb.toString());
	}

	private static void printDeSerializerPrototypes(PrintStream out) {
		for (ITypeRep t : encounteredTypesRequiringDeSerialization) {
			printDeSerializerPrototype(t, out);
		}
	}

	private static void printDeSerializerPrototype(ITypeRep t, PrintStream out) {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer();
		sb.append(GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE);
		sb.append(EXTERN_ + deserializerPrototype(t) + SPACE + SEMI + NEWLINE);
		out.print(sb.toString());
	}

	private static void printInitialiseFunctionPrototype(PrintStream out) {
		out.println();
		out.println(EXTERN_ + initializeSerializerFunctionsPrototype() + SEMI);
		out.println();
	}

	private static void printInitializeSerializerFunctions(PrintStream out) {
		StringBuffer sb = new StringBuffer();
		sb.append(GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE);
		sb.append(initializeSerializerFunctionsPrototype() + SPACE + LCB_ + NEWLINE);
		List<String> deSerializerGenerated = new ArrayList<String>();
		// iterate through all the serializers adding deserializers if there are any
		for (ITypeRep t : encounteredTypesRequiringSerialization) {

			if (encounteredTypeStringsRequiringDeSerialization.contains(t.toStringRep())) { 	// we have a pair to put in the table
				sb.append(TAB + functionCall("mapPut", serialiserMap,  						// the map
						DQUOTE + t.toStringRep() + DQUOTE,	// the key
						functionCall(Construct_FunctionPair, "(serialf_t)" + serializerName(t), "(deserialf_t)" + deserializerName(t)) // the fns
						) + SEMI + NEWLINE);
				deSerializerGenerated.add(t.toStringRep());
			} else {	// only the serializer to put in table
				sb.append(TAB + functionCall("mapPut", serialiserMap,  						// the map
						DQUOTE + t.toStringRep() + DQUOTE,	// the key
						functionCall(Construct_FunctionPair, "(serialf_t)" + serializerName(t), NULL) // the fns
						) + SEMI + NEWLINE);
			}
		}
		// iterate through all the deserializers and add them if not added already
		for (ITypeRep t : encounteredTypesRequiringDeSerialization) {
			if (!deSerializerGenerated.contains(t.toStringRep())) { 	// we have a pair to put in the table
				sb.append(TAB + functionCall("mapPut", serialiserMap, 					 	// the map
						DQUOTE + t.toStringRep() + DQUOTE,	// the key
						functionCall(Construct_FunctionPair, NULL, "(deserialf_t)" + deserializerName(t)) // the fns
						) + SEMI + NEWLINE);
			}
		}
		sb.append(RCB_ + NEWLINE);
		sb.append(NEWLINE);
		out.print(sb.toString());
	}

	/************************ File related Methods ************************/

	private static void generateSerializerFiles() {
		try {
			OutputFile f1 = new ImplFile(generateSerializerFileName());
			PrintStream ps1 = f1.getStream();
			// ps1.println(RADIO_CHECK_START);
			printIncludes(ps1);
			printLocalStatics(ps1);
			printSerializers(ps1);
			printDeSerializers(ps1);
			printInitializeSerializerFunctions(ps1);
			// ps1.println(RADIO_CHECK_END);
			f1.close();

		} catch (IOException e) {
			ErrorHandling.exceptionError(e, "Opening file: " + generateSerializerFileName());
		}
		try {
			OutputFile f2 = new HeaderFile(generateSerializerHeaderFileName());
			PrintStream ps2 = f2.getStream();

			printHeaderStart(ps2);
			printHeaderIncludes(ps2);
			printSerializerPrototypes(ps2);
			printDeSerializerPrototypes(ps2);
			printInitialiseFunctionPrototype(ps2);
			printHeaderEnd(ps2);
			f2.close();
		} catch (IOException e) {
			ErrorHandling.exceptionError(e, "Opening file: " + generateSerializerHeaderFileName());
		}
	}

	/************************ Private Memorisation Methods ************************/

	private boolean requireGeneration() {
		return !(encounteredTypesRequiringSerialization.isEmpty() && encounteredTypesRequiringDeSerialization.isEmpty());
	}

	/**
	 * @return true if the type has been potentially serialized in this compilation unit
	 */
	private static boolean encounteredSerializerAlready(ITypeRep t) {
		return encounteredTypesRequiringSerialization.contains(t);
	}

	/**
	 * @return true if the type has been potentially serialized in this compilation unit
	 */
	private static boolean encounteredDeSerializerAlready(ITypeRep t) {
		return encounteredTypesRequiringDeSerialization.contains(t);
	}

}
