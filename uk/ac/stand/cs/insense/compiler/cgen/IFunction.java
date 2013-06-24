package uk.ac.stand.cs.insense.compiler.cgen;

import java.util.List;
import java.util.Map;

import uk.ac.stand.cs.insense.compiler.incesosCCgen.Component;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.ProcedureContainer;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.symbols.STEntry;
import uk.ac.stand.cs.insense.compiler.types.FunctionType;

public interface IFunction extends IDeclarationContainer {

	public FunctionType getFt();

	public String getName();

	public List<String> getParamNames();

	public String getBody();

	public String generateCode(IProcedureContainer comp);
	
	public void setContainsReturnStatement(boolean has_return);
	
	public void setThrowsException(boolean throws_exception);

	public void append(String s);
	
	public void setSTEntry(STEntry ste);
	
	public String getEnd_proc_label();
	
	public String getCReturnParamName();

}