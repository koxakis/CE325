package ce325.hw2;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.locks.ReentrantLock;

public class FtpClient {
	Socket controlSocket;
	BufferedReader reader;
	PrintWriter out;
	BufferedReader in;
	boolean success;

	File workingDir;
	Thread threadS;
	ReentrantLock lock = new ReentrantLock();

	static boolean DEBUG = false;

	enum DBG {IN, OUT};

	void dbg(DBG direction, String msg) {
		if(DEBUG) {
			if(direction == DBG.IN)
				System.err.println("<- "+msg);
			else if(direction == DBG.OUT)
				System.err.println("-> "+msg);
			else
				System.err.println(msg);
		}
	}

	public FtpClient(boolean pasv, boolean overwrite) {
		reader = new BufferedReader( new InputStreamReader(System.in) );
		workingDir = new File(".");
	}

	public void bindUI(String [] args) {
		String inetAddress;
		int port=0;

		try {

			if( args!=null && args.length > 0 ) {
				inetAddress = args[0];
			}
			else {
				System.out.print("Hostname: ");
				inetAddress = reader.readLine();
			}

			if( args!=null && args.length > 1 ) {
				port = new Integer( args[1] ).intValue();;
			}
			else {
				System.out.print("Port: ");
				port = new Integer( reader.readLine() ).intValue();
			}

			if( bind(inetAddress, port) ) {
				System.out.println("Socket bind OK!");
			}else
			System.out.println("Socket bind FAILED!");

		} catch( IOException ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	public boolean bind(String inetAddress, int port) {
		try {
			controlSocket = new Socket(inetAddress, port);
			out = new PrintWriter(controlSocket.getOutputStream(), true);
			in = new BufferedReader( new InputStreamReader(controlSocket.getInputStream() ));

			return true;
		}catch(UnknownHostException ex){
			System.err.println("Don't know about host " + inetAddress + " Port: " + port);
			return false;
		}catch(IOException ex1){
			System.err.println("Couldn't get connection to " + inetAddress + " Port: " + port);
			return false;
		}

	}

	public void loginUI() {
		String username, passwd;
		String socketInput;

		try {
			System.out.print("Login Username: ");
			username = reader.readLine();
			System.out.print("Login Password: ");
			passwd = reader.readLine();

			if( login(username, passwd) )
			System.out.println("Login for user \""+username+"\" OK!");
			else
			System.out.println("Login for user \""+username+"\"Failed!");

		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	public boolean login(String username, String passwd) {
		try{
			out.println("USER " + username);
			out.println("PASS " + passwd);
			in.readLine();

			if (in.readLine().startsWith("230")){
				return true;
			}

			if (in.readLine().startsWith("530")){
				return false;
			}
			return true;
		}catch(Exception ex3){
			System.out.println(ex3.getMessage());
			return false;
		}
	}

	public void listUI() {
		try {
			System.out.print("Enter path to list (or . for the current directory): ");
			String path = reader.readLine();
			String info = list(path);

			if (info != null && !info.isEmpty()){
				List<RemoteFileInfo> list = parse(info);
				for(RemoteFileInfo listinfo : list)
					System.out.println(listinfo);

			}else{
				System.out.println("This directory is empty or not found.");
			}

		} catch(IOException ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
	}


	class threadSocket extends Thread {

		Socket dataSocket;
		BufferedReader serverIn;
		StringBuilder info;
		String temp;

		public threadSocket (String hostIp, int hostPort, StringBuilder arg_info) {
			try {
				//arg_info is used to pass the result to the main thread
				this.info = arg_info;
				//Opening the socket as dictaded from PASV
				dataSocket = new Socket(hostIp, hostPort);
				serverIn = new BufferedReader( new InputStreamReader(dataSocket.getInputStream() ));
				success = true;
			}catch(UnknownHostException ex){

<<<<<<< HEAD
				System.err.println("Don't know about host " + hostIp + " From threadSocket " + hostPort);
				success = false;
			}catch(IOException ex2){

				System.err.println(ex2.getMessage()+ " " + hostIp + " From threadSocket " + hostPort);
				success = false;
			}catch(Exception ex3){

				System.err.println(ex3.getMessage()+ " " + hostIp + " From threadSocket " + hostPort);
				success = false;
=======
				System.err.println(ex.getMessage() + " " + hostIp + " From threadSocket on port " + hostPort);
				System.exit(0);
			}catch(IOException ex1){

				System.err.println(ex1.getMessage()+ " " + hostIp + " From threadSocket on port " + hostPort);
				System.exit(0);
>>>>>>> master
			}
		}

		public void run() {

			lock.lock();
			try{
				//This while puts the 'ls -l' result to a StringBuilder
				while(( temp = serverIn.readLine()) != null){

					info.append(temp);
					info.append("\n");
				}

			}catch(IOException ex){
				System.out.println(ex.getMessage());
			}
			lock.unlock();
		}

	}

	public String list(String path) {
		String pasvModeData;
		String hostIp, hexMSB, hexLSB;
		StringBuilder info = new StringBuilder();
		int portMSB, portLSB;
		int hostPort;
		String temp = new String();

<<<<<<< HEAD
		do{
			lock.lock();
			out.println("PASV");
			try{
				pasvModeData = in.readLine();
			}catch(IOException ex6){
				System.out.println(ex6.getMessage());
				return "error";
			}
			lock.unlock();
			Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(pasvModeData);
=======
		//This lock is used to safeguard in case of another message disrupts the PASV message
		lock.lock();
		out.println("PASV");
		try{
			pasvModeData = in.readLine();
		}catch(IOException ex){
			System.out.println(ex.getMessage());
			return "error";
		}
		lock.unlock();

		/*Parsing of the data coming from the ftp server is being prossesed with REGEXP to determine the
			data inside the parentheses (IP1,IP2,IP3,IP4,PortMSB,portLSB) */
		Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(pasvModeData);
>>>>>>> master

			while(m.find()){
				temp = temp + m.group(1);
			}
			String[] tokens = temp.split(",");

			hostIp = tokens[0] + "." + tokens[1] + "." + tokens[2] + "." + tokens[3];

			portMSB = Integer.parseInt(tokens[4]);
			portLSB = Integer.parseInt(tokens[5]);

			hexMSB = Integer.toHexString(portMSB);
			hexLSB = Integer.toHexString(portLSB);

			hostPort = Integer.decode("0x" + hexMSB + hexLSB);

			threadS = new threadSocket(hostIp, hostPort, info);
		}while(!success);

		threadS.start();

		out.println("LIST " + path);
		try{
			//Main thread waits child thread to finish excecuting
			threadS.join();
		}catch(InterruptedException ex){
			System.out.println(ex.getMessage());
			return "error";
		}
		try{
			//This readLine() is used to clear the buffer from server messages
			in.readLine();
			in.readLine();
		}catch(IOException ex){
			System.out.println(ex.getMessage());
			return "error";
		}


		return info.toString();
	}

	class RemoteFileInfo {
		boolean dir = false; // is directory
		boolean ur = false;  // user read permission
		boolean uw = false;  // user write permission
		boolean ux = false;  // user execute permission
		boolean gr = false;  // group read permission
		boolean gw = false;  // group write permission
		boolean gx = false;  // group execute permission
		boolean or = false;  // other read permission
		boolean ow = false;  // other write permission
		boolean ox = false;  // other execute permission
		long size;           // file size
		String name;

		public RemoteFileInfo(String line) {

			/*This is the standard length of the ls -l command in witch we find the begining of the
				file name */
			int i = 8;

			//We take one line and split it between the spaces creating an array of tokens
			String[] tokens = line.split("\\s+");

			//The first token contains the file's/directory's permissions
			this.permissions(tokens[0]);

			//The fifth token contains the filesize
			size = Long.parseLong(tokens[4]);

			/*The file/dir name may contain spaces so we append the rest of the line in order
				to get the full file/dir name */
			name = tokens[i];
			while(i + 1 < tokens.length) {
				i++;
				name = name + " " + tokens[i];
			}

		}

		private void permissions(String perms) {

			if (perms.charAt(0) == 'd') dir = true;

			if (perms.charAt(1) == 'r') ur = true;
			if (perms.charAt(2) == 'w') uw = true;
			if (perms.charAt(3) == 'x') ux = true;

			if (perms.charAt(4) == 'r') gr = true;
			if (perms.charAt(5) == 'w') gw = true;
			if (perms.charAt(6) == 'x') gx = true;

			if (perms.charAt(7) == 'r') or = true;
			if (perms.charAt(8) == 'w') ow = true;
			if (perms.charAt(9) == 'x') ox = true;
		}

		public String toString() {

			String stringToPrint;

			stringToPrint = "-> \"" + name + "\"";

			if (dir) stringToPrint = stringToPrint + " is a directory";
			else stringToPrint = stringToPrint + " is a file";

			stringToPrint = stringToPrint + " of size " + size + " bytes\n    Permissions [ User: ";

			if (ur) stringToPrint = stringToPrint + "r";
			else stringToPrint = stringToPrint + "-";
			if (uw) stringToPrint = stringToPrint + "w";
			else stringToPrint = stringToPrint + "-";
			if (ux) stringToPrint = stringToPrint + "x";
			else stringToPrint = stringToPrint + "-";

			stringToPrint = stringToPrint + " | Group: ";

			if (gr) stringToPrint = stringToPrint + "r";
			else stringToPrint = stringToPrint + "-";
			if (gw) stringToPrint = stringToPrint + "w";
			else stringToPrint = stringToPrint + "-";
			if (gx) stringToPrint = stringToPrint + "x";
			else stringToPrint = stringToPrint + "-";

			stringToPrint = stringToPrint + " | Other: ";

			if (or) stringToPrint = stringToPrint + "r";
			else stringToPrint = stringToPrint + "-";
			if (ow) stringToPrint = stringToPrint + "w";
			else stringToPrint = stringToPrint + "-";
			if (ox) stringToPrint = stringToPrint + "x";
			else stringToPrint = stringToPrint + "-";

			stringToPrint = stringToPrint + " ]";

			return stringToPrint;
		}
	}

	/*Splits the received result of the ls -l in lines and creates a RemoteFileInfo object for each line
		and inserts it in a list */
	public List<RemoteFileInfo> parse(String info) {

		List<RemoteFileInfo> list = new LinkedList<RemoteFileInfo>();
		String[] tokens = info.split("\\n");

		if (info != null && !info.isEmpty()){
			for (int i=1; i < tokens.length; i++){

				RemoteFileInfo data = new RemoteFileInfo(tokens[i]);
				list.add(data);
			}
		}

		return list ;
	}

	public void uploadUI() {
		try {
			System.out.print("Enter file to upload: ");
			String filepath = reader.readLine();
			File file = new File(filepath);
			if(file.exists()){

				if (mupload(file)) {
					System.out.println("Filename \""+file.getName()+"\" upload successfull");
				}else{
					System.out.println("Filename \""+file.getName()+"\" upload failed");
				}
			}else{
				System.out.println("Filename or directory \""+file.getName()+"\" not found");
			}
		} catch(IOException ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	* Upload multiple files
	* @param f can be either a local filename or local directory
	*/
	public boolean mupload(File f) {
		if( f.isDirectory() ) {
			mkdir(f.getName() );
			cwd( f.getName() );
			File files[] = f.listFiles();
			for(File listentry : files) {

				mupload(listentry);
			}
			cwd("..");
			return true;
		}
		else {
			if( upload(f) ) {
				return true;
			}
			return false;
		}

	}

	class threadUpload extends Thread {

		Socket dataSocket;
		OutputStream clientOut;

		FileInputStream outToServer;

		public threadUpload (String hostIp, int hostPort, File fileName) {
			try {
				//Open a stream from inputed file
				outToServer = new FileInputStream(fileName);
				//Open socket connection
				dataSocket = new Socket(hostIp, hostPort);
				//Connect socket with a buffer
				clientOut = dataSocket.getOutputStream();

				success = true;
			}catch(UnknownHostException ex){

<<<<<<< HEAD
				System.err.println(ex.getMessage()+ " " + hostIp + "From threadUpload");
				success = false;
			}catch(IOException ex2){

				System.err.println(ex2.getMessage()+ " " + hostIp + "From threadUpload");
				success = false;
			}catch(Exception ex3){

				System.err.println(ex3.getMessage()+ " " + hostIp + " From threadUpload " + hostPort);
				success = false;
=======
				System.err.println(ex.getMessage() + " " + hostIp + " From threadUpload on port " + hostPort);
				System.exit(0);
			}catch(IOException ex1){

				System.err.println(ex1.getMessage() + " " + hostIp + " From threadUpload on port " + hostPort);
				System.exit(0);
>>>>>>> master
			}
		}

		public void run() {

			lock.lock();
			try {

				int read_len;
    			while( (read_len = outToServer.read()) != -1 ) {

        			clientOut.write(read_len);
					clientOut.flush();
    			}

				outToServer.close();
				dataSocket.close();

			}catch( IOException ex ) {
			    ex.printStackTrace();
			}
			lock.unlock();
		}

	}

	public boolean upload(File f){
		String pasvModeData;
		String hostIp, hexMSB, hexLSB;
		int portMSB, portLSB;
		int hostPort;
		String temp = new String();

		try{
			out.println("TYPE I");
			if (!in.readLine().startsWith("200")){
				System.out.println("Error in setting the ");
			}
		}catch(IOException ex){
			System.out.println(ex.getMessage());
			return false;
		}
<<<<<<< HEAD
		do{
			lock.lock();
			out.println("PASV");
			try{
				pasvModeData = in.readLine();
			}catch(IOException ex6){
				System.out.println(ex6.getMessage());
				return false;
			}
			lock.unlock();
			Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(pasvModeData);
=======
		lock.lock();
		out.println("PASV");
		try{
			pasvModeData = in.readLine();
		}catch(IOException ex){
			System.out.println(ex.getMessage());
			return false;
		}
		lock.unlock();
		Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(pasvModeData);

		while(m.find()){
			temp = temp + m.group(1);
		}
		String[] tokens = temp.split(",");
>>>>>>> master

			while(m.find()){
				temp = temp + m.group(1);
			}
			String[] tokens = temp.split(",");

			hostIp = tokens[0] + "." + tokens[1] + "." + tokens[2] + "." + tokens[3];

			portMSB = Integer.parseInt(tokens[4]);
			portLSB = Integer.parseInt(tokens[5]);

			hexMSB = Integer.toHexString(portMSB);
			hexLSB = Integer.toHexString(portLSB);

			hostPort = Integer.decode("0x" + hexMSB + hexLSB);
			threadS = new threadUpload(hostIp, hostPort, f);
		}while(!success);

		out.println("STOR " + f.getName());
		threadS.start();
		try{
			threadS.join();
		}catch(InterruptedException ex){
			System.out.println(ex.getMessage());
			return false;
		}

		try{
			in.readLine();
			in.readLine();
		}catch(IOException ex){
			System.out.println(ex.getMessage());
			return false;
		}

		return true;
	}

	public void downloadUI() {
		try {
			System.out.print("Enter file to download: ");
			String filename = reader.readLine();
			File file = new File(filename);                // we have an absolute path

			if( file.exists() && !file.isDirectory()) {
				System.out.println("File \""+file.getPath()+"\" already exist.");
				String yesno;
				do {
					System.out.print("Overwrite (y/n)? ");
					yesno = reader.readLine().toLowerCase();
				} while( !yesno.startsWith("y") && !yesno.startsWith("n") );
				if( yesno.startsWith("n") )
				return;
			}

			List<RemoteFileInfo> list = parse( list(filename) );
			if( list.size() > 1 || list.size()==0 || !list.get(0).name.equals(filename) ) {
				File filepath = file.getParentFile() != null ? file.getParentFile() : new File(".");
				list = parse( list( filepath.getPath() ) );
				boolean found = false, downloaded = false;
				for(RemoteFileInfo entry : list)
					if( entry.name.equals(filename) ) {
						found = true;
						if( mdownload(entry, "./") ) {
							downloaded = true;
						}
					}
				if(found && downloaded)
					System.out.println("Filename \""+filename+"\" download successfull");
				else if( !found )
					System.out.println("Unable to find \""+filename+"\"");
				else if( !downloaded )
					System.out.println("Failed to download \""+filename+"\"");

			}
			else if( list.size() == 1 ) {
				for(RemoteFileInfo entry : list) {
					if( !mdownload(entry, "./") ) {
						System.out.println("Failed to download filename \""+entry.name+"\"");
						return;
					}
					System.out.println("Filename \""+entry.name+"\" download successfull");
				}
			}

		} catch(IOException ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	* Download multiple files
	* @param entry can be either a filename or directory
	*/
	public boolean mdownload(RemoteFileInfo entry, String path) {
		if( entry.dir ) {
			File temp = new File(path + entry.name);
			temp.mkdir();
			path = path + entry.name + "/";
			cwd( entry.name );
			List<RemoteFileInfo> list = parse( list(".") );
			for(RemoteFileInfo listentry : list) {

				mdownload(listentry,path);
			}
			path = path.substring(0,path.length() - (entry.name.length() + 1 ) );
			cwd("..");
			return true;
		}
		else {
			if( download(entry, path) == 0 ) {
				return true;
			}
			return false;
		}
	}

	class threadDownload extends Thread {

		Socket dataSocket;
		BufferedReader serverIn;
		File writeFile;

		FileOutputStream outToFile;

		public threadDownload (String hostIp, int hostPort, String fileName, String path) {
			try {
				//Open a file to write to
				writeFile = new File(path+fileName);
				writeFile.createNewFile();
				//Open socket connection
				dataSocket = new Socket(hostIp, hostPort);
				//Connect socket with a buffer
				serverIn = new BufferedReader( new InputStreamReader(dataSocket.getInputStream() ));
				//Connect file with stream
				outToFile = new FileOutputStream(writeFile);
				success = true;

			}catch(UnknownHostException ex){

<<<<<<< HEAD
				System.err.println(ex.getMessage()+ " " + hostIp + "From threadDownload");
				success = false;
			}catch(IOException ex2){

				System.err.println(ex2.getMessage()+ " " + hostIp + "From threadDownload");
				success = false;
			}catch(Exception ex3){
				System.err.println(ex3.getMessage()+ " " + hostIp + "From threadDownload");
				success = false;
=======
				System.err.println(ex.getMessage() + " " + hostIp + " From threadDownload on port " + hostPort);
				System.exit(0);
			}catch(IOException ex1){

				System.err.println(ex1.getMessage() + " " + hostIp + " From threadDownload on port " + hostPort);
				System.exit(0);
>>>>>>> master
			}
		}

		public void run() {

			lock.lock();
			try {

				int read_len;
    			while( (read_len = serverIn.read()) != -1 ) {
        			outToFile.write(read_len);
    			}
    			outToFile.close();

			    } catch( IOException ex ) {
			    	ex.printStackTrace();
			    }
			lock.unlock();
		}

	}

	/**
	* Return values:
	*  0: success
	* -1: File exists and cannot overwritten
	* -2: download failure
	*/
	public int download(RemoteFileInfo entry, String path) {
		String pasvModeData;
		String hostIp, hexMSB, hexLSB;
		int portMSB, portLSB;
		int hostPort;
		String temp = new String();

		try{
			out.println("TYPE I");
			if (!in.readLine().startsWith("200")){
				System.out.println("Error in setting the ");
			}
		}catch(IOException ex){
			System.out.println(ex.getMessage());
			return -2;
		}
<<<<<<< HEAD
		do{
			lock.lock();
			out.println("PASV");
			try{
				pasvModeData = in.readLine();
			}catch(IOException ex6){
				System.out.println(ex6.getMessage());
				return -2;
			}
			lock.unlock();
			Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(pasvModeData);
=======
		lock.lock();
		out.println("PASV");
		try{
			pasvModeData = in.readLine();
		}catch(IOException ex){
			System.out.println(ex.getMessage());
			return -2;
		}
		lock.unlock();
		Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(pasvModeData);
>>>>>>> master

			while(m.find()){
				temp = temp + m.group(1);
			}
			String[] tokens = temp.split(",");

			hostIp = tokens[0] + "." + tokens[1] + "." + tokens[2] + "." + tokens[3];

			portMSB = Integer.parseInt(tokens[4]);
			portLSB = Integer.parseInt(tokens[5]);

			hexMSB = Integer.toHexString(portMSB);
			hexLSB = Integer.toHexString(portLSB);

			hostPort = Integer.decode("0x" + hexMSB + hexLSB);
			threadS = new threadDownload(hostIp, hostPort, entry.name, path);
		}while(!success);
		threadS.start();

		out.println("RETR " + entry.name);

		try{
			threadS.join();
		}catch(InterruptedException ex){
			System.out.println(ex.getMessage());
			return -2;
		}

		try{
			in.readLine();
			in.readLine();
		}catch(IOException ex){
			System.out.println(ex.getMessage());
			return -2;
		}

		return 0;
	}


	public boolean mkdir(String dirname) {
		out.println("MKD " + dirname);
		try{
			if (in.readLine().startsWith("257")){
				return true;
			}else{
				return false;
			}

		}catch(IOException ex){
			System.out.println(ex.getMessage());
			return false;
		}

	}

	public void mkdirUI() {
		String dirname, socketInput;
		try {
			System.out.print("Enter directory name: ");
			dirname = reader.readLine();

			if( mkdir(dirname) )
			System.out.println("Directory \""+ dirname +"\" created!" );
			else
			System.out.println("Directory creation failed!");
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	public boolean rmdir(String dirname) {
		out.println("RMD " + dirname);
		try{
			if (in.readLine().startsWith("250")){
				return true;
			}else{
				return false;
			}

		}catch(IOException ex){
			System.out.println(ex.getMessage());
			return false;
		}
	}

	public void rmdirUI() {
		String dirname, socketInput;
		try {
			System.out.print("Enter directory name: ");
			dirname = reader.readLine();

			if( rmdir(dirname) )
			System.out.println("Directory \""+ dirname +"\" deleted!" );
			else
			System.out.println("Directory deletion failed!");
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}



	public void deleteUI() {
		String filename, socketInput;
		try {
			System.out.print("Enter file to delete: ");
			filename = reader.readLine();
			File file = new File(filename);

			List<RemoteFileInfo> list = parse( list(filename) );
			if( list.size() > 1 || list.size()==0 || !list.get(0).name.equals(filename) ) {
				File filepath = file.getParentFile() != null ? file.getParentFile() : new File(".");
				list = parse( list( filepath.getPath() ) );
				boolean found = false, deleted = false;
				for(RemoteFileInfo entry : list)
					if( entry.name.equals(filename) ) {
						found = true;
						if( mdelete( entry ) ) {
							deleted = true;
						}
					}
				if(found && deleted)
					System.out.println("Filename \""+filename+"\" deleted successfully");
				else if( !found )
					System.out.println("Unable to find \""+filename+"\"");
				else if( !deleted )
					System.out.println("Failed to delete \""+filename+"\"");

			}
			else if( list.size() == 1 ) {
				for(RemoteFileInfo entry : list) {
					if( !mdelete( entry ) ) {
						System.out.println("Failed to delete filename \""+entry.name+"\"");
						return;
					}
					System.out.println("Filename \""+entry.name+"\" deleted successfully");
				}
			}

		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	/* Delete multiple files in case entry is a directory
	*/
	public boolean mdelete(RemoteFileInfo entry) {
		if( entry.dir ) {
			cwd( entry.name );
			List<RemoteFileInfo> list = parse( list(".") );
			for(RemoteFileInfo listentry : list) {

				mdelete(listentry);
			}
			cwd("..");
			if( !rmdir( entry.name ) ) {
				System.out.println("Deletion of directory \""+entry.name+"\" failed!");
				return false;
			}
			return true;
		}
		else {
			if( !delete( entry.name ) ) {
				System.out.println("Deletion of file \""+entry.name+"\" failed!");
				return false;
			}
			return true;
		}
	}

	public boolean delete(String filename) {

		out.println("DELE " + filename);
		try{
			if (in.readLine().startsWith("250")){
				return true;
			}else{
				return false;
			}

		}catch(IOException ex){
			System.out.println(ex.getMessage());
			return false;
		}
	}


	public void cwdUI() {
		String dirname, socketInput;
		try {
			System.out.print("Enter directory name: ");
			dirname = reader.readLine();
			dbg(null, "Read: "+dirname);

			if( cwd(dirname) )
				System.out.println("Directory changed successfully!");
			else
				System.out.println("Directory change failed!");
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	public boolean cwd(String dirname) {
		out.println("CWD " + dirname);
		try{
			if (in.readLine().startsWith("250")){
				return true;
			}

		}catch(IOException ex){
			System.out.println(ex.getMessage());
			return false;
		}
		return false;
	}

	public void pwdUI() {
		String dirname, socketInput;
		String pwdInfo = pwd();
		System.out.println("PWD: "+pwdInfo);
	}

	public String pwd() {
		out.println("PWD");
		try{
			return in.readLine().substring(4);
		}catch(IOException ex){
			return("Directory failed to be read " + ex.getMessage());
		}

	}

	public void renameUI() {
		try {
			System.out.print("Enter file or directory to rename: ");
			String from = reader.readLine();
			System.out.print("Enter new name: ");
			String to = reader.readLine();

			if( rename(from, to) )
			System.out.println("Rename successfull");
			else
			System.out.println("Rename failed!");
		} catch(IOException ex) {
			ex.printStackTrace();
			return;
		}
	}

	public boolean rename(String from, String to) {

		out.println("RNFR " + from);
		try{
			if (in.readLine().startsWith("350")){
				out.println("RNTO " + to);
				if (in.readLine().startsWith("503")){
					return false;
				}
				return true;
			}else{
				return false;
			}

		}catch(IOException ex){
			System.out.println(ex.getMessage());
			return false;
		}

	}

	public void helpUI() {
		System.out.println("OPTIONS:\n\tCONNECT\tLOGIN\tQUIT\tLIST\tUPLOAD\tDOWNLOAD\n\tRNM\tMKD\tRMD\tCWD\tPWD\tDEL");
	}

	public void checkInput(String command) {
		switch(command.toUpperCase()) {
			case "HELP" :
				helpUI();
			break;
			case "CONNECT" :
				bindUI(null);
			break;
			case "LOGIN" :
				loginUI();
			break;
			case "UP":
			case "UPLOAD" :
				uploadUI();
			break;
			case "DOWN":
			case "DOWNLOAD" :
				downloadUI();
			break;
			case "CWD" :
			case "CD" :
				cwdUI();
			break;
			case "PWD" :
				pwdUI();
			break;
			case "LIST" :
			case "LS" :
				listUI();
			break;
			case "MKD" :
			case "MKDIR" :
				mkdirUI();
			break;
			case "RMD" :
			case "RMDIR" :
				rmdirUI();
			break;
			case "DEL" :
			case "DELE" :
			case "DELETE" :
			case "DLT" :
				deleteUI();
			break;
			case "RENAME":
			case "RNM":
				renameUI();
			break;
			case "Q":
			case "QUIT" :
				System.out.println("Bye bye...");
				System.exit(1);
			break;
			default :
				System.out.println("ERROR: Unknown command \""+command+"\"");
		}
	}

	public static void main(String [] args) {
		FtpClient client = new FtpClient(true, true);
		client.bindUI(args);
		client.loginUI();
		System.out.print("$> ");
		try {
			String userInput;
			while( true ) {
				if( client.reader.ready() ) {
					userInput = client.reader.readLine();
					while( userInput.indexOf(' ') == 0 ) {
						userInput = userInput.substring(1);
					}
					if( userInput.indexOf(' ') < 0 ) {
						client.checkInput(userInput);
					}
					if( userInput.indexOf(' ') > 0 ) {
						client.checkInput( userInput.substring(0, userInput.indexOf(' ')) );
					}
					System.out.print("$> ");
				}
				else {
					Thread.sleep(500);
				}
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

}
