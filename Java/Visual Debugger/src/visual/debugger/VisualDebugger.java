/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visual.debugger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 *
 * @author Yen
 */
public class VisualDebugger {

public static void main (String [] args) throws Exception {
    HashMap<String, AddressItem> addMap = new HashMap<>();
    HashMap<Integer, Integer> lineNo = new HashMap<>();
    //call the ExtractAddress() function.
    int line = 0;
    int lineAssembly = 0;
    int lineC = 0;
    String add = null;
    
    ExtractAddress.ExtractAddress("acia.map",addMap);
    
    //String to let hex string have 8 characters with 0 in front
    String ZEROES = "00000000";
    FileReader in = null;
    String  thisLine = null;
    Display display = new Display ();
    Shell shell = new Shell (display);
    shell.setLayout(new GridLayout());
    Table table = new Table (shell, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
    table.setLinesVisible (false);
    table.setHeaderVisible (false);
    GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
    data.heightHint = 200;

    //pattern will detect {integers, " ", integers, " ", heximal, " ", Assembly code, " ", variable.}
    //which is the line with details that needed by debugger to display.
    String pattern = "(\\d+)(\\s+)([0-9a-fA-F]+)(\\s+)([0-9a-fA-F]+)(\\s+)(\\w+)(\\s)(\\S+)";
    //patterm will find the line that shows the location on the c file.
    String pattern1 = "(\\d+)(\\s+);(\\s+)(\\d+)(.+)";
    //this pattern use to find the assembly address (find the word switch and get the address name.)
    String addPattern = "(\\d+)(\\s+)switch(\\s+)(\\S+)";
    Pattern r = Pattern.compile(pattern);
    Pattern r1 = Pattern.compile(pattern1);//get the C code location to link to assembly code location
    Pattern r2 = Pattern.compile(addPattern);//get assembly address.

    table.setLayoutData(data);

    String[] titles = {"Column 0", "Column 1", "Column 2", "Column 3"};
    for (int i=0; i<titles.length; i++) {
            TableColumn column = new TableColumn (table, SWT.NONE);
            column.setText (titles [i]);
    }
    //read details from the acia.ls file.
    try{
       // open input stream test.txt for reading purpose.
       in = new FileReader("acia.ls");
       BufferedReader br = new BufferedReader(in);
       while ((thisLine = br.readLine()) != null) {
           Matcher m = r.matcher(thisLine);
           Matcher m1 = r1.matcher(thisLine);
           Matcher m2 = r2.matcher(thisLine);
           //System.out.println(thisLine);
           if(m2.find()){
                AddressItem myStr = addMap.get("acia"+ m2.group(4));
                //System.out.println(m2.group(1)+ "\t" + m2.group(4) + "\t" + myStr.length + "\t" + myStr.addr);
                line = Integer.parseInt(m2.group(1));
                add = myStr.addr;
           }
           if(m1.find()){
               //print the location of .c file.
               lineAssembly = Integer.parseInt(m1.group(1));
               lineC = Integer.parseInt(m1.group(4));
               //System.out.println(lineAssembly+"\t"+lineC);
               lineNo.put(lineC,lineAssembly);
               //System.out.println(lineNo.get(lineC));
           }
           if(m.find()){
                //System.out.println(m.group(3));
                int Val = 0;
                String[] part = m.group().split("(\\s+)");
                TableItem item = new TableItem (table, SWT.NONE);
                GetAddress.GetAddress(thisLine);
                int loc = Integer.parseInt(part[0]);
                //example:
                // in acia.text, starting address is 808a
                //          2084                    switch  .text
                //          2087    0000    88      push    a
                //loc = 2087, line = 2084,
                //     so the real address should be 0000 + 808a = 808a
                if (loc>line)
                    Val = Integer.parseInt(part[1], 16)+Integer.parseInt(add,16);
                
                //System.out.println(part[0]);
                //System.out.print(Val + "  ");
                //System.out.println(Long.toHexString(Val));
                String address = Integer.toHexString(Val);
                //System.out.println(address);
                part[1] = address.length() <= 10 ? ZEROES.substring(address.length()) + address : address;
                for (int i = 1; i<5;i++)
                    item.setText (i-1, part[i]);
                //System.out.println("");
           }
       }       
    }catch(Exception e){
       e.printStackTrace();
    }
    for (int i=0; i<titles.length; i++) {
            table.getColumn (i).pack ();
    }
    shell.pack ();
    shell.open ();
    while (!shell.isDisposed ()) {
            if (!display.readAndDispatch ()) display.sleep ();
    }
    display.dispose ();
}
} 
