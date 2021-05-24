/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

/**
 *
 * @author Lenovo
 */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.zip.*;

public class compression 
{
    public byte[] compress(byte[] data,String fname)
    {
       try
       {
            ByteArrayOutputStream arrayOutputStream= new ByteArrayOutputStream();
            ZipOutputStream zOut= new ZipOutputStream(arrayOutputStream);
            ZipEntry entry= new ZipEntry(fname);
            zOut.setLevel(5);
            zOut.putNextEntry(entry);
            zOut.write(data, 0, data.length);
            zOut.closeEntry();
            zOut.finish();
            zOut.close();
            
            data= arrayOutputStream.toByteArray();
       }
       catch(Exception e)
       {
           System.out.println(e);
       }
       
       return data;
    }
    
    public byte[] decompress(byte[] data)
    {
       try
       {
            ByteArrayOutputStream by= new ByteArrayOutputStream();
            DataOutputStream out= new DataOutputStream(by);

            ZipInputStream zipIn= new ZipInputStream(new ByteArrayInputStream(data));
            ZipEntry entry= zipIn.getNextEntry();
            File dataFile= new File(entry.getName());

            byte[] byteArrayIn= new byte[1024];
            int tempInt=0;
            while((tempInt= zipIn.read(byteArrayIn, 0, 1024))!= -1)
            out.write(byteArrayIn, 0, tempInt);

            zipIn.close();
            out.close();
            data= by.toByteArray();
       }
       catch(Exception e)
       {
           System.out.println(e);
       }
       
       return data;
    }
    
    
}
