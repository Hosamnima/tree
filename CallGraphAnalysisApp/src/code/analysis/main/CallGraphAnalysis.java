package code.analysis.main;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JStatement;
import com.sun.codemodel.JVar;

import soot.Body;
import soot.G;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.Targets;

public class CallGraphAnalysis  {

	String rtaAnalysisCommand = "-p cg.spark on -p cg.spark rta:true -p cg.spark on-fly-cg:false";
	String sparkAnalysisCommand = "-p cg.spark on -p cg.spark rta:true -p cg.spark on-fly-cg:false";
	String vtaAnalysisCommand = "-p cg.spark on -p cg.spark vta:true";
	String chaAnalysisCommand = "-p cg.cha on";
	
	
	final String temporaryFilePath = "e:\\temporaryresults.txt";
	final String sootMainClassPath = "c:\\users\\eng.hosam\\workspace2\\soot-develop\\soot-develop\\classes";
	final String finalResults ="e:\\gnuplot.txt";	

	public static void main(String[] args) {
		CallGraphAnalysis callGraphAnalysis = new CallGraphAnalysis();

//		final String targetMethod = "code.analysis.test.MyNewClass999";
//		callGraphAnalysis.analyze(targetMethod);				
		callGraphAnalysis.memoryTestForClassDownUpHierachy(1000, 100,callGraphAnalysis.chaAnalysisCommand);
		

	}

	
	private void memoryTestForClassHierachy(int methodNum, int intervalLength,String analysisType) {
//
		String targetClass = "code.analysis.test3.MyClass";
		Map<Integer, ArrayList<Double>> memoryValMap = new HashMap<Integer, ArrayList<Double>>();
		Map<Integer, ArrayList<Double>> timeValMap = new HashMap<Integer, ArrayList<Double>>();
		ArrayList<Double> maxMememoryUsageVals ;
		ArrayList<Double> timeConsumptionVals ;

		int interval = intervalLength;
		int numOfMethods = methodNum;
		int numOfGeneratedMethods = 0;

		while (numOfGeneratedMethods < numOfMethods) {

			// Create the hierachy
			maxMememoryUsageVals = new ArrayList<Double>();
			timeConsumptionVals = new ArrayList<Double>();

			if (numOfGeneratedMethods == 0)
				this.buildClassHierachy(interval);
			else
				this.buildClassHierachy(numOfGeneratedMethods + interval - 1);

			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Start Analyzing

			for (int i = 0; i < 5; i++) {

//				analysis(targetClass, analysisType);
				analyze(targetClass);

			}

			// Get analysis results from local file
			getResults(maxMememoryUsageVals,timeConsumptionVals);

			// Save in file compatible with gnuplot

			numOfGeneratedMethods += interval;

			memoryValMap.put(numOfGeneratedMethods, maxMememoryUsageVals);
			timeValMap.put(numOfGeneratedMethods, timeConsumptionVals);

		}

		showMemoryConsumptionResults("NumOfMothods vx Memory", "num of methods",
				"memory consumption(kbyte)", memoryValMap);
		showMemoryConsumptionResults("NumOfMothods vx Time", "num of methods",
				"time consumption(msec)", timeValMap);

	}

