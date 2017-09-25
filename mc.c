/**
 * mc.c
 * By Henry Zhu (henryzhu@seas.upenn.edu) and Jessica Yingjiao Li
 */

#include "lc4libc.h"


/************************************************
 *  DATA STRUCTURES FOR GAME STATE
 ***********************************************/

/** Width and height of lc4 */
#define SCREEN_WIDTH	  	128
#define SCREEN_HEIGHT     	124

/** Number of cities and missile commands in the game */
#define NUMOFCITIES 		 2
#define NUMOFMISSILECOMMANDS 1

/** Number of targets meteors can target */
#define NUMOFTARGETS 		 NUMOFCITIES + NUMOFMISSILECOMMANDS

/** Delay before another meteor appears */
static int delay_counter =	 0;
#define DELAY 				 5

/** wave_counter counts of the wave that the player is on */
static int wave_counter = 	 1;

/** Number of meteors per wave */
static int meteors_left_per_wave;
static int meteors_per_wave;
#define NUM_METEORS_PER_WAVE 5;

/** Cooldown timer between missile launches */
static int cooldown_counter =0;
#define COOLDOWN_DELAY 		 5

/** Number of maximum missiles and meteors allowed on the screen */
#define MAX_MISSILES 		 3
#define MAX_METEORS 		 3

/** Number of missiles allowed per wave*/
#define MISSILES_PER_WAVE	 8

/** The y position of where the cities and missile commands are located */
#define GROUND_LEVEL 		116

/** The x positions of where the cities and missile commands are located */
#define LEFT_CITY_XPOS		20
#define RIGHT_CITY_XPOS 	100
#define MISSILE_COMMAND_XPOS 60

/** The squared distance where a meteor will blow up if it is in the raidus */
#define BLOWUP_RADIUS		250

/** Delay between GETC_TIMER */
#define GETC_TIMER_DELAY    10

/** 2D array presenting the cursor */
 lc4uint cursorImage[] = {
 	0x00,
 	0x00,
 	0x18,
 	0x3C,
 	0x3C,
 	0x18,
 	0x00,
 	0x00
 };

/** Array of targets that the meteors will target */
lc4uint targets[NUMOFTARGETS];

/** City struct that consists of the x position and 2D array representing the city's image */
typedef struct {
 	lc4bool isDestroyed;
	lc4uint x;
	lc4uint cityImage[8];
}City;

/** Array consisting of cities */
City cities[NUMOFCITIES];

/** Missile Command Struct that consists of the number missiles that the command has left,
 its x position and 2D array representing the missile command's image */
typedef struct {
 	lc4bool isDestroyed;
	int missilesLeft;
	int x;
 	lc4uint commandImage[8];
}MissileCommand;

/** Instance of the MissileCommand Struct */
MissileCommand missileCommand;

/************************************************
 *  Projectile Struct
 *  lc4bool isDestroyed - boolean representing if the projectile is destroyed or not
 *  lc4uint fromX, fromY - uints representing the projectile's starting location when the projectile is fired
 *  lc4uint currentX, currentY - uints representing the projectile's current location from the starting location
 *  lc4uint fromX, fromY - uints representing the projectile's final location where the projectile will explode 
 *  int dx1, dx2, dy1, dx2, - ints representing the direction the projectile will travel
 *  int startLongest, longest, shortest, numerator - each is a part of the line algorithm
 ***********************************************/
typedef struct {
 	lc4bool isDestroyed;
 	lc4uint fromX, fromY, currentX, currentY, finalX, finalY;
 	int dx1;
 	int dy1;
 	int dx2;
 	int dy2;
 	int startLongest;
 	int longest;
 	int shortest;
 	int numerator;
}Projectile;

/** Array consisting of missiles on the screen */
Projectile missiles[MAX_MISSILES];

/** Array consisting of meteors on the screen */
Projectile meteors[MAX_METEORS];

/** Cursor struct consisting of the cursor's x and y position */
typedef struct{
 	int x;
 	int y;
}Cursor;

/** Instance of the cursor*/
Cursor cursor;

/************************************************
 *  Printnum - Useful for debugging
 *  Prints out the value on the lc4
 ***********************************************/

