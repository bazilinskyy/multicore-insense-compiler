package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.ac.stand.cs.insense.compiler.cgen.IConstructor;
import uk.ac.stand.cs.insense.compiler.cgen.IDeclarationContainer;
import uk.ac.stand.cs.insense.compiler.cgen.IProcedureContainer;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.symbols.STEntry;
import uk.ac.stand.cs.insense.compiler.types.FunctionType;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;
import uk.ac.stand.cs.insense.compiler.types.StructType;
import uk.ac.standrews.cs.nds.util.Diagnostic;
import uk.ac.standrews.cs.nds.util.DiagnosticLevel;

public class Constructor extends DeclarationContainer implements IConstructor {

	private int disambiguator;
	IProcedureContainer container;
	FunctionType ft;
	List<String> parameter_names;
	String name;
	StringBuffer body;

	private static final String CONSTRUCTOR_PREFIX = "Construct";

	// TODO JL Space Tracking
	private static Map<String, Integer> constructorStacks = new HashMap<String, Integer>();

	public Constructor(IProcedureContainer container, FunctionType ft, List<String> parameter_names, int scope_level) {
		this.container = container;
		this.ft = ft;
		if (parameter_names == null) {
			this.parameter_names = new ArrayList<String>();
		} else {
			this.parameter_names = parameter_names;
		}
		this.name = container.getName() + ((Component) container).getConstructorDisambiguator();
		this.body = new StringBuffer();
		int i = 0;
		for (ITypeRep param : ft.getArgs()) {
			STEntry faked_decl = new STEntry(parameter_names.get(i), param, false, scope_level, ISymbolTable.CONSTRUCTOR, 0);
			addLocation(new Decl(faked_decl));
			i++;
		}
	}

	public String arrayParamSignature(FunctionType ft, List<String> names, IDeclarationContainer container) {
		StringBuffer sb = new StringBuffer();
		sb.append(LRB_);
		int i = 0;
		for (ITypeRep param : ft.getArgs()) {
			if (i > 0)
				sb.append(COMMA + SPACE);
			sb.append(insenseTypeToCTypeName(param) + names.get(i));
			i++;
		}

		sb.append(SPACE + RRB_);
		return sb.toString();
	}

	public String paramSignature(FunctionType ft, List<String> names, IProcedureContainer container) {
		StringBuffer sb = new StringBuffer();
		sb.append(LRB_);
		sb.append(data_pntr_name(container.getName()) + "this");
		sb.append(COMMA + SPACE);
		sb.append("int _argc, void* _argv[]");
		sb.append(SPACE + RRB_);
		return sb.toString();
	}

	public String arrayFunctionSignature() {
		StringBuffer sb = new StringBuffer();
		sb.append(VOID_ + STAR + STAR + constructArrayName(name, disambiguator));

		sb.append(arrayParamSignature(ft, parameter_names, container));
		return sb.toString();
	}

	@Override
	public String constructorFunctionSignature() {
		StringBuffer sb = new StringBuffer();
		sb.append(VOID_ + constructorFunctionName(name, disambiguator));

		sb.append(paramSignature(ft, parameter_names, container));
		return sb.toString();
	}

	private static String genCompMallocAssign(String type, String lhs, String size) {
		StringBuffer sb = new StringBuffer();
		sb.append(GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE);
		sb.append(TAB + type + SPACE + lhs + EQUALS + LRB_ + type + RRB_);
		sb.append(functionCall("DAL_comp_alloc", size) + SEMI + NEWLINE);
		sb.append(TAB + IF + LRB_ + lhs + EQUALSEQUALS_ + NULL_ + RRB_ + LCB_ + NEWLINE);
		sb.append(TAB + TAB + "DAL_error(OUT_OF_MEMORY_ERROR);" + NEWLINE);
		sb.append(TAB + TAB + RETURN_ + NULL + SEMI + NEWLINE);
		sb.append(TAB + RCB_ + NEWLINE);
		return sb.toString();
	}

	private String genCompMallocSizeofAssign(String type, String lhs, String typename) {
		return genCompMallocAssign(type, lhs, functionCall("sizeof", typename));
	}

	public String arrayFunctionDecl() {
		StringBuffer sb = new StringBuffer();
		if (ft.getArgs().size() > 0) {
			sb.append(GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE);
			Component containingComponent = null;
			if (container instanceof Component) {
				containingComponent = (Component) container;
			} else {
				System.err.println("Constructor container is not component instance ....");
			}

			sb.append(arrayFunctionSignature() + LCB_ + NEWLINE);

			// dynamically create locations to put into argv array
			int i = 0;
			for (ITypeRep param : ft.getArgs()) {
				// only need to allocate space for non-pointer locations, pointer locations must have been created in
				// the calling context and can be inserted directly into the argv array using DAL_assign
				if (!param.isPointerType()) {
					sb.append(TAB + insenseTypeToCTypeName(param) + SPACE + STAR + "_p2" + parameter_names.get(i) + SPACE + EQUALS_
							+ functionCall("DAL_alloc", functionCall("sizeof", insenseTypeToCTypeName(param)), SPACE + "false") + SEMI + NEWLINE);
					sb.append(TAB + STAR + "_p2" + parameter_names.get(i) + SPACE + EQUALS_ + parameter_names.get(i) + SEMI + NEWLINE);
				}
				i++;
			}

			// dynamically create argv array
			sb.append(TAB + VOID_ + STAR + STAR + "_argv" + SPACE + EQUALS_
					+ functionCall("DAL_alloc", parameter_names.size() + STAR + functionCall("sizeof", VOIDSTAR_), SPACE + "false") + SEMI + NEWLINE);

			// put locations into argv array
			i = 0;
			for (ITypeRep param : ft.getArgs()) {
				// only need to allocate space for non-pointer locations, pointer locations must have been created in
				// the calling context and can be inserted directly into the argv array using DAL_assign
				String potential_p2_prefix = "";
				if (!param.isPointerType()) {
					potential_p2_prefix = "_p2";
				}
				sb.append(TAB + functionCall("DAL_assign", AMPERSAND + "_argv[" + i + "]", potential_p2_prefix + parameter_names.get(i)) + SEMI + NEWLINE);
				i++;
			}
			sb.append(TAB + RETURN_ + "_argv" + SEMI + NEWLINE);
			sb.append(RCB_ + NEWLINE);
		}
		return sb.toString();
	}

