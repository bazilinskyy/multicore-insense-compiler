package uk.ac.stand.cs.insense.compiler.interfaces;


/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:ajm@dcs.st-and.ac.uk"> Andrew J. McCarthy </a>
 */
public interface ILexicalAnalyser {
    public boolean atNewLine();
    public ISymbol current();
	public void nextSymbol();
	public boolean have(ISymbol next);
	public void mustBe(ISymbol mustBe);
    public void separator();
	public int lineNumber();
	public void setUnaryMinus( boolean truefalse );
	public String currentLine();
}
