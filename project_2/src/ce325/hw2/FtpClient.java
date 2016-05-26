package ce325.hw2;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class FtpClient {
	Socket controlSocket;
	BufferedReader reader;
	PrintWriter out;
	BufferedReader in;
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
			System.err.println("Don't know about host " + inetAddress);
			return false;
		}catch(IOException ex2){
			System.err.println("Couldn't get I/O for the connection to " + inetAddress);
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
				this.info = arg_info;
				dataSocket = new Socket(hostIp, hostPort);
				serverIn = new BufferedReader( new InputStreamReader(dataSocket.getInputStream() ));
			}catch(UnknownHostException ex){

				System.err.println("Don't know about host " + hostIp);
			}catch(IOException ex2){

				System.err.println(ex2.getMessage()+ " " + hostIp );
			}
		}

		public void run() {

			lock.lock();
			try{
				while(( temp = serverIn.readLine()) != null){

					info.append(temp);
					info.append("\n");
				}

			}catch(IOException ex6){
				System.out.println(ex6.getMessage());
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

		out.println("PASV");
		try{
			pasvModeData = in.readLine().substring(27);
		}catch(IOException ex6){
			System.out.println(ex6.getMessage());
			return "error";
		}
		pasvModeData = pasvModeData.substring(0, pasvModeData.length() - 2);
		String[] tokens = pasvModeData.split(",");
		hostIp = tokens[0] + "." + tokens[1] + "." + tokens[2] + "." + tokens[3];

		portMSB = Integer.parseInt(tokens[4]);
		portLSB = Integer.parseInt(tokens[5]);

		hexMSB = Integer.toHexString(portMSB);
		hexLSB = Integer.toHexString(portLSB);

		hostPort = Integer.decode("0x" + hexMSB + hexLSB);

		threadS = new threadSocket(hostIp, hostPort, info);
		threadS.start();

		out.println("LIST " + path);
		try{
			threadS.join();
		}catch(InterruptedException ex7){
			System.out.println(ex7.getMessage());
			return "error";
		}
		try{
			/*System.out.println(in.readLine());
			System.out.println(in.readLine());
			Borat
			*/
			in.readLine();
			in.readLine();
		}catch(IOException ex8){
			System.out.println(ex8.getMessage());
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
		String parentDir;

		public RemoteFileInfo(String line) {

			int i = 8;

			String[] tokens = line.split("\\s+");

			this.permissions(tokens[0]);

			size = Long.parseLong(tokens[4]);

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

	public List<RemoteFileInfo> parse(String info) {

		List<RemoteFileInfo> list = new LinkedList<RemoteFileInfo>();
		String[] tokens = info.split("\\n");

		if (info != null && !info.isEmpty()){
			for(String strLine : tokens){

				RemoteFileInfo data = new RemoteFileInfo(strLine);
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
			mupload(file);
		} catch(IOException ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	* Upload multiple files
	* @param f can be either a local filename or local directory
	*/
	public void mupload(File f) {

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

			/*if (mdownload()){
				System.out.println("Download success");
			}else{
				System.out.println("Download falid");
			}*/

		} catch(IOException ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	* Download multiple files
	* @param entry can be either a filename or directory
	*/
	public boolean mdownload(RemoteFileInfo entry) {
		return false;
	}

	/**
	* Return values:
	*  0: success
	* -1: File exists and cannot overwritten
	* -2: download failure
	*/
	public int download(RemoteFileInfo entry) {
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

		}catch(IOException ex11){
			System.out.println(ex11.getMessage());
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

		}catch(IOException ex11){
			System.out.println(ex11.getMessage());
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

		}catch(IOException ex10){
			System.out.println(ex10.getMessage());
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

		}catch(IOException ex5){
			System.out.println(ex5.getMessage());
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
		}catch(IOException ex4){
			return("Directory failed to be read " + ex4.getMessage());
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

		}catch(IOException ex9){
			System.out.println(ex9.getMessage());
			return false;
		}

	}

	public void helpUI() {
		System.out.println("OPTIONS:\n\tRNM\tLOGIN\tQUIT\tLIST\tUPLOAD\tDOWNLOAD\n\tMKD\tRMD\tCWD\tPWD\tDEL");
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
			case "UPLOAD" :
				uploadUI();
			break;
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
