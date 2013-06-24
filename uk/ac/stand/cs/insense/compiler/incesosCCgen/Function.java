package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.ac.stand.cs.insense.compiler.cgen.IDecl;
import uk.ac.stand.cs.insense.compiler.cgen.IFunction;
import uk.ac.stand.cs.insense.compiler.cgen.IProcedureContainer;
import uk.ac.stand.cs.insense.compiler.symbols.STEntry;
import uk.ac.stand.cs.insense.compiler.types.FunctionType;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;
import uk.ac.stand.cs.insense.compiler.types.VoidType;
import uk.ac.standrews.cs.nds.util.Diagnostic;
import uk.ac.standrews.cs.nds.util.DiagnosticLevel;

public class Function extends DeclarationContainer implements IFunction {

	private static final String PROCEDURE_CONS_PREFIX = "Call";
	IProcedureContainer container;
	private String name;
	private final List<String> parameter_names;
	private final FunctionType ft;
	private final StringBuffer body;
	private final String C_return_param_name = "proc_result";
	private final String end_proc_label = "end_proc";
	private boolean throws_exception = false;
	private boolean hasReturn = false;

	// A list of local function decls that are in scope.
	// This is used to track decls from e.g. sequences that have not yet completed.
	// These decls may need garbage collecting prior to jumping to the end of the proc
	// when an Insense return statement is processed
	private final ArrayList<IDecl> localDeclsInScope;

	// TODO JL Space Tracking
	private STEntry ste;
	private static Map<STEntry, Integer> functionStacks = new HashMap<STEntry, Integer>();

	public Function(IProcedureContainer container, String the_name, FunctionType ft, List<String> parameter_names, int scope_level) {
		super();
		this.container = container;
		if (container instanceof Component) {
			this.name = ((Component) container).getName() + UNDERBAR + the_name;
		} else {
			this.name = the_name;
		}
		this.parameter_names = parameter_names;
		this.ft = ft;
		this.body = new StringBuffer();
		this.localDeclsInScope = new ArrayList<IDecl>();
		this.ste = null;

		body.append(GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE);

		// int i = 0;
		// for( ITypeRep param : ft.getArgs() ) {
		// STEntry faked_decl = new STEntry( parameter_names.get(i), param, false, scope_level, context, 0 );
		// addLocation( new Decl( faked_decl ) );
		// i++;
		// }
	}

	public String paramSignature(FunctionType ft, List<String> names, IProcedureContainer container) {
		StringBuffer sb = new StringBuffer();
		sb.append(LRB_);
		if (container instanceof Component) {
			sb.append(data_pntr_name(container.getName()) + "this");
		} else {
			sb.append(VOIDSTAR_ + "this");
		}
		sb.append(COMMA + SPACE + "jmp_buf *ex_handler");
		// PARAMS HERE
		int i = 0;
		for (ITypeRep param : ft.getArgs()) {
			sb.append(COMMA + SPACE);
			sb.append(insenseTypeToCTypeName(param) + names.get(i));
			i++;
		}

		sb.append(SPACE + RRB_);
		return sb.toString();
	}

	public String functionSignature() {
		StringBuffer sb = new StringBuffer();
		sb.append(insenseTypeToCTypeName(ft.getResult()) + SPACE + name + UNDERBAR + "proc");
		sb.append(paramSignature(ft, parameter_names, container));
		return sb.toString();
	}

	@Override
	public String generateCode(IProcedureContainer container) {
		StringBuffer sb = new StringBuffer();
		sb.append(GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE);
		sb.append(functionSignature() + LCB_ + NEWLINE);

		// sb.append( TAB + "inceos_event_t op_status" + SEMI + "// for exception handling" + NEWLINE);
		if (!ft.getResult().equals(VoidType.TYPE)) { // i.e. function returns a value
			sb.append(TAB + insenseTypeToCTypeName(ft.getResult()) + SPACE + C_return_param_name + SPACE + EQUALS_ + ft.getResult().getDefaultCValue() + SEMI
					+ NEWLINE);
		}
		int i = 0;
		for (ITypeRep param : ft.getArgs()) {
			if (param.isPointerType()) {
				sb.append(TAB + functionCall("DAL_incRef", parameter_names.get(i)) + SEMI + NEWLINE);
			}
			i++;
		}

		// put in the body of the function
		sb.append(body.toString() + NEWLINE);

		// put in end_proc label
		if (this.hasReturn) {
			sb.append(end_proc_label + Code.COLON + NEWLINE);
		}
		// Garbage collect parameters here
		// Note: do not garbage collect the return value which will be
		// assigned and decRefed by the receiving context
		i = 0;
		sb.append(TAB + "// decRef any pntr function parameters" + NEWLINE);
		for (ITypeRep param : ft.getArgs()) {
			if (param.isPointerType()) {
				sb.append(TAB + functionCall("DAL_decRef", parameter_names.get(i)) + SEMI + NEWLINE);
			}
			i++;
		}

		// if procedure returns a value we must return it here
		if (!ft.getResult().equals(VoidType.TYPE)) {
			if (ft.getResult().isPointerType()) {
				sb.append(TAB + "// special decRef (without garbage collect) for function return value" + NEWLINE);
				sb.append(TAB + functionCall("DAL_modRef_by_n", C_return_param_name, "-1") + SEMI + NEWLINE);
			}
			sb.append(TAB + Code.RETURN_ + C_return_param_name + SEMI + NEWLINE);
		}

		// start procedure process and return to caller
		sb.append(RCB_ + NEWLINE);
		return sb.toString();
	}

	@Override
	public void append(String s) {
		body.append(s);
	}

	@Override
	public void setThrowsException(boolean throws_exception) {
		this.throws_exception = throws_exception;
	}

	@Override
	public String getBody() {
		return body.toString();
	}

	@Override
	public FunctionType getFt() {
		return ft;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<String> getParamNames() {
		return parameter_names;
	}

	@Override
	public void setContainsReturnStatement(boolean hasReturn) {
		this.hasReturn = hasReturn;
	}

	// TODO JL Space Tracking
	@Override
	public void setSTEntry(STEntry ste) {
		this.ste = ste;
	}

	public static int getFunctionStackSize(STEntry ste) {
		// for user defined functions
		Integer Size = functionStacks.get(ste);
		if (Size != null) {
			return Size.intValue();
		}
		// for standard functions included in the runtime
		List<ITypeRep> args = ((FunctionType) ste.getType()).getArgs();
		return MSP430Sizes.functionCallOverhead(args);

	}

	@Override
	public void complete() {
		functionStacks.put(ste, get_maximal_stack_usage());
		int proc_call_overhead = MSP430Sizes.procedureCallOverhead(ft.getArgs());
		Diagnostic.trace(DiagnosticLevel.FINAL, "proc " + name + ": call_overhead = " + proc_call_overhead + ", stack_usage = " + get_maximal_stack_usage());
		super.complete();
		if (!ft.getResult().equals(VoidType.TYPE) && !hasReturn) {
			Cgen.get_instance().getCompilerErrors()
					.generalError("Procedure " + this.name + " has return type " + ft.getResult().toHumanReadableString() + " but no return statement");
		}
	}

	@Override
	public String getEnd_proc_label() {
		return end_proc_label;
	}

	@Override
	public String getCReturnParamName() {
		return C_return_param_name;
	}

	public ArrayList<IDecl> getLocalDeclsInScope() {
		return localDeclsInScope;
	}
}
