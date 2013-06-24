package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.util.ArrayList;
import java.util.List;
import uk.ac.stand.cs.insense.compiler.cgen.IDereference;
import uk.ac.stand.cs.insense.compiler.symbols.STEntry;
import uk.ac.stand.cs.insense.compiler.types.ComponentType;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;
import uk.ac.stand.cs.insense.compiler.types.StructType;
import uk.ac.stand.cs.insense.compiler.types.UnknownType;

/**
 * @author al
 *         16-5-07
 *         Tricky code so more comments than normal :)
 *         This class handles (potentially multiple) component, structure and array dereferences
 *         It also handles assignment to locations that arise from lhs dereferences
 */
public class Dereference extends Location implements IDereference {

	private static final int CHANNEL = 2;	// A label used in kind below to track what kind of dereference is being made
	private static final int FIELD = 4;	// A label used in kind below to track what kind of dereference is being made
	private static final int ARRAY = 8;	// A label used in kind below to track what kind of dereference is being made
	private static final int ARRAY_LENGTH = 16; 	// JL added for .length on arrays

	/**
	 * Keeps track of each dereference made
	 */
	private class Deref {
		public int kind;
		public String deref;
		public ITypeRep type;	// the type of the field/array index/etc.

		public Deref(int kind, String deref) {
			this.kind = kind;
			this.deref = deref;
			this.type = UnknownType.TYPE;
		}
	}

	private final List<Deref> dereferences = new ArrayList<Deref>();	// Lists the multiple dereferences
	private Deref current; // the current deref struct being used - in the case of arrays code is filled in later
	private boolean lhs = false;	// tracks if the dereference is on the lhs of an assignment - i.e we are generating a location to assign to.
	private String rhs = "";
	private String target = "";	// the C expression representing the whole derefernce - used when hoisting code for if on rhs of assignment,
	private StringBuffer sb = new StringBuffer(); // used to build up current string
	private boolean need_exception_handling = false; // used to track if we need exception handling code to be generated

	private static boolean success_generated = false; // used in array dereference exception handing - tracks whether a C global has been generated

	private boolean channel_dereferenced = false;

	private final int fromContext;

	public Dereference(STEntry ste, int fromContext) {
		super(ste);
		this.fromContext = fromContext;
	}

	@Override
	public void channelDereference(String the_name) {
		if (ste.getType() instanceof ComponentType) {
			channel_dereferenced = true;
			current = new Deref(CHANNEL, the_name + "_comp"); // top level component channel - so need to accomodate Mangling
			// was current = new Deref( CHANNEL, "impl->get_" + the_name + "_comp" + LRB + ste.contextualName(fromContext) + RRB ); // top level component
			// channel - so need to accomodate Mangling
		} else {
			current = new Deref(CHANNEL, the_name);	// don't do this for local channels - they are not Mangled
		}
		dereferences.add(current);
	}

	@Override
	public void fieldDereference(String the_name) {
		current = new Deref(FIELD, the_name);
		dereferences.add(current);
	}

	// JL added for .length on arrays and strings
	@Override
	public void lengthDereference() {
		current = new Deref(ARRAY_LENGTH, "");
		dereferences.add(current);
	}

	@Override
	public void arrayDereference() {
		current = new Deref(ARRAY, "");
		dereferences.add(current);
	}

	@Override
	public void setDerefType(ITypeRep t) {
		current.type = t;
	}

	@Override
	public void leftHandSide() {
		lhs = true;
		generate_deref();
	}

	// Methods from ILocation

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.cgen.ILocation#getTargetName()
	 */
	public String getTargetName() {
		return target;
	}

	// Generative methods

