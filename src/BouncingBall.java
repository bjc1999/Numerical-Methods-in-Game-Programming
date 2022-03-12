import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BouncingBall extends JFrame
{
    private int X = 50;
    private int Y = 50;
    private int maxX = 1000;
    private int maxY = 900;
    private int minX = 0;
    private int minY = 0;
    
    private Timer timer;

    private int temporaryStore = 0;

    private int i = 0;
    private int j = 0;

    private int theTempXMouse = 0;
    private int theTempYMouse = 0;

    private int thePrevTempXMouse = 0;
    private int thePrevTempYMouse = 0;

    private boolean[][] screen = null;
    
    private Ball ball;
    private boolean hold = false;
    private double deformX = 1;
    private double deformY = 1;

    final drawPanel myDrawPanel;
    
    // Gravity is 9.8 m/s2
    final double GRAVITY = 9.8;

    public BouncingBall()
    {
        myDrawPanel = new drawPanel();
        MyKeyListener myKeyListener = new MyKeyListener();
        
        // ball instance, height start at 450 pixel (45m)
        ball = new Ball((int)(maxX/2), (int)(maxY/2), 20, 0, 0);

        this.add(myDrawPanel);
        addKeyListener(myKeyListener);
        this.setVisible(true);

        int windowXSize = maxX * 125 / 100;
        int windowYSize = maxY * 125 / 100;

        screen = new boolean[maxX][maxY];
        initializeScreen();

        this.setSize(windowXSize,windowYSize);

        MouseHandler myHandler = new MouseHandler();
        myDrawPanel.addMouseListener(myHandler);
        myDrawPanel.addMouseMotionListener(myHandler);

        myDrawPanel.setVisible(true);

        setDefaultCloseOperation( DISPOSE_ON_CLOSE );

        addWindowListener(
            new WindowAdapter()
            {
                // exit when window has closed
                public void windowClosed( WindowEvent event )
                {
                    System.exit( 0 );
                } // end method windowClosed
            } // end WindowAdapter inner class
        ); // end call to addWindowListener
        
        // timer handler with 60 fps or around 16 ms
        timer = new Timer((int)1000/60, new ActionHandler());
        // start the timer
        timer.start();
    }

    // action that performed by timer handler every 33.33 ms
    private class ActionHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e) 
        {
            // while ball not on the ground, ball vertical velocity affect by gravity
            // Gravity * time interval which is 0.016 s and converted to pixel using (1m : 10pixels) ratio
            if(!(ball.speedY ==0 && ball.y == maxY-(ball.radius*2)))
                ball.speedY = ball.speedY + GRAVITY*0.16;
            
            // if ball is rolling on the ground, friction applied to make it stop moving
            if(ball.y == maxY-(ball.radius*2))
                ball.speedX *= 0.7;
            
            // update location of ball every 16 ms
            if(ball.speedX > 0)
                ball.x = (int)Math.ceil(ball.x + ball.speedX*0.16);
            else
                ball.x = (int)Math.floor(ball.x + ball.speedX*0.16);
            if(ball.speedY > 0)
                ball.y = (int)Math.ceil(ball.y + ball.speedY*0.16);
            else
                ball.y = (int)Math.floor(ball.y + ball.speedY*0.16);
            
            // when ball strike the vertical wall bounce back and apply deformation
            if((ball.x < 0 || ball.x > maxX-(ball.radius*2)) && deformY==1)
            {
                // deformation occurs when velocity of ball is over 100 pixels/s or 10 m/s
                if(ball.speedX > 100 || ball.speedX < -100)
                {
                    // deformation degree increase every 100 pixels/s (10 m/s), maximum deformation occurs at 500 pixels/s (50 m/s)
                    int deform = (int)(ball.speedX/100);
                    if(deform < 0)
                        deform *= -1;
                    if(deform > 5)
                        deform = 5;
                    deformY = 1 + deform/10.0;
                    deformX = 1 - deform/10.0;
                }
                
                // 16% of energy loss when hit the wall
                //System.out.printf("Velocity loss: %.2f m/s\n", ball.speedX*0.016);
                ball.speedX *= -0.84;
                
                if(ball.x < 0)
                    ball.x = 0;
                else
                    ball.x = (int)(maxX-(ball.radius*2)*deformX);
                
            }else if(ball.x < maxX-(ball.radius*2) && deformX < 1)
            {
                deformX = 1;
                deformY = 1;
            }
            
            // when ball strike the horizontal wall bounce back and apply deformation
            if((ball.y < 0 || ball.y > maxY-(ball.radius*2)) && deformX==1)
            {
                // deformation occurs when velocity of ball is over 100 pixels/s or 10 m/s
                if(ball.speedY > 100 || ball.speedY < -100)
                {
                    // deformation degree increase every 100 pixels/s (10 m/s), maximum deformation occurs at 500 pixels/s (50 m/s)
                    int deform = (int)(ball.speedY/100);
                    if(deform < 0)
                        deform *= -1;
                    if(deform > 5)
                        deform = 5;
                    deformX = 1 + deform/10.0;
                    deformY = 1 - deform/10.0;
                }
                
                // 16% of energy loss when hit the wall
                //System.out.printf("Velocity loss: %.2f m/s\n", ball.speedX*0.016);
                ball.speedY *= -0.84;
                
                if(ball.y < 0)
                    ball.y = 0;
                else
                    ball.y = (int)(maxY-(ball.radius*2)*deformY);
                
                // if ball vertical velocity becoming very small, make it stop eventually on the ground
                if(ball.speedY < 5 && ball.speedY > -5)
                    ball.speedY = 0;
                
            }else if(ball.y < maxY-(ball.radius*2) && deformY < 1)
            {
                deformX = 1;
                deformY = 1;
            }
            
            // repaint the canvas every 16 ms to make human eye perceive motion
            repaint();
        }
    }

    private class MouseHandler implements MouseListener, MouseMotionListener
    {
        public void mouseClicked(MouseEvent event)
        {
        }//end public void mouseClicked(mouseEvent event)

        public void mousePressed(MouseEvent event)
        {         
            theTempXMouse = event.getX();
            theTempYMouse = event.getY();
            
            // calculate distance between location of mouse pressed and center of ball
            double sum = Math.pow(theTempXMouse-(ball.x+ball.radius) , 2) + Math.pow(theTempYMouse-(ball.y+ball.radius) , 2);
            double d = Math.sqrt(sum);
            
            // if distance < radius means user clicking the ball
            if(d <= ball.radius)
            {
                hold = true;
                ball.speedX = 0;
                ball.speedY = 0;
                // stop the timer handler to stop the gravity effect on ball
                timer.stop();
            }
            
            thePrevTempXMouse = theTempXMouse;
            thePrevTempYMouse = theTempYMouse;
        }//end public void mousePressed(mouseEvent event)

        public void mouseReleased(MouseEvent event)
        {
            theTempXMouse = event.getX();
            theTempYMouse = event.getY();
            if(theTempXMouse > 0 && theTempXMouse < maxX && theTempYMouse > 0 && theTempYMouse < maxY)
            {
                if(hold)
                {
                    thePrevTempXMouse = theTempXMouse;
                    thePrevTempYMouse = theTempYMouse;
                    hold = false;
                }
                myDrawPanel.repaint();
            }
            // start timer again when ball released
            timer.start();
        }//end public void mouseReleased(mouseEvent event)

        public void mouseEntered(MouseEvent event)
        {
            theTempXMouse = 0;
            theTempYMouse = 0;
        }//end public void mouseEntered(mouseEvent event)

        public void mouseExited(MouseEvent event)
        {
            theTempXMouse = 0;
            theTempYMouse = 0;
        }//end public void mouseExited(mouseEvent event)

        public void mouseDragged(MouseEvent event)
        {
            theTempXMouse = event.getX();
            theTempYMouse = event.getY();
            if(theTempXMouse > 0 && theTempXMouse < maxX-ball.radius && theTempYMouse > 0 && theTempYMouse < maxY-ball.radius)
            {
                if(hold)
                {
                    ball.x = theTempXMouse - (int)ball.radius;
                    ball.y = theTempYMouse - (int)ball.radius;
                    
                    // calculate velocity of ball assuming that dragging frame rate is around 0.15s 
                    ball.speedX = (theTempXMouse - thePrevTempXMouse) / 0.15;
                    ball.speedY = (theTempYMouse - thePrevTempYMouse) / 0.15;
                }
                myDrawPanel.repaint();
            }
            thePrevTempXMouse = theTempXMouse;
            thePrevTempYMouse = theTempYMouse;
            
        }//end public void mouseDragged(mouseEvent event)

        public void mouseMoved(MouseEvent event)
        {
        }//end public void mouseMoved(mouseEvent event)
        
    }//end private class MouseHandler implements MouseListener, MouseMotionListener

    private class MyKeyListener implements KeyListener
    {
        public void keyPressed(KeyEvent event)
        {
            switch(event.getKeyCode())
            {
                case KeyEvent.VK_DELETE:
                    initializeScreen();
                    myDrawPanel.repaint();
                    break;
                    
            }
        }
        
        public void keyReleased(KeyEvent event)
        {
        }

        public void keyTyped(KeyEvent event)
        {
        }
    }

    private class drawPanel extends JPanel
    {
        private int width = 10;
        private int height = 10;
        public void paint(Graphics grap)
        {
            super.paint(grap);
            // draw border line of box
            grap.drawLine(minX, minY, minX, maxY);
            grap.drawLine(minX, minY, maxX, minY);
            grap.drawLine(maxX, minY, maxX, maxY);
            grap.drawLine(minX, maxY, maxX, maxY);
            
            // draw the ball
            ball.drawBall(grap);
            
        }
    }
    
    private class Ball
    {
        int x, y;
        double speedX, speedY;
        double radius;
        
        public Ball(int x, int y, double radius, double speedX, double speedY)
        {
            this.x = x;
            this.y = y;
            this.speedX = speedX;
            this.speedY = speedY;
            this.radius = radius;
        }
        
        public void drawBall(Graphics grap)
        {
            double gradient = 255/(ball.radius*2);
            // draw the ball in shaded form to make 3d effect
            for(int i=0; i<=ball.radius*2; i++)
            {
                Color color = new Color(0+(int)(i*gradient), 0+(int)(i*gradient), 255);
                grap.setColor(color);
                grap.fillOval((int)((ball.x+(i/3))), (int)((ball.y+(i/3))), (int)((ball.radius*2-i)*deformX), (int)((ball.radius*2-i)*deformY));
            }
        }
    }

    private void initializeScreen()
    {
        for(i = 0; i < maxX; i++)
        {
            for(j = 0; j < maxY; j++)
            {
                screen[i][j] = false;
            }
        }
        myDrawPanel.repaint();
    }

    public static void main(String args[])
    {
        new BouncingBall();
    }
}
