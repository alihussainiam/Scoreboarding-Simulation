/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scoreboard;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ScoreBoardGernator {
    
    public void Generate(Vector<Instruction> allInstructions, Vector<functionalUnit> functionalUnitStatus, HashMap<String, String> registerStatus,
            HashMap<String, Integer> timeOfEachInstruction, int maxClockCycle, Scanner insStream)
    {        
                
        loadInstructionfromFile(allInstructions, registerStatus, insStream);
        timeOfEachInstruction.put("load", 1);
        timeOfEachInstruction.put("add", 2);
        timeOfEachInstruction.put("sub", 2);
        timeOfEachInstruction.put("mul", 10);
        timeOfEachInstruction.put("div", 40);
        
        

        Instruction executedInstruction=null;
        
        Instruction readInstruction=null;
        Vector  Read=new Vector();
        int instructions_completed=0;
       
        /*Score Board Computing  starts from here*/
        for(int currClockCycle=1; currClockCycle<=maxClockCycle; currClockCycle++)
        {
            Vector<Boolean> operationAvailablity = new Vector<Boolean>();
            for(int i=0; i<4; i++) 
                operationAvailablity.add(true);
                        
            for(int insNo=0; insNo<allInstructions.size(); insNo++)
            {
                Instruction currentIns = allInstructions.elementAt(insNo);
                for(int opNo=0; opNo<4; opNo++)
                {
                    if(operationAvailablity.elementAt(opNo) && currentIns.nextOperationToPerform() == opNo)
                    {
                        switch(opNo)
                        {
                            case 0:     // Issue
                                if(IssuePossible(allInstructions, insNo, functionalUnitStatus))
                                {
                                    currentIns.operations_clockcycles.set(opNo, currClockCycle); // Setting  on which clock cycle issue was performed
                                    
                                    operationAvailablity.set(opNo, false);
                                    opNo=4; // exiting the loop by making opno=4
                                    
                                    functionalUnit fu = getFunctionalUnit(currentIns.type, functionalUnitStatus); 
                                    
                                    fu.busy = true;
                                    fu.op=currentIns.type;
                                    fu.Fi = currentIns.destinationReg; 
                                    fu.Fj = currentIns.sourceReg.elementAt(0); 
                                    
                                    if(!currentIns.type.equals("load")) 
                                        fu.Fk = currentIns.sourceReg.elementAt(1);
                                    
                                    currentIns.functionalUnit = fu.name;
                                    
                                    registerStatus.put(currentIns.destinationReg, currentIns.functionalUnit);
                                }
                            break;
                            case 1:     // Read  Operand
                                if(operandPossible(allInstructions, insNo,functionalUnitStatus))
                                {
                                    opNo=4;                                   
                                    Read.add(insNo);
                                }
                            break;
                            case 2:     // Execute
                                if(currClockCycle - currentIns.operations_clockcycles.elementAt(1) >= timeOfEachInstruction.get(currentIns.type))
                                {
                                    currentIns.operations_clockcycles.set(opNo, currClockCycle);
                                    //operationAvailablity.set(opNo, false);
                                    opNo=4;                                    
                                }
                            break;
                            case 3:     // Write
                                if(writePossible(allInstructions, insNo))
                                {
                                    //operationAvailablity.set(opNo, false); 
                                    opNo=4;   
                                    executedInstruction = currentIns;
                                }
                            break;
                        }
                    }
                }
            }
            if(executedInstruction != null)                                      
            {
                if(currClockCycle!=maxClockCycle){
                executedInstruction.operations_clockcycles.set(3, currClockCycle);
                registerStatus.put(executedInstruction.destinationReg, "");
                for(int i=0; i<functionalUnitStatus.size() && executedInstruction!=null; i++)
                {
                    functionalUnit fu = functionalUnitStatus.elementAt(i);
                    if(fu.name.equals(executedInstruction.functionalUnit))
                    {
                        fu.release();
                        executedInstruction = null;
                    }
                }
                instructions_completed++;
                if(instructions_completed == allInstructions.size()) break;
            }
            }
            if(!Read.isEmpty())
            {
               for(int p=0;p<Read.size();p++)               
                {
                    readInstruction=allInstructions.elementAt((int) Read.elementAt(p));
                    readInstruction.operations_clockcycles.set(1, currClockCycle);
                }
            Read.clear();
            }
        }                
    }
    
        public void loadInstructionfromFile(Vector<Instruction> allInstructions, HashMap<String, String> registerStatus, Scanner insStream) {
            while(insStream.hasNext())
            {
                Instruction tmp = new Instruction();
                tmp.type = insStream.next();
                tmp.destinationReg = insStream.next();
                if(!registerStatus.containsKey(tmp.destinationReg)) registerStatus.put(tmp.destinationReg, "");
                
                int srcreg=2;
                if(tmp.type.compareTo("load") == 0) srcreg = 1;
                for(int i=0; i<srcreg; i++)
                {
                    tmp.sourceReg.add(insStream.next());
                    if(!registerStatus.containsKey(tmp.sourceReg.elementAt(i))) 
                        registerStatus.put(tmp.sourceReg.elementAt(i), "");
                }
                allInstructions.add(tmp);
            }        
    }

    private functionalUnit getFunctionalUnit(String type, Vector<functionalUnit> functionalUnitStatus) {

        String fuPrefix = "";
        
        switch(type)
        {
            case "load": fuPrefix = "integer"; break;
            case "add": fuPrefix = "adder"; break;
            case "sub":  fuPrefix = "adder"; break;
            case "mul":  fuPrefix = "mul"; break;
            case "div":  fuPrefix = "div"; break;
        }
        
        for(int i=0; i<functionalUnitStatus.size(); i++)
            if(functionalUnitStatus.elementAt(i).name.startsWith(fuPrefix) && !functionalUnitStatus.elementAt(i).busy)
                return functionalUnitStatus.elementAt(i);
        
        return null;
    }

    private boolean IssuePossible(Vector<Instruction> allInstructions, int insNo, Vector<functionalUnit> functionalUnitStatus) {
        Boolean bool=true;
        Instruction currentIns = allInstructions.elementAt(insNo);

        if(getFunctionalUnit(currentIns.type, functionalUnitStatus) == null) 
            bool=false;
        for(int prevIns=0; prevIns<insNo; prevIns++)
            if(allInstructions.elementAt(prevIns).destinationReg.equals(currentIns.destinationReg) 
                    && allInstructions.elementAt(prevIns).operations_clockcycles.elementAt(3) == -1)
                        bool = false;
        
        
        for(int srcno=0; srcno<currentIns.sourceReg.size(); srcno++)
            for(int prevIns=0; prevIns<insNo; prevIns++)
                if(allInstructions.elementAt(prevIns).destinationReg.equals(currentIns.sourceReg.elementAt(srcno)) 
                        && allInstructions.elementAt(prevIns).operations_clockcycles.elementAt(0) == -1)
                            bool = false;
        
        return bool;
    }


    private boolean operandPossible(Vector<Instruction> allInstructions, int insNo, Vector<functionalUnit> functionalUnitStatus) {
        Instruction ins = allInstructions.elementAt(insNo);
        boolean available=true;
        boolean check=true;
        functionalUnit current=getUnit(ins,functionalUnitStatus);
        for(int srcno=0; srcno<ins.sourceReg.size(); srcno++)
        {
            check=true;
            for(int prevIns=0; prevIns<insNo && check; prevIns++)
            {
                if(allInstructions.elementAt(prevIns).destinationReg.equals(ins.sourceReg.elementAt(srcno)) //checking source operands available 
                        && allInstructions.elementAt(prevIns).operations_clockcycles.elementAt(3) == -1)
                {
                        check=false;
                        
                        available=false;
                        
                        functionalUnit temp=getUnit(allInstructions.elementAt(prevIns),functionalUnitStatus);    
                        
                        if(srcno==0)
                        {
                            current.Qj=temp.getName();
                            current.Rj="No";
                        }
                        else
                        {
                            current.Qk=temp.getName();
                            current.Rk="No";
                        }
                }
            else
                {
                    if(srcno==0)
                        {
                            current.Qj="";
                            current.Rj="Yes";
                        }
                        else
                        {
                            current.Qk="";
                            current.Rk="Yes";
                        }
                }
            }
        }
        if(available==true)
        {            
            return true;
        }
            else
        {            
            return false;
        }
    }

    private functionalUnit getUnit( Instruction ins ,Vector<functionalUnit> functionalUnitStatus)
        {
            for(int i=0; i<functionalUnitStatus.size() && ins!=null; i++)
                            {
                            if(functionalUnitStatus.elementAt(i).name.equals(ins.functionalUnit))
                            {
                                return functionalUnitStatus.elementAt(i);
                            }
                            }
            return null;
}
    private boolean writePossible(Vector<Instruction> allInstructions, int insNo) {
        Instruction currentIns = allInstructions.elementAt(insNo);
        
        for(int prevIns=0; prevIns<insNo; prevIns++)
        {
            Instruction prevInstruction = allInstructions.elementAt(prevIns);
            for(int srcNo=0; srcNo<prevInstruction.sourceReg.size(); srcNo++)
                if(currentIns.destinationReg.equals(prevInstruction.sourceReg.elementAt(srcNo)) && prevInstruction.operations_clockcycles.elementAt(1) == -1)
                    return false;
        }
        return true;
    }
}

