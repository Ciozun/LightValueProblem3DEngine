package Main;

import java.awt.Color;
import java.awt.Graphics;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

public class Main
{
	boolean bool1 = false;
	
	int WIDTH = 900;
	int HEIGHT = 900;
	
	float[][][] mesh = loadMesh("meshes//teapot.obj");/*
	{
		{{0,0,0},{0,1,0},{1,1,0}}, {{0,0,0},{1,1,0},{1,0,0}}, // front
		{{1,0,0},{1,1,0},{1,1,1}}, {{1,0,0},{1,1,1},{1,0,1}}, // right
		{{1,0,1},{1,1,1},{0,1,1}}, {{1,0,1},{0,1,1},{0,0,1}}, // back
		{{0,0,1},{0,1,1},{0,1,0}}, {{0,0,1},{0,1,0},{0,0,0}}, // left
		{{0,1,0},{0,1,1},{1,1,1}}, {{0,1,0},{1,1,1},{1,1,0}}, // up
		{{1,0,0},{1,0,1},{0,0,1}}, {{1,0,0},{0,0,1},{0,0,0}}, // down
	};*/
	
	float[][] rotMatX = {{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}};
	float[][] rotMatZ = {{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}};

	float[] vCamera = {0, 0, 0};
	
	float theta = 0f;
	
	float near = 0.1f;
	float far = 1000f;
	float fov = 90f;
	float as = WIDTH * 1f / HEIGHT;
	float fovRad = (float) (1f / Math.tan(fov * 0.5f / 180f * Math.PI));
	
