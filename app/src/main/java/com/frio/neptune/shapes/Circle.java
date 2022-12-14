package com.frio.neptune.shapes;

import android.opengl.GLES32;
import android.opengl.Matrix;
import com.frio.neptune.GLRenderer;
import com.frio.neptune.utils.Vector3;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Circle {

  private FloatBuffer vertexBuffer;

  private final String vertexShaderCode =
      "uniform mat4 uMVPMatrix;"
          + "attribute vec4 vPosition;"
          + "uniform mat4 model;"
          + "void main() {"
          + "  gl_Position = uMVPMatrix * vPosition * model;"
          + "}";

  private final String fragmentShaderCode =
      "precision mediump float;"
          + "uniform vec4 vColor;"
          + "void main() {"
          + "  gl_FragColor = vColor;"
          + "}";

  // Use to access and set the view transformation
  private int vPMatrixHandle;

  static float circleCoords[] = new float[364 * 3];

  private final int mProgram;

  private int positionHandle;
  private int colorHandle;

  private Vector3 position = new Vector3(0, 0, 0);
  private Vector3 scale = new Vector3(1, 1, 1);
  private Vector3 rotation = new Vector3(0, 0, 0);

  public Circle() {
    circleCoords[0] = 0;
    circleCoords[1] = 0;
    circleCoords[2] = 0;

    for (int i = 1; i < 364; i++) {
      circleCoords[(i * 3) + 0] =
          (float) (0.25 * Math.cos((3.14 / 180) * (float) i) + circleCoords[0]);
      circleCoords[(i * 3) + 1] =
          (float) (0.25 * Math.sin((3.14 / 180) * (float) i) + circleCoords[1]);
      circleCoords[(i * 3) + 2] = 0;
    }

    ByteBuffer bb = ByteBuffer.allocateDirect(circleCoords.length * 4);

    bb.order(ByteOrder.nativeOrder());
    vertexBuffer = bb.asFloatBuffer();
    vertexBuffer.put(circleCoords);
    vertexBuffer.position(0);

    int vertexShader = GLRenderer.loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode);
    int fragmentShader = GLRenderer.loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode);

    mProgram = GLES32.glCreateProgram();

    GLES32.glAttachShader(mProgram, vertexShader);

    GLES32.glAttachShader(mProgram, fragmentShader);

    GLES32.glLinkProgram(mProgram);
  }

  public void draw(float[] mvpMatrix, float[] color) {
    float positionM[] = new float[16];
    Matrix.setIdentityM(positionM, 0);
    Matrix.translateM(positionM, 0, position.getX(), position.getY(), position.getZ());

    float rotationM[] = new float[16];
    Matrix.setIdentityM(rotationM, 0);
    Matrix.setRotateM(rotationM, 0, rotation.getX(), 1, 0, 0);
    Matrix.setRotateM(rotationM, 0, rotation.getY(), 0, 1, 0);
    Matrix.setRotateM(rotationM, 0, rotation.getZ(), 0, 0, 1);

    float scaleM[] = new float[16];
    Matrix.setIdentityM(scaleM, 0);
    Matrix.scaleM(scaleM, 0, scale.getX(), scale.getY(), scale.getZ());

    float model[] = new float[16];
    Matrix.multiplyMM(model, 0, scaleM, 0, rotationM, 0);
    Matrix.multiplyMM(model, 0, model, 0, positionM, 0);

    GLES32.glUseProgram(mProgram);

    positionHandle = GLES32.glGetAttribLocation(mProgram, "vPosition");

    GLES32.glEnableVertexAttribArray(positionHandle);

    GLES32.glVertexAttribPointer(positionHandle, 3, GLES32.GL_FLOAT, false, 12, vertexBuffer);

    colorHandle = GLES32.glGetUniformLocation(mProgram, "vColor");

    GLES32.glUniform4fv(colorHandle, 1, color, 0);

    vPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix");

    GLES32.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0);

    int modelHandle = GLES32.glGetUniformLocation(mProgram, "model");
    GLES32.glUniformMatrix4fv(modelHandle, 1, false, model, 0);

    GLES32.glDrawArrays(GLES32.GL_TRIANGLE_FAN, 0, 364);

    GLES32.glDisableVertexAttribArray(positionHandle);
  }
}
