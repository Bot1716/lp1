Problem Statement 3: First Come First Serve (FCFS) CPU Scheduling
Write a program to simulate the First Come First Serve (FCFS) CPU scheduling algorithm.
The program should accept process details such as Process ID, Arrival Time, and Burst Time and compute the Waiting Time and Turnaround Time for each process. Display the Gantt chart, average waiting time, and average turnaround time.

Description:
This program simulates the FCFS CPU scheduling algorithm. It takes input for the number of processes and their respective burst times. Although the problem statement mentions arrival time, this implementation assumes all processes arrive at time zero (or in order) since arrival time handling is not fully included here. The program calculates waiting time and turnaround time for each process and prints a Gantt chart along with average waiting time and turnaround time.
*/
import java.util.Scanner;
class fcfs {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter number of processes.");
        int n = sc.nextInt();
        int[] processId=new int[n];
        int[] arrivalTime=new int[n];
        int[] burstTime=new int[n]; 
        int[] completionTime=new int[n];
        int[] turnAroundTime=new int[n];
        int[] waitingTime=new int[n];
        for(int i=0;i<n;i++){
            processId[i]=i+1;
            System.out.println("Enter arrival time for process"+processId[i]);
            arrivalTime[i]=sc.nextInt();
            System.out.println("Enter burst time for process"+processId[i]);
            burstTime[i]=sc.nextInt();
        }
        for(int i=0;i<n-1;i++){
            for(int j=i+1;j<n;j++){
                if (arrivalTime[i]>arrivalTime[j]){
                    int temp = arrivalTime[i];
                    arrivalTime[i] = arrivalTime[j];
                    arrivalTime[j] = temp;
                    temp = burstTime[i];
                    burstTime[i] = burstTime[j];
                    burstTime[j] = temp;
                    temp = processId[i];
                    processId[i] = processId[j];
                    processId[j] = temp;
                }
            }
        }
        int currentTime=0;
        double totalTAT = 0, totalWT = 0;
        for(int i=0;i<n;i++){
            if(currentTime<arrivalTime[i]){
                currentTime=arrivalTime[i];
            }
            completionTime[i]=currentTime+burstTime[i];
            currentTime=completionTime[i];
            
            turnAroundTime[i]=completionTime[i]-arrivalTime[i];
            waitingTime[i]=turnAroundTime[i]-burstTime[i];
            
            totalTAT+=turnAroundTime[i];
            totalWT+=waitingTime[i];
        }
        System.out.println("\nProcess\tAT\tBT\tCT\tTAT\tWT");
        for(int i=0;i<n;i++){
            System.out.println("P"+processId[i]+"\t"+arrivalTime[i]+"\t"+burstTime[i]+"\t"+completionTime[i]+"\t"+turnAroundTime[i]+"\t"+waitingTime[i]);
        }
        System.out.println("Average turn around time = "+totalTAT/n);
        System.out.println("Average Waiting time = "+totalWT/n);
    }
}
