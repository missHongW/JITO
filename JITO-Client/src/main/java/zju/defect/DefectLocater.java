package zju.defect;



import org.apache.commons.text.StringEscapeUtils;
//import slp.core.example.BasicJavaRunner;
import zju.defect.util.CSV_handler;
import zju.defect.util.FileUtil;
import zju.defect.util.GitUtil;


import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


import com.google.gson.Gson;
import zju.plugin.DataCenter;


public class DefectLocater {
    public static int ngramLength = 6;
    private static GitUtil gitUtil = new GitUtil();

//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        this.doPost(request,response);
//    }

//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
//        //接收客户端数据
//        request.setCharacterEncoding("utf-8");
//        String projectName = request.getParameter("projectName");
//
//        Properties props = new Properties();
//        props.load(this.getClass().getResourceAsStream("/config.properties"));
//        String dataPath = props.getProperty("data-path");
//        String repoPath = dataPath + "/" + projectName;
//        String commitHash = gitUtil.getLastCommit(repoPath);
//
//        //返回数据
//        if(analyzeProb(commitHash)){
//            List<Sentence> sentences = modelRuning(repoPath, commitHash, ngramLength, projectName);
//            Gson gson = new Gson();
//            String json = gson.toJson(sentences);
//            response.setCharacterEncoding("UTF-8");
//            PrintWriter out = response.getWriter();
//            out.write(json);
//            out.close();
//        }
//        else{
//            PrintWriter out = response.getWriter();
//            String sendString = "None bug";
//            out.write(sendString);
//            out.close();
//        }
//    }
//    public void analyzeChange(String commitHash, String projectPath, String projectName) throws IOException{
//        if(analyzeProb(commitHash)){
//            List<Sentence> sentences = modelRuning(projectPath, commitHash, ngramLength, projectName);
//
//        }
//    }


    public Boolean analyzeProb(String commitHash){
        String result = null;
        try{

            String pythonenv = DataCenter.pythonEnv;
            String pythonpath = DataCenter.pythonProject+"/defect_features/run_model.py ";
            String pythonProjectPath = DataCenter.pythonProject;

            String pythonArgs = pythonenv + pythonpath + commitHash+" "+pythonProjectPath;
            System.out.println("pythonArgs:"+pythonArgs);
            Process proc = Runtime.getRuntime().exec(pythonArgs);
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            System.out.println(in.readLine());
            while ((line = in.readLine()) != null) {
                result = line;
            }
            in.close();
            proc.waitFor();

        }catch (IOException e) {
            e.printStackTrace();
        }catch (InterruptedException e) {
            e.printStackTrace();
        }

        return (result.equals("1"));
//        return result;

    }

    public List<Sentence> modelRuning(String repoPath, String commitHash, int ngramLength, String projectName) throws IOException {
//        Properties props = new Properties();
//        props.load(this.getClass().getResourceAsStream("/config.properties"));
//        String inputContent = props.getProperty("inputContent-path");

//        String trainSetPathJava = props.getProperty("trainSet-path")+"/"+projectName+"Train.java";

        String inputContent = DataCenter.pythonProject+"/defect_features/utils/data_tmp";
        String trainSetPathJava = DataCenter.pythonProject + "/train/"+projectName+"Train.java";


        List<String> bugFiles = gitUtil.getBugFilePath(repoPath, commitHash);
        getCleanLines(inputContent,trainSetPathJava, projectName);
        JavaRunner jr = new JavaRunner();
        jr.Initilation(repoPath, trainSetPathJava, bugFiles, ngramLength);
        List<Sentence> sentences = jr.LocateModeling();
        return sentences;
    }

    public void getCleanLines(String inputContent, String trainSetPathJava, String projetName)throws IOException {
        CSV_handler myCSV = new CSV_handler();
        File file = new File(trainSetPathJava);
        if (!file.exists()) {
            file.getParentFile().mkdir();
        }
        List<String> trainLineContent = new ArrayList<String>();
        String inputContentCsv = inputContent + "/" + projetName + "_a.csv";
        List<String[]> content = myCSV.getContentFromFile(new File(inputContentCsv));
        for (int i = 0; i < content.size(); i++) {
            String[] thisLine = content.get(i);
            String lineContent = thisLine[13];
            if ("".equals(lineContent) || "[]".equals(lineContent)) {
                continue;
            }
            String[] oneLine = lineContent.substring(2,lineContent.length()-2).split("\", \"");
            for(int j = 0; j < oneLine.length; j++){
                String codeLine = StringEscapeUtils.unescapeJava(oneLine[j]);
                if(codeLine.length() >= 10){

                    String preprocessedLineContent = FileUtil.PreprocessCode(codeLine.trim());
                    trainLineContent.add(preprocessedLineContent);
                }
            }
        }
        FileUtil.writeLinesToFile(trainLineContent, trainSetPathJava);
    }



    public static void main(String[] args) throws IOException {
        DefectLocater dfl = new DefectLocater();
//        List<Sentence> sentences = dfl.modelRuning("/Users/lifeasarain/IdeaProjects/druid", "9b12d5d1ecfaa83c13d6064c5da7d78e0c31600c", ngramLength, "druid");
//        for(Sentence sentence : sentences){
//            System.out.println(sentence.getLineNumber());
//        }
        DataCenter.pythonEnv = "C:/Users/宏大树的电脑/AppData/Local/Programs/Python/Python39/python.exe ";
        DataCenter.pythonProject = "E:/JITO/JITO-1.0/JITO-1.0/JIT-Identification";
        dfl.analyzeProb("17a53bd78a740c9c054843bfc130e93f323cfa20");
        List<Sentence> sentences = dfl.modelRuning("E:/代码/JITOTest", "17a53bd78a740c9c054843bfc130e93f323cfa20", 6, "JITOTest");
        for (Sentence sentence : sentences) {
            System.out.println(sentence.getEntropy());
        }
    }
}
