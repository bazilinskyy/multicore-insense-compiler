package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.cgen.IPublish;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.types.ChannelType;

public class Publish extends Code implements ICode, IPublish {

	private String try_except_handler_jump = "";
	private boolean in_try_block = false;
	private ISymbolTable context;
	private ChannelType ct;

	
	public Publish(ISymbolTable fromContext) {
		super();
		this.context = fromContext;
		append("\tpublishChannelAs_proc" + LRB_ + SPACE);

	}
	
	public void setInTryBlock(boolean in_try_block) {
		this.in_try_block = in_try_block;
	}

	
	public void as(ChannelType ct) {
		this.ct = ct;
		if(ct.getDirection() == ChannelType.OUT){
			append(COMMA + SPACE + "OUT_DIR");
		} else {
			append(COMMA + SPACE + "IN_DIR");
		}
		append( COMMA + SPACE);
	}

	public void complete() {
		append( COMMA + SPACE + functionCall("Construct_String0", "\"" + ct.toStringRep() + "\""));
		append(COMMA + SPACE);
		if(in_try_block){
			append(AMPERSAND + "ex_handler");
		} else {
			if(context.getContext() == ISymbolTable.FUNCTION){
				append("ex_handler");
			} else {
				append(NULL);
			}
		}
		append( RRB_ + SEMI + NEWLINE);
	}

}
