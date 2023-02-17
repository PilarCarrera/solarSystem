import java.awt.event.KeyEvent;
import java.nio.*;
import java.lang.Math;
import javax.swing.*;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CCW;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;
import com.jogamp.common.nio.Buffers;
import org.joml.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class HW2_FINAL extends JFrame implements GLEventListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
	
	private static final long serialVersionUID = 1L;
	private GLCanvas myCanvas;
	private int renderingProgram;
	private int renderingProgram2;
	private int vao[] = new int[1];
	private int vbo[] = new int[7];

	//textures
	private int earthTexture;
	private int moonTexture;
	private int sunTexture;
	private int marsTexture;
	private int myFaceTexture;
	private boolean showAxis = false;

	
	
	private Sphere mySphere;
	private int numSphereVerts;
	
	// allocate variables for display() function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4fStack mvStack = new Matrix4fStack(30);

	private Matrix4f pMat = new Matrix4f();  // perspective matrix
	private int mvLoc, projLoc;
	private float aspect;
	private double tf;
	private double startTime;
	private double elapsedTime;
	
	//Camera
	private float targetX, targetY, targetZ;//Where camera is looking at
	private Vector3f camVec, targetVec, f,r,u;//Vectors that define camera
	private float Ddist=1.0f, Dangle=0.01f; //Distance and angle increments
	private float cubeLocX, cubeLocY, cubeLocZ;
	private float cameraX, cameraY, cameraZ;
	private float sphLocX, sphLocY, sphLocZ;
	
	public HW2_FINAL() {	
		
		setTitle("Project 2");
		setSize(800, 800);
        GLProfile profile = GLProfile.get(GLProfile.GL4);
        GLCapabilities capabilities = new GLCapabilities(profile);
		myCanvas = new GLCanvas(capabilities);
		myCanvas.addGLEventListener(this);
		myCanvas.addKeyListener(this);
		
		this.add(myCanvas);
		this.setVisible(true);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		Animator animator = new Animator(myCanvas);
		animator.start();
	}

	
	public void display(GLAutoDrawable drawable) {	
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glClear(GL_COLOR_BUFFER_BIT);
		
		elapsedTime = System.currentTimeMillis() - startTime;

		gl.glUseProgram(renderingProgram);

		mvLoc = gl.glGetUniformLocation(renderingProgram, "mv_matrix");
		projLoc = gl.glGetUniformLocation(renderingProgram, "proj_matrix");
		

		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));
		
		
		mvStack.pushMatrix();
		mvStack.identity();
		mvStack.lookAt(camVec, targetVec, new Vector3f(0.0f,1.0f,0.0f));

		if(showAxis) {
			
			doAxis();
			
		}	
		

		gl.glUseProgram(renderingProgram);

		mvLoc = gl.glGetUniformLocation(renderingProgram, "mv_matrix");
		projLoc = gl.glGetUniformLocation(renderingProgram, "proj_matrix");
		
		mvStack.pushMatrix();
		mvStack.translate(-cameraX, -cameraY, -cameraZ);
		
		tf = elapsedTime/1000.0;  // time factor

		// ---------------------- Sun
		mvStack.pushMatrix();
		mvStack.translate(0.0f, 0.0f, 0.0f);
		mvStack.pushMatrix();
		mvStack.rotate((float)tf, 0.0f, 1.0f, 0.0f);

		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		gl.glEnable(GL_DEPTH_TEST);


		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, sunTexture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVerts);
		
		mvStack.popMatrix();
		
		//-----------------------  Earth
		mvStack.pushMatrix();
		mvStack.rotate((float)0.7854, 0.0f, 0.0f, 1.0f);
		mvStack.translate((float)Math.sin(tf)*4.0f, 0.0f, (float)Math.cos(tf)*4.0f);
		
		mvStack.pushMatrix();
		mvStack.rotate((float)tf, 0.0f, 1.0f, 0.0f);
		
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, earthTexture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVerts);
		
		mvStack.popMatrix();
		
		//----------------------- Moon --- Earth
		mvStack.pushMatrix();
		mvStack.translate(0.0f, (float)Math.sin(tf)*3.0f, (float)Math.cos(tf)*3.0f);
		mvStack.rotate((float)tf, 0.0f, 0.0f, 1.0f);
		mvStack.scale(0.25f, 0.25f, 0.25f);
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, moonTexture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVerts);
		mvStack.popMatrix(); mvStack.popMatrix();
		
		//------------------------- Mars
		mvStack.pushMatrix();
		mvStack.rotate((float)4, 0.0f, 0.0f, 1.0f);
		mvStack.translate((float)Math.sin(tf)*7.0f, 2.0f, (float)Math.cos(tf)*6.0f);
		mvStack.pushMatrix();
		mvStack.rotate((float)tf, 0.0f, 1.0f, 0.0f);
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, marsTexture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVerts);
		mvStack.popMatrix();
		
		//------------------------ Moon -- Mars

		mvStack.pushMatrix();
		mvStack.translate(1.0f, (float)Math.sin(tf)*3.0f, (float)Math.cos(tf)*3.0f);
		mvStack.rotate((float)tf, 0.0f, 0.0f, 1.0f);
		mvStack.scale(0.25f, 0.25f, 0.25f);
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, moonTexture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVerts);
		mvStack.popMatrix(); mvStack.popMatrix();
		
		//--------------------------- Pentágono
		
		mvStack.pushMatrix();
		mvStack.scale(0.5f, 0.5f, 0.5f);
		mvStack.rotate((float)-2, 0.0f, 0.0f, 1.0f);
		mvStack.translate((float)Math.sin(tf)*5.0f, 3.0f, (float)Math.cos(tf)*5.0f);

		mvStack.pushMatrix();
		mvStack.rotate((float)tf, 0.0f, 1.0f, 0.0f);
		
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, myFaceTexture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDrawArrays(GL_TRIANGLES, 0, numSphereVerts);
		
		mvStack.popMatrix();
				
		mvStack.popMatrix(); mvStack.popMatrix();
		mvStack.popMatrix(); mvStack.popMatrix(); 

	}

	private void doAxis() {
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		gl.glUseProgram(renderingProgram2);

		int mvLoc = gl.glGetUniformLocation(renderingProgram2, "mv_matrix");		
		int projLoc = gl.glGetUniformLocation(renderingProgram2, "proj_matrix");
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
		gl.glUniformMatrix4fv(projLoc, 1, false, pMat.get(vals));

		mvStack.pushMatrix();
		mvStack.translate(5, -5, 0);
		
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));

		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glDrawArrays(GL_LINE,0,6);
		
		mvStack.popMatrix();
				
	}
	
	public void init(GLAutoDrawable drawable) {
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		startTime = System.currentTimeMillis();
		
		//shaders
		renderingProgram = Utils.createShaderProgram("HW2_data/vertShader.glsl", "HW2_data/fragShader.glsl");
		renderingProgram2 = Utils.createShaderProgram("HW2_data/vertShader2D.glsl", "HW2_data/fragShader2D.glsl");
		
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);

		setupVertices();
		
		initCamera();
		
		initTexture();
		
	}

	private void initCamera() {
		
		cameraX = 0.0f; cameraY = 0.0f; cameraZ = 5f;
		sphLocX = 0.0f; sphLocY = 0.0f; sphLocZ = -1.0f;
		targetX=0.0f; targetY=0.0f; targetZ=0.0f;
		cubeLocX = 0.0f; cubeLocY = -2.0f; cubeLocZ = 0.0f;
		camVec=new Vector3f(cameraX, cameraY, cameraZ);
		targetVec=new Vector3f(targetX, targetY, targetZ);
		
	}

	private void initTexture() {
		
		earthTexture = Utils.loadTexture("HW2_data/earth.jpg");
		moonTexture =  Utils.loadTexture("HW2_data/moon.jpg");
		sunTexture = Utils.loadTexture("HW2_data/2k_sun.jpg");
		marsTexture = Utils.loadTexture("HW2_data/mars.jpg");
		myFaceTexture = Utils.loadTexture("HW2_data/fotoProfesional.jpg");
	
	}

	private void setupVertices() {
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
	
		mySphere = new Sphere(96);
		numSphereVerts = mySphere.getIndices().length;
	
		int[] indices = mySphere.getIndices();
		Vector3f[] vert = mySphere.getVertices();
		Vector2f[] tex  = mySphere.getTexCoords();
		Vector3f[] norm = mySphere.getNormals();
		
		float[] pvalues = new float[indices.length*3];
		float[] tvalues = new float[indices.length*2];
		float[] nvalues = new float[indices.length*3];
		
		for (int i=0; i<indices.length; i++) {
			
			pvalues[i*3] = (float) (vert[indices[i]]).x;
			pvalues[i*3+1] = (float) (vert[indices[i]]).y;
			pvalues[i*3+2] = (float) (vert[indices[i]]).z;
			tvalues[i*2] = (float) (tex[indices[i]]).x;
			tvalues[i*2+1] = (float) (tex[indices[i]]).y;
			nvalues[i*3] = (float) (norm[indices[i]]).x;
			nvalues[i*3+1]= (float)(norm[indices[i]]).y;
			nvalues[i*3+2]=(float) (norm[indices[i]]).z;
		}
		
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);
		
		//Esfera
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4,norBuf, GL_STATIC_DRAW);
		
		//Pentágono
		Pentagon myPrism = new Pentagon(2, 2f);
		
		float[] prismVectors = myPrism.getVertices();
		float[] prismTextCoords = myPrism.getTextureCoordinates();
		float numVertices = (prismVectors.length / 3);
		
		//vértices
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		FloatBuffer vertBufPrism = Buffers.newDirectFloatBuffer(prismVectors);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBufPrism.limit()*4, vertBufPrism, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		FloatBuffer texBufPrism = Buffers.newDirectFloatBuffer(prismTextCoords);
		gl.glBufferData(GL_ARRAY_BUFFER, texBufPrism.limit()*4, texBufPrism, GL_STATIC_DRAW);
		
		//axis
		float[] verticAxis = {0, 0, 0, 1, 0, 0,
							0, 0, 0, 0, 1, 0,
							0, 0, 0, 0, 0, 1};
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		FloatBuffer axisBuf = Buffers.newDirectFloatBuffer(verticAxis);
		gl.glBufferData(GL_ARRAY_BUFFER, axisBuf.limit()*4, axisBuf, GL_STATIC_DRAW);
		
		float[] colorAxis = {1, 0, 0, 1, 0, 0,
							0, 1, 0, 0, 1, 0,
							0, 0, 1, 0, 0, 1};
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		FloatBuffer axisColourBuf = Buffers.newDirectFloatBuffer(colorAxis);
		gl.glBufferData(GL_ARRAY_BUFFER, axisColourBuf.limit()*4, axisColourBuf, GL_STATIC_DRAW);
		
	}

	public static void main(String[] args) { new HW2_FINAL(); }
	public void dispose(GLAutoDrawable drawable) {}
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{	aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
	}
	
	
	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		Vector3f f=new Vector3f();
		camVec.sub(targetVec,f);
		f.normalize();
		Vector3f r=new Vector3f();
		f.negate(r);
		r.cross(0, 1, 0);
		Vector3f u=new Vector3f();
		f.cross(r,u);
		//System.out.println(e);
		
		if (e.getKeyChar()=='w'){//Move camera forward by Ddist
			camVec.sub(f.mul(Ddist));
			myCanvas.display();
		}		
		if (e.getKeyChar()=='s'){//Move camera backwards by Ddist
			camVec.add(f.mul(Ddist));
			//System.out.println(e.getKeyChar()+""+camVec);
			myCanvas.display();
		}		
		if (e.getKeyChar()=='d'){//Right
			camVec.add(r.mul(Ddist));
			myCanvas.display();
		}		
		if (e.getKeyChar()=='a'){//Left
			camVec.sub(r.mul(Ddist));
			myCanvas.display();
		}		
		if (e.getKeyCode()==38){//Up
			camVec.add(u.mul(Ddist));
			myCanvas.display();
		}		
		if (e.getKeyCode()==40){//Down
			camVec.sub(u.mul(Ddist));
			myCanvas.display();
		}		
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
	
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}
}