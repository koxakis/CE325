#include<stdio.h>
#include<stdlib.h>

int main (int argc, char* argv[]) {

	int i, j;
	char port[5];

	i = atoi(argv[1]);
	j = atoi(argv[2]);

	sprintf(port, "%x", i);
	sprintf(&port[2], "%x", j);

	printf("%s\n", port);

	i = (int) strtol(port, NULL, 16);

	printf("%d\n", i);

	return 0;
}
