import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class sender {

static int pktSentSoFar=0;
	public static void main(String[] args) {
		try{
			String url = "localhost";
			int portNumber = 5000;
			String fileName = "message.txt";
			if(args.length!=0){
				url = args[0];
				portNumber = Integer.parseInt(args[1]);
				fileName = args[2];
			}
			Socket socket = new Socket(url, portNumber);
			//System.out.println(socket);
			DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
			DataOutputStream dataOutputStream= new DataOutputStream(socket.getOutputStream());
			send(dataInputStream,dataOutputStream,fileName);
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	private static void send(DataInputStream dataInputStream, DataOutputStream dataOutputStream,String fileName) throws IOException {
		
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		ArrayList<String> messages = new ArrayList<String>();
 		while((st = br.readLine()) != null){
			//System.out.println(st);
			String arr[] = st.split(" ");
			for(String obj : arr){
				messages.add(obj);
			}
		}
 		int id = 0;
 		int seqNo = 0;
 		int ct = 1;
 		boolean lastMessage = false;
 		for(String obj : messages){
 			String message = Integer.toString(seqNo) + " " +Integer.toString(id) + " " + Integer.toString(checkSum(obj)) + " " + obj;
 			if(id == messages.size()-1)
 				lastMessage = true;
 			while(!sendPacket(dataInputStream,dataOutputStream,message,seqNo,lastMessage)){
 				//ct+=1;
 			}
 			id+=1;
 			seqNo+=1;
 			seqNo%=2;
 		}
 		br.close();
 		sendPacket(dataInputStream, dataOutputStream, "-1", seqNo,lastMessage);
	}

	private static boolean sendPacket(DataInputStream dataInputStream, DataOutputStream dataOutputStream, String message,int seqNo,boolean lastMessage) throws IOException {

		pktSentSoFar+=1;
		dataOutputStream.writeUTF(message);
		if(message.equals("-1"))
			return true;
		String[] response = dataInputStream.readUTF().split(" ");
		if(response[0].equals("2")){
			System.out.println("Waiting ACK"+ seqNo + ", " + pktSentSoFar + ", DROP"  + ", resend Packet" + seqNo);
			return false;
		}
		else if( (response[0].equals("1") && seqNo == 1) || (response[0].equals("0") && seqNo == 0) ){
			if(lastMessage)
				System.out.println("Waiting ACK"+ seqNo + ", " + pktSentSoFar + ", ACK" + response[0] + ", no more packets to send");
			else	
				System.out.println("Waiting ACK"+ seqNo + ", " + pktSentSoFar + ", ACK" + response[0] + ", next Packet to send Packet" + (seqNo+1)%2);
			return true;
		}
		else if( (response[0].equals("1") && seqNo == 0) || (response[0].equals("0") && seqNo == 1) ){
			System.out.println("Waiting ACK"+ seqNo + ", " + pktSentSoFar + ", ACK" + response[0] + ", resend Packet" + seqNo);
			return false;
		}
		else{
			System.out.println("Waiting ACK"+ seqNo + ", " + pktSentSoFar + ", DROP ACK" + response[0] + ", resend Packet" + seqNo);
			return false;
		}
	}

	private static int checkSum(String obj) {
		int ret=0;
		for(int i=0;i<obj.length();i++){
			ret += (obj.charAt(i) - 'a');
		}
		return ret;
	}

}