void printnum (int n) {
  int abs_n;
  char str[10], *ptr;

  // Corner case (n == 0)
  if (n == 0) {
    lc4_puts ((lc4uint*)"0");
    return;
  }
 
  abs_n = (n < 0) ? -n : n;

  // Corner case (n == -32768) no corresponding +ve value
  if (abs_n < 0) {
    lc4_puts ((lc4uint*)"-32768");
    return;
  }

  ptr = str + 10; // beyond last character in string

  *(--ptr) = 0; // null termination

  while (abs_n) {
    *(--ptr) = (abs_n % 10) + 48; // generate ascii code for digit
    abs_n /= 10;
  }

  // Handle -ve numbers by adding - sign
  if (n < 0) *(--ptr) = '-';

  lc4_puts((lc4uint*)ptr);
}

void endl () {
  lc4_puts((lc4uint*)"\n");
}

/************************************************
 *  UpdateLine - 
 *  Draws the line from its start location to the current location if the projectile is not destroyed 
 ***********************************************/
 void UpdateLine(Projectile projectile, lc4uint color) 
 {
 	if(projectile.currentX == projectile.finalX && projectile.currentY == projectile.finalY) return;
 	lc4_draw_line(projectile.fromX, projectile.fromY, projectile.currentX, projectile.currentY, color);
 }

/************************************************
 * DrawCursor - 
 * Draws the cursor sprite in white 
 ***********************************************/
 void DrawCursor() 
 {
 	lc4_draw_sprite(cursor.x, cursor.y, (lc4uint)WHITE, &cursorImage[0]);
 }

/************************************************
 *  DrawMissileCommand - 
 *  Draws the missile command in white based on the number of missiles left
 ***********************************************/
 void DrawMissileCommand()
 {
 	if(missileCommand.missilesLeft == 8) {
 		missileCommand.commandImage[0] = 0x42;
 		missileCommand.commandImage[1] = 0xA5;
 		missileCommand.commandImage[2] = 0x42;
 		missileCommand.commandImage[3] = 0xA5;
 		missileCommand.commandImage[4] = 0x42;
 		missileCommand.commandImage[5] = 0xA5;
 		missileCommand.commandImage[6] = 0x42;
 		missileCommand.commandImage[7] = 0xA5;
 	} else if(missileCommand.missilesLeft == 7) {
 		missileCommand.commandImage[0] = 0x02;
 		missileCommand.commandImage[1] = 0x05;
 		missileCommand.commandImage[2] = 0x42;
 		missileCommand.commandImage[3] = 0xA5;
 		missileCommand.commandImage[4] = 0x42;
 		missileCommand.commandImage[5] = 0xA5;
 		missileCommand.commandImage[6] = 0x42;
 		missileCommand.commandImage[7] = 0xA5;
 	} else if(missileCommand.missilesLeft == 6) {
 		missileCommand.commandImage[0] = 0x00;
 		missileCommand.commandImage[1] = 0x00;
 		missileCommand.commandImage[2] = 0x42;
 		missileCommand.commandImage[3] = 0xA5;
 		missileCommand.commandImage[4] = 0x42;
 		missileCommand.commandImage[5] = 0xA5;
 		missileCommand.commandImage[6] = 0x42;
 		missileCommand.commandImage[7] = 0xA5;
 	} else if(missileCommand.missilesLeft == 5) {
 		missileCommand.commandImage[0] = 0x00;
 		missileCommand.commandImage[1] = 0x00;
 		missileCommand.commandImage[2] = 0x02;
 		missileCommand.commandImage[3] = 0x05;
 		missileCommand.commandImage[4] = 0x42;
 		missileCommand.commandImage[5] = 0xA5;
 		missileCommand.commandImage[6] = 0x42;
 		missileCommand.commandImage[7] = 0xA5;
 	} else if(missileCommand.missilesLeft == 4) {
 		missileCommand.commandImage[0] = 0x00;
 		missileCommand.commandImage[1] = 0x00;
 		missileCommand.commandImage[2] = 0x00;
 		missileCommand.commandImage[3] = 0x00;
 		missileCommand.commandImage[4] = 0x42;
 		missileCommand.commandImage[5] = 0xA5;
 		missileCommand.commandImage[6] = 0x42;
 		missileCommand.commandImage[7] = 0xA5;
 	} else if(missileCommand.missilesLeft == 3) {
 		missileCommand.commandImage[0] = 0x00;
 		missileCommand.commandImage[1] = 0x00;
 		missileCommand.commandImage[2] = 0x00;
 		missileCommand.commandImage[3] = 0x00;
 		missileCommand.commandImage[4] = 0x02;
 		missileCommand.commandImage[5] = 0x05;
 		missileCommand.commandImage[6] = 0x42;
 		missileCommand.commandImage[7] = 0xA5;
 	} else if(missileCommand.missilesLeft == 2) {
 		missileCommand.commandImage[0] = 0x00;
 		missileCommand.commandImage[1] = 0x00;
 		missileCommand.commandImage[2] = 0x00;
 		missileCommand.commandImage[3] = 0x00;
 		missileCommand.commandImage[4] = 0x00;
 		missileCommand.commandImage[5] = 0x00;
 		missileCommand.commandImage[6] = 0x42;
 		missileCommand.commandImage[7] = 0xA5;
 	} else if(missileCommand.missilesLeft == 1) {
 		missileCommand.commandImage[0] = 0x00;
 		missileCommand.commandImage[1] = 0x00;
 		missileCommand.commandImage[2] = 0x00;
 		missileCommand.commandImage[3] = 0x00;
 		missileCommand.commandImage[4] = 0x00;
 		missileCommand.commandImage[5] = 0x00;
 		missileCommand.commandImage[6] = 0x02;
 		missileCommand.commandImage[7] = 0x05;
 	} else if(missileCommand.missilesLeft == 0) {
 		missileCommand.commandImage[0] = 0x00;
 		missileCommand.commandImage[1] = 0x00;
 		missileCommand.commandImage[2] = 0x00;
 		missileCommand.commandImage[3] = 0x00;
 		missileCommand.commandImage[4] = 0x00;
 		missileCommand.commandImage[5] = 0x00;
 		missileCommand.commandImage[6] = 0x00;
 		missileCommand.commandImage[7] = 0x00;
 	}


 	lc4_draw_sprite(missileCommand.x, GROUND_LEVEL, (lc4uint)WHITE, &missileCommand.commandImage[0]);
 }

