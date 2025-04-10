/*
 * Hunter Brown
 * Prof. Gordon
 * CSC 155
 */

package a3;

public class Platform {
    private float tilingAmount = 10f;

    private float[] vertices = new float[]{   
        //36 vertices, wide and long platform but not a plane
        -10f, 1f, 10f,    10f, 1f, 10f,     -10f, -1f, 10f, 
        -10f, -1f, 10f,   10f, -1f, 10f,    10f, 1f, 10f, //Front
        10f, -1f, 10f,    10f, 1f, 10f,     10f, 1f, -10f, 
        10f, -1f, 10f,    10f, -1f, -10f,   10f, 1f, -10f, //Right Side
        -10f, 1f, 10f,    -10f, 1f, -10f,   10f, 1f, 10f, 
        10f, 1f, 10f,     10f, 1f, -10f,    -10f, 1f, -10f, //Top
        -10f, -1f, -10f,  -10f, 1f, -10f,   -10f, 1f, 10f, 
        -10f, -1f, -10f,  -10f, -1f, 10f,   -10f, 1f, 10f, //Left Side
        -10f, -1f, -10f,  10f, -1f, -10f,   10f, -1f, 10f, 
        -10f, -1f, -10f,  -10f, -1f, 10f,   10f, -1f, 10f, //Bottom
        -10f, 1f, -10f,   10f, 1f, -10f,    10f, -1f, -10f, 
        -10f, 1f, -10f,   -10f, -1f, -10f,  10f, -1f, -10f //Back Side
    };

    //Only the top shows the texture
    private float[] texCoords = new float[]{
        0f, 0f,     0f, 0f,     0f, 0f, 
        0f, 0f,     0f, 0f,     0f, 0f, //Front
        0f, 0f,     0f, 0f,     0f, 0f,
        0f, 0f,     0f, 0f,     0f, 0f, //Right Side
        0f, tilingAmount,     tilingAmount, tilingAmount,     0f, 0f,
        0f, 0f,     tilingAmount, 0f,     tilingAmount, tilingAmount, //Top
        0f, 0f,     0f, 0f,     0f, 0f, 
        0f, 0f,     0f, 0f,     0f, 0f, //Left Side
        0f, 0f,     0f, 0f,     0f, 0f,
        0f, 0f,     0f, 0f,     0f, 0f, //Bottom
        0f, 0f,     0f, 0f,     0f, 0f, 
        0f, 0f,     0f, 0f,     0f, 0f //Back Side

    };

    private float[] normals = new float[]{
        0f, 0f, 1f,     0f, 0f, 1f,     0f, 0f, 1f, 
        0f, 0f, 1f,     0f, 0f, 1f,     0f, 0f, 1f, //Front
        1f, 0f, 0f,     1f, 0f, 0f,     1f, 0f, 0f, 
        1f, 0f, 0f,     1f, 0f, 0f,     1f, 0f, 0f, //Right Side
        0f, 1f, 0f,     0f, 1f, 0f,     0f, 1f, 0f, 
        0f, 1f, 0f,     0f, 1f, 0f,     0f, 1f, 0f, //Top
        -1f, 0f, 0f,    -1f, 0f, 0f,    -1f, 0f, 0f, 
        -1f, 0f, 0f,    -1f, 0f, 0f,    -1f, 0f, 0f, //Left Side
        0f, -1f, 0f,    0f, -1f, 0f,    0f, -1f, 0f, 
        0f, -1f, 0f,    0f, -1f, 0f,    0f, -1f, 0f, //Bottom
        0f, 0f, -1f,     0f, 0f, -1f,     0f, 0f, -1f, 
        0f, 0f, -1f,     0f, 0f, -1f,     0f, 0f, -1f //Back Side
    };

    public Platform(){
        
    }

    public float[] getVertices(){
        return vertices;
    }

    public float[] getTexCoords(){
        return texCoords;
    }

    public float[] getNormals(){
        return normals;
    }
}