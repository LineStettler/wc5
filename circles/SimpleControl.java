package ch.fhnw.ws4c.circles;

import javafx.animation.Animation;
import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.RotateBuilder;
import javafx.scene.paint.Paint;
import ch.fhnw.ws4c.circles.Switch;
import javafx.util.Duration;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

/**
 * @author Dieter Holz
 */
public class SimpleControl extends Region {
    private static final String FONTS_CSS = "fonts.css";
    private static final String STYLE_CSS = "style.css";

    private static final double PREFERRED_WIDTH = 300;
    private static final double PREFERRED_HEIGHT = 300;

    private static final double ASPECT_RATIO = PREFERRED_WIDTH / PREFERRED_HEIGHT;

    private static final double MINIMUM_WIDTH = 80;
    private static final double MINIMUM_HEIGHT = MINIMUM_WIDTH / ASPECT_RATIO;

    private static final double MAXIMUM_WIDTH = 800;

    private static final double SWITCH_PREFERRED_WIDTH  = 40;
    private static final double SWITCH_PREFERRED_HEIGHT = 26;

    private static final double SWITCH_ASPECT_RATIO = PREFERRED_WIDTH / PREFERRED_HEIGHT;

    private static final double SWITCH_MINIMUM_WIDTH  = 20;
    private static final double SWITCH_MINIMUM_HEIGHT = MINIMUM_WIDTH / ASPECT_RATIO;

    private static final double SWITCH_MAXIMUM_WIDTH = 160;

    private static final Color THUMB_ON  = Color.rgb(62, 130, 247);
    private static final Color THUMB_OFF = Color.rgb(245,245,245);
    private static final Color FRAME_ON  = Color.rgb(162, 197, 255);
    private static final Color FRAME_OFF = Color.rgb(153, 153, 153);

    // all parts
    private Arc arc1;
    private Arc arc2;
    private Arc arc1back;
    private Arc arc2back;
    private Arc arc3;
    private Text display;
    private Pane drawingPane;

    // all Switch parts
    private Circle    thumb;
    private Rectangle frame;

    // all Switch animations;
    private Animation onAnimation;
    private Animation offAnimation;

    // all properties
    private final BooleanProperty on = new SimpleBooleanProperty();


    DateFormat df = new SimpleDateFormat("hh:mm");
    Date today = Calendar.getInstance().getTime();
    String reportDate = df.format(today);
    private final StringProperty text = new SimpleStringProperty(reportDate);
    private Node marks;

    public SimpleControl() {
        init();
        initializeParts();
        layoutParts();
        initializeAnimations();
        addEventHandlers();
        addValueChangedListeners();
        addBindings();
        setOn(true);
    }

    private void init() {
        addStyleSheets(this);
        getStyleClass().add(getStyleClassName());
    }

