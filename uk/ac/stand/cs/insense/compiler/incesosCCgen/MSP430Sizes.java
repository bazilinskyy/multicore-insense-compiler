package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.util.List;
import uk.ac.stand.cs.insense.compiler.types.AnyType;
import uk.ac.stand.cs.insense.compiler.types.ArrayType;
import uk.ac.stand.cs.insense.compiler.types.BooleanType;
import uk.ac.stand.cs.insense.compiler.types.ByteType;
import uk.ac.stand.cs.insense.compiler.types.ChannelType;
import uk.ac.stand.cs.insense.compiler.types.ComponentType;
import uk.ac.stand.cs.insense.compiler.types.EnumType;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;
import uk.ac.stand.cs.insense.compiler.types.IntegerType;
import uk.ac.stand.cs.insense.compiler.types.RealType;
import uk.ac.stand.cs.insense.compiler.types.StringType;
import uk.ac.stand.cs.insense.compiler.types.StructType;
import uk.ac.stand.cs.insense.compiler.types.UnsignedIntegerType;
import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
import uk.ac.standrews.cs.nds.util.Diagnostic;
import uk.ac.standrews.cs.nds.util.ErrorHandling;

/**
 * @author jonl
 *
 */
/**
 * @author jonl
 *
 */
public abstract class MSP430Sizes {
	
	// These are all MSP430 SIZES
	public static final int HWORD_SIZE = 1;                    // half word size
	public static final int WORD_SIZE  = 2;                    // word size 
	public static final int DWORD_SIZE = 4;                    // double word size

	public static final int BYTE_SIZE = HWORD_SIZE;
	public static final int INT_SIZE  = WORD_SIZE;
	public static final int UNSIGNED_INT_SIZE  = INT_SIZE;
	public static final int REAL_SIZE = DWORD_SIZE;
	public static final int ENUM_SIZE = INT_SIZE;
	public static final int BOOL_SIZE = INT_SIZE;
	public static final int PNTR_SIZE = WORD_SIZE;
	
	public static final int INCH_REMOTE_CONNECT_STACK_USE = 100;
	public static final int INCH_CONNECT_STACK_USE = 80;
	public static final int INCH_REMOTE_DISCONNECT_STACK_USE = 100;
	public static final int INCH_DISCONNECT_STACK_USE = 80;
	
	
	public static final int CALL_SUB_OVERHEAD = 2*WORD_SIZE;    // call sub entails pushing PC+2, FP
	public static final int MAX_REGISTER_ARGUMENT_SIZE = 4*WORD_SIZE; // up to 4 words can be passed to a subroutine in registers
	
	public static final int UART_INTERRUPT = 14;      // CC2420 Receive Transmit interrupt
	
	public static final int INTERRUPT_OVERHEAD = UART_INTERRUPT; // + ....
	
	private static final int MAX_ANYTYPE_DCREF_NESTING = 5; // decRef->any->array->struct->string
	private static final int MAX_COMPONENTTYPE_DECREF_NESTING = MAX_ANYTYPE_DCREF_NESTING + 1; // decRef->component->any->...

	
	
	
	/**
	 * @param args the args of which to find the maximum value
	 * @return the maximum integer value
	 * General Purpose max(...) function to find the maximum integer value in a list of integers
	 */
	public static int maxValueOf(int... args){
		int max = 0;
		for(int i=0; i<args.length; i++){
			if(args[i] > max) max = args[i];
		}
		return max;
	}
	

	/**
	 * @param types a list of types of which to find the maximum size value
	 * @return the maximum size of the given types
	 */
	public static int maxValueOf(List<ITypeRep> types){
		int[] type_sizes = new int[types.size()];
		return maxValueOf(type_sizes);
	}
	

	
	/*** Generic Type Size Methods ***/