	float[][] matProj =
	{
		{as * fovRad, 0, 					 0, 							0},
		{0, 		  fovRad, 			     0, 							0},
		{0, 		  0, 					 far / (far - near),			1},
		{0, 		  0,					 (- far * near) / (far - near), 0}
	};
	
	
	public Main() throws Exception
	{		
		SimpleGameEngine sge = new SimpleGameEngine(WIDTH, HEIGHT, "3D Engine")
		{
			public void update()
			{
				if(!escapeKey())
				{
					float elapsedTime = getElapsedTime() / 1000000000f;
					theta += elapsedTime * 1f;
					
					if(f3Key())
					{
						if(!bool1)
						{
							setFPSLock(!getFPSLock());
							bool1 = true;
						}
					}else bool1 = false;
					
					rotMatX[0][0] = 1f;
					rotMatX[1][1] = (float) Math.cos(theta * 0.5f);
					rotMatX[1][2] = (float) Math.sin(theta * 0.5f);
					rotMatX[2][1] = (float) - Math.sin(theta * 0.5f);
					rotMatX[2][2] = (float) Math.cos(theta * 0.5f);
					rotMatX[3][3] = 1f;
					
					rotMatZ[0][0] = (float) Math.cos(theta);
					rotMatZ[0][1] = (float) Math.sin(theta);
					rotMatZ[1][0] = (float) - Math.sin(theta);
					rotMatZ[1][1] = (float) Math.cos(theta);
					rotMatZ[2][2] = 1f;
					rotMatZ[3][3] = 1f;										
				}
				else
					stop();
			}
			
			public void render()
			{
				Graphics g = getGraphics();
				
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, WIDTH, HEIGHT);
				
				for(float[][] tri : mesh)
				{		
					float[][] triRotZ = new float[4][4];
					float[][] triRotZX = new float[4][4];
					
					mulMatVet(tri[0], triRotZ[0], rotMatZ);
					mulMatVet(tri[1], triRotZ[1], rotMatZ);
					mulMatVet(tri[2], triRotZ[2], rotMatZ);
					
					mulMatVet(triRotZ[0], triRotZX[0], rotMatX);
					mulMatVet(triRotZ[1], triRotZX[1], rotMatX);
					mulMatVet(triRotZ[2], triRotZX[2], rotMatX);
					
					float[][] triTranslated = triRotZX;
					
					triTranslated[0][2] += 7f;
					triTranslated[1][2] += 7f;
					triTranslated[2][2] += 7f;
					
					float[] normal = new float[3];
					float[] line1 = new float[3];
					float[] line2 = new float[3];
					
					line1[0] = triTranslated[1][0] - triTranslated[0][0];
					line1[1] = triTranslated[1][1] - triTranslated[0][1];
					line1[2] = triTranslated[1][2] - triTranslated[0][2];
					
					line2[0] = triTranslated[2][0] - triTranslated[0][0];
					line2[1] = triTranslated[2][1] - triTranslated[0][1];
					line2[2] = triTranslated[2][2] - triTranslated[0][2];
					
					normal[0] = line1[1] * line2[2] - line1[2] * line2[1];
					normal[1] = line1[2] * line2[0] - line1[0] * line2[2];
					normal[2] = line1[0] * line2[1] - line1[1] * line2[0];
					
					float l = (float) Math.sqrt(normal[0] * normal[0] + normal[1] * normal[1] + normal[2] * normal[2]);
					normal[0] /= l; normal[1] /= l; normal[2] /= l;
					
					if(normal[0] * (triTranslated[0][0] - vCamera[0]) +
					   normal[1] * (triTranslated[0][1] - vCamera[1]) +
					   normal[2] * (triTranslated[0][2] - vCamera[2]) < 0)
					{			
						float[] vLight = {0, 0, -1};
						float l1 = (float) Math.sqrt(vLight[0] * vLight[0] + vLight[1] * vLight[1] + vLight[2] * vLight[2]);
						vLight[0] /= l1; vLight[1] /= l1; vLight[2] /= l1;
						
						float dp = normal[0] * vLight[0] + normal[1] * vLight[1] + normal[2] * vLight[2];
						
						dp = dp < 0f ? 0 : dp;
						
						g.setColor(new Color((int)(dp * 255), (int)(dp * 255), (int)(dp * 255)));
						
						float[][] triProjected = new float[3][3];
						
						mulMatVet(triTranslated[0], triProjected[0], matProj);
						mulMatVet(triTranslated[1], triProjected[1], matProj);
						mulMatVet(triTranslated[2], triProjected[2], matProj);
						
						triProjected[0][0] += 1; triProjected[0][1] += 1f;
						triProjected[1][0] += 1; triProjected[1][1] += 1f;
						triProjected[2][0] += 1; triProjected[2][1] += 1f;
						
						triProjected[0][0] *= 0.5f * WIDTH;
						triProjected[0][1] *= 0.5f * HEIGHT;
						triProjected[1][0] *= 0.5f * WIDTH;
						triProjected[1][1] *= 0.5f * HEIGHT;
						triProjected[2][0] *= 0.5f * WIDTH;
						triProjected[2][1] *= 0.5f * HEIGHT;
						
						int[] xs = new int[]{(int)triProjected[0][0], (int)triProjected[1][0], (int)triProjected[2][0]};
						int[] ys = new int[]{(int)triProjected[0][1], (int)triProjected[1][1], (int)triProjected[2][1]};
					
						g.fillPolygon(xs, ys, 3);
						
						g.setColor(Color.WHITE);
						g.drawPolygon(xs, ys, 3);
					}
				}
				
				drawGraphics();
			}
		};
	}
	
	void mulMatVet(float[] i, float[] o, float[][] m)
	{
		o[0] = i[0] * m[0][0] + i[1] * m[1][0] + i[2] * m[2][0] + m[3][0];
		o[1] = i[0] * m[0][1] + i[1] * m[1][1] + i[2] * m[2][1] + m[3][1];
		o[2] = i[0] * m[0][2] + i[1] * m[1][2] + i[2] * m[2][2] + m[3][2];
		float w = i[0] * m[0][3] + i[1] * m[1][3] + i[2] * m[2][3] + m[3][3];

		if (w != 0.0f)
		{
			o[0] /= w; o[1] /= w; o[2] /= w;
		}
	}
	
	float[][][] loadMesh(String filePath) throws FileNotFoundException
	{		
		FileReader fr = new FileReader(filePath);
		Scanner sca = new Scanner(fr);
		
		ArrayList<float[]> vectors = new ArrayList<float[]>();
		ArrayList<float[][]> triangles = new ArrayList<float[][]>();
		
		int counter = 0;
		
		while(sca.hasNext())
		{	
			String line = sca.next();
			
			if(line.charAt(0) != '#')
			{					
				if(line.charAt(0) == 'v')
				{		
					float x = (float) Double.parseDouble(sca.next());
					float y = (float) Double.parseDouble(sca.next());
					float z = (float) Double.parseDouble(sca.next());
					vectors.add(new float[] {x, y, z});
				}
				else if(line.charAt(0) == 'f')
				{
					int vectorN0 = sca.nextInt() - 1;
					int vectorN1 = sca.nextInt() - 1;
					int vectorN2 = sca.nextInt() - 1;						

					triangles.add(new float[][] {vectors.get(vectorN0), vectors.get(vectorN1), vectors.get(vectorN2)});
					counter++;
				}				
			}
			else
				sca.nextLine();	
		}
		
		sca.close();
		
		if(counter == 0)
			throw new IllegalStateException("Illegal file!");			
					
		float[][][] mesh = new float[counter][3][3];
		
		for(int i = 0; i < counter; i++)
			mesh[i] = triangles.get(i);
		
		return mesh;
	}
}
