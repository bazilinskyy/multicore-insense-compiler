package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import uk.ac.stand.cs.insense.compiler.cgen.IStructDeclaration;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling.HeaderFile;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling.ImplFile;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling.OutputFile;
import uk.ac.stand.cs.insense.compiler.types.ArrayType;
import uk.ac.stand.cs.insense.compiler.types.ChannelType;
import uk.ac.stand.cs.insense.compiler.types.EnumType;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;
import uk.ac.stand.cs.insense.compiler.types.StructType;
import uk.ac.standrews.cs.nds.util.Diagnostic;
import uk.ac.standrews.cs.nds.util.ErrorHandling;

public abstract class StructDeclaration extends Code implements IStructDeclaration {

	private List<ITypeRep> fieldTypes;
	private List<String> fieldNames;
	private StructType type;
	private StringBuffer arrayBuffer = new StringBuffer();
	private final List<String> externalIncludes;

	private boolean decldAlready;

	private static String PARAMNAME = "p";

	private static String copy_start = "copy_";
	private static String copy_end = "_array";

	/**
	 * Tracks already generated structs
	 * Maps from concatenatedfieldtypes and types to struct name
	 */
	private static List<String> generatedAlready = new ArrayList<String>();

	public StructDeclaration() {
		this.fieldTypes = new ArrayList<ITypeRep>();
		this.fieldNames = new ArrayList<String>();
		this.decldAlready = false;
		this.externalIncludes = new ArrayList<String>();
	}