	public void lappend(String s) { // local append - builds up StringBuffer
		sb.append(s);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IStructDereference#addFieldName(java.lang.String)
	 */
	public void addFieldDeref(String field_name) {
		lappend(ARROW);
		lappend(field_name);
		if (ste.getType() instanceof StructType) {
			StructValue sv = new StructValue(((StructType) ste.getType()));
			sv.complete();
		}
	}

	public void addChannelDeref(String channel_name) {
		lappend(ARROW);
		lappend(channel_name);
	}

	public void addArrayLengthDeref(Deref d) {
		lappend(ARROW + "length" + SPACE);
	}

	public void addArrayDeref(boolean last, Deref d) {
		String subject = sb.toString();
		sb = new StringBuffer();	// reset the buffer as we are about to suck it all up into the array dereference.
		if (last && lhs) {	// this is the last dereference and we have an assignment so generate code to create a location.
			String fn = ArrayOps.array_lhs_deref_function(d.type);
			lappend(TAB + LRB + functionCall(fn, subject, d.deref) + RRB);
		} else {
			String fn = ArrayOps.array_deref_function(d.type);
			lappend(TAB + LRB + functionCall(fn, subject, d.deref) + RRB);
		}
		if (ExceptionBlock.inExceptionBlock()) {
			need_exception_handling = true;
		}
	}

	// Methods from ICode

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.Code#append(java.lang.String)
	 */
	@Override
	public void append(String s) {
		if (lhs) {			// we have already finished doing dereferences and we are now into an assign statement
			// rhs = s;
			// JL mod make append bloomin do simple append
			rhs += s;
		} else {
			/*
			 * if( !( current.kind == ARRAY && current.deref.equals( "" ) ) ) {
			 * throw new RuntimeException( "Array dereference in inexpected context" );
			 * } else {
			 * current.deref = s; // add the array deref code to the current dereference context.
			 * }
			 */
			// JL mod, not sure what the above exception means
			// and not sure why deref string is overwritten in else block
			// have done append to deref string here instead ask Al
			current.deref += s;
		}
	}

	/**
	 * Generate the dereference code
	 * Note that this doesnt put the code out - instead it assigns it to target
	 */
	public void generate_deref() {
		// if(channel_dereferenced)
		// lappend(STAR);
		lappend(ste.contextualName(fromContext)); // the subject of the dereference
		int size = dereferences.size(); // number of elements in the list
		int count = 1;
		for (Deref d : dereferences) {
			switch (d.kind) {
				case CHANNEL:
					addChannelDeref(d.deref);
					break;
				case FIELD:
					addFieldDeref(d.deref);
					break;
				case ARRAY:
					addArrayDeref(count == size, d);
					break;
				case ARRAY_LENGTH:
					addArrayLengthDeref(d);
					break;
				default:
					throw new RuntimeException("unexpected kind tag in dereference");
			}
			count++;
		}
		target = sb.toString();		// this is the lhs code if we have an assignment
		sb = new StringBuffer(); 	// reset the buffer - we have put this out now
									// note we shouldn't add anything to this again
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.Code#complete()
	 */
	@Override
	public void complete() {
		boolean need_dal_assign = current.type.isPointerType();
		if (!lhs) {
			generate_deref();			// generate the code
			super.append(target);		// get the code out
		} else {
			if (!reordered) {
				if (need_dal_assign)
					super.append("DAL_assign" + LRB + AMPERSAND + target + COMMA + SPACE);
				else
					super.append(target + SPACE + EQUALS_);		// get the code out - it hasn't been reordered
			}
			if (sb.length() > 0) {
				throw new RuntimeException("illegal code added mistakenly to dereference");
			}
			if (need_dal_assign)
				super.append(rhs + RRB);
			else
				super.append(rhs);
		}
		// we need the exception code no matter what!
		if (need_exception_handling) {
			// TODO JL this exception handling will not work, if e.g. 0 is returned and
			// 0 is dereferenced before the if statement!
			// super.append( "; if( ! success ) { goto " + ExceptionBlock.getLabel() + RCB_ + SEMI + NEWLINE );
			// JL don't need below, have put global variable success into runtime
			/*
			 * if( ! success_generated ) {
			 * success_generated = true;
			 * Cgen.get_instance().addhoistedCodeToCurrentContext( TAB + "bool success;" + NEWLINE );
			 * }
			 */
		}
	}
}
