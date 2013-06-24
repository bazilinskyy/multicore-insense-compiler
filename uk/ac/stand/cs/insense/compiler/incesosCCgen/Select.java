package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.util.ArrayList;
import java.util.List;

import uk.ac.stand.cs.insense.compiler.cgen.IDecl;
import uk.ac.stand.cs.insense.compiler.cgen.IDeclarationContainer;
import uk.ac.stand.cs.insense.compiler.cgen.ISelect;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.symbols.STEntry;
import uk.ac.stand.cs.insense.compiler.types.BooleanType;
import uk.ac.stand.cs.insense.compiler.types.ByteType;
import uk.ac.stand.cs.insense.compiler.types.ChannelType;
import uk.ac.stand.cs.insense.compiler.types.EnumType;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;
import uk.ac.stand.cs.insense.compiler.types.IntegerType;
import uk.ac.stand.cs.insense.compiler.types.RealType;
import uk.ac.stand.cs.insense.compiler.types.StructType;

public class Select extends Code implements ISelect { // TODO JL needs updating for InceOS

	boolean first = true;
	boolean whenpart = false;
	boolean acknowledgeLastChannelReceipt = false;
	boolean ird = false;	// generated an if_ready_deliver()
	private int fromContext;
	String select_start =  NEWLINE + LCB_ + "// Start of Select" + NEWLINE;

	String select_end = RCB_ + "// End of Select" + NEWLINE;		

	private String channel_name = "";
	private int num_channels = 0;
	private String guard_string = "";
	private String var_name = "";
	private IDecl decl;
	private ITypeRep channelType = null;
	private List<ITypeRep> arm_types;
	private ITypeRep maxSizeChannelType = null;
	private boolean involves_pointer_type = false;
	
	private static final String START_GUARD_CLAUSE = "start_guard_clause";
	
	// records C code that constructs the guard_ready_set 
	private StringBuffer guard_ready_set_code = new StringBuffer(); 

	private int branch_number = 0; 
	
	// have_default_block is a parameter to the run-time select function
	// is set to true if default block has been defined for this Select
	private boolean have_default_block = false;


	
	public Select(int fromContext) {
		super();
		this.fromContext = fromContext;
		arm_types = new ArrayList<ITypeRep>();
		decl = null;
	}
	
	private void addToGuardReadySetCode(String channel_name, int branch_number, String guard_string, boolean acknowledge){
		this.channel_name = channel_name;
		guard_ready_set_code.append( TAB +"if(" + guard_string + ") _chans[_i++] = " + channel_name + SEMI + NEWLINE);
   		guard_ready_set_code.append( TAB + functionCall("set_in_ack_after", channel_name, (acknowledge?"true":"false") ) + SEMI + NEWLINE);
	}
	
	public void defaultArm() {
		have_default_block =  true;
		if( ! first ) {
			if(decl.getSymbolTableEntry().getType().isPointerType()){
				append(tab(2) + functionCall("DAL_assign", AMPERSAND + decl.getSymbolTableEntry().getName(), "NULL") + SEMI + NEWLINE);
			}
			if(acknowledgeLastChannelReceipt){
				append(TAB + TAB + functionCall("channel_acknowledge", channel_name, "GENERAL_SUCCESS_EVENT") + SEMI + NEWLINE);
			}
			append(TAB + RCB_);
		}
		first = false;
		append( NEWLINE + TAB + "if" + LRB + branchIndexVarName() + SPACE + EQUALSEQUALS_ + "SELECT_DEFAULT" + RRB_ + LCB_ + NEWLINE + TAB);
	}

	public void from(IDecl decl ) {
		num_channels++;
		this.decl = decl;
		var_name = decl.getSymbolTableEntry().getName();
		channelType = decl.getType();
		arm_types.add(channelType);
		if(channelType.isPointerType()){
			involves_pointer_type = true;
		}
		if(maxSizeChannelType == null){
			maxSizeChannelType = channelType;
		} else if(channelType.getCValueSizeIndicator() > maxSizeChannelType.getCValueSizeIndicator()){
			maxSizeChannelType = channelType;
		}
		if( channelType instanceof StructType){
			StructValue sv = new StructValue(((StructType) channelType));
			sv.complete();
		}

	}

	public void receiveArm(boolean acknowledge) {
		whenpart = false;
		if(!first){
			if(decl.getSymbolTableEntry().getType().isPointerType()){
				append(tab(2) + functionCall("DAL_assign", AMPERSAND + decl.getSymbolTableEntry().getName(), "NULL") + SEMI + NEWLINE);
			}
			if(acknowledgeLastChannelReceipt){
				append(TAB + TAB + functionCall("channel_acknowledge", channel_name, "GENERAL_SUCCESS_EVENT") + SEMI + NEWLINE);
			}
			append(TAB + RCB_);
		}
		acknowledgeLastChannelReceipt = acknowledge;
		first = false;
		branch_number++;
	}

	// called from syntax analyser on WHEN symbol
	// gets channel name
	// sets delimiter for start of guard clause
	public void when() {
		whenpart = true;
		channel_name = pop();
		append(START_GUARD_CLAUSE);
	}
	
