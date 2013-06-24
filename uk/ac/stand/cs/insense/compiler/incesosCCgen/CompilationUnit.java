package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.cgen.ICompilationUnit;
import uk.ac.stand.cs.insense.compiler.cgen.IDecl;
import uk.ac.stand.cs.insense.compiler.cgen.IFunction;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling.HeaderFile;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling.ImplFile;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling.OutputFile;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;
import uk.ac.standrews.cs.nds.util.Diagnostic;
import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
import uk.ac.standrews.cs.nds.util.ErrorHandling;

public class CompilationUnit extends ProcedureContainer implements ICode, ICompilationUnit {

	private final List<String> vtblInitialiser;
	private final List<String> vtblDecl;
	private final List<String> externalIncludes;

	public CompilationUnit() {
		super("IComponent");
		vtblInitialiser = new ArrayList<String>();
		vtblDecl = new ArrayList<String>();
		externalIncludes = new ArrayList<String>();
		// TODO JL Space Tracking
		track_add_stack_byte(MSP430Sizes.functionCallOverhead(MSP430Sizes.PNTR_SIZE)); // calling main(void *pntr)
		track_call_space(MSP430Sizes.dalAssignCallOverHeadNoDecRef()); // for DAL_assign(&serialiserMap, Construct_StringMap() )
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.ICompilationUnit#complete()
	 */
	@Override
	public void complete() {
		generateMainProgramFile();
		generateMainHeaderFile();
		// TODO JL Space Tracking
		Diagnostic.trace(DiagnosticLevel.FINAL, "Compilation unit stack usage: " + get_maximal_stack_usage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.ICompilationUnit#addExternalInclude(java.lang.String)
	 */
	@Override
	public void addExternalInclude(String s) {
		if (!externalIncludes.contains(s)) {
			externalIncludes.add(s);
		}
	}

	private void generateMainProgramFile() {
		try {
			OutputFile f = new ImplFile(generateMainFileName());
			PrintStream ps = f.getStream();
			// PrintStream ps = System.out; // for debugging
			// Traverse data structures
			printHeaders(ps);
			printDALErrorFileName(ps);
			printHoistedCode(ps);
			printGlobals(ps);
			printGlobalFunctions(ps);
			printMain(ps);
			f.close();
		} catch (IOException e) {
			ErrorHandling.exceptionError(e, "Opening file: " + generateMainFileName());
		}
	}

	// TODO NOT NEEDED FOR INCEOS?

	// private void generateMainStruct(PrintStream ps){
	// ps.println("typedef struct Main_data MainStruct, *MainPNTR;");
	// ps.println("struct Main_data {");
	// ps.println(TAB + "void (*_decRef)(void *pntr);");
	// ps.println(TAB + "struct process *process_pntr;");
	// ps.println(TAB + "void *impl;");
	// ps.println( TAB + "ChannelPayloadStruct data_struct;" );
	// ps.println( TAB + "List_PNTR guard_ready_set;");
	// ps.println( TAB + "HalfChannel_PNTR branch_result;");
	// ps.println("};");
	// ps.println();
	// }

	// used by DAL_error macro
	private void printDALErrorFileName(PrintStream ps) {
		ps.println("#ifndef DALSMALL");
		ps.println("static char *file_name = \"" + "main" + "\";");
		ps.println("#endif");
	}

	private void generateMainHeaderFile() {
		try {
			OutputFile f = new HeaderFile(generateMainHeaderFileName());
			PrintStream ps = f.getStream();
			ps.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
			ps.println(IFNDEF_ + header_name("main"));
			ps.println(DEFINE_ + header_name("main"));
			ps.println();
			// ps.println( HASH_INCLUDE_ + DQUOTE + "DIAS.h" + DQUOTE ); // TODO NOT NEEDED FOR INCEOS?
			ps.println(HASH_INCLUDE_ + DQUOTE + "InsenseRuntime.h" + DQUOTE);
			ps.println(HASH_INCLUDE_ + DQUOTE + "marshaller.h" + DQUOTE);
			ps.println(HASH_INCLUDE_ + DQUOTE + "setjmp.h" + DQUOTE);
			ps.println(HASH_INCLUDE_ + DQUOTE + "events.h" + DQUOTE);
			ps.println(HASH_INCLUDE_ + DQUOTE + "GlobalVars.h" + DQUOTE);
			// ps.println( HASH_INCLUDE_ + DQUOTE + "AnyType.h" + DQUOTE ); // TODO Need moved to InsenseRuntime.h
			// ps.println( HASH_INCLUDE_ + DQUOTE + "Bool.h" + DQUOTE ); // TODO Need moved to InsenseRuntime.h
			ps.println();
			ps.println(generateExternalIncludes());
			printExternalProcIncludes(ps);
			ps.println();
			// generateMainStruct(ps); // TODO NOT NEEDED FOR INCEOS?
			// printProcedureFrameDecls( ps ); // TODO NOT NEEDD INCEOS?
			// printProcedureCallFunctionSignatures( ps ); // TODO NOT NEEDD INCEOS?
			// printCopyMacros( ps ); // JL copy macros not needed anymore, so removed, check that this is ok
			printGlobalFunctionDecls(ps);
			ps.println(ENDIF_ + C_COMMENT_OPEN_ + header_name("main") + C_COMMENT_CLOSE_);
			ps.println();
			f.close();
		} catch (IOException e) {
			ErrorHandling.exceptionError(e, "Opening file: " + generateMainHeaderFileName());
		}
	}

	private void printGlobals(PrintStream ps) {
		ps.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		for (String s : vtblDecl) {
			ps.println(s);
		}
		// TODO this is not a good place for this code but will do for now.
		TypeMarshaller.generateSerializersAndDeserializers();
		ps.println(globalLocationDecls());
		// main_stack_size definition so that InceOS knows size of main component
		ps.println("// main_stack_size definition so that InceOS knows size of main component\n" + "int main_stack_size = " + get_maximal_stack_usage() + SEMI
				+ NEWLINE);

	}

	protected String globalLocationDecls() {
		StringBuffer sb = new StringBuffer();
		// Set<String> ks = locations.keySet();
		// for( String s : ks ) {
		for (IDecl l : locations) {
			ITypeRep tr = l.getType();
			sb.append(STATIC_ + insenseTypeToCTypeName(tr) + SPACE + l.getSymbolTableEntry().getName() /* + SPACE + EQUALS_ + ZERO */+ SEMI + NEWLINE);
		}
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.ICompilationUnit#addVTBLUsage(java.lang.String)
	 */
	@Override
	public void addVTBLUsage(String s) {
		// This is an interface requirement for CompilationUnit.
	}

	// TODO NOT NEEDED INCEOS?
	// public void addVTBLUsage(String s) {
	// String initialiser = vtbl_global_name( s ) + EQUALS + functionCall( vtbl_constructor_name( s ),"" ) + SEMI;
	// String decl = vtbl_decl(s);
	// if( ! vtblInitialiser.contains( initialiser ) ) {
	// vtblInitialiser.add( initialiser );
	// vtblDecl.add( decl );
	// }
	// }

	private String generateMainFileName() {
		return "main.c";
	}

	private String generateMainHeaderFileName() {
		return "main.h";
	}

	private String generateExternalIncludes() {
		StringBuffer sb = new StringBuffer();
		for (String s : externalIncludes) {
			sb.append(s + NEWLINE);
		}
		return sb.toString();
	}

	private void printHeaders(PrintStream ps) {
		ps.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		// ps.println( "#include \"contiki.h\"" ); // TODO NOT NEEDED FOR INCEOS?
		// ps.println( "#include \"pt.h\"" );
		// ps.println( "#include \"DIAS.h\"" );
		// ps.println( HASH_INCLUDE_ + DQUOTE + "inceos.h" + DQUOTE );
		ps.println("#include \"main.h\"");
		ps.println(generateExternalIncludes());
		ps.println();
	}

	private void printGlobalFunctions(PrintStream ps) {
		ps.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		ps.println();
		Collection<IFunction> impls = functionBodies.values();
		for (IFunction code : impls) {
			ps.println(code.generateCode(this));
			ps.println();
		}
	}

	private void printGlobalFunctionDecls(PrintStream ps) {
		ps.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		Collection<IFunction> impls = functionBodies.values();
		for (IFunction code : impls) {
			ps.println(((Function) code).functionSignature() + SEMI);
		}
		ps.println();
	}

	private void print_vtbl_initialisation(PrintStream ps) {
		ps.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		for (String s : vtblInitialiser) {
			ps.println(TAB + s);
		}

	}

	private void printMain(PrintStream ps) {
		ps.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());

		ps.println("void primordial_main( void *this ) {");
		// ps.println(TAB + "inceos_event_t op_status;// for exception handling");
		// print_vtbl_initialisation( ps ); // TODO NOT NEEDED FOR INCEOS?
		ps.println(super.toString());

		ps.println(TAB + functionCall("component_exit") + SEMI
				+ "// as the primordial is a component itself created by the boot code, must deleted to return the memory and space it uses");
		ps.println("}");
	}
}