	/**
	 * @param tr a type representation 
	 * @return the estimated size for the type
	 */
	public static int type_size(ITypeRep tr){
		if( tr instanceof BooleanType ) {
			return INT_SIZE ;
		} else if( tr.equals( ByteType.TYPE ) ) {
			return BYTE_SIZE ;
		} else if( tr.equals( RealType.TYPE ) ) {
			return REAL_SIZE ;
		} else if( tr.equals( IntegerType.TYPE ) ) {
			return INT_SIZE ;
		} else if( tr.equals( UnsignedIntegerType.TYPE ) ) {
			return UNSIGNED_INT_SIZE ;
		} else if( tr instanceof ChannelType ) {
			return INT_SIZE ;
		} else if( tr instanceof EnumType ) {
			return ENUM_SIZE ;
		}  else if( tr.isPointerType() ) {
			return PNTR_SIZE ;
		} else {
			ErrorHandling.hardError( "Found unrecognised type in add_stack_element" + tr.toStringRep() );
			return -1;
		}
	}

	/**
	 * @param types a list of type representations
	 * @return the estimated size for all types
	 */
	public static int sum_type_sizes(List<ITypeRep> types){
		int sum = 0;
		for(ITypeRep tr :types){
			sum += type_size(tr);
		}
		return sum;
	}
	

	/*** Generic function and constructor call overhead estimation methods ***/
	
	
	/**
	 * @param argument_size the summed size of all arguments passed to a function
	 * @return the estimated stack overhead from making the call
	 * This method assumes that MAX_REGISTER_ARGUMENT_SIZE bytes can be passed to a 
	 * subroutine in registers, remaining arguments are passed on the stack.
	 * It also assumes the worst case that all arguments may be modified by the 
	 * callee and must consequently be stored on the callee's stack.
	 * There is also an overhead associated with calling a subroutine which 
	 * requires the return address and frame pointer to be stored on the stack.
	 */
	public static int functionCallOverhead(int total_argument_size){
		int stack_argument_size = 0;
		int register_argument_size = 0;
		if(total_argument_size <= MAX_REGISTER_ARGUMENT_SIZE){
			register_argument_size = total_argument_size;
		} else {
			register_argument_size = MAX_REGISTER_ARGUMENT_SIZE;
			stack_argument_size = total_argument_size - MAX_REGISTER_ARGUMENT_SIZE;
		}
		return CALL_SUB_OVERHEAD + register_argument_size + 2 * stack_argument_size;
	}
	
	
	/**
	 * @param args the arguments to the function as a list of ITypeRep types
	 * @return the estimated stack overhead of a function call given a list of argument types
	 */
	public static int functionCallOverhead(List<ITypeRep> args){
		return functionCallOverhead(sum_type_sizes(args));
	}
	
	/**
	 * @param the total size of the proceudre arguments
	 * @return the estimated stack overhead of the procedure call given the total argument size
	 * Insense procedures all take a <code> void *this </code> and <code>jmp_buf *op_status</code> 
	 * pointers in addition to the Insense procedure arguments.
	 * This method returns the estimated stack overhead of calling a function 
	 * with the given args and an additional <code>void *this </code> and <code>jmp_buf *op_status</code> 
	 * pointer argument.
	 */
	public static int procedureCallOverhead(int total_argument_size){
		return functionCallOverhead(2*PNTR_SIZE + total_argument_size);
	}
	
