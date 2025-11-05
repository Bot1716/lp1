/*
Problem Statement 6: Round Robin (RR) Scheduling
Write a program to simulate the Round Robin (Preemptive) CPU scheduling algorithm.
The program should take time quantum as input and schedule processes in a cyclic order.
Display the Gantt chart, waiting time, turnaround time, and average values for all processes.
*/
import java.util.*;

public class RoundRobin {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();

        int[] processId = new int[n];
        int[] arrivalTime = new int[n];
        int[] burstTime = new int[n];
        int[] remainingTime = new int[n];
        int[] waitingTime = new int[n];
        int[] turnaroundTime = new int[n];
        int[] completionTime = new int[n];

        for (int i = 0; i < n; i++) {
            processId[i] = i + 1;
            System.out.print("Enter Arrival Time for Process " + (i + 1) + ": ");
            arrivalTime[i] = sc.nextInt();
            System.out.print("Enter Burst Time for Process " + (i + 1) + ": ");
            burstTime[i] = sc.nextInt();
            remainingTime[i] = burstTime[i];
        }

        System.out.print("Enter Time Quantum: ");
        int tq = sc.nextInt();

        int time = 0, completed = 0;
        Queue<Integer> q = new LinkedList<>();
        boolean[] inQueue = new boolean[n];

        System.out.println("\nGantt Chart:");
        while (completed < n) {
            
            for (int i = 0; i < n; i++) {
                if (arrivalTime[i] <= time && !inQueue[i] && remainingTime[i] > 0) {
                    q.add(i);
                    inQueue[i] = true;
                }
            }

            if (q.isEmpty()) {
                time++;
                continue;
            }

            int i = q.poll();
            System.out.print("P" + processId[i] + " | ");

            int execTime = Math.min(remainingTime[i], tq);
            remainingTime[i] -= execTime;
            time += execTime;

            
            for (int j = 0; j < n; j++) {
                if (arrivalTime[j] <= time && !inQueue[j] && remainingTime[j] > 0) {
                    q.add(j);
                    inQueue[j] = true;
                }
            }

            if (remainingTime[i] > 0) {
                q.add(i);
            } else {
                completionTime[i] = time;
                turnaroundTime[i] = completionTime[i] - arrivalTime[i];
                waitingTime[i] = turnaroundTime[i] - burstTime[i];
                completed++;
            }
        }

        System.out.println("\n\nPID\tAT\tBT\tCT\tTAT\tWT");

        float avgTAT = 0, avgWT = 0;
        for (int i = 0; i < n; i++) {
            System.out.println("P" + processId[i] + "\t" + arrivalTime[i] + "\t" + burstTime[i] + "\t" +
                    completionTime[i] + "\t" + turnaroundTime[i] + "\t" + waitingTime[i]);
            avgTAT += turnaroundTime[i];
            avgWT += waitingTime[i];
        }

        System.out.printf("\nAverage Turnaround Time = %.2f", avgTAT / n);
        System.out.printf("\nAverage Waiting Time = %.2f\n", avgWT / n);
    }
}
