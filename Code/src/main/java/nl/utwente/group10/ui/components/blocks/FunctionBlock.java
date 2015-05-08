package nl.utwente.group10.ui.components.blocks;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import nl.utwente.group10.haskell.env.Env;
import nl.utwente.group10.haskell.exceptions.HaskellException;
import nl.utwente.group10.haskell.expr.Apply;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.expr.Ident;
import nl.utwente.group10.haskell.type.FuncT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.BackendUtils;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.anchors.ConnectionAnchor;
import nl.utwente.group10.ui.components.anchors.InputAnchor;
import nl.utwente.group10.ui.components.anchors.OutputAnchor;
import nl.utwente.group10.ui.exceptions.FunctionDefinitionException;
import nl.utwente.group10.ui.exceptions.TypeUnavailableException;

/**
 * Main building block for the visual interface, this class represents a Haskell
 * function together with it's arguments and visual representation.
 */
public class FunctionBlock extends Block implements InputBlock, OutputBlock, ChangeListener<Number> {
    /** The inputs for this FunctionBlock. **/
    private List<InputAnchor> inputs;

    /**
     * The index of the bowtie, all inputs with index higher or equal to the
     * bowtie are be inactive.
     */
    private IntegerProperty bowtieIndex;

    private OutputAnchor output;

    /** The function name. **/
    private StringProperty name;

    /** The type of this Function. **/
    private StringProperty type;

    /** The space containing the input anchor(s). */
    @FXML
    private Pane anchorSpace;

    /** The space containing the output anchor. */
    @FXML
    private Pane outputSpace;

    /** The space containing all the argument fields of the function. */
    @FXML
    private Pane argumentSpace;

    @FXML private Pane inputTypesSpace;

    @FXML private Pane outputTypesSpace;

    /**
     * Method that creates a newInstance of this class along with it's visual
     * representation
     *
     * @param name
     *            The name of the function.
     * @param type
     *            The function's type (usually a FuncT).
     * @param pane
     *            The parent pane in which this FunctionBlock exists.
     */
    public FunctionBlock(String name, Type type, CustomUIPane pane) {
        super(pane);

        this.name = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type.toHaskellType());
        this.bowtieIndex = new SimpleIntegerProperty();
        bowtieIndex.addListener(this);

        this.loadFXML("FunctionBlock");

        // Collect argument types
        ArrayList<String> args = new ArrayList<>();
        Type t = type;
        while (t instanceof FuncT) {
            FuncT ft = (FuncT) t;
            args.add(ft.getArgs()[0].toHaskellType());
            t = ft.getArgs()[1];
        }

        this.inputs = new ArrayList<InputAnchor>();

        // Create anchors and labels for each argument
        for (int i = 0; i < args.size(); i++) {
            inputs.add(new InputAnchor(this, pane));
            anchorSpace.getChildren().add(inputs.get(i));

            argumentSpace.getChildren().add(new Label(args.get(i)));
        }
        setBowtieIndex(getAllInputs().size());

