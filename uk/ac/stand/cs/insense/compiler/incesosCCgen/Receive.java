package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import uk.ac.stand.cs.insense.compiler.cgen.IReceive;
import uk.ac.stand.cs.insense.compiler.cgen.IStructDeclaration;
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

public class Receive extends Decl implements IReceive {
	
	private STEntry channelSTE;
	private int fromContext;

	public Receive( STEntry lhs, STEntry rhs, int fromContext ) {
		super( lhs );
		channelSTE = rhs;
		this.fromContext = fromContext;

	}

	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IReceive#complete()
	 */
	public void complete() {
		
		// generates something like channel_receive( this->input,&this->text, sizeof(StringPNTR) );
		
		// We need to have this here to catch instances such as receive x on y where
		// x is a struct and there is no decl of the struct in the context
		if( ste.getType() instanceof StructType){
			StructValue sv = new StructValue(((StructType) ste.getType()));
			sv.complete();
		}
		
		String channel = channelSTE.contextualName(this.fromContext) ;
		String variable = ste.contextualName(this.fromContext);
		
		ITypeRep vtype = ste.getType();
		
		// If we are receiving a type that is send by reference and copied before 
		// sending we should free off the memory used for the last copy before receiving
		// a pointer to the next copy.

		// Make call to receive function
		super.append("\n"+TAB + "// Make call to receive function \n");
		//super.append(TAB + functionCall("channel_receive", potential_component_deref + channel, AMPERSAND + variable, functionCall( "sizeof", insenseTypeToCTypeName( vtype ) ) ) + SEMI + NEWLINE); // TODO JL remove, not needed for post asynch impl
		super.append(TAB + functionCall("channel_receive", channel, AMPERSAND + variable , "false" /* not ack after recv */) + SEMI + NEWLINE);
		if(vtype instanceof ChannelType){
			super.append(TAB + functionCall("channel_adopt", variable)+ SEMI + NEWLINE);
		}
		Cgen.get_instance().findEnclosingDelcarationContainer().track_call_space(MSP430Sizes.channelReceiveCallOverhead(vtype));
	}
}
