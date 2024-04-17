package zju.defect;

//import org.apache.commons.collections.BagUtils;
import zju.plugin.DataCenter;

import java.io.*;

import java.io.IOException;
import java.util.Properties;


public class Build {



    public String buildModel(String projectName, String projectPath){
        String result = null;
        try{

            String dataPath = projectPath.substring(0, projectPath.length()-projectName.length());
            String pythonenv = DataCenter.pythonEnv;
            String pythonpath = DataCenter.pythonProject+"/defect_features/build_model.py ";
            String pythonProjectPath = DataCenter.pythonProject;

            String pythonArgs = pythonenv+ " " + pythonpath+ " " + projectName + " "+pythonProjectPath +" "+ dataPath;
            System.out.println(pythonArgs);
            Process proc = Runtime.getRuntime().exec(pythonArgs);
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            System.out.println(proc);
            String line = null;
            while ((line = in.readLine()) != null) {
                result = line;
            }
            System.out.println(result);
        }catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String agrs[]){
        Build build = new Build();
        String test = "E:/JITO/JITO-2.0/JITO-1.0/JIT-Identification/defect_features";
        System.out.println(test.substring(0,test.length()-15));
        DataCenter.pythonEnv = "D:/Anaconda/python.exe";
        DataCenter.pythonProject = "E:/JITO/JITO-2.0/JITO-1.0/JIT-Identification";
        build.buildModel("JITOTest","E:/代码/JITOTest");
    }

}


