/*
 * Copyright (c) 2013, modified by Pavlo Bazilinskyy <pavlo.bazilinskyy@gmail.com>
 * Original work by Jonathan Lewis and Alan Dearle
 * School of Computer Science, St. Andrews University
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.util.ArrayList;
import java.util.List;
import uk.ac.stand.cs.insense.compiler.cgen.IArrayConstructor;
import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.interfaces.ICompilerErrors;
import uk.ac.stand.cs.insense.compiler.types.AnyType;
import uk.ac.stand.cs.insense.compiler.types.ArrayType;
import uk.ac.stand.cs.insense.compiler.types.BooleanType;
import uk.ac.stand.cs.insense.compiler.types.ByteType;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;
import uk.ac.stand.cs.insense.compiler.types.IntegerType;
import uk.ac.stand.cs.insense.compiler.types.RealType;
import uk.ac.stand.cs.insense.compiler.types.StructType;
import uk.ac.standrews.cs.nds.util.Diagnostic;

public class ArrayConstructor extends ArrayOps implements ICode, IArrayConstructor {

	private boolean initialising = false;
	private String initialiser = "";
	private final List<String> dimensions = new ArrayList<String>();
	private ITypeRep base_type = null;
	private ITypeRep array_type = null;
	private static List<String> generatedFunctionsList = new ArrayList<String>();
	private final ICompilerErrors compilerErrors;

	public ArrayConstructor(ICompilerErrors compilerErrors) {
		this.compilerErrors = compilerErrors;
	}

	@Override
	public void initialiser() {
		initialising = true;
	}

	@Override
	public void type(ITypeRep base_type, ITypeRep array_type) {
		this.base_type = base_type;
		this.array_type = array_type;
	}

	@Override
	public void append(String s) {
		if (initialising) {
			initialiser = s;
		} else {
			dimensions.add(s);
		}
	}

	@Override
	public void complete() {

		if (!generatedConstructorAlready(array_type)) {
			generate_generator_function(array_type);
		}

		if (!generatedCopyfunctionAlready(array_type)) {
			generate_copy_function(array_type);
		}
		StringBuffer params = new StringBuffer();
		for (String size : dimensions) {
			if (Integer.parseInt(size) <= 0) {
				compilerErrors.generalError("Array lengths must be > 0!");
			}
			params.append(size);
			params.append(COMMA);
		}
		params.append(initialiser);
		String constructor_name = generate_unique_constructor_name(array_type);
		super.append(functionCall(constructor_name, params.toString()));
		// TODO JL Space Tracking
		Cgen.get_instance().findEnclosingDelcarationContainer().track_call_space(MSP430Sizes.arrayConstructorCallOverhead(dimensions.size()));
	}

	public static String union_selector(ITypeRep referend_type) {
		String c_array_type = "pointer_value";
		if (referend_type.equals(IntegerType.TYPE)) {
			c_array_type = "int_value";
		} else if (referend_type.equals(BooleanType.TYPE)) {
			c_array_type = "bool_value";
		} else if (referend_type.equals(RealType.TYPE)) {
			c_array_type = "real_value";
		} else if (referend_type.equals(ByteType.TYPE)) {
			c_array_type = "byte_value";
		}
		return c_array_type;
	}

	protected static String constructorCode(ITypeRep array_type) {
		generatedFunctionsList.add(generate_unique_constructor_name(array_type));

		String constructor_name = generate_unique_constructor_name(array_type);
		int num_dimensions = ((ArrayType) array_type).getDimensionality();
		ITypeRep final_base_type = ((ArrayType) array_type).getBaseType();

		ITypeRep base_type = ((ArrayType) array_type).getArray_type();
		StringBuffer buff = new StringBuffer();
		buff.append(NEWLINE + GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE);
		buff.append("IArrayPNTR "); // return type
		buff.append(constructor_name + LRB);
		int index = 0;
		for (index = 0; index < num_dimensions; index++)
			// add the params - one for each dimension
			buff.append("int " + paramName(index) + COMMA);
		buff.append(SPACE + insenseTypeToCTypeName(final_base_type) + " init " + RRB); // add the initialiser
		buff.append(LCB_ + NEWLINE);

		String body = null;
		int count = 0;
		String last_variable = "";

		for (int dim = num_dimensions - 1; dim >= 0; dim--) {
			boolean on_data_dimension = dim == (num_dimensions - 1);
			boolean isPointerType = on_data_dimension ? final_base_type.isPointerType() : true;
			String potentially_ampersand = isPointerType ? (SPACE) : (SPACE + AMPERSAND);
			// String potentially_ampersand = SPACE + AMPERSAND;
			String initialiser = on_data_dimension ? potentially_ampersand + "init" : SPACE + "NULL";
			String contains_pointers = isPointerType ? " true" : " false";
			String variable = "x" + dim;
			if (body == null) { // the starting case
				// this is the innermost loop in the initialisation if the array is multi dimensional
				body = tab(dim + 1)
						+ "IArrayPNTR"
						+ SPACE
						+ variable
						+ SPACE
						+ EQUALS
						+ SPACE
						+ functionCall(array_constructor_name(base_type), paramName(dim), functionCall("sizeof", insenseTypeToCTypeName(final_base_type)),
								SPACE + initialiser, contains_pointers) + SEMI + NEWLINE;
			} else {
				StringBuffer sb = new StringBuffer();
				sb.append(tab(dim + 1) + " int " + loopName(count) + SEMI + NEWLINE);
				sb.append(tab(dim + 1)
						+ "IArrayPNTR"
						+ SPACE
						+ variable
						+ SPACE
						+ EQUALS
						+ SPACE
						+ functionCall(array_constructor_name(base_type), paramName(dim), functionCall("sizeof", insenseTypeToCTypeName(base_type)),
								initialiser, contains_pointers) + SEMI + NEWLINE);
				sb.append(tab(dim + 1) + FOR + LRB + loopName(count) + EQUALS + ZERO + SEMI + loopName(count) + LT_ + paramName(dim) + SEMI + loopName(count)
						+ PLUS_PLUS_);
				sb.append(tab(dim + 1) + RRB + LCB_ + NEWLINE);
				sb.append(body);
				// sb.append( tab(dim+2) + functionCall( array_lhs_deref_function( type ), variable, loopName( count ),AMPERSAND + "success" ) + " = " +
				// last_variable + SEMI + NEWLINE );
				// JL mod
				sb.append(tab(dim + 2)
						+ functionCall("DAL_assign", AMPERSAND + functionCall(array_lhs_deref_function(base_type), variable, loopName(count)) + COMMA
								+ last_variable) + SEMI + NEWLINE);
				sb.append(tab(dim + 1) + RCB_ + NEWLINE);
				count++;
				body = sb.toString();

			}
			last_variable = variable;
			base_type = new ArrayType(base_type);
		}
		// buff.append( TAB + "bool success;" + NEWLINE );
		// JL commented out above, does not appear to be needed in constructor
		// should be in behaviour and passed to array_<type>_loc as parameter
		buff.append(body);
		buff.append(TAB + "return" + SPACE + last_variable + SEMI + NEWLINE);
		buff.append(RCB_ + NEWLINE);
		return buff.toString();
	}

	protected static String copyFunctionCode(ITypeRep array_type) {
		generatedFunctionsList.add(generate_unique_copy_function_name(array_type));

		String constructor_name = generate_unique_constructor_name(array_type);
		String copy_function_name = generate_unique_copy_function_name(array_type);

		int num_dimensions = ((ArrayType) array_type).getDimensionality();
		ITypeRep final_base_type = array_type;
		for (int i = 0; i < num_dimensions; i++)
			final_base_type = ((ArrayType) final_base_type).getArray_type();
		// for(int dim = num_dimensions-1; dim >=0 ; dim--)
		// System.out.println(function_name + " num_dims: " + num_dimensions + " array_type: "+array_type + " base_type: " + final_base_type);
		StringBuffer buff = new StringBuffer();
		buff.append(NEWLINE + GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE);
		buff.append("IArrayPNTR "); // return type
		buff.append(copy_function_name + LRB);
		buff.append("IArrayPNTR " + SPACE + THIS_ + RRB); // return type
		buff.append(LCB_ + NEWLINE);

		// increment ref count of parameter this (the source array)
		buff.append(TAB + functionCall("DAL_incRef", "this") + SEMI + NEWLINE);

		// make new Array using generated constructor
		buff.append(TAB + "IArrayPNTR copy = ");
		StringBuffer param_buffer = new StringBuffer();
		String cast = STAR + LRB + "IArrayPNTR* " + RRB;
		String length_deref = ARROW + "length";
		String base_object = THIS;
		String data_field = "data";

		String array_length = length_deref;
		List<String> parameters = new ArrayList<String>();
		parameters.add(base_object + array_length);
		param_buffer.append(parameters.get(0));
		for (int i = 1; i < num_dimensions; i++) {
			base_object = LRB + cast + base_object + ARROW + data_field + RRB;
			parameters.add(base_object + length_deref);
			param_buffer.append(COMMA + SPACE + parameters.get(i));
		}
		param_buffer.append(COMMA + SPACE + final_base_type.getDefaultCValue());
		buff.append(functionCall(constructor_name, param_buffer.toString()) + SEMI + NEWLINE);

		int count = 0;
		String loop_counter_prefix = "v";

		// declare local loop counter variable and start nested C for loops to do copy
		for (int i = 0; i < num_dimensions; i++) {
			String loop_counter = loop_counter_prefix + count;
			buff.append(tab(count + 1) + "unsigned " + loop_counter + SEMI + NEWLINE);
			buff.append(tab(count + 1) + FOR + LRB + loop_counter + EQUALS + ZERO + SEMI + SPACE + loop_counter + "<" + parameters.get(count) + SEMI + SPACE
					+ loop_counter + "++" + RRB + LCB_ + NEWLINE);
			count++;
		}
		// generate assignment to perform the copy
		buff.append(tab(count + 1));
		if (final_base_type.isPointerType())
			buff.append("DAL_assign" + LRB + AMPERSAND);

		buff.append(array_deref_function(final_base_type) + LRB);
		for (int dim = 1; dim < num_dimensions; dim++) {
			buff.append(array_deref_function(array_type) + LRB);
		}
		buff.append("copy");
		for (int count2 = 0; count2 < count; count2++)
			buff.append(COMMA + SPACE + loop_counter_prefix + count2 + RRB);
		if (final_base_type.isPointerType()) {
			buff.append(SPACE + COMMA + SPACE);
			if (!(final_base_type instanceof AnyType)) { // AnyTypes cannot be assigned to, so only need to copy ref to AnyType
				buff.append("copy_");
			}
			if (final_base_type instanceof StructType)
				buff.append("struct_");
			if (!(final_base_type instanceof AnyType)) { // AnyTypes cannot be assigned to, so only need to copy ref to AnyType
				buff.append(final_base_type.toStringRep());
			}
			buff.append(LRB);
		} else
			buff.append(SPACE + EQUALS + SPACE);
		buff.append(array_deref_function(final_base_type) + LRB);
		for (int dim = 1; dim < num_dimensions; dim++) {
			buff.append(array_deref_function(array_type) + LRB);
		}
		buff.append("this");
		for (int count2 = 0; count2 < count; count2++)
			buff.append(COMMA + SPACE + loop_counter_prefix + count2 + RRB);
		if (final_base_type.isPointerType())
			buff.append(RRB + RRB);

		buff.append(SEMI);
		for (int count2 = count; count2 > 0; count2--)
			buff.append(NEWLINE + tab(count2) + RCB_);
		buff.append(NEWLINE);
		// end of nested C for loops performing the copy

		// decrement ref count of parameter this (the source array)
		buff.append(TAB + functionCall("DAL_decRef", "this") + SEMI + NEWLINE);

		buff.append(TAB + "return" + SPACE + "copy" + SEMI + NEWLINE);

		buff.append(RCB_ + NEWLINE);
		return buff.toString();

	}

	/**
	 * Generates a constructor for arrays
	 * 
	 * @param constructor_name
	 *            the name of the constructor that is generated
	 */
	protected static void generate_generator_function(ITypeRep array_type) {
		Cgen.get_instance().addHoistedCodeToComponentOrCompilationUnitContext(constructorCode(array_type));
	}

	/**
	 * Generates a copy function for arrays
	 * 
	 * @param copy_function_name
	 *            the name of the function that is generated
	 * @param constructor_name
	 *            the name of the constructor for this array type
	 */
	protected static void generate_copy_function(ITypeRep array_type) {
		Cgen.get_instance().addHoistedCodeToComponentOrCompilationUnitContext(copyFunctionCode(array_type));
	}

	protected static boolean generatedConstructorAlready(ITypeRep type) {
		return generatedAlready(generate_unique_constructor_name(type));
	}

	protected static boolean generatedCopyfunctionAlready(ITypeRep type) {
		return generatedAlready(generate_unique_copy_function_name(type));
	}

	private static boolean generatedAlready(String name) {
		return generatedFunctionsList.contains(name);
	}

}
