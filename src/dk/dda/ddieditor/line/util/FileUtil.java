package dk.dda.ddieditor.line.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class FileUtil {
	/**
	 * Persist properties to a file
	 * 
	 * @param path
	 *            to file
	 * @param properties
	 *            to store
	 * @throws Exception
	 */
	public static void storeProperties(String path, Properties properties)
			throws Exception {
		storeProperties(new File(path), properties);
	}

	/**
	 * Persist properties to a file
	 * 
	 * @param file
	 *            to store in
	 * @param properties
	 *            to store
	 * @throws Exception
	 */
	public static void storeProperties(File file, Properties properties)
			throws Exception {
		if (file.exists()) {
			file.delete();
		}

		try {
			FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
			properties.store(fileWriter, "Stored by: "
					+ System.getenv().get("USER"));
			fileWriter.close();
		} catch (Exception e) {
			throw new Exception("Error storing at: " + file.getAbsoluteFile()
					+ " properties: " + properties.toString(), e);
		}
	}

	/**
	 * Load properties from a file
	 * 
	 * @param file
	 *            to load from
	 * @return loaded properties
	 * @throws Exception
	 */
	public static Properties loadProperties(File file) throws Exception {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new Exception("File notfound: "
					+ file.getAbsoluteFile().getAbsolutePath(), e);
		} catch (IOException e) {
			throw new Exception("File IO error"
					+ file.getAbsoluteFile().getAbsolutePath(), e);
		}
		return properties;
	}
}
