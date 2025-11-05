/*
Problem Statement 5: Priority Scheduling (Non-Preemptive)
Write a program to simulate the Priority Scheduling (Non-Preemptive) algorithm.
Each process should have an associated priority value, and the scheduler should select the process with the highest priority for execution next.
Compute and display the waiting time, turnaround time, and average times for all processes.
*/
import java.util.Scanner;
class priority {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter number of processes.");
        int n = sc.nextInt();
        int[] processId=new int[n];
        int[] arrivalTime=new int[n];
        int[] burstTime=new int[n]; 
        int[] priority=new int[n]; 
        int[] completionTime=new int[n];
        int[] turnAroundTime=new int[n];
        int[] waitingTime=new int[n];
        boolean[] isCompleted = new boolean[n];
        for(int i=0;i<n;i++){
            processId[i]=i+1;
            System.out.println("Enter arrival time for p"+processId[i]);
            arrivalTime[i]=sc.nextInt();
            System.out.println("Enter burst time for p"+processId[i]);
            burstTime[i]=sc.nextInt();
            System.out.println("Enter priority for p"+processId[i]);
            priority[i]=sc.nextInt();
        }
        int completed=0,currentTime=0;
        float totalTAT=0,totalWT=0;
        while(completed!=n){
            int currentProcess=-1;
            int highestPriority=Integer.MAX_VALUE;
            for(int i = 0;i<n;i++){
                if(arrivalTime[i] <= currentTime && !isCompleted[i]){
                    if(priority[i]<highestPriority){
                        highestPriority=priority[i];
                        currentProcess=i;
                    }
                }
            }
            if(currentProcess==-1){
                currentTime++;
                continue;
            }
            currentTime+=burstTime[currentProcess];
            completionTime[currentProcess]=currentTime;
            turnAroundTime[currentProcess]=completionTime[currentProcess]-arrivalTime[currentProcess];
            waitingTime[currentProcess]=turnAroundTime[currentProcess]-burstTime[currentProcess];
            isCompleted[currentProcess]=true;
            totalTAT+=turnAroundTime[currentProcess];
            totalWT+=waitingTime[currentProcess];
            completed++;
        }
        System.out.println("\nPid\tAT\tBT\tPr\tCT\tTAT\tWT");
        for(int i=0;i<n;i++){
            System.out.println("P"+processId[i]+"\t"+arrivalTime[i]+"\t"+burstTime[i]+"\t"+priority[i]+"\t"+completionTime[i]+"\t"+turnAroundTime[i]+"\t"+waitingTime[i]);
        }
        System.out.println("Average turn around time = "+totalTAT/n);
        System.out.println("Average Waiting time = "+totalWT/n);
    }
}