        // Create an anchor and label for the result
        Label lbl = new Label(t.toHaskellType());
        lbl.getStyleClass().add("result");
        argumentSpace.getChildren().add(lbl);
        output = new OutputAnchor(this, pane);
        outputSpace.getChildren().add(output);
    }

    /**
     * Nest another {@link Node} object within this FunctionBlock
     *
     * @param node
     *            The node to nest.
     */
    public final void nest(Node node) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param name
     *            The name of this FunctionBlock
     */
    public final void setName(String name) {
        this.name.set(name);
    }

    /**
     * @param type
     *            The new Haskell type for this FunctionBlock.
     */
    public final void setType(String type) {
        this.type.set(type);
    }

    /**
     * @param index
     *            The new bowtie index for this FunctionBlock.
     */
    public final void setBowtieIndex(int index) {
        if (index >= 0 && index <= getAllInputs().size()) {
            this.bowtieIndex.set(index);
        }
    }

    /**
     * Returns the index of the argument matched to the Anchor.
     *
     * @param anchor
     *            The anchor to look up.
     * @return The index of the given Anchor in the input anchor array.
     */
    @Override
    public final int getInputIndex(InputAnchor anchor) {
        return inputs.indexOf(anchor);
    }

    /** Returns the array of input anchors for this function block. */
    public final List<InputAnchor> getAllInputs() {
        return inputs;
    }
    
    /**
     * @return Only the active (as specified with the bowtie) inputs.
     */
    @Override
    public List<InputAnchor> getActiveInputs() {
        return inputs.subList(0, getBowtieIndex());
    }

    /** Returns the name property of this FunctionBlock. */
    public final String getName() {
        return name.get();
    }

    /** Returns the Haskell type of this FunctionBlock. */
    public final String getType() {
        return type.get();
    }

    /** Returns the bowtie index of this FunctionBlock. */
    public final Integer getBowtieIndex() {
        return bowtieIndex.get();
    }

    /** Returns the StringProperty for the name of the function. */
    public final StringProperty nameProperty() {
        return name;
    }

    /** Returns the StringProperty for the type of the function. */
    public final StringProperty typeProperty() {
        return type;
    }

    /** Returns the IntegerPorperty for the bowtie index of this FunctionBlock. */
    public final IntegerProperty bowtieIndexProperty() {
        return bowtieIndex;
    }

    @Override
    public boolean inputsAreConnected() {
        return getActiveInputs().stream().allMatch(ConnectionAnchor::isConnected);
    }

    @Override
    public boolean inputIsConnected(int index) {
        return index >= 0 && index < getBowtieIndex() && inputs.get(index).isConnected();
    }

    /**
     * @return The current (output) expression belonging to this block.
     */
    @Override
    public final Expr asExpr() {
        Expr expr = new Ident(getName());
        for (InputAnchor in : getActiveInputs()) {
            expr = new Apply(expr, in.asExpr());
        }

        return expr;
    }

    /**
     * @return The function signature as specified in the catalog.
     */
    public Type getFunctionSignature() {
        return getFunctionSignature(getPane().getEnvInstance());
    }

    public Type getFunctionSignature(Env env) {
        return env.getFreshExprType(this.getName()).get();
    }

    @Override
    public Type getInputSignature(InputAnchor input) {
        return getInputSignature(getInputIndex(input));
    }

    @Override
    public Type getInputSignature(int index) {
        if (index >= 0 && index < inputs.size()) {
            if (getFunctionSignature() instanceof FuncT) {
                return BackendUtils.dive((FuncT) getFunctionSignature(), index + 1);
            } else {
                throw new FunctionDefinitionException();
            }
        } else {
            throw new TypeUnavailableException();
        }
    }

    @Override
    public Type getInputType(InputAnchor input) {
        return getInputType(getInputIndex(input));
    }

    @Override
    public Type getInputType(int index) {
        if (getAllInputs().get(index).isConnected()) {
            return getAllInputs().get(index).getOtherAnchor().get().getType();
        } else {
            return getInputSignature(index);
        }
    }

    @Override
    public Type getOutputSignature() {
        return getOutputSignature(getPane().getEnvInstance());
    }

    @Override
    public Type getOutputSignature(Env env) {
        Type type = getFunctionSignature();
        for (int i = 0; i < getBowtieIndex(); i++) {
            if (type instanceof FuncT) {
                type = ((FuncT) type).getArgs()[1];
            } else {
                throw new FunctionDefinitionException();
            }
        }
        return type;
    }

    @Override
    public Type getOutputType() {
        return getOutputType(getPane().getEnvInstance());
    }

    @Override
    public Type getOutputType(Env env) {
        try {
            return asExpr().analyze(env).prune();
        } catch (HaskellException e) {
            e.printStackTrace();
            return getOutputSignature();
        }
    }

    @Override
    public void invalidate() {
        invalidate(getPane().getEnvInstance());
    }

    public void invalidate(Env env) {
        // TODO not clear and re-add all labels every invalidate()
        invalidateInput();
        invalidateOutput();
    }

    /**
     * Updates the input types to the Block's new state.
     */
    private void invalidateInput() {
        List<Label> labels = new ArrayList<Label>();
        for (int i = 0; i < getBowtieIndex(); i++) {
            labels.add(new Label(getInputType(i).toHaskellType()));
        }
        inputTypesSpace.getChildren().setAll(labels);
    }

    /**
     * Updates the output types to the Block's new state.
     */
    private void invalidateOutput() {
        Label label = new Label(getOutputType().toHaskellType());
        outputTypesSpace.getChildren().setAll(label);
    }

    @Override
    public final void error() {
        for (InputAnchor in : getAllInputs()) {
            if (!in.hasConnection()) {
                argumentSpace.getChildren().get(getInputIndex(in)).getStyleClass().add("error");
            } else if (in.hasConnection()) {
                argumentSpace.getChildren().get(getInputIndex(in)).getStyleClass().remove("error");
            }
        }
        this.getStyleClass().add("error");
    }

    @Override
    public OutputAnchor getOutputAnchor() {
        return output;
    }

    @Override
    public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
        if (arg0.equals(bowtieIndex)) {
            invalidateBowtieIndex();
        }
    }

    private void invalidateBowtieIndex() {
        for (InputAnchor input : getAllInputs()) {
            input.setVisible(getInputIndex(input) < getBowtieIndex());
            if(input.isConnected()){
                input.getConnection().get().disconnect();
            }
        }
    }
}
