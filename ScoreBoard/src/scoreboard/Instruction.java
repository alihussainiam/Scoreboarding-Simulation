/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scoreboard;

import java.util.Vector;

public class Instruction {
    

        Instruction()
        {
            operations_clockcycles = new Vector<Integer>();
            sourceReg = new Vector<String>();
            for(int i=0; i<4; i++) operations_clockcycles.add(-1);
        }
            
        public Vector<Integer> operations_clockcycles; // Storing Clock Cyclcles of Each Phase for particular Instruction
        public String type;
        public String destinationReg;
        public Vector<String> sourceReg; 
        public String functionalUnit;

        public int nextOperationToPerform()
        {
            for(int i=0; i<4; i++)
                if(operations_clockcycles.elementAt(i) == -1)
                    return i;
            
            return -1;
        }
        
    @Override
    public String toString() {
        String str = "";
        str += type + " " + destinationReg + " ";
        for(int i=0; i<sourceReg.size(); i++)
            str += sourceReg.elementAt(i) + " ";

      
        return str;
    }
        
        
        
}
