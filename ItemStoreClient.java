package com.ItemStore;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.io.*;

public class ItemStoreClient {

	private String host = null;
	private int port;
	private Socket socket = null;
	private DataOutputStream out = null;
	private DataInputStream in = null;
	private ObjectInputStream input = null;

	/**
	 * ItemStoreClient Constructor, initializes default values for class members
	 *
	 * @param host host url
	 * @param port port number
	 */
	public ItemStoreClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * Opens a socket and establish a connection to the object store server running
	 * on a given host and port.
	 *
	 * @return n/a, however throw an exception if any issues occur
	 */
	public void connect() {

		try {
			socket = new Socket(host, port);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
			input = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends an arbitrary data object to the object store server. If an object with
	 * the same key already exists, the object should NOT be overwritten
	 * 
	 * @param key  key to be used as the unique identifier for the object
	 * @param data byte array representing arbitrary data object
	 * 
	 * @return 0 upon success 1 if key already exists Throw an exception otherwise
	 */
	public int put(String key, byte[] data) {

		try {
			// Testing Prints
			// System.out.println("We are going to send " + data);
			// System.out.println("In Stringform: " + new String(data));
			
			out.writeInt(0); // This is denote we want to "put"

			// Testing Prints
			// System.out.println("Here is the key: " + key);
			// System.out.println("Here is the key in bytes: " + key.getBytes(StandardCharsets.UTF_8));

			// Using StandardCharsets.UTF_8 for keys
			byte[] newKey = key.getBytes(StandardCharsets.UTF_8);
			out.writeInt(key.length());
			out.write(newKey);

			// Check for duplication notice from server
			int duplicate = in.readInt();
			if(duplicate == 0){
				// Key is a duplicate
				System.out.println("Key: '" + key + "' is a duplicate");
				return 1;
			}

			out.writeInt(data.length);
			out.write(data);
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;

	}

	/**
	 * Sends an arbitrary data object to the object store server. If an object with
	 * the same key already exists, the object should NOT be overwritten.
	 * 
	 * @param key       key to be used as the unique identifier for the object
	 * @param file_path path of file data to transfer
	 * 
	 * @return 0 upon success 1 if key already exists Throw an exception otherwise
	 */
	public int put(String key, String file_path) {

		// System.out.println("We are in put file");
		File fileIn = new File(file_path);
		
		// See if input file exists
		if(fileIn.exists()){
			try {
				byte[] fileInBytes = Files.readAllBytes(fileIn.toPath());

				out.writeInt(0); // This is denote we want to "put"

				// Testing Prints
				//System.out.println("Here is the key: " + key);
				//System.out.println("Here is the key in bytes: " + key.getBytes(StandardCharsets.UTF_8));

				// Using StandardCharsets.UTF_8 for keys
				byte[] newKey = key.getBytes(StandardCharsets.UTF_8);
				out.writeInt(key.length());
				out.write(newKey);

				// Check for duplication
				int duplicate = in.readInt();
				if(duplicate == 0){
					// Key is a duplicate
					System.out.println("Key: '" + key + "' is a duplicate");
					return 1;
				}

				out.writeInt(fileInBytes.length);
				out.write(fileInBytes);
				return 0;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// In case the file does not exist, throw an exception as per the instructions
			FileNotFoundException noFile = new FileNotFoundException();
			try {
				throw noFile;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		return -1;

	}

	/**
	 * Downloads arbitrary data object associated with a given key from the object
	 * store server.
	 * 
	 * @param key key associated with the object
	 * 
	 * @return object data as a byte array, null if key doesn't exist. Throw an
	 *         exception if any other issues occur.
	 */
	public byte[] get(String key) {

		try {
			out.writeInt(1); // This is denote we want to "get"

			// Testing Prints
			// System.out.println("Here is the key: " + key);
			// System.out.println("Here is the key in bytes: " + key.getBytes(StandardCharsets.UTF_8));

			// Using StandardCharsets.UTF_8 for keys
			byte[] newKey = key.getBytes(StandardCharsets.UTF_8);
			out.writeInt(key.length());
			out.write(newKey);

			// Receive the size of the byte array and then byte array of data
			int sizeOfRet = in.readInt();
			if (sizeOfRet > 0) {
				byte[] ret = new byte[sizeOfRet];
				in.readFully(ret, 0, ret.length);

				// Testing Prints
				// System.out.println("Received data: " + ret);
				// System.out.println("Data in String: " + new String(ret));
				
				return ret;
			} else if (sizeOfRet == -1){ 
				System.out.println("Key: '" + key + "' doesn't exist on server");
				return null;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * Downloads arbitrary data object associated with a given key from the object
	 * store server and places it in a file.
	 * 
	 * @param key       key associated with the object
	 * @param file_path output file path
	 * 
	 * @return 0 upon success 1 if key doesn't exist Throw an exception otherwise
	 */
	public int get(String key, String file_path) {

		File fileOut = new File(file_path);
		try {
			FileOutputStream os  = new FileOutputStream(fileOut);

			try {
				out.writeInt(1); // This is denote we want to "get"

				// Testing Prints
				// System.out.println("Here is the key: " + key);
				// System.out.println("Here is the key in bytes: " + key.getBytes(StandardCharsets.UTF_8));

				// Using StandardCharsets.UTF_8 for keys
				byte[] newKey = key.getBytes(StandardCharsets.UTF_8);
				out.writeInt(key.length());
				out.write(newKey);
	
				// Receive the byte array
				int sizeOfRet = in.readInt();
				if (sizeOfRet > 0) {
					byte[] ret = new byte[sizeOfRet];
					in.readFully(ret, 0, ret.length);
					os.write(ret);
					os.close();

					// Testing Prints (not very useful for files)
					// System.out.println("Received data: " + ret);
					// System.out.println("Data in String: " + new String(ret));

					return 0;
				} else if(sizeOfRet == -1) {
					// The key isn't on our server, so we have to delete the new file we created
					System.out.println("Key: '" + key + "' doesn't exist on server");
					
					os.close();
					
					if (!fileOut.delete()) {
						throw new IOException("Unable to delete file: " + fileOut.getAbsolutePath());
					}
					return 1;
				}
	
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} 

		return -1;

	}

	/**
	 * Removes data object associated with a given key from the object store server.
	 * Note: No need to download the data object, simply invoke the object store
	 * server to remove object on server side
	 * 
	 * @param key key associated with the object
	 * 
	 * @return 0 upon success 1 if key doesn't exist Throw an exception otherwise
	 */
	public int remove(String key) {

		try {
			out.writeInt(3); // This is denote we want to "remove"

			// Testing Prints
			// System.out.println("Here is the key: " + key);
			// System.out.println("Here is the key in bytes: " + key.getBytes(StandardCharsets.UTF_8));

			// Using StandardCharsets.UTF_8 for keys
			byte[] newKey = key.getBytes(StandardCharsets.UTF_8);
			out.writeInt(key.length());
			out.write(newKey);

			// Receive the response
			int removed = in.readInt();
			if (removed == 1) {
				System.out.println("Successfully deleted data associated with Key: '" + key + "'");
				return 0;
			} else if(removed == -1) {
				System.out.println("Key: '" + key + "' doesn't exist on server");
				return 1;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;

	}

	/**
	 * Retrieves of list of object keys from the object store server
	 * 
	 * @return List of keys as string array, null if there are no keys. Throw an
	 *         exception if any other issues occur.
	 */
	public String[] list() {

		try {
			out.writeInt(2); // This is denote we want to "list"

			// Receive the response
			// int sizeOfArr = in.readInt();
			try {
				String[] ret = (String[]) input.readObject();
				// System.out.println("Here is the size of array: " + ret.length);
				
				if(ret.length > 0){
					System.out.println("Successfully retrieved a list of keys");
					
					/* 
					// Testing if list returned properly
					int i;
					for(i = 0; i < ret.length; i++){
						System.out.println(ret[i]);
					} */
					
					return ret;
				} else {
					// There are no keys returned
					System.out.println("There are no keys on the server");
					return null;
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} catch (IOException e){
			e.printStackTrace();
		}


		return null;

	}

	/**
	 * Signals to server to close connection before closes the client socket.
	 * 
	 * @return n/a, however throw an exception if any issues occur
	 */
	public void disconnect() {

		try {
			out.writeInt(4); // This is to note we want to "disconnect"
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