	private void analysis(String targetClass, String analysisType) {
		Process process;
		try {
			process = Runtime
					.getRuntime()
					.exec("java -cp \"libs/*;\" soot.Main "+ "-w "+ "-output-format none -src-prec only-class -process-dir ./targetsBin -pp -no-bodies-for-excluded "
							+ analysisType + " " + targetClass,
							null,
							new File(sootMainClassPath));

			BufferedReader input = new BufferedReader(new InputStreamReader(
					process.getInputStream()));

			String line = null;

			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}

			int exitVal;

			exitVal = process.waitFor();
			

			System.out.println("Exited with error code " + exitVal);
			process.destroy();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void getResults(ArrayList<Double> memoryValList,ArrayList<Double> timeValList) {

		try {

			
			File file = new File(temporaryFilePath);

			if(!file.exists()){
    			return;
    		}
			
			Scanner sc2 = null;
		    try {
		        sc2 = new Scanner(file);
		    } catch (FileNotFoundException e) {
		        e.printStackTrace();  
		    }
//		    sc2.nextLine();
//		    sc2.nextLine();
//		    sc2.nextLine();
		    while (sc2.hasNextLine()) {
		            Scanner s2 = new Scanner(sc2.nextLine());

		            String s = s2.next();
		            
		            
		            Double maxMemoryVal = Double.parseDouble(s);
					
					memoryValList.add(maxMemoryVal);
					s = s2.next();
					 Double timeVal = Double.parseDouble(s);
						
					timeValList.add(timeVal);
		            

		    }
			
			sc2.close();


			if (file.delete()) {
				System.out.println(file.getName() + " is deleted!");
			} else {
				System.out.println("Delete operation is failed.");
			}
		} catch (Exception e) {

			e.printStackTrace();

		}

	}

	private void showMemoryConsumptionResults(String chartTitle, String xAxis, String yAxis,
			Map<Integer, ArrayList<Double>> map) {

		JFreeChartImp jFreeChart = new JFreeChartImp(chartTitle, xAxis, yAxis, map);
	
	}
		
	private void showTimeConsumptionResults(String chartTitle, String xAxis, String yAxis,
			Map<Integer, ArrayList<Double>> map) {
		
		JFreeChartImp jFreeChart = new JFreeChartImp(chartTitle, xAxis, yAxis, map);
	}

	private void memoryTestForFactoryMethodHierachy(int classNum,
			int intervalLength, String analysisType) {

		
		final String targetClass = "code.analysis.test2.MainClass";
		
		
		Map<Integer, ArrayList<Double>> memoryValMap = new HashMap<Integer, ArrayList<Double>>();
		Map<Integer, ArrayList<Double>> timeValMap = new HashMap<Integer, ArrayList<Double>>();
		ArrayList<Double> maxMememoryUsageVals ;
		ArrayList<Double> timeConsumptionVals ;

		int interval = intervalLength;
		int numOfClasses = classNum;
		int numOfGeneratedClasses = 0;

		while (numOfGeneratedClasses < numOfClasses) {

			maxMememoryUsageVals = new ArrayList<Double>();
			timeConsumptionVals = new ArrayList<Double>();
			
			
			// don't comment this part at all
			

			if (numOfGeneratedClasses == 0)
				this.buildFactoryMethodHierachy(numOfGeneratedClasses, interval);
			else
				this.buildFactoryMethodHierachy(numOfGeneratedClasses,
						numOfGeneratedClasses + interval - 1);

			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int i = 0; i < 5; i++) {

//				analysis(targetClass, analysisType);
				analyze(targetClass);

			}

			// Get analysis results from local file
			getResults(maxMememoryUsageVals,timeConsumptionVals);

			// Save in file compatible with gnuplot

			numOfGeneratedClasses += interval;

			memoryValMap.put(numOfGeneratedClasses, maxMememoryUsageVals);
			timeValMap.put(numOfGeneratedClasses, timeConsumptionVals);

		}

		showMemoryConsumptionResults("NumOfClasses vs Memory", "num of classes",
				"memory consumption(kbyte)", memoryValMap);
		showMemoryConsumptionResults("NumOfClasses vs Time", "num of classes",
				"time consumption(msec)", timeValMap);
		

	}
	
	private void memoryTestForClassDownUpHierachy(int classNum,
			int intervalLength, String analysisType) {
		// TODO Auto-generated method stub

		Map<Integer, ArrayList<Double>> memoryValMap = new HashMap<Integer, ArrayList<Double>>();
		Map<Integer, ArrayList<Double>> timeValMap = new HashMap<Integer, ArrayList<Double>>();
		ArrayList<Double> maxMememoryUsageVals ;
		ArrayList<Double> timeConsumptionVals ;

		int interval = intervalLength;
		int numOfClasses = classNum;
		int numOfGeneratedClasses = 0;

		while (numOfGeneratedClasses < numOfClasses) {
			maxMememoryUsageVals = new ArrayList<Double>();
			timeConsumptionVals = new ArrayList<Double>();
			
			String targetClass = "code.analysis.test.MyNewClass"
					+ String.valueOf(interval + numOfGeneratedClasses - 1);

			if (numOfGeneratedClasses == 0)
				this.buildDownUpHierachy(numOfGeneratedClasses, interval - 1);
			else
				this.buildDownUpHierachy(numOfGeneratedClasses,
						numOfGeneratedClasses + interval - 1);
			
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int i = 0; i < 5; i++) {

//				analysis(targetClass, analysisType);
				analyze(targetClass);

			}
			// Get analysis results from local file
			getResults(maxMememoryUsageVals,timeConsumptionVals);

			// Save in file compatible with gnuplot

			numOfGeneratedClasses += interval;

			memoryValMap.put(numOfGeneratedClasses, maxMememoryUsageVals);
			timeValMap.put(numOfGeneratedClasses, timeConsumptionVals);

		}
		
