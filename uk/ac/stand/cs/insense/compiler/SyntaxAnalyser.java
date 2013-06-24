package uk.ac.stand.cs.insense.compiler;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

import uk.ac.stand.cs.insense.compiler.cgen.IAcknowledge;
import uk.ac.stand.cs.insense.compiler.cgen.IAnyConstructor;
import uk.ac.stand.cs.insense.compiler.cgen.IAnyProject;
import uk.ac.stand.cs.insense.compiler.cgen.IArrayConstructor;
import uk.ac.stand.cs.insense.compiler.cgen.IAssign;
import uk.ac.stand.cs.insense.compiler.cgen.ICode;
import uk.ac.stand.cs.insense.compiler.cgen.ICodeGenerator;
import uk.ac.stand.cs.insense.compiler.cgen.ICompilationUnit;
import uk.ac.stand.cs.insense.compiler.cgen.IComponent;
import uk.ac.stand.cs.insense.compiler.cgen.IConstructorCall;
import uk.ac.stand.cs.insense.compiler.cgen.IConditional;
import uk.ac.stand.cs.insense.compiler.cgen.IConnect;
import uk.ac.stand.cs.insense.compiler.cgen.IDecl;
import uk.ac.stand.cs.insense.compiler.cgen.IDeclarationContainer;
import uk.ac.stand.cs.insense.compiler.cgen.IDereference;
import uk.ac.stand.cs.insense.compiler.cgen.IDisconnect;
import uk.ac.stand.cs.insense.compiler.cgen.IExceptionBlock;
import uk.ac.stand.cs.insense.compiler.cgen.IForLoop;
import uk.ac.stand.cs.insense.compiler.cgen.IFunction;
import uk.ac.stand.cs.insense.compiler.cgen.ILocation;
import uk.ac.stand.cs.insense.compiler.cgen.IProcCall;
import uk.ac.stand.cs.insense.compiler.cgen.IPublish;
import uk.ac.stand.cs.insense.compiler.cgen.IReceive;
import uk.ac.stand.cs.insense.compiler.cgen.IReturn;
import uk.ac.stand.cs.insense.compiler.cgen.ISelect;
import uk.ac.stand.cs.insense.compiler.cgen.ISend;
import uk.ac.stand.cs.insense.compiler.cgen.IStop;
import uk.ac.stand.cs.insense.compiler.cgen.IStructConstructor;
import uk.ac.stand.cs.insense.compiler.cgen.ISwitch;
import uk.ac.stand.cs.insense.compiler.cgen.IThrow;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.Cgen;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.Code;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.Decl;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.DeclarationContainer;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.ExceptionBlock;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.Location;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.StructConstructor;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.StructValue;
import uk.ac.stand.cs.insense.compiler.interfaces.ICompilerErrors;
import uk.ac.stand.cs.insense.compiler.interfaces.ILexicalAnalyser;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbol;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.interfaces.ISyntaxAnalyser;
import uk.ac.stand.cs.insense.compiler.interfaces.ITypeChecker;
import uk.ac.stand.cs.insense.compiler.symbols.AnyProjectSTEntry;
import uk.ac.stand.cs.insense.compiler.symbols.STEntry;
import uk.ac.stand.cs.insense.compiler.symbols.Symbols;
import uk.ac.stand.cs.insense.compiler.types.AnyType;
import uk.ac.stand.cs.insense.compiler.types.ArrayType;
import uk.ac.stand.cs.insense.compiler.types.BooleanType;
import uk.ac.stand.cs.insense.compiler.types.ByteType;
import uk.ac.stand.cs.insense.compiler.types.CType;
import uk.ac.stand.cs.insense.compiler.types.ChannelType;
import uk.ac.stand.cs.insense.compiler.types.ComponentType;
import uk.ac.stand.cs.insense.compiler.types.EnumType;
import uk.ac.stand.cs.insense.compiler.types.FunctionType;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;
import uk.ac.stand.cs.insense.compiler.types.ITypeWithList;
import uk.ac.stand.cs.insense.compiler.types.IntegerType;
import uk.ac.stand.cs.insense.compiler.types.InterfaceType;
import uk.ac.stand.cs.insense.compiler.types.RealType;
import uk.ac.stand.cs.insense.compiler.types.ScalarType;
import uk.ac.stand.cs.insense.compiler.types.StringType;
import uk.ac.stand.cs.insense.compiler.types.StructType;
import uk.ac.stand.cs.insense.compiler.types.TypeName;
import uk.ac.stand.cs.insense.compiler.types.UnknownType;
import uk.ac.stand.cs.insense.compiler.types.UnsignedIntegerType;
import uk.ac.stand.cs.insense.compiler.types.VoidType;
import uk.ac.standrews.cs.nds.util.Diagnostic;
import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:al@dcs.st-and.ac.uk"> Alan Dearle </a>
 */
public class SyntaxAnalyser implements ISyntaxAnalyser {

	public static final boolean ABSTRACT = true;

	protected ILexicalAnalyser lex;
	private ITypeChecker types;
	protected ISymbolTable currentScope; 
	protected ICompilerErrors compilerErrors;
	private ICodeGenerator cgen;
	private ICompilationUnit cu;
	private boolean value_decls_allowed = true;	// are we allowed to make value decls?
	private boolean requires_reordering = false; // do we need to reorder rhs of assigns

	public SyntaxAnalyser(ILexicalAnalyser lex, ITypeChecker tc, ICompilerErrors ce, ICodeGenerator cgen, ISymbolTable scope ) {
		this.lex = lex;
		this.currentScope = scope; //SymbolTable.newScope( ISymbolTable.GLOBAL );

		this.types = tc;
		this.compilerErrors = ce;
		this.cgen = cgen;
	}

	public void parse() {
		prog();
	}

	/***************************** Programs *****************************/

	private void prog() {   // DB18 syntax - checked alan
//		<prog> ::= {<global_decl> <semi_colon>} <sequence>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		lex.nextSymbol();   // pre load the lexical analyser 
		cu = cgen.compilationUnit();
		global_decls();
		sequence();
		cgen.complete();
	}

	private void global_decls() {   // DB18 syntax - checked alan
//		<prog> ::= {<global_decl> <semi_colon>} <sequence>
		while( lex.current().equals( Symbols.TYPE_SYM ) || lex.current().equals( Symbols.COMPONENT_SYM ) || lex.current().equals( Symbols.PROC_SYM ) ) {
			global_decl();
			separator();
		}
	}

