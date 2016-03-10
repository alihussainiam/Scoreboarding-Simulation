/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scoreboard;

public class functionalUnit {

    public functionalUnit() {
        busy = false;
    }
    
    public functionalUnit(String name)
    {
        this.name = name;
        busy = false;
    }
    
    public String getName()
    {
        return name;
    }
    
    
    public String name;
    public Boolean busy;
    public String Fi;
    public String Fj;
    public String Fk;
    public String Qj;
    public String Qk;
    public String Rj;
    public String Rk;
    public String op;
    
    public void release()
    {
        busy = false;
        op="";
        Fi="";
        Fj="";
        Fk="";
        Qj="";
        Qk="";
        Rj="";
        Rk="";
    }
}
