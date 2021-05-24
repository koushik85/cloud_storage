/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monitoringsystem;

import dbpack.dbconnection;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import utilities.DES;
import utilities.compression;
import utilities.ips;
import utilities.SendMailExample;
/**
 *
 * @author Lenovo
 */
public class readreq extends Thread
{
    CMS obj;
    Connection con;
    
    readreq(CMS obj)
    {
        super();
        this.obj=obj;
        start();
    }
    
    public void run()
    {
        try
        {
             con=new dbconnection().connect();
           ServerSocket ss=new ServerSocket(2000);
           while (true)
           {
               Socket soc=ss.accept();
               ObjectOutputStream oos=new ObjectOutputStream(soc.getOutputStream());
               ObjectInputStream oin=new ObjectInputStream(soc.getInputStream());
               
               String req=(String)oin.readObject();
               
               if (req.equals("REGISTER"))
               {
                   String uname=(String)oin.readObject();
                   String pass=(String)oin.readObject();
                   String email=(String)oin.readObject();
                   String reply=insertuser(uname,pass,email);
                   
                   oos.writeObject(reply);
               }
               else
               if (req.equals("LOGIN"))
               {
                   String uname=(String)oin.readObject();
                   String pass=(String)oin.readObject();
                   String reply=verifyuser(uname,pass);
                   oos.writeObject(reply);
               }
               else 
               if (req.equals("DELETE"))
               {
                   String uname=(String)oin.readObject();
                   String fname=(String)oin.readObject();
                   
                   
                   PreparedStatement pst2=con.prepareStatement("delete from sharedetails where uname=? and fname=?");
                   pst2.setString(1,uname);
                   pst2.setString(2,fname);
                   pst2.executeUpdate();
                   //oos.writeObject("SUCCESS");
                   
                   PreparedStatement pst=con.prepareStatement("select * from sharedetails where uname!=? and fname=?");
                   pst.setString(1,uname);
                   pst.setString(2,fname);
                   ResultSet rs=pst.executeQuery();
                   
                   if (rs.next())  //file is shared
                   {
                       oos.writeObject("SUCCESS");
                   }
                   else //file not shared
                   {
                       PreparedStatement pst3=con.prepareStatement("select storage from filedetails where fname=?");
                       pst3.setString(1,fname);
                       ResultSet rs3=pst3.executeQuery();
                       
                       rs3.next();
                       
                       String storage=rs3.getString(1).trim();
                       
                       String reply=deletefile(fname,storage);
                       oos.writeObject(reply);
                       
                       if (reply.equals("SUCCESS"))
                       {
                           PreparedStatement pst4=con.prepareStatement("delete from filedetails where fname=?");
                           pst4.setString(1,fname);
                           pst4.executeUpdate();
                           findrank();
                           obj.viewallfiles();
                       }
                           
                   }
               }
               else
               if (req.equals("DOWNLOAD"))
               {
                   String uname=(String)oin.readObject();
                   String fname=(String)oin.readObject();
                   
                   Vector v=new Vector();
                   v.add(uname);
                   v.add(fname);
                   
                   PreparedStatement pst=con.prepareStatement("select * from filedetails where fname=?");
                   pst.setString(1,fname);
                   ResultSet rs=pst.executeQuery();
                   
                   if (rs.next())
                   {
                       byte b[]=getfile(rs.getString("fname").trim(),rs.getString("storage").trim());
                       b=new compression().decompress(b);
                       b=new DES().decrypt(b);
                       
                       PreparedStatement pst1=con.prepareStatement("select email from user where uname=?");
                       pst1.setString(1,uname);
                       ResultSet rs1=pst1.executeQuery();
                       
                       rs1.next();
                       
                       String email=rs1.getString(1);
                       
                       SendMailExample sm=new SendMailExample();
                       String otp=sm.getotp();
                       System.out.println("OTP: "+otp);
                       b=new DES().encrypt(b,otp);
                       sm.main(email, otp);
                       
                       oos.writeObject("SUCCESS");
                       oos.writeObject(b);
                       
                       updatefrequency(fname);
                       findrank();
                       obj.viewallfiles();
                       v.add("SUCCESS");
                       
                   }
                   else
                   {
                       oos.writeObject("FAILURE");
                       v.add("FAILURE");
                   }
                   
                   DefaultTableModel dft=(DefaultTableModel)obj.jTable2.getModel();
                   dft.addRow(v);
               }
               else
               if (req.equals("UPLOAD"))
               {
                   String uname=(String)oin.readObject();
                   String fname=(String)oin.readObject();
                   byte b[]=(byte[])oin.readObject();
                   int len=b.length;
                   Vector vins=new Vector();
                   vins.add(uname);
                   vins.add(fname);
                   vins.add(len);
                   
                   Vector v=checkduplicate(fname,b);
                   System.out.println("size: "+v.size());
                   
                   if (v.get(0).toString().trim().equals("DUPLICATED")) //cancel upload
                   {
                       oos.writeObject("DUPLICATED");
                       oos.writeObject(v.get(1).toString().trim());
                       addshare(uname,v.get(1).toString().trim());
                       updatefrequency(fname);
                       findrank();
                       vins.add("YES");
                       vins.add("UPLOAD CANCELLED");
                   }
                   else  //uppload file to storage node
                   {
                       boolean exist=checkfilename(fname);
                       vins.add("NO");
                       if (exist)
                       {
                           oos.writeObject("FILENAMECLASH");
                           vins.add("FILENAME CLASH");
                       }
                       else
                       {
                           
                       
                       
                       b=new DES().encrypt(b);
                       b=new compression().compress(b,fname);
                       
                       
                       String s1=getmemory(ips.storage1ip,3000);
                       String s2=getmemory(ips.storage2ip,4000);
                       String s3=getmemory(ips.storage3ip,5000);
                       
                       int s1int=Integer.parseInt(s1);
                       int s2int=Integer.parseInt(s2);
                       int s3int=Integer.parseInt(s3);
                       
                       if (b.length<=s1int || b.length<=s2int || b.length<=s3int)
                       {    
                           String reply="";
                           String storage="";
                            if (s1int>=s2int && s1int>=s3int) //upload to storage1
                            {
                                reply=upload(fname,b,len,ips.storage1ip,3000);
                                storage="STORAGE1";
                            }
                            else
                            if (s2int>=s3int) //upload to storage2
                            {
                               reply=upload(fname,b,len,ips.storage2ip,4000);
                               storage="STORAGE2";
                            }
                            else //upload to storage 3
                            {
                                reply=upload(fname,b,len,ips.storage3ip,5000);
                                storage="STORAGE3";
                           
                            }
                       
                            if (reply.equals("SUCCESS"))
                            {
                               updatedb(uname,fname,len,storage);
                               findrank();
                               obj.viewallfiles();
                               vins.add("UPLOAD SUCCESS");
                            }
                           
                            oos.writeObject(reply);
                            oos.writeObject(storage);
                        
                       
                       }
                       else
                       {
                           oos.writeObject("LOWMEMORY");
                           vins.add("NO");
                           vins.add("LOW MEMORY");
                       }
                       
                       
                   }
                   }    
                   DefaultTableModel dft=(DefaultTableModel)obj.jTable1.getModel();
                    dft.addRow(vins);
                       
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
    
    void updatefrequency(String fname)
    {
        try
        {
            PreparedStatement pst=con.prepareStatement("select frequency from filedetails where fname=?");
            pst.setString(1,fname);
            ResultSet rs=pst.executeQuery();
            rs.next();
            
            int f=rs.getInt(1);
            f++;
            
            PreparedStatement pst2=con.prepareStatement("update filedetails set frequency=? where fname=?");
            pst2.setInt(1,f);
            pst2.setString(2,fname);
            pst2.executeUpdate();
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }    
    
    boolean checkfilename(String fname)
    {
        boolean exist=false;
        try
        {
            PreparedStatement pst=con.prepareStatement("select * from filedetails where fname=?");
            pst.setString(1,fname);
            ResultSet rs=pst.executeQuery();
            
            if (rs.next())
            {
                exist=true;
            }
            else
            {
                exist=false;
            }
            
            
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        return exist;
    }        
            
    void findrank()
    {
        try
        {
           PreparedStatement pst=con.prepareStatement("select * from filedetails order by frequency desc");
           ResultSet rs=pst.executeQuery();
           
           int rank=0;
           int oldfeq=-1;
           while (rs.next())
           {
               String fname=rs.getString(1).trim();
               String ftype=rs.getString(2).trim();
               
               int frequency=rs.getInt("frequency");
               if (oldfeq!=frequency)
                   rank++;
               PreparedStatement pst2=con.prepareStatement("update filedetails set rank=? where fname=? and ftype=? ");
               pst2.setInt(1,rank);
               pst2.setString(2,fname);
               pst2.setString(3,ftype);
               pst2.executeUpdate();
               oldfeq=frequency;
           }
           
           updatestorageUI(ips.storage1ip,3000);
           updatestorageUI(ips.storage2ip,4000);
           updatestorageUI(ips.storage3ip,5000);
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    void updatestorageUI(String ip,int port)
    {
        try
        {
            Socket soc=new Socket(ip,port);
            ObjectOutputStream oos=new ObjectOutputStream(soc.getOutputStream());
            ObjectInputStream oin=new ObjectInputStream(soc.getInputStream());
            
            oos.writeObject("UPDATERANK");
            String reply=(String)oin.readObject();
            
            oos.close();
            oin.close();
            soc.close();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
    
    void updatedb(String uname,String fname,int len,String storage)
    {
        try
        {
            PreparedStatement pst=con.prepareStatement("insert into filedetails values(?,?,?,?,?,?,?)");
            pst.setString(1,fname);
            String ext="";
            StringTokenizer st=new StringTokenizer(fname,".");
            while (st.hasMoreTokens())
                ext=st.nextToken();
            
            pst.setString(2,ext);
            pst.setInt(3,len);
            pst.setInt(4,0);
            pst.setInt(5,0);
            pst.setString(6,storage);
            java.util.Date d=new java.util.Date();
            SimpleDateFormat sf=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String df=sf.format(d);
            pst.setString(7,df);
            pst.executeUpdate();
            
            addshare(uname,fname);
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    void addshare(String uname,String fname)
    {
        try
        {
            PreparedStatement pst=con.prepareStatement("insert into sharedetails values(?,?)");
            pst.setString(1,uname);
            pst.setString(2,fname);
            pst.executeUpdate();
            
            
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    
     String deletefile(String fname,String storage)
     {
         String reply="FAILED";
         try
         {
             String ip="";
             int port=0;
             
            if (storage.equals("STORAGE1"))
            {
                ip=ips.storage1ip;
                port=3000;
            }
            else
            if (storage.equals("STORAGE2"))    
            {
                ip=ips.storage2ip;
                port=4000;
            }
            else
            if (storage.equals("STORAGE3"))    
            {
                ip=ips.storage3ip;
                port=5000;
            }
            
            Socket soc=new Socket(ip,port);
            ObjectOutputStream oos=new ObjectOutputStream(soc.getOutputStream());
            ObjectInputStream oin=new ObjectInputStream(soc.getInputStream());
            
            oos.writeObject("DELETEFILE");
            oos.writeObject(fname);
            reply=(String)oin.readObject();
            
            oos.close();
            oin.close();
            soc.close();
            
         }
         catch(Exception e)
         {
             System.out.println(e);
             e.printStackTrace();
         }
         
         return reply;
     }
    
    String upload(String fname,byte[] b,int len,String ip,int port)
    {
        String reply="FAILED";
        try
        {
            Socket soc=new Socket(ip,port);
            ObjectOutputStream oos=new ObjectOutputStream(soc.getOutputStream());
            ObjectInputStream oin=new ObjectInputStream(soc.getInputStream());
            
            oos.writeObject("UPLOAD");
            oos.writeObject(fname);
            oos.writeObject(b);
            reply=(String)oin.readObject();
            
             oos.close();
             oin.close();
             soc.close();
             
            
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        return reply;
    }
    
    String getmemory(String ip,int port)
    {
        String m="";
        try
        {
            Socket soc=new Socket(ip,port);
            ObjectOutputStream oos=new ObjectOutputStream(soc.getOutputStream());
            ObjectInputStream oin=new ObjectInputStream(soc.getInputStream());
            
            oos.writeObject("MEMORY");
             m=(String)oin.readObject();
            
             oos.close();
             oin.close();
             soc.close();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        
        return m;
            
    }
    
    Vector checkduplicate(String fname,byte[] b)
    {
        Vector v=new Vector();
        System.out.println("in checkduplicate..");
        try
        {
            int size=b.length;
            String ext="";
            StringTokenizer st=new StringTokenizer(fname,".");
            while (st.hasMoreTokens())
                ext=st.nextToken();
            
            
            PreparedStatement pst=con.prepareStatement("select * from filedetails where ftype=? and fsize=?");
            pst.setString(1,ext);
            pst.setInt(2,size);
            ResultSet rs=pst.executeQuery();
            
            
           boolean exist=false;
            while (rs.next())
            {
                exist=true;
                System.out.println("in while..");
                String dfile=rs.getString("fname");
                byte b1[]=getfile(dfile,rs.getString("storage"));
                
                
                b1=new compression().decompress(b1);
                b1=new DES().decrypt(b1);
                
                String result=checkduplicate(b,b1);
                
                if (result.equals("YES"))
                {
                    v.add("DUPLICATED");
                    v.add(dfile);
                    break;
                }
                
                
                
                
            }
            if (exist==false)
                System.out.println("No files..");
            System.out.println("after while..");
            if (v.size()==0)
            {
                v.add("NODUPLICATE");
            }
            System.out.println("CHECK DUPLICATE: "+v.get(0).toString().trim());    
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
            
        }
        return v;
    }
    
    String checkduplicate(byte b[],byte b1[])
    {
        String result="YES";
        try
        {
            if (b.length<100)
            {
                for (int i=0;i<b.length;i++)
                {
                    if (b[i]!=b1[i])
                    {
                        result="NO";
                        break;
                    }
                }
            }
            else
            {
               for (int i=0;i<50;i++) //check first 50 bytes
               {
                   if (b[i]!=b1[i])
                   {
                       result="NO";
                       break;
                   }
               }
               
               if (result.equals("YES"))  //first 50 bytes matched
               {
                    for (int i=b.length-50;i<b.length;i++) //check last 50 bytes
                    {
                        if (b[i]!=b1[i])
                        {
                            result="NO";
                            break;
                        }
                    }
                   
               }
            }
              
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        
        return result;
    }
    
    
    byte[] getfile(String fname,String storage)
    {
        byte b[]=null;
        try
        {
            String ip="";
            int port=3000;
            if (storage.equals("STORAGE1"))
            {
                ip=ips.storage1ip;
                port=3000;
            }   
            else
            if (storage.equals("STORAGE2"))
            {
                ip=ips.storage2ip;  
                port=4000;
            }   
            else
            if (storage.equals("STORAGE3"))
            {
                ip=ips.storage3ip;    
                    port=5000;
            }
                
            
            Socket soc=new Socket(ip,port);
            ObjectOutputStream oos=new ObjectOutputStream(soc.getOutputStream());
            ObjectInputStream oin=new ObjectInputStream(soc.getInputStream());
            oos.writeObject("GETFILE");
            oos.writeObject(fname);
            b=(byte[])oin.readObject();
            
            oos.close();
            oin.close();
            soc.close();
            
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
        
        return b;
    }
        
    
    String insertuser(String uname,String pass,String email)
    {
        String reply="FAILED";
        try
        {
            PreparedStatement pst=con.prepareStatement("select * from user where uname=?");
            pst.setString(1,uname);
            ResultSet rs=pst.executeQuery();
            if (rs.next())
            {
                reply="AVAILABLE";
            }
            else
            {
                PreparedStatement pst1=con.prepareStatement("insert into user values(?,?,?)");
                pst1.setString(1,uname);
                pst1.setString(2,pass);
                pst1.setString(3,email);
                pst1.executeUpdate();
                reply="SUCCESS";
                Vector v=new Vector();
                v.add(uname);
                v.add(pass);
                DefaultTableModel dft=(DefaultTableModel)obj.jTable3.getModel();
                dft.addRow(v);
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        
        return reply;
    }
    
    String verifyuser(String uname,String pass)
    {
        String reply="FAILED";
        try
        {
            PreparedStatement pst=con.prepareStatement("select * from user where uname=? and pass=?");
            pst.setString(1,uname);
            pst.setString(2,pass);
            ResultSet rs=pst.executeQuery();
            if (rs.next())
            {
                reply="SUCCESS";
            }
            
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        
        return reply;
    }
    
    
}
