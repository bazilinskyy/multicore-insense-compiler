package uk.ac.stand.cs.insense.compiler.cgen;

import java.util.List;

import uk.ac.stand.cs.insense.compiler.incesosCCgen.StructConstructor;
import uk.ac.stand.cs.insense.compiler.interfaces.ICompilerErrors;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.symbols.STEntry;
import uk.ac.stand.cs.insense.compiler.types.ChannelType;
import uk.ac.stand.cs.insense.compiler.types.FunctionType;
import uk.ac.stand.cs.insense.compiler.types.StructType;

public interface ICodeGenerator {

	void finish();
	
	String popLastAppend();
	
	ICode popCurrentCode();
	
	void complete();

	IFunction findEnclosingFunctionContainer();
	
	IDeclarationContainer findEnclosingDelcarationContainer();
	
	ICompilationUnit compilationUnit();
	
	void addIncludeToCurrentContext( String s );
	
	// void addVTBLUsageToCompilationUnit( String s ); // TODO NOT NEEDED FOR INCEOS?
	
	IComponent componentBody(String the_name);
	
	ICode procedureBody(String the_name, FunctionType ft, List<String> names, int scope_level);

	ICode constructorBody(FunctionType ft, List<String> names, int scope_level);
	
	IBehaviour behaviourBody();
	
	IDecl newLocation( String name , int scopeLevel );
	
	void locationUsage( STEntry entry , int fromContext);
	
	void channel( IDecl loc, ChannelType ct );
	
	void channel( STEntry entry );
	
	ISequence sequence();
	
	IArrayConstructor arrayConstructor();
	
	IConstructorCall constructorCall( String the_name );
	
	IStructConstructor structConstructor( StructType st );
	
	IConditional ifClause();
	
	IForLoop forLoop(ISymbolTable for_table);
	
	ISwitch switchcode();	
	
	IAssign assign( STEntry ste , int fromContext);
	
	void setRuntimeComponentFlags(String code);
	
	IConnect connect(int fromContext);
	
	void connect_call_end( );

	IPublish publish(ISymbolTable fromContext);
	
	void publish_call_end( );

	IDisconnect disconnect(int fromContext);
	
	void disconnect_call_end( );

	ISelect select(int fromContext);
	
	ISend send(int fromContext);
	
	void send_call_end( );
	
	IStop stop(int fromContext);
	
	IReceive receive( STEntry lhs, STEntry rhs, int fromContext );
	
	IAcknowledge acknowledge( STEntry lhs, STEntry rhs, int fromContext);
	
	void binaryOp( String op );
	
	void unaryOp( String op );
	
	void insertIntoCodeStream(String s);
	
	void literal( String literal );
	
	void stringLiteral( String literal );
	
	IProcCall proc_call( STEntry ste, ISymbolTable context );
	
	void proc_call_end( );

	IDereference dereference( STEntry ste , int fromContext);
	
	IArrayDereference arrayDereference( STEntry ste );

	void enum_use( STEntry ste , int fromContext);
	
	IAnyConstructor anyConstructor();
	
	//IAnyProject anyProject( STEntry ste , int fromContext);
	IAnyProject anyProject( String subject_name , int fromContext);

	IExceptionBlock tryCatchBlock(int fromContext);
	
	IReturn return_clause();
	
	IThrow throw_clause(String exception);
	
	void throw_clause_end();
	
	public ICompilerErrors getCompilerErrors();
}
