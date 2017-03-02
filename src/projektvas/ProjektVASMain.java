/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projektvas;

import java.text.ParseException;
import java.util.Scanner;

/**
 *
 * @author Mariofil
 */
public class ProjektVASMain {

    public static void main(String[] args) throws ParseException {
        System.out.println("Enter starting point: ");
        Scanner scan = new Scanner(System.in);
        
        String startPoint = scan.nextLine();
        System.out.println("Enter destination point: ");
        
        String destPoint = scan.nextLine();
        System.out.println("Please enter starting date in format: YYYY-MM-DD");
        
        String startDate = scan.nextLine();
        System.out.println("Please enter ending date in format: YYYY-MM-DD");
        String endDate = scan.nextLine();
        
        TripData td = new TripData();
        td.setStartPoint(startPoint);
        td.setDestinationPoint(destPoint);
        td.setStartDate(startDate);
        td.setEndDate(endDate);
        
        AgentVAS vas = new AgentVAS();
        vas.setup(td);
        vas.run();

    }

}
