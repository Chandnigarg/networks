import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class receiver {

	public static void main(String[] args) {
		try{
			String url = "localhost";
			int portNumber = 5000;
			if(args.length!=0){
				url = args[0];
				portNumber = Integer.parseInt(args[1]);
			}
			Socket socket = new Socket(url, portNumber);
			//System.out.println(socket);
			DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
			DataOutputStream dataOutputStream= new DataOutputStream(socket.getOutputStream());
			receive(dataInputStream,dataOutputStream);
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	private static void receive(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
		int seqNo = 0;
		int pktReceivedSoFar = 0;
		int ackNo = 0;
		ArrayList<String> messageReceived = new ArrayList<String>();
		int oldId = -1;
		while(true){
			String[] response = dataInputStream.readUTF().split(" ");
			if(response[0].equals("-1"))
				break;
			boolean check = verify(response[2],response[3]);
			if(check){
				ackNo = seqNo;
				if(oldId != Integer.parseInt(response[1])){
					messageReceived.add(response[3]);
					oldId = Integer.parseInt(response[1]);
				}
			}
			else
				ackNo = (seqNo + 1)%2;
			System.out.println("Waiting " + seqNo + ", " + pktReceivedSoFar + ", " + response[0] + " " + response[1] + " " + response[2]
					+ " " + response[3] + ", ACK" + ackNo);
			
			dataOutputStream.writeUTF(Integer.toString(ackNo));
			pktReceivedSoFar+=1;
			if(check)
				seqNo = (seqNo + 1)%2;
		}
		System.out.print("Message:");
		for(String message : messageReceived){
			System.out.print( " " + message);
		}
	}

	private static boolean verify(String checkSum, String message) {
		int ret = 0;
		for(int i=0;i<message.length();i++){
			ret+= (message.charAt(i) - 'a');
		}
		if(ret == Integer.parseInt(checkSum))
			return true;
		return false;
	}

}
