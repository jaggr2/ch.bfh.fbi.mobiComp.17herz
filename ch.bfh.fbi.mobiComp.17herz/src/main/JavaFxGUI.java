package main;

import ch.quantasy.tinkerforge.tinker.application.implementation.AbstractTinkerforgeApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.*;
import javafx.scene.chart.*;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import sensor.BarometerApplication;

import java.util.LinkedList;
import java.util.List;

public class JavaFxGUI extends Application implements IEventLogEntryListener {
    SplitPane splitPane1 = null;
    BorderPane pane1;
    BorderPane pane2;
    Line LV1, LV2;

    public static final int MAX_DATA_POINTS = 1000;
    public static List<Stage> stages = new LinkedList<Stage>();
    private NumberAxis xAxis;
    private NumberAxis yAxis;

    private NumberAxis initXAxis() {
        final NumberAxis xAxis = new NumberAxis(0, MAX_DATA_POINTS, MAX_DATA_POINTS / 10);
        xAxis.setTickLabelFont(Font.font("Arial", FontWeight.MEDIUM, 14));
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
        yAxis.setTickLabelFont(Font.font("Arial", FontWeight.MEDIUM, 14));
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
        lineChart.setId("Live Barometer Values");
        lineChart.setTitle("Barometer App");

        return lineChart;
    }

    //XYChart.Series series1 = new XYChart.Series();
    //XYChart.Series series2 = new XYChart.Series();
    private TableView table = new TableView();
    private ObservableList<EventLogEntry> items;


    @Override
    public void start(Stage stage) {
        stage.setTitle("Lines plot");

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

        The17HerzApplication.getInstance().addEventLogEntryListener(this);

        pane1 = new BorderPane();
        pane1.setCenter(chart);

        splitPane1 = new SplitPane();
        splitPane1.setOrientation(Orientation.VERTICAL);
        splitPane1.getItems().addAll(pane1);
        splitPane1.setDividerPosition(0, 1);

        table.setEditable(true);

        TableColumn timeColumn = new TableColumn("Time");
        timeColumn.setMinWidth(100);
        timeColumn.setCellValueFactory(new PropertyValueFactory<EventLogEntry, Long>("timestamp"));

        TableColumn descriptionColumn = new TableColumn("Description");
        descriptionColumn.setMinWidth(500);
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<EventLogEntry, String>("description"));

        table.getColumns().addAll(timeColumn, descriptionColumn);

        items = FXCollections.observableArrayList();
        table.setItems(items);


        pane2 = new BorderPane();
        pane2.setCenter(table);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                double percSplit;

                splitPane1.getItems().addAll(pane2);

                ObservableList<SplitPane.Divider> splitDiv = splitPane1.getDividers();

                percSplit = 1 / (double) (splitDiv.size() + 1);
                for (int i = 0; i < splitDiv.size(); i++) {
                    splitPane1.setDividerPosition(i, percSplit);
                    percSplit += 1 / (double) (splitDiv.size() + 1);
                }
            }
        });

        LV1 = LineBuilder.create()
                .strokeWidth(2)
                .startY(0)
                .endY(300)
                .stroke(Color.FORESTGREEN)
                .build();

        StackPane stack = new StackPane();
        Pane glassPane = new Pane();
        glassPane.getChildren().add(LV1);
        glassPane.minWidthProperty().bind(splitPane1.widthProperty());
        glassPane.minHeightProperty().bind(splitPane1.heightProperty());
        glassPane.setMouseTransparent(true);
        stack.getChildren().addAll(splitPane1, glassPane);
        Scene scene = new Scene(stack, 800, 600);
        LV1.endYProperty().bind(pane1.heightProperty());
        stage.setScene(scene);
        //pane1.setOnMouseMoved(mouseHandler);

        stage.show();
    }

    EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {
        @Override public void handle(MouseEvent mouseEvent) {
            XYChart<Number, Number> chart1 = (XYChart<Number, Number>) pane1.getCenter();
            plotLine(chart1, LV1, mouseEvent.getX() + 1);
        }
    };

    private void plotLine(XYChart<Number, Number> chart, Line line, double x) {
        Axis xAxis = chart.getXAxis(), yAxis = chart.getYAxis();
        final double min = getSceneShift(xAxis);
        final double max = min + xAxis.getWidth();
        boolean setCrosshair = false;
        if (x > min && x < min + xAxis.getWidth()) {
            LV1.setStartX(x); LV1.setEndX(x);
            setCrosshair = true;
        } else if (x <= min){
            LV1.setStartX(min); LV1.setEndX(min);
        } else if (x >= max){
            LV1.setStartX(max); LV1.setEndX(max);
        }
        if (setCrosshair) {
            chart.setCursor(Cursor.CROSSHAIR);
        } else {
            chart.setCursor(Cursor.DEFAULT);
        }
    }

    private static double getSceneShift(Node node) {
        double shift = 0;
        do {
            shift += node.getLayoutX();
            node = node.getParent();
        } while (node != null);
        return shift;
    }
/*
    public static void main(String[] args) {
        launch(args);
    }*/

    @Override
    public void logEventHappened(Object source, EventLogEntry entry) {
        items.add(entry);
    }
}
