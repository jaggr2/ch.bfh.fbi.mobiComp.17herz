package main;

import ch.quantasy.tinkerforge.tinker.application.implementation.AbstractTinkerforgeApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import sensor.BarometerApplication;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class GUIApplication extends Application {

    public static final int MAX_DATA_POINTS = 1000;
    public static List<Stage> stages = new LinkedList<Stage>();
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    public GUIApplication() {

    }

    public static void finish() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    private NumberAxis initXAxis() {
        final NumberAxis xAxis = new NumberAxis(0, MAX_DATA_POINTS, MAX_DATA_POINTS / 10);
        xAxis.setTickLabelFont(Font.font("Arial", FontWeight.MEDIUM, 18));
        xAxis.setForceZeroInRange(false);
        xAxis.setLabel("timestamp");
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
                for (AbstractTinkerforgeApplication application : The17HerzApplication.connectedApps.values()) {
                    if (application instanceof BarometerApplication) {
                        BarometerApplication barometerApplication = (BarometerApplication) application;
                        barometerApplication.initChart(chart, xAxis, yAxis);
                    }
                }

                primaryStage.setScene(new Scene(chart));
                primaryStage.show();
            }
        });

    }

}
