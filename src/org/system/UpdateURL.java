package org.system;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

public class UpdateURL {

	String url = "";
	Properties parameters = new Properties();
	
	public UpdateURL(String fullurl) {
		url = fullurl.substring(0,fullurl.indexOf("?"));
		String params = fullurl.substring(fullurl.indexOf("?")+1,fullurl.length());
		String[] list = params.split("&");
		for (int i=0;i<list.length;i++) {
			parameters.setProperty(list[i].split("=")[0], list[i].split("=")[1]);
		}
	}

	public String getParameters() {
		return parameters.toString();
	}
	
	public String getParameter(String parameter) {
		return parameters.getProperty(parameter);
	}

	public void setParameter(String parameter, String value) {
		parameters.setProperty(parameter, value);
	}

	public String getFullURL() {
		String fullurl = url + "?";
		Enumeration<Object> e = parameters.keys();
		while (e.hasMoreElements()) {
			String key = (String)e.nextElement();
			fullurl = fullurl+key + "=" + parameters.getProperty(key)+"&";
		}
		return fullurl.substring(0, fullurl.length()-1);
	}
	
	public String getDeviceID() {
		String uid = this.getParameter("model");
		String id = Devices.getIdFromVariant(uid);
		if (id.equals(uid)) return "";
		return id;
	}

	public String getVariant() {
		String uid = this.getParameter("model");
		return uid;
	}
	
	public void dumpTo(String path) throws IOException {
		TextFile t = new TextFile(path+File.separator+"updateurl","ISO8859-15");
		t.open(false);
		t.write(this.getFullURL());
		t.close();
	}
}