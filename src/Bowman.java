import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Random;

public class Bowman extends JFrame{
    private int X = 50;
    private int Y = 50;
    private int maxX = 2000;
    private int maxY = 1900;
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

    final drawPanel myDrawPanel;
    
    private final double GRAVITY = 9.8;
    
    private Slider slider;
    private boolean sliderControl = false;
    private boolean shoot = false;
    private boolean reset = false;
    private boolean relief = false;
    private boolean slow = false;
    private boolean effect = false;
    
    private HPBar hp1;
    private HPBar hp2;
    
    private Human player1;
    private Human player2;
    
    private int scrollX = 0;
    private int scrollY = 0;
    private int turn = 1;
    private int fps = 30;
    private JLabel winner;
    
    public Bowman(){
        myDrawPanel = new drawPanel();
        myDrawPanel.setLayout(null);
        MyKeyListener myKeyListener = new MyKeyListener();
        
        this.add(myDrawPanel);
        addKeyListener(myKeyListener);
        this.setVisible(true);

        int windowXSize = maxX * 125 / 100;
        int windowYSize = maxY * 125 / 100;

        this.setSize(windowXSize,windowYSize);

        MouseHandler myHandler = new MouseHandler();
        myDrawPanel.addMouseListener(myHandler);
        myDrawPanel.addMouseMotionListener(myHandler);

        myDrawPanel.setVisible(true);
        
        slider = new Slider();
        hp1 = new HPBar(50, 50);
        hp2 = new HPBar(1500, 50);
        winner = new JLabel();
        winner.setBounds(780, 300, 750, 150);
        winner.setFont(winner.getFont().deriveFont(50.0f));
        winner.setForeground(Color.red);
        Random rand = new Random();
        player1 = new Human(rand.nextInt(3500)-2000, 1);
        player2 = new Human(rand.nextInt(1500)+2000, -1);
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

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
        timer = new Timer((int)1000/fps, new ActionHandler());
    }
    
