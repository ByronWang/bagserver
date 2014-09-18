package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class GetAllClass {
	public static void main(String[] args) throws IOException {
		try {
			File file = new File(args[0]);
			String line = null;
			String jarFile = null;
			String filename = null;
			Map<String, JarFile> jarfiles = new HashMap<String, JarFile>();
			
			
			BufferedReader r = new BufferedReader(new FileReader(file));
			
			while((line = r.readLine())!=null){
				String[] s= line.split("\t");
				jarFile = s[0];
				if(s[0].equals("instance")){
					continue;
				}
				if(s[0].equals("__JVM_DefineClass__")){
					continue;
				}
				if(s[1].startsWith("$Proxy")){
					continue;
				}
				
				
				filename = s[1].replace("." ,"/") + ".class";
				
				JarFile jar = jarfiles.get(jarFile);
				
				if(jar==null){
					jar = new JarFile(new File(jarFile));
					jarfiles.put(jarFile, jar);
				}
				
//				Enumeration<JarEntry> entries = jar.entries();
//				while(entries.hasMoreElements()){
//					JarEntry entry= entries.nextElement();
//					System.out.println(entry.getName());
//				}

				System.out.println("copy file from : " + jar.getName() + " ! " + filename) ;
				
				JarEntry jarEntry = jar.getJarEntry(filename);
				InputStream in  = jar.getInputStream(jarEntry);	
				
				File ofile = new File(filename);
				ensurePath(ofile.getParentFile());
				
				FileOutputStream out=  new FileOutputStream(filename);				
	            byte[] buffer = new byte[1024];
	            int length = -1;
	            while ((length = in.read(buffer)) > 0) {
	                out.write(buffer, 0, length);
	            }    
				out.close();
				in.close();				
			}
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void ensurePath(File file){
		if(!file.exists()){			
			if(file.getParentFile()!= null && !file.getParentFile().exists()){
				ensurePath(file.getParentFile());
			}
			file.mkdir();
		}
	}
}
