/*
 * LunarTunerMobile.java
 *
 * Created on 2007/07/01, 11:55
 */

package mobile;


import java.io.ByteArrayOutputStream;
import java.util.Date;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;


/**
 *
 * @author Atsuya Takagi
 * @version
 */
public class LunarTunerMobile extends MIDlet
{
	public LunarTunerMobile()
	{
	}
	
	
	public void startApp()
	{
		//Display.getDisplay(this).setCurrent(alert);
		
		System.out.println("Audio Support: "+System.getProperty("supports.audio.capture"));
		
		System.out.println("Checking supported content types.");
		String[] contentTypes = Manager.getSupportedContentTypes("capture");
		for(int i = 0; i < contentTypes.length; i++)
		{
			System.out.println("ContentType: "+contentTypes[i]);
		}
		
		try
		{
			Player player = Manager.createPlayer("capture://audio");
			player.realize();
			RecordControl recordControl = (RecordControl)player.getControl("RecordControl");
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			
			while(true)
			{
				outputStream.reset();
				recordControl.setRecordStream(outputStream);
				recordControl.startRecord();
				player.start();
				Thread.currentThread().sleep(5000);
				recordControl.commit();
				System.out.println("Recorded: "+outputStream.size()+" bytes.");
			}
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
		}
	}

	public void pauseApp()
	{
	}

	public void destroyApp(boolean unconditional)
	{
	}
}
