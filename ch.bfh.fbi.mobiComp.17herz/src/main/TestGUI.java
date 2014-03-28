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

public class TestGUI extends Application implements IEventLogEntryListener {
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
/*
        primaryStage.setScene(new Scene(chart));
        primaryStage.show();

        final NumberAxis xAxis = new NumberAxis(1, 12, 1);
        final NumberAxis yAxis = new NumberAxis(0.53000, 0.53910, 0.0005);

        xAxis.setAnimated(false);
        xAxis.setScaleX(0);
        yAxis.setAnimated(false);

        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(Number object) {
                return String.format("%7.5f", object);
            }
        });



        final LineChart<Number, Number> lineChart1 = new LineChart<Number, Number>(xAxis, yAxis);

        lineChart1.setCreateSymbols(false);
        lineChart1.setAlternativeRowFillVisible(false);
        lineChart1.setAnimated(false);
        lineChart1.setLegendVisible(false);

        series1.getData().add(new XYChart.Data(1, 0.53185));
        series1.getData().add(new XYChart.Data(2, 0.532235));
        series1.getData().add(new XYChart.Data(3, 0.53234));
        series1.getData().add(new XYChart.Data(4, 0.538765));
        series1.getData().add(new XYChart.Data(5, 0.53442));
        series1.getData().add(new XYChart.Data(6, 0.534658));
        series1.getData().add(new XYChart.Data(7, 0.53023));
        series1.getData().add(new XYChart.Data(8, 0.53001));
        series1.getData().add(new XYChart.Data(9, 0.53589));
        series1.getData().add(new XYChart.Data(10, 0.53476));
        series1.getData().add(new XYChart.Data(11, 0.530123));
        series1.getData().add(new XYChart.Data(12, 0.531035));

        lineChart1.getData().addAll(series1);
*/
        pane1 = new BorderPane();
        pane1.setCenter(chart);

        splitPane1 = new SplitPane();
        splitPane1.setOrientation(Orientation.VERTICAL);
        splitPane1.getItems().addAll(pane1);
        splitPane1.setDividerPosition(0, 1);

        final CategoryAxis xAxis2 = new CategoryAxis();
        final NumberAxis yAxis2 = new NumberAxis();

        yAxis2.setTickUnit(1);
        yAxis2.setPrefWidth(35);
        yAxis2.setMinorTickCount(10);

        yAxis2.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis2) {
            @Override
            public String toString(Number object) {
                String label;
                label = String.format("%7.2f", object.floatValue());
                return label;
            }
        });

        /*
        final ListView<String> list = new ListView<String>();
        ObservableList<String> items = FXCollections.observableArrayList(
                "Single", "Double", "Suite", "Family App");
        list.setItems(items);
*/
        table.setEditable(true);

        TableColumn timeColumn = new TableColumn("Time");
        timeColumn.setMinWidth(100);
        timeColumn.setCellValueFactory(new PropertyValueFactory<EventLogEntry, Long>("timestamp"));

        TableColumn descriptionColumn = new TableColumn("Description");
        descriptionColumn.setMinWidth(500);
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<EventLogEntry, String>("description"));

        table.getColumns().addAll(timeColumn, descriptionColumn);


        items = FXCollections.observableArrayList();
                //new EventLogEntry(1L, "test"), new EventLogEntry(2L, "test2"), new EventLogEntry(2L, "test2"), new EventLogEntry(2L, "test2"), new EventLogEntry(2L, "test2"), new EventLogEntry(2L, "test2"), new EventLogEntry(2L, "test2"), new EventLogEntry(2L, "test2"), new EventLogEntry(2L, "test2"), new EventLogEntry(2L, "test2"), new EventLogEntry(2L, "test2"), new EventLogEntry(2L, "test2"), new EventLogEntry(2L, "test2"));

        table.setItems(items);


        The17HerzApplication.getInstance().addEventLogEntryListener(this);

/*
        final BarChart<String, Number> BarChart = new BarChart<String, Number>(xAxis2, yAxis2);

        BarChart.setAlternativeRowFillVisible(false);
        BarChart.setLegendVisible(false);
        BarChart.setAnimated(false);

        XYChart.Series series2 = new XYChart.Series();

        series2.getData().add(new XYChart.Data("Jan", 1));
        series2.getData().add(new XYChart.Data("Feb", 3));
        series2.getData().add(new XYChart.Data("Mar", 1.5));
        series2.getData().add(new XYChart.Data("Apr", 3));
        series2.getData().add(new XYChart.Data("May", 4.5));
        series2.getData().add(new XYChart.Data("Jun", 5));
        series2.getData().add(new XYChart.Data("Jul", 4));
        series2.getData().add(new XYChart.Data("Aug", 8));
        series2.getData().add(new XYChart.Data("Sep", 16.5));
        series2.getData().add(new XYChart.Data("Oct", 13.9));
        series2.getData().add(new XYChart.Data("Nov", 17));
        series2.getData().add(new XYChart.Data("Dec", 20));

        BarChart.getData().addAll(series2); */

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

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void logEventHappened(Object source, EventLogEntry entry) {
        items.add(entry);
    }
}
