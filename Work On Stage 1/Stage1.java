import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Stage1 {
    public static void main(String[] args) {
        try {
            Socket s = new Socket("127.0.0.1", 50000);
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());

            BufferedReader bin = new BufferedReader(new InputStreamReader(s.getInputStream()));

            // System.out.println("Target IP: " + s.getInetAddress() + "Target Port: " +
            // s.getPort());
            // System.out.println("Local IP: " + s.getLocalAddress() + "Local Port: " +
            // s.getLocalPort());

            String name = System.getProperty("user.name"); // gets the username information from system

            dout.write(("HELO\n").getBytes()); // Client Sends HELO to server
            dout.flush();
            // System.out.println("Sent: Helo");

            String str = bin.readLine(); // Displays Response Should say RCVD: OK
            // System.out.println("RCVD: " + str);

            dout.write(("AUTH " + name + "\n").getBytes()); // sends auth with name from username
            dout.flush();
            str = bin.readLine();
            // System.out.println("AUTH: " + str);

            while (true) {

                dout.write(("REDY\n").getBytes()); // Sends redy mesg to recive JOBN details
                dout.flush();
                str = bin.readLine();
                // System.out.println("REDY: " + str);

                String[] jobInfo = str.split(" "); // Splits the JOBN string into different array segments
                String responseType = jobInfo[0]; // checks to see if its NONE, JCPL OR JOBN
                if (responseType.equals("NONE")) {
                    break;
                } else if (responseType.equals("JCPL")) {
                    continue;
                } else if (!responseType.equals("JOBN")) {
                    continue;
                }
                int coresRequired = Integer.parseInt(jobInfo[4]); // parse info for job cores required
                int memoryRequired = Integer.parseInt(jobInfo[5]); // parse info for job memory requried
                int diskRequired = Integer.parseInt(jobInfo[6]); // parse info for job diskspace required
                int JobID = Integer.parseInt(jobInfo[2]); // parse info for job id

                String largestType = ""; // Largest type of server will be an unknown string.
                int largestServerSize = 0; // initialise following variables.
                int largestServerSizeAmt = 0;
                int largestQty = 0;
                int serverID = 0;
                int serverAmount = 0; // Amount of servers
                int serverSize = 0; // Size of cores
                String serverType = "";

                while (largestServerSize == 0) {
                    dout.write(
                            ("GETS Capable " + coresRequired + " " + memoryRequired + " " + diskRequired + "\n")
                                    .getBytes()); // Gets variables from JOBN To ensure that it can run all jobs within
                                                  // a server.
                    dout.flush();
                    str = bin.readLine();
                    // System.out.println("Response: " + str);

                    String[] getsResponse = str.split(" "); // Gets Data Response and splits it
                    int numberOfRecords = Integer.parseInt(getsResponse[1]);
                    if (numberOfRecords == 0) { // If Number of record is 0 cmd ok to continue process.
                        dout.write(("OK\n").getBytes());
                        dout.flush();
                        str = bin.readLine();
                        // System.out.println("OK : " + str);
                        continue;
                    }

                    dout.write(("OK\n").getBytes()); // OK Again as it needs two oks to proceed process
                    dout.flush();

                    for (int i = 0; i < numberOfRecords; i++) { // itterate through the number of jobs

                        str = bin.readLine();
                        String[] serverInfo = str.split(" "); // Read and split server records
                        serverType = serverInfo[0]; // set the new server type
                        serverAmount = Integer.parseInt(serverInfo[1]); // set new server amount
                        serverSize = Integer.parseInt(serverInfo[4]); // set new server size
                        if (serverSize >= largestServerSize) {
                            largestServerSize = serverSize;
                            largestType = serverInfo[0]; // Basically sets the biggest server type
                        }

                        // System.out.println(serverType + ": serverType\n");
                        // System.out.println(serverAmount + ": serverAmount\n");
                        // System.out.println(serverSize + ": serverSize\n");
                        // System.out.println(typeId + ": typeId\n");

                        // int serverSize = Integer.parseInt(serverInfo[4]); // size of cores
                        // int serverAmount = Integer.parseInt(serverInfo[1]);// amount of servers
                        // // String serverType = (serverInfo[0]);
                        // // System.out.println(serverType + ": serverType\n");
                        // System.out.println(serverAmount + ": serverAmount\n");
                        // System.out.println(serverSize + ": serverSize\n");

                        // if (serverSize >= largestServerSize) {
                        // largestServerSize = serverSize;
                        // largestType = serverInfo[0]; // Basically sets the biggest server type
                        // }
                        // if (largestType == "t2.aws") {
                        // serverID = 222;
                        // }
                        // if (largestType == "xlarge") {
                        // serverID = 999;

                        // }

                        // if (Integer.parseInt(serverInfo[4]) > serverSize) {
                        // // serverType = serverInfo[0]
                        // serverSize = Integer.parseInt(serverInfo[4]);
                        // serverAmount = Integer.parseInt(serverInfo[1]);
                        //

                        // if (serverSize >= largestServerSize) {
                        // largestServerSize = serverSize;
                        // largestType = serverInfo[0]; // Basically sets the biggest server type
                        // }
                        // if (serverAmount == 1) {
                        // serverID = 0;
                        // } else {
                        // serverID = 1;
                        // }

                    }
                    // System.out.println("largestServer: " + largestType + ", " +
                    // largestServerSize);
                    dout.write(("OK\n").getBytes());
                    dout.flush();
                    str = bin.readLine();
                    // System.out.println("OK : " + str);
                    if (largestServerSize == 0) {

                        // System.out.println("No Avail servers off in time 1 sec ");
                        Thread.sleep(1000);
                    }
                }
                if (responseType.equals("JOBN")) { // IF JOBN write JOBID, LARGEST TYPE and serverid
                    // System.out.println("SCHD");
                    dout.write(("SCHD " + JobID + " " + largestType + " " + serverID + "\n").getBytes());
                    dout.flush();
                    str = bin.readLine();
                    // System.out.println("SCHD : " + str);

                } else if (responseType.equals("NONE")) { // Break out of loop if non achieved
                    break;
                }

            }
            dout.write(("QUIT\n").getBytes()); // Sends Quit command to gracefully Exit
            dout.flush();
            str = bin.readLine();
            // System.out.println("QUIT : " + str);
            bin.close();
            dout.close();
            s.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}