/************************************************
 *  DrawCities - 
 *  Draws the cities in white
 ***********************************************/

 void DrawCities()
 {
 	int i = 0;
 	for(i = 0; i < NUMOFCITIES; i++) 
 	{
 		lc4_draw_sprite(cities[i].x, GROUND_LEVEL, (lc4uint)RED, &cities[i].cityImage[0]);
 	}
 }

/************************************************
 *  DrawMeteors - 
 *  Draws each meteor (if it is not destroyed)
 ***********************************************/
 void DrawMeteors()
 {
 	int i = 0;
 	for(i = 0; i < MAX_METEORS; i++)
 	{
 		if(meteors[i].isDestroyed == FALSE)
 		{
 			lc4_draw_line(meteors[i].fromX, meteors[i].fromY, 
 				meteors[i].currentX, meteors[i].currentY, (lc4uint)YELLOW);
 		}
 	}
 }

/************************************************
 *  DrawMissiles
 *  Dras each missile (if it is not destroyed)
 ***********************************************/
 void DrawMissiles()
 {
 	int i = 0;
 	for(i = 0; i < MAX_MISSILES; i++)
 	{
 		if(missiles[i].isDestroyed == FALSE)
 		{
 			lc4_draw_line(missiles[i].fromX, missiles[i].fromY, 
 				missiles[i].currentX, missiles[i].currentY, (lc4uint)GREEN);
 		}
 	}
 }

