/*
 * Created on 02-Jul-2006 at 21:43:11.
 */
package uk.ac.stand.cs.insense.compiler.types;

import java.util.LinkedList;
import java.util.List;

/**
 * DIAS Project
 * Insense
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:al@cs.st-andrews.ac.uk"> Alan Dearle </a>
 * @author <a href="mailto:jonl@cs.st-andrews.ac.uk"> Jon Lewis </a>
 */
public class ComponentType extends 	ConstructedType {    
    private List<FunctionType> constructors = new LinkedList<FunctionType>();
    private InterfaceType it;
    private String name;
    
	public static final ComponentType TYPE = new ComponentType();

    
    public static int NOT_FOUND = -1;
    
    public ComponentType() {
        super();
    }
  
    public ComponentType( String name ) {
        this();
        this.name = name;
    }
    
    public void addConstuctor( FunctionType constr ) {
    	// TODO should check for type uniqueness of constructors a la Java 
        constr.setConstructor();
        constructors.add( constr );
    }
    
    public void addInterface( InterfaceType it ) {
        this.it = it;
    }
    
    public InterfaceType getInterface() {
        return it;
    }
    
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
     * @param ft the type of the consructor being searched for
     * @return the constructor index, they are indexed from zero in order of declaration
     * @return NOT_FOUND (-1) if not found
     * @return index the constructor index if the constructor was found
     */
    public int match_constructor(FunctionType ft) {
        // iterate through the constructors trying to find a match
    	int count = 0;
        for( FunctionType cons : constructors ) {
            if( cons.equals( ft ) ) {
                return count;
            }
            count++;
        }
        return NOT_FOUND;
    }
    
    @Override
    public boolean equals( ITypeRep other ) {
        return other instanceof ComponentType ; 
    }
  
	public boolean isPointerType() {
		return true;
	}
	
    public String toHumanReadableString() {
        String consString = "";
        for( FunctionType cons : constructors ) {
            consString += cons.toHumanReadableString() + "\n";
        }
        return "component exports " + it.toString() + "{\n" + consString + "}";
    }
    
    public String toStringRep() {
        String consString = "";
        for( FunctionType cons : constructors ) {
            consString += cons.toStringRep() + DELIMITER;
        }
        return COMPONENT_REP + it.toString() + consString + DELIMITER;
   	
    }

	public String getDefaultCValue() {
		return "NULL";
	}

	public int getCValueSizeIndicator() {
		return RELATIVE_POINTER_SIZE;
	}

    

}
