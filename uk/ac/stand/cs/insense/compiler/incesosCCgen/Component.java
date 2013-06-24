package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.cgen.IComponent;
import uk.ac.stand.cs.insense.compiler.cgen.IConstructor;
import uk.ac.stand.cs.insense.compiler.cgen.IDecl;
import uk.ac.stand.cs.insense.compiler.cgen.IFunction;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling.HeaderFile;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling.ImplFile;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.fileHandling.OutputFile;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.types.ChannelType;
import uk.ac.stand.cs.insense.compiler.types.FunctionType;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;
import uk.ac.stand.cs.insense.compiler.types.InterfaceType;
import uk.ac.stand.cs.insense.compiler.types.TypeName;
import uk.ac.standrews.cs.nds.util.Diagnostic;
import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
import uk.ac.standrews.cs.nds.util.ErrorHandling;

public class Component extends ProcedureContainer implements ICode, IComponent {

	private final String filename;
	private final String componentname;
	private final String initGlobalsName;
	private ICode behaviour;
	private boolean is_complete;

	private final HashMap<String, ChannelType> channels;
	private final ConcurrentSkipListSet<String> channel_names;

	private final List<IConstructor> constructors;
	private final String behaviour_name;
	private final String process_name;
	private final String outputted_component_name;
	private final List<String> externalIncludes;
	private static String COMP = "_comp";
	private int constructorDisambiguator;
	private boolean contains_stop_statement;

	// TODO JL Space Tracking
	private static Map<String, Integer> componentStacks = new HashMap<String, Integer>();

	public Component(String componentname) {
		super(componentname);
		this.componentname = componentname;
		this.behaviour = null;
		this.filename = componentname;
		this.channels = new HashMap<String, ChannelType>();
		this.channel_names = new ConcurrentSkipListSet<String>();

		this.constructors = new ArrayList<IConstructor>();
		this.behaviour_name = BEHAVIOUR_ + componentname;
		this.outputted_component_name = componentname + "_name";
		this.process_name = componentname + UNDERBAR + PROCESS;
		this.initGlobalsName = componentname + UNDERBAR + "init_globals";
		this.externalIncludes = new ArrayList<String>();
		this.is_complete = false;
		this.constructorDisambiguator = 0;
		this.contains_stop_statement = false;
	}

	// TODO JL Space Tracking
	public static int getComponentStackSize(String name) {
		return componentStacks.get(name).intValue();
	}

	// ICode Methods

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IComponent#complete()
	 */
	@Override
	public void complete() {
		generateImplFile();
		generateHeaderFile();
		is_complete = true;
		// TODO JL Space Tracking
		// generatePropertiesFile();
		track_add_stack_byte(behaviour.get_maximal_stack_usage());
		int stack_usage = get_maximal_stack_usage();
		stack_usage += stack_usage % 2;// ensure size is even
		set_maximal_stack_usage(stack_usage);

		componentStacks.put(componentname, get_maximal_stack_usage());
		Diagnostic.trace(DiagnosticLevel.FINAL, "Stack space for component " + componentname + ": " + get_maximal_stack_usage());
	}

