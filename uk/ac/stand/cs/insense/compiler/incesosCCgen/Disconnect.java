package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.util.ArrayList;
import java.util.List;

import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.cgen.IDisconnect;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.types.ChannelType;
import uk.ac.standrews.cs.nds.util.Diagnostic;

public class Disconnect extends Code implements ICode, IDisconnect {

	private StringBuffer sb;
	private List<StringBuffer> channel_fragments;
	private int channels_fragments_index;
	private boolean is_inter_node_channel;
	private boolean in_try_block = false;
	private int context;
	
	public Disconnect(int fromContext) {
		super();
		this.context = fromContext;
		sb = new StringBuffer();
		channel_fragments = new ArrayList<StringBuffer>();
		for(int i=0; i<2; i++){
			channel_fragments.add(new StringBuffer());
		}
		channels_fragments_index = 0;
		is_inter_node_channel = false;
	}
	

	public void on(){
		is_inter_node_channel = true;
		channels_fragments_index++;
	}
		
	public void setInTryBlock(boolean in_try_block) {
		this.in_try_block = in_try_block;
	}

	
	public void append(String s){
		channel_fragments.get(channels_fragments_index).append(s);
	}
	
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IConnect#complete()
	 */
	public void complete() {
		sb.append(NEWLINE + GENERATED_FROM + Diagnostic.getMethodInCallChain() +  NEWLINE);
		
		String ex_handler = "";
		if(in_try_block){
			ex_handler = AMPERSAND + "ex_handler";
		} else {
			if(context == ISymbolTable.FUNCTION){
				ex_handler = "ex_handler";
			} else {
				ex_handler = NULL;
			}
		}
		
		if(!is_inter_node_channel){
			sb.append(TAB + "// this channel could  be connected to a remote channel, so need to call wrapper function in the runtime" + NEWLINE);
			sb.append(TAB + "// which will potentially do remote unbinding prior to calling normal channel_unbind in InceOS" + NEWLINE);
			sb.append(TAB + functionCall("remoteAnonymousUnbind_proc", channel_fragments.get(0).toString(), ex_handler) + SEMI + NEWLINE);
		}
		else { // if(is_inter_node_channel){
			sb.append(tab(1) + functionCall("remoteUnbind_proc", channel_fragments.get(1).toString(), channel_fragments.get(0).toString(), ex_handler ) + SEMI + NEWLINE);
		}		
		super.append(NEWLINE + sb.toString());
	}
}