	private void global_decl()   {  // DB18 syntax - checked alan
//		<global_decl>   ::= <component_decl> | <function_decl> | <type_decl>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		if( lex.have( Symbols.COMPONENT_SYM ) ) {
			component_decl();
		} else if( lex.have( Symbols.PROC_SYM ) ) {
			proc_decl();
		} else if( lex.have( Symbols.TYPE_SYM ) ) {
			type_decl();
		} else {
			compilerErrors.syntaxError( lex.current().toString(), "component, function or type" );
		}
	}

	/***************************** Type Declarations *****************************/

	protected void type_decl() {      // V18 syntax - checked al (and alan)
//		<type_decl> ::= type <identifier> is <type>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		ISymbol s = lex.current();
		lex.mustBe( Symbols.IDENTIFIER );
		String the_name = s.toString();
		lex.mustBe( Symbols.IS_SYM );
		ITypeRep tr = type();
		if( ! currentScope.declare( the_name, tr, true ) ) {
			compilerErrors.nameDeclaredError(the_name);     
		}
	}


	private void component_decl() { // V27 syntax - JL 
//		<component_decl>   ::= component <identifier> presents  <identifier_list> <component_body>
//		<component_body> ::= <lcb> <component_local_decls> <constructors> <behaviour> <rcb>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		ISymbol s = lex.current();
		lex.mustBe( Symbols.IDENTIFIER );
		String the_name = s.toString();
		ComponentType ct = new ComponentType( the_name );
		IComponent this_comp = cgen.componentBody( the_name );
		//cu.addInclude( this_comp.getHeaderFileName() );
		// JL put includes into compilation unit's main.h
		cu.addExternalInclude(Code.IFNDEF_ + Code.header_name( the_name ) + Code.NEWLINE + Code.SPACE + Code.HASH_INCLUDE_ + Code.DQUOTE + this_comp.getHeaderFileName() + Code.DQUOTE + Code.NEWLINE + Code.ENDIF_);
		currentScope = currentScope.enterScope( currentScope,ISymbolTable.COMPONENT );
		lex.mustBe( Symbols.PRESENTS_SYM );          
		ct.addInterface( interface_list( true) ); 
		// body
		lex.mustBe( Symbols.LCB_SYM );
		if (component_local_decls())		// Returns true if constructor found
		{
			constructors( ct );
			behaviour();
			lex.mustBe( Symbols.RCB_SYM );  
			currentScope = currentScope.getParentScope();
			if( ! currentScope.declare( the_name, ct, true ) ) {
				compilerErrors.nameDeclaredError(the_name);     
			}
			cgen.complete();
		}
	}


	private void named_channel_list( InterfaceType it, boolean declare_channels )  {  // V18 syntax checked al (and alan)
//		<named_channel_list>    ::= <named_channels> [<semi_colon><named_channel_list>]
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		do {
			named_channels( it, declare_channels );
		}
		while( lex.have( Symbols.SEMI_SYM ) );    
	}

	private InterfaceType interface_list( boolean make_declarations ) {    // DB8 syntax - checked al
//		<interface_list> ::=  <interface> [ <comma> <interface_list> ]
//		<interface> ::= <lrb><named_channel_list><rrb> | <identifier>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		InterfaceType new_interface = new InterfaceType();
		do {
			ISymbol s = lex.current();
			lex.mustBe( Symbols.IDENTIFIER );
			String the_name = s.toString();
			STEntry ste = currentScope.lookup( the_name );
			if( ste == null ) {
				compilerErrors.nameUndeclaredError(the_name); 
			}
			else if( ! ste.isType() ) {   // check this is a type decl
				compilerErrors.valueForType(the_name);
			} else {
				if( make_declarations ) {
					// check it is a interface then decl all the names
					if( ste.getType() instanceof InterfaceType ) {
						InterfaceType it = (InterfaceType) ( ste.getType() );
						for( TypeName t : it ) {
							if( ! currentScope.declare(t.name, t.type, false) ) {
								compilerErrors.nameDeclaredError(t.name); 
							}
							new_interface.addChannel( t.name, t.type );
							cgen.channel( ste );
						}
					} else {
						compilerErrors.typeError(ste.getType(), "interface" );
					}
				}
			}
		}
		while( lex.have( Symbols.COMMA_SYM ) );
		return new_interface;
	}

	private void proc_decl() {  // V27 syntax - JL
//		<proc_decl> ::=  proc < identifier> <lrb> [<named_param_list>] <rrb> [<colon> <type> ] <block>
//		<block>	::= <lcb> <sequence> <rcb>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		ISymbol s = lex.current();
		lex.mustBe( Symbols.IDENTIFIER );
		String the_name = s.toString();
		FunctionType ft = new FunctionType();
		currentScope = currentScope.enterScope( currentScope,ISymbolTable.FUNCTION );
		lex.mustBe( Symbols.LB_SYM );
		List<String> names = named_parameter_list( ft,true );
		IFunction proc_body = (IFunction) cgen.procedureBody( the_name, ft, names, currentScope.getScopeLevel() );
		lex.mustBe( Symbols.RB_SYM );
		ITypeRep returnType = VoidType.TYPE;
		if( lex.have( Symbols.COLON_SYM ) ) {
			returnType = type();
		}
		ft.addReturn( returnType );


		// Old code had 
		//value_decls_allowed = false;
		//clause();
		//value_decls_allowed = true;
		// end old code

		// new code
		lex.mustBe( Symbols.LCB_SYM );
		value_decls_allowed = true;
		sequence();
		//value_decls_allowed = false;
		lex.mustBe( Symbols.RCB_SYM );
		// end new code

		currentScope = currentScope.getParentScope();
		if( ! currentScope.declare( the_name, ft, false ) ) {
			compilerErrors.nameDeclaredError(the_name);     
		}
		// TODO JL Space Tracking
		proc_body.setSTEntry(currentScope.lookup(the_name));
		cgen.complete();
	}

	protected ITypeRep type(){
//		<type>   ::= <type1> {<lsb> <rsb>}
		ITypeRep t = type1();
		while( lex.have( Symbols.LSB_SYM ) ) {
			lex.mustBe( Symbols.RSB_SYM );
			t = new ArrayType( t );
		}
		return t;     
	}

	private ITypeRep type1() {  // DB8 syntax - checked al
//		<type>  ::= integer | real | bool | string | byte | any |
//		<interface_type> | <channel_type> | <tuple_type> | 
//		<enum_type> | <function_type> | <identifier>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		ITypeRep tdesc = UnknownType.TYPE;
		if ( lex.have( Symbols.INTEGER_SYM ) ) {
			tdesc = IntegerType.TYPE;
		}
		else if ( lex.have( Symbols.UNSIGNED_INTEGER_SYM ) ) {
			tdesc =  UnsignedIntegerType.TYPE;
		}
		else if ( lex.have( Symbols.REAL_SYM ) ) {
			tdesc =  RealType.TYPE;
		}
		else if ( lex.have( Symbols.BOOL_SYM ) ) {
			tdesc =  BooleanType.TYPE;
		}
		else if ( lex.have( Symbols.BYTE_SYM ) ) {
			tdesc =  ByteType.TYPE;
		}
		else if ( lex.have( Symbols.ANY_SYM ) ) {
			tdesc =  AnyType.TYPE;
		}
		else if ( lex.have( Symbols.STRING_SYM ) ) {
			tdesc =  StringType.TYPE;
		}               
		else if( lex.have( Symbols.INTERFACE_SYM ) ) {  
			tdesc =  interface_type();
		}
		else if( lex.have( Symbols.IN_SYM ) ) { 
			tdesc =  channel_type( ChannelType.IN );
		}      
		else if( lex.have( Symbols.OUT_SYM ) ) { 
			tdesc =  channel_type( ChannelType.OUT );
		} 
		else if( lex.have( Symbols.STRUCT_SYM ) ) {  
			tdesc = struct_type();
		}      
		else if( lex.have( Symbols.ENUM_SYM ) ) {
			tdesc =  enum_type();
		}    
		else if( lex.have( Symbols.PROC_SYM ) ) {  
			tdesc =  proc_type();
		}     
		else {
			ISymbol s = lex.current();
			lex.mustBe( Symbols.IDENTIFIER );
			String the_name = s.toString();
			STEntry ste = currentScope.lookup( the_name );
			if( ste == null ) {
				compilerErrors.nameUndeclaredError(the_name);
			} else if( ! ste.isType() ) { // check this is a type
				compilerErrors.typeForValue(the_name);
			}else {
				tdesc =  ste.getType();
			}
		}
		return tdesc;
	}

	private ITypeRep interface_type()  {    // V18 syntax checked al
//		<interface_type>    ::= interface <lrb><named_channel_list><rrb>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		InterfaceType it = new InterfaceType();
		lex.mustBe( Symbols.LB_SYM );
		named_channel_list( it, false );
		lex.mustBe( Symbols.RB_SYM );
		return it;    
	}

	private void named_channels( InterfaceType it, boolean declare_channels )  {  // DB10 syntax checked al - refactored
//		<named_channel> ::= <channel_type> <identifier> [ <comma> <identifier> ]
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		if(!lex.current().equals(Symbols.RB_SYM)){
			int direction;
			if( lex.have( Symbols.IN_SYM ) ) { 
				direction = ChannelType.IN;
			} else {
				lex.mustBe(Symbols.OUT_SYM);
				direction = ChannelType.OUT;
			} 
			ChannelType ct = channel_type( direction );
			do {
				ISymbol s = lex.current();
				lex.mustBe( Symbols.IDENTIFIER );
				String the_name = s.toString();
				if( declare_channels ) {
					if( ! currentScope.declare( the_name, ct, false ) ) {
						compilerErrors.nameDeclaredError(the_name);     
					}
				}
				it.addChannel( the_name, ct );
			}
			while( lex.have( Symbols.COMMA_SYM ) );
		}
	}

	private ChannelType channel_type( int direction ) {   // V18 syntax checked al
//		<channel_type>  ::= <direction> connection <lrb> <type> <rrb>
//		<direction> ::= in | out
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		ITypeRep tr = type();
		return new ChannelType( tr,direction );
	}

	private ITypeRep struct_type()  { 
//		<struct_type>    ::= struct <lrb> <named_parameter_list> <rrb>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		lex.mustBe( Symbols.LB_SYM );
		StructType tt = new StructType();
		named_parameter_list( tt,false );
		for( ITypeRep t : tt.getFields() ) {
			if( t instanceof StructType ) {
				compilerErrors.generalError( "Structs within struct encountered" );
				break;
			}
		}
		lex.mustBe( Symbols.RB_SYM );   
		return tt;   
	}

	private ITypeRep enum_type() {  // V18 syntax checked al
//		<enum_type> ::= enum <lrb> <enum_list> <rrb>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		lex.mustBe( Symbols.LB_SYM );
		EnumType et = new EnumType(); 
		enum_list(et);
		lex.mustBe( Symbols.RB_SYM );  
		return et;
	}

	private void enum_list( EnumType et ) { // V18 syntax checked al
//		<enum_list> ::= <identifier> [<comma> <enum_list>]
		do {
			ISymbol s = lex.current();
			lex.mustBe( Symbols.IDENTIFIER );
			String the_name = s.toString();
			et.addLabel(the_name);
			if( ! currentScope.declare(the_name, et, false) ) {
				compilerErrors.nameDeclaredError(the_name);  
			}
		} while( lex.have( Symbols.COMMA_SYM ) );
		Diagnostic.trace( DiagnosticLevel.RUN,"" );        
	}

	private void type_list( FunctionType tr )  { // V18 syntax checked al
//		<type_list> ::= <type> [<comma> <type_list>]
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		do {
			tr.addParam( type() );
		}
		while( lex.have( Symbols.SEMI_SYM ) );        

	}

	private ITypeRep proc_type() {  // V26
//		<proc_type> ::= proc <type> <lrb> [<type_list>] <rrb> [<colon> <type>]
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		FunctionType ft = new FunctionType();
		lex.mustBe( Symbols.LB_SYM );
		if(!lex.current().equals(Symbols.RB_SYM)){
			type_list( ft );
		}
		lex.mustBe( Symbols.RB_SYM );  
		if( lex.have( Symbols.COLON_SYM ) ) {
			ft.addReturn( type() );
		} else {
			ft.addReturn( VoidType.TYPE );
		}
		return ft;    
	}   

	/***************************** Value Declarations *****************************/

	private void component_local_decl()   {     // V27 syntax - added by JL
//		<component_local_decl>	::=	<value_decl> | <proc_decl> | <type_decl>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		if ( lex.have( Symbols.TYPE_SYM ) ) {
			type_decl();
		} else if( lex.have( Symbols.PROC_SYM ) ) {
			proc_decl();
		} else {
			value_decl();
		}
	}



	private void decl()   {     // V27 syntax - JL - proc_decl rewmoved 
		// <decl>	::=	<value_decl> | <type_decl>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		if ( lex.have( Symbols.TYPE_SYM ) ) {
			type_decl();
		} else {
			value_decl();
		}
	}

	private void value_decl() { // V14 syntax
//		<value_decl> ::= <identifier> <equals> <value>   // V18 syntax
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		ISymbol s = lex.current();
		lex.mustBe( Symbols.IDENTIFIER );
		String the_name = s.toString();

		IDecl loc = cgen.newLocation( the_name, currentScope.getScopeLevel() );
		lex.mustBe( Symbols.EQUALS_SYM );
		ITypeRep tr = value( loc );
		if( ! currentScope.declare( the_name, tr, false ) ) {
			compilerErrors.nameDeclaredError(the_name);
		} else {
			loc.addSymbolTableEntry( currentScope.lookup( the_name ) );
		}
		if(tr instanceof ChannelType){
			cgen.channel(currentScope.lookup( the_name ));
		}
		cgen.complete();
	}



	/**
	 * @param the_name - if on a rhs of an expression the name to which the clause is being assigned.
	 * @return
	 */
	private ITypeRep value( IDecl loc )  {     // V18 syntax checked al
//		<value> ::= <clause> | <value_constructor>  
		if( lex.have( Symbols.NEW_SYM ) ) {
			return value_constructor(loc);
		} else {
			boolean remember_rr = requires_reordering;
			requires_reordering = true;
			ITypeRep t =  clause( loc );
			requires_reordering = remember_rr;
			return t;
		}
	}

	/***************************** Sequencing *****************************/

	public boolean sequence_more() {
		return ! ( lex.current().equals( Symbols.EOT_SYM ) ||
				lex.current().equals( Symbols.RCB_SYM ) );
	}

	// 	V27 Syntax - JL
	//  <sequence> ::= <statement> [ <semi_colon> <sequence>]
	//	<statement>	::= <decl> | <catch_clause>
	//	<decl>	::=	<value_decl> | <type_decl>
	private void sequence() {
		ITypeRep tr = VoidType.TYPE; // empty sequence
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		cgen.sequence();
		while ( sequence_more() ) {
			// <clause> | <decl>
			if( lex.current().equals( Symbols.TYPE_SYM ) ) {
				type();
			} 
			else {
				tr = catch_clause();
				types.match( VoidType.TYPE, tr );
			}
			Diagnostic.trace( DiagnosticLevel.RUN,"not eot loop" );
			if( sequence_more() ){
				separator();
			}
		}
		cgen.complete();
	}

	/**
	 * Checks that the next symbol is a new line or semicolon
	 */
	public void separator() {
		if ( ! lex.current().equals( Symbols.SEMI_SYM ) && ! lex.atNewLine() && ! lex.current().equals(  Symbols.EOT_SYM  )) {
			compilerErrors.syntaxError( lex.current().toString(), Symbols.SEMI_SYM.toString() );
		} else {
			while ( ! lex.have( Symbols.SEMI_SYM ) && ! lex.atNewLine() && ! lex.current().equals(  Symbols.EOT_SYM  ) ) {
				lex.nextSymbol();
				Diagnostic.trace( DiagnosticLevel.RUN,"while loop" );
			}
		}
	}


	/***************************** Clauses *****************************/

	private ITypeRep catch_clause() {
		if( lex.have( Symbols.TRY_SYM ) ) {
			return try_except();
		} else {
			return clause();
		}
	}

	private ITypeRep try_except() { // V30 Syntax
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		IExceptionBlock code = cgen.tryCatchBlock(currentScope.getContext());
		ITypeRep t1 = clause();
		// JL, must have one labelled except clause for a try clause
		lex.mustBe( Symbols.EXCEPT_SYM );
		do{
			String the_name = lex.current().toString();
			lex.mustBe(Symbols.IDENTIFIER); // TODO JL lex.mustBe(Symbols.IDENTIFIER) does not work as I thought, need to check for valid exception label in enum here 
			STEntry ste = currentScope.lookup( the_name );
			if( ste == null ) { // JL check label is declared
				compilerErrors.nameUndeclaredError( the_name );   
			} else  { // JL check label corresponds to exception enum type
				// TODO JL this lookup is a hack to find the type of the enum containing exceptions 
				STEntry exception_ste = currentScope.lookup( ExceptionBlock.getAN_EXCEPTION_LABEL() );
				types.match( exception_ste.getType(), ste.getType() ) ;
			}
			code.onExcept(the_name);
			ITypeRep t2 = clause();
			types.match( t1,t2 );
			// JL, permit multiple labelled except clauses per try clause
		} while( lex.have(Symbols.EXCEPT_SYM));
		cgen.complete();
		return t1;
	}

	// Test Syntax for V30 to test try-except 
	// ... and to permit bindLocalChannel to be written in Insense ...
	// permits a procedure to throw an exception OperationFailedException
	private ITypeRep throw_clause() { 
		// <throw_clause>    ::= throw <name>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		if(currentScope.getContext() != ISymbolTable.FUNCTION && !ExceptionBlock.inExceptionBlock()){
			compilerErrors.generalError("throw only allowed in procedures or in try block");
		}
		//lex.nextSymbol();
		String the_name = lex.current().toString();
		lex.mustBe(Symbols.IDENTIFIER);

		STEntry ste = currentScope.lookup( the_name );
		if( ste == null ) { // JL check label is declared
			compilerErrors.nameUndeclaredError( the_name );   
		} else  { // JL check label corresponds to exception enum type
			// TODO JL this lookup is a hack to find the type of the enum containing exceptions 
			STEntry exception_ste = currentScope.lookup( ExceptionBlock.getAN_EXCEPTION_LABEL() );
			types.match( exception_ste.getType(), ste.getType() ) ;
		} // TODO JL could alter to permit users to throw any exception
