package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.util.ArrayList;
import java.util.List;

import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.cgen.IConnect;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.types.ChannelType;
import uk.ac.standrews.cs.nds.util.Diagnostic;

public class Connect extends Code implements ICode, IConnect {

	private StringBuffer sb;
	private List<StringBuffer> channel_fragments;
	private int channels_fragments_index;
	private boolean[] is_inter_node_channel;
	private ChannelType[] channel_type;
	private int channel_index;
	private boolean in_try_block = false;
	private int context;

	public Connect(int fromContext) {
		super();
		this.context = fromContext;
		sb = new StringBuffer();
		channel_fragments = new ArrayList<StringBuffer>();
		for(int i=0; i<4; i++){
			channel_fragments.add(new StringBuffer());
		}
		channels_fragments_index = 0;
		is_inter_node_channel = new boolean[2]; // initialises to false
		channel_type = new ChannelType[2];
		channel_index = 0;
	}


	public void on(){
		is_inter_node_channel[channel_index] = true;
		channels_fragments_index++;
	}

	public void to(){
		channels_fragments_index++;
		channel_index++;
	}

	public void setChannelType(ChannelType ct){
		channel_type[channel_index] = ct;
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

		ICode container_stack = Cgen.get_instance().findEnclosingDelcarationContainer();
		int connect_call_overhead = MSP430Sizes.sysFunctionCallOverhead(5*MSP430Sizes.WORD_SIZE);
		int connect_stack_usage = MSP430Sizes.channelBindCallOverhead();
		
		if(!is_inter_node_channel[0] && is_inter_node_channel[1]){
			sb.append(TAB + functionCall("remoteAnonymousBind_proc", 
					channel_fragments.get(0).toString(), 
					channel_fragments.get(2).toString(),
					channel_fragments.get(1).toString(),
					functionCall("Construct_String0", "\"" + channel_type[0].toStringRep() + "\""),
					ex_handler)
					+ SEMI + NEWLINE);
			connect_stack_usage = MSP430Sizes.INCH_CONNECT_STACK_USE;
		}
		else if(is_inter_node_channel[0] && !is_inter_node_channel[1]){
			sb.append(TAB + functionCall("remoteAnonymousBind_proc", 
					channel_fragments.get(2).toString(), 
					channel_fragments.get(1).toString(),
					channel_fragments.get(0).toString(),
					functionCall("Construct_String0", "\"" + channel_type[1].toStringRep() + "\""),
					ex_handler)
					+ SEMI + NEWLINE);
			connect_stack_usage = MSP430Sizes.INCH_CONNECT_STACK_USE;
			container_stack.track_call_space(connect_call_overhead + connect_stack_usage);
		}
		else if(is_inter_node_channel[0] && is_inter_node_channel[1]){
			sb.append(TAB + "// Connect 2 remote public channels" + NEWLINE);
			sb.append(TAB + functionCall("remoteBindRemotely_proc", 
					channel_fragments.get(1).toString(), 
					channel_fragments.get(0).toString(), 
					channel_fragments.get(3).toString(),
					channel_fragments.get(2).toString(),
					ex_handler)
					+ SEMI + NEWLINE);
			connect_stack_usage = MSP430Sizes.INCH_REMOTE_CONNECT_STACK_USE;
			container_stack.track_call_space(connect_call_overhead + connect_stack_usage);
		}
		else if(!(is_inter_node_channel[0] || is_inter_node_channel[1])){
			// Both channels are local
			sb.append(TAB + functionCall("channel_bind", channel_fragments.get(0).toString(), channel_fragments.get(1).toString()) + SEMI + NEWLINE);
		}
		
		container_stack.track_call_space(connect_call_overhead + connect_stack_usage);
		super.append(NEWLINE + sb.toString());
	}

}