	/**
	 * @param args the arguments to the Insense procedure as a list of ITypeRep types
	 * @return the estimated stack overhead of the procedure call given a list of argument types
	 * Insense procedures all take a <code> void *this </code> pointer in addition to the Insense
	 * procedure arguments. This method returns the estimated stack overhead of calling a function 
	 * with the given args and an additional <code>void *this </code> pointer argument.
	 */
	public static int procedureCallOverhead(List<ITypeRep> args){
		return procedureCallOverhead(sum_type_sizes(args));
	}

	
	/**
	 * @param size the size of the arguments passed to the system function
	 * @return the estimated stack overhead from making the system function call
	 */
	public static int sysFunctionCallOverhead(int size){
		return functionCallOverhead(size) + functionCallOverhead(0); // additional call due to disable interrupts/preemption... 
	}

	
	/**
	 * @param args the arguments to the Insense constructor as a list of ITypeRep types
	 * @return the overhead of a component constructor call given a list of argument types
	 */
	public static int componentConstructorCallOverhead(List<ITypeRep> args){
//		Diagnostic.trace(DiagnosticLevel.FINAL, "argvArrayFunctionOverhead: " + argvArrayFunctionOverhead(args));
//		Diagnostic.trace(DiagnosticLevel.FINAL, "componentCreateCallOverhead: " + componentCreateCallOverhead());
//		Diagnostic.trace(DiagnosticLevel.FINAL, "constructorFunctionCallOverhead: " + constructorFunctionCallOverhead(args));
		return maxValueOf(argvArrayFunctionOverhead(args), componentCreateCallOverhead() + constructorFunctionCallOverhead(args)); 
	}

	
	
	/**
	 * @param args the constructor arguments as a list of ITypeReps
	 * @return the stack overhead of calling the argv array constructor 
	 */
	public static int argvArrayFunctionOverhead(List<ITypeRep> args){
		if(args.size() > 0){
			int array_function_decl_sizes = 0;
			for(ITypeRep tr : args){
				if(!tr.isPointerType()){
					array_function_decl_sizes += PNTR_SIZE; // local variables int*, float*, etc. are decld for non-pointer-type constructor args
				}
			}
			return functionCallOverhead(sum_type_sizes(args)) + array_function_decl_sizes + mallocCallOverhead();
		}
		return 0; // the argvArrayFunction is not called for zero-argument constructor
	}

	

	/***   Specialised size estimation methods for system and runtime   ***/
	
	
	/**
	 * @return estimated stack size overhead from generating an any
	 */
	public static int anyTypeConstructCallOverhead(ITypeRep arg){
		return functionCallOverhead(type_size(arg) + PNTR_SIZE) + functionCallOverhead(0) + PNTR_SIZE + dalAllocCallOverhead();
	}
	
	/**
	 * @param arms the arms of the project clause
	 * @return the estimated size of projecting onto the specified arms
	 */
	public static int anyTypeProjectOverhead(List<ITypeRep> arms){
		return maxValueOf(anyTypeIsEqualCallOverhead(), anyTypeProjectionVariableOverhead(arms) + anyTypeGetCallOverhead());
	}
	
	/**
	 * @return the estimated stack overhead of calling the anyTypeIsEqual function
	 */
	public static int anyTypeIsEqualCallOverhead(){
		return functionCallOverhead(PNTR_SIZE * 2) + strcmpCallOverhead();
	}
	
	/**
	 * @param arms the arm types in a project clause
	 * @return the estimated stack overhead of declaring a variable <code>val<code> when compiling <code>project anyVal as val onto ...</code> 
	 */
	public static int anyTypeProjectionVariableOverhead(List<ITypeRep> arms){
		return maxValueOf(arms);
	}
	
	/**
	 * @return the estimated stack overhead of calling the anyTypeGet function to get the appropriate value out of the union
	 */
	public static int anyTypeGetCallOverhead(){
		return functionCallOverhead(PNTR_SIZE);
	}
	
	
	/**
	 * @param dimensionality the dimensionality of the array
	 * @return the estimated stack overhead of constructing an array with this dimensionality
	 */
	public static int arrayConstructorCallOverhead(int dimensionality){
		return functionCallOverhead(dimensionality * INT_SIZE + INT_SIZE) + maxValueOf(dimensionality * (PNTR_SIZE + arrayGenericConstructorCallOverhead()), arrayDerefFunctionCallOverhead(dimensionality) );  
	}
	
	/**
	 * @return the estimated stack overhead of calling the generic runtime array constructor function 
	 */
	public static int arrayGenericConstructorCallOverhead(){
		return functionCallOverhead(2*INT_SIZE + PNTR_SIZE + BOOL_SIZE) + PNTR_SIZE + maxValueOf(dalAllocCallOverhead(), dalAssignCallOverHeadNoDecRef(), INT_SIZE + PNTR_SIZE + maxValueOf(memncpyCallOverhead(), dalModRefIncCallOverhead()));
	}
	
