package uk.ac.stand.cs.insense.compiler.incesosCCgen;
import uk.ac.stand.cs.insense.compiler.cgen.IStop;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.symbols.STEntry;

public class Stop extends Code implements IStop {

	String data = "";
	STEntry target = null;
	private int fromContext;
	private static final String stop_function = "component_stop";
	//private static final String end_string = "behaviour_end";

	
	public Stop(int fromContext) {
		super();
		this.fromContext = fromContext;
	}

		
	public void addTarget(STEntry target) {
		this.target = target;	
	}
	
	public void complete() {
		
		String thispointer_string = THIS;

		String target_string = "";
		String caller_process_string = "";
		if( target != null ) { // stop named component
			target_string = target.contextualName(this.fromContext);
		} else { // stop this component
			target_string = thispointer_string;
		}
		super.append(NEWLINE + TAB + functionCall(stop_function, target_string)+ SEMI + NEWLINE);
		if(target != null ) { // stop named component
	//		super.append(TAB + functionCall("PROCESS_WAIT_EVENT_UNTIL", "ev" + SPACE + EQUALSEQUALS_ + "DAL_EVENT_PROC_RETURN" )+ SEMI + NEWLINE);			
		} else if (fromContext != ISymbolTable.FUNCTION){ // TODO JL do we need this, depends on semantics of STOP
			//super.append(TAB + GOTO_ + end_string + SEMI + NEWLINE);
		} else {
		}
	}

}
