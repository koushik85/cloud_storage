package utilities;

import javax.crypto.*;
import javax.crypto.spec.*;
import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;
import java.security.*;

public class DES
{
    public boolean status=true;
    String key="12345678";
	public DES()
	{
		
	}
	
	public byte[] encrypt(byte[] msg)
	{
		
		try
		{
			
			
			//if (key.length()==8)
			{
			
				Cipher cipher= Cipher.getInstance("DES");
				SecretKeySpec spec= new SecretKeySpec(key.getBytes(), "DES");
				cipher.init(Cipher.ENCRYPT_MODE, spec);
				//byte messageArray[]=msg.getBytes("UTF8");
				msg= cipher.doFinal(msg,0,msg.length);
				//msg=new BASE64Encoder().encode(messageArray);
                                
                                
				
			}
			
			
			
			
			
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		return msg;
	}
        
        public byte[] encrypt(byte[] msg,String otp)
	{
		
		try
		{
			
			
			//if (key.length()==8)
			{
			
				Cipher cipher= Cipher.getInstance("DES");
				SecretKeySpec spec= new SecretKeySpec(otp.getBytes(), "DES");
				cipher.init(Cipher.ENCRYPT_MODE, spec);
				//byte messageArray[]=msg.getBytes("UTF8");
				msg= cipher.doFinal(msg,0,msg.length);
				//msg=new BASE64Encoder().encode(messageArray);
                                
                                
				
			}
			
			
			
			
			
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		return msg;
	}
	
	public byte[] decrypt(byte[] msg)
	{
			
		
		
		try
		{
			
			Cipher cipher= Cipher.getInstance("DES");
			SecretKeySpec spec= new SecretKeySpec(key.getBytes(), "DES");
			cipher.init(Cipher.DECRYPT_MODE, spec);
			//byte msg[]=new BASE64Decoder().decodeBuffer(msg);
			msg= cipher.doFinal(msg);
			//msg="";
                        /*
			for (int i=0;i<messageArray.length;i++)
			msg+=(char) messageArray[i];
			msg.trim();*/
			status=true;
		
			
		}
		catch(Exception e)
		{
			System.out.println(e);
                        status=false;
			
		}
		
		
		return msg;
	}
        
        public byte[] decrypt(byte[] msg,String otp)
	{
			
		
		
		try
		{
			
			Cipher cipher= Cipher.getInstance("DES");
			SecretKeySpec spec= new SecretKeySpec(otp.getBytes(), "DES");
			cipher.init(Cipher.DECRYPT_MODE, spec);
			//byte msg[]=new BASE64Decoder().decodeBuffer(msg);
			msg= cipher.doFinal(msg);
			//msg="";
                        /*
			for (int i=0;i<messageArray.length;i++)
			msg+=(char) messageArray[i];
			msg.trim();*/
			status=true;
		
			
		}
		catch(Exception e)
		{
			System.out.println(e);
                        status=false;
			
		}
		
		
		return msg;
	}
	
	
}