    private class ActionHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e) 
        {
            if(slow)
            {
                effect = !effect;
                if(turn == 1)
                {
                    if(hp2.prevHp!=hp2.hp)
                    {
                        hp2.prevHp -= 1;
                    }else
                    {
                        timer.stop();
                        slow = false;
                        if(hp2.hp == 0)
                        {
                            winner.setText("Player 1 win!");
                            myDrawPanel.add(winner);
                        }else
                        {
                            timer = new Timer((int)1000/fps, new ActionHandler());
                            timer.start();
                            effect = false;
                        }
                    }
                }else
                {
                    if(hp1.prevHp!=hp1.hp)
                    {
                        hp1.prevHp -= 1;
                    }else
                    {
                        timer.stop();
                        slow = false;
                        if(hp1.hp == 0)
                        {
                            winner.setText("Player 2 win!");
                            myDrawPanel.add(winner);
                        }else
                        {
                            timer = new Timer((int)1000/fps, new ActionHandler());
                            timer.start();
                            effect = false;
                        }
                    }
                }
            }else
            {
                if(reset)
                {
                    if(turn == 1 && scrollX <= 0)
                    {
                        scrollX = 0;
                        reset = false;
                        relief = true;
                    }else if(turn == 2 && scrollX >=0)
                    {
                        scrollX = 0;
                        reset = false;
                        relief = true;
                    }else if(scrollX != 0)
                    {
                        if(turn == 1) scrollX -= 50;
                        else scrollX += 50;
                    }
                }
                if(shoot || (relief && ((turn == 1 && !(player1.angle == 70 && player1.getBow().forceX == 0)) || (turn == 2 && !(player2.angle == 70 && player2.getBow().forceX == 0)))))
                {
                    if(turn == 1)
                    {
                        if(player1.angle != 70)
                        if(player1.angle <= 75 && player1.angle >= 65)
                            player1.setAngle(70);
                        else if(player1.angle < 70)
                            player1.setAngle(player1.angle+5);
                        else
                            player1.setAngle(player1.angle-5);
                        if(player1.getBow().forceX != 0)
                            player1.getBow().forceX -= 5;
                        if(player1.getBow().forceX >= -5 && player1.getBow().forceX <= 5)
                            player1.getBow().forceX = 0;
                    }else
                    {
                        if(player2.angle != 70)
                        if(player2.angle <= 75 && player2.angle >= 65)
                            player2.setAngle(70);
                        else if(player2.angle < 70)
                            player2.setAngle(player2.angle+5);
                        else
                            player2.setAngle(player2.angle-5);
                        if(player2.getBow().forceX != 0)
                            player2.getBow().forceX -= 5;
                        if(player2.getBow().forceX >= -5 && player2.getBow().forceX <= 5)
                            player2.getBow().forceX = 0;
                    }

                }

                if(turn == 1)
                {
                    if(scrollX == 0 && player1.angle == 70 && player1.getBow().forceX == 0 && relief)
                    {
                        turn = 2;
                        relief = false;
                        timer.stop();
                    }
                }else
                    if(scrollX == 0 && player2.angle == 70 && player2.getBow().forceX == 0 && relief)
                    {
                        turn = 1;
                        relief = false;
                        timer.stop();
                    }
            }
            
            slider.setScroll(scrollX);
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
            if(slider.isClicked(theTempXMouse, theTempYMouse))
                sliderControl = true;
            thePrevTempXMouse = theTempXMouse;
            thePrevTempYMouse = theTempYMouse;
        }//end public void mousePressed(mouseEvent event)

        public void mouseReleased(MouseEvent event)
        {
            if(!sliderControl)
            {
                shoot = true;
            }else
                sliderControl = false;
            
            timer.start();
            
            theTempXMouse = event.getX();
            theTempYMouse = event.getY();
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
            if(sliderControl)
                slider.setX(theTempXMouse-15);
            else
            {
                double power = Math.sqrt(Math.pow(theTempXMouse - thePrevTempXMouse, 2) + Math.pow(theTempYMouse - thePrevTempYMouse, 2));
                double angle;
                if(theTempXMouse - thePrevTempXMouse == 0)
                    if(thePrevTempYMouse - theTempYMouse > 0)
                        angle = -90.0;
                    else if(thePrevTempYMouse - theTempYMouse < 0)
                        angle = 90.0;
                    else
                        angle = 0;
                else
                    angle = Math.atan((thePrevTempYMouse - theTempYMouse)*1.0/(thePrevTempXMouse - theTempXMouse))*180*7/22;
                if(turn == 1)
                {
                    hp1.setPower(power);
                    hp1.setAngle(angle);
                    player1.setPower(power);
                    player1.setAngle(angle);
                }else
                {
                    hp2.setPower(power);
                    hp2.setAngle(angle);
                    player2.setPower(power);
                    player2.setAngle(angle);
                }
                
            }
            myDrawPanel.repaint();
                        
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
        public void paint(Graphics grap)
        {
            super.paint(grap);
            // draw horizon
            for(int i=1; i<=10; i++)
            {
                Color color = new Color((int)(255*(i/10.0)), (int)(255*(i/10.0)), (int)(255*(i/10.0)));
                grap.setColor(color);
                grap.drawLine(0, 699+i-scrollY, 4000, 699+i-scrollY);
            }
            if(slow && effect)
            {
                for(int i=1; i<=100; i++)
                {
                    Color color = new Color((int)(0+255*(i/100.0)), (int)(0), (int)(0));
                    grap.setColor(color);
                    grap.drawLine(0+i, 0+i, 1925-i, 0+i);
                    grap.drawLine(0+i, 0+i, 0+i, 1050-i);
                    grap.drawLine(1925-i, 0+i, 1925-i, 1050-i);
                    grap.drawLine(0+i, 1050-i, 1925-i, 1050-i);
                }
            }
            player1.getBow().drawArrow(grap);
            player2.getBow().drawArrow(grap);
            slider.draw(grap);
            hp1.draw(grap);
            hp2.draw(grap);
            player1.draw(grap);
            player2.draw(grap);
            
        }
    }
    
    private class Slider
    {
        private int x;
        private int y;
        
        public Slider()
        {
            x = 435;
            y = 790;
        }
        
        public void draw(Graphics grap)
        {
            for(int i=0; i<=3; i++)
            {
                Color color = new Color((int)(255*(i/3.0)), (int)(255*(i/3.0)), (int)(255*(i/3.0)));
                grap.setColor(color);
                grap.fillRect(200+i, 800+i, 500-2*i, 10-2*i);
            }
            for(int i=0; i<=9; i++)
            {
                Color color = new Color((int)(255*(i/9.0)), (int)(255*(i/9.0)), (int)(255*(i/9.0)));
                grap.setColor(color);
                grap.fillRect(x+i, y+i, 30-2*i, 30-2*i);
            }
        }
        
        public boolean isClicked(int x, int y)
        {
            return (Math.abs(this.x+15-x)<=15 && Math.abs(this.y+15-y)<=15);
        }
        
        public void setX(int x)
        {
            if(x >= 685)
                this.x = 685;
            else if(x <= 185)
                this.x = 185;
            else
                this.x = x;
            scrollX = (this.x - 435)*8;
        }
        
        public void setScroll(int scrollX)
        {
            this.x = scrollX/8 + 435;
            if(x >= 685)
                this.x = 685;
            else if(x <= 185)
                this.x = 185;
            else
                this.x = x;
        }
    
    }
    
    private class HPBar
    {
        private int hp;
        private int prevHp;
        private int x;
        private int y;
        private double power = 0;
        private double angle = 0;
        private JLabel powerLabel;
        private JLabel angleLabel;
        
        public HPBar(int x, int y)
        {
            hp = 100;
            prevHp = 100;
            this.x = x;
            this.y = y;
            powerLabel = new JLabel("Power = "+(int)(power*100)/100.0);
            powerLabel.setBounds(x, y+50, 150, 20);
            angleLabel = new JLabel("Angle = "+(int)(angle*100)/100.0);
            angleLabel.setBounds(x, y+70, 150, 20);
            myDrawPanel.add(powerLabel);
            myDrawPanel.add(angleLabel);
        }
        
        public void draw(Graphics grap)
        {
            for(int i=0; i<=10; i++)
            {
                Color color = new Color((int)(255*(i/10.0)), (int)(255*(i/10.0)), (int)(255*(i/10.0)));
                grap.setColor(color);
                grap.fillRect(x+i, y+i, 300-2*i, 30-2*i);
            }
            for(int i=0; i<=10; i++)
            {
                Color color = new Color((int)(255*(i/10.0)), (int)(255*(i/10.0)), 255);
                grap.setColor(color);
                grap.fillRect(x+20+i, y+2+i, prevHp*3-2*i, 25-2*i);
            }
        }
        
        public void setPower(double power)
        {
            if(power <= 500)
                this.power = (int)power/5;
            else
                this.power = 100;
            powerLabel.setText("Power = "+this.power);
        }
        
        public void setAngle(double angle)
        {
            this.angle = angle;
            angleLabel.setText("Angle = "+-1*(int)(angle*100)/100.0);
        }
        
    }
    
    private class Human
    {
        private int x;
        private int y = 520;
        private final int HEAD_DIAMETER = 30;
        private final int BODY_DIAMETER = 90;
        private Bow bow;
        private double angle;
        private int side;
        
        public Human(int x, int side)
        {
            this.x = x;
            this.side = side;
            if(this.side == 1)
                bow = new Bow(x+10+110, y+HEAD_DIAMETER+10, this.side);
            else
                bow = new Bow(x+20-110, y+HEAD_DIAMETER+10, this.side);
            angle = 70;
        }
        
        public void draw(Graphics grap)
        {
            if(this.side == 1)
            {
                // draw head
                for(int i=0; i<=10; i++)
                {
                    Color color = new Color((int)(255*(i/10.0)), (int)(255*(i/10.0)), (int)(255*(i/10.0)));
                    grap.setColor(color);
                    grap.fillOval(x+i-scrollX, y+i-scrollY, HEAD_DIAMETER-2*i, HEAD_DIAMETER-2*i);
                }

                Graphics2D g2d;
                g2d = (Graphics2D)grap.create();
                g2d.rotate(Math.toRadians(angle), x+10-scrollX, y+HEAD_DIAMETER+10-scrollY);
                // draw back upper hand
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x+25-scrollX;
                    int temp_y = y+HEAD_DIAMETER+10-scrollY;
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    g2d.setColor(color);
                    g2d.fillPolygon(new int[] {temp_x+3*i, temp_x+3+i, temp_x+50}, new int[] {temp_y+3*i, temp_y+10-i, temp_y}, 3);
                }
                // draw back lower hand
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x+25-scrollX+50;
                    int temp_y = y+HEAD_DIAMETER+10-scrollY;
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    g2d.setColor(color);
                    g2d.fillPolygon(new int[] {temp_x+3*i, temp_x+3+i, temp_x+50}, new int[] {temp_y+3*i, temp_y+10-i, temp_y}, 3);
                }
                // draw palm
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x+25-scrollX+90;
                    int temp_y = y+HEAD_DIAMETER+5-scrollY;
                    Color color = new Color((int)(255*(i/3.0)), (int)(255*(i/3.0)), (int)(255*(i/3.0)));
                    g2d.setColor(color);
                    g2d.fillOval(temp_x, temp_y, 10-i, 10-i);
                }
                g2d.dispose();

                // draw back upper leg
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x+15-scrollX;
                    int temp_y = y+HEAD_DIAMETER+BODY_DIAMETER-7-scrollY;
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    grap.setColor(color);
                    grap.fillPolygon(new int[] {temp_x+3*i, temp_x+3+i, temp_x+10}, new int[] {temp_y+3*i, temp_y+50-i, temp_y+6}, 3);
                }

                // draw back lower leg
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x+15-scrollX+3;
                    int temp_y = y+HEAD_DIAMETER+BODY_DIAMETER-7+50-scrollY;
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    grap.setColor(color);
                    grap.fillPolygon(new int[] {temp_x+3*i, temp_x-3+i, temp_x+7}, new int[] {temp_y+3*i, temp_y+50-i, temp_y+6}, 3);
                }

                // draw body
                for(int i=0; i<=12; i++)
                {
                    Color color = new Color((int)(255*(i/12.0)), (int)(255*(i/12.0)), (int)(255*(i/12.0)));
                    grap.setColor(color);
                    grap.fillOval(x+i-scrollX, y+HEAD_DIAMETER+i-scrollY, HEAD_DIAMETER-2*i, BODY_DIAMETER-2*i);
                }

                // draw front upper leg
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x+5-scrollX;
                    int temp_y = y+HEAD_DIAMETER+BODY_DIAMETER-7-scrollY;
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    grap.setColor(color);
                    grap.fillPolygon(new int[] {temp_x+3*i, temp_x+3+i, temp_x+10}, new int[] {temp_y+3*i, temp_y+50-i, temp_y+6}, 3);
                }

                // draw front lower leg
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x+5+3-scrollX;
                    int temp_y = y+HEAD_DIAMETER+BODY_DIAMETER-7+50-scrollY;
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    grap.setColor(color);
                    grap.fillPolygon(new int[] {temp_x+3*i, temp_x-3+i, temp_x+7}, new int[] {temp_y+3*i, temp_y+50-i, temp_y+6}, 3);
                }

                // draw front hand
                g2d = (Graphics2D)grap.create();
                g2d.rotate(Math.toRadians(angle), x+10-scrollX, y+HEAD_DIAMETER+10-scrollY);
                // draw front upper hand
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x-5-scrollX;
                    int temp_y = y+HEAD_DIAMETER+10-scrollY;
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    g2d.setColor(color);
                    g2d.fillPolygon(new int[] {temp_x+3*i, temp_x+3+i, x+75-scrollX-bow.getForceX()}, new int[] {temp_y+3*i, temp_y+10-i, y+HEAD_DIAMETER+10-scrollY+10}, 3);
                }
                // draw front lower hand
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x+70-scrollX-bow.getForceX();
                    int temp_y = y+HEAD_DIAMETER+10-scrollY;
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    g2d.setColor(color);
                    g2d.fillPolygon(new int[] {temp_x+3*i, temp_x+3+i, temp_x+50}, new int[] {temp_y+10+3*i, temp_y+20-i, temp_y}, 3);
                }
                // draw palm
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x+110-scrollX-bow.getForceX();
                    int temp_y = y+HEAD_DIAMETER+5-scrollY;
                    Color color = new Color((int)(255*(i/3.0)), (int)(255*(i/3.0)), (int)(255*(i/3.0)));
                    g2d.setColor(color);
                    g2d.fillOval(temp_x, temp_y, 10-i, 10-i);
                }
                g2d.dispose();

                // draw back feet
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x+15+3-3-scrollX;
                    int temp_y = y+HEAD_DIAMETER+BODY_DIAMETER-7+50+50-scrollY;
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    grap.setColor(color);
                    grap.fillPolygon(new int[] {temp_x+3*i, temp_x+5+i, temp_x+40}, new int[] {temp_y, temp_y+5-i, temp_y}, 3);
                }

                // draw front feet
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x+5+3-3-scrollX;
                    int temp_y = y+HEAD_DIAMETER+BODY_DIAMETER-7+50+50-scrollY;
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    grap.setColor(color);
                    grap.fillPolygon(new int[] {temp_x+3*i, temp_x+5+i, temp_x+40}, new int[] {temp_y, temp_y+5-i, temp_y}, 3);
                }
            }else
            {
                // draw head
                for(int i=0; i<=10; i++)
                {
                    Color color = new Color((int)(255*(i/10.0)), (int)(255*(i/10.0)), (int)(255*(i/10.0)));
                    grap.setColor(color);
                    grap.fillOval(x+i-scrollX, y+i-scrollY, HEAD_DIAMETER-2*i, HEAD_DIAMETER-2*i);
                }

                Graphics2D g2d;
                g2d = (Graphics2D)grap.create();
                g2d.rotate(Math.toRadians(-angle), x+20-scrollX, y+HEAD_DIAMETER+10-scrollY);
                // draw back upper hand
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x+5-scrollX;
                    int temp_y = y+HEAD_DIAMETER+10-scrollY;
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    g2d.setColor(color);
                    g2d.fillPolygon(new int[] {temp_x-3*i, temp_x-3+i, temp_x-50}, new int[] {temp_y+3*i, temp_y+10-i, temp_y}, 3);
                }
                // draw back lower hand
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x+5-scrollX-50;
                    int temp_y = y+HEAD_DIAMETER+10-scrollY;
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    g2d.setColor(color);
                    g2d.fillPolygon(new int[] {temp_x-3*i, temp_x-3+i, temp_x-50}, new int[] {temp_y+3*i, temp_y+10-i, temp_y}, 3);
                }
                // draw palm
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x+5-scrollX-90-10;
                    int temp_y = y+HEAD_DIAMETER+5-scrollY;
                    Color color = new Color((int)(255*(i/3.0)), (int)(255*(i/3.0)), (int)(255*(i/3.0)));
                    g2d.setColor(color);
                    g2d.fillOval(temp_x, temp_y, 10-i, 10-i);
                }
                g2d.dispose();

                // draw back upper leg
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x+25-scrollX;
                    int temp_y = y+HEAD_DIAMETER+BODY_DIAMETER-7-scrollY;
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    grap.setColor(color);
                    grap.fillPolygon(new int[] {temp_x-3*i, temp_x-3+i, temp_x-10}, new int[] {temp_y+3*i, temp_y+50-i, temp_y+6}, 3);
                }

                // draw back lower leg
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x+25-scrollX-3;
                    int temp_y = y+HEAD_DIAMETER+BODY_DIAMETER-7+50-scrollY;
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    grap.setColor(color);
                    grap.fillPolygon(new int[] {temp_x-3*i, temp_x-3+i, temp_x-7}, new int[] {temp_y+3*i, temp_y+50-i, temp_y+6}, 3);
                }

                // draw body
                for(int i=0; i<=12; i++)
                {
                    Color color = new Color((int)(255*(i/12.0)), (int)(255*(i/12.0)), (int)(255*(i/12.0)));
                    grap.setColor(color);
                    grap.fillOval(x+i-scrollX, y+HEAD_DIAMETER+i-scrollY, HEAD_DIAMETER-2*i, BODY_DIAMETER-2*i);
                }

                // draw front upper leg
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x+15-scrollX;
                    int temp_y = y+HEAD_DIAMETER+BODY_DIAMETER-7-scrollY;
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    grap.setColor(color);
                    grap.fillPolygon(new int[] {temp_x-3*i, temp_x-3+i, temp_x-10}, new int[] {temp_y+3*i, temp_y+50-i, temp_y+6}, 3);
                }

                // draw front lower leg
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x+15-scrollX-3;
                    int temp_y = y+HEAD_DIAMETER+BODY_DIAMETER-7+50-scrollY;
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    grap.setColor(color);
                    grap.fillPolygon(new int[] {temp_x-3*i, temp_x-3+i, temp_x-7}, new int[] {temp_y+3*i, temp_y+50-i, temp_y+6}, 3);
                }

                // draw front hand
                g2d = (Graphics2D)grap.create();
                g2d.rotate(Math.toRadians(-angle), x+20-scrollX, y+HEAD_DIAMETER+10-scrollY);
                // draw front upper hand
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x+35-scrollX;
                    int temp_y = y+HEAD_DIAMETER+10-scrollY;
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    g2d.setColor(color);
                    g2d.fillPolygon(new int[] {temp_x-3*i, temp_x-3+i, x-50-scrollX+bow.getForceX()}, new int[] {temp_y+3*i, temp_y+10-i, y+HEAD_DIAMETER+10-scrollY+10}, 3);
                }
                // draw front lower hand
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x-40-scrollX+bow.getForceX();
                    int temp_y = y+HEAD_DIAMETER+10-scrollY;
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    g2d.setColor(color);
                    g2d.fillPolygon(new int[] {temp_x-3*i, temp_x-3+i, temp_x-50}, new int[] {temp_y+10+3*i, temp_y+20-i, temp_y}, 3);
                }
                // draw palm
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x-90-scrollX+bow.getForceX();
                    int temp_y = y+HEAD_DIAMETER+5-scrollY;
                    Color color = new Color((int)(255*(i/3.0)), (int)(255*(i/3.0)), (int)(255*(i/3.0)));
                    g2d.setColor(color);
                    g2d.fillOval(temp_x, temp_y, 10-i, 10-i);
                }
                g2d.dispose();

                // draw back feet
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x+25+3-3-scrollX;
                    int temp_y = y+HEAD_DIAMETER+BODY_DIAMETER-7+50+50-scrollY;
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    grap.setColor(color);
                    grap.fillPolygon(new int[] {temp_x-3*i, temp_x-5+i, temp_x-40}, new int[] {temp_y, temp_y+5-i, temp_y}, 3);
                }

                // draw front feet
                for(int i=0; i<=3; i++)
                {
                    int temp_x = x+15+3-3-scrollX;
                    int temp_y = y+HEAD_DIAMETER+BODY_DIAMETER-7+50+50-scrollY;
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    grap.setColor(color);
                    grap.fillPolygon(new int[] {temp_x-3*i, temp_x-5+i, temp_x-40}, new int[] {temp_y, temp_y+5-i, temp_y}, 3);
                }
            }
            
            
            bow.draw(grap);
            
        }
        
        public void setPower(double power)
        {
            bow.setForceX(power);
        }
        
        public void setAngle(double angle)
        {
            bow.setAngle(angle);
            this.angle = angle;
        }
        
        public Bow getBow()
        {
            return this.bow;
        }
    }
    
    private class Bow
    {
        private int x;
        private int y;
        private int forceX;
        private double angle;
        private double speedX;
        private double speedY;
        private int arrowX;
        private int arrowY;
        private double arrowAngle;
        private int side;
        private LinkedList<Integer> arrows = new LinkedList<>();
        private LinkedList<Double> arrowsRed = new LinkedList<>();
        
        public Bow(int x, int y, int side)
        {
            this.x = x;
            this.y = y;
            forceX = 0;
            angle = 70;
            speedX = 0;
            speedY = 0;
            this.side = side;
        }
        
        private void draw(Graphics grap)
        {
            int temp_x = this.x - scrollX;
            int temp_y = this.y - scrollY;
            Graphics2D g2d;
            g2d = (Graphics2D)grap.create();
            if(this.side == 1)
            {
                g2d.rotate(Math.toRadians(angle), temp_x-110, temp_y);

                for(int i=0; i<=3; i++)
                {
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    g2d.setColor(color);
                    g2d.fillPolygon(new int[] {temp_x, temp_x-8, temp_x-20}, new int[] {temp_y, temp_y, temp_y-100}, 3);
                }

                for(int i=0; i<=3; i++)
                {
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    g2d.setColor(color);
                    g2d.fillPolygon(new int[] {temp_x, temp_x-8, temp_x-20}, new int[] {temp_y, temp_y, temp_y+100}, 3);
                }

                g2d.setColor(Color.BLACK);
                g2d.drawLine(temp_x-20, temp_y-100, temp_x-8-forceX, temp_y);
                g2d.drawLine(temp_x-20, temp_y+100, temp_x-8-forceX, temp_y);

                if(!shoot || turn != 1)
                    drawArrow(grap);

                if(arrows.size()!=0)
                {
                    for(int i=0; i<arrows.size(); i+=2)
                    {
                        if(i==arrows.size()-2 && turn == 1)
                            grap.setColor(Color.GREEN);
                        else
                            grap.setColor(Color.BLACK);
                        grap.drawLine(arrows.get(i)-scrollX, arrows.get(i+1)-scrollY, arrows.get(i)-118-scrollX, arrows.get(i+1)-scrollY);
                        grap.drawLine(arrows.get(i)-scrollX, arrows.get(i+1)-scrollY, arrows.get(i)-10-scrollX, arrows.get(i+1)+10-scrollY);
                        grap.drawLine(arrows.get(i)-scrollX, arrows.get(i+1)-scrollY, arrows.get(i)-10-scrollX, arrows.get(i+1)-10-scrollY);
                    }
                }
                if(arrowsRed.size()!=0)
                {
                    Graphics2D g2d2;
                    for(int i=0; i<arrowsRed.size(); i+= 3)
                    {
                        g2d2 = (Graphics2D)grap.create();
                        g2d2.rotate(arrowsRed.get(i+2), arrowsRed.get(i)-110-scrollX, arrowsRed.get(i+1)-scrollY);
                        g2d2.setColor(Color.red);
                        int x = (int)Math.round(arrowsRed.get(i));
                        int y = (int)Math.round(arrowsRed.get(i+1));
                        g2d2.drawLine(x-scrollX, y-scrollY, x-118-scrollX, y-scrollY);
                        g2d2.drawLine(x-scrollX, y-scrollY, x-10-scrollX, y+10-scrollY);
                        g2d2.drawLine(x-scrollX, y-scrollY, x-10-scrollX, y-10-scrollY);
                        g2d2.dispose();
                    }
                }
            }else
            {
                g2d.rotate(Math.toRadians(-angle), temp_x+110, temp_y);

                for(int i=0; i<=3; i++)
                {
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    g2d.setColor(color);
                    g2d.fillPolygon(new int[] {temp_x, temp_x+8, temp_x+20}, new int[] {temp_y, temp_y, temp_y-100}, 3);
                }

                for(int i=0; i<=3; i++)
                {
                    Color color = new Color((int)(255*(i/5.0)), (int)(255*(i/5.0)), (int)(255*(i/5.0)));
                    g2d.setColor(color);
                    g2d.fillPolygon(new int[] {temp_x, temp_x+8, temp_x+20}, new int[] {temp_y, temp_y, temp_y+100}, 3);
                }

                g2d.setColor(Color.BLACK);
                g2d.drawLine(temp_x+20, temp_y-100, temp_x+8+forceX, temp_y);
                g2d.drawLine(temp_x+20, temp_y+100, temp_x+8+forceX, temp_y);

                if(!shoot || turn != 2)
                    drawArrow(grap);

                if(arrows.size()!=0)
                {
                    for(int i=0; i<arrows.size(); i+=2)
                    {
                        if(i==arrows.size()-2 && turn == 2)
                            grap.setColor(Color.GREEN);
                        else
                            grap.setColor(Color.BLACK);
                        grap.drawLine(arrows.get(i)-scrollX, arrows.get(i+1)-scrollY, arrows.get(i)+118-scrollX, arrows.get(i+1)-scrollY);
                        grap.drawLine(arrows.get(i)-scrollX, arrows.get(i+1)-scrollY, arrows.get(i)+10-scrollX, arrows.get(i+1)+10-scrollY);
                        grap.drawLine(arrows.get(i)-scrollX, arrows.get(i+1)-scrollY, arrows.get(i)+10-scrollX, arrows.get(i+1)-10-scrollY);
                    }
                }
                if(arrowsRed.size()!=0)
                {
                    Graphics2D g2d2;
                    for(int i=0; i<arrowsRed.size(); i+= 3)
                    {
                        g2d2 = (Graphics2D)grap.create();
                        g2d2.rotate(-arrowsRed.get(i+2), arrowsRed.get(i)-110-scrollX, arrowsRed.get(i+1)-scrollY);
                        g2d2.setColor(Color.red);
                        int x = (int)Math.round(arrowsRed.get(i));
                        int y = (int)Math.round(arrowsRed.get(i+1));
                        g2d2.drawLine(x-scrollX, y-scrollY, x+118-scrollX, y-scrollY);
                        g2d2.drawLine(x-scrollX, y-scrollY, x+10-scrollX, y+10-scrollY);
                        g2d2.drawLine(x-scrollX, y-scrollY, x+10-scrollX, y-10-scrollY);
                        g2d2.dispose();
                    }
                }
            }
            
                        
            g2d.dispose();
        }
        
        public void drawArrow(Graphics grap)
        {
            Graphics2D g2d;
            g2d = (Graphics2D)grap.create();
            g2d.setColor(Color.BLACK);
            
            if(this.side == 1)
            {
                if(shoot && turn == 1)
                {
                    if(speedX == 0 && arrowAngle == 0)
                        g2d.setColor(Color.GREEN);
                    g2d.rotate(arrowAngle, arrowX-118-(100-forceX)-scrollX, arrowY-scrollY);
                    g2d.drawLine(arrowX-scrollX, arrowY-scrollY, arrowX-118-scrollX, arrowY-scrollY);
                    g2d.drawLine(arrowX-scrollX, arrowY-scrollY, arrowX-10-scrollX, arrowY+10-scrollY);
                    g2d.drawLine(arrowX-scrollX, arrowY-scrollY, arrowX-10-scrollX, arrowY-10-scrollY);
                    
                    if(arrowX >= player2.x && arrowX-20 <= player2.x+30 && arrowY+118*Math.tan(arrowAngle) >= player2.y)
                    {
                        arrowsRed.add(arrowX*1.0+5);
                        if(arrowY < 730) arrowY +=5;
                        arrowsRed.add(arrowY*1.0);
                        arrowsRed.add(arrowAngle);
                        scrollX = 0;
                        scrollY = 0;
                        repaint();
                        shoot = false;
                        reset = true;
                        slow = true;
                        timer.stop();
                        timer = new Timer((int)1000/10, new ActionHandler());
                        timer.start();
                        hp2.hp -= 25;
                    }
                    if(speedX == 0 && arrowAngle == 0 && speedY == 0 && arrowY == 730)
                    {
                        arrows.add(arrowX);
                        arrows.add(arrowY);
                        scrollX = 0;
                        scrollY = 0;
                        repaint();
                        shoot = false;
                        reset = true;
                    }

                    speedY = speedY + GRAVITY*0.16;
                    arrowAngle = Math.atan(speedY/speedX);
                    arrowX = (int)Math.round(arrowX + speedX * 0.16);
                    arrowY = (int)Math.round(arrowY + speedY * 0.16);

                    if((118+100-forceX)*Math.sin(arrowAngle)+arrowY >= 750)
                    {
                        arrowY = 730;
                        speedX = 0;
                        speedY = 0;
                        arrowAngle = 0;
                    }
                    if(arrowY <= maxY/5)
                    {
                        scrollY = arrowY-380;
                    }
                    if(arrowX-scrollX >= maxX/2)
                    {
                        scrollX = arrowX-1000;
                    }
                }else
                {
                    g2d.rotate(Math.toRadians(angle), x-110-scrollX, y-scrollY);
                    g2d.drawLine(x+110-forceX-scrollX, y-scrollY, x-8-forceX-scrollX, y-scrollY);
                    g2d.drawLine(x+110-forceX-scrollX, y-scrollY, x+100-forceX-scrollX, y+10-scrollY);
                    g2d.drawLine(x+110-forceX-scrollX, y-scrollY, x+100-forceX-scrollX, y-10-scrollY);
                    arrowX = x+110-forceX;
                    arrowY = y-scrollY;
                }

            }else
            {
                if(shoot && turn == 2)
                {
                    if(speedX == 0 && arrowAngle == 0)
                        g2d.setColor(Color.GREEN);
                    g2d.rotate(-arrowAngle, arrowX+118-(100-forceX)-scrollX, arrowY-scrollY);
                    g2d.drawLine(arrowX-scrollX, arrowY-scrollY, arrowX+118-scrollX, arrowY-scrollY);
                    g2d.drawLine(arrowX-scrollX, arrowY-scrollY, arrowX+10-scrollX, arrowY+10-scrollY);
                    g2d.drawLine(arrowX-scrollX, arrowY-scrollY, arrowX+10-scrollX, arrowY-10-scrollY);

                    if(arrowX+20 >= player1.x && arrowX <= player1.x+30 && arrowY >= player1.y)
                    {
                        arrowsRed.add(arrowX*1.0-5);
                        if(arrowY < 730) arrowY +=5;
                        arrowsRed.add(arrowY*1.0);
                        arrowsRed.add(arrowAngle);
                        scrollX = 0;
                        scrollY = 0;
                        repaint();
                        shoot = false;
                        reset = true;
                        slow = true;
                        timer.stop();
                        timer = new Timer((int)1000/10, new ActionHandler());
                        timer.start();
                        hp1.hp -= 25;
                    }
                    if(speedX == 0 && arrowAngle == 0 && speedY == 0 && arrowY == 730)
                    {
                        arrows.add(arrowX);
                        arrows.add(arrowY);
                        scrollX = 0;
                        scrollY = 0;
                        repaint();
                        shoot = false;
                        reset = true;
                    }

                    speedY = speedY + GRAVITY*0.16;
                    arrowAngle = Math.atan(speedY/speedX);
                    arrowX = (int)Math.round(arrowX - speedX * 0.16);
                    arrowY = (int)Math.round(arrowY + speedY * 0.16);

                    if((118+100-forceX)*Math.sin(arrowAngle)+arrowY >= 750)
                    {
                        arrowY = 730;
                        speedX = 0;
                        speedY = 0;
                        arrowAngle = 0;
                    }
                    if(arrowY <= maxY/5)
                    {
                        scrollY = arrowY-380;
                    }
                    if(arrowX-scrollX <= maxX/2)
                    {
                        scrollX = arrowX-1000;
                    }
                }else
                {
                    g2d.rotate(Math.toRadians(-angle), x+110-scrollX, y-scrollY);
                    g2d.drawLine(x-110+forceX-scrollX, y-scrollY, x+8+forceX-scrollX, y-scrollY);
                    g2d.drawLine(x-110+forceX-scrollX, y-scrollY, x-100+forceX-scrollX, y+10-scrollY);
                    g2d.drawLine(x-110+forceX-scrollX, y-scrollY, x-100+forceX-scrollX, y-10-scrollY);
                    arrowX = x+110-forceX;
                    arrowY = y-scrollY;
                }
            }
                
            g2d.dispose();
        }
        
        public void setForceX(double power)
        {
            if(power <= 500)
                forceX = (int)power/5;
            else
                forceX = 100;
            speedX = forceX*3 * Math.cos(Math.toRadians(angle));
            speedY = forceX*3 * Math.sin(Math.toRadians(angle));
        }
        
        public void setAngle(double angle)
        {
            this.angle = angle;
            if(!shoot)
                arrowAngle = Math.toRadians(angle);
        }
        
        public int getForceX()
        {
            return this.forceX;
        }
        
    }
    
    public static void main(String args[])
    {
        new Bowman();
    }
    
}