	public String constructorFunctionDecl() {
		StringBuffer sb = new StringBuffer();
		sb.append(GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE);
		Component containingComponent = null;
		if (container instanceof Component) {
			containingComponent = (Component) container;
		} else {
			System.err.println("Constructor container is not component instance ....");
		}
		sb.append(constructorFunctionSignature() + LCB_ + NEWLINE);

		// sort out the params back into their proper names from the argv
		int i = 0;
		for (ITypeRep param : ft.getArgs()) {

			sb.append(TAB + insenseTypeToCTypeName(param) + parameter_names.get(i) + SPACE + EQUALS_);
			String cast_and_deref_star = "";
			if (!param.isPointerType()) {
				cast_and_deref_star = STAR;
			}
			sb.append(cast_and_deref_star + LRB + LRB + insenseTypeToCTypeName(param) + cast_and_deref_star + RRB + "_argv[" + i++ + "]" + RRB + SEMI + NEWLINE);
		}

		sb.append(TAB + functionCall(containingComponent.getInitGlobalsName(), "this") + SEMI + NEWLINE);
		sb.append(TAB + functionCall("sem_post" , "&this->component_create_sem") + SEMI + NEWLINE);

		sb.append(generateHoistedCode());
		sb.append(body.toString() + NEWLINE);

		// decRef the argv array passed to the constructor
		if (parameter_names.size() > 0) {
			sb.append(TAB + "int _i" + SEMI + NEWLINE);
			sb.append(TAB + FOR + LRB + "_i = 0 ;_i <_argc ;_i++ " + RRB + LCB_ + NEWLINE);
			sb.append(tab(2) + functionCall("DAL_decRef", "_argv[_i]") + SEMI + NEWLINE);
			sb.append(TAB + RCB_ + NEWLINE);
			sb.append(TAB + functionCall("DAL_decRef", "_argv") + SEMI + NEWLINE);
		}
		sb.append(TAB + functionCall(BEHAVIOUR_ + container.getName(), "this") + SEMI + NEWLINE);
		sb.append(RCB_ + NEWLINE);

		return sb.toString();
	}

	public static String constructArrayName(String componentName, int disambiguator) {
		return CONSTRUCTOR_PREFIX + UNDERBAR + "argv" + UNDERBAR + "array" + UNDERBAR + componentName;
	}

	public static String constructorFunctionName(String componentName, int disambiguator) {
		return CONSTRUCTOR_PREFIX + UNDERBAR + componentName;
	}

	@Override
	public int getDisambiguator() {
		return disambiguator;
	}

	@Override
	public void setDisambiguator(int disambiguator) {
		this.disambiguator = disambiguator;
	}

	@Override
	public void append(String s) {
		body.append(s);
	}

	private String getExternalIncludes() {
		StringBuffer sb = new StringBuffer();
		for (ITypeRep param : ft.getArgs()) {
			if (param instanceof StructType) {
				String structName = ((StructType) param).getName() + ".h"; // refactor? structName in StructureDeclaration?
				String include = HASH_INCLUDE_ + "\"" + structName + "\"" + NEWLINE;
				sb.append(include);
			}
		}
		return sb.toString();
	}

	@Override
	public String generateCode(IProcedureContainer container) {
		StringBuffer sb = new StringBuffer();
		sb.append(GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE);
		sb.append(arrayFunctionDecl());
		sb.append(constructorFunctionDecl());
		container.addExternalProcIncludes(NEWLINE + getExternalIncludes());
		container.addProcessConstructorFunctionSignature(NEWLINE + EXTERN_ + constructorFunctionSignature() + SEMI);
		return sb.toString();
	}

	@Override
	public FunctionType getFt() {
		return ft;
	}

	// TODO JL Space Tracking
	public static int getConstructorStackSize(String name) {
		return constructorStacks.get(name).intValue();
	}

	@Override
	public void complete() {
		constructorStacks.put(constructorFunctionName(name, disambiguator), get_maximal_stack_usage());
		int constructor_call_overhead = MSP430Sizes.componentConstructorCallOverhead(ft.getArgs());
		Diagnostic.trace(DiagnosticLevel.FINAL, "constructor " + name + ": call_overhead = " + constructor_call_overhead + ", stack_usage = "
				+ get_maximal_stack_usage());
		super.complete();
	}

}
