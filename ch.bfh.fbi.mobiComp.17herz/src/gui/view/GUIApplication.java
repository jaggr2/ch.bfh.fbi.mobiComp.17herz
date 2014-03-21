package gui.view;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ch.quantasy.tinkerforge.tinker.application.implementation.AbstractTinkerforgeApplication;
import gui.BarometerApplication2;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import main.The17HerzApplication;
import sensor.BarometerApplication;

/**
 */
public class GUIApplication extends Application {

	public GUIApplication() {

	}

    public static List<Stage> stages = new LinkedList<Stage>();

    private static final int MAX_DATA_POINTS = 1000;
    private NumberAxis xAxis;
    private NumberAxis yAxis;

    private NumberAxis initXAxis() {
        final NumberAxis xAxis = new NumberAxis(0, MAX_DATA_POINTS, MAX_DATA_POINTS / 10);
        xAxis.setTickLabelFont(Font.font("Arial", FontWeight.MEDIUM, 18));
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);
        return xAxis;
    }

    private NumberAxis initYAxis() {
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(final Number object) {
                return String.format("%6.2f", object);
            }
        });
        yAxis.setTickLabelFont(Font.font("Arial", FontWeight.MEDIUM, 18));
        yAxis.setPrefWidth(120);
        yAxis.setAutoRanging(true);
        yAxis.setLabel("miliBar");
        yAxis.setForceZeroInRange(false);
        yAxis.setAnimated(true);
        return yAxis;
    }

    private LineChart<Number, Number> initChart() {
        // -- Chart
        final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(
                this.xAxis, this.yAxis) {
            // Override to remove symbols on each data point
            @Override
            protected void dataItemAdded(final Series<Number, Number> series,
                                         final int itemIndex, final Data<Number, Number> item) {
            }
        };
        lineChart.setAnimated(false);
        lineChart.setId("Live Altitude Position");
        lineChart.setTitle("Sensor-Fusion (Altitude)");
        return lineChart;
    }

    /*
    public void initStage(final Stage stage) {
        this.xAxis = this.initXAxis();
        this.yAxis = this.initYAxis();
        final LineChart<Number, Number> chart = this.initChart();

        // Chart Series
        this.guiSeries = new XYChart.Series<Number, Number>();
        this.guiSeries.setName("Barometer " + this.getId());
        chart.getData().add(this.guiSeries);

        stage.setScene(new Scene(chart));
    }
    */


    @Override
	public void start(final Stage primaryStage) throws Exception {
		// primaryStage.show();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {

                xAxis = initXAxis();
                yAxis = initYAxis();
                final LineChart<Number, Number> chart = initChart();

                // Chart Series
                for(AbstractTinkerforgeApplication application : The17HerzApplication.connectedApps.values()) {
                    if(application instanceof BarometerApplication) {
                        BarometerApplication barometerApplication = (BarometerApplication)application;
                        //Stage theNewStage = new Stage();
                        barometerApplication.initChart(chart);
                        //theNewStage.show();
                    }
                }

                primaryStage.setScene(new Scene(chart));
                primaryStage.show();

                    /*
                for(AbstractTinkerforgeApplication application : The17HerzApplication.connectedApps.values()) {
                    if(application instanceof BarometerApplication) {
                        BarometerApplication barometerApplication = (BarometerApplication)application;
                        Stage theNewStage = new Stage();
                        barometerApplication.initStage(theNewStage);
                        theNewStage.show();
                    }
                }
                */

                //primaryStage.show();

                /*
                final Stage altitudeStage = new Stage();
				final AltitudeProfileView e = new AltitudeProfileView(
						altitudeStage);
				GUIApplication.stages.add(altitudeStage);
				
				for (final Stage stage : GUIApplication.stages) {
					stage.show();
				}
				*/
			}
		});

	}

	public static void finish() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
/*
				for (final Stage stage : GUIApplication.stages) {
					stage.close();
				}
*/
			}
		});
	}

}
