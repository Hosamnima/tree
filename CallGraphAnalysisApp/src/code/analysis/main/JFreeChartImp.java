package code.analysis.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class JFreeChartImp extends ApplicationFrame{

	public JFreeChartImp(String chartTitle, String xAxis, String yAxis,
			Map<Integer, ArrayList<Double>> map) {
		super(chartTitle);
		// TODO Auto-generated constructor stub
	
		JFreeChart jfreechart = ChartFactory.createXYBarChart(chartTitle,
				xAxis, false, yAxis, addToChart(map), PlotOrientation.VERTICAL,
				true, false, false);
	
	XYBarRenderer barRenderer = (XYBarRenderer) jfreechart.getXYPlot()
			.getRenderer();
	XYErrorRenderer errorRenderer = new XYErrorRenderer();

	jfreechart.getXYPlot().setRenderer(0, errorRenderer);
	jfreechart.getXYPlot().setRenderer(1, barRenderer);
	ChartPanel chartPanel = new ChartPanel(jfreechart);
	chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
	setContentPane(chartPanel);
	this.pack();
	RefineryUtilities.centerFrameOnScreen(this);
	this.setVisible(true);
	}
	
	
	private IntervalXYDataset addToChart(Map<Integer, ArrayList<Double>> map) {
		// TODO Auto-generated method stub

		YIntervalSeriesCollection yintervalseriescollection = new YIntervalSeriesCollection();
		YIntervalSeries yintervalseries = new YIntervalSeries("results");

		ArrayList<Double> maxVals;

		for (Integer key : map.keySet()) {

			maxVals = map.get(key);

			double average = this.calculateAverage(maxVals);
			double max = Collections.max(maxVals);
			double min = Collections.min(maxVals);

			yintervalseries.add(key, average, min, max);

		}
		yintervalseriescollection.addSeries(yintervalseries);
		return yintervalseriescollection;
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

}