	/**
	 * @param dimensionality the dimensionality of the array to dereference
	 * @return the estimated stack overhead of dereferencing the array using the runtime array_loc function
	 */
	public static int arrayDerefFunctionCallOverhead(int dimensionality){
		return functionCallOverhead(PNTR_SIZE + INT_SIZE + BOOL_SIZE);
	}
	
	/**
	 * @return estimated stack overhead of calling the behaviour function
	 * Component behaviour functions are single argument functions of the form behaviour_ZZZ(void *this)
	 * so we need stack space to pass this to the behaviour and stack space to store this in the behaviour
	 * context, in case this is modified by the behaviour. Strictly speaking the stack space for 
	 * passing this to the behaviour is not part of the component's stack usage, but is left here
	 * for now as an overestimate.
	 */
	public static int behaviourFunctionCallOverhead(){
		return functionCallOverhead(PNTR_SIZE);
	}
	
	
	/**
	 * @return estimated stack overhead of making a system call to channel_bind
	 */
	public static int channelBindCallOverhead(){
		return sysFunctionCallOverhead(2*INT_SIZE);
	}
	
	/**
	 * @return the estimated stack overhead from calling channel_create(int direction)
	 */
	public static int channelCreateCallOverhead(){
		return sysFunctionCallOverhead(2 * INT_SIZE + BOOL_SIZE) ;
	}

	/**
	 * @return estimated stack overhead of making a system call to channel_send
	 */
	public static int channelSendCallOverhead(ITypeRep tr){
		int decl_size = type_size(tr);
		int send_call_size = sysFunctionCallOverhead(INT_SIZE + 2 * PNTR_SIZE);
		int inc_ref_size = 0;
		if(tr.isPointerType()){
			inc_ref_size = dalModRefIncCallOverhead();
		}
		return maxValueOf( decl_size + send_call_size , inc_ref_size);
	}
	
	/**
	 * @return the estimated stack overhead from calling channel_receive
	 */
	public static int channelReceiveCallOverhead(ITypeRep tr){
		int decl_size = type_size(tr);
		int recv_call_size = sysFunctionCallOverhead(INT_SIZE + 2 * PNTR_SIZE);
		int decref_call_size = 0;
		if(tr.isPointerType()){
			decref_call_size = dalDecRefCallOverhead(tr);
		}
		return maxValueOf( decl_size + recv_call_size , decref_call_size);
	}


	/**
	 * @return the estimated stack overhead from calling channel_select
	 */
	public static int channelSelectCallOverhead(List<ITypeRep> arm_types){
		int select_struct_overhead = 4 * WORD_SIZE;
		int select_call_size = sysFunctionCallOverhead(PNTR_SIZE);
		int max_gc_size = 0;
		int max_decl_size = 0;
		ITypeRep max_sized_type = null;
		for(ITypeRep tr : arm_types){
			int type_size = type_size(tr);
			if(type_size > max_decl_size){
				max_decl_size = type_size;
				max_sized_type = tr;
			}
			if(tr.isPointerType()){
				int gc_size = maxValueOf(dalAssignCallOverHead(tr) , dalDecRefCallOverhead(tr));
				if(gc_size > max_gc_size){
					max_gc_size = gc_size;
				}
			}
		}
		int select_struct_buffer_overhead = max_decl_size;
		select_struct_overhead = select_struct_overhead + select_struct_buffer_overhead;
		int receive_call_size = channelReceiveCallOverhead(max_sized_type);
		return select_struct_overhead + select_call_size + maxValueOf( max_decl_size  + receive_call_size, max_gc_size);
	}

	
	
