#include <stdio.h>

int queue[5];
int front = -1;
int rear = -1;

void enqueue(int value){
    if((rear + 1) % 5 == front){
        printf("Queue is overflow\n");
        return;
    }
    if(front == -1){
        front = 0;
    }
    rear = (rear + 1) % 5;
    queue[rear] = value;
}

void dequeue(){
    if(front == -1){
        printf("Queue is underflow\n");
        return;
    }
    if(front == rear){
        front = -1;
        rear = -1;
    } else {
        front = (front + 1) % 5;
    }
}

void display(){
    if(front == -1){
        printf("Queue is empty\n");
        return;
    }
    int i = front;
    while(1){
        printf("%d ", queue[i]);
        if(i == rear){
            break;
        }
        i = (i + 1) % 5;
    }
    printf("\n");
}

int main(){
    enqueue(10);
    enqueue(20);
    enqueue(30);
    enqueue(40);
    enqueue(50);

    display(); // Output: 10 20 30 40 50
    dequeue(); // Remove 10
    enqueue(60); // Add 60, should wrap around
    display(); // Output: 20 30 40 50 60 Array : [60, 20, 30, 40, 50]


    // Display the entire array to show the circular nature of the queue
    // for(int i = 0; i < 5; i++){
    //     printf("%d ", queue[i]); // Output: 60 20 30 40 50
    // }


    printf("\n");
    return 0;
}