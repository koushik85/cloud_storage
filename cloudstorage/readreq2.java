/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudstorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Lenovo
 */
public class readreq2 extends Thread
{
    storage2 obj;
    
    readreq2(storage2 obj)
    {
        super();
        this.obj=obj;
        start();
    }
    
    public void run()
    {
        try
        {
            ServerSocket ss=new ServerSocket(4000);
            
            while (true)
            {
                Socket soc=ss.accept();
                ObjectOutputStream oos=new ObjectOutputStream(soc.getOutputStream());
                ObjectInputStream oin=new ObjectInputStream(soc.getInputStream());
                
                String req=(String)oin.readObject();
                
                if (req.equals("MEMORY"))
                {
                    oos.writeObject(obj.jTextField3.getText().trim());
                }
                else
                if (req.equals("UPDATERANK"))
                {
                    obj.viewfiles();
                    oos.writeObject("SUCCESS");
                }
                else
                if (req.equals("GETFILE"))
                {
                    String fname=(String)oin.readObject();
                    FileInputStream fin=new FileInputStream("STORAGE2/"+fname);
                    byte b[]=new byte[fin.available()];
                    fin.read(b);
                    fin.close();
                    oos.writeObject(b);
                    obj.viewfiles();
                }
                else
                if (req.equals("UPLOAD"))
                {
                    String fname=(String)oin.readObject();
                    byte b[]=(byte[])oin.readObject();
                    File f=new File("STORAGE2");
                    if (!f.exists())
                        f.mkdir();
                    FileOutputStream fout=new FileOutputStream("STORAGE2/"+fname);
                    fout.write(b);
                    fout.close();
                    obj.updatememory();
                    oos.writeObject("SUCCESS");
                    obj.viewfiles();
                }
                else
                if (req.equals("DELETEFILE"))
                {
                    String fname=(String)oin.readObject();
                    
                    File f=new File("STORAGE2/"+fname);
                    if (f.exists())
                    {
                        f.delete();
                        
                        oos.writeObject("SUCCESS");
                        obj.updatememory();
                        obj.viewfiles();
                    }
                    else
                    {
                        oos.writeObject("FAILED");
                    }
                }
                    
                
                oos.close();
                oin.close();
                soc.close();
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }
}