	public StructDeclaration(StructType type) {
		this();
		this.type = type;
		this.decldAlready = true;
		this.fieldTypes = type.getFields();
		this.fieldNames = type.getFieldNames();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IStructDeclaration#set_type(uk.ac.stand.cs.insense.compiler.types.StructType)
	 */
	@Override
	public void set_type(StructType type) {
		this.type = type;
	}

	protected String struct_name() {
		return struct_name(type);
	}

	public static String struct_name(StructType type) {
		return type.getName();
	}

	protected String constructorName() {
		return constructor_name(type);
	}

	public static String constructor_name(StructType type) {
		return "construct_" + struct_name(type);
	}

	/**
	 * @return true if the struct has been generated already in this compilation unit
	 */
	protected boolean generatedAlready() {
		return generatedAlready.contains(struct_name());
	}

	private String struct_pntr_name() {
		return struct_name() + UNDERBAR + PNTR_;
	}

	private String struct_struct_name() {
		return struct_name() + UNDERBAR + STRUCT_; // this was changed and may be wrong now - al
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IStructDeclaration#fieldType(uk.ac.stand.cs.insense.compiler.types.ITypeRep)
	 */
	@Override
	public void fieldType(ITypeRep tr) {
		if (!decldAlready) {
			fieldTypes.add(tr);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IStructDeclaration#fieldName(java.lang.String)
	 */
	@Override
	public void fieldName(String s) {
		if (!decldAlready) {
			fieldNames.add(s);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IStructDeclaration#complete()
	 */
	@Override
	public void complete() {
		if (!generatedAlready()) {
			generateSourceFile();
			generateHeaderFile();
		}
	}

	// JL added following to deal with struct header file
	// generation

	/**
	 * @return true if the struct has been generated already in this compilation unit
	 */
	private static boolean generatedAlready(StructType type) {
		return generatedAlready.contains(type.getName());
	}

	/**
	 * Generate a struct decl for this type
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.incesosCCgen.Code#complete()
	 */
	public void generate(StructType type) {
		if (!generatedAlready(type)) {
			generateHeaderFile();
		}
		Cgen.get_instance().addIncludeToCurrentContext(include_headers());
	}

	/**
	 * Generate a struct decl for this type and generate the construction of the Insense struct.
	 */
	private String generateStructDecl() {
		StringBuffer sb = new StringBuffer();
		sb.append(GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE);
		sb.append(TYPEDEF_ + STRUCT_ + struct_name() + SPACE + "*" + struct_pntr_name() + COMMA + SPACE + struct_struct_name() + SEMI + NEWLINE);
		sb.append(STRUCT_ + struct_name() + SPACE + LCB_ + NEWLINE);
		sb.append(generateStructDecls());
		sb.append(RCB_ + SEMI + NEWLINE);
		sb.append(NEWLINE);
		generatedAlready.add(struct_name());
		return sb.toString();
	}

	private static String array_copy_function_name(ITypeRep type) {
		return copy_start + type.toString() + copy_end;
	}

	private String struct_copy_function_name(StructType type) {
		return "copy_" + struct_name(type);
	}

	private StringBuffer getArrayCopyBuffer() {
		StringBuffer result = arrayBuffer;
		arrayBuffer = new StringBuffer();
		return result;
	}

	/**
	 * @return a String of constructor arguments used in the copy function
	 *         Arrays and Channels are deep copied.
	 *         Assumptions
	 *         1. Structs cannot contain other structs
	 *         2. Structs cannot contain components
	 *         3. Strings (if we have them) are non mutable and can be shallow copied
	 *         4. Arrays and Channels must be copied by using the appropriate array and channel copy function
	 *         5. All other types are scalars and can be value copied.
	 */
	private String copyFunctionConstructorArguments() {
		StringBuffer sb = new StringBuffer();
		int index = 0;
		for (ITypeRep tr : fieldTypes) {
			String fname = fieldNames.get(index++);
			String copyfield;
			if (tr instanceof ArrayType) {
				ArrayType at = (ArrayType) tr;
				generateArrayCopyFunction(at);
			}
			if (tr instanceof ArrayType || tr instanceof ChannelType) {
				copyfield = functionCall(generate_unique_copy_function_name(tr), PARAMNAME + ARROW + fname);
			} else {
				copyfield = PARAMNAME + ARROW + fname;
			}
			sb.append(copyfield);
			if (index < fieldTypes.size()) {
				sb.append(COMMA + SPACE);
			}
		}
		return sb.toString();
	}

	private void generateArrayCopyFunction(ArrayType at) {
		if (!ArrayConstructor.generatedConstructorAlready(at)) {
			arrayBuffer.append(ArrayConstructor.constructorCode(at));
		}
		if (!ArrayConstructor.generatedCopyfunctionAlready(at)) {
			arrayBuffer.append(ArrayConstructor.copyFunctionCode(at));
		}
	}

	private String copyFuncPrototype() {
		return struct_pntr_name() + "copy_" + struct_name() + LRB_ + struct_pntr_name() + PARAMNAME + SPACE + RRB_;
	}

	/*
	 * // old code generates copy function that replicates constructor code
	 * private String generateCopyFunction() {
	 * StringBuffer sb = new StringBuffer();
	 * sb.append( copyFuncPrototype() + SPACE + LCB_ + NEWLINE );
	 * sb.append( genMallocSizeofAssign( struct_pntr_name(), "pntr", struct_struct_name() , this.structContainsPointers() ) );
	 * if(this.structContainsPointers())
	 * sb.append( TAB + "pntr" + ARROW + "_decRef" + EQUALS + PARAMNAME + ARROW + "_decRef" + SEMI + NEWLINE );
	 * sb.append( structFieldCopy() );
	 * sb.append( TAB + RETURN_ + "pntr" + SEMI + NEWLINE );
	 * sb.append( RCB_ + NEWLINE );
	 * sb.append( NEWLINE );
	 * sb.insert(0, getArrayCopyBuffer() );
	 * return sb.toString();
	 * }
	 */

	private String generateCopyFunction() {
		StringBuffer sb = new StringBuffer();
		sb.append(copyFuncPrototype() + SPACE + LCB_ + NEWLINE);

		// increment ref count of parameter this (the source array)
		sb.append(TAB + functionCall("DAL_incRef", "p") + SEMI + NEWLINE);

		sb.append(TAB + struct_pntr_name() + "copy" + SPACE + EQUALS_ + constructorName() + LRB + SPACE + copyFunctionConstructorArguments() + SPACE + RRB
				+ SEMI + NEWLINE);

		// decrement ref count of parameter this (the source array)
		sb.append(TAB + functionCall("DAL_decRef", "p") + SEMI + NEWLINE);

		sb.append(TAB + RETURN_ + "copy" + SEMI + NEWLINE);
		sb.append(RCB_ + NEWLINE);
		sb.append(NEWLINE);
		// sb.insert(0, getArrayCopyBuffer() );
		return sb.toString();
	}

	private String constructorPrototype() {
		return struct_pntr_name() + constructorName() + LRB_ + generateConstuctorParamDecls() + SPACE + RRB;
	}

	private String generateConstructor() {
		StringBuffer sb = new StringBuffer();
		sb.append(constructorPrototype() + SPACE + LCB_ + NEWLINE);
		sb.append(genMallocSizeofAssign(struct_pntr_name(), "pntr", struct_struct_name(), this.structContainsPointers()));
		sb.append(structFieldInitialisers());
		sb.append(TAB + RETURN_ + "pntr" + SEMI + NEWLINE);
		sb.append(RCB_ + NEWLINE);
		sb.append(NEWLINE);
		return sb.toString();
	}

	private String decRefPrototype() {
		return VOID_ + "decRef_" + struct_name() + LRB_ + struct_pntr_name() + "pntr" + SPACE + RRB;
	}

	private String structFieldDecrementers() {
		StringBuffer sb = new StringBuffer();
		int index = 0;
		for (ITypeRep tr : fieldTypes) {
			if (tr.isPointerType()) {
				String fname = fieldNames.get(index);
				sb.append(TAB + functionCall("DAL_decRef", "pntr" + ARROW + fname) + SEMI + NEWLINE);
			}
			index++;
		}
		return sb.toString();
	}

	private String generateDecRef() {
		StringBuffer sb = new StringBuffer();
		sb.append(decRefPrototype() + SPACE + LCB_ + NEWLINE);
		if (structContainsPointers()) {
			sb.append(structFieldDecrementers());
		}
		sb.append(RCB_ + NEWLINE);
		sb.append(NEWLINE);
		return sb.toString();
	}

	private boolean structContainsPointers() {
		for (ITypeRep tr : fieldTypes) {
			if (tr.isPointerType()) {
				return true;
			}
		}
		return false;
	}

	private String generateStructDecls() {
		StringBuffer sb = new StringBuffer();
		int index = 0;
		if (this.structContainsPointers())
			sb.append(TAB + "void (*_decRef)" + LRB + struct_pntr_name() + " pntr" + RRB + SEMI + NEWLINE);	// declare decRef function
		for (ITypeRep tr : fieldTypes) {
			sb.append(TAB + insenseTypeToCTypeName(tr) + SPACE + fieldNames.get(index++) + SEMI + NEWLINE);
		}
		return sb.toString();
	}

	private String generateConstuctorParamDecls() {
		StringBuffer sb = new StringBuffer();
		int index = 0;
		for (ITypeRep tr : fieldTypes) {
			if (index != 0) {
				sb.append(COMMA);
			}
			sb.append(insenseTypeToCTypeName(tr) + SPACE + fieldNames.get(index++));
		}
		return sb.toString();
	}

	private String structFieldInitialisers() {
		StringBuffer sb = new StringBuffer();
		// generate the decrement
		if (structContainsPointers()) {
			sb.append(TAB + "pntr" + ARROW + "_decRef" + EQUALS + "decRef_" + struct_name() + SEMI + NEWLINE);
		}
		int index = 0;
		for (ITypeRep tr : fieldTypes) {
			String fname = fieldNames.get(index++);
			if (tr.isPointerType()) {
				sb.append(TAB + functionCall("DAL_assign", AMPERSAND + "pntr" + ARROW + fname, fname) + SEMI + NEWLINE);
			} else {
				sb.append(TAB + "pntr" + ARROW + fname + EQUALS + fname + SEMI + NEWLINE);
			}
		}
		return sb.toString();
	}

	// File handling stuff

	public String include_filename() {
		return struct_name() + ".h";
	}

	public String source_filename() {
		return struct_name() + ".c";
	}

	public String header_name() {
		return struct_name().toUpperCase() + "_H_";
	}

	public String include_headers() {
		return include_filename();
	}

	public void addExternalIncludes(String s) {
		// System.err.println("Adding " + s);
		if (!externalIncludes.contains(s)) {
			externalIncludes.add(s);
		}
	}

	private String generateExternalIncludes() {
		StringBuffer sb = new StringBuffer();
		for (String s : externalIncludes) {
			sb.append(s + NEWLINE);
		}
		return sb.toString();
	}

	/**
	 * Writes the standard include file headers to the stream
	 * 
	 * @param ps
	 *            - the stream on which the decls are written
	 */
	private void printDOTHHeaders(PrintStream ps) {
		ps.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		ps.println(IFNDEF_ + header_name());
		ps.println(DEFINE_ + header_name());
		ps.print("#include \"InsenseRuntime.h\"\n");
		ps.print("#include \"String.h\"\n");
		ps.print("#include \"AnyType.h\"\n");
		ps.print(generateExternalIncludes());
		// JL this should probably be fixed somewhere else
		// But for a quick hack to cause the enum header to be included
		// when a struct contains an enum
		int index = 0;
		for (ITypeRep tr : fieldTypes) {
			if (tr instanceof EnumType) {
				String ftype = ((EnumType) tr).getName();
				ps.print("#include \"" + ftype + ".h\"\n");
			}
		}
		// ps.print("#include \"OutInEither.h\"\n");
		ps.print("\n");
	}

	/**
	 * Writes the standard include file trailers to the stream
	 * 
	 * @param ps
	 *            - the stream on which the decls are written
	 */
	private void printTrailers(PrintStream ps) {
		ps.println(ENDIF_ + C_COMMENT_OPEN_ + header_name() + C_COMMENT_CLOSE_);
		ps.println();
	}

	// used by DAL_error macro
	private void printDALErrorFileName(PrintStream ps) {
		ps.println("#ifndef DALSMALL");
		ps.println("static char *file_name = \"" + struct_name() + "\";");
		ps.println("#endif");
	}

	private void generateSourceFile() {
		try {
			OutputFile f = new ImplFile(source_filename());
			PrintStream ps = f.getStream();
			ps.print("#include \"" + include_filename() + "\"\n");
			ps.print("\n");
			printDALErrorFileName(ps);
			String copyFunction = generateCopyFunction() + "\n";
			String decRef = "";
			if (this.structContainsPointers())
				decRef += generateDecRef() + "\n";
			String constructorFunction = generateConstructor() + "\n";
			String arrayFunctions = getArrayCopyBuffer().toString() + "\n";
			ps.print(arrayFunctions);
			ps.print(copyFunction);
			ps.print(decRef);
			ps.print(constructorFunction);
			f.close();
		} catch (IOException e) {
			ErrorHandling.exceptionError(e, "Opening file: " + source_filename());
		}

	}

	private void generateHeaderFile() {
		try {
			OutputFile f = new HeaderFile(include_filename());
			PrintStream ps = f.getStream();
			printDOTHHeaders(ps);
			ps.println(generateStructDecl());
			ps.println("extern " + constructorPrototype() + ";\n");
			ps.println("extern " + copyFuncPrototype() + ";\n");
			printTrailers(ps);
			f.close();
		} catch (IOException e) {
			ErrorHandling.exceptionError(e, "Opening file: " + include_filename());
		}
	}

}
