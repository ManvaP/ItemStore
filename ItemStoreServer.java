package com.ItemStore;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Set;
import java.io.*;

public class ItemStoreServer {

	private ServerSocket server = null;
	private Socket socket = null;
	private DataInputStream input = null;
	private DataOutputStream output = null;
	private ObjectOutputStream out = null;
	
	private HashMap<String, byte[]> map = null;

	public ItemStoreServer(int port) {
		map = new HashMap<>();

		try {
			server = new ServerSocket(port);
			System.out.println("Server started. Listening on port " + port);
			
			while(true){
				socket = server.accept();
				System.out.println("Connection Established");

				input = new DataInputStream(socket.getInputStream());
				output = new DataOutputStream(socket.getOutputStream());
				out = new ObjectOutputStream(socket.getOutputStream());

				while(true){
					int command = input.readInt();
				
					if(command == 0){ // Command 0 = put
						putServer();
					} else if(command == 1){ // Command 1 = get
						getServer();
					} else if(command == 2){ // Command 2 = list
						listServer();
					} else if(command == 3){ // Command 3 = remove
						removeServer();
					} else if(command == 4){ // Command 4 = disconnect
						System.out.println("Closing Connection to: " + socket);
						System.out.println("Listening on port " + port);
						break;
					}
				}
			}
		} catch (IOException i) {
			System.out.println(i);
		}
	}

	private void listServer() throws IOException {
		// Get the Set of keys in the HashMap
		int sizeOfMap = map.size();
		Set<String> setOfKeys = map.keySet();
		String[] listOfKeys = new String[sizeOfMap];

		// Convert the HashMap to a String Array
		int index = 0;
		for(String str : setOfKeys){
			listOfKeys[index] = str;
			++index;
		}
		
		// Testing Prints/Response
		// System.out.println("KeySet: "+ map.keySet());
		// System.out.println("List of keys: " + listOfKeys[0]);
		// output.writeInt(listOfKeys.length);
		
		System.out.println("Sending list of keys");
		out.writeObject(listOfKeys);
	}

	private void removeServer() throws IOException {
		byte[] key = null;

		// Get the key
		int lengthOfKey = input.readInt();
		if(lengthOfKey > 0){
			key = new byte[lengthOfKey];

			input.readFully(key, 0, key.length);
		}
		String newKey = new String(key, StandardCharsets.UTF_8);
		
		// Testing Prints
		// System.out.println("Key on server in bytes = " + key);
		// System.out.println("Key on server = " + newKey);

		// If the map contains the key we need to remove, remove it from the HashMap and return 1 for success, otherwise, -1 for failure
		if(map.containsKey(newKey)){
			// Testing Prints/Data
			// System.out.println("We found the key in remove!");
			// data = map.get(newKey);
			// System.out.println("Corresponding data: " + data);

			// Remove the key and data
			System.out.println("removing \"" + newKey + "\"");
			map.remove(newKey);
			output.writeInt(1);
		} else{
			output.writeInt(-1);
		}
	}

	private void getServer() throws IOException {
		byte[] key = null;
		byte[] data = null;

		// Get the key
		int lengthOfKey = input.readInt();
		if(lengthOfKey > 0){
			key = new byte[lengthOfKey];

			input.readFully(key, 0, key.length);
		}
		String newKey = new String(key, StandardCharsets.UTF_8);
		
		// Testing Prints
		// System.out.println("Key on server in bytes = " + key);
		// System.out.println("Key on server = " + newKey);

		if(map.containsKey(newKey)){
			data = map.get(newKey);
		} else {
			System.out.println("Key not found on Server");
			output.writeInt(-1);
			return;
		}

		System.out.println("getting \"" + newKey + "\"");
		output.writeInt(data.length);
		output.write(data);
	}

	private void putServer() throws IOException {
		byte[] key = null;
		byte[] data = null;

		// Get the key
		int lengthOfKey = input.readInt();
		if(lengthOfKey > 0){
			key = new byte[lengthOfKey];

			input.readFully(key, 0, key.length);
		}
		String newKey = new String(key, StandardCharsets.UTF_8);
		
		// Testing Prints
		// System.out.println("Key on server in bytes = " + key);
		// System.out.println("Key on server = " + newKey);
		
		// Duplicate Key Check - Send 0 for duplicate and 1 for non-duplicate
		if(map.containsKey(newKey)){
			output.writeInt(0);
			return;
		} else {
			output.writeInt(1);
		}

		// Get the data
		int count = input.readInt();
		if(count > 0){
			data = new byte[count];
			input.readFully(data, 0, data.length);
		}

		// Put the key and data in our HashMap
		map.put(newKey, data);
		System.out.println("putting \"" + newKey + "\"");
	}

	/**
	 * ItemObjectServer Main(). Note: Accepts one argument -> port number
	 */
	public static void main(String args[]) {

		// Check if at least one argument that is potentially a port number
		if (args.length != 1) {
			System.out.println("Invalid number of arguments. You must provide a port number.");
			return;
		}

		// Try and parse port # from argument
		int port = Integer.parseInt(args[0]);

		ItemStoreServer server = new ItemStoreServer(port);

	}

}
