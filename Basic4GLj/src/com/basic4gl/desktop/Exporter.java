package com.basic4gl.desktop;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.swing.JOptionPane;

import com.basic4gl.lib.util.Library;
import com.basic4gl.lib.util.Target;

public class Exporter {
	public void run(Target main, List<Library> libraries) throws IOException
	{
		int i;
		String name, path;
		boolean hasAnonymous; //Whether or not anonymous classes exist within a class
		Class<?> c;

		ClassLoader loader;
		List<String> dependencies;
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, main.getClass().getName());
		
		//path = ".";
		//Generate class path
		/*for(Library lib: libraries){
			dependencies = lib.getDependenciesForClassPath();
			if (dependencies != null)
				for (String dependency: dependencies)
					path += (" " + dependency);
		}*/
		manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, "");
		//manifest.getMainAttributes().put(Attributes.Name.)

		JarOutputStream target = new JarOutputStream(new FileOutputStream("output.jar"), manifest);
		//Add library interfaces
		add(com.basic4gl.lib.util.Compiler.class, target);
		add(com.basic4gl.lib.util.Target.class, target);
		add(com.basic4gl.lib.util.TaskCallback.class, target);
		add(com.basic4gl.lib.util.FileAccessor.class, target);
		add(com.basic4gl.lib.util.Library.class, target);
		add(com.basic4gl.lib.util.Text.class, target);

		//add(new File(main.getClass().getResource(main.getClass().getSimpleName() + ".class").getPath()), target);
		//TODO Add libraries and their dependencies
		for(Library lib: libraries){	//Target is included in libraries
			//c = lib.getClass();
			add(lib.getClass(), target);	//Add library to Jar
			//Add inner classes
			for (Class<?> c2: lib.getClass().getDeclaredClasses())
				add(c2, target);
			//Add anonymous classes
			i = 1;
			name = lib.getClass().getName().replace('.', '/');
			loader = lib.getClass().getClassLoader();
			do{
				hasAnonymous = false;
				if (loader.getResourceAsStream(name + "$" + i + ".class") != null){
					hasAnonymous = true;
					add(lib.getClass(), name + "$" + i + ".class", target);
					i++;
				}

			}while (hasAnonymous);
			//add(lib.getClass().getClasses(),target)
			//Add library's dependencies to Jar
			dependencies = lib.getDependencies();
			if (dependencies != null)
				for (String dependency: dependencies){
					add(lib.getClass(), dependency, target);
				}
		}

		target.close();
		JOptionPane.showMessageDialog(null, "Done!");
	}


	private static void add(Class<?> c, JarOutputStream jarOutputStream) throws IOException
	{
		ClassLoader loader;
		loader = c.getClassLoader();
		//if (loader == null)
		//loader = c.getClassLoader();
		String path = c.getName().replace('.', '/') + ".class";
		jarOutputStream.putNextEntry(new JarEntry(path));
		jarOutputStream.write(toByteArray(loader.getResourceAsStream(path)));
		jarOutputStream.closeEntry();
	}
	private static void add(Class<?> c, String path, JarOutputStream jarOutputStream) throws IOException
	{
		jarOutputStream.putNextEntry(new JarEntry(path));
		jarOutputStream.write(toByteArray(c.getClassLoader().getResourceAsStream(path)));
		jarOutputStream.closeEntry();
	}
	public static byte[] toByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[0x1000];
		while (true) {
			int r = in.read(buf);
			if (r == -1) {
				break;
			}
			out.write(buf, 0, r);
		}
		return out.toByteArray();
	}
	private void add(File source, JarOutputStream target) throws IOException
	{
		BufferedInputStream in = null;
		try
		{
			if (source.isDirectory())
			{
				String name = source.getPath().replace("\\", "/");
				if (!name.isEmpty())
				{
					if (!name.endsWith("/"))
						name += "/";
					JarEntry entry = new JarEntry(name);
					entry.setTime(source.lastModified());
					target.putNextEntry(entry);
					target.closeEntry();
				}
				for (File nestedFile: source.listFiles())
					add(nestedFile, target);
				return;
			}

			JarEntry entry = new JarEntry(source.getPath().replace("\\", "/"));
			entry.setTime(source.lastModified());
			target.putNextEntry(entry);
			in = new BufferedInputStream(new FileInputStream(source));

			byte[] buffer = new byte[1024];
			while (true)
			{
				int count = in.read(buffer);
				if (count == -1)
					break;
				target.write(buffer, 0, count);
			}
			target.closeEntry();
		}
		finally
		{
			if (in != null)
				in.close();
		}
	}
}
