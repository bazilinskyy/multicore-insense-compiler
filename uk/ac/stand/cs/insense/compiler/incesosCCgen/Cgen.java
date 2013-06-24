package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.util.List;
import java.util.Stack;
import uk.ac.stand.cs.insense.compiler.cgen.IAcknowledge;
import uk.ac.stand.cs.insense.compiler.cgen.IAnyConstructor;
import uk.ac.stand.cs.insense.compiler.cgen.IAnyProject;
import uk.ac.stand.cs.insense.compiler.cgen.IArrayConstructor;
import uk.ac.stand.cs.insense.compiler.cgen.IArrayDereference;
import uk.ac.stand.cs.insense.compiler.cgen.IAssign;
import uk.ac.stand.cs.insense.compiler.cgen.IBehaviour;
import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.cgen.ICodeGenerator;
import uk.ac.stand.cs.insense.compiler.cgen.ICompilationUnit;
import uk.ac.stand.cs.insense.compiler.cgen.IComponent;
import uk.ac.stand.cs.insense.compiler.cgen.IConditional;
import uk.ac.stand.cs.insense.compiler.cgen.IConnect;
import uk.ac.stand.cs.insense.compiler.cgen.IConstructor;
import uk.ac.stand.cs.insense.compiler.cgen.IConstructorCall;
import uk.ac.stand.cs.insense.compiler.cgen.IDecl;
import uk.ac.stand.cs.insense.compiler.cgen.IDeclarationContainer;
import uk.ac.stand.cs.insense.compiler.cgen.IDereference;
import uk.ac.stand.cs.insense.compiler.cgen.IDisconnect;
import uk.ac.stand.cs.insense.compiler.cgen.IExceptionBlock;
import uk.ac.stand.cs.insense.compiler.cgen.IForLoop;
import uk.ac.stand.cs.insense.compiler.cgen.IFunction;
import uk.ac.stand.cs.insense.compiler.cgen.IMakefile;
import uk.ac.stand.cs.insense.compiler.cgen.IProcCall;
import uk.ac.stand.cs.insense.compiler.cgen.IProcedureContainer;
import uk.ac.stand.cs.insense.compiler.cgen.IPublish;
import uk.ac.stand.cs.insense.compiler.cgen.IReceive;
import uk.ac.stand.cs.insense.compiler.cgen.IReturn;
import uk.ac.stand.cs.insense.compiler.cgen.ISelect;
import uk.ac.stand.cs.insense.compiler.cgen.ISend;
import uk.ac.stand.cs.insense.compiler.cgen.ISequence;
import uk.ac.stand.cs.insense.compiler.cgen.IStop;
import uk.ac.stand.cs.insense.compiler.cgen.IStructConstructor;
import uk.ac.stand.cs.insense.compiler.cgen.ISwitch;
import uk.ac.stand.cs.insense.compiler.cgen.IThrow;
import uk.ac.stand.cs.insense.compiler.interfaces.ICompilerErrors;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.symbols.STEntry;
import uk.ac.stand.cs.insense.compiler.symbols.Symbols;
import uk.ac.stand.cs.insense.compiler.types.ChannelType;
import uk.ac.stand.cs.insense.compiler.types.EnumType;
import uk.ac.stand.cs.insense.compiler.types.FunctionType;
import uk.ac.stand.cs.insense.compiler.types.InterfaceType;
import uk.ac.stand.cs.insense.compiler.types.StructType;

public class Cgen implements ICodeGenerator {

	private static Cgen instance;

	private ICode currentCode = null;		 // The current code body being compled
	private Stack<ICode> bodyStack = null;   // keeps track of bodies in nested contexts
	private final IMakefile makefile;
	private IFunction currentProcedure;

	private final ICompilerErrors compilerErrors;

	public Cgen(ICompilerErrors ce, String project_name) {
		this.compilerErrors = ce;
		currentCode = new Sequence(null);
		bodyStack = new Stack<ICode>();
		instance = this;
		makefile = new MakefileInceOS(project_name);
	}

	@Override
	public void finish() {
		makefile.generateMakeFile();
	}

