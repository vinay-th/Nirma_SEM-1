// Write a C program to implement the CPU scheduling algorithm (FCFS and
// SPN). Take the number of processes as the user input, along with the arrival
// time and service time for each process. Calculate the Turn Around Time(TAT)
// and Waiting Time(WT) for each process. Also, calculate the average TAT and WT.
#include <stdio.h>
#include <stdlib.h>

struct Process {
    int id;
    int at;
    int st;
    int ct;
    int tat; 
    int wt;
    int done;  
};

void FCFS(struct Process *p, int n) {
    int currentTime = 0;
    int totalTAT = 0;
    int totalWT = 0;

    for (int i = 0; i < n; i++) {
        if (currentTime < p[i].at) {
            currentTime = p[i].at;
        }
        p[i].ct = currentTime + p[i].st;
        p[i].tat = p[i].ct - p[i].at;
        p[i].wt = p[i].tat - p[i].st;
        currentTime += p[i].st;

        totalTAT += p[i].tat;
        totalWT += p[i].wt;
    }

    printf("FCFS Scheduling:\n");
    printf("Process ID\tArrival Time\tService Time\tCompletion Time\tTurn Around Time\tWaiting Time\n");
    for (int i = 0; i < n; i++) {
        printf("%d\t\t%d\t\t%d\t\t%d\t\t%d\t\t%d\n", p[i].id, p[i].at, p[i].st, p[i].ct, p[i].tat, p[i].wt);
    }

    printf("\nAverage Turn Around Time: %.2f\n", (float)totalTAT / n);
    printf("Average Waiting Time: %.2f\n", (float)totalWT / n);
}

void SPN(struct Process *p, int n) {
    int currentTime = 0;
    int totalTAT = 0;
    int totalWT = 0;
    int completed = 0;

    while (completed < n) {
        int idx = -1;
        for (int i = 0; i < n; i++) {
            if (!p[i].done && p[i].at <= currentTime) {
                if (idx == -1 || p[i].st < p[idx].st) {
                    idx = i;
                }
            }
        }

        if (idx != -1) {
            p[idx].ct = currentTime + p[idx].st;
            p[idx].tat = p[idx].ct - p[idx].at;
            p[idx].wt = p[idx].tat - p[idx].st;
            currentTime += p[idx].st;
            p[idx].done = 1;

            totalTAT += p[idx].tat;
            totalWT += p[idx].wt;
            completed++;
        } else {
            currentTime++;
        }
    }

    printf("SPN Scheduling:\n");
    printf("Process ID\tArrival Time\tService Time\tCompletion Time\tTurn Around Time\tWaiting Time\n");
    for (int i = 0; i < n; i++) {
        printf("%d\t\t%d\t\t%d\t\t%d\t\t%d\t\t%d\n", p[i].id, p[i].at, p[i].st, p[i].ct, p[i].tat, p[i].wt);
    }

    printf("\nAverage Turn Around Time: %.2f\n", (float)totalTAT / n);
    printf("Average Waiting Time: %.2f\n", (float)totalWT / n);
}

int main(){
    struct Process p[100];
    int n;

    printf("Enter number of processes: ");
    scanf("%d", &n);

    if (n <= 0 || n > 100) {
        printf("Invalid number of processes!\n");
        return 1;
    }

    for (int i = 0; i < n; i++) {
        p[i].id = i + 1;
        p[i].done = 0;

        printf("\nEnter Arrival Time of P%d: ", i + 1);
        scanf("%d", &p[i].at);

        printf("Enter Service Time of P%d: ", i + 1);
        scanf("%d", &p[i].st);

        if (p[i].at < 0 || p[i].st <= 0) {
            printf("Invalid arrival or service time!\n");
            return 1;
        }
    }

    FCFS(p, n);
    SPN(p, n);

    return 0;
}