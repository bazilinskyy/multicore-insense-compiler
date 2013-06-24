package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.io.PrintStream;
import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.cgen.IProcedureContainer;
import uk.ac.standrews.cs.nds.util.Diagnostic;

public class ProcedureContainer extends DeclarationContainer implements ICode, IProcedureContainer {

	private StringBuffer procedureFrameDecls;
	private StringBuffer procedureCallFunctionSignatures;
	private StringBuffer externalProcIncludes;
	private String name;
	public ProcedureContainer( String name ) {
		super();
		this.name = name;
		this.procedureCallFunctionSignatures = new StringBuffer(); 
		this.procedureFrameDecls = new StringBuffer();
		this.externalProcIncludes = new StringBuffer();
	}

	public void addExternalProcIncludes(String s){
		externalProcIncludes.append(s);
	}
	
	public void addProcessFrameDecl(String s){
		procedureFrameDecls.append(s);
	}

	
	public void addProcessConstructorFunctionSignature(String s){
		procedureCallFunctionSignatures.append(s);
	}
	
	public void printExternalProcIncludes(PrintStream ps){
		ps.println( GENERATED_FROM + Diagnostic.getMethodInCallChain() );
		ps.println(externalProcIncludes.toString());
		ps.println();		
	}
	
	public void printProcedureFrameDecls(PrintStream ps) {
		ps.println( GENERATED_FROM + Diagnostic.getMethodInCallChain() );
		ps.println(procedureFrameDecls.toString());
		ps.println();
	}

	
	public void printProcedureCallFunctionSignatures(PrintStream ps) {
		ps.println( GENERATED_FROM + Diagnostic.getMethodInCallChain() );
		ps.println(procedureCallFunctionSignatures.toString());
		ps.println();
	}


	public String getName() {
		return name;
	}
	
}
