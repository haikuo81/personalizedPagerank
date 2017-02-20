/*
 * Base class for parameters of the algorithms.
 */
package personalizedpagerank.Utility;

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

    //generated automatically
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Parameters other = (Parameters) obj;
        if (this.edges != other.edges) {
            return false;
        }
        if (this.vertices != other.vertices) {
            return false;
        }
        if (this.iterations != other.iterations) {
            return false;
        }
        if (Double.doubleToLongBits(this.damping) != Double.doubleToLongBits(other.damping)) {
            return false;
        }
        if (Double.doubleToLongBits(this.tolerance) != Double.doubleToLongBits(other.tolerance)) {
            return false;
        }
        return true;
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
