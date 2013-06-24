package uk.ac.stand.cs.insense.compiler.symbols;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import uk.ac.stand.cs.insense.compiler.interfaces.ISymbol;


/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:ajm@dcs.st-and.ac.uk"> Andrew J. McCarthy </a>
 * @author <a href="mailto:al@dcs.st-and.ac.uk"> Alan Dearle </a>
*/
public class Symbols {	

    public static ISymbol getKeywordSymbol(String stringRep){
        return (ISymbol) keywordSymbols.get( stringRep );
    }
    
	
	public static Collection<String> getKeywords() {
		return keywordSymbols.keySet();
	}
    
    private static Map<String,ISymbol> keywordSymbols = new HashMap<String,ISymbol>();
    
    // Keyword strings
        
    public static final String as_s = "as";
    public static final String and_s = "and";
    public static final String any_s = "any";
    public static final String behaviour_s = "behaviour";
    public static final String bitwise_and_s = "&";
    public static final String bitwise_or_s = "|";
    public static final String bitwise_xor_s = "^";
    public static final String bitwise_not_s = "~";
    public static final String bool_s = "bool";
    public static final String byte_s = "byte";
    public static final String case_s = "case";
    public static final String component_s = "component";
    public static final String connect_s = "connect";
    public static final String constructor_s = "constructor";
    public static final String default_s = "default";
    public static final String disconnect_s = "disconnect";
    public static final String do_s = "do";
    public static final String then_s = "then";
    public static final String else_s = "else";
    public static final String enum_s = "enum";
    public static final String except_s = "except";
    public static final String false_s = "false";
    public static final String for_s = "for";
    public static final String from_s = "from";
    public static final String proc_s = "proc";
    public static final String if_s = "if";
    public static final String in_s = "in";
    public static final String integer_s = "integer";
    public static final String unsigned_integer_s = "unsigned";
    public static final String interface_s = "interface";
    public static final String is_s = "is";
    public static final String new_s = "new";
    public static final String not_s = "not";
    public static final String of_s = "of";
    public static final String on_s = "on";
    public static final String onto_s = "onto";
    public static final String or_s = "or";
    public static final String out_s = "out";
    public static final String presents_s = "presents";
    public static final String project_s = "project";
    public static final String real_s = "real";
    public static final String receive_s = "receive";
    public static final String acknowledge_s = "acknowledge";
    public static final String after_s = "after";
    public static final String return_s = "return";
    public static final String select_s = "select";
    public static final String send_s = "send";
    public static final String start_s = "start";
    public static final String stop_s = "stop";
    public static final String string_s = "string";
    public static final String struct_s = "struct";
    public static final String to_s = "to";
    public static final String true_s = "true";
    public static final String try_s = "try";
    public static final String throw_s = "throw"; // testing syntax for try-except
    public static final String type_s = "type";
    public static final String when_s = "when";
    public static final String while_s = "while";
    public static final String with_s = "with";
    public static final String xor_s = "xor";
    public static final String length_deref_s = "length";
    public static final String publish_s = "publish";

    // Single and double character Symbols
                     
    public static final ISymbol LESS_THAN_SYM = new Symbol( "<" );
    public static final ISymbol GREATER_THAN_SYM = new Symbol( ">" );
    public static final ISymbol NOT_SYM = new Symbol( "!" );
    public static final ISymbol GREATER_THAN_EQUALS_SYM = new Symbol( ">=" );
    public static final ISymbol LESS_THAN_EQUALS_SYM = new Symbol( "<=" );
    public static final ISymbol EQUALSEQUALS_SYM = new Symbol( "==" );
    public static final ISymbol EQUALS_SYM = new Symbol( "=" );
    public static final ISymbol ASSIGN_SYM = new Symbol( ":=" );
    public static final ISymbol NOT_EQUALS_SYM = new Symbol( "!=");
    public static final ISymbol SEMI_SYM = new Symbol( ";" );  
    public static final ISymbol COLON_SYM = new Symbol( ":" ); 
    public static final ISymbol LSB_SYM = new Symbol( "[" );
    public static final ISymbol RSB_SYM = new Symbol( "]" );    
    public static final ISymbol LB_SYM = new Symbol( "(" );
    public static final ISymbol RB_SYM = new Symbol( ")" );
    public static final ISymbol DOT_SYM = new Symbol( "." ); 
    public static final ISymbol DOTDOT_SYM = new Symbol( ".." );
    public static final ISymbol COMMA_SYM = new Symbol( "," );   
    public static final ISymbol PLUS_SYM = new Symbol( "+" );
    public static final ISymbol MINUS_SYM = new Symbol( "-" );
    public static final ISymbol MULT_SYM = new Symbol( "*" );
    public static final ISymbol SLASH_SYM = new Symbol( "/" );
    public static final ISymbol PERCENT_SYM = new Symbol( "%" );
    public static final ISymbol LCB_SYM = new Symbol( "{" );
    public static final ISymbol RCB_SYM = new Symbol( "}" );
    public static final ISymbol EOT_SYM = new Symbol( "?" );
    public static final ISymbol BITWISE_AND_SYM = new Symbol( bitwise_and_s );
    public static final ISymbol BITWISE_OR_SYM = new Symbol( bitwise_or_s );
    public static final ISymbol BITWISE_XOR_SYM = new Symbol( bitwise_xor_s );
    public static final ISymbol BITWISE_NOT_SYM = new Symbol( bitwise_not_s );
    
