import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.nio.Buffer;
import java.text.DecimalFormat;
import java.util.Scanner;

public class Model
{
    int iLimit = 8;
    int jLimit = 7;
    int RLimit = 5;
    int i;
    int j;
    double X = 1.0;
    int XMax = 3000;
    int step = 1;
    double s;

    double[] H = new double[iLimit];
    double[] w = new double[iLimit];
    double[] c = new double[iLimit];
    
    double[][] x = new double[iLimit][jLimit];
    double[][] b = new double[iLimit][jLimit];
    double[][] f = new double[iLimit][jLimit];
    double[][] k = new double[iLimit][RLimit-1];
    double[][] K = new double[jLimit][RLimit-1];

    double[][][] R = new double[iLimit][RLimit][2];
    
    FileWriter writer = null;
    Scanner scanner = new Scanner(System.in);
    DecimalFormat format = new DecimalFormat("0.0000");

    public Model()
    {
        try 
        {
            writer = new FileWriter(new File("output.txt"));
            BufferedReader reader = new BufferedReader(new FileReader(new File("CalcWeight.txt")));
            String str;
            i = 0;
            while((str = reader.readLine()) != null)
            {
                if(!str.isEmpty())
                {
                    String [] nums = str.split(" ");
                    for (j = 0; j < jLimit; ++j)
                        x[i][j] = Double.valueOf(nums[j]);
                    ++i;
                }
            }
            reader.close();

            reader = new BufferedReader(new FileReader(new File("Calck.txt")));
            i=0;
            while((str = reader.readLine()) != null)
            {
                if(!str.isEmpty())
                {
                    String[] nums = str.split(" ");
                    for (j = 0; j < RLimit; ++j) 
                    {
                        R[i][j][0] = Double.valueOf(nums[2*j]);
                        R[i][j][1] = Double.valueOf(nums[2*j+1]);
                    }
                    ++i;
                }
            }
            
            reader.close();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    private void printInputValue()
    {
        for(j=0;j<RLimit;++j)
        {
            System.out.println("R["+(j+1)+"]");
            for(i=0;i<iLimit;++i)
            {
                System.out.println("c"+(i+1)+"("+R[i][j][0]+","+R[i][j][1]+")");
            }
            System.out.println();
        }

        System.out.println();
        for(i=0;i<iLimit;++i)
            System.out.println("c["+(i+1)+"] "+c[i]);
        System.out.println();
    }

    private void addResultHeader()
    {
        try
        {
            writer.write("X\tK1\tK2\tK3\tK4\n");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
    }

    private void calcAllAndOutput()
    {
        addResultHeader();
        for(int xx = 0;xx<=XMax;xx += step)
        {
            X = xx;
            calc();
            outputToFile();
            X = xx+0.5;
            calc();
            outputToFile();
        }
    }

    private void setStep(int step)
    {
        this.step = step;
    }

    private void setMax(int max)
    {
        XMax = max;
    }

    private void calc()
    {
        calcc();
        calcw();
        calck();
        calcK();
    }

    private void calcc()
    {
        c[0] = X;
        c[1] = 0.5;
        c[2] = 7.45 * X;
        c[3] = 0.2;
        c[4] = 31.56 * X;
        c[5] = 3540-40.051*X;
        c[6] = 7.5;
        c[7] = 6;
    }

    private void calcK()
    {
        for(i=0;i<RLimit-1;++i)
        {
            s = 0.0;
            for (int t = 0; t < iLimit; ++t) 
                s += k[t][i] * w[t];
            K[0][i] = s;
        }
    }

    private void calck()
    {
        for (j = 0; j < RLimit-1; ++j) 
        {
            for (i = 0; i < iLimit; ++i) 
            {
                k[i][j] = (
                    (c[i] < R[i][j][0] || c[i] > R[i][j][1]) ? 
                    p(i, j) / (p(i, RLimit-1) - p(i, j)) :
                    -p(i, j) / Math.abs(c[i]) 
                );
            }
        }
    }

    private double p(int i,int j)
    {
        return Math.abs(c[i] - 0.5*(R[i][j][0]+R[i][j][1]))-0.5*(R[i][j][1]-R[i][j][0]);
    }

    private void calcw()
    {
        for(i=0;i<iLimit;++i)
        {
            double min = x[i][0];
            double max = x[i][0];
            for(j=0;j<jLimit;++j)
            {
                if(min > x[i][j])
                    min = x[i][j];
                if(max < x[i][j])
                    max = x[i][j];
            }
            s = 0.0;
            for(j=0;j<jLimit;++j)
            {
                b[i][j] = (x[i][j] - min) / (max - min);
                s += 1+b[i][j];
            }
            for(j=0;j<jLimit;++j)
                f[i][j] = (1+b[i][j]) / s;
        }

        double ss = 0.0;
        for(i=0;i<iLimit;++i)
        {
            s = 0.0;
            for(j=0;j<jLimit;++j)
                s += f[i][j]*Math.log(f[i][j]);
            H[i] = -s/Math.log(jLimit);
            ss += H[i];
        }

        for(i=0;i<iLimit;++i)
            w[i] = (1 - H[i]) / (iLimit - ss);
    }

    private void outputToConsole()
    {
        System.out.println("---------------w--------------");
        double t = 0.0;
        for(i=0;i<iLimit;++i)
            t += w[i];
        System.out.println("w sum is " + t);
        System.out.println();

        for (i = 0; i < iLimit; ++i)
        System.out.println("w[" + (i + 1) + "]=" + format.format(w[i]) + "\n");
        System.out.println();

        System.out.println("---------------k--------------");
        for(i=0;i<iLimit;++i)
        {
            System.out.print("kj(v"+(i+1)+") ");
            for(j=0;j<RLimit-1;++j)
            System.out.print(format.format(k[i][j])+" ");
            System.out.println("\n");
        }

        System.out.println("---------------K--------------");
        for(j=0;j<RLimit-1;++j)
            System.out.print(format.format(K[0][j])+" ");
        System.out.println();
    }

    private void outputToFile()
    {
        try 
        {           
            StringBuilder result = new StringBuilder();
            result.append(X+"\t");
            for(i=0;i<RLimit-1;++i)
            {
                if(Double.isInfinite(K[0][i]))
                    result.append("Infinity\t");
                else
                    result.append(format.format(K[0][i])+"\t");
            }
            result.append("\n");
            writer.write(result.toString());
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    private void close()
    {
        try 
        {
            writer.close();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) 
    {
        Model model = new Model();
        model.setMax(500);
        model.setStep(1);
        model.calcAllAndOutput();
        model.close();
    }
}