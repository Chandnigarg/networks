import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;


public class network extends Thread{
	
	static DataInputStream receiverInput;
	static DataOutputStream receiverOuput;
	static DataInputStream senderInput;
	static DataOutputStream senderOutput;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		int portNumer = 5000;
		if(args.length != 0){
			portNumer = Integer.parseInt(args[0]);
		}
		ServerSocket serverSocket = new ServerSocket(portNumer);
		establishConnection(serverSocket);
	}

	private static void establishConnection(ServerSocket serverSocket) throws IOException, InterruptedException {
		Socket receiver = serverSocket.accept();
		//System.out.println("receiver connected");
		Socket sender = serverSocket.accept();
		//System.out.println("sender connected");
		receiverInput = new DataInputStream(receiver.getInputStream());
		receiverOuput = new DataOutputStream(receiver.getOutputStream());
		senderInput = new DataInputStream(sender.getInputStream());
		senderOutput = new DataOutputStream(sender.getOutputStream());
		
		
		Thread t1 = new Thread(){
			public void run(){
				try {
					control(receiverInput, receiverOuput, senderInput, senderOutput);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		
		t1.start();
		
		t1.join();
		//control(receiverInput,receiverOuput,senderInput,senderOutput);
	}

	private static void control(DataInputStream receiverInput, DataOutputStream receiverOuput,
			DataInputStream senderInput, DataOutputStream senderOutput) throws IOException, InterruptedException {
		
		while(true){
			String[] messageObjects = senderInput.readUTF().split(" ");
			if(messageObjects[0].equals("-1"))
				break;
			System.out.print("Received: Packet" + messageObjects[0] + ", " + messageObjects[1] + ", ");
			Random r = new Random();
			Double probability = r.nextDouble();
			if(probability <= 0.5){
				String message = messageObjects[0] + " " + messageObjects[1] + " " + messageObjects [2] + " " + messageObjects[3];
				receiverOuput.writeUTF(message);
				System.out.println("PASS");
				//control2(receiverInput,receiverOuput,senderInput,senderOutput);
				Thread t2 = new Thread(){
					public void run(){
						try {
							control2(receiverInput, receiverOuput, senderInput, senderOutput);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				t2.start();
				t2.join();
			}
			else if(probability > 0.5 && probability<=0.75){
				messageObjects[2] = Integer.toString(Integer.parseInt(messageObjects[2]) + 1);
				String message = messageObjects[0] + " " + messageObjects[1] + " " + messageObjects [2] + " " + messageObjects[3];
				receiverOuput.writeUTF(message);
				System.out.println("CORRUPT");
				//control2(receiverInput,receiverOuput,senderInput,senderOutput);
				Thread t2 = new Thread(){
					public void run(){
						try {
							control2(receiverInput, receiverOuput, senderInput, senderOutput);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				t2.start();
				t2.join();
			}
			else{
				messageObjects[0]  = "2";
				String message = messageObjects[0] + " " + messageObjects[1] + " " + messageObjects [2] + " " + messageObjects[3];
				senderOutput.writeUTF(message);
				System.out.println("DROP");
				//control(receiverInput,receiverOuput,senderInput,senderOutput);
			}
		}
		receiverOuput.writeUTF("-1");
		
	}

	private static void control2(DataInputStream receiverInput, DataOutputStream receiverOuput,
			DataInputStream senderInput, DataOutputStream senderOutput) throws IOException {
		Random r = new Random();
		Double probability = r.nextDouble();
		String message = receiverInput.readUTF();
		System.out.print("Received: ACK" + message + ", ");
		if(probability <= 0.75){
			System.out.println("PASS");
			senderOutput.writeUTF(message);
		}
		else{
			System.out.println("DROP");
			senderOutput.writeUTF("DROP");
		}
		
	}

}
