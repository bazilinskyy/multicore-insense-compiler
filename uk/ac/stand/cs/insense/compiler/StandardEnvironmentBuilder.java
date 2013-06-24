package uk.ac.stand.cs.insense.compiler;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

import uk.ac.stand.cs.insense.compiler.cgen.IAnyConstructor;
import uk.ac.stand.cs.insense.compiler.cgen.IAnyProject;
import uk.ac.stand.cs.insense.compiler.cgen.IArrayConstructor;
import uk.ac.stand.cs.insense.compiler.cgen.IAssign;
import uk.ac.stand.cs.insense.compiler.cgen.ICodeGenerator;
import uk.ac.stand.cs.insense.compiler.cgen.ICompilationUnit;
import uk.ac.stand.cs.insense.compiler.cgen.IComponent;
import uk.ac.stand.cs.insense.compiler.cgen.IConstructorCall;
import uk.ac.stand.cs.insense.compiler.cgen.IConditional;
import uk.ac.stand.cs.insense.compiler.cgen.IConnect;
import uk.ac.stand.cs.insense.compiler.cgen.IDecl;
import uk.ac.stand.cs.insense.compiler.cgen.IDeclarationContainer;
import uk.ac.stand.cs.insense.compiler.cgen.IDereference;
import uk.ac.stand.cs.insense.compiler.cgen.IExceptionBlock;
import uk.ac.stand.cs.insense.compiler.cgen.IForLoop;
import uk.ac.stand.cs.insense.compiler.cgen.ILocation;
import uk.ac.stand.cs.insense.compiler.cgen.IReceive;
import uk.ac.stand.cs.insense.compiler.cgen.ISelect;
import uk.ac.stand.cs.insense.compiler.cgen.ISend;
import uk.ac.stand.cs.insense.compiler.cgen.ISwitch;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.Cgen;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.Decl;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.DeclarationContainer;
import uk.ac.stand.cs.insense.compiler.incesosCCgen.StructConstructor;
import uk.ac.stand.cs.insense.compiler.interfaces.ICompilerErrors;
import uk.ac.stand.cs.insense.compiler.interfaces.ILexicalAnalyser;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbol;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.interfaces.ISyntaxAnalyser;
import uk.ac.stand.cs.insense.compiler.interfaces.ITypeChecker;
import uk.ac.stand.cs.insense.compiler.symbols.STEntry;
import uk.ac.stand.cs.insense.compiler.symbols.Symbol;
import uk.ac.stand.cs.insense.compiler.symbols.SymbolTable;
import uk.ac.stand.cs.insense.compiler.symbols.Symbols;
import uk.ac.stand.cs.insense.compiler.types.AnyType;
import uk.ac.stand.cs.insense.compiler.types.ArrayType;
import uk.ac.stand.cs.insense.compiler.types.BooleanType;
import uk.ac.stand.cs.insense.compiler.types.ByteType;
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
public class StandardEnvironmentBuilder extends SyntaxAnalyser implements ISyntaxAnalyser {

	public StandardEnvironmentBuilder(ILexicalAnalyser lex, ITypeChecker tc, ICompilerErrors ce, ICodeGenerator cgen, ISymbolTable scope ) {
		super( lex, tc, ce, cgen,scope );
	}
	
	public void parse() {
		lex.nextSymbol(); // pre load the lexical analyser
        while( ! lex.current().equals( Symbols.EOT_SYM ) ) {
            definitions();
            separator();
        }
	}

	private void definitions() {
        Diagnostic.trace( DiagnosticLevel.RUN,"" );
        if( lex.have( Symbols.TYPE_SYM ) ) {
            type_decl();
        } else {
        	defintion();
        }
	}

	private void defintion() {
        Diagnostic.trace( DiagnosticLevel.RUN,"" );
        ISymbol s = lex.current();
        lex.mustBe( Symbols.IDENTIFIER );
        String the_name = s.toString();
        lex.mustBe( Symbols.COLON_SYM );
        ITypeRep theType = type();
        // We cannot have instances of interfaces - only components
        // so wrap up the interface to look like a component.
        if( theType instanceof InterfaceType ) {
            ComponentType ct = new ComponentType( the_name );      
            ct.addInterface( (InterfaceType) theType ); 
            theType = ct;
        }
        if( ! currentScope.declare( the_name, theType, false ) ) {
            compilerErrors.nameDeclaredError(the_name);     
        }
	}
}


