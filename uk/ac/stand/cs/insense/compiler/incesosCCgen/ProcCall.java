package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.util.ArrayList;
import java.util.List;

import uk.ac.stand.cs.insense.compiler.cgen.IDeclarationContainer;
import uk.ac.stand.cs.insense.compiler.cgen.ILocation;
import uk.ac.stand.cs.insense.compiler.cgen.IProcCall;
import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.symbols.STEntry;
import uk.ac.stand.cs.insense.compiler.types.CType;
import uk.ac.stand.cs.insense.compiler.types.FunctionType;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;
import uk.ac.stand.cs.insense.compiler.types.VoidType;

public class ProcCall extends Code implements ICode, IProcCall {
	
	STEntry the_fun;
	FunctionType ft;
	ITypeRep frt;
	boolean returns_result;
	List<String> params;
	ISymbolTable context;
	private StringBuffer procCallCode; 
	private StringBuffer paramCode;
	private String try_except_handler_jump = "";
	private static int counter = 0;
	private static boolean in_print_proc_call = false;   // JL hack to save some space, record whether we are in a call to a print function
	private boolean in_try_block = false;
	
	
	private Object selectedOutput = null;
	private IDeclarationContainer declContainer;
	
	public ProcCall( STEntry ste , ISymbolTable context, IDeclarationContainer declContainer) {
		params = new ArrayList<String>();
		the_fun = ste;
		ft = (FunctionType) ste.getType();
		frt = ft.getResult();
		returns_result = !frt.equals(VoidType.TYPE);
		this.context = context;
		this.declContainer = declContainer;
		this.procCallCode = new StringBuffer();
		this.paramCode = new StringBuffer();
	}


	public void append( String s ) {
		paramCode.append( s );
	}

		
	
	public void complete() {

		paramCode.append(SPACE + RRB_);
		
		// TODO JL terrible hack to prevent serialiser generation when any is constructed for printAny
		if(the_fun.baseName().equals("printAny")){
			in_print_proc_call = true;
		}
		if(!returns_result){
			procCallCode.append(TAB);
		}
		if(the_fun.getContext() == ISymbolTable.COMPONENT && declContainer != null){
			procCallCode.append(((Component) declContainer).getName() + UNDERBAR);
		}
		procCallCode.append( the_fun.baseName()+"_proc" ); 
		procCallCode.append(LRB + THIS);
		// TODO for exception handling
		procCallCode.append(COMMA + SPACE);
		if(in_try_block){
			procCallCode.append(AMPERSAND + "ex_handler");
		} else {
			if(context.getContext() == ISymbolTable.FUNCTION){
				procCallCode.append("ex_handler");
			} else {
				procCallCode.append(NULL);
			}
		}
		procCallCode.append(paramCode.toString());
		//procCallCode.append(try_except_handler_jump);
		
		super.append(procCallCode.toString());
		
		// TODO JL terrible hack to prevent serialiser generation for printAny
		// reset in_print_proc_call field when leaving 
		if(the_fun.baseName().equals("printAny")){
			in_print_proc_call = false;
		}

		// TODO JL Space Tracking
		ICode container_stack = Cgen.get_instance().findEnclosingDelcarationContainer();
		int proc_call_overhead = MSP430Sizes.procedureCallOverhead(ft.getArgs());
		int proc_stack_usage = Function.getFunctionStackSize(the_fun);
		container_stack.track_call_space( proc_call_overhead + proc_stack_usage	);

	}


	public void setInTryBlock(boolean in_try_block) {
		this.in_try_block = in_try_block;
	}

	public static boolean isIn_print_proc_call() {
		return in_print_proc_call;
	}
}
