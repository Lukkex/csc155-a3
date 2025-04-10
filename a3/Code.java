/*
 * Hunter Brown
 * Prof. Gordon
 * CSC 155
 * 13 Mar. 2025
 */

package a3;

import javax.swing.*;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL4.*;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.Math;
import java.lang.Math.*;
import java.nio.FloatBuffer;
import java.io.*;
import java.util.*;

import org.joml.*;

public class Code extends JFrame implements GLEventListener, KeyListener{	
	private GLCanvas myCanvas;
	private int renderingProgram;

	//Models & Objects
	private final int numOfModels = 6;
	private final int numOfObjects = 10;
	private int vao[] = new int[1];
	private int vbo[] = new int[numOfObjects*3]; //3 VBOs per model
	private ImportedModel models[] = new ImportedModel[numOfModels];
	private int textures[] = new int[numOfObjects];
	private Platform plat = new Platform();
	private boolean areAxesVisible = true;

	//Used for position, texture coordinates, and normal vector values and plugging them into buffers
	private ArrayList<float[]> allpvalues = new ArrayList<float[]>();
	private ArrayList<float[]> alltvalues = new ArrayList<float[]>();
	private ArrayList<float[]> allnvalues = new ArrayList<float[]>();
	private float[] pvalues, tvalues, nvalues;
	private Vector3f[] vertices;
	private Vector2f[] texCoords;
	private Vector3f[] normals;
	private FloatBuffer vertBuf, texBuf, norBuf;
	private int numObjVertices = 0;

	//World Vector3f components
	Vector3f worldRightVector = new Vector3f(1f, 0f, 0f);
	Vector3f worldUpVector = new Vector3f(0f, 1f, 0f);
	Vector3f worldForwardVector = new Vector3f(0f, 0f, -1f);
	Vector3f worldOrigin = new Vector3f(0f, 0f, 0f);

	//Camera components
	private Camera cam = new Camera(this);
	private final float DEFAULT_CAM_X = 0.0f;
	private final float DEFAULT_CAM_Y = 0.0f;
	private final float DEFAULT_CAM_Z = 4.5f;

	//Allocate variables for display() function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4f pMat = new Matrix4f();  // perspective matrix
	private Matrix4f vMat = new Matrix4f();  // view matrix
	private Matrix4f[] modelMatrices = new Matrix4f[numOfObjects]; //Stores all the model matrices for all models
	private Matrix4f mMat, temp = new Matrix4f();  // model matrix for temporary use per model
	private Matrix4f mvMat = new Matrix4f(); // model-view matrix
	private int mvLoc, pLoc;
	private float aspect;
	private float deltaTime = 0.0f;
	private float pitchAmount = 0.02f;
	private float panAmount = 0.02f;
	private float movementSpeed = 0.02f;
	private float rotationSpeed = 0.02f;
	private double floatingState = 0.0;
	private long lastTime = 0L;
	private long curTime = 0L;

	private float axisLineLength = 50f; //Made to be large enough so you can see it outside the inner chamber
	
	public Code()
	{	setTitle("CSC 155 - Lab #2");
		setSize(900, 900);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		myCanvas.addKeyListener(this);
		this.add(myCanvas);
		this.setVisible(true);
		Animator animator = new Animator(myCanvas);
		animator.start();
	}

	public void init(GLAutoDrawable drawable){	
		GL4 gl = (GL4) GLContext.getCurrentGL();

		//Sets all mMats for all models to a default identity matrix
		for (int i = 0; i < numOfObjects; i++){
			modelMatrices[i] = new Matrix4f().identity();
		}

		//Objects with external models
		models[0] = new ImportedModel("models/GHOUL.obj");
		textures[0] = Utils.loadTexture("textures/GHOUL.jpg");

		models[1] = new ImportedModel("models/Sanctum.obj");
		textures[1] = Utils.loadTexture("textures/Sanctum.jpg");

		models[2] = new ImportedModel("models/Star.obj");
		textures[2] = Utils.loadTexture("textures/Star.jpg");

		models[3] = new ImportedModel("models/Chamber.obj");
		textures[3] = Utils.loadTexture("textures/Chamber.jpg");

		models[4] = new ImportedModel("models/FakeSkybox.obj");
		textures[4] = Utils.loadTexture("textures/FakeSkybox.jpg");

		models[5] = new ImportedModel("models/Cone.obj");
		textures[5] = Utils.loadTexture("textures/brick1.jpg"); //From the book
		modelMatrices[5].translate(new Vector3f(0f, -0.25f, 0f)); // Starts off lower in the world

		//Objects without any external models
		textures[6] = Utils.loadTexture("textures/ground.jpg"); //From online site
		modelMatrices[numOfModels].translate(new Vector3f(0f, -3f, 0f)); // Starts off lower in the world
		
		textures[7] = Utils.loadTexture("textures/X.png");

		textures[8] = Utils.loadTexture("textures/Y.png");

		textures[9] = Utils.loadTexture("textures/Z.png");

		renderingProgram = Utils.createShaderProgram("a3/vertShader.glsl", "a3/fragShader.glsl");

		float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(100.0f), aspect, 0.1f, 1000.0f);