	public static Cgen get_instance() {
		return instance;
	}

	// Method for dealing with Code context

	@Override
	public String popLastAppend() {
		return currentCode.pop();
	}

	@Override
	public ICode popCurrentCode() {
		currentCode.complete();
		ICode returnCode = currentCode;

		if (!bodyStack.empty())
			currentCode = bodyStack.pop();
		else
			currentCode = new Sequence();

		return returnCode;
	}

	@Override
	public void complete() {
		if (compilerErrors.getErrorCount() == 0) {
			String s = popCurrentCode().toString();
			currentCode.append(s);
		}
	}

	private void newCodeBody(ICode newBody) {
		bodyStack.push(currentCode);
		currentCode = newBody;
	}

	// dealing with hoisted code

	// JL should we rename addhoistedCodeToCurrentContext to ...ComponentContext
	// or add further contexts here?
	public void addHoistedCodeToComponentContext(String s) {
		// look back the stack until we find a Component
		int lastIndex = bodyStack.size() - 1;
		for (int i = lastIndex; i > 0; i--) {
			ICode thisone = bodyStack.get(i);
			if (thisone instanceof Component) {
				((IComponent) thisone).addHoistedCode(s);
				return;
			}
		}
		throw new RuntimeException("Didn't find Component when adding hoisted code");
	}

	public void addHoistedCodeToComponentOrCompilationUnitContext(String s) {
		// look back the stack until we find a Component
		int lastIndex = bodyStack.size() - 1;
		for (int i = lastIndex; i > 0; i--) {
			ICode thisone = bodyStack.get(i);
			if (thisone instanceof Component) {
				((IComponent) thisone).addHoistedCode(s);
				return;
			} else if (thisone instanceof CompilationUnit) {
				((ICompilationUnit) thisone).addHoistedCode(s);
				return;
			}
		}
		throw new RuntimeException("Didn't find Component or Compilation Unit when adding hoisted code");
	}

	// JL should we look back on stack for next valid context for code hoisting e.g.
	// hoisting makes sense for ?expression?, procedure, behaviour, component, sequence ...
	public void addHoistedCodeToCurrentContext(String s) {
		// look back the stack until we find valid context for hoisting
		int lastIndex = bodyStack.size() - 1;
		for (int i = lastIndex; i >= 0; i--) {
			ICode thisone = bodyStack.get(i);
			if (thisone instanceof Sequence) {
				((ISequence) thisone).addHoistedCode(s);
				return;
			} else if (thisone instanceof Function) {
				((IFunction) thisone).addHoistedCode(s);
				return;
			} else if (thisone instanceof Behaviour) {
				((IBehaviour) thisone).addHoistedCode(s);
				return;
			} else if (thisone instanceof Component) {
				((IComponent) thisone).addHoistedCode(s);
				return;
			}
		}
		throw new RuntimeException("Didn't find suitable context for code hoisting");
	}

	// dealing with includes

	@Override
	public void addIncludeToCurrentContext(String s) {
		// look back the stack until we find a CompilationUnit or a Component
		ICode thisone = null;

		// not sure if needed
		thisone = currentCode;
		if (thisone instanceof Component) {
			((IComponent) thisone).addExternalIncludes(Code.HASH_INCLUDE_ + Code.DQUOTE + s + Code.DQUOTE);
			return;
		} else if (thisone instanceof CompilationUnit) {
			((ICompilationUnit) thisone).addExternalInclude(Code.HASH_INCLUDE_ + Code.DQUOTE + s + Code.DQUOTE);
			// System.out.println("adding '" + s + "' to " + ((ICompilationUnit) thisone ));
			return;
		}
		int lastIndex = bodyStack.size() - 1;
		for (int i = lastIndex; i > 0; i--) {
			thisone = bodyStack.get(i);
			if (thisone instanceof StructDeclaration) {
				((StructDeclaration) thisone).addExternalIncludes(Code.HASH_INCLUDE_ + Code.DQUOTE + s + Code.DQUOTE);
				return;
			} else if (thisone instanceof Component) {
				((IComponent) thisone).addExternalIncludes(Code.HASH_INCLUDE_ + Code.DQUOTE + s + Code.DQUOTE);
				return;
			} else if (thisone instanceof CompilationUnit) {
				((ICompilationUnit) thisone).addExternalInclude(Code.HASH_INCLUDE_ + Code.DQUOTE + s + Code.DQUOTE);
				return;
			}
		}
		throw new RuntimeException("Didn't find Compilation Unit or Component when adding includes");
	}