/************************************************
 *  Redraw - 
 *  Assuming that the PennSim is run with double buffered mode, (using the command -> (java -jar PennSim.jar -d))
 *  First, we should clear the video memory buffer using lc4_reset_vmem
 *  Then we draw the scene and then swap the video memory buffer using lc4_blt_vmem
 ***********************************************/
 void Redraw()
 {
 	lc4_reset_vmem();

 	DrawCursor();
 	DrawMissileCommand();
 	DrawCities();
 	DrawMissiles();
 	DrawMeteors();
 	lc4_blt_vmem();
 }

/************************************************
 *  Reset - 
 *  Clears the screen
 ***********************************************/
 void reset()
 {
 	lc4_reset_vmem();

 	lc4_blt_vmem();
 }

/************************************************
 *  ResetGame - 
 *  Resets the game. 
 *  For each missile command, reset the number of missiles, position and image
 *  For each city, reset position and reset the image
 *  For each projectile (meteor/missile), ensure that they are destroyed
 *  Ensure that that the targets the meteors will be aiming at are correct
 ***********************************************/

 void ResetGame()
 {
 	int i = 0;

 	for(i = 0; i < NUMOFMISSILECOMMANDS; i++) 
 	{
 		missileCommand.isDestroyed = FALSE;

 		missileCommand.missilesLeft = 8;

 		missileCommand.x = MISSILE_COMMAND_XPOS;
 	}


 	cities[0].x = LEFT_CITY_XPOS;
 	cities[1].x = RIGHT_CITY_XPOS;
 	for(i = 0; i < NUMOFCITIES; i++) 
 	{
 		cities[i].isDestroyed = FALSE;

 		cities[i].cityImage[0] = 0x18;
 		cities[i].cityImage[1] = 0x18;
 		cities[i].cityImage[2] = 0x3B;
 		cities[i].cityImage[3] = 0x3B;
 		cities[i].cityImage[4] = 0x7D;
 		cities[i].cityImage[5] = 0x7D;	
 		cities[i].cityImage[6] = 0xFF;
 		cities[i].cityImage[7] = 0xFF;
 	}

 	for(i = 0; i < MAX_MISSILES; i++) 
 	{
 		missiles[i].isDestroyed = TRUE;
 	}

 	for(i = 0; i < MAX_METEORS; i++) 
 	{
		meteors[i].isDestroyed = TRUE;
 	}

 	targets[0] = LEFT_CITY_XPOS;
 	targets[1] = MISSILE_COMMAND_XPOS;
 	targets[2] = RIGHT_CITY_XPOS;

 	wave_counter = 1;
 	meteors_per_wave = NUM_METEORS_PER_WAVE;
 	meteors_left_per_wave = meteors_per_wave;
 }

/************************************************
 *  abs - 
 *  returns the absolute value
 ***********************************************/
 int abs(int value)
 {
 	if(value < 0)
 		return -value;
 	return value;
 }

/************************************************
 *  BlowUpMeteor -
 *  Set the index meteor to being destroyed
 ***********************************************/
 void BlowUpMeteor(int index)
 {
 	meteors[index].isDestroyed = TRUE;
 	meteors_left_per_wave--;
 }


/************************************************
 *  CheckForMeteorCollsion - 
 *  Destroys the building if the meteor lands on it
 ***********************************************/
 void CheckForMeteorCollsion(int i)
 {
 	//destroy the building
 	if(meteors[i].finalX == LEFT_CITY_XPOS)
 	{
 		cities[0].isDestroyed = TRUE;
 		cities[0].cityImage[0] = 0x00;
	 	cities[0].cityImage[1] = 0x00;
	 	cities[0].cityImage[2] = 0x00;
	 	cities[0].cityImage[3] = 0x00;
	 	cities[0].cityImage[4] = 0x7F;
	 	cities[0].cityImage[5] = 0xCF;	
	 	cities[0].cityImage[6] = 0xFF;
	 	cities[0].cityImage[7] = 0xFF;
 	} 
 	else if(meteors[i].finalX == RIGHT_CITY_XPOS)
 	{
 		cities[1].isDestroyed = TRUE;
 		cities[1].cityImage[0] = 0x00;
	 	cities[1].cityImage[1] = 0x00;
	 	cities[1].cityImage[2] = 0x00;
	 	cities[1].cityImage[3] = 0x00;
	 	cities[1].cityImage[4] = 0x7F;
	 	cities[1].cityImage[5] = 0xCF;	
	 	cities[1].cityImage[6] = 0xFF;
	 	cities[1].cityImage[7] = 0xFF;
 	}
 	else 
 	{
 		missileCommand.isDestroyed = TRUE;
 		missileCommand.missilesLeft = 0;
 	}
 	BlowUpMeteor(i);
 }

