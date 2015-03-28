package nl.utwente.group10.ui.components;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;

import java.io.IOException;
import java.util.ArrayList;

import javafx.scene.control.Label;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.gestures.CustomGesture;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * Main building block for the visual interface, this class
 * represents a Haskell function together with it's arguments and
 * visual representation.
 */
public class FunctionBlock extends Block {
	/** The inputs for this FunctionBlock.**/
	private ConnectionAnchor[] inputs;

	/** The types of the input for this FunctionBlock. **/
	private Label[] labels;
	
	/** The name of this Function. **/
	private StringProperty name;

	/** The type of this Function. **/
	private StringProperty type;

	/** The arguments of this Function. */
	private ArrayList<String> args;

	/** intstance to create Events for this FunctionBlock. **/
	private static CustomGesture cg;

	@FXML private Pane nestSpace;
	
	@FXML private Pane anchorSpace;
	
	@FXML private Pane outputSpace;
	
	@FXML private Pane argumentSpace;

	/**
	 * Method that creates a newInstance of this class along with it's visual representation
	 *
	 * @param name The name of the function.
	 * @param type The function's type (usually a FuncT).
	 * @param pane The CustomUIPane in which this FunctionBlock exists. Via this this FunctionBlock knows which other FunctionBlocks exist.  @return a new instance of this class
	 * @throws IOException
	 */
	public FunctionBlock(String name, Type type, CustomUIPane pane) throws IOException {
		super("FunctionBlock", pane);
		
		this.name = new SimpleStringProperty(name);
		this.type = new SimpleStringProperty(type.toHaskellType());
		this.args = new ArrayList<>();

		cg = new CustomGesture(this, this);
		
		this.getLoader().load();
		
		// Collect argument types
		Type t = type;
		while (t instanceof FuncT) {
			FuncT ft = (FuncT) t;
			args.add(ft.getArgs()[0].toHaskellType());
			t = ft.getArgs()[1];
		}

		this.inputs = new ConnectionAnchor[args.size()];
		this.labels = new Label[args.size()];

		// Create anchors and labels for each argument
		for (int i = 0; i < args.size(); i++) {
			inputs[i] = new ConnectionAnchor();
			anchorSpace.getChildren().add(inputs[i]);

			argumentSpace.getChildren().add(new Label(args.get(i)));
		}

		// Create an anchor and label for the result
		Label lbl = new Label(t.toHaskellType());
		lbl.getStyleClass().add("result");
		argumentSpace.getChildren().add(lbl);
		outputSpace.getChildren().add(this.getOutputAnchor());
	}
	
	/**
	 * Executes this FunctionBlock and returns the output as a String
	 * @return Output of the Function
	 */
	public String executeMethod() {		
		return new String("DEBUG-OUTPUT");
	}
	
	/**
	 * Nest another Node object within this FunctionBlock
	 * @param node to nest
	 */
	public void nest(Node node) {
		name.set("Higher order function");
		nestSpace.getChildren().add(node);
	}

	/**
	 * Get the name property of this FunctionBlock.
	 * @return name
	 */
	public String getName() {
		return name.get();
	}
	
	/**
	 * @param name for this FunctionBlock
	 */
	public void setName(String name) {
		this.name.set(name);
	}

	/**
	 * Get the type property of this FunctionBlock.
	 * @return type
	 */
	public String getType() {
		return type.get();
	}

	/**
	 * @param type the type property of this FunctionBlock.
	 */
	public void setType(String type) {
		this.type.set(type);
	}
	
	/**
	 * the StringProperty for the name of this FunctionBlock.
	 * @return name
	 */
	public StringProperty nameProperty() {
		return name;
	}
	
	/**
	 * the StringProperty for the type of this FunctionBlock.
	 * @return type
	 */
	public StringProperty typeProperty() {
		return type;
	}
	
	/**
	 * Method to fetch an array containing all of the input anchors for this
	 * FunctionBlock
	 * @return inputAnchors
	 */
	public ConnectionAnchor[] getInputs(){
		return inputs;
	}
	
	/**
	 * Returns the index of the argument matched to the Anchor.
	 * @param anchor
	 * @return argumentIndex
	 */
	public int getArgumentIndex(ConnectionAnchor anchor) {
		int index=0;
		/**
		 * @invariant index < inputs.length
		 */
		while((inputs[index]!=anchor)&&(index<inputs.length)) {
			index++;
		}
		return index;
	}
}
