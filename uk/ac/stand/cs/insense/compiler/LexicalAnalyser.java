package uk.ac.stand.cs.insense.compiler;

import uk.ac.stand.cs.insense.compiler.interfaces.ICompilerErrors;
import uk.ac.stand.cs.insense.compiler.interfaces.ILexicalAnalyser;
import uk.ac.stand.cs.insense.compiler.interfaces.ISourceRepresentation;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbol;
import uk.ac.stand.cs.insense.compiler.symbols.ByteSymbol;
import uk.ac.stand.cs.insense.compiler.symbols.IdentifierSymbol;
import uk.ac.stand.cs.insense.compiler.symbols.IntegerSymbol;
import uk.ac.stand.cs.insense.compiler.symbols.RealSymbol;
import uk.ac.stand.cs.insense.compiler.symbols.StringSymbol;
import uk.ac.stand.cs.insense.compiler.symbols.Symbol;
import uk.ac.stand.cs.insense.compiler.symbols.Symbols;
import uk.ac.stand.cs.insense.compiler.symbols.UnsignedIntegerSymbol;
import uk.ac.standrews.cs.nds.util.Diagnostic;
import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
import uk.ac.standrews.cs.nds.util.ErrorHandling;

/**
 * DIAS Project
 * INSENSE
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:ajm@dcs.st-and.ac.uk"> Andrew J. McCarthy </a>
 * @author <a href="mailto:al@dcs.st-and.ac.uk"> Alan Dearle </a>
 */



//<literal> ::= <int-literal> | <real-literal> | <bool-literal> | <string-literal> | <byte-literal>
//<int-literal> ::= [add-op] digit {digit}
//<real-literal ::= int-literal.{digit}[e <int-literal>]
//<bool-literal> ::= true | false
//<string-literal> ::= " {char} "
//<byte-literal> ::= # digit {digit}

public class LexicalAnalyser implements ILexicalAnalyser {
	/* CONSTANTS */
    private static final int SECOND_CHAR_INDEX = 1;

    /* Scanning */
    private static final char NEW_LINE_CHAR = '\n';
    private static final char TAB_CHAR = '\t';
    private static final char SPACE_CHAR = ' ';
    private static final char C_RETURN_CHAR = '\r';
    private static final char FORM_FEED_CHAR = '\f';
    private static final char HASH_CHAR = '#';
    private static final char DOLLAR_CHAR = '$';
    private static final char SEMI_COLON = ';';
    private static final char LESS_THAN = '<';
    private static final char GREATER_THAN = '>';
    private static final char NOT = '!';
    private static final char EQUALS = '=';
    private static final char LB = '(';
    private static final char RB = ')';    
    private static final char LCB = '{'; 
    private static final char RCB = '}'; 
    private static final char LSB = '['; 
    private static final char RSB = ']'; 
    private static final char DOT = '.'; 
    private static final char COMMA = ','; 
    private static final char COLON = ':';
    private static final char PLUS = '+'; 
    private static final char MINUS = '-'; 
    private static final char MULT = '*'; 
    private static final char SLASH = '/';
    private static final char PERCENT = '%';
    private static final char BIWISE_AND = '&';
    private static final char BITWISE_OR = '|';
    private static final char BITWISE_XOR = '^';
    private static final char BITWISE_NOT = '~';
    private static final String PLUSPLUS = "++";
    private static final String DOTDOT = "..";
    private static final String COLONEQUALS = ":=";
    private static final String EQUALSEQUALS = "==";
    private static final String LESS_THAN_EQUALS = "<=";
    private static final String GREATER_THAN_EQUALS = ">=";
    private static final String NOT_EQUALS = "!=";   
    public static final char STRING_DELIM = '"';
	private char ZERO = '0';
	
	final String maxintstr = String.valueOf(Integer.MAX_VALUE);
	final String minintstr = String.valueOf(Integer.MAX_VALUE + 1);
        
	private ISourceRepresentation source;
	private ISymbol waiting = null;
    private ICompilerErrors compilerError;
    private boolean recovered = true;
    private boolean at_new_line = false;
    private boolean unary_minus = false;

	
	public LexicalAnalyser( ISourceRepresentation source, ICompilerErrors ce ){
		this.source = source;
        this.compilerError = ce;
        ce.setLex( this );
	}
	

	public void setUnaryMinus(boolean truefalse) {
		unary_minus = truefalse;
	}
    
