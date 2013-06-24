package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import uk.ac.stand.cs.insense.compiler.cgen.IFunction;
import uk.ac.stand.cs.insense.compiler.cgen.IThrow;

public class Throw extends Code implements IThrow {

	IFunction proc;
	private boolean in_try_block = false;
	StringBuffer appendBuffer;
	String exception;
	
	public Throw(IFunction fromProc, String exception){
		this.proc = fromProc;
		this.exception = exception;
		appendBuffer = new StringBuffer();

		if(proc != null){
			proc.setThrowsException(true);
		}
	}
	
	public void append( String s ) {
		appendBuffer.append(s);
	}
	
	public void setInTryBlock(boolean in_try_block){
		this.in_try_block = in_try_block;
	}
	
	public void complete() {
		String ex_handler = "ex_handler";
		if(!in_try_block){
			ex_handler = STAR + ex_handler; 
		}
		super.append(TAB);
		if(!in_try_block){
			super.append(IF + LRB + "ex_handler" + RRB + SPACE );
		}
		super.append(functionCall("longjmp", ex_handler, exception) );
		super.append(appendBuffer.toString());		
		super.append(SEMI + NEWLINE);
	}
	
	public IFunction getProc() {
		return proc;
	}

}
