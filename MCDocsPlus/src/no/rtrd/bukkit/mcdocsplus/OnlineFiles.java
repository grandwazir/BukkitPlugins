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

public class OnlineFiles {

	protected long time;
	protected String URL;

	public OnlineFiles(long time, String URL){
		this.time = time;
		this.URL = URL;
	}
	
	public long getMs(){
		return time;
	}
	
	public String getURL(){
		return URL;
	}
}