    public boolean atNewLine() {
        return at_new_line;
    }
    
    public ISymbol current() {
        return waiting;
    }
    
    public String currentLine() {
    	return source.currentLine();
    }
    
	public void nextSymbol()  {
		char nextChar = source.getNextChar();
		
        at_new_line = false;
        boolean more = true;
        while (more) {
            if( nextChar == SPACE_CHAR || nextChar == TAB_CHAR ) {
                nextChar = source.getNextChar();
            } else if( nextChar == NEW_LINE_CHAR ) {
                at_new_line = true;
                source.resetCurrentLine();
                nextChar = source.getNextChar();
            } else if( nextChar == C_RETURN_CHAR ) {
                at_new_line = true;
                source.resetCurrentLine();
                nextChar = source.getNextChar();
            } else if( nextChar == FORM_FEED_CHAR ) {
                at_new_line = true;
                source.resetCurrentLine();
                nextChar = source.getNextChar();
            } else if( nextChar == SLASH ) {
                char look = source.peek(); // was nextChar
                if ( look == MULT  ) { // we are in a /* comment
                    nextChar = source.getNextChar(); // is the *
                    nextChar = source.getNextChar(); // move the parse on
                    boolean inCommentBlock = true;
                    while( inCommentBlock ) {
                        if( nextChar == MULT ){             // looking for */
                            nextChar = source.getNextChar();
                            if ( nextChar == SLASH ){           // got a * looking for /
                                nextChar =  source.getNextChar();
                                inCommentBlock = false;
                            }
                        } else {
                            nextChar = source.getNextChar();    // throw away if not a */
                        }
                    }
                } else if ( look == SLASH ) {  // this is the // comment case
                	nextChar = source.getNextChar(); // the second / character
                    nextChar = source.getNextChar(); // move the parse on
                    boolean inCommentBlock = true;
                    while ( inCommentBlock ) {
                        if ( nextChar == NEW_LINE_CHAR || nextChar == FORM_FEED_CHAR  || nextChar == C_RETURN_CHAR ){
                            inCommentBlock = false;
                        } else {
                        	nextChar = source.getNextChar();
                        }
                    }
                } else {
                	more = false; // must be a / on its own.
                }
            } else {
                more = false;
            }
        } 
        
        if( nextChar == HASH_CHAR ) {	// Because we send the first DIGIT to byteLiteral - not the hash
        	waiting = byteLiteral( source.getNextChar() );
        }
        else if( nextChar == DOLLAR_CHAR ) {
        	waiting = unsignedLiteral( source.getNextChar() );
        }
        else if ( Character.isDigit( nextChar ) ) {
            waiting = numericalLiteral( nextChar );
		}
		else if ( Character.isLetter( nextChar ) ) {
            waiting = tryIdentifier( nextChar );
		}
		else {
//            Diagnostic.trace( Diagnostic.RUN,"Next char waiting == " + nextChar );
			switch( nextChar ) {
				case (char) -1:
                    waiting = Symbols.EOT_SYM;  // al was here
                    break;
                case SEMI_COLON:
                    waiting = Symbols.SEMI_SYM;
                    break;
				case STRING_DELIM:
                    waiting = readString();
                    break;
				case LESS_THAN:
					if( composite( LESS_THAN_EQUALS ) ){
                        waiting = Symbols.LESS_THAN_EQUALS_SYM;
					}
					else {
                        waiting = Symbols.LESS_THAN_SYM;
					}
                    break;
				case GREATER_THAN:
					if( composite( GREATER_THAN_EQUALS ) ) {
                        waiting = Symbols.GREATER_THAN_EQUALS_SYM;
					}
					else {
                        waiting = Symbols.GREATER_THAN_SYM;
					}
                    break;
				case NOT:
					if( composite( NOT_EQUALS ) ) {
                        waiting = Symbols.NOT_EQUALS_SYM;
					}
					else {
                        waiting = Symbols.NOT_SYM;
					}
                    break;
                case DOT:
                	if( composite( DOTDOT ) ) {
                		waiting = Symbols.DOTDOT_SYM;
                	}
                	else {
                		waiting = Symbols.DOT_SYM;
                	}
                    break;
				case EQUALS:
					if( composite( EQUALSEQUALS ) ) {
                		waiting = Symbols.EQUALSEQUALS_SYM;
                	}
                	else {
                		waiting = Symbols.EQUALS_SYM;	
                	}
                    break;  
                case COLON:
                	if( composite( COLONEQUALS ) ) {
                		waiting = Symbols.ASSIGN_SYM;
                	} else
                	{
                		waiting = Symbols.COLON_SYM;
                	}
                	break;
				case PLUS:
            		waiting = Symbols.PLUS_SYM;	
            		break;
				case MINUS:
                    waiting = Symbols.MINUS_SYM;
                    break;
                case LB:
                    waiting = Symbols.LB_SYM;
                    break;
				case RB:
                    waiting = Symbols.RB_SYM;
                    break;
                case LCB:
                    waiting = Symbols.LCB_SYM;
                    break;
                case RCB:
                    waiting = Symbols.RCB_SYM;
                    break;				
                case LSB:
                    waiting = Symbols.LSB_SYM;
                    break;
                case RSB:
                    waiting = Symbols.RSB_SYM;
                    break;	
				case COMMA:
                    waiting = Symbols.COMMA_SYM;
                    break;
				case MULT:
                    waiting = Symbols.MULT_SYM;
                    break;
				case PERCENT:
                    waiting = Symbols.PERCENT_SYM;
                    break;
				case BIWISE_AND:
                    waiting = Symbols.BITWISE_AND_SYM;
                    break;
				case BITWISE_OR:
                    waiting = Symbols.BITWISE_OR_SYM;
                    break;
				case BITWISE_XOR:
                    waiting = Symbols.BITWISE_XOR_SYM;
                    break;
				case BITWISE_NOT:
                    waiting = Symbols.BITWISE_NOT_SYM;
                    break;
				case SLASH:
                    waiting = Symbols.SLASH_SYM;
                    break;
                default:
                    waiting = new Symbol( new String( new char[] { nextChar } ) );
					ErrorHandling.error("CANT RECOGNISE CHARACTER >" + nextChar + "< line: " + lineNumber());
			}
		}
        Diagnostic.trace( DiagnosticLevel.RUN,"Next Symbol waiting == " + waiting );
	}

