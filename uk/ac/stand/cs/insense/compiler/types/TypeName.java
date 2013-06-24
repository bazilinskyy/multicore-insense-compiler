/*
 * Created on 10 Aug 2006 at 15:25:03.
 */
package uk.ac.stand.cs.insense.compiler.types;

/**
 * DIAS Project
 * Essence
 * 
 * http://www-systems.dcs.st-and.ac.uk/dias
 *
 * University of St Andrews 2006
 * @author <a href="mailto:al@dcs.st-and.ac.uk"> AAlan Dearle </a>
 */

public class TypeName implements Comparable{
    public String name;
    public ChannelType type;
    
    public TypeName( String name, ChannelType type ) {
        this.name = name;
        this.type = type;
    }
    
    public boolean equals( TypeName other ) {
        return name.equals( other.name ) && type.equals( other.type );
    }

	public int compareTo(Object other) {
		return this.name.compareTo(((TypeName) other).name);
	}
}