		System.out.println(memoryValMap);
		System.out.println(timeValMap);

		appendDataToTxtFile(memoryValMap, timeValMap);
		showMemoryConsumptionResults("NumOfClasses vs Memory", "num of classes",
				"memory consumption(kbyte)", memoryValMap);
		showMemoryConsumptionResults("NumOfClasses vs Time", "num of classes",
				"time consumption(msec)", timeValMap);

	}
		
	private void appendDataToTxtFile(Map<Integer,ArrayList<Double>> memoryMap, Map<Integer,ArrayList<Double>> TimeMap) {
		// TODO Auto-generated method stub
		
		
		try{
			File file =new File(finalResults);
			if(file.exists()){
			if (file.delete()) {
				System.out.println(file.getName() + " is deleted!");
			} else {
				System.out.println("Delete operation is failed.");
			}
			}
	    		if(!file.exists()){
	    			file.createNewFile();
	    		}
	    		
	    		
			for (Integer key : memoryMap.keySet()) {

				double memoryAverage = this.calculateAverage(memoryMap.get(key));
				double memoryMax = Collections.max(memoryMap.get(key));
				double memoryMin = Collections.min(memoryMap.get(key));
				
				double timeAverage = this.calculateAverage(TimeMap.get(key));
				double timeMax = Collections.max(TimeMap.get(key));
				double timeMin = Collections.min(TimeMap.get(key));
						
    		String data = String.valueOf(key)+"  "+String.valueOf(memoryAverage)+"  "+String.valueOf(memoryMin)+"  "+String.valueOf(memoryMax)+"  "+String.valueOf(timeAverage)+"  "+String.valueOf(timeMin)+"  "+String.valueOf(timeMax);
 
    		
 
    		//if file doesnt exists, then create it
    		
 
    		
    		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(finalResults, true)));
    		    out.println(data);
    		    out.close();
    		
			}
 
    	}catch(IOException e){
    		e.printStackTrace();
    	}
	}

	private double calculateAverage(List<Double> maxMemoryUsageVals) {
		if (maxMemoryUsageVals == null || maxMemoryUsageVals.isEmpty()) {
			return 0;
		}

		double sum = 0;
		for (Double memoryUsage : maxMemoryUsageVals) {
			sum += memoryUsage;
		}

		return sum / maxMemoryUsageVals.size();
	}

	private void printEdgesOutOfAdd(final String targetMethod) {
		final CallGraph cg = Scene.v().getCallGraph();

		SootMethod meth = Scene.v().getMethod(targetMethod);
		// SootMethod meth =
		// Scene.v().getMethod("<code.analysis.test.MyNewClass4: void call()>");
		// System.out.println("printEdges");
		// JimpleBody body = Jimple.v().newBody(meth);
		// meth.setActiveBody(body);
		if (meth.hasActiveBody())
			for (Unit u : meth.getActiveBody().getUnits()) {
				Stmt s = (Stmt) u;
				draw(s, cg);

			}
	}

	private void draw(Stmt s, CallGraph cg) {
		// TODO Auto-generated method stub
		if (s.containsInvokeExpr()) {
			InvokeExpr iexpr = s.getInvokeExpr();
			for (Iterator<Edge> edgeIt = cg.edgesOutOf(s); edgeIt.hasNext();) {
				Edge edge = edgeIt.next();
				
				System.out.println("\t->" +edge.src()+" calls  "+ edge.tgt());
				
				// if (iexpr.getMethod().getName().equals("bar")){
				try {
					Body b = iexpr.getMethod().getActiveBody();
					for (Unit un : b.getUnits()) {

						Stmt st = (Stmt) un;
						draw(st, cg);
						
					}
					break;
				} catch (RuntimeException e) {

				}
			}
		}

	}

	private void analyze(final String targetClass) {
		G.reset();
		PackManager.v().getPack("wjtp")
				.add(new Transform("wjtp.chaTest", new SceneTransformer() {

					@Override
					protected void internalTransform(String phaseName,
							Map<String, String> options) {
						String target = "<" + targetClass + ": void m()>";
						System.out.println("Analyzing this method " + target);

						// CHATransformer.v().transform();
						 printEdgesOutOfAdd(target);
					}

				}));

		soot.Main.main(new String[] { "-w", "-output-format", "none",
				"-src-prec", "only-class", "-process-dir", "./targetsBin",
//				 "-src-prec", "only-class", "-process-dir",
//				 "factoryMethodHierachyBin",

				"-pp", "-no-bodies-for-excluded",

//				 for RTA
//				 "-p", "cg.spark", "on",
//				 "-p", "cg.spark", "rta:true",
//				 "-p", "cg.spark", "on-fly-cg:false",

				// for spark
				// "-p", "cg.spark", "on", "-p",
				// "cg.spark", "rta:true", "-p", "cg.spark", "on-fly-cg:false",

				// for CHA
				"-p", "cg.cha", "on",

				// for VTA
				// "-p", "cg.spark", "on", "-p",
				// "cg.spark", "vta:true",

				// "code.analysis.test.CallGraphs"
				targetClass

		});
	}

	public void buildDownUpHierachy(int from, int to) {

		JCodeModel codeModel = new JCodeModel();
		JDefinedClass fClass = null;
		JDefinedClass definedClass = null;
		for (int i = from; i <= to; i++) {

			String className = "code.analysis.test.MyNewClass"
					+ String.valueOf(i);
			try {
				if (i == 0) {
					definedClass = codeModel._class(className);
					fClass = definedClass;
					JMethod method = definedClass.method(JMod.PUBLIC,
							void.class, "m");
					
					codeModel.build(new File("./targets"));
				} else {
					if (i == from) {
						String className2 = "code.analysis.test.MyNewClass"
								+ String.valueOf(i - 1);
						JClass x = codeModel.directClass(className2);
						JClass y = codeModel.directClass("code.analysis.test.MyNewClass"
								+ String.valueOf(i - 2));
						
						definedClass = codeModel._class(className)._extends(x);
						codeModel._class(className2)._extends(y).method(JMod.PUBLIC,
								void.class, "m").body().add(JExpr._super().invoke("m"));
//								
//						
						
						
						
						codeModel._getClass(x.fullName()).methods().remove(0);
						
						
						
								
						
						
					} else {

						definedClass = codeModel._class(className)._extends(
								fClass);
					}

					// String className2 =
					// "code.analysis.test.MyNewClass"+String.valueOf(i-1);
					// JFieldVar field = (JFieldVar)
					// definedClass.field(JMod.PRIVATE,
					// codeModel._getClass(className2),
					// "myName").init(JExpr._new(codeModel._getClass(className2)));
					
					

					JMethod method = definedClass.method(JMod.PUBLIC,
							void.class, "m");

					JBlock body;
					body = method.body();

					// JVar myNameVariable = body.decl(
					// codeModel._getClass(className2), "myName");
					// body.assign(myNameVariable,
					// JExpr._new(codeModel._getClass(className2)));
					// JInvocation invocation = field
					// .invoke(method);

					body.add(JExpr._super().invoke("m"));

					
					if(i==to){
						
						JMethod mainMethod = definedClass.method(JMod.PUBLIC | JMod.STATIC, void.class,
								"main");
						mainMethod.param(codeModel._ref(String[].class),"args");
//						JBlock body;
						body = mainMethod.body();			
						 JVar classInstance = body.decl(
						 codeModel._getClass(className), "myClass");
						 body.assign(classInstance,
						 JExpr._new(codeModel._getClass(className)));
						
						 body.add(classInstance.invoke("m"));
						
					}
					fClass = definedClass;
					codeModel.build(new File("./targets"));
				}
				
				
				// num++;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			catch (JClassAlreadyExistsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public void buildNewHierachy(int from, int to) {

			JCodeModel codeModel = new JCodeModel();
			JDefinedClass definedClass = null;
			for (int i = from; i <= to; i++) {

				String className = "code.analysis.test.MyNewClass"
						+ String.valueOf(i);
				try {
					
						
						String parentClassName = "MyNewClass"+ String.valueOf(i - 1);
							
							
						definedClass = codeModel._class(className);
if(i!=0){
						JMethod method = definedClass.method(JMod.PUBLIC | JMod.STATIC,
								void.class, "m");

						JBlock body;
						body = method.body();

						

						body.directStatement(parentClassName+".m();");
}

						
						if(i==to){
							
							JMethod mainMethod = definedClass.method(JMod.PUBLIC | JMod.STATIC, void.class,
									"main");
							mainMethod.param(codeModel._ref(String[].class),"args");
							JBlock body;
							body = mainMethod.body();			
							 JVar classInstance = body.decl(
							 codeModel._getClass(className), "myClass");
							 body.assign(classInstance,
							 JExpr._new(codeModel._getClass(className)));
							
							 body.add(classInstance.invoke("m"));
							
						}
						
						codeModel.build(new File("./targets"));
					
					
					
					// num++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				catch (JClassAlreadyExistsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}

	public void buildClassHierachy(int methodNum) {
		JCodeModel codeModel = new JCodeModel();
		String className = "code.analysis.test3.MyClass";
		try {
			JDefinedClass definedClass = codeModel._class(className);
			JMethod mainMethod = definedClass.method(JMod.PUBLIC | JMod.STATIC, void.class,
					"main");
			mainMethod.param(codeModel._ref(String[].class),"args");
			JBlock body;
			body = mainMethod.body();			
			 JVar classInstance = body.decl(
			 codeModel._getClass(className), "myClass");
			 body.assign(classInstance,
			 JExpr._new(codeModel._getClass(className)));
			
			 body.add(classInstance.invoke("m0"));

			for (int i = 0; i <= methodNum; i++) {
				JMethod method = definedClass.method(JMod.PUBLIC, void.class,
						"m" + String.valueOf(i));
				
				body = method.body();
				for (int j = 0; j <= methodNum; j++) {

					body.invoke(("m") + String.valueOf(j));
					// body.add(JExpr._super().invoke(("m")+String.valueOf(j)));
				}
			}
			codeModel.build(new File("./targets"));
		} catch (JClassAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void buildFactoryMethodHierachy(int from, int to) {
		String interfaceName = "code.analysis.test2.ISuperClass";
		String className = "code.analysis.test2.Class";
		JCodeModel codeModel = new JCodeModel();
		try {

			JDefinedClass definedInterface = codeModel._class(interfaceName,
					ClassType.INTERFACE);

			String methodName = "toBeImplemented";
			JClass returnType = codeModel.directClass("ISuperClass");
			JMethod method = definedInterface.method(JMod.PUBLIC, returnType,
					methodName);
			method.param(String.class,"x");
			JConditional cond = method.body()._if(JExpr.direct("foo"));
			cond._elseif(JExpr.direct("bar"));
			
			
//			String paramName = parameter.getSimpleName().toString();
//			String paramClassFullyQualifiedName = parameter.asType().toString();
//			JClass paramClass = codeModel.ref(paramClassFullyQualifiedName);
//			JFieldVar setterField = builderClass.field(JMod.PRIVATE, paramClass, paramName);

			for (int i = from; i <= to; i++) {

				JDefinedClass definedClass = codeModel._class(
						className + String.valueOf(i))._implements(
						definedInterface);
				JMethod jMethod = definedClass.method(JMod.PUBLIC, returnType,
						methodName);
				jMethod.annotate(codeModel.ref(Override.class));
				jMethod.param(String.class,"x");

			}
			//
			className = "code.analysis.test2.MainClass";
			JDefinedClass definedClass = codeModel._class(className);
			
			JMethod mainMethod = definedClass.method(JMod.PUBLIC | JMod.STATIC, void.class,
					"main");
			mainMethod.param(codeModel._ref(String[].class),"args");
			JBlock body;
			body = mainMethod.body();			
			 JVar classInstance = body.decl(
			 codeModel._getClass(interfaceName), "superClass");
			 body.assign(classInstance,
			 JExpr._new(codeModel._getClass(className)));
			
			 body.add(classInstance.invoke("tobeImplemented"));

			JMethod jMethod = definedClass.method(JMod.PUBLIC, void.class,
					methodName);

			jMethod.param(definedInterface, "superClass");
			
			body = jMethod.body();

			JVar[] var = jMethod.listParams();
			JInvocation invocation = var[0].invoke(method);
			// .invoke(method);
			body.add(invocation);

			codeModel.build(new File("./targets"));

		} catch (JClassAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
