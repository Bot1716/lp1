/*
Problem Statement 4: Shortest Job First (SJF – Preemptive) Scheduling
Write a program to simulate the Shortest Job First (SJF – Preemptive) CPU scheduling algorithm. The program should calculate and display the order of execution, waiting time, turnaround time, and their averages for all processes.
Description:
This program simulates the SJF Preemptive scheduling algorithm where the CPU is assigned to the process with the shortest remaining burst time at every time unit. Processes with earlier arrival times and smaller burst times are prioritized. The program tracks remaining time for each process, calculates waiting and turnaround times after completion, and prints the results including average waiting and turnaround times.
*/
import java.util.Scanner;
class sjf{
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter number of processes.");
        int n = sc.nextInt();
        int[] remainingTime = new int[n];
        int[] arrivalTime=new int[n];
        int[] burstTime=new int[n]; 
        int[] completionTime=new int[n];
        int[] turnAroundTime=new int[n];
        int[] waitingTime=new int[n];
        for(int i=0;i<n;i++){
            
            System.out.println("Enter arrival time for process"+(i+1));
            arrivalTime[i]=sc.nextInt();
            System.out.println("Enter burst time for process"+(i+1));
            burstTime[i]=sc.nextInt();
            remainingTime[i]=burstTime[i];
        }
        int completed=0,currentTime=0;
        float totalTAT=0,totalWT=0;
        while(completed!=n){
            int currentProcess=-1;
            int minRemaining=Integer.MAX_VALUE;
            
            for(int i=0;i<n;i++){
                if(arrivalTime[i]<=currentTime && remainingTime[i]<minRemaining &&remainingTime[i]>0){
                    minRemaining=remainingTime[i];
                    currentProcess=i;
                }
            }
            if(currentProcess==-1){
                currentTime++;
                continue;
            }
            remainingTime[currentProcess]--;
            currentTime++;
            if(remainingTime[currentProcess]==0){
                completed++;
                completionTime[currentProcess]=currentTime;
                turnAroundTime[currentProcess]=completionTime[currentProcess]-arrivalTime[currentProcess];
                waitingTime[currentProcess]=turnAroundTime[currentProcess]-burstTime[currentProcess];
                totalTAT+=turnAroundTime[currentProcess];
                totalWT+=waitingTime[currentProcess];
            }
        }
        System.out.println("\nPid\tAT\tBT\tCT\tTAT\tWT");
        for(int i=0;i<n;i++){
            System.out.println("P"+(i+1)+"\t"+arrivalTime[i]+"\t"+burstTime[i]+"\t"+completionTime[i]+"\t"+turnAroundTime[i]+"\t"+waitingTime[i]);
        }
        System.out.println("Average turn around time = "+totalTAT/n);
        System.out.println("Average Waiting time = "+totalWT/n);
    }
}