	/**
	 * 
	 * @return he estimated overhead from calling Construct(int argc, void *argv[])
	 */
	public static int constructorFunctionCallOverhead(List<ITypeRep> args){
		int size = functionCallOverhead(PNTR_SIZE + INT_SIZE + PNTR_SIZE) // call of Construct_Comp(Comp_PNTR this, int argc, void *argv[])
				+ sum_type_sizes(args);                                   // assignment from argv[] to local arg stack variables in Construct_Comp
				
		int decRef_local_args_space = 0;
		if (args.size() > 0){                                             // decRef of local arg stack variables in Construct_Comp
			decRef_local_args_space = maxDalDecRefCallOverhead(args);
//			Diagnostic.trace(DiagnosticLevel.FINAL, "decRef_local_args_space: " + decRef_local_args_space);
		}

//		Diagnostic.trace(DiagnosticLevel.FINAL, "size: " + size);
		size += maxValueOf(initGlobalsCallOverhead(), decRef_local_args_space);
//		Diagnostic.trace(DiagnosticLevel.FINAL, "initGlobalsCallOverhead: " + initGlobalsCallOverhead());
//		Diagnostic.trace(DiagnosticLevel.FINAL, "decRef_local_args_space: " + decRef_local_args_space);
//		Diagnostic.trace(DiagnosticLevel.FINAL, "size: " + size);
		return size;
	}
	
	/**
	 * @return the estimated stack overhead from calling component_create
	 */
	public static int componentCreateCallOverhead(){
		return sysFunctionCallOverhead(3*PNTR_SIZE + 3*INT_SIZE) + 3*PNTR_SIZE + mallocCallOverhead();
	}

	/**
	 * @return estimated stack size from calling DAL_assign to initialise an empty location (no decRef needed)
	 */
	public static int dalAssignCallOverHeadNoDecRef(){
		return functionCallOverhead(2*PNTR_SIZE) + 2*PNTR_SIZE;
	}
	
	/**
	 * @return estimated stack size from calling DAL_assign when decRef may be called as a result
	 */
	public static int dalAssignCallOverHead(ITypeRep tr){
		//return 0;
		return dalAssignCallOverHeadNoDecRef() + dalDecRefCallOverhead(tr);
	}

	
	public static int constructStruct_CallOverhead(StructType st){
		List<ITypeRep> field_types = st.getFields();
		int size = functionCallOverhead(field_types);             // for calling the constructor
		size += PNTR_SIZE;                                        // to store pntr to the new struct                      
		size += dalAllocCallOverhead();                           // for calling DAL_alloc
		Diagnostic.trace(DiagnosticLevel.FINAL, "construct " + st.getName() + ": " + size);
		return size;
	}
	
	/**
	 * @return estimated stack size from calling DAL_decRef for given type
	 */
	public static int dalDecRefCallOverhead(ITypeRep tr){
		int nesting = 1;
		int potential_array_base_decref_size = 0;
		int potential_struct_field_decref_size = 0;
		if(tr instanceof StringType){
			nesting = 2; // decRef(p) -> string_decRef(p)
		}
		if(tr instanceof StructType){
			nesting = 2; // decRef(p) -> struct_decRef(p)
			// plus max cost (over all struct fields) of field decRefs
			StructType st = (StructType) tr;
			List<ITypeRep> field_types = st.getFields();
			potential_struct_field_decref_size = maxDalDecRefCallOverhead(field_types);
		}
		if(tr instanceof ArrayType){
			nesting = 2; // decRef(p) -> array_decRef(p)
			// plus further decRef for each dimensionality
			ArrayType at = (ArrayType) tr;
			nesting += at.getDimensionality();
			// plus further decRef if array payload is PNTR type
			if(at.getBaseType().isPointerType()){
				potential_array_base_decref_size = dalDecRefCallOverhead(at.getBaseType());
			}
		}
		if(tr instanceof AnyType){
			nesting = MAX_ANYTYPE_DCREF_NESTING;
		}
		if(tr instanceof ComponentType){
			nesting = MAX_COMPONENTTYPE_DECREF_NESTING; 
		}
		int overhead = potential_array_base_decref_size
		+	potential_struct_field_decref_size
		+	nesting*(functionCallOverhead(PNTR_SIZE) + PNTR_SIZE * 2)
		+ 	dalFreeCallOverhead();
		return overhead; 
	}


