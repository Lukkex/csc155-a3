/*
 * Hunter Brown
 * Prof. Gordon
 * CSC 155
 */

package a3;

import org.joml.*;

public class Camera {
    private float x, y, z = 0.0f;
    private Vector3f location; //Local location
    private Vector3f temp = new Vector3f(0f, 0f, 0f);
    private Vector3f U, V, N, worldRight, worldUp, worldForward;
    private Matrix4f viewMatrix = new Matrix4f();
    private Matrix4f viewMatrixT = new Matrix4f();
    private Matrix4f viewMatrixR = new Matrix4f();
    private Code c;

    public Camera(Code c){
        this.c = c;
        location = new Vector3f(0f, 0f, 0f);
        U = new Vector3f(1f, 0f, 0f);
        V = new Vector3f(0f, 1f, 0f);
        N = new Vector3f(0f, 0f, -1f);
        worldRight = c.getWorldRightVector();
        worldUp = c.getWorldUpVector();
        worldForward = c.getWorldForwardVector();
    }

    //Creates view matrix using U, V, N vectors and camera position
    public Matrix4f buildViewMatrix(){
        viewMatrixT.set(1f, 0f, 0f, 0f,
                       0f, 1f, 0f, 0f,
                       0f, 0f, 1f, 0f,
                       -location.x(), -location.y(), -location.z(), 1f);
        viewMatrixR.set(U.x(), V.x(), -N.x(), 0f,
                       U.y(), V.y(), -N.y(), 0f,
                       U.z(), V.z(), -N.z(), 0f,
                       0f, 0f, 0f, 1f);
        viewMatrix.identity().mul(viewMatrixR).mul(viewMatrixT);

        return viewMatrix;
    }

    //Returns new Vector3f of N so it doesn't change N when mul is done to the Vector3f
    public Vector3f getN(){
        return new Vector3f(N);
    }

    //Returns new Vector3f of U so it doesn't change N when mul is done to the Vector3f
    public Vector3f getU(){
        return new Vector3f(U);
    }

            //Returns new Vector3f of V so it doesn't change N when mul is done to the Vector3f
    public Vector3f getV(){
        return new Vector3f(V);
    }

    //Forward and backward movement
    public void moveAlongN (float amount){
        location = location.add(this.getN().mul(amount));
    }

    //Left and right movement
    public void moveAlongU (float amount){
        location = location.add(this.getU().mul(amount));
    }

    //Up and down movement
    public void moveAlongV (float amount){
        location = location.add(this.getV().mul(amount));
    }

    public void yaw (float amount){
        //Moves camera to world origin and then conducts rotations, then moves it back after
        temp = location;
        location = c.getWorldOrigin();

        //Global yaw uses worldUpVector so to not induce roll
        N.rotateAxis(amount, worldUp.x, worldUp.y, worldUp.z);
        N.cross(worldUp, U);
        U.cross(N, V);

        location = temp;
    }

    public void pitch (float amount){
        //Moves camera to world origin and then conducts rotations, then moves it back after
        temp = location;
        location = c.getWorldOrigin();

        V.rotateAxis(amount, U.x(), U.y(), U.z());
        N.rotateAxis(amount, U.x(), U.y(), U.z());

        location = temp;
    }

    //Getters and setters
    public Vector3f getLocalLocation(){
        return this.location;
    }

    public void setLocation(Vector3f newLocation){
        this.location = newLocation;
    }

    public void setX(float x){
        this.location.set(x, location.y(), location.z());
    }

    public void setY(float y){
        this.location.set(location.x(), y, location.z());
    }

    public void setZ(float z){
        this.location.set(location.x(), location.y(), z);
    }
}