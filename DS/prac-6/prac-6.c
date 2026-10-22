// Implement a music playlist application using the appropriate
// data structure. There is no estimation for the number of music
// files to be managed by this application. Users are expected to
// frequently add new music files and/or remove existing ones.
// Support functionalities for (a) playing the next file, (b) playing
// the previous file, (c) playing the first file and (d) playing the
// last file, and (e) playing the specific file.
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

struct Music {
    char title[100];
    struct Music* next;
    struct Music* prev;
};

struct Music* head = NULL;
struct Music* tail = NULL; 

struct Music* current = NULL;

void addMusic(char title[]) {
    struct Music* newMusic = (struct Music*)malloc(sizeof(struct Music));
    strcpy(newMusic->title, title);
    newMusic->next = NULL;
    newMusic->prev = NULL;

    if (head == NULL) {
        head = newMusic;
        tail = newMusic;
    } else {
        struct Music* temp = head;
        while (temp->next != NULL) {
            temp = temp->next;
        }
        temp->next = newMusic;
        newMusic->prev = temp;
        tail = newMusic;
    }
}

void playNext() {
    if (current == NULL) {
        printf("No music to play.\n");
        return;
    }
    if (current->next != NULL) {
        current = current->next;
        printf("Playing next: %s\n", current->title);
    } else {
        printf("This is the last music file.\n");
    }
}


void playPrevious() {
    if (current == NULL) {
        printf("No music to play.\n");
        return;
    }
    if (current->prev != NULL) {
        current = current->prev;
        printf("Playing previous: %s\n", current->title);
    } else {
        printf("This is the first music file.\n");
    }
}

void playFirst() {
    if (head == NULL) {
        printf("No music to play.\n");
        return;
    }
    current = head;
    printf("Playing first: %s\n", current->title);
}

void playLast(){
    if(head == NULL){
        printf("No music to play.\n");
        return;
    }
    current = tail;
    printf("Playing last: %s\n", current->title);
}

void displayPlaylist() {
    struct Music* temp = head;
    if (temp == NULL) {
        printf("No music in the playlist.\n");
        return;
    }
    printf("Music Playlist:\n");
    while (temp != NULL) {
        if(temp == current) {
            printf("-> ");
        } else {
            printf("   ");
        }
        printf("%s\n", temp->title);
        temp = temp->next;
    }
}

int main(){
    int choice;
    char title[100];

    while (1) {
        printf("\nMusic Playlist Application\n");
        printf("1. Add Music\n");
        printf("2. Play Current\n");
        printf("3. Play Next\n");
        printf("4. Play Previous\n");
        printf("5. Play First\n");
        printf("6. Display Playlist\n");
        printf("7. Exit\n");
        printf("Enter your choice: ");
        scanf("%d", &choice);

        switch (choice) {
            case 1:
                printf("Enter music title: ");
                scanf(" %[^\n]s", title);
                addMusic(title);
                break;
            case 2:
                if(current == NULL && head != NULL) {
                    current = head;
                }
                if(current == NULL) {
                    printf("No music is currently playing.\n");
                } 
                if (current != NULL) {
                    printf("Playing current: %s\n", current->title);
                }
                break;
            case 3:
                playNext();
                break;
            case 4:
                playPrevious();
                break;
            case 5:
                playFirst();
                break;
            case 6:
                displayPlaylist();
                break;
            case 7:
                exit(0);
            default:
                printf("Invalid choice! Please try again.\n");
        }
    }       
    return 0;
}