	/**
	 * @return estimated maximum stack size from calling DAL_decRef for each of the given types
	 */
	public static int maxDalDecRefCallOverhead(List<ITypeRep> types){
		int max_stack_required = 0;
		for(ITypeRep tr : types){
			if(tr.isPointerType()){
				int stack_required = dalDecRefCallOverhead(tr);
				if(stack_required > max_stack_required){
					max_stack_required = stack_required;
				}
			}
		}
		return max_stack_required; 
	}

	
	/**
	 * @return estimated stack size from calling DAL_modRef(pntr, n) for incrementing the ref count
	 */
	public static int dalModRefIncCallOverhead(){
		return functionCallOverhead(PNTR_SIZE + INT_SIZE); 
	}
	
	/**
	 * @return estimated stack size from calling  DAL_alloc(int size, bool contains_pointers)
	 */
	public static int dalAllocCallOverhead(){
		return functionCallOverhead(INT_SIZE + BOOL_SIZE) + PNTR_SIZE + mallocCallOverhead();
	}
	
	/**
	 * @return estimated stack size from calling  DAL_free(void *pntr)
	 */
	public static int dalFreeCallOverhead(){
		return functionCallOverhead(PNTR_SIZE) + PNTR_SIZE + freeCallOverhead();
	}

	/**
	 * @return estimated stack size from calling initGlobals(void *pntr)
	 */
	public static int initGlobalsCallOverhead(){
		int initGlobals_call_overhead = 
			functionCallOverhead(PNTR_SIZE)    // for calling initGlobal(pntr)
			+ dalAssignCallOverHeadNoDecRef(); // for (potentially) assigning component locations (no decref)
		return initGlobals_call_overhead;
	}
	
	/**
	 * @return estimated stack size from calling free(void *pntr)
	 */
	public static int freeCallOverhead(){
		return functionCallOverhead(PNTR_SIZE) ; // TODO + .... check free impl
	}
	
	/**
	 * @return estimated stack size from calling malloc(int size)
	 */
	public static int mallocCallOverhead(){
		return functionCallOverhead(INT_SIZE) + PNTR_SIZE; // TODO + ... check malloc impl
	}
	
	/**
	 * @return the estimated stack overhead of calling the memncpy function
	 */
	public static int memncpyCallOverhead(){
		return functionCallOverhead(2*PNTR_SIZE + INT_SIZE) + INT_SIZE;
	}
	
	/**
	 * @return the estimated stack overhead of calling strcmp
	 */
	public static int strcmpCallOverhead(){
		return functionCallOverhead(PNTR_SIZE * 2);
	}
	
	/**
	 * @return the estimated stack overhead of calling strlen
	 */
	public static int strlenCallOverhead(){
		return functionCallOverhead(PNTR_SIZE);
	}
	
	/**
	 * @return the estimated stack overhead of calling strcpy
	 */
	public static int strcpyCallOverhead(){
		return functionCallOverhead(PNTR_SIZE*2);
	}
	
	/**
	 * @return the estimated stack overhead of calling Construct_String0
	 */
	public static int stringConstruct0CallOverhead(){
		return functionCallOverhead(PNTR_SIZE) + functionCallOverhead(PNTR_SIZE + BOOL_SIZE) + PNTR_SIZE + maxValueOf(dalAllocCallOverhead(), strlenCallOverhead());
	}

	/**
	 * @return the estimated stack overhead of calling Construct_String1
	 */
	public static int stringConstruct1CallOverhead(){
		return functionCallOverhead(PNTR_SIZE) + functionCallOverhead(PNTR_SIZE + BOOL_SIZE) + PNTR_SIZE*2 + maxValueOf(dalAllocCallOverhead(), strlenCallOverhead(), strcpyCallOverhead(), dalAssignCallOverHeadNoDecRef());
	}
		
}