	public boolean have( ISymbol next )  {
		if( waiting.equals( next ) ) {
            Diagnostic.trace( DiagnosticLevel.RUN,"<- Have " + next.toString() + " TRUE" );
            nextSymbol();
			return true;
		}
		else {
            Diagnostic.trace( DiagnosticLevel.RUN,"<- Have " + next.toString() + " FALSE" );
			return false;
		}
	}
	
	public void mustBe(ISymbol required)   {
	    Diagnostic.trace( DiagnosticLevel.RUN,"mustbe? " + required.toString() );
		if ( waiting.equals( required ) ){
            recovered = true;
			nextSymbol();
		}
		else if( recovered ) { 
			compilerError.syntaxError( waiting.toString(), required.toString()  );   // lineNumber() , waiting );
            recovered = false;
		} else {
            while( ! waiting.equals( required ) && ! waiting.equals( Symbols.EOT_SYM ) ) {
                nextSymbol();
            }
            if( waiting.equals( required ) ) {
                recovered = true;
                nextSymbol();
            }		        
        }
	}
    
    public void separator() {
        if ( ! have( Symbols.SEMI_SYM ) && ! at_new_line ) {
            if( recovered ){
                compilerError.syntaxError( waiting.toString(), ";" );
            }
            else {
                while ( ! have( Symbols.SEMI_SYM ) && !at_new_line && ! waiting.equals( Symbols.EOT_SYM ) )
                    nextSymbol();
                if( ! waiting.equals( Symbols.EOT_SYM ) )
                    recovered = true;
            }
        }
    }
    
	public int lineNumber(){
		return source.lineNumber();
	}
	
	public boolean composite(String compSym) {
		if ( source.peek() == compSym.charAt( SECOND_CHAR_INDEX ) ) { //assumes 2 character composite symbols
            source.getNextChar();
            return true;
        }
		return false;
	}
    
	public ISymbol tryIdentifier(char firstChar)  {
		StringBuffer buf = new StringBuffer();
		buf.append( firstChar );
		
		while ( Character.isLetterOrDigit( source.peek() ) ){
			buf.append( source.getNextChar() );
		}
		
		String identifier = buf.toString();
		
		ISymbol keyWord = Symbols.getKeywordSymbol( identifier );
		
		if ( keyWord == null ){
			return new IdentifierSymbol( identifier );
		}
		else {
			return keyWord;
		}
	}
	