	// General code generation methods

	@Override
	public ICompilationUnit compilationUnit() {
		CompilationUnit code = new CompilationUnit();
		newCodeBody(code);
		return code;
	}

	@Override
	public IComponent componentBody(String component_name) {
		Component newbody = new Component(component_name);
		newCodeBody(newbody);
		return newbody;
	}

	@Override
	public void channel(IDecl loc, ChannelType ct) {
		currentCode.append(Code.channelConstructorName(ct));
		// TODO JL Space Tracking
		ICode call_stack = findEnclosingDelcarationContainer();
		call_stack.track_call_space(MSP430Sizes.channelCreateCallOverhead());
	}

	@Override
	public void channel(STEntry entry) {
		IDeclarationContainer container = findEnclosingDelcarationContainer();
		if (currentCode instanceof Component) {	// Usual case
			if (entry.getType() instanceof ChannelType) {
				((IComponent) currentCode).addChannel(entry.getName(), (ChannelType) entry.getType());
			} else if (entry.getType() instanceof InterfaceType) {
				((IComponent) currentCode).addInterface(entry.getName(), (InterfaceType) entry.getType());
			} else {
				throw new RuntimeException("unknown type encountered in channel definition" + entry.getType().getClass().getName());
			}
		} else if (container instanceof Component) {
			if (entry.getType() instanceof ChannelType) {
				((IComponent) container).addChannel(entry.getName(), (ChannelType) entry.getType());
			} else if (entry.getType() instanceof InterfaceType) {
				((IComponent) container).addInterface(entry.getName(), (InterfaceType) entry.getType());
			} else {
				throw new RuntimeException("unknown type encountered in channel definition" + entry.getType().getClass().getName());
			}
		} else if (currentCode instanceof CompilationUnit) { // this is a top level interface definition - no code generation needed
			// do nothing
		} else {	// error situation
			throw new RuntimeException("unknown context for channel definition" + currentCode.getClass().getName());
		}
	}

	@Override
	public ICode procedureBody(String the_name, FunctionType ft, List<String> names, int scope_level) {
		IFunction newbody = new Function((IProcedureContainer) findEnclosingDelcarationContainer(), the_name, ft, names, scope_level);
		currentProcedure = newbody;
		((IDeclarationContainer) currentCode).addFunction(newbody);
		newCodeBody(newbody);
		return newbody;
	}

	@Override
	public IBehaviour behaviourBody() {
		Behaviour newbody = new Behaviour(findEnclosingDelcarationContainer());
		if (currentCode instanceof IComponent) {	// may not be if an error has occurred
			((IComponent) currentCode).addBehaviour(newbody);
			newCodeBody(newbody);
		}
		return newbody;
	}

	@Override
	public ICode constructorBody(FunctionType ft, List<String> names, int scope_level) {
		IConstructor newbody = new Constructor(((IProcedureContainer) findEnclosingDelcarationContainer()), ft, names, scope_level);
		if (currentCode instanceof IComponent) {					// it may not be if an error has occurred.
			((IComponent) currentCode).addConstructor(newbody);
			newCodeBody(newbody);
		}
		return newbody;
	}