//		if(!the_name.equals(ExceptionBlock.getAN_EXCEPTION_LABEL())){
//		compilerErrors.generalError("throw can only throw " + ExceptionBlock.getAN_EXCEPTION_LABEL() + " at present.");
//		}

		IThrow c = cgen.throw_clause( the_name );
		cgen.throw_clause_end();
		cgen.complete();
		return VoidType.TYPE;
	}


	private ITypeRep clause() { // just to keep things tidy
		return clause( null );
	}

	/**
	 * @param lhs_loc - if on rhs of expression the location to which a value must be assigned
	 * @return
	 */
	private ITypeRep clause( ILocation lhs_loc ) {  // V29  syntax
//		<clause>    ::= <if_clause> | <for_clause> | 
//		<switch_clause> | < assign_clause> |
//		<connect_clause> | <select_clause> | <send_clause> | 
//		<receive_clause> | <acknowledge_clause> | <publish_clause> | stop | break | continue | 
//		return <expression> | <expression>    
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		if( lex.have( Symbols.IF_SYM ) ) {
			return if_clause( lhs_loc );
		} else if( lex.have( Symbols.FOR_SYM ) ) {
			return for_clause();
		} else if( lex.have( Symbols.CASE_SYM ) ) {
			return case_clause( lhs_loc );
		} else if( lex.have( Symbols.CONNECT_SYM ) ) {
			return connect_clause();
		} else if( lex.have( Symbols.DISCONNECT_SYM ) ) {
			return disconnect_clause();
		} else if( lex.have( Symbols.SELECT_SYM ) ) {
			return select_clause( lhs_loc );
		} else if( lex.have( Symbols.SEND_SYM ) ) {
			return send_clause();
		} else if( lex.have( Symbols.RECEIVE_SYM ) ) {
			return receive_clause( lhs_loc );
		} else if( lex.have( Symbols.ACKNOWLEDGE_SYM ) ) {
			return acknowledge_clause( lhs_loc );
		} else if( lex.have( Symbols.PUBLISH_SYM ) ) {
			return publish_clause( lhs_loc );
		} else if( lex.have( Symbols.PROJECT_SYM ) ) {
			return project_clause( lhs_loc );
		} else if( lex.have( Symbols.STOP_SYM ) ) {
			return stop_clause();
		} else if (lex.have( Symbols.RETURN_SYM )){
			return return_clause();
		} else if (lex.have( Symbols.THROW_SYM )){
			return throw_clause();
		} else if( lex.have( Symbols.ANY_SYM ) ) {
			return any_value();
		} else if ( lex.current().equals( Symbols.LCB_SYM ) ) {	// <clause_block>
			lex.nextSymbol();
			return clause_block();
			//return VoidType.TYPE;
		} else {
			ITypeRep e = expression(false);
			return e;
		}
	}

	private ITypeRep for_clause() {   // V26 syntax
		//   	<for_clause>		::=	for <identifier> <equals>  <expression> <dotdot> <expression>  [ <colon>[+|-]<integer_literal> ] do <clause>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		currentScope = currentScope.enterScope(currentScope);
		IForLoop code = cgen.forLoop(currentScope);
		String the_name = lex.current().toString();
		lex.mustBe( Symbols.IDENTIFIER );
		lex.mustBe( Symbols.EQUALS_SYM );
		ITypeRep tr1 = expression(false);
		types.match( IntegerType.TYPE, tr1 );
		lex.mustBe( Symbols.DOTDOT_SYM );
		code.finalValue();
		ITypeRep tr2 = expression(false);
		types.match( IntegerType.TYPE, tr2 );
		if( ! currentScope.declare( the_name, tr2, false ) ) {
			compilerErrors.nameDeclaredError(the_name);     
		}
		code.addDecl( currentScope.lookup( the_name ) );
		if( lex.have( Symbols.COLON_SYM ) ) {
			code.increment();	
			if( lex.current().equals( Symbols.MINUS_SYM ) ) {
				code.negativeIncrement();
				lex.nextSymbol();
			}
			if( lex.current().equals( Symbols.INTEGER_LITERAL ) ) {
				cgen.literal( lex.current().toString() );
				lex.nextSymbol();
			} else {
				compilerErrors.syntaxError( lex.current().toString(), "integer literal" );
			}

		}
		lex.mustBe( Symbols.DO_SYM );
		code.body();
		cgen.sequence();		// creates an environment to permit assign reordering
		types.match( VoidType.TYPE, clause() );
		cgen.complete(); // sequence
		cgen.complete(); // for
		currentScope = currentScope.getParentScope();
		return VoidType.TYPE;
	}

	private ITypeRep if_clause( ILocation lhs_loc ) {   // V26
		//   	<if_clause>	::=	if  <clause> then <clause> [ else <clause> ]
		boolean remember_rr = requires_reordering;
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		IConditional code = cgen.ifClause();
		types.match( BooleanType.TYPE, clause() );
		lex.mustBe( Symbols.THEN_SYM );
		currentScope = currentScope.enterScope(currentScope);
		code.thenBranch(currentScope);
		ITypeRep t = clause( lhs_loc );
		currentScope = currentScope.getParentScope();
		if( lhs_loc != null && ! t.equals( VoidType.TYPE ) && requires_reordering ) {
			lhs_loc.performReordering( code, t , currentScope.getContext());
		}
		if( lex.have( Symbols.ELSE_SYM ) ) {
			currentScope = currentScope.enterScope(currentScope);
			code.elseBranch(currentScope);
			ITypeRep t1 =  clause( lhs_loc );
			currentScope = currentScope.getParentScope();
			types.match( t,t1 );
			if( lhs_loc != null && ! t1.equals( VoidType.TYPE ) && remember_rr ) {
				lhs_loc.performReordering( code, t , currentScope.getContext());
				requires_reordering = false;
			}
		}
		cgen.complete();
		return t;
	}

	private ITypeRep case_clause( ILocation lhs_loc ) { // V26 syntax
//		<case_clause>	::=	case <expression> of
//		<case_list> default <colon> <clause> 
//		<case_list>		::=	<clause_list> <colon> <clause> [<separator> <case_list>]

		boolean remember_rr = requires_reordering;
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		ITypeRep returnType = null;
		ISwitch code = cgen.switchcode();
		ITypeRep matchType = expression(false);
		lex.mustBe( Symbols.OF_SYM );
		code.switchMatchType( matchType );
		while( ! lex.have( Symbols.DEFAULT_SYM ) ) {
			code.switchArm();
			ITypeRep lhsType = clause( lhs_loc );
			types.match( matchType, lhsType );
			lex.mustBe( Symbols.COLON_SYM );
			code.switchExp();
			ITypeRep rhsType = clause( lhs_loc );
			if( lhs_loc != null && ! rhsType.equals( VoidType.TYPE ) && remember_rr ) {
				lhs_loc.performReordering( code, rhsType , currentScope.getContext());
				requires_reordering = false;
			}
			if( returnType != null ) {
				types.match( returnType, rhsType );
			} else {
				returnType = rhsType;
			}
		}
		// Default case
		code.defaultArm();
		lex.mustBe( Symbols.COLON_SYM );
		ITypeRep rhsType = clause( lhs_loc );
		if( lhs_loc != null && ! rhsType.equals( VoidType.TYPE ) && remember_rr ) {
			lhs_loc.performReordering( code, rhsType , currentScope.getContext());
			requires_reordering = false;
		}
		if( returnType != null ) {
			types.match( returnType, rhsType );
		} else {
			returnType = rhsType;
		}              
		return returnType;
	}

	private ITypeRep connect_clause() { // V18 syntax checked al
//		<connect_clause>	::=	connect <expression> [on <expression> to
//		<expression> [ on <expression> ]
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		IConnect c = cgen.connect(currentScope.getContext());
		ITypeRep tr1 = expression(false);

		boolean have_inter_node_channel = false;
		if( lex.have(Symbols.ON_SYM)) {
			c.on();
			types.match( tr1, StringType.TYPE );
			ITypeRep tr2 = expression(false);
			types.match( tr2, UnsignedIntegerType.TYPE );
			have_inter_node_channel = true;
		} else if(tr1 instanceof ChannelType){
			c.setChannelType((ChannelType)tr1);        	
		}


		lex.mustBe( Symbols.TO_SYM );
		c.to();
		ITypeRep tr3 = expression(false);
		if( lex.have(Symbols.ON_SYM)) {
			c.on();
			types.match( tr3, StringType.TYPE );
			ITypeRep tr4 = expression(false);
			types.match( tr4, UnsignedIntegerType.TYPE );
			have_inter_node_channel = true;
		} else if(tr3 instanceof ChannelType){
			c.setChannelType((ChannelType)tr3);
		}
		if( ! have_inter_node_channel ) {
			check_connect_compatible( tr1, tr3 );
		}
		cgen.setRuntimeComponentFlags(c.toString());
		cgen.connect_call_end();
		cgen.complete();
		return VoidType.TYPE;
	}

	private void check_connect_compatible( ITypeRep tr1, ITypeRep tr2) {
		if( ! ( tr1 instanceof ChannelType ) ) {
			compilerErrors.typeError( tr1, "channel" );
		}
		if( ! ( tr2 instanceof ChannelType ) ) {
			compilerErrors.typeError( tr2, "channel" );
			return;
		}
		// now they are both channels need to check they are different directions
		ChannelType chan1 = (ChannelType) tr1;
		ChannelType chan2 = (ChannelType) tr2;
		if( ! chan1.compatible( chan2 ) ) {
			compilerErrors.typeError( tr1, tr2 );
			return;
		}
	}

	private ITypeRep disconnect_clause() { 
//		<disconnect_clause>	::=	disconnect <expression> [on <expression>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		IDisconnect c = cgen.disconnect(currentScope.getContext());
		ITypeRep tr1 = expression(false);

		boolean have_inter_node_channel = false;
		if( lex.have(Symbols.ON_SYM)) {
			c.on();
			types.match( tr1, StringType.TYPE );
			ITypeRep tr2 = expression(false);
			types.match( tr2, UnsignedIntegerType.TYPE );
			have_inter_node_channel = true;
		} else if(! (tr1 instanceof ChannelType)){
			compilerErrors.expressionError("Channel Type expected");
		}

		cgen.setRuntimeComponentFlags(c.toString());
		cgen.disconnect_call_end();
		cgen.complete();
		return VoidType.TYPE;
	}


	private ITypeRep select_clause( ILocation lhs_loc ) {   // V16 syntax checked al
//		<select_clause>	::=	select <lcb> <select_list> [default <colon> <clause>] <rcb>
//		<select_list>	::=	<select_receive_clause> [ <select_list> ] | 
//		<select_acknowledge_clause> [ <select_list> ]
//		<select_receive_clause> ::= 
//		receive <identifier> from <name> [when <clause> ]<colon> <clause> 
//		<select_acknowledge_clause> ::=
//		acknowledge <identifier> from <name> [when <clause> ] after <colon> <clause> 
		boolean remember_rr = requires_reordering;
		//ArrayList<IDecl> select_decls = new ArrayList<IDecl>();
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		lex.mustBe( Symbols.LCB_SYM );
		ISelect code = cgen.select(currentScope.getContext());
		ITypeRep resultType = UnknownType.TYPE;
		boolean acknowledge = false;
		while( lex.have( Symbols.RECEIVE_SYM ) || (acknowledge = lex.have(Symbols.ACKNOWLEDGE_SYM)) ) {
			currentScope = currentScope.enterScope(currentScope);
			code.receiveArm(acknowledge);
			ISymbol s = lex.current();
			lex.mustBe( Symbols.IDENTIFIER );
			String decl_name = s.toString();
			//System.err.println("SyntaxAnalyser.select_clause: decl_name = " + decl_name);
			lex.mustBe( Symbols.FROM_SYM );  
			ITypeRep tr = expression(false);
			ITypeRep baseType; // of channel
			// must be a channel must be an in channel
			if( tr instanceof ChannelType && ((ChannelType)(tr)).getDirection() == ChannelType.IN ) {
				baseType = ((ChannelType)(tr)).getChannel_type();
			} else {
				compilerErrors.typeError(tr, "in channel type");
				baseType = UnknownType.TYPE;
			}
			IDecl loc = null;
			if( ! currentScope.declare( decl_name, baseType, false ) ) {
				compilerErrors.nameDeclaredError(decl_name); 
			} else {
				loc = new Decl( decl_name , currentScope.getScopeLevel());
				loc.addSymbolTableEntry( currentScope.lookup( decl_name ) );
				//Cgen.get_instance().findEnclosingDelcarationContainer().addLocation(loc);
			}
			code.from( loc );
			if( lex.have( Symbols.WHEN_SYM ) ) {
				code.when(); // sets delimiter for when clause on code fragments
				types.match( BooleanType.TYPE, clause() ); // gets when clause
			}
			if(acknowledge)
				lex.mustBe(Symbols.AFTER_SYM);
			lex.mustBe( Symbols.COLON_SYM );
			code.selectExp(acknowledge);
			ITypeRep tr2 = clause( lhs_loc );
			if( lhs_loc != null && ! tr2.equals( VoidType.TYPE ) && remember_rr ) {
				lhs_loc.performReordering( code, tr2 , currentScope.getContext());
				requires_reordering = false;
			}
			if( ! resultType.equals( UnknownType.TYPE ) ) {
				types.match( resultType, tr2 );
			} else {
				resultType = tr2;
			}
			acknowledge = false;
			currentScope = currentScope.getParentScope();
		}
		if( lex.have( Symbols.DEFAULT_SYM ) ) {
			lex.mustBe( Symbols.COLON_SYM );
			currentScope = currentScope.enterScope(currentScope);
			code.defaultArm();
			ITypeRep tr2 = clause( lhs_loc );
			if( lhs_loc != null && ! tr2.equals( VoidType.TYPE ) && remember_rr ) {
				lhs_loc.performReordering( code, tr2 , currentScope.getContext());
				requires_reordering = false;
			}
			if( ! resultType.equals( UnknownType.TYPE ) ) {
				types.match( resultType, tr2 );
			} else {
				resultType = tr2;
			}
			currentScope = currentScope.getParentScope();
		}
		lex.mustBe( Symbols.RCB_SYM );
		cgen.complete();
		return resultType;
	}


	private ITypeRep send_clause() { // V31 syntax
//		<send_clause>   ::= send <expression> on <name> 
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		ISend code = cgen.send(currentScope.getContext());

		ITypeRep tr1 = clause(); // was expression(true);

		lex.mustBe( Symbols.ON_SYM );

		String channel_name = lex.current().toString();
		lex.mustBe( Symbols.IDENTIFIER );
		STEntry ste = currentScope.lookup( channel_name );
		if( ste == null ) {
			compilerErrors.nameUndeclaredError( channel_name ); 
			return UnknownType.TYPE;
		}
		ITypeRep tr2 = ste.getType();
		code.addChannel( ste );
		if( tr2 instanceof ChannelType && ((ChannelType)(tr2)).getDirection() == ChannelType.OUT ) {
			ITypeRep baseType = ((ChannelType)(tr2)).getChannel_type();
			types.match(tr1, baseType );
		} else {
			compilerErrors.typeError(tr2, "out channel type");
		}
		cgen.send_call_end(); // for try catch blocks, must tell code generation that we are in try block
		cgen.complete();
		return VoidType.TYPE;
	}

	private ITypeRep receive_clause( ILocation lhs_loc ) {  // V25 syntax checked al
//		<receive_clause>    ::= receive <identifier> from <name>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );   
		ISymbol s = lex.current();
		lex.mustBe( Symbols.IDENTIFIER );
		String decl_name = s.toString();
		lex.mustBe( Symbols.FROM_SYM );       
		String channel_name = lex.current().toString();
		lex.mustBe( Symbols.IDENTIFIER );
		STEntry ste = currentScope.lookup( channel_name );
		if( ste == null ) {
			compilerErrors.nameUndeclaredError( channel_name ); 
			return UnknownType.TYPE;
		} else { 
			ITypeRep tr = ste.getType();
			// must be a channel and must be an in channel
			if( tr instanceof ChannelType && ((ChannelType)(tr)).getDirection() == ChannelType.IN ) {
				ITypeRep baseType = ((ChannelType)(tr)).getChannel_type();
				if( ! currentScope.declare( decl_name, baseType, false ) ) {
					compilerErrors.nameDeclaredError(decl_name);  
				} else {
					IReceive code = cgen.receive( currentScope.lookup( decl_name ), ste, currentScope.getContext());
				}
			} else {
				compilerErrors.typeError(tr, "in channel type");
			}
		}
		cgen.complete();
		return VoidType.TYPE;
	}  


	private ITypeRep acknowledge_clause( ILocation lhs_loc ) {  // V29 syntax
//		<acknowledge_clause> ::= acknowledge <identifier> from <name> after <clause>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );   
		ISymbol s = lex.current();
		lex.mustBe( Symbols.IDENTIFIER );
		String decl_name = s.toString();
		lex.mustBe( Symbols.FROM_SYM );       
		String channel_name = lex.current().toString();
		lex.mustBe( Symbols.IDENTIFIER );
		STEntry ste = currentScope.lookup( channel_name );
		if( ste == null ) {
			compilerErrors.nameUndeclaredError( channel_name ); 
			return UnknownType.TYPE;
		} else { 
			ITypeRep tr = ste.getType();
			// must be a channel and must be an in channel
			if( tr instanceof ChannelType && ((ChannelType)(tr)).getDirection() == ChannelType.IN ) {
				ITypeRep baseType = ((ChannelType)(tr)).getChannel_type();
				if( ! currentScope.declare( decl_name, baseType, false ) ) {
					compilerErrors.nameDeclaredError(decl_name);  
				} else {
					lex.mustBe(Symbols.AFTER_SYM);
					currentScope = currentScope.enterScope( currentScope );
					IAcknowledge code = cgen.acknowledge( currentScope.lookup( decl_name ), ste, currentScope.getContext());
					ITypeRep tr2 = clause( lhs_loc );
					types.match( tr2, VoidType.TYPE );
					currentScope = currentScope.getParentScope();
				}
			} else {
				compilerErrors.typeError(tr, "in channel type");
			}
		}
		cgen.complete();
		return VoidType.TYPE;
	}  



	private ITypeRep publish_clause( ILocation lhs_loc ) {  // V31 syntax
		//  publish <expression> as <expression>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		IPublish p = cgen.publish(currentScope);
		ITypeRep tr1 = expression(false);
		if( ! ( tr1 instanceof ChannelType ) ) {
			compilerErrors.typeError( tr1, "channel" );
		}
		p.as((ChannelType)tr1);
		lex.mustBe( Symbols.AS_SYM );
		ITypeRep tr2 = expression(false);
		if( ! ( tr2 instanceof StringType ) ) {
			compilerErrors.typeError( tr2, "string" );
		}
		cgen.publish_call_end(); // for try catch blocks, must do jump to handler on exception
		cgen.complete();
		return VoidType.TYPE;
	}  



	private ITypeRep project_clause( ILocation lhs_loc ) { // V26 syntax NOT CHANGED YET

//		<project_clause>	::= project <clause> as <identifier> onto <project_list> default <colon> <clause>
//		<project_list> 	::= <type> <colon> <clause> [ <project_list> ]

		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		//lex.mustBe( Symbols.IDENTIFIER );
		//String the_name1 = lex.current().toString();
		//STEntry ste = currentScope.lookup( the_name1 );
		ITypeRep type1 = clause(lhs_loc);
		String subject_name = cgen.popLastAppend();
		types.match(AnyType.TYPE, type1);
		//String subject_name = "**THIS->PAYLOAD**";
		//IAnyProject code = cgen.anyProject( ste , currentScope.getContext());
		IAnyProject code = cgen.anyProject( subject_name , currentScope.getContext());
		lex.mustBe( Symbols.AS_SYM ); 
		String the_name2 = lex.current().toString();
		lex.mustBe( Symbols.IDENTIFIER );
		lex.mustBe( Symbols.ONTO_SYM );

		ITypeRep armType = null;

		while( ! lex.have( Symbols.DEFAULT_SYM ) ) {
			// <type> <colon> <clause> 
			ITypeRep tr = type();
			// force generation of struct files on projection from any
			if(tr instanceof StructType){
				StructType stype = (StructType) tr;
				StructValue dummy = new StructValue(stype);
				dummy.complete();
			}

			currentScope = currentScope.enterScope( currentScope );
			//AnyProjectSTEntry entry = currentScope.declareAnyProject( the_name2, tr, false );
			if (!currentScope.declare( the_name2, tr, false )){
				compilerErrors.generalError("project as declararion failed");
			}
			STEntry entry = currentScope.lookup(the_name2);
			code.choiceArm( entry, currentScope );
			lex.mustBe( Symbols.COLON_SYM );
			ITypeRep tr2 = clause( lhs_loc );
			if( armType != null ) {
				types.match( armType, tr2 );	
			} 
			armType = tr2;
			currentScope = currentScope.getParentScope();
		}
		// have( Symbols.DEFAULT_SYM )
		// default <colon> <clause> 
		lex.mustBe( Symbols.COLON_SYM );
		currentScope = currentScope.enterScope( currentScope );
		code.defaultArm(currentScope);
		ITypeRep tr2 = clause( lhs_loc );
		if( armType == null ) {
			// no arms in the project - illegal
			compilerErrors.syntaxError( "default", "project arm" );
		} else {
			types.match( armType, tr2 );
		}
		currentScope = currentScope.getParentScope();

		cgen.complete();
		return armType;
	}

	private ITypeRep stop_clause() { // V28 Syntax
		// stop [ <name> ]  
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		IStop stop_code = cgen.stop(currentScope.getContext());
		// This is really messy but they made me do it :)
		if( ! ( lex.have( Symbols.SEMI_SYM ) ||  lex.current().equals( Symbols.ELSE_SYM ) ||
				lex.current().equals( Symbols.RB_SYM ) || lex.current().equals( Symbols.RCB_SYM ) ||
				lex.atNewLine() ) )  {
			String component_name = lex.current().toString();
			lex.nextSymbol();
			STEntry ste = currentScope.lookup( component_name );
			if( ste == null ) {
				compilerErrors.nameUndeclaredError( component_name ); 
				return UnknownType.TYPE;
			}
			ITypeRep tr = ste.getType();
			types.match(ComponentType.TYPE, tr);
			stop_code.addTarget(ste);
		}
		cgen.complete();
		return VoidType.TYPE;        
	}


	private ITypeRep return_clause() { // V28 syntax
//		<return_clause>    ::= return <expression>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		if(currentScope.getContext() != ISymbolTable.FUNCTION)
			compilerErrors.generalError("return only allowed in procedures");
		IReturn c = cgen.return_clause();
		if(c.proc_returns_result()){
			ITypeRep tr1 = expression(false);
			types.match(c.getProcReturnType(), tr1);
		}
		cgen.complete();
		return VoidType.TYPE;
	}



	/***************************** Expressions *****************************/

	public ITypeRep expression(boolean inSend) {// lhs_loc only non-null if reoredering required
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		requires_reordering = true;
		return expr( -1 , inSend);
	}

	class _Expr {
		boolean more;
		ISymbol symbol;
		ITypeRep type;
	}

	public ITypeRep rel_type(ITypeRep t, ITypeRep t1 ) {
		if( !t.equals( UnknownType.TYPE ) && !t1.equals( UnknownType.TYPE ) && ( t.equals( IntegerType.TYPE ) || t.equals( UnsignedIntegerType.TYPE ) || t.equals( RealType.TYPE ) ) || t.equals( ByteType.TYPE ) ) {
			types.coerce(t, t1);
			return BooleanType.TYPE;
		} else if  ( t.equals( StringType.TYPE ) ) {
			types.match( t, t1 ); 
			return BooleanType.TYPE;
		} else {
			compilerErrors.typeError(t, t1);
			return UnknownType.TYPE;
		}
	}


	public boolean le2(int n, int p, _Expr _expr) {
		if (n <= p) {
			_expr.symbol = lex.current();
			lex.nextSymbol();
			return true;
		} else {
			_expr.more = false;
			return false;
		}
	}

	public void and_or_op(int n1, int n, _Expr _expr, ISymbol sym ) {
		if (le2(n, n1, _expr)) {
			types.match( BooleanType.TYPE, _expr.type );
			cgen.binaryOp( sym.toString() );
			types.match( BooleanType.TYPE, expr(n1 + 1  , false) );
			_expr.type = BooleanType.TYPE;
		}
	}

	public ITypeRep unary_op( int n , boolean inSend) { 
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		if ( lex.current().equals( Symbols.PLUS_SYM ) ) {
			le1(n, 4);
			ITypeRep t1 = int_real(expr(4 , false));
			return t1;
		} else if( lex.current().equals( Symbols.MINUS_SYM ) ) {
			lex.setUnaryMinus( true );		
			le1(n, 4);
			lex.setUnaryMinus( false );
			ITypeRep t1 = int_real(expr(4 , false));
			return t1;
		} else if( lex.current().equals( Symbols.STRING_LITERAL ) ) { 
			cgen.stringLiteral( lex.current().toString() );
			lex.nextSymbol();
			return StringType.TYPE;
		} else if( lex.current().equals( Symbols.INTEGER_LITERAL ) ) {
			cgen.literal( lex.current().toString() );
			lex.nextSymbol();
			return IntegerType.TYPE;
		} else if( lex.current().equals( Symbols.REAL_LITERAL ) ) {
			cgen.literal( lex.current().toString() );
			lex.nextSymbol();
			return RealType.TYPE;
		} else if( lex.current().equals( Symbols.BOOL_LITERAL ) ) {
			cgen.literal( lex.current().toString() );
			lex.nextSymbol();
			return BooleanType.TYPE;
		} else if( lex.current().equals( Symbols.BYTE_LITERAL ) ) {
			cgen.literal( lex.current().toString() );
			lex.nextSymbol();
			return ByteType.TYPE;
		} else if( lex.current().equals( Symbols.UNSIGNED_INTEGER_LITERAL ) ) {
			cgen.literal( lex.current().toString() );
			lex.nextSymbol();
			return UnsignedIntegerType.TYPE; 
		} else if( lex.current().equals( Symbols.LB_SYM ) ) {
			cgen.insertIntoCodeStream( Symbols.LB_SYM.toString() );
			lex.nextSymbol();
			ITypeRep t = clause();
			lex.mustBe( Symbols.RB_SYM );
			cgen.insertIntoCodeStream( Symbols.RB_SYM.toString() );
			return t;
		} else if( lex.current().equals( Symbols.NOT_SYM ) ) {
			cgen.unaryOp( lex.current().toString() );
			le1(n, 3);
			types.match( BooleanType.TYPE, expr(3 , false) );
			return BooleanType.TYPE;
		}else if( lex.current().equals( Symbols.BITWISE_NOT_SYM ) ) {
			cgen.unaryOp( Symbols.BITWISE_NOT_SYM.toString() );
			le1(n, 3);
			return types.integerCoerce(ByteType.TYPE, expr(3 , false) );
		} else if( lex.current().equals( Symbols.IDENTIFIER  ) ) {	
			return handle_name(inSend);
		} else {
			// identifier handling drops to here if we haven't read in an indentifier
			lex.mustBe( Symbols.IDENTIFIER );		// This will throw the right error if no identifier found
			return UnknownType.TYPE;
		}
	}
	// identifier handling starts here

	/**
	 * pre condition - have identifier
	 * @return
	 */
	public ITypeRep handle_name( boolean inSend) {
		String the_name = lex.current().toString();
		STEntry p = null;
		lex.mustBe( Symbols.IDENTIFIER  );		 // safe by precondition
		p = currentScope.lookup( the_name );
		if( p != null ) {
			if(p.getType() instanceof ChannelType){
				cgen.setRuntimeComponentFlags(p.getName());
			}
			if( p.getType() instanceof FunctionType ) {
				return proc_call(p);
			} else if( p.getType() instanceof EnumType ){
				cgen.enum_use( p , currentScope.getContext()); 
			} else if( lex.current().equals( Symbols.DOT_SYM ) || lex.current().equals( Symbols.LSB_SYM ) ) {
				return deref( p );	
			}
			// if STEntry is in send we need to flag it up in field
			// N.B. this sets inSend flags in all STEs that appear as the first name in an expression in a send
			// therefore constructs like send a+2 on c will be flagged even though it won't be used 
			if(inSend){
				p.setSent(true);
			}
		} 

		if( lex.current().equals( Symbols.EQUALS_SYM ) ) { // this is a <simple_decl> 
			IDecl loc = cgen.newLocation( the_name , currentScope.getScopeLevel() );
			lex.nextSymbol();
			Diagnostic.trace( DiagnosticLevel.RUN,"Simple Decl" );
			ITypeRep tr;
			boolean value_defined = false;
			if( value_decls_allowed ) {
				tr = value( loc );
				value_defined = true;
			} else {
				tr = expression(false);
			}
			if( ! currentScope.declare( the_name, tr, false ) ) {
				compilerErrors.nameDeclaredError(the_name);
			} else {
				loc.addSymbolTableEntry( currentScope.lookup(the_name) );
			}
			cgen.complete();
			return VoidType.TYPE;
		} else if( lex.current().equals( Symbols.ASSIGN_SYM ) ) { // this is an <assign_clause>
			// cgen.locationUsage( p );
			lex.nextSymbol();	
			IAssign code = cgen.assign( p ,  currentScope.getContext());
			Diagnostic.trace( DiagnosticLevel.RUN, "Assign" );
			if( p == null ) {
				compilerErrors.nameUndeclaredError(the_name);
				clause( code );
			} else {
				types.match( p.getType(), clause( code ) );
			}
			cgen.complete();
			return VoidType.TYPE;
		}
		if( p == null ) { 						// undeclared name
			compilerErrors.nameUndeclaredError(the_name);
			return UnknownType.TYPE;
		} else if( p.isType() ) {				// check that isnt a type  - it should be a value
			compilerErrors.typeForValue(the_name);
			return p.getType();
		} else {
			//cgen.locationUsage( p );	// it is just an identifier	
			cgen.locationUsage( p , currentScope.getContext());	// it is just an identifier	
			return p.getType();
		}
	}

	public ITypeRep deref( STEntry p ) {
//		<dereference>   ::= <component_struct_dereference> | <array_dereference>
//		<component_struct_dereference> ::= <expression> { <dot> <dereference> }
//		<array_dereference> ::= <expression> <lsb> [<clause_list>] <rsb>

		// JL added to track assignments to struct fields and array elements
		// needed to force copying if array or struct is also sent over a channel
		boolean just_dereferenced_struct_or_array = false;
		boolean just_dereferenced_component = false;
		ITypeRep t = p.getType();
		//IDereference code = cgen.dereference( p , currentScope.getScopeLevel());
		IDereference code = cgen.dereference( p , currentScope.getContext());
		boolean first_deref = true;
		while( lex.current().equals( Symbols.DOT_SYM ) || lex.current().equals( Symbols.LSB_SYM ) ) {
			if(first_deref){
				t = p.getType();
				first_deref = false;
			}
			//if( lex.have( Symbols.DOT_SYM ) ) {
			// JL insert for array.length
			if( !(t instanceof ArrayType || t instanceof StringType) && lex.have( Symbols.DOT_SYM ) ) {
				if( t instanceof ComponentType && !p.isType()) {
					t = component_deref( t, code );
					// remember that component was dereferenced
					just_dereferenced_component = true;
				} else if (t instanceof StructType){
					t = struct_deref( t, code );
					// remember that struct was dereferenced
					just_dereferenced_struct_or_array = true;
				} else {
					compilerErrors.dereferenceError(p.getType());
				}
			} 
			// JL insert for array.length
			else if((t instanceof ArrayType || t instanceof StringType) && lex.have(Symbols.DOT_SYM )){
				if(lex.have(Symbols.LENGTH_DEREF_SYM)){
					code.lengthDereference();
					t = IntegerType.TYPE;
					code.setDerefType(t);
					cgen.complete();
					return t;
				}
				else
					compilerErrors.dereferenceError(p.getType());
			}
			else if( lex.have( Symbols.LSB_SYM )) {
				do {                // is <clause_list> in Syntax
					code.arrayDereference();
					// remember that array was dereferenced
					just_dereferenced_struct_or_array = true;
					ITypeRep ctr = clause();
					types.match( IntegerType.TYPE, ctr );
					if( t instanceof ArrayType ) {
						t = ((ArrayType) t).getArray_type();
						code.setDerefType( t );
					} else {
						compilerErrors.dereferenceError( t );
						t = UnknownType.TYPE;
					}    
				}
				while( lex.have( Symbols.COMMA_SYM ) );
				// end of do while
				lex.mustBe( Symbols.RSB_SYM );
				if( lex.have(Symbols.DOT_SYM)){
					if( t instanceof ComponentType ) {
						t = component_deref( t, code );
						// remember that component was dereferenced
						just_dereferenced_component = true;
					} else if (t instanceof StructType){
						t = struct_deref( t, code );
						// remember that struct was dereferenced
						just_dereferenced_struct_or_array = true;
					} else if(t instanceof ArrayType){ // JL insert for array[i].length on multi-D arrays
						if(lex.have(Symbols.LENGTH_DEREF_SYM)){
							code.lengthDereference();
							t = IntegerType.TYPE;
							code.setDerefType(t);
							cgen.complete();
							return t;
						} else {
							compilerErrors.dereferenceError(p.getType());
						}
					} else {
						compilerErrors.dereferenceError(p.getType());
					}
				}
			}	
		}
		if( lex.have( Symbols.ASSIGN_SYM ) ) {
			if(just_dereferenced_struct_or_array)
				p.setAssignedTo(true);
			// copied from unary_op
			code.leftHandSide();
			// OLD IAssign code = cgen.assign( p );
			types.match( t, clause( code ) );
			Diagnostic.trace( DiagnosticLevel.RUN,"Assign" );
			cgen.complete();								// in the case of assignment (location)
			return VoidType.TYPE;
		}
		cgen.complete(); 									// in the case of no assignment (value)
		return t;
	}

	/**
	 * Checks that the supplied type is an int or a real
	 * @param t
	 * @return The resulting type
	 */
	public ITypeRep int_real( ITypeRep t ) {
		if( IntegerType.TYPE.equals( t ) )
			return IntegerType.TYPE;
		else if( UnsignedIntegerType.TYPE.equals( t ) )
			return UnsignedIntegerType.TYPE;
		else if( RealType.TYPE.equals( t ) )
			return RealType.TYPE;
		else {
			compilerErrors.typeError( IntegerType.TYPE, t );
			return UnknownType.TYPE;
		}
	}

	// ripped from S-algol compiler
	public ITypeRep expr( int n , boolean inSend) {
		// newline();
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		_Expr _expr = new _Expr();
		_expr.more = true;
		_expr.symbol = null;
		_expr.type = unary_op( n , inSend);

		// TODO JL should add parantheses, boolean and/or ops not working 

		while( _expr.more && ! lex.atNewLine() ) {
			ISymbol sy = lex.current();
			if ( lex.current().equals( Symbols.OR_SYM ) ) {
				and_or_op( 0, n, _expr, Symbols.OR_SYM );
			} else if( lex.current().equals( Symbols.AND_SYM ) ) {
				and_or_op(1, n, _expr, Symbols.AND_SYM );
			} else if( lex.current().equals( Symbols.BITWISE_OR_SYM )) {
				if (le2(n, 2, _expr)) {
					cgen.binaryOp( sy.toString() );
					_expr.type = types.integerCoerce(_expr.type, expr(3 , false));
				}
			} else if( lex.current().equals( Symbols.BITWISE_XOR_SYM )) {
				if (le2(n, 3, _expr)) {
					cgen.binaryOp( sy.toString() );
					_expr.type = types.integerCoerce(_expr.type, expr(4 , false));
				}
			} else if( lex.current().equals( Symbols.BITWISE_AND_SYM )) {
				if (le2(n, 4, _expr)) {
					cgen.binaryOp( sy.toString() );
					_expr.type = types.integerCoerce(_expr.type, expr(5 , false));
				}
			} else if( lex.current().equals( Symbols.EQUALSEQUALS_SYM ) || lex.current().equals( Symbols.NOT_EQUALS_SYM ) ) {
				if (le2(n, 5, _expr)) {
					cgen.binaryOp(sy.toString());
					if( ! _expr.type.equals( UnknownType.TYPE ) && (IntegerType.TYPE.equals( _expr.type ) || UnsignedIntegerType.TYPE.equals(_expr.type) || ByteType.TYPE.equals(_expr.type)) ) { // was eq2
						_expr.type = types.coerce(_expr.type, expr(6 , false));
					} else
						types.match(_expr.type, expr(6 , false));
					_expr.type = BooleanType.TYPE;
				}
			} else if( lex.current().equals( Symbols.LESS_THAN_EQUALS_SYM ) || 
					lex.current().equals( Symbols.LESS_THAN_SYM ) ||
					lex.current().equals( Symbols.GREATER_THAN_EQUALS_SYM ) ||
					lex.current().equals( Symbols.GREATER_THAN_SYM ) ) {
				if( le2( n, 5, _expr ) ) {
					cgen.binaryOp( sy.toString() );
					_expr.type = rel_type( _expr.type, expr(6 , false) );
				}
			} else if( lex.current().equals( Symbols.PLUS_SYM ) || 
					lex.current().equals( Symbols.MINUS_SYM ) ) {
				if (le2(n, 6, _expr)) {
					cgen.binaryOp( sy.toString() );
					_expr.type = types.coerce(_expr.type, expr(7 , false));
				}
			} else if( lex.current().equals( Symbols.MULT_SYM ) ) {
				if (le2(n, 7, _expr)) {
					cgen.binaryOp( Symbols.MULT_SYM.toString() );
					_expr.type = types.coerce(_expr.type, expr(8 , false));
				}
			} else if( lex.current().equals( Symbols.SLASH_SYM ) ) {
				if (le2(n, 7, _expr)) { 
					cgen.binaryOp( Symbols.SLASH_SYM.toString() );
					_expr.type = types.coerce(_expr.type, expr(8 , false));
				}
			} else if( lex.current().equals( Symbols.PERCENT_SYM ) ) {
				if (le2(n, 7, _expr)) {
					cgen.binaryOp( Symbols.PERCENT_SYM.toString() );
					types.match( IntegerType.TYPE, _expr.type );
					types.match( IntegerType.TYPE, expr(8 , false) );                        
					_expr.type = IntegerType.TYPE;
				}
			} else {
				_expr.more = false;
			}
		}
		return _expr.type;
	}


	private ITypeRep clause_block() {
//		<clause_block>	::= <lcb> <clause_sequence>  <rcb>
//		<clause_sequence> ::= <catch_clause> [ <semi_colon> <clause_sequence>]
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		ITypeRep type = VoidType.TYPE;
		boolean old_value_decls_allowed = value_decls_allowed;
		value_decls_allowed = false; 
		cgen.sequence();
		while( sequence_more() ) {
			type = catch_clause();
			if( sequence_more() ) {
				types.match( VoidType.TYPE,type );
				separator();
			}
		} 

		value_decls_allowed = old_value_decls_allowed;
		lex.mustBe( Symbols.RCB_SYM ); // this is probably wrong!
		cgen.complete();

		return type;
	}

	private ITypeRep struct_deref( ITypeRep t, IDereference context ) {        
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		StructType st = (StructType)t;
		//  lex.nextSymbol();
		ISymbol symb = lex.current();
		String the_name = symb.toString();
		context.fieldDereference( the_name );
		lex.mustBe( Symbols.IDENTIFIER );
		if( t instanceof StructType ) {
			ITypeRep field_type = st.getField(the_name);
			if( field_type.equals( UnknownType.TYPE ) ) {
				compilerErrors.dereferenceError( t,the_name );
			} 
			context.setDerefType( field_type );
			return field_type;
		} else return UnknownType.TYPE;
	}

	private ITypeRep component_deref( ITypeRep t, IDereference context ) {        
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		//    lex.nextSymbol();
		ISymbol symb = lex.current();
		String the_name = symb.toString();
		STEntry channelSTE = currentScope.lookup(the_name);
		context.channelDereference( the_name );
		//context.channelDereference( channelSTE );
		lex.mustBe( Symbols.IDENTIFIER );
		if( t instanceof ComponentType ) {
			InterfaceType interface_type = ((ComponentType) t).getInterface();
			ITypeRep channel_type = interface_type.getChannelType(the_name);
			if( channel_type.equals( UnknownType.TYPE ) ) {
				compilerErrors.dereferenceError( t,the_name );
			} 
			context.setDerefType( channel_type );
			return channel_type;
		} else return UnknownType.TYPE;
	}

	public void le1( int n, int p ) {
		if (n > p) {
			compilerErrors.expressionError( "Warning higher precedence expression");
		}
		lex.nextSymbol();
	}

	public ITypeRep proc_call( STEntry p ) {
		FunctionType procType = (FunctionType) p.getType() ; // FunctionType is precond.
		// for fist class procs, see if we are doing proc call or assignment 
		if( lex.have( Symbols.LB_SYM ) ) {	// we have a proc call
			//lex.mustBe( Symbols.LB_SYM );

			// JL start new proc_call code
			IProcCall code = cgen.proc_call( p , currentScope);
			cgen.setRuntimeComponentFlags(p.baseName());
			List<ITypeRep> argTypesList = procType.getArgs() ;
			if ( ! argTypesList.isEmpty() ) {
				int index = 0; 
				do {
					cgen.insertIntoCodeStream(", ");
					if( index < argTypesList.size() && ( argTypesList.get(index) instanceof FunctionType ) ) {
						ISymbol symb = lex.current();
						String the_name = symb.toString();
						STEntry ste = currentScope.lookup( the_name );
						if( ste == null ) {
							compilerErrors.nameUndeclaredError( the_name );   
						} else if( ! ste.isType() ) { // check this is a type decl
							compilerErrors.valueForType(the_name);
						} else  { 
							types.match( argTypesList.get(index), ste.getType() ) ;
						}
						lex.nextSymbol();
					} else if( index < argTypesList.size() ) { // normal case, clause argument of procedure
						types.match( argTypesList.get(index), clause() );
					} else {
						compilerErrors.syntaxError( "parameter", Symbols.RB_SYM.toString() );   
						clause();
					}
					index++;
				} while( lex.have( Symbols.COMMA_SYM ) );

				if( argTypesList.size() > index ) {  // still get param to process
					compilerErrors.syntaxError( lex.current().toString(),"parameter"  );                        
				}	
			}
			lex.mustBe( Symbols.RB_SYM );
			cgen.proc_call_end(); // for try catch blocks, must do jump to handler on exception
			ITypeRep procReturnType = procType.getResult();

			cgen.complete();
			return procReturnType;
		}  else { // it is a proc value
			return p.getType();
			/*  
			 * 
			 * IF we have first class procs this will deal with them -
			 * I have take this out for now due to implementation complexity
			 * 16/6/08
			 *
        	if( lex.have( Symbols.ASSIGN_SYM ) ) {

        		// TODO need code gen here 

    			types.match( t, clause() );

    			Diagnostic.trace( DiagnosticLevel.RUN,"Assign" );
    			//cgen.complete();								// in the case of assignment (location)
    			return VoidType.TYPE;
        	} else {
        		cgen.locationUsage( p );	// in the case of no assignment (value)
        		return t;
        	}
			 */
		}   
	}

	/***************************** Value Constructors *****************************/

	private ITypeRep value_constructor(IDecl loc) {  // V14 syntax checked al 
//		<value_constructor> ::= new <value_def>
//		<value_def> ::= <array_value> | <array_literal> | <component_value> | <channel_value> | <tuple_value>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );

		if( lex.have( Symbols.IN_SYM ) ) {  
			return channel_value( loc, ChannelType.IN );
		} else if( lex.have( Symbols.OUT_SYM ) ) {
			return channel_value( loc, ChannelType.OUT );
		} else if( lex.current().equals( Symbols.IDENTIFIER ) ) {
//			<array_value>   ::= <type> <dim_expr>
//			<component_value> ::= <identifier> <lrb> [ <clause_list> ] <rrb>
//			both can start with an identifier
			String the_name = lex.current().toString();
			STEntry ste = currentScope.lookup( the_name );
			if( ste == null ) {
				compilerErrors.nameUndeclaredError(the_name);
				return UnknownType.TYPE; 
			} else if( ste.getType() instanceof ComponentType ) {
				lex.nextSymbol();
				return component_value( the_name, (ComponentType) ste.getType() );
			} else if( ste.getType() instanceof ArrayType ) {
				lex.nextSymbol();
				return array_value( ste.getType() );
			} else if( ste.getType() instanceof StructType ) {
				lex.nextSymbol();
				if( lex.current().equals( Symbols.LSB_SYM ) ) {  // we have an array of Structs
					ITypeRep t = ste.getType();								 // need to fix up
					return array_value( t );
				} else 
					return struct_value( the_name, (StructType) ste.getType() );
			} else {
				compilerErrors.typeError( ste.getType(), "component/tuple or array" );
				return UnknownType.TYPE; // never called????                   
			}
		} else {
			// must be a <type1> <dim_expr> of <expression>
			return array_value( type1() );            
		}
	}

	private ITypeRep any_value() {
		lex.mustBe( Symbols.LB_SYM );
		IAnyConstructor code = cgen.anyConstructor();
		ITypeRep tr = expression(false);
		code.valueType( tr );
		lex.mustBe( Symbols.RB_SYM );
		cgen.complete();
		return AnyType.TYPE;
	}

	/**
	 * @param base_type the base type of the array - e.g. for new integer[3][4]
	 * this type will be integer.
	 * @return the type of the array  i.e. for above array( array( integer ) )
	 */
	private ITypeRep array_value( ITypeRep base_type ) { // V14 syntax checked al
//		<array_value> ::= <type1> <dim_expr> of <expression> 
		IArrayConstructor code = cgen.arrayConstructor();
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		ITypeRep arrai_type = base_type;
		while( lex.have( Symbols.LSB_SYM ) ) {     
			cgen.literal( lex.current().toString() );
			lex.mustBe( Symbols.INTEGER_LITERAL );
			lex.mustBe( Symbols.RSB_SYM );
			arrai_type = new ArrayType( arrai_type );
		}
		lex.mustBe( Symbols.OF_SYM );
		code.initialiser();
		ITypeRep exprType = expression(false);
		types.match( base_type,exprType );
		code.type(base_type,arrai_type);
		cgen.complete();
		return arrai_type;
	}

	private ITypeRep struct_value( String the_name, StructType st ) {   
//		<named_struct_value> ::= <identifier> <lrb> [ <clause_list> ] <rrb>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		List<ITypeRep> fields = st.getFields();
		List<String> names = st.getFieldNames();

		lex.mustBe( Symbols.LB_SYM );
		IStructConstructor code = cgen.structConstructor( st );
		int index = 0;
		if( ! lex.have( Symbols.RB_SYM ) ) {
			do {            // is <clause_list> in Syntax
				//        	code.parameter();
				if(index>0) { // put comma separator between constructor arguments
					cgen.insertIntoCodeStream(", ");
				}
				ITypeRep tr = clause();
				if( index < fields.size() && index < names.size() ) {
					ITypeRep fieldType = fields.get(index);
					code.fieldType( fieldType );
					code.fieldName( names.get(index) );
					types.match( fieldType, tr );
					index++;
				} else {
					compilerErrors.syntaxError( "field", ")" );
				}
			} while( lex.have( Symbols.COMMA_SYM ) );
			if(index < fields.size()){
				compilerErrors.syntaxError( ")", "field" );
			}
			lex.mustBe( Symbols.RB_SYM );
		}
		cgen.complete();
		return st;
	}

	////////////////////////
	private ITypeRep component_value( String the_name, ComponentType ct ) {   // V18 syntax checked al
//		<component_value> ::= <identifier> <lrb> [ <clause_list> ] <rrb>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		lex.mustBe( Symbols.LB_SYM );
		IConstructorCall code = cgen.constructorCall( the_name );

		FunctionType ft = new FunctionType();   // type of called constructor for matching
		ft.setConstructor();
		if( ! lex.have( Symbols.RB_SYM ) ) {
			do {            // is <clause_list> in Syntax
				code.parameter();
				ITypeRep t = clause();
				ft.addParam(t);
			}
			while( lex.have( Symbols.COMMA_SYM ) );
			lex.mustBe( Symbols.RB_SYM );
		}
		int index = ct.match_constructor( ft );
		if( index == ComponentType.NOT_FOUND ) {
			compilerErrors.constructorError( ct, ft );
		}
		code.setDisambiguatorAndParameters( index , ft.getArgs());
		cgen.setRuntimeComponentFlags(code.toString());

		cgen.complete();

		//Cgen.get_instance().addHoistedCodeToCurrentContext(sb1.toString());
		//cgen.insertIntoCodeStream(sb1.toString());


		/******************************************************************* 2008-08-11 Al & Jon fun **********************************/
		return ct; // was ct.getInterface(); 
		//return ct.getInterface(); 
	}

	private ITypeRep channel_value( IDecl loc, int direction ) {  // V18 syntax
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		ChannelType ct = channel_type( direction );
		cgen.channel( loc, ct );
		return ct;
	}

	private void struct_list( ITypeWithList st,StructConstructor tc ) {  // V18 syntax checked al
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		do {
			String the_name = lex.current().toString();
			lex.mustBe( Symbols.IDENTIFIER );
			lex.mustBe( Symbols.EQUALS_SYM );
			ITypeRep tr = expression(false);
			st.add( the_name,tr );
			tc.fieldType( tr );
			tc.fieldName( the_name );

		} while( lex.have( Symbols.COMMA_SYM ) );
	}

	// V27 Syntax, changed local_decls 
	//     <local_decls>        ::= <value_decl> [;<local_decls>]
	// to component_local_decls
	//	   <component_local_decls> ::= <component_local_decl> <separator> [<component_local_decls>]
	private boolean component_local_decls() {
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		while( ! lex.have( Symbols.CONSTRUCTOR_SYM )) {
			if ( lex.have( Symbols.EOT_SYM ) || lex.have(Symbols.RCB_SYM) || lex.have(Symbols.BEHAVIOUR_SYM)) {
				compilerErrors.missingConstructor();
				return false;
			}
			// Syntax V27 - altered by JL
			//decl();
			component_local_decl();
			separator();
		}
		return true;
	}

	private void constructors( ComponentType ct ) { // V18 Syntax checked
//		<constructors> ::=  constructor <lrb> [<named-param-list> ] <lcb> <non-alloc-block> <rcb> [ <semi_colon> <constructors> ] 
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		do {
			constructor(ct);
			separator();
		}
		while( lex.have( Symbols.CONSTRUCTOR_SYM ) );
	}

	private void constructor( ComponentType ct ) { 
//		<constructor>    ::=     constructor <lrb> [<named_param_list>] <rrb>
//		<lcb> <sequence> <rcb>

		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		lex.mustBe( Symbols.LB_SYM );
		FunctionType ft = new FunctionType();
		currentScope = currentScope.enterScope( currentScope,ISymbolTable.CONSTRUCTOR );        

		List<String> names = null;
		if( ! lex.have( Symbols.RB_SYM ) ) {
			names = named_parameter_list( ft,true );
			lex.mustBe( Symbols.RB_SYM );
		}
		lex.mustBe( Symbols.LCB_SYM );
		cgen.constructorBody( ft,names, currentScope.getScopeLevel() );
		sequence();
		lex.mustBe( Symbols.RCB_SYM );
		currentScope = currentScope.getParentScope();
		ct.addConstuctor(ft);
		cgen.complete();
	}

	private void behaviour() {      // V18 Syntax checked
//		<behaviour> ::= behaviour <alloc-block>
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		currentScope = currentScope.enterScope( currentScope,ISymbolTable.BEHAVIOUR );
		cgen.behaviourBody();
		lex.mustBe( Symbols.BEHAVIOUR_SYM );
		lex.mustBe( Symbols.LCB_SYM );
		sequence();
		lex.mustBe( Symbols.RCB_SYM );
		currentScope = currentScope.getParentScope();
		cgen.complete();
	}

	private List<String> named_parameter_list( ITypeWithList ft, boolean decl )  {  // V18 Syntax
//		<named_param_list>  ::= <type> <identifier_list>
//		[<semi_colon> <named_param_list>]  
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		List<String> names = new LinkedList<String>();
		if( ! lex.current().equals( Symbols.RB_SYM ) ) {	// al fixed 18/8/2008
			do {
				ITypeRep ptype = type();
				identifier_list( ft,ptype,names,decl );
			}
			while( lex.have( Symbols.SEMI_SYM ) ); 
		}
		return names;
	}

	private void identifier_list( ITypeWithList ft, ITypeRep declType, List<String> names, boolean decl )  {  // DB10 syntax checked al
//		<identifier> [ <comma> <identifier> ]
		Diagnostic.trace( DiagnosticLevel.RUN,"" );
		do {
			ISymbol s = lex.current();
			lex.mustBe( Symbols.IDENTIFIER );
			String the_name = s.toString();
			names.add( the_name );
			if( decl && ! currentScope.declare( the_name, declType, false ) ) {
				compilerErrors.nameDeclaredError(the_name);     
			}
			ft.add( the_name, declType );
		}
		while( lex.have( Symbols.COMMA_SYM ) );
	} 
}


