import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Stage2 {
    public static void main(String[] args) {
        try {
            Socket s = new Socket("127.0.0.1", 50000);
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());

            BufferedReader bin = new BufferedReader(new InputStreamReader(s.getInputStream()));

            String name = System.getProperty("user.name"); // gets the username information from system

            dout.write(("HELO\n").getBytes()); // Client Sends HELO to server
            dout.flush();

            String str = bin.readLine(); // Displays Response Should say RCVD: OK

            dout.write(("AUTH " + name + "\n").getBytes()); // sends auth with name from username
            dout.flush();
            str = bin.readLine();

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
                int largestServerSize = -1; // initialise following variables.
                int serverWaiting = -1;
                int largestserverWaiting = Integer.MAX_VALUE;
                int largestServerMemory = 0;
                int largestServerSizeAmt = 0;
                int serverAmount = 0; // Amount of servers
                int serverSize = 0; // Size of cores
                String serverType = "";

                while (largestServerSize == -1) {
                    dout.write(
                            ("GETS Capable " + coresRequired + " " + memoryRequired + " " + diskRequired + "\n")
                                    .getBytes()); // Gets variables from JOBN To ensure that it can run all jobs within
                                                  // a server.
                    dout.flush();
                    str = bin.readLine();

                    String[] getsResponse = str.split(" "); // Gets Data Response and splits it
                    int numberOfRecords = Integer.parseInt(getsResponse[1]);
                    if (numberOfRecords == 0) { // If Number of record is 0 cmd ok to continue process.
                        dout.write(("OK\n").getBytes());
                        dout.flush();
                        str = bin.readLine();
                        continue;
                    }

                    dout.write(("OK\n").getBytes()); // OK Again as it needs two oks to proceed process
                    dout.flush();

                    for (int i = 0; i < numberOfRecords; i++) { // itterate through the number of jobs

                        str = bin.readLine();
                        String[] serverInfo = str.split(" "); // Read and split server records
                        // System.out.println(str);
                        serverType = serverInfo[0]; // set the new server type
                        serverAmount = Integer.parseInt(serverInfo[1]); // set new server amount
                        serverSize = Integer.parseInt(serverInfo[4]); // set new server size
                        int serverMemory = Integer.parseInt(serverInfo[6]); // set server memory
                        serverWaiting = Integer.parseInt(serverInfo[7]); // jobs in queue for server
                        int serverCurrentJob = Integer.parseInt(serverInfo[8]); // jobs currently being run on server
                        serverWaiting += serverCurrentJob; // adding current running jobs to jobs in queue

                        if (serverWaiting < largestserverWaiting // if this servers queue is shorter then what we have
                                                                 // seen or
                                || (serverWaiting == largestserverWaiting && serverSize > largestServerSize)
                                || (serverWaiting == largestserverWaiting && serverSize == largestServerSize
                                        && serverMemory > largestServerMemory)) {
                            // the queue is equal shortest and its a bigger server then we store information
                            // to queue the job.
                            largestserverWaiting = serverWaiting; // Remember how short the queue is
                            largestServerSizeAmt = serverAmount;// remember the best servers id
                            largestServerSize = serverSize;// remember the best servers size
                            largestType = serverType; // Basically sets the biggest server type
                            largestServerMemory = serverMemory; // remember best server memory

                        }

                    }
                    dout.write(("OK\n").getBytes());
                    dout.flush();
                    str = bin.readLine();
                }

                if (responseType.equals("JOBN")) { // IF JOBN write JOBID, LARGEST TYPE and serverid
                    dout.write(("SCHD " + JobID + " " + largestType + " " + largestServerSizeAmt + "\n").getBytes());
                    dout.flush();
                    str = bin.readLine();

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