/************************************************
 *  BlowUpMissile -
 *  Check if each meteor is in the blow up radius and mark
 *  the meteor as destroyed if the meteor is in the radius
 *  Make sure to mark the missile as destroyed as well
 ***********************************************/
 void BlowUpMissile(int index)
 {
 	int i = 0;
 	for(i = 0; i < MAX_METEORS; i++)
 	{
 		if(meteors[i].isDestroyed == FALSE)
 		{
 			if((abs((meteors[i].currentX - missiles[index].finalX) * 
 				(meteors[i].currentX - missiles[index].finalX)) + 
 				((meteors[i].currentY - missiles[index].finalY) *
 				(meteors[i].currentY - missiles[index].finalY))) < BLOWUP_RADIUS)
 			{
 				BlowUpMeteor(i); 
 			}
 		}
 	}
 	missiles[index].isDestroyed = TRUE;
 }

/************************************************
 *  UpdateProjectile -
 *  This function draws new lines each frame, so that the 
 *  missiles and meteors look like they are animated
 *  This implementation functions exactly like the line
 *  algorithm where the dx and dy are applied to each
 *  projectile.
 *  For the meteors, ensure that there is a longer delay before
 *  the meteor moves by using the delay counter.
 ***********************************************/

 void UpdateProjectiles()
 {

 	int i = 0;
 	for(i = 0; i < MAX_MISSILES; i++)
 	{
 		if(missiles[i].isDestroyed == FALSE)
 		{
 			missiles[i].numerator += missiles[i].shortest;
 			if(missiles[i].numerator >= missiles[i].longest)
 			{
 				missiles[i].numerator -= missiles[i].longest;
 				missiles[i].currentX += missiles[i].dx1;
 				missiles[i].currentY += missiles[i].dy1;
 			} else {
 				missiles[i].currentX += missiles[i].dx2;
 				missiles[i].currentY += missiles[i].dy2;
 			}
 			if(missiles[i].startLongest > missiles[i].longest)
 			{
 				BlowUpMissile(i);
 			}
 			missiles[i].startLongest++;
 		}
 	}

 	if(delay_counter == DELAY)
 	{
 		for(i = 0; i < MAX_METEORS; i++)
	 	{
	 		if(meteors[i].isDestroyed == FALSE)
	 		{
	 			meteors[i].numerator += meteors[i].shortest;
	 			if(meteors[i].numerator >= meteors[i].longest)
	 			{
	 				meteors[i].numerator -= meteors[i].longest;
	 				meteors[i].currentX += meteors[i].dx1;
	 				meteors[i].currentY += meteors[i].dy1;
	 			} else {
	 				meteors[i].currentX += meteors[i].dx2;
	 				meteors[i].currentY += meteors[i].dy2;
	 			}
	 			if(meteors[i].startLongest > meteors[i].longest)
	 			{
	 				CheckForMeteorCollsion(i);
	 			}
	 			meteors[i].startLongest++;
	 		}
	 	}
 		delay_counter = 0;
 	} else {
 		delay_counter++;
 	}
 }