    private void initializeParts() {
        display = new Text(getText());
        display.getStyleClass().add("display");
        applyCss(display);
        display.setTextOrigin(VPos.CENTER);
        display.setTextAlignment(TextAlignment.CENTER);
        display.setY(PREFERRED_HEIGHT * 0.5);


        arc1 = new Arc(PREFERRED_HEIGHT * 0.33, PREFERRED_HEIGHT * 0.33, PREFERRED_HEIGHT * 0.33, PREFERRED_HEIGHT * 0.33, 90, -90);
        arc1back = new Arc(PREFERRED_HEIGHT * 0.33, PREFERRED_HEIGHT * 0.33, PREFERRED_HEIGHT * 0.33, PREFERRED_HEIGHT * 0.33, -90, 360);

        arc2 = new Arc(PREFERRED_HEIGHT * 0.38, PREFERRED_HEIGHT * 0.38, PREFERRED_HEIGHT * 0.38, PREFERRED_HEIGHT * 0.38, 90, -230);
        arc2back = new Arc(PREFERRED_HEIGHT * 0.38, PREFERRED_HEIGHT * 0.38, PREFERRED_HEIGHT * 0.38, PREFERRED_HEIGHT * 0.38, 90, 360);
        arc3 = new Arc(PREFERRED_HEIGHT * 0.25, PREFERRED_HEIGHT * 0.25, PREFERRED_HEIGHT * 0.25, PREFERRED_HEIGHT * 0.25, 360, 130);

        arc1.setCenterX(PREFERRED_HEIGHT * 0.5);
        arc1.setCenterY(PREFERRED_HEIGHT * 0.5);
        arc1back.setCenterX(PREFERRED_HEIGHT * 0.5);
        arc1back.setCenterY(PREFERRED_HEIGHT * 0.5);
        arc2.setCenterX(PREFERRED_HEIGHT * 0.5);
        arc2.setCenterY(PREFERRED_HEIGHT * 0.5);
        arc2back.setCenterX(PREFERRED_HEIGHT * 0.5);
        arc2back.setCenterY(PREFERRED_HEIGHT * 0.5);
        arc3.setCenterX(PREFERRED_HEIGHT * 0.5);
        arc3.setCenterY(PREFERRED_HEIGHT * 0.5);

        arc1.getStyleClass().add("arc1");
        arc1back.getStyleClass().add("arc1back");
        arc2back.getStyleClass().add("arc2back");
        arc2.getStyleClass().add("arc2");
        arc3.getStyleClass().add("arc3");


        thumb = new Circle(12, 13, 10);
        thumb.setCenterX(PREFERRED_HEIGHT * 0.5-15);
        thumb.setCenterY(PREFERRED_HEIGHT * 0.67);

        thumb.getStyleClass().add("thumb");
        thumb.setStrokeWidth(0);
        thumb.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.3), 4, 0, 0, 1));

        frame = new Rectangle(PREFERRED_HEIGHT * 0.5-15, PREFERRED_HEIGHT * 0.67 - 6, SWITCH_PREFERRED_WIDTH - 4, SWITCH_PREFERRED_HEIGHT - 12);
        frame.getStyleClass().add("frame");

        marks = tickMarks();

        Platform.runLater(this::relocateDisplay);

        // always needed
        drawingPane = new Pane();
        drawingPane.setMaxSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        drawingPane.setMinSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
        drawingPane.setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
    }

    private void layoutParts() {
        drawingPane.getChildren().addAll( arc1back, arc2back, arc1, arc2, display, marks, frame, thumb);
        getChildren().add(drawingPane);
    }

    private void initializeAnimations() {
        Duration duration = Duration.millis(200);

        TranslateTransition onTransition = new TranslateTransition(duration, thumb);
        onTransition.setFromX(30.0);
        onTransition.setToX(2.0);

        FillTransition onFillThumb = new FillTransition(duration, thumb, THUMB_OFF, THUMB_ON);
        FillTransition onFillFrame = new FillTransition(duration, frame, FRAME_OFF, FRAME_ON);

        onAnimation = new ParallelTransition(onTransition, onFillThumb, onFillFrame);

        TranslateTransition offTransition = new TranslateTransition(duration, thumb);
        offTransition.setFromX(2);
        offTransition.setToX(30.0);

        FillTransition offFillThumb = new FillTransition(duration, thumb, THUMB_ON, THUMB_OFF);
        FillTransition offFillFrame = new FillTransition(duration, frame, FRAME_ON, FRAME_OFF);

        offAnimation = new ParallelTransition(offTransition, offFillThumb, offFillFrame);
    }

    private void addEventHandlers() {
        arc1.setOnMouseDragged(event -> {
            arc1.setLength(Math.min(360.0, -event.getY()));

        });
        arc2.setOnMouseDragged(event -> {
            arc2.setLength(Math.min(360.0, -event.getY()));

        });
        arc3.setOnMouseDragged(event -> {
            arc3.setLength(Math.min(360.0, event.getY()));

        });


        thumb.setOnMouseClicked(event -> {
            setOn(!getOn());
        });
    }

    public String getTime(){
        return display.getText();
    }
    private void addValueChangedListeners() {
        textProperty().addListener((observable, oldValue, newValue) -> {
            display.setText(newValue);
            String[] values = display.getText().split(":");
            relocateDisplay();
        });

        arc1.lengthProperty().addListener((observable, oldValue, newValue) -> setText(String.format("%2.0f", -1 * (arc2.lengthProperty().getValue() / 30 +0.5) % 11) + ":" + String.format("%2.0f", -1 * ((newValue.doubleValue() / 6) ) % 59)));
        arc2.lengthProperty().addListener((observable, oldValue, newValue) -> setText(String.format("%2.0f", -1 * (newValue.doubleValue() / 30 +0.5) % 11) + ":" + String.format("%2.0f", -1 * (arc1.lengthProperty().getValue() / 6 ) % 59)));

        onProperty().addListener((observable, oldValue, newValue) -> {
            onAnimation.stop();
            offAnimation.stop();
            if (newValue) {
                onAnimation.play();
            } else {
                offAnimation.play();
            }
        });

        // always needed
        widthProperty().addListener((observable, oldValue, newValue) -> resize());
        heightProperty().addListener((observable, oldValue, newValue) -> resize());
    }

    private void addBindings() {


    }

    private void resize() {
        Insets padding = getPadding();
        double availableWidth = getWidth() - padding.getLeft() - padding.getRight();
        double availableHeight = getHeight() - padding.getTop() - padding.getBottom();

        double width = Math.max(Math.min(Math.min(availableWidth, availableHeight * ASPECT_RATIO), MAXIMUM_WIDTH), MINIMUM_WIDTH);

        double scalingFactor = width / PREFERRED_WIDTH;

        if (availableWidth > 0 && availableHeight > 0) {
            drawingPane.relocate((getWidth() - PREFERRED_WIDTH) * 0.5, (getHeight() - PREFERRED_HEIGHT) * 0.5);
            drawingPane.setScaleX(scalingFactor);
            drawingPane.setScaleY(scalingFactor);
        }
    }

    private void relocateDisplay() {
        display.setX((PREFERRED_WIDTH - display.getLayoutBounds().getWidth()) * 0.5);
    }

    // some useful helper-methods

    private void applyCss(Node node) {
        Group group = new Group(node);
        group.getStyleClass().add(getStyleClassName());
        addStyleSheets(group);
        new Scene(group);
        node.applyCss();
    }

    private void addStyleSheets(Parent parent) {
        String fonts = getClass().getResource(FONTS_CSS).toExternalForm();
        parent.getStylesheets().add(fonts);

        String stylesheet = getClass().getResource(STYLE_CSS).toExternalForm();
        parent.getStylesheets().add(stylesheet);
    }

    private String getStyleClassName() {
        String className = this.getClass().getSimpleName();

        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }

    private Node tickMarks() {
        Group tickMarkGroup = new Group();
        for (int n = 0; n < 12; n++) {
            tickMarkGroup.getChildren().add(tickMark(n));
        }
        return tickMarkGroup;
    }
    private Node tickMark(int n) {
        double unit = PREFERRED_HEIGHT * 0.5;
        return LineBuilder.create()
                .startX(unit)
                .endX(unit)
                .startY(unit * 0.12)
                .endY(unit * (n %  3== 0 ? 0.2 : 0.1))
                .strokeWidth(n % 3 == 0 ? 3 : 1)
                .stroke(Color.DIMGRAY)
                .transforms(
                        RotateBuilder.create()
                                .pivotX(unit)
                                .pivotY(unit)
                                .angle(360 / 12 * n)
                                .build()
                )
                .strokeWidth(2)
                .build();
    }


    // compute sizes

    @Override
    protected double computeMinWidth(double height) {
        Insets padding = getPadding();
        double horizontalPadding = padding.getLeft() + padding.getRight();

        return MINIMUM_WIDTH + horizontalPadding;
    }

    @Override
    protected double computeMinHeight(double width) {
        Insets padding = getPadding();
        double verticalPadding = padding.getTop() + padding.getBottom();

        return MINIMUM_HEIGHT + verticalPadding;
    }

    @Override
    protected double computePrefWidth(double height) {
        Insets padding = getPadding();
        double horizontalPadding = padding.getLeft() + padding.getRight();

        return PREFERRED_WIDTH + horizontalPadding;
    }

    @Override
    protected double computePrefHeight(double width) {
        Insets padding = getPadding();
        double verticalPadding = padding.getTop() + padding.getBottom();

        return PREFERRED_HEIGHT + verticalPadding;
    }


    // getter and setter for all properties

    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public boolean getOn() {
        return on.get();
    }

    public BooleanProperty onProperty() {
        return on;
    }

    public void setOn(boolean on) {
        this.on.set(on);
    }
}



