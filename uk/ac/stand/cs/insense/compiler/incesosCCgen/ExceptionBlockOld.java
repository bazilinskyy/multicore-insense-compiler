package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.cgen.IExceptionBlock;

/**
 * 
 * @author al
 *         9/5/07
 */
public class ExceptionBlockOld extends Code implements ICode, IExceptionBlock {

	private static String label = null;
	private final String last_label;							                      // holds last label used
	private static boolean in_exception_block = false;
	private boolean first_except_clause;
	private boolean in_try_clause;
	private final boolean last_in_exception_block;                                     // remembers whether already in an exception block
	private static final String AN_EXCEPTION_LABEL = "OperationFailedException"; // one of the pre-defined exception labels

	public static String getAN_EXCEPTION_LABEL() {
		return AN_EXCEPTION_LABEL;
	}

	/**
	 * Here is how it works -
	 * For code
	 * try {
	 * XXXX
	 * } except {
	 * YYY
	 * }
	 * 
	 * The inline code that is generated is:
	 * XXXX
	 * goto handler;
	 * 
	 * handler:
	 * YYY
	 * 
	 */

	public ExceptionBlockOld() {
		super();
		last_label = label;
		label = generate_unique_id();
		last_in_exception_block = in_exception_block;
		in_exception_block = true;
		first_except_clause = true;
		in_try_clause = true;
		append(NEWLINE + LCB_ + TAB + "// Start of try block" + NEWLINE);
		// append( TAB + "inceos_event_t op_status;");
	}

	public String getHandlerJumpInstruction() {
		if (in_try_clause)
			return IF + LRB + SPACE + functionCall("is_exception_event", "op_status") + RRB + SPACE + GOTO_ + label + SEMI + NEWLINE;
		return "";
	}

	// we don't use different exception types for now, but may in future
	public void onExcept() {
		onExcept("GENERAL_EXCEPTION_EVENT");
	}

	@Override
	public void onExcept(String exception) {
		in_try_clause = false;
		if (first_except_clause) {
			append(TAB + "// END of try block" + NEWLINE);
			append(TAB + "// No exception occurred if we get here, so skip past exception handler(s)" + NEWLINE);
			append(TAB + GOTO_ + END + label + SEMI + NEWLINE);
			append(TAB + "// Start of exception handler(s)" + NEWLINE);
			append(label + COLON + NEWLINE);
			first_except_clause = false;
		} else {
			append(TAB + "// Have dealt with this exception, cannot be any other exception, skip past exception handler(s)" + NEWLINE);
			append(TAB + GOTO_ + END + label + SEMI + NEWLINE);
			append(TAB + RCB_ + NEWLINE);
		}
		append(TAB + "// Start new except block" + NEWLINE);
		append(TAB + IF + LRB + "op_status" + SPACE + EQUALSEQUALS_ + exception + RRB + LCB_ + NEWLINE);
	}

	@Override
	public void complete() {
		super.complete();
		append(TAB + RCB_ + NEWLINE);
		append(TAB + "// End of exception handler(s)" + NEWLINE);
		append(END + label + COLON + NEWLINE + RCB_ + NEWLINE);
		label = last_label;
		in_exception_block = last_in_exception_block;
	}

	public static boolean inExceptionBlock() {
		return in_exception_block;
	}

	public static String getLabel() {
		return label;
	}

}