/************************************************
 *  CalculateMissileMovement - 
 *  This function calculates the dx and dy and other necessary variables
 *  required in the line algrothm for each missile at an index
 ***********************************************/
 void CalculateMissileMovement(int i)
 {
 	int w = missiles[i].finalX - missiles[i].fromX;
 	int h = missiles[i].finalY - missiles[i].fromY;
 	int dx1 = 0;
 	int dy1 = 0;
 	int dx2 = 0;
 	int dy2 = 0;
 	if (w<0) dx1 = -1;
 	if (w>0) dx1 = 1;
 	if (h<0) dy1 = -1;
 	if (h>0) dy1 = 1;
 	if (w<0) dx2 = -1;
 	if (w>0) dx2 = 1;

 	missiles[i].longest = abs(w);
 	missiles[i].shortest = abs(h);

 	if(missiles[i].longest <= missiles[i].shortest)
 	{
 		missiles[i].longest = abs(h);
 		missiles[i].shortest = abs(w);
 		if (h<0) dy2 = -1;
 		if (h>0) dy2 = 1;
 		dx2 = 0;
 	}

 	missiles[i].dx1 = dx1;
 	missiles[i].dy1 = dy1;
 	missiles[i].dx2 = dx2;
 	missiles[i].dy2 = dy2;

 	missiles[i].numerator = missiles[i].longest >> 1;
 	missiles[i].startLongest = 0;

 }

/************************************************
 *  CalculateMeteorMovement - 
 *  This function does the same thing as CalculateMissileMovement, but 
 *  is for meteors
 ***********************************************/
 void CalculateMeteorMovement(int i)
 {
 	int w = meteors[i].finalX - meteors[i].fromX;
 	int h = meteors[i].finalY - meteors[i].fromY;
 	int dx1 = 0;
 	int dy1 = 0;
 	int dx2 = 0;
 	int dy2 = 0;
 	if (w<0) dx1 = -1;
 	if (w>0) dx1 = 1;
 	if (h<0) dy1 = -1;
 	if (h>0) dy1 = 1;
 	if (w<0) dx2 = -1;
 	if (w>0) dx2 = 1;

 	meteors[i].longest = abs(w);
 	meteors[i].shortest = abs(h);

 	if(meteors[i].longest <= meteors[i].shortest)
 	{
 		meteors[i].longest = abs(h);
 		meteors[i].shortest = abs(w);
 		if (h<0) dy2 = -1;
 		if (h>0) dy2 = 1;
 		dx2 = 0;
 	}

 	meteors[i].dx1 = dx1;
 	meteors[i].dy1 = dy1;
 	meteors[i].dx2 = dx2;
 	meteors[i].dy2 = dy2;

 	meteors[i].numerator = meteors[i].longest >> 1;
 	meteors[i].startLongest = 0;

 }

/************************************************
 *  CreateNewMissile - 
 *  This function creates a new missile when the user presses the fire button.
 *  This function should check if the missile is destroyed and set the starting
 *  and final location. In addition, it should also calculate the dx and dy 
 *  by calling CalculateMissileMovement.
 ***********************************************/

 void CreateNewMissile()
 {
 	int i = 0;
 	for(i = 0; i < MAX_MISSILES; i++)
 	{
 		if(missiles[i].isDestroyed == TRUE)
 		{
 			missiles[i].isDestroyed = FALSE;
 			missiles[i].fromX = missileCommand.x + 4;
 			missiles[i].fromY = GROUND_LEVEL + 4;
 			missiles[i].currentX = missiles[i].fromX;
 			missiles[i].currentY = missiles[i].fromY;
 			missiles[i].finalX = cursor.x;
 			missiles[i].finalY = cursor.y;
 			CalculateMissileMovement(i);
 			missileCommand.missilesLeft--;
 			return;
 		}
 	}
 }

/************************************************
 *  CreateNewMeteors - 
 *  This function creates a new meteor like how CreateNewMissile works, but only if the meteors left during the wave is greater than 0.
 ***********************************************/
 void CreateNewMeteors()
 {
 	int i = 0;
	for(i = 0; i < MAX_METEORS; i++)
	{
		if(meteors[i].isDestroyed == TRUE)
	 	{
			meteors[i].isDestroyed = FALSE;
	 		meteors[i].fromX = rand16();
			meteors[i].fromY = 0;
	 		meteors[i].currentX = meteors[i].fromX;
	 		meteors[i].currentY = meteors[i].fromY;
	 		meteors[i].finalX = targets[(rand16() * 3)/128];
	 		meteors[i].finalY = GROUND_LEVEL;
	 		CalculateMeteorMovement(i);
	 	}
	} 
}