	// called from syntax analyser when COLON is next symbol
	//   if no when clause, sets guard to true and pops channel name
	//   otherwise pops when clause stopping at delimiter setup by when()
	// assigns result of select call to variable depending on type
	public void selectExp(boolean acknowledge) {
		if( whenpart == false ) {
			channel_name = pop();
			guard_string = "true";
		} else {
			guard_string = "";
			String guard_fragment = "";
			guard_fragment = pop();
			while(!guard_fragment.equals(START_GUARD_CLAUSE)){
				guard_string = guard_fragment + guard_string;
				guard_fragment = pop();
			}
		}
		append( NEWLINE + TAB + "if" + LRB + branchIndexVarName() + SPACE + EQUALSEQUALS_ + channel_name + RRB_ + LCB_ + NEWLINE);
		addToGuardReadySetCode(channel_name , branch_number, guard_string, acknowledge);
		super.append(tab(2) + insenseTypeToCTypeName(channelType) + var_name + " = " + getSelectResult() + ";\n" + tab(2));
		if(channelType instanceof ChannelType){
			super.append(tab(2) + functionCall("channel_adopt", var_name)+ SEMI + NEWLINE);
		}

	}

	private String branchIndexVarName(){
		return "_choice";
	}
	
	private String getSelectResult(){
		// The conditional is used to get hold of the value 
		// returned in the select struct buffer
		String pointer_casting = "*((void **)";
		String buffer = " _s.buffer)";
		String casting = pointer_casting;
		if( channelType instanceof BooleanType ) {
			casting = "*((bool *)";
		} else if( channelType instanceof ByteType ) {
			casting = "*((uint8_t *)";
		} else if( channelType instanceof RealType ) {
			casting = "*((float *)";
		} else if( channelType instanceof IntegerType ) {
			casting = "*((int *)";
		} else if( channelType instanceof EnumType ) {
			casting = "*((int *)";
		} 
		String selectResult = casting + buffer;
		return selectResult;
	}
	
	
	private String preambleCode(){
		StringBuffer sb = new StringBuffer();
		sb.append(select_start);
		sb.append(TAB + "// decls and initilisation needed by select" + NEWLINE);
		sb.append(TAB + "struct select_struct _s" + SEMI + NEWLINE);
		sb.append(TAB + "unsigned char _buffer[" + getBufferSize() + "]" + SEMI + NEWLINE);
		sb.append(TAB + "chan_id _chans[" + num_channels + "]" + SEMI + NEWLINE);
		sb.append(TAB + "int _i = 0" + SEMI + NEWLINE);
		return sb.toString();
	}
	
	private String getBufferSize(){
		return functionCall("sizeof", insenseTypeToCTypeName(maxSizeChannelType));
	}
	
	private String branchSelectIfStatementEndCode(){
		StringBuffer sb = new StringBuffer();
		if(!first && !have_default_block){
			if(decl.getSymbolTableEntry().getType().isPointerType()){
				sb.append(tab(2) + functionCall("DAL_decRef", decl.getSymbolTableEntry().getName()) + SEMI + NEWLINE);
			}
			if(acknowledgeLastChannelReceipt){
				sb.append(TAB + TAB + functionCall("channel_acknowledge", channel_name, "GENERAL_SUCCESS_EVENT") + SEMI + NEWLINE);
			}
		}
		sb.append(TAB + RCB_ + NEWLINE);
		return sb.toString();
	}
	
	// not needed anymore as "PROCESS_PAUSE();" is executed at end of behaviour loop now
	private String postambleCode(){
		StringBuffer sb = new StringBuffer();
		//sb.append(TAB + "// if default arm executed, temporarily yield selector process to avoid cpu hogging\n");
		//sb.append(TAB + "if(" + branchIndexVarName() + " == SELECT_DEFAULT) component_yield();\n");
		sb.append(select_end);
		return sb.toString();	
	}
	
	
	private String selectStructFieldInitialisers(){
		StringBuffer sb = new StringBuffer();	
		sb.append(TAB + "_s.have_default" + SPACE + EQUALS_ + (have_default_block?"true":"false") + SEMI + NEWLINE);
		sb.append(TAB + "_s.chans" + SPACE + EQUALS_ + "_chans" + SEMI + NEWLINE);
		sb.append(TAB + "_s.buffer" + SPACE + EQUALS_ + "_buffer" + SEMI + NEWLINE);
		sb.append(TAB + "_s.nchans" + SPACE + EQUALS_ + "_i" + SEMI + NEWLINE);
		return sb.toString();
	}
	
	public void complete() {
		StringBuffer sb = new StringBuffer();
		sb.append(preambleCode());
		sb.append(guard_ready_set_code.toString());
		sb.append(selectStructFieldInitialisers());
		sb.append(selectCallCode());
		sb.append(super.toString()); 
		sb.append(branchSelectIfStatementEndCode());
		sb.append(postambleCode());
		Cgen.get_instance().findEnclosingDelcarationContainer().track_call_space(MSP430Sizes.channelSelectCallOverhead(arm_types));
		super.reset(sb.toString());		
	}
	
	
	private String selectCallCode(){		
		StringBuffer sb = new StringBuffer();

		sb.append(NEWLINE + TAB + "// Call select function, will return result & chan_id of branch to execute" + NEWLINE);
		sb.append(TAB + "chan_id " + branchIndexVarName() + SPACE + EQUALS_ + functionCall("channel_select", AMPERSAND + "_s") + SEMI + NEWLINE); 
		return sb.toString();
	}

}