    // Keyword symbols
    
    public static final ISymbol AS_SYM = new Symbol( as_s );
    public static final ISymbol AND_SYM = new Symbol( and_s );
    public static final ISymbol ANY_SYM = new Symbol( any_s );
    public static final ISymbol BOOL_SYM = new Symbol( bool_s );
    public static final ISymbol BYTE_SYM = new Symbol( byte_s );
    public static final ISymbol CASE_SYM = new Symbol( case_s ); 
    public static final ISymbol BEHAVIOUR_SYM = new Symbol( behaviour_s );
    public static final ISymbol COMPONENT_SYM = new Symbol( component_s ); 
    public static final ISymbol CONNECT_SYM = new Symbol( connect_s );
    public static final ISymbol CONSTRUCTOR_SYM = new Symbol( constructor_s );
    public static final ISymbol DEFAULT_SYM = new Symbol( default_s );
    public static final ISymbol DISCONNECT_SYM = new Symbol( disconnect_s );
    public static final ISymbol DO_SYM = new Symbol( do_s );
    public static final ISymbol THEN_SYM = new Symbol( then_s ); 
    public static final ISymbol ELSE_SYM = new Symbol( else_s );    
    public static final ISymbol ENUM_SYM = new Symbol( enum_s );  
    public static final ISymbol EXCEPT_SYM = new Symbol( except_s );  
    public static final ISymbol FALSE_SYM = new BoolSymbol( false_s );
    public static final ISymbol FOR_SYM = new Symbol( for_s );
    public static final ISymbol FROM_SYM = new Symbol( from_s );
    public static final ISymbol PROC_SYM = new Symbol( proc_s ); 
    public static final ISymbol IF_SYM = new Symbol( if_s );
    public static final ISymbol IN_SYM = new Symbol( in_s ); 
    public static final ISymbol INTEGER_SYM = new Symbol( integer_s ); 
    public static final ISymbol UNSIGNED_INTEGER_SYM = new Symbol( unsigned_integer_s ); 
    public static final ISymbol INTERFACE_SYM = new Symbol( interface_s );
    public static final ISymbol IS_SYM = new Symbol( is_s );      
    public static final ISymbol NEW_SYM = new Symbol( new_s );
    public static final ISymbol NOT_LOGICAL_SYM = new Symbol( not_s );  
    public static final ISymbol OF_SYM = new Symbol( of_s );
    public static final ISymbol ON_SYM = new Symbol( on_s );
    public static final ISymbol ONTO_SYM = new Symbol( onto_s );
    public static final ISymbol OR_SYM = new Symbol( or_s );
    public static final ISymbol OUT_SYM = new Symbol( out_s ); 
    public static final ISymbol PRESENTS_SYM = new Symbol( presents_s );
    public static final ISymbol PROJECT_SYM = new Symbol( project_s );
    public static final ISymbol REAL_SYM = new Symbol( real_s );
    public static final ISymbol RECEIVE_SYM = new Symbol( receive_s );
    public static final ISymbol ACKNOWLEDGE_SYM = new Symbol( acknowledge_s );
    public static final ISymbol AFTER_SYM = new Symbol( after_s );
    public static final ISymbol RETURN_SYM = new Symbol( return_s );   
    public static final ISymbol SELECT_SYM = new Symbol( select_s );
    public static final ISymbol SEND_SYM = new Symbol( send_s );
    public static final ISymbol STOP_SYM = new Symbol( stop_s );
    public static final ISymbol STRING_SYM = new Symbol( string_s );
    public static final ISymbol STRUCT_SYM = new Symbol(struct_s ); 
    public static final ISymbol THROW_SYM = new Symbol( throw_s ); // testing syntax for try-except
    public static final ISymbol TO_SYM = new Symbol( to_s );
    public static final ISymbol TRUE_SYM = new BoolSymbol( true_s );
    public static final ISymbol TRY_SYM = new BoolSymbol( try_s );
    public static final ISymbol TYPE_SYM = new Symbol( type_s );  
    public static final ISymbol WHILE_SYM = new Symbol( while_s );  
    public static final ISymbol WITH_SYM = new Symbol( with_s );
    public static final ISymbol WHEN_SYM = new Symbol( when_s );     
    public static final ISymbol XOR_SYM = new Symbol( xor_s );
    public static final ISymbol LENGTH_DEREF_SYM = new Symbol(length_deref_s);
    public static final ISymbol PUBLISH_SYM = new Symbol(publish_s);
  