/************************************************
 *  CreateNewMeteors - 
 *  This function executes cursor movement and firing the missile based on the keyboard input.
 *  If the input is 'a', then move the cursor to the right
 *  If the input is 'd', then move the cursor to the left
 *  If the input is 'w', then move the cursor to the up
 *  If the input is 's', then move the cursor to the down
 *  Ensure that the cursor will not go out of bounds (ie. beyond the screen height/width)
 *  If the input is 'r', then create a new missile that will fly to the final position
 *  Ensure that the there is a cooldown interval between each fire
 ***********************************************/
 void ExecuteInput(lc4int input)
 {
 	if (input == 'a')
 	{
 		cursor.x--;
 		if(cursor.x < 0) cursor.x = 0;
 	}
 	else if (input == 'd')
 	{
 		cursor.x++;
 		if(cursor.x == SCREEN_WIDTH) cursor.x = SCREEN_WIDTH - 1;
 	}
 	else if (input == 'w')
 	{
 		cursor.y--;
 		if(cursor.y < 0) cursor.y = 0;
 	}
 	else if (input == 's')
 	{
 		cursor.y++;
 		if(cursor.y == SCREEN_HEIGHT) cursor.y = SCREEN_HEIGHT - 1;
 	}
 	else if (input == 'r' && missileCommand.isDestroyed == FALSE && missileCommand.missilesLeft > 0)
 	{
 		if(cooldown_counter > COOLDOWN_DELAY)
 		{
 			CreateNewMissile();
 		}
 	}
 	cooldown_counter++;
 }

 int rand16 ()
 {
 	int lfsr;

  // Advance the lfsr seven times
 	lfsr = lc4_lfsr();
 	lfsr = lc4_lfsr();
 	lfsr = lc4_lfsr();
 	lfsr = lc4_lfsr();
 	lfsr = lc4_lfsr();
 	lfsr = lc4_lfsr();
 	lfsr = lc4_lfsr();

  // return the last 7 bits
 	return (lfsr & 0x7F);
 }

/************************************************
 *  PrepareForNextWave - 
 *  If the meteors per wave is less than or equal to 0, then
 *  increment the number of meteors per wave and set the number of meteors left for that wave to that number
 *  Increment the current wave number and print that onto the screen
 *  and reset the number of missile left for the missile command and redraw the missileCommand if it was destroyed.
 ***********************************************/
 void PrepareForNextWave() 
 {
 	if(meteors_left_per_wave <= 0)
 	{
 		int i = 0;
		for(i = 0; i < MAX_METEORS; i++)
		{
			meteors[i].isDestroyed = TRUE;
		}

 		meteors_per_wave += NUM_METEORS_PER_WAVE;
 		meteors_left_per_wave = meteors_per_wave;

 		wave_counter++;
 		lc4_puts ((lc4uint*)"Current wave: ");
 		printnum(wave_counter);
		lc4_puts ((lc4uint*)"\n");

	 	missileCommand.missilesLeft = 8;
	 	missileCommand.isDestroyed = FALSE;
 	}
 }

/************************************************
 *  main - 
 *  Initalize game state by reseting the game state
 *  Loops until the the user loses
 ***********************************************/
 int main ()
 {
 	//** Print to screen and initalize game state. */
 	lc4_puts ((lc4uint*)"Welcome to the Missile Command!\n");

 	ResetGame();

 	while(1) 
 	{
 		//** Print to screen the instructions of the game */

 		lc4_puts ((lc4uint*)"Press r to shoot a missile\n");

 		lc4_puts ((lc4uint*)"Press w, a, s and d to move the cursor\n");

 		//** Main game loop calls the functions above to update the */
 		//** states of the missiles, meteors, cursors, etc and redraws the necessary images *

 		//** If the user loses, be sure to print out "---GAME OVER---" before resetting the game
 		while(1)
 		{
 			if(cities[0].isDestroyed && cities[1].isDestroyed)
 				break;
 			Redraw();
 			ExecuteInput(lc4_getc_timer(GETC_TIMER_DELAY));
  			CreateNewMeteors();
 			UpdateProjectiles();
 			PrepareForNextWave();
 		}

 		lc4_puts ((lc4uint*)"---GAME OVER---\n");

 		ResetGame();
 	}

 	return 0;
 }
