# Item Store Documentation

## General Implementation:

## ItemStoreClient

The ItemStoreClient file interacts with a remote ItemStore Server. This section will include a
general overview of how the methods were created and may not include some minor details that
were included in the actual code. The goal of this section is to provide a basic understanding of
how the individual methods were generated. The public class includes variables include the host
name (string), port number (integer), socket (socket), as well as input and output streams
(DataOutputStream, DataInputStream, ObjectInputStream). All these variables will need to be
used for the methods listed.

public ItemStoreClient(String host, int port) {

This method is simply the ItemStoreClient constructor. It initializes the host name and port
number for when the client decides to connect.

public void connect() {

This function opens a socket and establishes a connection to the object store server using the host
name and port number established in the constructor. The method will attempt to open the socket
with this information and then attempts to initialize our DataOutputStream, DataInputStream,
and ObjectInputStream. We throw exceptions if any of these fails.

public int put(String key, byte[] data) {

This method sends an arbitrary data object to the object store server. Initially, we send the integer
“0” to the server as this tells the server we are in the put method and wish to insert a byte array to
the store. Once this is sent, the function will modify the string key to a byte array in UTF- 8
format. This allows the server to know how to decode the bytes after it is sent over. We then
send the length of the key’s byte array to the server and then send the key’s byte array. After
these are sent, the server should respond with an integer; if this response is “0”, then the server is
indicating that the key is a duplicate and cannot be inserted to the store. In this case, the function
prints an error message and returns “1”. Otherwise, the server did not indicate any duplication
and it is now fine to submit the data to the server. Similarly, to the key, we first send and integer
containing the size of the data’s byte array before sending the data’s byte array itself. After this is
submitted, we return “0”.

public int put(String key, String file_path) {
This method functions similarly to its predecessor when the data is a byte array, however, some
additional steps are necessary to send the file. Initially, the method creates a file object using the
given file path. If the file does not exist, we throw a FileNotFound Exception. Otherwise, the
method will generate a byte array of the file using the Files.readAllBytes(). Once this is done, the
function emulates the logistics of put(String key, byte[] data).

public byte[] get(String key) {

The get method downloads arbitrary data in the form of a byte array given the key string.
Initially, we send the integer “1” to the server as this informs the server that we are in the get
method and wish to retrieve data from the server’s store. Similarly, to the put method, we modify
the string key to a byte array using UTF-8 format and send the length of the key and the key to
the server. After this, the server will send a response of an integer which indicates the size of the
return byte array. If the key does not exist on the server, it will manually return a size of “-1” and
in this case, we know we cannot receive anything else. At this point, we return a null array.
However, if the size received is greater than 0, we create a new return byte array of this size and
read that many bytes from the server into our return byte array. This is then returned to the client.

public int get(String key, String file_path) {

This method again functions similarly to its predecessor with some additional steps to write the
new file. At first, we generate a file object using the given path. After this, we generate a
FileOutputStream so we can later write the received bytes into the file object. At this point, we
follow the same steps in the previous get function and send the indication of get method as well
as the length of the key and the key to the server. The server will respond with the size of the
return array and if the value is “-1”, the key does not exist on the server. The FileOutputStream is
closed, the newly created file is then deleted and the function returns “1”. If the server tells us the
size of the byte array is greater than 0, we read in those bytes and write them to the file using the
FileOutputStream. Once this is done, we close the output stream and return “0”.

public int remove(String key) {

The remove function removes the data object associated with the given key from the store server.
This function is relatively simple, we first send the integer “3” to the server as this lets the server
know we want to remove something from its store. We follow the same steps as all previous
methods and convert the string key to a byte array using UTF-8 and send the length of the key as
well as the byte array key to the server. The server will then send a response integer. If the


response is “1”, the object was successfully deleted and return “0”. If the response is “-1”, the
key does not exist on the server and we return “1”.

public String[] list() {

The list function is the only method to use an ObjectInputStream along with the
DataOutputStream. This function begins by sending the integer “2” to the server as this lets the
server know we want to receive a list of strings of keys. After this, we read in the object stream
as a string array. If the length of the array is greater than 0, we can return the array we received.
If it is not greater than 0, we return null.

public void disconnect() {

This function is simple, it first sends an integer to the server with the number “4” as this denotes
the client wants to disconnect from the server. After this, the function simply closes the socket.

### ItemStoreServer

The ItemStoreServer file interacts with any clients that want to connect to it. This section will
include a general overview of how the methods were created and may not include some minor
details that were included in the actual code. The goal of this section is to provide a basic
understanding of how the individual methods were generated. The public class includes variables
include a ServerSocket, socket (socket), as well as input and output streams (DataOutputStream,
DataInputStream, ObjectOutputStream) and finally, a map (HashMap<String, byte[]>) to store
the entries. All these variables will need to be used for the methods listed.

public static void main(String args[]) {

This function was generated in the template. The only addition to this method is calling the
ItemStoreServer constructor with the associated port.

public ItemStoreServer(int port) {

This constructor starts the server and waits for a connection. The method begins by initializing
the hashmap and server socket on the given port. Once the server has started, we enter a while
loop which lasts forever; this is so the server will always be listening for a connection (this
means the server must be manually shut down). In this while loop, the server attempts to accept
connections and once it received one, it initializes the three input/output streams (DataOutputStream, DataInputStream, ObjectOutputStream). Once this is done, we enter another while loop which continues until the client disconnects. This 
inner while loop reads in an integer and based on the integer sent, it knows which command the client wishes to use (put = 0, get = 1,list = 2, remove = 3, disconnect = 4). If the client calls disconnect, we break out of this inner
while loop and wait for further connections.

private void putServer() throws IOException {

This first helper method is used to put objects into our HashMap and is called when the client
wishes to insert something to the store (either a byte array of data or a file). The method begins
by declaring a byte array for the key and data that will be received and initializing them to null.
The client should then send the length of the key; we read this integer and initialize our key byte
array to be this size. We then read in a number of bytes equal to the size into the key byte array.
We then use UTF- 8 to decode this key as a new string for our server to use for the HashMap.
After the key has compiled on the server, we do our duplication check; if the map already
contains the key, we send the client the integer “0” to denote that the key is a duplicate and
return from this function to await further commands. If it was not a duplicate, we send “1” and
continue. After confirming that the key is not a duplicate, the client should send another integer
containing the size of the byte array that it intends to send. We use this to initialize our data byte
array and read in the number of bytes equal to the size of the data byte array. Once this is done,
we have our key and byte array on the server and simply add it to our HashMap.

private void getServer() throws IOException {

The second helper method is used to get objects from our HashMap and return them to the client.
This function acts similarly to putServer as it creates a byte array for the key and data and
receives the key length and key from the client and converts it using UTF-8. Once we receive the
key, if the HashMap does not contain the key, we send the client an integer (which would
normally indicate the length of the return data byte array) of “-1”. The value “-1” indicates to the
client that the key was not found in the server’s HashMap and following this the method simply
returns. If the HashMap does contain the key, we set our data byte array equal to the contents in
the HashMap. After this, we send the client an integer with the length of the data byte array
followed by the data itself.

private void removeServer() throws IOException {

The third helper method of our server is used to remove entries from our HashMap. There is no
return value. This method creates a null byte array to hold the key. We read in the length of the
key from the client and then read in the key that is received afterwards. We use UTF-8 to format
the key properly as a string on the server. We then check to see if the map contains the key and if
it does not, we send the integer “-1” to the client to let it know that the key does not exist on the
server. If it does exist, we remove the key and associated data from our HashMap and send the
integer “1” to the client to indicate success.

private void listServer() throws IOException {

The fourth helper method returns a list of keys in our HashMap. It is the only method to use the
ObjectOutputStream. We first create a Set of Strings to hold the key set of the map. We then
create a string array and convert the set to a string array. Once this is done, we simply send the
string array to the client through the ObjectOutputStream.

## Questions and Answers

### How does the client and server handle the different object store operations? Describe the design and implementation of your communication protocol.

I am including these questions together as a lot of the implementation designs are described
under General Implementation. However, the communication protocol of the server and client do
tend to follow a certain structure. The client sends an integer to the server to indicate to the
server which command is about to be processed. Afterwards, the client would normally send
another integer with the size of the key, followed by the key itself. Depending on the method, the
client may or may not receive some sort of error/duplication check on the key. After this the data
length of the data and then the data itself is either sent or received to/from the server’s store.

### What kind of data structures did you use to store and retrieve your data objects?

The server uses a HashMap with a String key and Byte Array value. All data (keys and values)
would be converted to a byte array to send between the client and server. They would be
converted back when ItemStoreClient returns the values to the original client.

### Do you think your implementation could be better? What was the hardest parts about implementing your project and how did you overcome them?

Very much so. I do not believe I implemented this project in the best capacity possible. I have
never done socket programming in Java before and even when I have done socket programming
in other languages, they were very simple projects. I was not used to used input and output
streams and had to test a lot of different methods of sending data before I settled on
DataInput/DataOutputStreams. This initial aspect of sending the data and being able to properly
read it on the server was by far the biggest struggle I had implementing this project.  I am not 
completely sure if my methodology for sending data is the proper way to
handle these streams even though I seem to pass my own test
cases. However, after resolving these issues and using the data streams, I found implementing
the rest of the project to be relatively easier since they followed a similar format.

If my intention were to make this project as efficient as possible, I certainly would not have used
a HashMap to store the data. There are better data structures available for a project such as this
though a HashMap seemed to be the simplest to use. I am also aware that my error checking may
not be complete and would not be surprised if there were cases where client crashed. For
example, if the client attempts to print out the values of the return value from client.list() and the
list is empty (no keys have been inserted into the store), I found that this makes the socket
connection reset and I could not figure out why this was the case. All in all, I do not think this
project was intended to be very difficult, the concept is simple, and I enjoyed the idea of it as
well as the finished product. I believe my implementation is sufficient but can be improved upon.

