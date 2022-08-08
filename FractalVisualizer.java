/*
 * Fractal Visualizer by Harrison Collazo
 */

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;

public class FractalVisualizer extends JFrame {
	
	static final int WIDTH  = 600;
	static final int HEIGHT = 600;
	
	Canvas canvas;
	BufferedImage fractalImage;
	
    //Sets finite number of iterations to determine if complex point is within the fractal set. Can be lowered to 100 to improve runtime
	static final int MAX_ITER = 200;
	
	static final double DEFAULT_ZOOM       = 100.0;
	static final double DEFAULT_TOP_LEFT_X = -3.0;
    //Y-Value flipped compared to X
	static final double DEFAULT_TOP_LEFT_Y = +3.0;
	
	double zoomFactor = DEFAULT_ZOOM;
	double topLeftX   = DEFAULT_TOP_LEFT_X;
	double topLeftY   = DEFAULT_TOP_LEFT_Y;

// -------------------------------------------------------------------
    //Initializes GUI and adds canvas to blank frame
    public FractalVisualizer() {
		setInitialGUIProperties();
		addCanvas();
        canvas.addKeyStrokeEvents();
		updateFractal();
	}
	
// -------------------------------------------------------------------

	//Calls the visualizer to start
    public static void main(String[] args) {
		new FractalVisualizer();
	}
	
// -------------------------------------------------------------------

	//Builds canvas to put on top of frame (window turns black instead of blank)	
    private void addCanvas() {
			
			canvas = new Canvas();
			fractalImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
			canvas.setVisible(true);
			this.add(canvas, BorderLayout.CENTER);
			
		} // addCanvas

// -------------------------------------------------------------------

		//Builds frame to put canvas on (blank window)
		private void setInitialGUIProperties() {
			
			this.setTitle("Fractal Visualizer");
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setSize(WIDTH, HEIGHT);
			this.setResizable(false);
			this.setLocationRelativeTo(null);
			this.setVisible(true);
			
		} // setInitialGUIProperties
		
// -------------------------------------------------------------------
	private double getXPos(double x) {
		return x/zoomFactor + topLeftX;
	} // getXPos
// -------------------------------------------------------------------
	private double getYPos(double y) {
		return y/zoomFactor - topLeftY;
	} // getYPos
// -------------------------------------------------------------------
	
	/**
	 * Updates the fractal by computing the number of iterations
	 * for each point in the fractal and changing the color
	 * based on that.
	 **/
	
	public void updateFractal() {
		
        //loop through all pixels on canvas
		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++ ) {
				
                //Real values on x-axis, imaginary values on y-axis
				double c_r = getXPos(x);
				double c_i = getYPos(y);
				
                //calculate number of iterations
				int iterCount = computeIterations(c_r, c_i);
				
                //set pixel color based on number of iterations
				int pixelColor = makeColor(iterCount);
				fractalImage.setRGB(x, y, pixelColor);
				
			}
		}
		
		canvas.repaint();
		
	} // updateFractal
// -------------------------------------------------------------------	
	private int makeColor(int iterCount) {
		
        //color stored in 3 bytes (8-bits/1-byte per RGB integer value)
		int color = 0b001011100001100101101001; 
		int mask  = 0b000000000000010101110111; 
        //13 derived from number of leading zeros in mask bytes
		int shiftMag = iterCount / 13;
		
		if (iterCount == MAX_ITER) 
			return Color.BLACK.getRGB();
		
		return color | (mask << shiftMag);
		
	} // makeColor

// -------------------------------------------------------------------

	private int computeIterations(double c_r, double c_i) {
		
		/*
		Mandelbrot Fractal formula as follows
		Let c = c_r + c_i
		Let z = z_r + z_i
		
		z' = z*z + c
		   = (z_r + z_i)(z_r + z_i) + c_r + c_i
			 = z_r² + 2*z_r*z_i - z_i² + c_r + c_i
			
			 z_r' = z_r² - z_i² + c_r
			 z_i' = 2*z_i*z_r + c_i
		
		*/

		double z_r = 0.0;
		double z_i = 0.0;
		
		int iterCount = 0;

		// Modulus (distance) formula:
		// √(a² + b²) <= 2.0
		// a² + b² <= 4.0   <-- Faster check
		while ( z_r*z_r + z_i*z_i <= 4.0 ) {
			
			double z_r_tmp = z_r;
			
			z_r = z_r*z_r - z_i*z_i + c_r;
			z_i = 2*z_i*z_r_tmp + c_i;
			
			// Point was inside the Mandelbrot set
			if (iterCount >= MAX_ITER) 
				return MAX_ITER;
			
			iterCount++;
			
		}
		
		// Complex point was outside Mandelbrot set
		return iterCount;
		
	} // computeIterations