	@Override
	public IDecl newLocation(String name, int scopeLevel) {
		Decl newcode = new Decl(name, scopeLevel);
		if (currentCode instanceof IDeclarationContainer) {	// following compilation error
			((IDeclarationContainer) currentCode).addLocation(newcode);
			// TODO check out error here in a for loop
			// loop can't be cast to IDeclarationContainer
		}
		// If this decl is within a function add it to a list recording
		// decls that are in scope and may require garbage collection
		Function f = findEnclosingFunctionContainer();
		if (f != null) {
			f.getLocalDeclsInScope().add(newcode);
		}
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public void locationUsage(STEntry entry, int fromContext) {
		if (entry != null) {
			currentCode.append(entry.contextualName(fromContext));
		}
	}

	@Override
	public void unaryOp(String op) {
		currentCode.append(op);
	}

	@Override
	public void insertIntoCodeStream(String s) {
		currentCode.append(s);
	}

	@Override
	public IProcCall proc_call(STEntry ste, ISymbolTable context) {
		if (context.getContext() == ISymbolTable.CONSTRUCTOR)
			compilerErrors.warning("procedure calls from constructors can cause deadlock if procedures use unconnected component channels");
		ProcCall newcode = new ProcCall(ste, context, findComponentContainer());
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public void proc_call_end() {
		IExceptionBlock exception_code = findNearestEnclosingTryExceptBlock();
		if (exception_code != null) {
			((ProcCall) currentCode).setInTryBlock(true);
		}
	}

	@Override
	public IArrayConstructor arrayConstructor() {
		IArrayConstructor newcode = new ArrayConstructor(compilerErrors);
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public IDereference dereference(STEntry ste, int fromContext) {
		Dereference newcode = new Dereference(ste, fromContext);
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public IArrayDereference arrayDereference(STEntry ste) {
		ArrayDereference newcode = new ArrayDereference(ste);
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public IAssign assign(STEntry ste, int fromContext) {
		IAssign newcode = new Assign(ste, fromContext); // new Assign( (Sequence) currentCode );
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public void binaryOp(String op) {
		if (op.equals(Symbols.or_s)) {
			currentCode.append(Code.OROR_);
		} else if (op.equals(Symbols.and_s)) {
			currentCode.append(Code.ANDAND_);
		} else {
			currentCode.append(op.toString());
		}
	}

	@Override
	public IConstructorCall constructorCall(String the_name) {
		ConstructorCall newcode = new ConstructorCall(the_name);
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public void setRuntimeComponentFlags(String code) {
		String glob_suffix = "_glob";
		// replace strings with ?STE? lookup of defs in Standard_Defs.txt
		if (code.contains("buttonSensor" + glob_suffix))
			MakefileInceOS.setDAL_BUTTON(true);
		if (code.contains("lightHumidTempSensor" + glob_suffix))
			MakefileInceOS.setDAL_SENSORS(true);
		if (code.contains("leds" + glob_suffix))
			MakefileInceOS.setDAL_LEDS(true);
		if (code.contains("standardOut" + glob_suffix))
			MakefileInceOS.setDAL_STDOUT(true);
		if (code.contains("radioIn" + glob_suffix) || code.contains("radioOut" + glob_suffix) || code.contains("radio" + glob_suffix)
				|| code.contains("setRadioPower"))
			MakefileInceOS.setDAL_RADIO(true);
		if (code.contains("radioIn") || code.contains("setRadioPower")) {
			MakefileInceOS.setDAL_RECEIVE(true);
		}
		if (code.contains("publishOut") || code.contains("publishIn") || code.contains("bind") || code.contains("unbind") || code.contains("getNumberHopsTo")
				|| code.contains("findNodesPublishing") || code.contains("getNeighbours") || code.contains("getNeighboursOf")
				|| code.contains("getPublicChannels") || code.contains("getPublicChannelsOf") || code.contains("enableInterNodeChannelRouting"))
			MakefileInceOS.setDAL_INTERNODECHANNEL(true);
		if (code.contains("periodicEnSchedule")) {
			MakefileInceOS.setDAL_SCHEDULER(true);
		}

	}

	@Override
	public IConnect connect(int fromContext) {
		// if(fromContext == ISymbolTable.CONSTRUCTOR)
		// compilerErrors.warning("connect not allowed in constructors");
		IConnect newcode = new Connect(fromContext);
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public void connect_call_end() {
		IExceptionBlock exception_code = findNearestEnclosingTryExceptBlock();
		if (exception_code != null) {
			((Connect) currentCode).setInTryBlock(true);
		}
	}

	@Override
	public IPublish publish(ISymbolTable fromContext) {
		Publish newcode = new Publish(fromContext);
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public void publish_call_end() {
		IExceptionBlock exception_code = findNearestEnclosingTryExceptBlock();
		if (exception_code != null) {
			((Publish) currentCode).setInTryBlock(true);
		}
	}

	@Override
	public IDisconnect disconnect(int fromContext) {
		// if(fromContext == ISymbolTable.CONSTRUCTOR)
		// compilerErrors.generalError("disconnect not allowed in constructors");
		IDisconnect newcode = new Disconnect(fromContext);
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public void disconnect_call_end() {
		IExceptionBlock exception_code = findNearestEnclosingTryExceptBlock();
		if (exception_code != null) {
			((Disconnect) currentCode).setInTryBlock(true);
		}
	}

	@Override
	public IForLoop forLoop(ISymbolTable for_table) {
		ForLoop newcode = new ForLoop(for_table);
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public IConditional ifClause() {
		Conditional newcode = new Conditional();
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public void literal(String literal) {
		currentCode.append(literal);
	}

	@Override
	public void stringLiteral(String literal) {
		currentCode.append("Construct_String0(" + literal + ")");
	}

	@Override
	public IReceive receive(STEntry lhs, STEntry rhs, int fromContext) {
		if (fromContext == ISymbolTable.CONSTRUCTOR)
			compilerErrors.warning("receive on unconnected component channels from constructors causes deadlock");
		Receive newcode = new Receive(lhs, rhs, fromContext);
		if (currentCode != null && currentCode instanceof DeclarationContainer) {
			// following compilation error
			// this is always the case in non-erroneous programs
			((DeclarationContainer) currentCode).addLocation(newcode);
		}
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public IAcknowledge acknowledge(STEntry lhs, STEntry rhs, int fromContext) {
		if (fromContext == ISymbolTable.CONSTRUCTOR)
			compilerErrors.warning("acknowledge x from y on unconnected component channels from constructors causes deadlock");
		Acknowledge newcode = new Acknowledge(lhs, rhs, fromContext);
		if (currentCode != null && currentCode instanceof DeclarationContainer) {	// following compilation error
			// this is always the case in non-erroneous programs
			((DeclarationContainer) currentCode).addLocation(newcode);
		}
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public ISelect select(int fromContext) {
		if (fromContext == ISymbolTable.CONSTRUCTOR)
			compilerErrors.warning("select on unconnected component channels from constructors can cause deadlock");
		Select newcode = new Select(fromContext);
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public ISend send(int fromContext) {
		if (fromContext == ISymbolTable.CONSTRUCTOR)
			compilerErrors.warning("send on unconnected component channels from constructors causes deadlock");
		Send newcode = new Send(fromContext);
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public void send_call_end() {
		IExceptionBlock exception_code = findNearestEnclosingTryExceptBlock();
		if (exception_code != null) {
			((Send) currentCode).setInTryBlock(true);
		}
	}

	@Override
	public IStop stop(int fromContext) {
		if (fromContext == ISymbolTable.CONSTRUCTOR)
			compilerErrors.warning("stop not allowed in constructors");
		Stop newcode = new Stop(fromContext);
		Component container = findComponentContainer();
		if (container != null)
			container.setContains_stop_statement(true);
		newCodeBody(newcode);
		return newcode;
	}

	public IExceptionBlock findNearestEnclosingTryExceptBlock() {
		if (currentCode instanceof ExceptionBlock)
			return (IExceptionBlock) currentCode;
		// then check body stack from top to bottom (can't use standard iterator ;-()
		int size = bodyStack.size();
		if (size > 0) {
			for (int i = size - 1; i >= 0; i--) { // Eeeek impl dependent
				ICode code = bodyStack.get(i);
				if (code instanceof ExceptionBlock) {
					return (IExceptionBlock) code;
				}
			}
		}
		return null;
	}

	public Component findComponentContainer() {
		if (currentCode instanceof Component)
			return (Component) currentCode;
		// then check body stack from top to bottom (can't use standard iterator ;-()
		int size = bodyStack.size();
		if (size > 0) {
			for (int i = size - 1; i >= 0; i--) { // Eeeek impl dependent
				ICode code = bodyStack.get(i);
				if (code instanceof Component) {
					return (Component) code;
				}
			}
		}
		return null;
	}

	@Override
	public IDeclarationContainer findEnclosingDelcarationContainer() {
		// check currentCode first
		// looking for most specific first
		if (currentCode instanceof Function) {
			return (Function) currentCode;
		}
		if (currentCode instanceof Constructor) {
			return (Constructor) currentCode;
		}
		if (currentCode instanceof Component) {
			return (Component) currentCode;
		}
		if (currentCode instanceof CompilationUnit) {
			return (CompilationUnit) currentCode;
		}

		// then check body stack
		// similarly looking for most specific first
		int size = bodyStack.size();
		if (size > 0) {
			for (int i = size - 1; i >= 0; i--) { // Eeeek impl dependent
				ICode code = bodyStack.get(i);
				if (code instanceof Function) {
					return (Function) code;
				}
				if (code instanceof Constructor) {
					return (Constructor) code;
				}
				if (code instanceof Component) {
					return (Component) code;
				}
				if (code instanceof CompilationUnit) {
					return (CompilationUnit) code;
				}
			}
		}
		// should never happen
		return null;
	}

	@Override
	public Function findEnclosingFunctionContainer() {
		// check currentCode first
		// looking for most specific first
		if (currentCode instanceof Function) {
			return (Function) currentCode;
		}
		// then check body stack
		// similarly looking for most specific first
		int size = bodyStack.size();
		if (size > 0) {
			for (int i = size - 1; i >= 0; i--) { // Eeeek impl dependent
				ICode code = bodyStack.get(i);
				if (code instanceof Function) {
					return (Function) code;
				}
			}
		}
		// should never happen
		return null;
	}

	@Override
	public ISequence sequence() {
		ISequence newcode = new Sequence(findEnclosingDelcarationContainer());
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public ISwitch switchcode() {
		Switch newcode = new Switch();
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public IStructConstructor structConstructor(StructType st) {
		IStructConstructor newcode = new StructConstructor(st);
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public void enum_use(STEntry ste, int fromContext) {
		if (ste != null && ste.getType() instanceof EnumType)
			EnumDeclaration.generate((EnumType) (ste.getType()));
	}

	@Override
	public IAnyConstructor anyConstructor() {
		ICode call_stack = findEnclosingDelcarationContainer();
		AnyConstructor newcode = new AnyConstructor(call_stack);
		// TODO JL terrible hack to prevent serialiser generation for anys merely being printed
		if (ProcCall.isIn_print_proc_call()) {
			newcode.setNeed_serialization(false);
		}
		newCodeBody(newcode);
		return newcode;
	}

	// public IAnyProject anyProject( STEntry ste , int fromContext ) {
	@Override
	public IAnyProject anyProject(String subject_name, int fromContext) {
		// AnyProject newcode = new AnyProject( ste,findEnclosingDelcarationContainer() , fromContext );
		AnyProject newcode = new AnyProject(subject_name, findEnclosingDelcarationContainer(), fromContext);
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public IExceptionBlock tryCatchBlock(int fromContext) {
		ExceptionBlock newcode = new ExceptionBlock(fromContext);
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public IReturn return_clause() {
		Return newcode = new Return(currentProcedure);
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public IThrow throw_clause(String exception) {
		Throw newcode = new Throw(currentProcedure, exception);
		newCodeBody(newcode);
		return newcode;
	}

	@Override
	public void throw_clause_end() {
		IExceptionBlock exception_code = findNearestEnclosingTryExceptBlock();
		if (exception_code != null) {
			((Throw) currentCode).setInTryBlock(true);
		}
	}

	@Override
	public ICompilerErrors getCompilerErrors() {
		return compilerErrors;
	}

}
