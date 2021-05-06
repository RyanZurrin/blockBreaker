/**
 * @Program ZurrinRyanBB.java
 * @author Ryan Zurrin
 * @Description  Build a block breaker game using shapes and scene graph.
 * @Assignment Chapter 22 exercise #22.13-22.14
 * @DueDate 5/4/2021
 */
import java.awt.Graphics;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.JFrame;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;


/**
 * Main class is a driver class that contains the main method to run the
 * program
 * @author Ryan Zurrin
 */
public class ZurrinRyanBB {
        /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JFrame obj = new JFrame();
        BlockBreaker game = new BlockBreaker();
        obj.setBounds(8, 8,800, 700);
        obj.setTitle("Block Breaker Game");
        obj.setResizable(false);
        obj.setVisible(true);
        obj.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        obj.add(game);
        obj.setLocationRelativeTo(null);
    }
}//end class ZurrinRyanBB

/**
 * BlockBreaker Class is used as the primary class for maintaining all the
 * Listeners as well it is responsible for building and painting the scene during
 * game play, keeping everything running and working smoothly.
 * @author Ryan Zurirn
 */
class BlockBreaker extends JPanel implements
        KeyListener,
        ActionListener,
        MouseMotionListener,
        MouseListener
{
    //constants to set the number of blocks as well as the number of rows
    // and columns to use. the total of NUM_BLOCKS thould be = ROWS * COLS
    private final int NUM_BLOCKS = 20;
    private final int ROWS = 5; //keep this at 5 to maintain the points system
    private final int COLS = 4;

    // this can be adjusted to maintain smooth and steady play. different
    //systems process at different speeds so if you find things start too fast
    // you can increase the SEQUENCER_DELAY varaible, if to slow then decrease.
    private final Timer FRAME_SEQUENCER;
    private final int SEQUENCER_DELAY = 10;// the lower this value the faster

    //variable used to keep track of blocks
    private int totalBlocks = NUM_BLOCKS;

    // boolean values used to control flow of the game
    private boolean playing = false;
    private boolean winner = false;
    private boolean waitToRestart = false;

    // variables to keep track of the score, the lives and the level of player
    private int score = 0;
    private int lives = 3;
    private int level = 1;

    // variable to track the paddles position
    private int paddlePosX = 410;
    private int padH = 8;
    private int padW = 120;
    private int edgeCase = 680;


    //variable to set the starting positions on restarts and after lost lives
    private int bStartPosX = paddlePosX;
    private int bStartPosY = 630;

    //variables to set the starting speed and used to track the movement of the ball
    private int dX = -2;
    private int dY = -2;

    //variables to keep track of the increase in ball speed as levels increase
    private int lev_dX = -2;
    private int lev_dY = -2;

    // BlockMaker object for building the blocks in the game
    private BlockMaker blocks;

    /**
     * constructor that instantiates the block object as well as calls the
     * initialize method which sets all the Listeners to listen to this class
     * for mouse movements as well as keystrokes for moving the paddle and
     * playing the game. Also instantiates the FRAME_SEQUENCER as well as calls
     * the start method to begin the game.
     */
    public BlockBreaker(){
        blocks = new BlockMaker(ROWS, COLS);
        initialize();
        FRAME_SEQUENCER= new Timer(SEQUENCER_DELAY, this);
        FRAME_SEQUENCER.start();
    }//end PlayGame constructor

    /**
     * initialize method used to instantiate all the Listeners for the game
     */
    private void initialize(){
        addMouseMotionListener(this);
        addMouseListener(this);
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
    }

    /**
     * the paint method is called to update the scene with current data
     * @param bb block builder Graphics variable passed to paint the scene
     */
    @Override
    public void paint(Graphics bb){
        // setting the background color
        bb.setColor(Color.DARK_GRAY);
        bb.fillRect(1, 1, 792, 692);

        //making the blocks
        blocks.draw((Graphics2D) bb);

        // setting the game borders
        bb.setColor(Color.green);
        bb.fillRect(0, 0, 3, 692);
        bb.fillRect(0, 0, 792, 3);
        bb.fillRect(781, 0, 3, 692);

        // setting the lives font
        bb.setColor(Color.white);
        bb.setFont(new Font("arial", Font.BOLD, 25));
        bb.drawString("Lives: "+lives, 50, 30);

        // setting the level font
        bb.setColor(Color.white);
        bb.setFont(new Font("arial", Font.BOLD, 25));
        bb.drawString("Level: "+level, 360, 30);

        // setting the score font
        bb.setColor(Color.white);
        bb.setFont(new Font("ariel", Font.BOLD, 25));
        bb.drawString("Score: "+score, 620, 30);

        // setting the paddle color and size
        bb.setColor(Color.yellow);
        bb.fillRect(paddlePosX, 650, padW, padH);

        // setting the ball color and starting positions to be above the paddle
        // when not playing
        bb.setColor(Color.pink);
        if (!playing) {
            bb.fillOval(paddlePosX+40, 630, 20, 20);
        }else{
            bb.fillOval(bStartPosX, bStartPosY, 20, 20);
        }//end if

        // if player breaks all the blocks, gets ready for new level
        if (totalBlocks <= 0  ||
                (waitToRestart==true && totalBlocks <=0 ) ) {
            playing = false;
            dX = -0;
            dY = 0;
            winner = true;
            bb.setColor(Color.red);
            bb.setFont(new Font("serif", Font.BOLD, 30));
            bb.drawString("Level Won, Score: "+score, 260, 425);
            bb.setFont(new Font("serif", Font.BOLD, 20));
            bb.drawString("Press Enter or Click for level "  + (level+1) , 270, 475);
            bStartPosY = 630;
            waitToRestart = true;
            blocks = new BlockMaker(ROWS, COLS);
            repaint();
        }// end if

        // if player loses a ball
        if ((bStartPosY >671 && lives >0) ||
                (waitToRestart==true && lives >0 && totalBlocks != 0 )) {
            playing = false;
            dX = 0;
            dY = 0;
            bb.setColor(Color.red);
            bb.setFont(new Font("serif", Font.BOLD, 30));
            bb.drawString("Ball Lost, Balls remaining: "+ lives, 220, 425);
            bb.setFont(new Font("serif", Font.BOLD, 20));
            bb.drawString("Press Enter or Click to try agian", 270, 475);
            bStartPosY = 630;
            waitToRestart = true;
        }//end if

        // if player loses the game by running out of lives
        if (bStartPosY > 671 && lives < 1  ||
                (waitToRestart==true && lives <1 ) ) {
            playing = false;
            dX = 0;
            dY = 0;
            bb.setColor(Color.red);
            bb.setFont(new Font("serif", Font.BOLD, 30));
            bb.drawString("Game Over, Score: "+score, 250, 425);
            bb.setFont(new Font("serif", Font.BOLD, 20));
            bb.drawString("Made it to level: "+level, 320, 450);
            bb.setFont(new Font("serif", Font.BOLD, 20));
            bb.drawString("Press Enter or Click to Restart", 270, 475);
            bStartPosY = 630;
            waitToRestart = true;
        }//end if

        // dispose is called to remove reference to frame no longer used and
        // and make room for new frame
        bb.dispose();
    }//end method paint

    /**
     * This is the method that is used to determine what action to take for
     * certain key press events on the keyboard
     * @param e is the Event variable that gives me access to specific key press
     * events that are being listened for within the game
     */
    @Override
    public void keyPressed(KeyEvent e) {
         if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (paddlePosX >= 692) {
                paddlePosX = 692;
            }
            else{
                paddlePosX+=30;
            }
         }
         if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (paddlePosX < 12) {
                paddlePosX = 12;
            }
            else{
                paddlePosX-=30;
            }
        }

         if (e.getKeyCode() == KeyEvent.VK_ENTER && lives >0 && winner == false) {
                     if (!playing) {
                         tryAgain();
                     }
        }

         if (e.getKeyCode() == KeyEvent.VK_ENTER && (lives == 0)) {
                     if (!playing) {
                         gameOver();
                     }
         }
         if (e.getKeyCode() == KeyEvent.VK_ENTER && winner == true) {
                     if (!playing) {
                         levelUp();
                     }
        }
    }//end method keyPressed

    /**
     *
     */
    private void tryAgain(){
        playing = true;
        bStartPosX = paddlePosX;
        bStartPosY = 630;
        dX = lev_dX;
        dY = lev_dY;
        paddlePosX = paddlePosX;
        waitToRestart = false;
        repaint();
    }//end method tryAgain

    /**
     *
     */
    private void levelUp(){
        playing = true;
        winner = false;
        bStartPosX = paddlePosX;
        bStartPosY = 630;
        dX = lev_dX-1;
        dY = lev_dY-1;
        lev_dX = dX;
        lev_dY = dY;
        paddlePosX = paddlePosX;
        padW = (padW-(padW*5)/100);
        edgeCase+=5;
        level++;
        totalBlocks = NUM_BLOCKS;
        blocks = new BlockMaker(ROWS, COLS);
        waitToRestart = false;
        repaint();
    }//end method levelUp

    /**
     *
     */
    private void gameOver(){
        playing = true;
        winner = false;
        bStartPosX = paddlePosX;
        bStartPosY = 630;
        dX = -2;
        dY = -3;
        lev_dX = -2;
        lev_dY = -3;
        paddlePosX = paddlePosX;
        score = 0;
        lives = 3;
        level = 1;
        edgeCase = 680;
        padW = 100;
        totalBlocks = NUM_BLOCKS;
        blocks = new BlockMaker(ROWS, COLS);
        waitToRestart = false;
        repaint();
    }//end method gameOver

    /**
     * method that determines the appropriate response to specific actions that
     * are performed, such as a block being hit or the ball hitting the paddle.
     * @param e is the Action event object used to
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        FRAME_SEQUENCER.start();

        if (playing) {
            if (new Rectangle(bStartPosX, bStartPosY, 20, 20).//left zone
                    intersects(new Rectangle(paddlePosX, 650, (padW*1/3),padH)) ) {
                dY = -dY;
            }
           else if (new Rectangle(bStartPosX, bStartPosY, 20, 20).//center zone
                    intersects(new Rectangle(paddlePosX, 650,(padW*1/3)+(padW*1/3),padH)) ) {
                dY = -dY;
            }
           else  if (new Rectangle(bStartPosX, bStartPosY, 20, 20).//right zone
                    intersects(new Rectangle(paddlePosX, 650, (padW*2/3)+(padW*1/3),padH)) ) {
                dY = -dY;
            }

            BlockHit:  //used to step out of the loop when needed to break
            for (int i = 0; i < blocks.blockArray.length ; i++) {
                for (int j = 0; j < blocks.blockArray[0].length; j++) {
                    if (blocks.blockArray[i][j] > 0) {
                        int blockX = j* blocks.blockWidth + 80;
                        int blockY= i* blocks.blockHeight + 50;
                        int blockWidth = blocks.blockWidth;
                        int blockHeight = blocks.blockHeight;
                        Rectangle rect = new Rectangle(blockX, blockY, blockWidth, blockHeight);
                        Rectangle ballRect = new Rectangle(bStartPosX, bStartPosY, 20,20);
                        Rectangle blockRect = rect;

                        // gives score relitive to block position
                        int k = blocks.blockArray[i][j];
                        if (ballRect.intersects(blockRect)) {
                            blocks.destroyBlock(k-1, i, j);
                            if (blocks.blockArray[i][j] == 0) {
                                totalBlocks--;
                                switch (i) {
                                    case 0:
                                        score += 20;
                                        break;
                                    case 1:
                                        score += 15;
                                        break;
                                    case 2:
                                        score += 10;
                                        break;
                                    case 3:
                                        score += 5;
                                        break;
                                    default:
                                        score += 1;
                                        break;
                                }
                            }

                            if (bStartPosX + 19 <= blockRect.x ||
                                    bStartPosX + 1 >= blockRect.x + blockRect.width) {
                                dX = -dX;
                            }
                            else{
                                dY = -dY;
                            }
                            break BlockHit;
                        }
                    }
                }
            }
            bStartPosX += dX;
            bStartPosY += dY;
            if (bStartPosX < 0) {
                dX =  -dX;
            }
            if (bStartPosY < 0) {
                dY =  -dY;
            }
            if (bStartPosX > 762) {
                dX =  -dX;
            }
            if (bStartPosY>670) {
                lives--;
            }
        }
        repaint();
    }//end method actionPerformed

    @Override
    public void mouseMoved(MouseEvent e) {
            if (e.getX() >= edgeCase) {
                paddlePosX = edgeCase;
            }
            else{
                paddlePosX = e.getX();
            }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!playing && lives >0 && winner == false ) {
            tryAgain();
        }
        if (!playing && lives == 0  ) {
            gameOver();
        }
        if (!playing && winner == true) {
            levelUp();
        }
    }


    @Override// not used
    public void keyTyped(KeyEvent e) {}//end method keyTyped
    @Override// not used
    public void keyReleased(KeyEvent e) {}//end method keyReleased
    @Override// not used
    public void mouseDragged(MouseEvent e) {}//end method mouseDragged
    @Override// not used
    public void mousePressed(MouseEvent e) {}//end method mousePressed
    @Override// not used
    public void mouseReleased(MouseEvent e) {}//end method mouseReleased
    @Override// not used
    public void mouseEntered(MouseEvent e) {}//end method mouseEntered
    @Override// not used
    public void mouseExited(MouseEvent e) {}//end method mouseExited
}//end class PlayGame

/**
 * BlockMaker is a block building class with only one responsibility, to set the
 * blocks in the array and give them initial values and keep track of what ones
 * are still in play and what ones are not.
 * @author Ryan Zurrin
 */
