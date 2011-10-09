/*
 * MCDocsPlus by RTRD (Originally by Tazzernator)
 * (RTRD ~ http://rtrd.no)
 * 
 * THIS PLUGIN IS LICENSED UNDER THE WTFPL - (Do What The Fuck You Want To Public License)
 * 
 * This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details.
 * 
 * TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
 *   
 * 0. You just DO WHAT THE FUCK YOU WANT TO.
 *   
 * */

/*
 * 
 * MCDocsPlus by RTRD
 * (RTRD ~ http://rtrd.no)
 *    
 */

package no.rtrd.bukkit.mcdocsplus;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.bukkit.Bukkit;

public class Time {
    public String getTime() {
    	Bukkit.getServer().getWorld("world").getTime();
        Date now = new Date();
        Format formatter = new SimpleDateFormat(Listener.tFormat);
        String time = (formatter.format(now));
        return time;
    }
}