	// Public Component code methods

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IComponent#addChannel(java.lang.String, uk.ac.stand.cs.insense.compiler.types.ChannelType)
	 */
	@Override
	public void addChannel(String name, ChannelType tr) {
		channels.put(name, tr);
		channel_names.add(name);
		// addExternalIncludes( Code.HASH_INCLUDE_ + Code.DQUOTE + channelIncludeName( tr ) + Code.DQUOTE); // TODO NOT NEEDED INCEOS?
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IComponent#addInterface(java.lang.String, uk.ac.stand.cs.insense.compiler.types.InterfaceType)
	 */
	@Override
	public void addInterface(String name, InterfaceType type) {
		for (TypeName iter : type) {
			if (iter.type instanceof ChannelType) {
				addChannel(iter.name + "_comp", iter.type);
			} else {
				throw new RuntimeException("unknown type encountered in Interface " + iter.type.getClass().getName());
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IComponent#addExternalInclude(java.lang.String)
	 */
	@Override
	public void addExternalIncludes(String s) {
		if (!externalIncludes.contains(s)) {
			externalIncludes.add(s);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IComponent#addConstructor(uk.ac.stand.cs.insense.compiler.Ccgen.Constructor)
	 */
	@Override
	public void addConstructor(IConstructor code) {
		code.setDisambiguator(constructorDisambiguator++);
		constructors.add(code);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IComponent#addBehaviour(uk.ac.stand.cs.insense.compiler.cgen.ICode)
	 */
	@Override
	public void addBehaviour(ICode code) {
		this.behaviour = code;
	}

	// /////////////////////// Private /////////////////////////

	// Generative methods
	private String getter_name(String s) {
		return "get_" + s;
	}

	/*
	 * TODO NOT NEEDED INCEOS?
	 * /**
	 * Writes a getter function signature for a channel to the stream ps
	 * 
	 * @param ps - the stream on which the decls are written
	 * 
	 * @param name - the name of channel whose getter is being written
	 * 
	 * @param tab_level - the indent level for the code
	 * 
	 * @param isdefinition - if the code is used in a struct definintion and hence has bracketed fn name and ends in a semi colon
	 * 
	 * @see printStructChannelSetterSignature for an example of the two forms
	 * 
	 * private void printStructChannelGetterSignature( PrintStream ps, String name, ITypeRep type, int tab_level, boolean isdefinition ) {
	 * ps.println( GENERATED_FROM + Diagnostic.getMethodInCallChain() );
	 * for( int i = 0; i < tab_level; i++ ) {
	 * ps.print( TAB );
	 * }
	 * if(!isdefinition)
	 * ps.print(STATIC_);
	 * ps.print( channelName( type ) + SPACE + STAR);
	 * if( isdefinition ) {
	 * ps.print( LRB_ + STAR );
	 * }
	 * ps.print( getter_name( name ) );
	 * if( isdefinition ) {
	 * ps.print( RRB_ );
	 * }
	 * ps.print( LRB_ + data_pntr_name( componentname ) + THIS_ + RRB_ );
	 * if( isdefinition ) {
	 * ps.println( SEMI );
	 * }
	 * }
	 */

	/**
	 * Writes the Signatures for Channels declared in the component interface to the stream
	 * 
	 * @param ps
	 *            - the stream on which the decls are written
	 */
	void printStructChannelSignatures(PrintStream ps) {
		ps.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		Set<String> ks = channels.keySet();
		// for( String s : ks ) {
		// JL using alphabetically ordered set of channel names
		// TODO NOT NEEDED INCEOS?
		// for( String s : channel_names ) {
		// printStructChannelGetterSignature( ps, s, channels.get( s ), 1, true );
		// }
	}

	/**
	 * Writes the Contiki process structure declaration to the stream
	 * 
	 * @param ps
	 *            - the stream on which the decls are written
	 */
	private void printCommonDecls(PrintStream ps) {
		ps.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		ps.print(TAB + "void (*decRef)(" + data_pntr_name(componentname) + "pntr)" + SEMI + NEWLINE);
		ps.print(TAB + "bool" + SPACE + "stopped" + SEMI + NEWLINE);
		ps.print(TAB + "pthread_t" + SPACE + "behav_thread" + SEMI + NEWLINE);
		ps.print(TAB + "sem_t" + SPACE + "component_create_sem" + SEMI + NEWLINE);
		// ps.print( TAB + STRUCT_ + "process " + STAR + "process_pntr" + SEMI + NEWLINE ); // Contiki process pntr // TODO NOT NEEDED INCEOS?
		// ps.print( TAB + STRUCT_ + "process " + STAR + "caller" + SEMI + NEWLINE ); // Contiki process pntr // TODO NOT NEEDED INCEOS?
		// ps.print( TAB + STRUCT_ + funcs_name( componentname ) + STAR + "impl " + SEMI + NEWLINE); // TODO NOT NEEDED INCEOS?
		// ps.print( TAB + "ChannelPayloadStruct" + SPACE + "data_struct" + SEMI + NEWLINE ); // TODO NOT NEEDED INCEOS?
		// ps.print( TAB + "List_PNTR" + SPACE + "guard_ready_set" + SEMI + NEWLINE); // TODO NOT NEEDED INCEOS?
		// ps.print( TAB + "HalfChannel_PNTR" + SPACE + "branch_result" + SEMI + NEWLINE); // TODO NOT NEEDED INCEOS?
	}

	/**
	 * Writes the declarations for Channels declared in the component interface to the stream
	 * 
	 * @param ps
	 *            - the stream on which the decls are written
	 */
	private void printStructChannelDecls(PrintStream ps) {
		ps.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		Set<String> ks = channels.keySet();
		// Alphabetically ordered set of channel names
		for (String s : channel_names) {
			ITypeRep tr = channels.get(s);
			ps.println(TAB + CHANNEL_ + s + SEMI);
		}
	}

	/**
	 * Writes the declarations for channels and local variables to the stream
	 * 
	 * @param ps
	 *            - the stream on which the decls are written
	 */
	private void printStructDecl(PrintStream ps) {
		ps.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		ps.println(TYPEDEF_ + STRUCT_ + componentname + SPACE + "*" + data_pntr_name(componentname) + COMMA + SPACE + data_struct_name(componentname) + SEMI);
		ps.println(STRUCT_ + componentname + SPACE + LCB_);
		printCommonDecls(ps);
		printStructChannelDecls(ps);
		printLocationDecls(ps);
		ps.println();
		ps.println(RCB_ + SEMI);
		ps.println();
	}

	/**
	 * Writes the C includes necessary for the component implementation
	 * 
	 * @param ps
	 *            - the stream on which the decls are written
	 */
	private void printImplIncludes(PrintStream ps) {
		ps.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		// ps.println( HASH_INCLUDE_ + DQUOTE + getHeaderFileName() + DQUOTE );
		ps.println(HASH_INCLUDE_ + DQUOTE + "main.h" + DQUOTE);
		ps.println(HASH_INCLUDE_ + DQUOTE + "semaphore.h" + DQUOTE);
		ps.println(HASH_INCLUDE_ + DQUOTE + "pthread.h" + DQUOTE);
		// ps.println( HASH_INCLUDE_ + DQUOTE + "IComponent.h" + DQUOTE );
		// ps.println( HASH_INCLUDE_ + DQUOTE + "sys/lc.h" + DQUOTE );
		// ps.println( HASH_INCLUDE_ + DQUOTE + "InsenseRuntime.h" + DQUOTE );
		ps.println("// TODO put remainder of impl includes here");
		ps.println(generateExternalIncludes());
		ps.println();
	}

	/**
	 * Writes the impl for the setter and getter functions for channels to the stream
	 * 
	 * @param ps
	 *            - the stream on which the decls are written
	 */

	// TODO NOT NEEDED INCEOS?
	/*
	 * void printStructChannelGetterImpls(PrintStream ps) {
	 * ps.println( GENERATED_FROM + Diagnostic.getMethodInCallChain() );
	 * ps.println( "// setters and getters" );
	 * Set<String> ks = channels.keySet();
	 * //for( String s : ks ) {
	 * // JL using alphabetically ordered set of channel names
	 * for( String s : channel_names ) {
	 * // generate getter
	 * 
	 * printStructChannelGetterSignature( ps, s, channels.get( s ), 0, false );
	 * ps.println( LCB_ );
	 * ps.println( TAB + "return" + SPACE + AMPERSAND + THIS + ARROW + s + SEMI );
	 * ps.println( RCB_ );
	 * ps.println();
	 * }
	 * 
	 * }
	 */

	/**
	 * Writes the declaration for the constructor functions to the stream
	 * 
	 * @param ps
	 *            - the stream on which the decls are written
	 */
	private void printConstructorSignatures(PrintStream ps) {
		ps.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		int i = 0;
		for (IConstructor cc : this.constructors) {
			ps.print(EXTERN_);
			ps.print(cc.constructorFunctionSignature());
			ps.println(SEMI);
		}
		ps.println();
	}

	/**
	 * Writes a constructor signature to ps
	 * 
	 * @param ps
	 *            - the stream on which the decls are written
	 * @param ft
	 *            - the typeof the (constructor) function
	 * @param - paramNames a list of the parameter names
	 * @param - the constructor index
	 */
	private void printConstructorSignature(PrintStream ps, FunctionType ft, List<String> paramNames, int index) {
		// ps.println( GENERATED_FROM + Diagnostic.getMethodInCallChain() );
		ps.print(data_pntr_name(componentname) + constructor_name(componentname, index));
		printParamSignatures(ps, ft, paramNames);
	}

	private String behaviourSignature() {
		return VOID_ + behaviour_name + LRB_ + data_pntr_name(componentname) + THIS_ + RRB_;
	}

	/**
	 * Writes the implementation for the behaviour function to the stream
	 * 
	 * @param ps
	 *            - the stream on which the decls are written
	 */
	private void printBehaviourImpl(PrintStream ps) {
		ps.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		ps.println(behaviourSignature() + LCB_);	// start of behaviour definintion

		// ps.println(TAB + "inceos_event_t op_status" + SEMI + "// for exception handling" + NEWLINE);

		ps.println(TAB + "int i = 0" + SEMI); // Limit number of runs of the bahviour.

		ps.println(TAB + WHILE + LRB_ + NOT_ + THIS + ARROW + "stopped" + SPACE + RRB_ + LCB_);					// start of behaviour while loop
		ps.println(TAB + IF + LRB_ + "i == BEHAVIOUR_COUNT && BEHAVIOUR_COUNT != -1" + RRB_ + SPACE + "component_stop" + LRB_ + "this" + RRB_ + SEMI); // Limit
																																						// on
																																						// number
																																						// of
																																						// runs
																																						// of
																																						// the
		// behaviour, limit
		// defined in
		// GlboalVars.h in runtime
		ps.println(TAB + "i = i + 1" + SEMI);

		if (behaviour != null) {
			ps.println(((Behaviour) behaviour).generateHoistedCode());
			ps.println(TAB + behaviour.toString());
		}
		ps.println(TAB + RCB_);												// end of behaviour while loop
		ps.println(TAB + functionCall("component_exit") + SEMI + NEWLINE);

		ps.println(RCB_);														// end of behaviour definintion
		ps.println();

	}

	// JL new decRef stuff for ref counting garbage collection

	private String decRefPrototype() {
		return "static " + VOID_ + "decRef_" + componentname + LRB_ + data_pntr_name(componentname) + "this" + SPACE + RRB;
	}

	private String componentFieldDecrementers() {
		StringBuffer sb = new StringBuffer();
		// Set<String> ks = locations.keySet();
		// for( String s : ks ) {
		for (IDecl l : locations) {
			ITypeRep tr = l.getType();
			if (tr.isPointerType()) {
				if (tr instanceof ChannelType && l.getSymbolTableEntry().getContext() > ISymbolTable.COMPONENT) {

					// sb.append(TAB + IF + LRB + l.getSymbolTableEntry().contextualName(ISymbolTable.COMPONENT) + ARROW + "LCN" + RRB + LCB_ + NEWLINE);
					// sb.append(tab(2) + functionCall("Call_unbind_proc", THIS+ARROW+"process_pntr", NULL, "inch_comp" + ARROW + "node_addr",
					// l.getSymbolTableEntry().contextualName(ISymbolTable.COMPONENT) + ARROW + "LCN") + SEMI + NEWLINE);
					// sb.append(tab(2) + functionCall(TAB + "PROCESS_WAIT_EVENT_UNTIL", functionCall("isReturnOrException", "ev") )+ SEMI + NEWLINE);
					// sb.append(TAB + RCB_ + NEWLINE);
					sb.append(TAB + functionCall("channel_unbind", l.getSymbolTableEntry().contextualName(ISymbolTable.COMPONENT)) + SEMI + NEWLINE);
					// TODO need InceOS channel decRef
					sb.append(TAB + functionCall("// TODO need InceOS Channel_decRef", l.getSymbolTableEntry().contextualName(ISymbolTable.COMPONENT)) + SEMI
							+ NEWLINE);
				} else if (!(tr instanceof ChannelType) && l.getSymbolTableEntry().getContext() == ISymbolTable.COMPONENT) {
					sb.append(TAB + functionCall("DAL_decRef", l.getSymbolTableEntry().contextualName(ISymbolTable.BEHAVIOUR)) + SEMI + NEWLINE);
				}
			}
		}
		// ks = channels.keySet();
		// for( String s : ks ) {
		// JL using alphabetically ordered set of channel names
		for (String s : channel_names) {
			ITypeRep tr = channels.get(s);
			sb.append(TAB + functionCall("channel_unbind", "this" + ARROW + s) + SEMI + NEWLINE);
			// TODO need InceOS channel decRef
			sb.append(TAB + functionCall("// TODO need InceOS Channel_decRef", "this" + ARROW + s) + SEMI + NEWLINE);
		}
		return sb.toString();
	}

	private String generateDecRef() {
		StringBuffer sb = new StringBuffer();
		sb.append(NEWLINE + GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE);
		sb.append(decRefPrototype() + SPACE + LCB_ + NEWLINE);
		sb.append(componentFieldDecrementers());
		// sb.append( TAB + functionCall( "DAL_decRef" , AMPERSAND + THIS + ARROW + "process_pntr" + ARROW + "name" ) + SEMI + NEWLINE );
		// sb.append( TAB + functionCall( "DAL_decRef" , THIS + ARROW + "process_pntr" ) + SEMI + NEWLINE );
		sb.append(NEWLINE + RCB_ + NEWLINE);
		sb.append(NEWLINE);
		return sb.toString();
	}

	/**
	 * Writes the implementation for the constructor functions to the stream
	 * 
	 * @param ps
	 *            - the stream on which the decls are written
	 */
	private void printConstructorImpls(PrintStream ps) {
		ps.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		printConstructorGlobalInitialiser(ps);
		// printConstructorFunctionAssigners( ps ); // TODO NOT NEEDED INCEOS? <<<<<<<<<<<<<<<<<<<<
		for (IConstructor cc : constructors) {
			ps.println(cc.generateCode(this));
		}
		ps.println();
	}

	/**
	 * Writes the definition of a function to initialise component globals
	 * 
	 * @param ps
	 *            - the stream on which the decls are written
	 */
	private void printConstructorGlobalInitialiser(PrintStream ps) {
		ps.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		ps.println(VOID_ + initGlobalsName + LRB_ + data_pntr_name(componentname) + THIS_ + RRB_);
		ps.println(LCB_);
		// ps.println( TAB + THIS + ARROW + IMPL + EQUALS + vtbl_global_name( componentname ) + SEMI ); // TODO NOT NEEDED FOR INCEOS?
		ps.println(TAB + THIS + ARROW + "decRef " + EQUALS_ + "decRef_" + componentname + SEMI);
		// ps.println( TAB + THIS + ARROW + "stopped " + EQUALS_ + "false" + SEMI);// TODO NOT NEEDED FOR INCEOS?
		printChannelInitialisers(ps);
		ps.println(locationInitialisers());
		ps.println(RCB_);
		ps.println();
	}

	/**
	 * @param ps
	 *            - the stream on which the initialisers are written
	 * @return initialisers for incoming channels to this component
	 */
	private void printChannelInitialisers(PrintStream ps) {
		// Alphabetically ordered set of channel names
		for (String s : channel_names) {
			ChannelType ct = channels.get(s);
			// alloc sets to 0
			ps.println(TAB + THIS + ARROW + s + EQUALS + channelConstructorName(ct) + SEMI);
		}
	}

	/**
	 * Writes the forward declaration of the behaviour function to the stream
	 * 
	 * @param ps
	 *            - the stream on which the decls are written
	 */
	protected void printBehaviourForwardDecl(PrintStream ps) {
		ps.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		ps.println(EXTERN_ + behaviourSignature() + SEMI);
	}

	// /**
	// * Writes the implementation for the component functions to the stream
	// * This is the equivalent of the VTBL functions for C++
	// * @param ps - the stream on which the decls are written
	// */
	// protected void printComponentFuncsCallFunctionForwardDecl(PrintStream ps) {
	// ps.println( GENERATED_FROM + Diagnostic.getMethodInCallChain() ); // // TODO NOT NEEDED FOR INCEOS?
	// Collection<IFunction> impls = functionBodies.values();
	// for( IFunction proc : impls ) {
	// ps.println(proc.processConstructorFunctionForwardDecl());
	// }
	// ps.println();
	// }

	// TODO NOT NEEDED INCEOS?
	/**
	 * Writes the implementation for the component functions to the stream
	 * This is the equivalent of the VTBL functions for C++
	 * 
	 * @param ps
	 *            - the stream on which the decls are written
	 */
	protected void printComponentFuncsImpl(PrintStream ps) {
		ps.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		Collection<IFunction> impls = functionBodies.values();
		for (IFunction code : impls) {
			ps.println(code.generateCode(this));
		}
		// printStructChannelGetterImpls( ps );
		ps.println();
	}

	// protected void printComponentLocalFuncsAssigners(PrintStream ps) { // TODO NOT NEEDED FOR INCEOS?
	// ps.println( GENERATED_FROM + Diagnostic.getMethodInCallChain() );
	// Collection<IFunction> impls = functionBodies.values();
	// for( IFunction code : impls ) {
	// ps.println( TAB + FUNCS + ARROW + code.getName() + EQUALS + Function.functionName(code.getName()) + SEMI );
	// }
	// }

	/**
	 * Writes the declaration for the component functions to the stream
	 * 
	 * @param ps
	 *            - the stream on which the decls are written
	 */
	protected void printComponentFuncsDecls(PrintStream ps) {
		ps.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		Collection<IFunction> impls = functionBodies.values();
		for (IFunction code : impls) {
			ps.println(((Function) code).functionSignature() + SEMI);
		}
		ps.println();
	}

	/**
	 * Writes the parameter signaure list for functions to the stream
	 * 
	 * @param ps
	 *            - the stream on which the decls are written
	 * @param ft
	 *            - the function whose parmater list is being generated
	 */
	private void printParamSignatures(PrintStream ps, FunctionType ft, List<String> paramNames) {
		ps.print(paramSignatures(ft, paramNames));
	}

	/**
	 * Writes the standard #ifdef conditional compilation code to the stream
	 * 
	 * @param ps
	 *            - the stream on which the decls are written
	 */
	private void print_ifdef(PrintStream ps) {
		ps.println(GENERATED_FROM + Diagnostic.getMethodInCallChain());
		ps.println(IFNDEF_ + header_name(componentname));
		ps.println(DEFINE_ + header_name(componentname));
		ps.println();
	}

	/**
	 * Writes the standard include file trailers to the stream
	 * 
	 * @param ps
	 *            - the stream on which the decls are written
	 */
	private void printTrailers(PrintStream ps) {
		ps.println(ENDIF_ + C_COMMENT_OPEN_ + header_name(componentname) + C_COMMENT_CLOSE_);
		ps.println();
	}

	// File Handling

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IComponent#getHeaderFileName()
	 */
	@Override
	public String getHeaderFileName() {
		return FILENAMESTART + filename + FILENAMEHEADERTAIL;
	}

	private String getImplFileName() {
		return FILENAMESTART + filename + FILENAMEIMPLTAIL;
	}

	private String getPropertiesFileName() {
		return filename + FILENAMEPROPERTIESTAIL;
	}

	private String generateExternalIncludes() {
		StringBuffer sb = new StringBuffer();
		for (String s : externalIncludes) {
			sb.append(s + NEWLINE);
		}
		return sb.toString();
	}

	/**
	 * @return empty string - prevents the bodies from cascading to a global level
	 */
	@Override
	public String toString() {
		return "";
	}

	private void generateHeaderFile() {
		try {
			OutputFile f = new HeaderFile(getHeaderFileName());
			PrintStream ps = f.getStream();
			// PrintStream ps = System.out; // for debugging
			// Traverse data structures
			print_ifdef(ps);
			printImplIncludes(ps);
			printStructDecl(ps);
			printConstructorArrayFunctionDecls(ps);
			// printCopyMacros( ps ); // JL copy macros not needed anymore, so removed, check that this is ok
			printComponentFuncsDecls(ps);
			printConstructorSignatures(ps);
			printBehaviourForwardDecl(ps);
			printTrailers(ps);
			f.close();
		} catch (IOException e) {
			ErrorHandling.exceptionError(e, "Opening file: " + getHeaderFileName());
		}
	}

	private void printConstructorArrayFunctionDecls(PrintStream ps) {
		StringBuffer sb = new StringBuffer();
		sb.append(GENERATED_FROM + Diagnostic.getMethodInCallChain() + NEWLINE);
		for (IConstructor constructor : constructors) {
			if (constructor.getFt().getArgs().size() > 0) {
				sb.append(EXTERN_ + constructor.arrayFunctionSignature() + SEMI + NEWLINE);
			}
		}
		ps.println(sb.toString());
	}

	// used by DAL_error macro
	private void printDALErrorFileName(PrintStream ps) {
		ps.println("#ifndef DALSMALL");
		ps.println("static char *file_name = \"" + componentname + "\";");
		ps.println("#endif");
	}

	private void generateImplFile() {
		try {
			OutputFile f = new ImplFile(getImplFileName());
			PrintStream ps = f.getStream();
			// PrintStream ps = System.out; // for debugging
			// Traverse data structures
			printImplIncludes(ps);
			printDALErrorFileName(ps);
			// ps.println( EXTERN_ + // TODO NOT NEEDED INCEOS?
			// funcs_pntr_name( componentname ) + SPACE +
			// vtbl_global_name( componentname ) + SEMI );
			printHoistedCode(ps);
			ps.print(generateDecRef());
			// printComponentFuncsCallFunctionForwardDecl( ps );
			printComponentFuncsImpl(ps); // TODO NOT NEEDED FOR INCEOS?
			printBehaviourImpl(ps);
			printConstructorImpls(ps);
			f.close();
		} catch (IOException e) {
			ErrorHandling.exceptionError(e, "Opening file: " + getImplFileName());
		}
	}

	public String getFilename() {
		return filename;
	}

	public String getComponentname() {
		return componentname;
	}

	public String getInitGlobalsName() {
		return initGlobalsName;
	}

	public HashMap<String, ChannelType> getChannels() {
		return channels;
	}

	public ConcurrentSkipListSet<String> getChannel_names() {
		return channel_names;
	}

	public String getBehaviour_name() {
		return behaviour_name;
	}

	public String getProcess_name() {
		return process_name;
	}

	public int getConstructorDisambiguator() {
		return constructorDisambiguator;
	}

	public String getOutputted_component_name() {
		return outputted_component_name;
	}

	public void setContains_stop_statement(boolean contains_stop_statement) {
		this.contains_stop_statement = contains_stop_statement;
	}
}