    /* Literals and identifiers */
    
    public static final ISymbol IDENTIFIER = IdentifierSymbol.IDENTIFIER;
    public static final ISymbol INTEGER_LITERAL = IntegerSymbol.INTEGER;
    public static final ISymbol UNSIGNED_INTEGER_LITERAL = UnsignedIntegerSymbol.UNSIGNEDINTEGER;
    public static final ISymbol REAL_LITERAL = RealSymbol.REAL;
    public static final ISymbol BYTE_LITERAL = ByteSymbol.BYTE;
    public static final ISymbol STRING_LITERAL = StringSymbol.STRING;
    public static final ISymbol BOOL_LITERAL = BoolSymbol.BOOL;
    
    
    /* Keyword symbols map */
    
	static {   // These have been checked against DB9 syntax
        keywordSymbols.put( acknowledge_s,ACKNOWLEDGE_SYM );
        keywordSymbols.put( after_s, AFTER_SYM );
        keywordSymbols.put( length_deref_s, LENGTH_DEREF_SYM );
		keywordSymbols.put( as_s,AS_SYM );
		keywordSymbols.put( and_s,AND_SYM );
		keywordSymbols.put( any_s,ANY_SYM );
		keywordSymbols.put( behaviour_s,BEHAVIOUR_SYM );
		keywordSymbols.put( bitwise_and_s, BITWISE_AND_SYM );
		keywordSymbols.put( bitwise_or_s, BITWISE_OR_SYM );
		keywordSymbols.put( bitwise_xor_s, BITWISE_XOR_SYM );
		keywordSymbols.put( bitwise_not_s, BITWISE_NOT_SYM );
        keywordSymbols.put( bool_s,BOOL_SYM ); 
        keywordSymbols.put( byte_s,BYTE_SYM ); 
        keywordSymbols.put( component_s,COMPONENT_SYM );
        keywordSymbols.put( connect_s,CONNECT_SYM );
        keywordSymbols.put( constructor_s,CONSTRUCTOR_SYM );
        keywordSymbols.put( default_s,DEFAULT_SYM );
        keywordSymbols.put( disconnect_s,DISCONNECT_SYM );
        keywordSymbols.put( do_s,DO_SYM );
        keywordSymbols.put( else_s,ELSE_SYM );
        keywordSymbols.put( enum_s,ENUM_SYM );
        keywordSymbols.put( except_s,EXCEPT_SYM );
        keywordSymbols.put( false_s,FALSE_SYM );
        keywordSymbols.put( for_s,FOR_SYM );
        keywordSymbols.put( from_s,FROM_SYM );
        keywordSymbols.put( proc_s,PROC_SYM );
        keywordSymbols.put( if_s,IF_SYM );
        keywordSymbols.put( in_s,IN_SYM );
        keywordSymbols.put( integer_s,INTEGER_SYM );
        keywordSymbols.put( unsigned_integer_s,UNSIGNED_INTEGER_SYM );
        keywordSymbols.put( interface_s,INTERFACE_SYM );
        keywordSymbols.put( is_s,IS_SYM );
        keywordSymbols.put( new_s,NEW_SYM );
        keywordSymbols.put( of_s,OF_SYM );
        keywordSymbols.put( on_s,ON_SYM );
        keywordSymbols.put( onto_s,ONTO_SYM );
        keywordSymbols.put( or_s,OR_SYM );
        keywordSymbols.put( out_s,OUT_SYM );
        keywordSymbols.put( presents_s, PRESENTS_SYM );
        keywordSymbols.put( project_s, PROJECT_SYM );
        keywordSymbols.put( real_s,REAL_SYM );
        keywordSymbols.put( receive_s,RECEIVE_SYM );
        keywordSymbols.put( return_s,RETURN_SYM );
        keywordSymbols.put( select_s,SELECT_SYM );
        keywordSymbols.put( send_s,SEND_SYM );
        keywordSymbols.put( stop_s,STOP_SYM );
        keywordSymbols.put( string_s,STRING_SYM );
        keywordSymbols.put( struct_s,STRUCT_SYM );
        keywordSymbols.put( case_s,CASE_SYM );
        keywordSymbols.put( then_s,THEN_SYM );
        keywordSymbols.put( throw_s,THROW_SYM ); // testing syntax for try-except
        keywordSymbols.put( to_s,TO_SYM );
        keywordSymbols.put( true_s,TRUE_SYM );
        keywordSymbols.put( try_s,TRY_SYM );
        keywordSymbols.put( type_s,TYPE_SYM );
        keywordSymbols.put( when_s, WHEN_SYM );
        keywordSymbols.put( with_s,WITH_SYM );
        keywordSymbols.put( publish_s,PUBLISH_SYM );
        }
}