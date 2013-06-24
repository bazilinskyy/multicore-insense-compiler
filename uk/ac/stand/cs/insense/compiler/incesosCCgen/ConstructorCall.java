package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.util.ArrayList;
import java.util.List;
import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.cgen.IConstructorCall;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;

public class ConstructorCall extends Code implements ICode, IConstructorCall {

	String componentname;
	int constructor_index = 0;
	boolean first_param = true;
	private List<ITypeRep> paramTypes;
	private String[] params;

	private final String param_name_prefix = "param";

	public ConstructorCall(String name) {
		this.componentname = name;
		// Cgen.get_instance().addVTBLUsageToCompilationUnit( componentname ); // TODO NOT NEEDED FOR INCEOS?
		this.constructor_index = 38808; // error
		this.params = new String[0];
		this.paramTypes = new ArrayList<ITypeRep>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IComponentConstructor#complete()
	 */
	@Override
	public void complete() {
		// rearrange the params into the right order with the right caller name in it
		StringBuffer call_code = new StringBuffer();

		// if we have some args need to marshall them up here

		int length = params.length;

		call_code.append("component_create"); 		// starting to create a function call
		call_code.append(LRB_);
		call_code.append(constructor_name());
		// call_code.append( COMMA + SPACE + "behaviour_" + componentname); // no longer pas behaviour to component_create, it is called from each constructor
		call_code.append(COMMA + SPACE + functionCall("sizeof", componentname + "Struct"));
		int component_stack_usage = Component.getComponentStackSize(componentname);
		call_code.append(COMMA + SPACE + component_stack_usage);

		if (length > 0) { // If we have constructor parameters
			call_code.append(COMMA + SPACE + length + COMMA + SPACE);
			call_code.append(functionCall(Constructor.constructArrayName(componentname + constructor_index, constructor_index), super.toString()));
		} else { // no params to constructor
			call_code.append(COMMA + SPACE + ZERO + COMMA + SPACE + NULL_);
		}
		call_code.append(COMMA + SPACE + "-1"); // ID of a core for setting affinity. -1 by default, means that affinity is not set manually.
		call_code.append(RRB_);
		super.reset(call_code.toString());

		// TODO JL Space Tracking
		ICode container_stack = Cgen.get_instance().findEnclosingDelcarationContainer();
		int constructor_call_overhead = MSP430Sizes.componentConstructorCallOverhead(paramTypes);
		int constructor_stack_usage = Constructor.getConstructorStackSize(constructor_name());
		container_stack.track_call_space(constructor_call_overhead + constructor_stack_usage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IComponentConstructor#setConstructorIndex(int)
	 */
	@Override
	public void setDisambiguatorAndParameters(int index, List<ITypeRep> paramTypes) {
		this.constructor_index = index;
		this.paramTypes = paramTypes;
		if (paramTypes.size() > 0) {
			this.params = super.toString().split(",");
		}
	}

	private String constructor_name() {
		return CONSTRUCT + UNDERBAR + componentname + constructor_index;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IComponentConstructor#parameter()
	 */
	@Override
	public void parameter() {
		if (first_param) {
			first_param = false;
		} else {
			append(COMMA);
		}
	}
}
