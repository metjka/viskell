package nl.utwente.group10.ui.components.anchors;

import java.util.Optional;

import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.ui.CustomUIPane;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.components.blocks.OutputBlock;
import nl.utwente.group10.ui.components.lines.Connection;
import nl.utwente.group10.ui.exceptions.TypeUnavailableException;
import nl.utwente.group10.ui.handlers.AnchorHandler;

/**
 * Anchor that specifically functions as an output.
 */
public class OutputAnchor extends ConnectionAnchor {
    /**
     * @param block
     *            The block this Anchor is connected to.
     * @param pane
     *            The parent pane on which this anchor resides.
     */
    public OutputAnchor(Block block, CustomUIPane pane) {
        super(block, pane);
        new AnchorHandler(pane.getConnectionCreationManager(), this);
    }

    /**
     * Creates a {@link Connection} to another anchor from this one and returns
     * the connection.
     *
     * @param other
     *            The other anchor to establish a connection to.
     * @return {@link Optional#empty()}
     */
    public Connection createConnectionTo(InputAnchor other) {
        new Connection(getPane(), this, other);
        getPane().getChildren().add(getConnection().get());
        getPane().invalidate();
        return getConnection().get();
    }

    @Override
    public void removeConnection(Connection connection) {
        // Currently does not keep track of its connections.
    }

    @Override
    public boolean canConnect() {
        // OutputAnchors can have multiple connections;
        return true;
    }

    @Override
    public Optional<Connection> getConnection() {
        // Does not keep track of its connections.
        return Optional.empty();
    }

    @Override
    public Type getType() {
        if (getBlock() instanceof OutputBlock) {
            return ((OutputBlock) getBlock()).getOutputType();
        } else {
            throw new TypeUnavailableException();
        }
    }
}