class BlockMaker {

    public int blockArray[][];
    private final Color colorKeeper[][];
    public int blockWidth;
    public int blockHeight;
    static Random rand = new Random();
    /**
     * overloaded constructor that takes the row and column number from the main
     * method so it knows how many blocks to build and color for the game.
     * @param row is the variable to set the row numbers
     * @param col is the variable to set the column numbers
     */
    public BlockMaker(int row, int col){
        blockArray = new int[row][col]; // instanciates the block arrray
        colorKeeper = new Color[row][col]; // instanciates the color array
        //setting the random colors for the blocks to use when they are hit for
        //first time
        for (int i = 0; i < colorKeeper.length; i++) {
            for (int j = 0; j < colorKeeper[0].length; j++) {
                colorKeeper[i][j] = getColor();
            }
        }
        // setting the blocks array values to 2 which is for tracking the hits
        for (int i = 0; i < blockArray.length; i++) {
            for (int j = 0; j < blockArray[0].length; j++) {
                blockArray[i][j] = 2;
            }
        }
        // setting the size of the blocks relitive to the number of rows and
        // columns to maintain the integrity of the play area
        blockWidth = 640/col;
        blockHeight = 250/row;
    }//end BlockMaker constructor

    /**
     * a dray method used to draw the blocks each frame which is determined by
     * whether the block was not hit, hit once, or destroyed. This is kept track
     * of in the array value of the block array.
     * @param blocks is a Graphics@D object used to draw the blocks each time the
     * scene is repainted in the block breaker class
     */
    public void draw(Graphics2D blocks){
        for (int i = 0; i < blockArray.length; i++) {
            for (int j = 0; j < blockArray[0].length; j++) {
                if (blockArray[i][j] > 0) {
                    if (blockArray[i][j] ==2) {
                        blocks.setColor(getColor());
                    }else{
                         blocks.setColor(colorKeeper[i][j]);
                    }
                    blocks.fillRect(j * blockWidth + 80, i * blockHeight + 50, blockWidth, blockHeight);
                    blocks.setStroke(new BasicStroke(3));
                    blocks.setColor(Color.black);
                     blocks.drawRect(j * blockWidth + 80, i * blockHeight + 50, blockWidth, blockHeight);
                }
            }
        }
    }//end method draw

    /**
     * method used to set the value of the array in order to maintain the current
     * state of each block
     * @param val is used to determine if the block has been hit once or destroyed
     * , which is when it is hit twice
     * @param row variable of the row number being changed
     * @param col variable of the column number being changed
     */
    public void destroyBlock(int val, int row, int col) {
        blockArray[row][col] = val;
    }//end method destroyBlock

    /**
     * method used to generate random colors for the blocks to use
     * @return
     */
    static Color getColor()
    {
        int min = 1, max = 5;
        int randomPick = rand.nextInt(max - min) + min;
        //added this to select the four colors the specifications asked for.
        //you can comment this out and uncomment the bottom return and
        //get a bigger variety of random colors which is pretty cool looking.
        switch(randomPick)
        {
            case 1:
               return new Color(249,12,12);//red
            case 2:
                return new  Color(12,249,20);//green
            case 3:
                return new Color(30,50,233);//blue
            case 4:
               return new Color(250,242,4);//yellow
            default:
                return new Color(250,242,4);
        }
       //return new Color(rand.nextFloat(),rand.nextFloat(),rand.nextFloat());
    } //end method getColor

}//end class BlockMaker
