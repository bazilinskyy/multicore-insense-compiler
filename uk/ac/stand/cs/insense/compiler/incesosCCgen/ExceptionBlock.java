package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.util.HashMap;

import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.cgen.IExceptionBlock;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;

/**
 * 
 * @author al
 *	9/5/07
 */
public class ExceptionBlock extends Code implements ICode,IExceptionBlock  {

	private static String label = null;
	private static String end_prefix = "end";
	private static boolean in_exception_block = false;
	private boolean first_except_clause;
	private boolean in_try_clause;
	private boolean in_function;
	private int fromContext;
	private boolean last_in_exception_block;                                     // remembers whether already in an exception block
	private static final String AN_EXCEPTION_LABEL = "OperationFailedException"; // one of the pre-defined exception labels

	public static String getAN_EXCEPTION_LABEL() {
		return AN_EXCEPTION_LABEL;
	}


	public ExceptionBlock(int fromContext) {
		super();
		this.fromContext = fromContext;
		if(fromContext == ISymbolTable.FUNCTION){
			in_function = true;
		} else {
			in_function = false;
		}
		label = generate_unique_id();
		last_in_exception_block = in_exception_block;
		in_exception_block = true;
		first_except_clause = true;
		in_try_clause = true;
		append( NEWLINE + LCB_ + TAB + "// Start of try block" + NEWLINE);
		append( TAB + "jmp_buf ex_handler" + SEMI + NEWLINE);
		append( TAB + IF + LRB + SPACE + LRB + "op_status" + SPACE + EQUALS_ + functionCall("setjmp", "ex_handler") + RRB + SPACE + EQUALSEQUALS_ + ZERO + RRB + LCB_ + NEWLINE);
	}
	

	public void onExcept(String exception) {
		in_try_clause = false;
		if(first_except_clause) {
			append(TAB + RCB_ + SPACE + ELSE_ + SPACE + LCB_ + NEWLINE + TAB);
			first_except_clause = false;
		} else {
			append(TAB + RCB_ + ELSE_);
		}
		append(IF + LRB + "op_status" + SPACE + EQUALSEQUALS_ + exception + RRB + LCB_ + NEWLINE);
	}

	
	public void complete() {
		super.complete();
		if(in_function){
			append( TAB + RCB_ + ELSE_ + LCB_ + "// don't have handler for this exception here" + NEWLINE);
			append( TAB + TAB + GOTO_ + label + SEMI  + SPACE + "// to throw exception to caller" + NEWLINE);
		}
		append( TAB + RCB_ + NEWLINE);
		append( TAB + RCB_ + "// End of local exception handler(s)" + NEWLINE);
		append( RCB_ + NEWLINE);
		if(in_function){
			append(TAB + GOTO_ + end_prefix + label + SEMI + "// either no exception occurred or we have dealt with exception above, so do not pass it up to the caller" + NEWLINE);
			append( label + COLON + TAB + "// throw exception to caller if caller defined a handler" + NEWLINE);
			append( TAB + IF + LRB + "ex_handler" + RRB + SPACE + functionCall("longjmp", STAR + "ex_handler", "op_status") + SEMI + NEWLINE);
		}
		if(in_function){
			append(end_prefix + label + COLON);
		}
		append("// End of try-except block" + NEWLINE);
		in_exception_block = last_in_exception_block;
	}
	
	public static boolean inExceptionBlock() {
		return in_exception_block;
	}
	
	public static String getLabel() {
		return label;
	}
	
}
