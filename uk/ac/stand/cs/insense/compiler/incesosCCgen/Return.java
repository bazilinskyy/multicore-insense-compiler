package uk.ac.stand.cs.insense.compiler.incesosCCgen;

import java.util.Map;

import uk.ac.stand.cs.insense.compiler.cgen.IDecl;
import uk.ac.stand.cs.insense.compiler.cgen.IDeclarationContainer;
import uk.ac.stand.cs.insense.compiler.cgen.IFunction;
import uk.ac.stand.cs.insense.compiler.cgen.IReturn;
import uk.ac.stand.cs.insense.compiler.cgen.ISequence;
import uk.ac.stand.cs.insense.compiler.interfaces.ISymbolTable;
import uk.ac.stand.cs.insense.compiler.symbols.STEntry;
import uk.ac.stand.cs.insense.compiler.types.ArrayType;
import uk.ac.stand.cs.insense.compiler.types.BooleanType;
import uk.ac.stand.cs.insense.compiler.types.ByteType;
import uk.ac.stand.cs.insense.compiler.types.ChannelType;
import uk.ac.stand.cs.insense.compiler.types.EnumType;
import uk.ac.stand.cs.insense.compiler.types.ITypeRep;
import uk.ac.stand.cs.insense.compiler.types.IntegerType;
import uk.ac.stand.cs.insense.compiler.types.RealType;
import uk.ac.stand.cs.insense.compiler.types.StructType;
import uk.ac.stand.cs.insense.compiler.types.VoidType;

public class Return extends Code implements IReturn {

	IFunction proc;
	ITypeRep proc_return_type;
	boolean proc_returns_result;
	
	ITypeRep return_type;
	

	public Return(IFunction fromProc){
		this.proc = fromProc;
		fromProc.setContainsReturnStatement(true);
		proc_return_type = proc.getFt().getResult();
		proc_returns_result = !proc_return_type.equals(VoidType.TYPE);
		super.append(TAB + "// START return statement" + NEWLINE);
//		super.append(NEWLINE + TAB + RETURN_);
		if(proc_returns_result){
//			super.append(TAB + proc.getCReturnParamName() + SPACE + EQUALS_);
			if(proc_return_type.isPointerType())
				super.append(TAB + "DAL_assign" + LRB + AMPERSAND + proc.getCReturnParamName() + COMMA + SPACE);
			else
				super.append(TAB + proc.getCReturnParamName() + SPACE + EQUALS_ );
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.ISend#append(java.lang.String)
	 */
	public void append( String s ) {
		super.append(s);
	}

	
	public String generateFunctionDeclDecRefs(){
		StringBuffer sb = new StringBuffer();
		Function f = Cgen.get_instance().findEnclosingFunctionContainer();
		if(f != null){
			if(f.getLocalDeclsInScope().size() == 0){
				sb.append(TAB + "// no local decls requiring GC prior to jump" + NEWLINE);
			} else {
				sb.append(TAB + "// garbage collect uncollected local decls that are in scope prior to jump" + NEWLINE);			
				for(IDecl l : f.getLocalDeclsInScope()){
					if(l.getType().isPointerType()){
						sb.append(TAB + functionCall("DAL_decRef", l.getBaseName()) + SEMI + NEWLINE);
					}
				}
			}
		} else {
			sb.append(TAB + "// no function container found when searching for decls requiring GC prior to jump" + NEWLINE);
		}
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.stand.cs.insense.compiler.Ccgen.IReturn#complete()
	 */
	public void complete() {
		if(proc_return_type.isPointerType()){
			super.append(RRB);
		}
		super.append(SEMI + NEWLINE);
		// garbage collect any uncollected local decls that are in scope
		super.append(generateFunctionDeclDecRefs()); 
		// output the goto instruction to jump to the end of the procedure 
		super.append(TAB + "// jump to end of procedure" + NEWLINE);
		super.append(TAB + Code.GOTO_ + proc.getEnd_proc_label() + SEMI + NEWLINE);
		super.append(TAB + "// END return statement" + NEWLINE);

	}
	
	public IFunction getProc() {
		return proc;
	}

	public boolean proc_returns_result() {
		return proc_returns_result;
	}

	public ITypeRep getProcReturnType() {
		return proc_return_type;
	}


}
