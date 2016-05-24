package ch.fhnw.ws4c.circles;

import javafx.animation.Animation;
import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * @author Dieter Holz
 */
public class Switch extends Region {
	private static final String FONTS_CSS = "fonts.css";
	private static final String STYLE_CSS = "style.css";

	//Todo : Redundant zu style.css. Einführen von StyledProperties und custom pseudoClass 'off'
	// wird aber erst später behandelt.
	private static final Color THUMB_ON  = Color.rgb(62, 130, 247);
	private static final Color THUMB_OFF = Color.rgb(250, 250, 250);
	private static final Color FRAME_ON  = Color.rgb(162, 197, 255);
	private static final Color FRAME_OFF = Color.rgb(153, 153, 153);

	private static final double PREFERRED_WIDTH  = 40;
	private static final double PREFERRED_HEIGHT = 26;

	private static final double ASPECT_RATIO = PREFERRED_WIDTH / PREFERRED_HEIGHT;

	private static final double MINIMUM_WIDTH  = 20;
	private static final double MINIMUM_HEIGHT = MINIMUM_WIDTH / ASPECT_RATIO;

	private static final double MAXIMUM_WIDTH = 160;

	// all parts
	private Circle    thumb;
	private Rectangle frame;

	private Pane drawingPane;

	// all properties
	private final BooleanProperty on = new SimpleBooleanProperty();

	// all animations;
	private Animation onAnimation;
	private Animation offAnimation;

	public Switch() {
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
		thumb = new Circle(12, 13, 10);
		thumb.getStyleClass().add("thumb");
		thumb.setStrokeWidth(0);
		thumb.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.3), 4, 0, 0, 1));

		frame = new Rectangle(2.0, 6.0, 10.0, 5.0);
		frame.getStyleClass().add("frame");

	}

	private void layoutParts() {
		drawingPane.getChildren().addAll(frame, thumb);
		getChildren().add(drawingPane);
	}

	private void initializeAnimations() {
		Duration duration = Duration.millis(200);

		TranslateTransition onTransition = new TranslateTransition(duration, thumb);
		onTransition.setFromX(0.0);
		onTransition.setToX(16.0);

		FillTransition onFillThumb = new FillTransition(duration, thumb, THUMB_OFF, THUMB_ON);
		//FillTransition onFillFrame = new FillTransition(duration, frame, FRAME_OFF, FRAME_ON);

		onAnimation = new ParallelTransition(onTransition, onFillThumb/*, onFillFrame*/);

		TranslateTransition offTransition = new TranslateTransition(duration, thumb);
		offTransition.setFromX(16.0);
		offTransition.setToX(0.0);

		FillTransition offFillThumb = new FillTransition(duration, thumb, THUMB_ON, THUMB_OFF);
		//FillTransition offFillFrame = new FillTransition(duration, frame, FRAME_ON, FRAME_OFF);

		offAnimation = new ParallelTransition(offTransition, offFillThumb/*, offFillFrame*/);
	}

	private void addEventHandlers() {
		//frame.setMouseTransparent(true);
		thumb.setMouseTransparent(true);
		drawingPane.setOnMouseClicked(event -> {
			setOn(!getOn());
		});
	}

	private void addValueChangedListeners() {
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
		Insets padding         = getPadding();
		double availableWidth  = getWidth() - padding.getLeft() - padding.getRight();
		double availableHeight = getHeight() - padding.getTop() - padding.getBottom();

		double width = Math.max(Math.min(Math.min(availableWidth, availableHeight * ASPECT_RATIO), MAXIMUM_WIDTH), MINIMUM_WIDTH);

		double scalingFactor = width / PREFERRED_WIDTH;

		if (availableWidth > 0 && availableHeight > 0) {
			drawingPane.relocate((getWidth() - PREFERRED_WIDTH) * 0.5, (getHeight() - PREFERRED_HEIGHT) * 0.5);
			drawingPane.setScaleX(scalingFactor);
			drawingPane.setScaleY(scalingFactor);
		}
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


	// compute sizes

	@Override
	protected double computeMinWidth(double height) {
		Insets padding           = getPadding();
		double horizontalPadding = padding.getLeft() + padding.getRight();

		return MINIMUM_WIDTH + horizontalPadding;
	}

	@Override
	protected double computeMinHeight(double width) {
		Insets padding         = getPadding();
		double verticalPadding = padding.getTop() + padding.getBottom();

		return MINIMUM_HEIGHT + verticalPadding;
	}

	@Override
	protected double computePrefWidth(double height) {
		Insets padding           = getPadding();
		double horizontalPadding = padding.getLeft() + padding.getRight();

		return PREFERRED_WIDTH + horizontalPadding;
	}

	@Override
	protected double computePrefHeight(double width) {
		Insets padding         = getPadding();
		double verticalPadding = padding.getTop() + padding.getBottom();

		return PREFERRED_HEIGHT + verticalPadding;
	}

	// getter and setter for all properties

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
