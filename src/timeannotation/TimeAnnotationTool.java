package timeannotation;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.sound.sampled.Line;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import org.apache.bcel.classfile.*;
import org.apache.bcel.util.*;

import sun.misc.Regexp;

public class TimeAnnotationTool extends JFrame {
	
	final String tableFileName = "src/resources/Bytecode WCET Table.txt";

	private JPanel contentPane;
	private JTextField txtInputFile;
	private JButton btnAdd;
	private JButton btnAnalyze;
	
	private File file;
	private BufferedReader tableBufferedReader;
	
	public File getFile(){
		return this.file;
	}
	public void setFile(File value){
		this.file = value;
	}
	
	public BufferedReader getTableBufferedReader(){
		return this.tableBufferedReader;
	}
	public void setTableBufferedReader(BufferedReader value){
		this.tableBufferedReader = value;
	}
//	
//	private String sFileName; 
//	
//	public String getFileName(){
//		return this.sFileName;
//	}
//	public void setFileName(String value){
//		this.sFileName = value;
//	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TimeAnnotationTool frame = new TimeAnnotationTool();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void openFileChoose(){
		JFileChooser fchFile = new JFileChooser();
		FileNameExtensionFilter extFilter = new FileNameExtensionFilter("java files", "java");
		fchFile.setAcceptAllFileFilterUsed(false);
		fchFile.addChoosableFileFilter(extFilter);
		if (fchFile.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
			file = fchFile.getSelectedFile();
			txtInputFile.setText(file.getAbsolutePath());					
		}
	}
	
	private boolean compileCode(){
		try {			
			FileWriter fw = new FileWriter("log");
			String sSourceFile = txtInputFile.getText();
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			DiagnosticCollector<JavaFileObject> diagnostics =
				       new DiagnosticCollector<JavaFileObject>();
			StandardJavaFileManager fm = compiler.getStandardFileManager(diagnostics, getLocale(), null);
				        
	        List<File> sourceFileList = new ArrayList <File> ();
	        sourceFileList.add (new File (sSourceFile));
	        Iterable<? extends JavaFileObject> compilationUnits =fm.getJavaFileObjectsFromFiles (sourceFileList);
	        
	        CompilationTask task = compiler.getTask (fw,fm, null, null, null, compilationUnits);
	        boolean result = task.call();
	        try {
	        	fm.close ();
	        } catch (IOException e) {
	        }
	        if (result) {
	            System.out.println ("Compilation was successful");	  
	            return true;
	        } else {
	            System.out.println ("Compilation failed");
	            return false;
	        }	        
		}catch (FileNotFoundException e) { 
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return false;
	}
	
	private void loadTable(){
		try {
			FileReader fr = new FileReader(tableFileName);
			setTableBufferedReader(new BufferedReader(fr));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean isAnalysable(int lineNumber, Method [] aMethods){		
		for (Method method : aMethods) {
			if (method.getName().equalsIgnoreCase("main") || method.getName().equalsIgnoreCase("<init>"))
				continue;
			else {
				LineNumberTable lineNumberTable = method.getLineNumberTable();		
				for(int i=0; i < lineNumberTable.getLength(); i++){
					if (lineNumber == lineNumberTable.getSourceLine(i)){
						return true;
					}
					else if (lineNumber < lineNumberTable.getSourceLine(i)){
						return false;
					}
				}
			}
		}
		return false;
	}
	
	private String getTime(int lineNumber, Method[] aMethods){
		int iIndexCode = 0; 
		int iIndexLineNumber = 0;
		String sByteCodeLineNumber = "";
		String sByteCodeString = "";
		for(Method method : aMethods){
			if (method.getName().equalsIgnoreCase("main") || method.getName().equalsIgnoreCase("<init>"))
				continue;
			else {
				LineNumberTable lineNumberTable = method.getLineNumberTable();
				Code code = method.getCode();
				iIndexLineNumber = code.toString().indexOf("LineNumber", iIndexLineNumber);
				sByteCodeLineNumber = code.toString().substring(iIndexLineNumber + 11, code.toString().indexOf(",",iIndexLineNumber)) + ":";
				iIndexCode = code.toString().indexOf(sByteCodeLineNumber, iIndexCode);
				sByteCodeString = code.toString().substring(code.toString().indexOf(":", iIndexCode) + 1, code.toString().indexOf(System.getProperty("line.separator")));
				sByteCodeString = sByteCodeString.trim();
				if (sByteCodeString.contains(" ")){
					sByteCodeString = sByteCodeString.substring(0, sByteCodeString.indexOf(" "));
				}
				if (sByteCodeString.contains("\t")){
					sByteCodeString = sByteCodeString.substring(0, sByteCodeString.indexOf("\t"));
				}
				if (sByteCodeString.contains(System.getProperty("line.separator"))){
					sByteCodeString = sByteCodeString.split(System.getProperty("line.separator"))[0];
				}
				if (sByteCodeString.contains(":")){
					int iIndexLineNumberTemp = Integer.parseInt(sByteCodeLineNumber.substring(0, sByteCodeLineNumber.length()-1)) + 1;
					String sGarbage = iIndexLineNumberTemp + ":";
					sByteCodeString = sByteCodeString.substring(0, sByteCodeString.indexOf(iIndexLineNumberTemp + ":"));
				}
				System.out.println(sByteCodeString);
				System.out.println(method.getName());
				System.out.println(code.toString());
			    
			}
				
			
//			LineNumberTable oLineNumberTable = code.getLineNumberTable();					
//			System.out.println("");					
//			System.out.println(oLineNumberTable.getLength());
//			System.out.println("");		
//			for(int i=0; i < oLineNumberTable.getLength(); i++){
//				System.out.println(oLineNumberTable.getSourceLine(i));	
//			}					
		}		
		return "";
	}
	
	private void startAnalysis(){
		boolean bSuccess = compileCode();	
		if (bSuccess){
			try {
				loadTable();
				ClassParser oClassParser = new ClassParser(file.getAbsolutePath().replace(".java" , ".class" )) ;
				OutputStream osw = null;
				JavaClass jc = oClassParser.parse();
				Method [] aMethods = jc.getMethods();
				BufferedReader javaBufferedReader = new BufferedReader(new FileReader(file));
				BufferedWriter javaBufferedWriter = new BufferedWriter(new FileWriter(file.getAbsolutePath().replace(".java", "_a.java")));
				String line = null;
				int lineNumber = 1;
				while((line = javaBufferedReader.readLine()) != null)
				{
					if (isAnalysable(lineNumber, aMethods)){
						//TODO: write analysis
						String sAnnotation = getTime(lineNumber, aMethods);
						javaBufferedWriter.write(line);
						javaBufferedWriter.write(System.getProperty("line.separator"));
						javaBufferedWriter.flush();
					}
					else {
						javaBufferedWriter.write(line);
						javaBufferedWriter.write(System.getProperty("line.separator"));
						javaBufferedWriter.flush();
					}
					lineNumber++;
				}
				javaBufferedReader.close();
				javaBufferedWriter.close();
						
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			
		}
	}

	/**
	 * Create the frame.
	 */
	public TimeAnnotationTool() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 200);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		txtInputFile = new JTextField();
		txtInputFile.setBounds(122, 36, 346, 20);
		contentPane.add(txtInputFile);
		txtInputFile.setColumns(10);
		
		JLabel lblInputFile = new JLabel("Input java file: ");
		lblInputFile.setBounds(10, 39, 102, 14);
		contentPane.add(lblInputFile);
		
		btnAdd = new JButton("Add...");
		btnAdd.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				openFileChoose();
			}
		});
		btnAdd.setBounds(480, 35, 90, 23);
		contentPane.add(btnAdd);
		
		btnAnalyze = new JButton("Analyze");
		btnAnalyze.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				startAnalysis();
			}
		});
		btnAnalyze.setBounds(122, 67, 90, 23);
		contentPane.add(btnAnalyze);
	}
}
