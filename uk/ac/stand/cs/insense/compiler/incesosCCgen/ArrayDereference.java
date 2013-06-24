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

import uk.ac.stand.cs.insense.compiler.cgen.IArrayDereference;
import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.symbols.STEntry;

/*
 * @author al
 * @deprecated as of 16/5/07 by the new class @see uk.ac.stand.cs.insense.compiler.cgen.Dereference
 */
public class ArrayDereference extends ArrayOps implements ICode, IArrayDereference {

	private final String fn;
	private final String array_name;
	private String array_index;

	private static boolean success_generated = false;

	public ArrayDereference(STEntry ste) { // ITypeRep referend_type ) {
		super();
		array_name = ste.getName();
		fn = array_deref_function(ste.getType());
	}

	@Override
	public void append(String s) {
		array_index = s;
	}

	@Override
	public void complete() {
		super.append(functionCall(fn, array_name, array_index) + RRB);
		if (ExceptionBlock.inExceptionBlock()) {
			super.append("; if( ! success ) { goto " + ExceptionBlock.getLabel() + RCB_ + SEMI + NEWLINE);
		}
		if (!success_generated) {
			success_generated = true;
			// Cgen.get_instance().addHoistedCodeToComponentContext( TAB + "bool success;" + NEWLINE );
		}
	}

}