		setupVertices();

		//Set default camera location
		cam.setLocation(new Vector3f(DEFAULT_CAM_X, DEFAULT_CAM_Y, DEFAULT_CAM_Z));
	}

	public void display(GLAutoDrawable drawable){	
		GL4 gl = (GL4) GLContext.getCurrentGL();

		//Clears color & depth buffers to default and uses prev. created renderingProgram object
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glUseProgram(renderingProgram);

		//Calculates delta time, used in movement functions to standardize speed across devices
		calculateDeltaTime();

		//Update all objects in the scene and draw them to the screen
		updateObjects(gl);
	}

	public void updateObjects(GL4 gl){
		//Gets int pointer to mv_matrix uniform variable
		int mvLoc = gl.glGetUniformLocation(renderingProgram, "mv_matrix");

		//Gets int pointer to p_matrix uniform variable
		int pLoc = gl.glGetUniformLocation(renderingProgram, "p_matrix");
		
		//Get view matrix for the current frame
		vMat.set(cam.buildViewMatrix());

		//Iterates through every single object (with a model) in the scene and updates their local positions into the world
		//using their respective model matrix and based on the view, perspective matrices as well
		for (int i = 0; i < numOfModels; i++){
			mMat = modelMatrices[i];

			//If Ghoul, then hover up and down
			if (i == 0){ 
				temp.translation(0, (float) java.lang.Math.cos((double) ((deltaTime * floatingState)))/5000 * 10, 0);
				mMat.mul(temp); 
				floatingState += movementSpeed/10;
				floatingState %= (2 * java.lang.Math.PI);
			}
			//If it's the star, rotate it about the Y axis
			else if (i == 2)
				mMat.rotateY((float)Math.toRadians(-rotationSpeed * 5 * deltaTime));
			//If it is the inner chamber object, rotate it about the Y axis
			else if (i == 3) 
				mMat.rotateY((float)Math.toRadians(rotationSpeed * deltaTime));
			
			modelMatrices[i] = mMat;

			mvMat.identity();
			mvMat.mul(vMat);
			mvMat.mul(mMat);

			gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
			gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));

			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[i*3]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);

			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[(i*3)+1]);
			gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(1);

			gl.glActiveTexture(GL_TEXTURE0);
			gl.glBindTexture(GL_TEXTURE_2D, textures[i]);

			gl.glEnable(GL_DEPTH_TEST);
			gl.glDepthFunc(GL_LEQUAL);
			
			gl.glDrawArrays(GL_TRIANGLES, 0, models[i].getNumVertices());
		}

		//Draws the remaining objects that don't have external models

		//Platform object
		mMat = modelMatrices[numOfModels];
		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);

		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[numOfModels*3]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[(numOfModels*3)+1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, textures[numOfModels]);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);

		if (areAxesVisible){
			//Axis lines
			for (int i = 1; i <= 3; i++){
				mMat = modelMatrices[(numOfModels + i)];
				mvMat.identity();
				mvMat.mul(vMat);
				mvMat.mul(mMat);

				gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
				gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));

				gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[(numOfModels + i)*3]);
				gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
				gl.glEnableVertexAttribArray(0);

				gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[((numOfModels + i)*3)+1]);
				gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
				gl.glEnableVertexAttribArray(1);

				gl.glActiveTexture(GL_TEXTURE0);
				gl.glBindTexture(GL_TEXTURE_2D, textures[(numOfModels + i)]);

				gl.glEnable(GL_DEPTH_TEST);
				gl.glDepthFunc(GL_LEQUAL);
				
				gl.glDrawArrays(GL_LINES, 0, 2);
			}
		}
	}

	private void setupVertices(){	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);

		for (int k = 0; k < numOfModels; k++){
			numObjVertices = models[k].getNumVertices();
			vertices = models[k].getVertices();
			texCoords = models[k].getTexCoords();
			normals = models[k].getNormals();

			pvalues = new float[numObjVertices*3];
			tvalues = new float[numObjVertices*2];
			nvalues = new float[numObjVertices*3];
			
			for (int i = 0; i < numObjVertices; i++){	
				pvalues[i*3]   = (float) (vertices[i]).x();
				pvalues[i*3+1] = (float) (vertices[i]).y();
				pvalues[i*3+2] = (float) (vertices[i]).z();
				tvalues[i*2]   = (float) (texCoords[i]).x();
				tvalues[i*2+1] = (float) (texCoords[i]).y();
				nvalues[i*3]   = (float) (normals[i]).x();
				nvalues[i*3+1] = (float) (normals[i]).y();
				nvalues[i*3+2] = (float) (normals[i]).z();
			}
			allpvalues.add(k, pvalues);
			alltvalues.add(k, tvalues);
			allnvalues.add(k, nvalues);

			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3*k]);
			vertBuf = Buffers.newDirectFloatBuffer(pvalues);
			gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[(3*k)+1]);
			texBuf = Buffers.newDirectFloatBuffer(tvalues);
			gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);
			
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[(3*k)+2]);
			norBuf = Buffers.newDirectFloatBuffer(nvalues);
			gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4,norBuf, GL_STATIC_DRAW);
		}

		//Setup any remaining objects that don't have an external OBJ file

		//Platform object
		numObjVertices = 36;
		pvalues = plat.getVertices();
		tvalues = plat.getTexCoords();
		nvalues = plat.getNormals();

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3*numOfModels]);
		vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[(3*numOfModels)+1]);
		texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[(3*numOfModels)+2]);
		norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4,norBuf, GL_STATIC_DRAW);

		//X axis line
		numObjVertices = 2;
		pvalues = new float[]{axisLineLength, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}; 

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3*(numOfModels + 1)]);
		vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[(3*(numOfModels + 1))+1]);
		texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[(3*(numOfModels + 1))+2]);
		norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4,norBuf, GL_STATIC_DRAW);

		//Y axis line
		numObjVertices = 2;
		pvalues = new float[]{0.0f, axisLineLength, 0.0f, 0.0f, 0.0f, 0.0f}; 

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3*(numOfModels + 2)]);
		vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[(3*(numOfModels + 2))+1]);
		texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[(3*(numOfModels + 2))+2]);
		norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4,norBuf, GL_STATIC_DRAW);

		//Z axis line
		numObjVertices = 2;
		pvalues = new float[]{0.0f, 0.0f, axisLineLength, 0.0f, 0.0f, 0.0f}; 

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3*(numOfModels + 3)]);
		vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[(3*(numOfModels + 3))+1]);
		texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[(3*(numOfModels + 3))+2]);
		norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4,norBuf, GL_STATIC_DRAW);
	}

	//Calculates the amount of time between the current frame and previous frame, sets deltaTime to the difference
	public float calculateDeltaTime(){
		if (lastTime == 0L)
			lastTime = System.currentTimeMillis();
		curTime = System.currentTimeMillis();
		deltaTime = (float)(curTime - lastTime);
		lastTime = curTime;
		return deltaTime;
	}

	//Getters and setters
	public Vector3f getWorldRightVector(){
		return worldRightVector;
	}

	public Vector3f getWorldUpVector(){
		return worldUpVector;
	}

	public Vector3f getWorldForwardVector(){
		return worldForwardVector;
	}

	public Vector3f getWorldOrigin(){
		return worldOrigin;
	}

	//KeyListener implemented functions
	@Override
	public void keyPressed(KeyEvent e){	
		switch (e.getKeyCode()){
			case KeyEvent.VK_W:
				//Move camera forward
				cam.moveAlongN(movementSpeed);
				break;
			case KeyEvent.VK_S:
				//Move camera backward
				cam.moveAlongN(-movementSpeed);
				break;
			case KeyEvent.VK_A:
				//Move camera left
				cam.moveAlongU(-movementSpeed);
				break;
			case KeyEvent.VK_D:
				//Move camera right
				cam.moveAlongU(movementSpeed);
				break;
			case KeyEvent.VK_Q:
				//Move camera up
				cam.moveAlongV(movementSpeed);
				break;
			case KeyEvent.VK_E:
				//Move camera down
				cam.moveAlongV(-movementSpeed);
				break;
			case KeyEvent.VK_UP:
				//Rotate camera up (pitch up)
				cam.pitch(pitchAmount);
				break;
			case KeyEvent.VK_DOWN:
				//Rotate camera down (pitch down)
				cam.pitch(-pitchAmount);
				break;
			case KeyEvent.VK_LEFT:
				//Rotate camera left (yaw left)
				cam.yaw(panAmount);
				break;
			case KeyEvent.VK_RIGHT:
				//Rotate camera right (yaw right)
				cam.yaw(-panAmount);
				break;
			case KeyEvent.VK_SPACE:
				//Toggle world axis
				areAxesVisible = !areAxesVisible;
				break;
		}
	}

	//Required implementations, but not used
	@Override
	public void keyReleased(KeyEvent e){}

	@Override
	public void keyTyped(KeyEvent e){}

	public static void main(String[] args) { new Code(); }
	public void dispose(GLAutoDrawable drawable) {}
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{	float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
	}
}