	/**
	 * Coverts a string to an integer
	 * @param s the string to parse
	 * @param minus whether this number is negative or not
	 * @return The integer representation of the string
	 */
	public int int_conv(String s, boolean minus) {
		if (s == null || s.equals(""))
			return 0;
		if (minus)
			return Integer.parseInt(s.trim()) * -1;
		else
			return Integer.parseInt(s.trim());
	}
	
	/**
	 * Reads a real out of a string
	 * @param s the string to parse
	 * @param the_scale
	 * @return Real number parsed out of the string
	 */
	public double real_conv(String s, int the_scale) {
		double n = 0;
		int no = s.length();
		boolean more = true;
		while (no >= 1 && more)
			if (s.charAt(no - 1) == '0')
				no--;
			else
				more = false;
		for (int i = 1; i <= no; i++)
			n = n * 10 + (s.charAt(i - 1) - ZERO );
		the_scale = the_scale + s.length() - no;
		if (the_scale != 0)
			if (the_scale < 0)
				n = n / ex(-the_scale);
			else
				n = n * ex(the_scale);
		return n;
	}
	
	public double ex(int the_scale) {
		double r = 1;
		double fac = 10;
		while (the_scale != 0) {
			if (the_scale % 2 == 1)
				r = r * fac;
			fac *= fac;
			the_scale = the_scale / 2;
		}
		return r;
	}
	
	/**
	 * returns true is s is less than s1
	 * @param s
	 * @param s1
	 * @return True if s is less than s1
	 */
	public boolean le(String s, String s1) {
		return s.length() < s1.length() || s.length() == s1.length()
				&& s.compareTo(s1) <= 0;
	}
	
	public String intString(char firstDigit) {
		StringBuffer buf= new StringBuffer();
		buf.append( firstDigit );
		while ( Character.isDigit( source.peek() ) ) {
			buf.append( source.getNextChar() );
		}
		return buf.toString();
	}	
	
	public ISymbol unsignedLiteral(char firstDigit){
		String digits = intString( firstDigit );
		int int_val = int_conv( digits, false );
		return new UnsignedIntegerSymbol( int_val );
	}
	
	public ISymbol byteLiteral(char firstDigit) {
		String digits = intString( firstDigit );
		int int_val = int_conv( digits, false );
		if( int_val >= 0 && int_val <= 255 ) {
			return new ByteSymbol( int_val );
		} else {
			compilerError.generalError(  "Byte literal out of range " + int_val + " with digits " + digits);
			return Symbols.BYTE_LITERAL;
		}
	}
	
	public ISymbol numericalLiteral(char firstDigit) {
		boolean sign = true;	// do we have a -
		boolean isreal = false;	// is it a real number
		boolean exp = false;	// does it have an exponent
		
		String ipart = intString( firstDigit );
		String dpart = "";
		if( source.peek() == '.' ) {
			source.getNextChar();
			isreal = true;
			dpart = intString( source.getNextChar() );
		}
		String epart = "";
		if( source.peek() == 'e' ) {
			source.getNextChar();
			exp = true;
			if ( source.peek() == '-' ) {
				source.getNextChar();
				sign = true;
			} else if ( source.peek() == '+' ) {
				source.getNextChar();	// throw it away
			}
			epart = intString( source.getNextChar() );
		}
		if( isreal ) {
			int the_scale = int_conv(epart, sign);
			double real_value = real_conv(ipart, the_scale);	// TODO do something with real numbers later
			if (!dpart.equals(""))
				real_value = real_value
						+ real_conv(dpart, the_scale - dpart.length());
			if (unary_minus)
				real_value = -real_value;
			return new RealSymbol( real_value );
		} else if( unary_minus && le( ipart, minintstr ) || !unary_minus && le( ipart, maxintstr ) ) {
				if( ipart.equals("") ) {
					return new IntegerSymbol( 0 );
				} else {
					return new IntegerSymbol( int_conv( ipart, unary_minus ) );
				}
		}
		else {
			compilerError.generalError(  "Integer literal out of range" );
			return Symbols.REAL_LITERAL;
		}
	}
	
	
	public ISymbol readString() {
		StringBuffer buf = new StringBuffer();
		
		while( source.peek() != STRING_DELIM ){
			buf.append( source.getNextChar() );
		}
		
		source.getNextChar(); // throw this away - it is the String Delimiter
		
		return new StringSymbol( buf.toString() );
	}
}
