/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

/**
 *
 * @author Lenovo
 */
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
  
class GFG {
  
    // Function to print difference in
    // time start_date and end_date
    String findDifference(String start_date,
                   String end_date)
    {
  
        // SimpleDateFormat converts the
        // string format to date object
        SimpleDateFormat sdf
            = new SimpleDateFormat(
                "dd-MM-yyyy HH:mm:ss");
        String diff="";
        // Try Block
        try {
  
            // parse method is used to parse
            // the text from a string to
            // produce the date
            Date d1 = sdf.parse(start_date);
            Date d2 = sdf.parse(end_date);
  
            // Calucalte time difference
            // in milliseconds
            long difference_In_Time
                = d2.getTime() - d1.getTime();
            // Calucalte time difference in
            // seconds, minutes, hours, years,
  
            // and days
            long difference_In_Seconds
                = (difference_In_Time
                   / 1000)
                  % 60;
  
            long difference_In_Minutes
                = (difference_In_Time
                   / (1000 * 60))
                  % 60;
  
            long difference_In_Hours
                = (difference_In_Time
                   / (1000 * 60 * 60))
                  % 24;
  
            long difference_In_Years
                = (difference_In_Time
                   / (1000l * 60 * 60 * 24 * 365));
  
            long difference_In_Days
                = (difference_In_Time
                   / (1000 * 60 * 60 * 24))
                  % 365;
  
            // Print the date difference in
            // years, in days, in hours, in
            // minutes, and in seconds
  
           /* System.out.print(
                "Difference "
                + "between two dates is: ");
  
            System.out.println(
                difference_In_Years
                + " years, "
                + difference_In_Days
                + " days, "
                + difference_In_Hours
                + " hours, "
                + difference_In_Minutes
                + " minutes, "
                + difference_In_Seconds
                + " seconds");*/
            
             diff=difference_In_Years
                + " years, "
                + difference_In_Days
                + " days ";
               
                
        }
  
        // Catch the Exception
        catch (ParseException e) {
            e.printStackTrace();
        }
        
        return diff;
    }
  
    // Driver Code
    /*
    public static void main(String[] args)
    {
        // Given start Date
        String start_date
            = "10-01-2018 01:10:20";
  
        // Given end Date
        String end_date
            = "10-06-2020 06:30:50";
        
        Date d=new Date();
        SimpleDateFormat sf=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String end=sf.format(d);
        System.out.println(end);
        // Function Call
        new GFG().findDifference(start_date, end);
    }
    */
}