//-----------------------------------------------------------------

//keyboard actions

    private void moveUp() {
        double currentHeight = HEIGHT / zoomFactor;
        //6 is arbitrary, can be changed to a third or half
        topLeftY += currentHeight / 6;
        updateFractal();
    }

    private void moveDown() {
        double currentHeight = HEIGHT / zoomFactor;
        //6 is arbitrary, can be changed to a third or half
        topLeftY -= currentHeight / 6;
        updateFractal();
    }

    private void moveLeft() {
        double currentWidth = WIDTH / zoomFactor;
        //6 is arbitrary, can be changed to a third or half
        topLeftX -= currentWidth / 6;
        updateFractal();
    }

    private void moveRight() {
        double currentWidth = WIDTH / zoomFactor;
        //6 is arbitrary, can be changed to a third or half
        topLeftX += currentWidth / 6;
        updateFractal();
    }
// -------------------------------------------------------------------		

	private void adjustZoom( double newX, double newY, double newZoomFactor ) {
		
		topLeftX += newX/zoomFactor;
		topLeftY -= newY/zoomFactor;
		
		zoomFactor = newZoomFactor;
		
        //re-centering view after shifting x and y
		topLeftX -= ( WIDTH/2) / zoomFactor;
		topLeftY += (HEIGHT/2) / zoomFactor;
		
		updateFractal();
		
	}

// -------------------------------------------------------------------	
	
    //Funtion to put canvas on top of JFrame
	private class Canvas extends JPanel implements MouseListener {
		
		public Canvas() {
			addMouseListener(this);
		}
		
		@Override public Dimension getPreferredSize() {
			return new Dimension(WIDTH, HEIGHT);
		} // getPreferredSize
		
		@Override public void paintComponent(Graphics drawingObj) {
			drawingObj.drawImage( fractalImage, 0, 0, null );
		} // paintComponent
		
		@Override public void mousePressed(MouseEvent mouse) {
			
			double x = (double) mouse.getX();
			double y = (double) mouse.getY();
			
			switch(mouse.getButton()) {
				
				// Left - Zoom in
				case MouseEvent.BUTTON1:
					adjustZoom(x, y, zoomFactor*2);
					break;
				
				// Right - Zoom out
				case MouseEvent.BUTTON3:
					adjustZoom(x, y, zoomFactor/2);
					break;
				
			}
			
		}

        //key-stroke additions to allow for panning around fractal
        public void addKeyStrokeEvents() {
            KeyStroke wKey = KeyStroke.getKeyStroke(KeyEvent.VK_W, 0);
            KeyStroke aKey = KeyStroke.getKeyStroke(KeyEvent.VK_A, 0);
            KeyStroke sKey = KeyStroke.getKeyStroke(KeyEvent.VK_S, 0);
            KeyStroke dKey = KeyStroke.getKeyStroke(KeyEvent.VK_D, 0);

            Action wPressed = new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    moveUp();
                }
            };

            Action aPressed = new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    moveLeft();
                }
            };

            Action sPressed = new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    moveDown();
                }
            };

            Action dPressed = new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    moveRight();
                }
            };

            //pseudo hash-map to store and then call key values via Input and Action
            this.getInputMap().put(wKey, "w_key");
            this.getInputMap().put(aKey, "a_key");
            this.getInputMap().put(sKey, "s_key");
            this.getInputMap().put(dKey, "d_key");

            this.getActionMap().put("w_key", wPressed);
            this.getActionMap().put("a_key", aPressed);
            this.getActionMap().put("s_key", sPressed);
            this.getActionMap().put("d_key", dPressed);
        }
		
		@Override public void mouseReleased(MouseEvent mouse){ }
		@Override public void mouseClicked(MouseEvent mouse) { }
		@Override public void mouseEntered(MouseEvent mouse) { }
		@Override public void mouseExited (MouseEvent mouse) { }
		
	} // Canvas
	
} // FractalVisualizer