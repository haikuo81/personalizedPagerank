/*
 * Base class for parameters of the algorithms.
 */
package personalizedpagerank;

public class Parameters 
{
    private final int edges;
    private final int vertices;
    private final int iterations; 
    private final double damping;
    private final double tolerance;
    
    public Parameters(final int vertices, final int edges, final int iterations,
            final double damping, final double tolerance)
    {
        this.edges = edges;
        this.vertices = vertices;
        this.iterations = iterations;
        this.damping = damping;
        this.tolerance = tolerance;
    }

    public int getIterations() {
        return iterations;
    }

    public double getDamping() {
        return damping;
    }

    public double getTolerance() {
        return tolerance;
    }

    public int getEdges() {
        return edges;
    }

    public int getVertices() {
        return vertices;
    }
    
}
