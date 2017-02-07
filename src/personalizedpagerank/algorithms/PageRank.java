/*
 * (C) Copyright 2016-2017, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 * 
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * NOTE: this is a modified version of the PageRank.java file from JGraphT library, the
 * purpose of this modification is to allow custom starting scores and custom
 * teleporation chance for all vertixes; code regarding weighted edges will be removed.
 * Some comments from the original authors might be removed, while I might
 * add some new comments to help myself in understanding the code.
 */
package personalizedpagerank.algorithms;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;

/**
 ** 
 * <p>
 * This is a simple iterative implementation of PageRank which stops after a given number of
 * iterations or if the PageRank values between two iterations do not change more than a predefined
 * value. The implementation uses the variant which divides by the number of nodes, thus forming a
 * probability distribution over graph nodes.
 * </p>
 *
 * <p>
 * Each iteration of the algorithm runs in linear time O(n+m) when n is the number of nodes and m
 * the number of edges of the graph. The maximum number of iterations can be adjusted by the caller.
 * The default value is {@link PageRank#MAX_ITERATIONS_DEFAULT}.
 * </p>
 * 
 * <p>
 * If the graph is a weighted graph, a weighted variant is used where the probability of following
 * an edge e out of node v is equal to the weight of e over the sum of weights of all outgoing edges
 * of v.
 * </p>
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 * @since August 2016
 */
public final class PageRank<V, E>
    implements VertexScoringAlgorithm<V, Double>
{
    /**
     * Default number of maximum iterations.
     */
    public static final int MAX_ITERATIONS_DEFAULT = 100;

    /**
     * Default value for the tolerance. The calculation will stop if the difference of PageRank
     * values between iterations change less than this value.
     */
    public static final double TOLERANCE_DEFAULT = 0.0001;

    /**
     * Damping factor default value.
     */
    public static final double DAMPING_FACTOR_DEFAULT = 0.85d;

    private final Graph<V, E> g;
    private Map<V, Double> scores;

    /**
     * Create and execute an instance of PageRank.
     * 
     * @param g the input graph
     */
    public PageRank(Graph<V, E> g)
    {
        this(g, DAMPING_FACTOR_DEFAULT, MAX_ITERATIONS_DEFAULT, TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of PageRank.
     * 
     * @param g the input graph
     * @param dampingFactor the damping factor
     */
    public PageRank(Graph<V, E> g, double dampingFactor)
    {
        this(g, dampingFactor, MAX_ITERATIONS_DEFAULT, TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of PageRank.
     * 
     * @param g the input graph
     * @param dampingFactor the damping factor
     * @param maxIterations the maximum number of iterations to perform
     */
    public PageRank(Graph<V, E> g, double dampingFactor, int maxIterations)
    {
        this(g, dampingFactor, maxIterations, TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of PageRank.
     * 
     * @param g the input graph
     * @param dampingFactor the damping factor
     * @param maxIterations the maximum number of iterations to perform
     * @param tolerance the calculation will stop if the difference of PageRank values between
     *        iterations change less than this value
     */
    public PageRank(Graph<V, E> g, double dampingFactor, int maxIterations, double tolerance)
    {
        this.g = g;
        this.scores = new HashMap<>();

        if (maxIterations <= 0) {
            throw new IllegalArgumentException("Maximum iterations must be positive");
        }

        if (dampingFactor < 0.0 || dampingFactor > 1.0) {
            throw new IllegalArgumentException("Damping factor not valid");
        }

        if (tolerance <= 0.0) {
            throw new IllegalArgumentException("Tolerance not valid, must be positive");
        }

        run(dampingFactor, maxIterations, tolerance);
    }
    
    /**
     * Create and execute an instance of personalized pagerank starting from the
     * origin.
     * 
     * @param g the input graph
     * @param dampingFactor the damping factor
     * @param maxIterations the maximum number of iterations to perform
     * @param tolerance the calculation will stop if the difference of PageRank values between
     *        iterations change less than this value
     * @param origin the node for wich to run personalized pagerank
     */
    public PageRank(Graph<V, E> g, double dampingFactor, int maxIterations, double tolerance, V origin)
    {
        this.g = g;
        this.scores = new HashMap<>();

        if (maxIterations <= 0) 
        {
            throw new IllegalArgumentException("Maximum iterations must be positive");
        }

        if (dampingFactor < 0.0 || dampingFactor > 1.0) 
        {
            throw new IllegalArgumentException("Damping factor not valid");
        }

        if (tolerance <= 0.0) 
        {
            throw new IllegalArgumentException("Tolerance not valid, must be positive");
        }
        
        if(!g.containsVertex(origin))
        {
            throw new IllegalArgumentException("origin vertex must be part of the grap");
        }

        run(dampingFactor, maxIterations, tolerance, origin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<V, Double> getScores()
    {
        return Collections.unmodifiableMap(scores);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getVertexScore(V v)
    {
        if (!g.containsVertex(v)) {
            throw new IllegalArgumentException("Cannot return score of unknown vertex");
        }
        return scores.get(v);
    }

    private void run(double dampingFactor, int maxIterations, double tolerance)
    {
        // initialization
        Specifics specifics;
        if (g instanceof DirectedGraph<?, ?>) {
            specifics = new DirectedSpecifics((DirectedGraph<V, E>) g);
        } else {
            specifics = new UndirectedSpecifics(g);
        }
        
        int totalVertices = g.vertexSet().size();
        Map<V, Double> weights = Collections.emptyMap();

        /*init every vertex with equal score*/
        double initScore = 1.0d / totalVertices;
        for (V v : g.vertexSet()) 
            scores.put(v, initScore);
            

        // run PageRank
        Map<V, Double> nextScores = new HashMap<>();
        double maxChange = tolerance;

        while (maxIterations > 0 && maxChange >= tolerance) 
        {
            /*compute score from teleport*/
            double r = 0d;
            for (V v : g.vertexSet()) 
            {
                /*
                if there are outgoing edges send to teleport only a fraction of the score
                if no outgoing edges the entire score is sent to teleport
                */
                if (specifics.outgoingEdgesOf(v).size() > 0)
                {
                    r += (1d - dampingFactor) * scores.get(v);
                }
                else 
                {
                    r += scores.get(v);
                }
            }
            /*score from teleport will be distributed equally to all vertices*/
            r /= totalVertices;

            maxChange = 0d;
            for (V v : g.vertexSet()) 
            {
                double contribution = 0d;
                /* 
                for every incoming edge accumulate pagerank from the parent
                */
                for (E e : specifics.incomingEdgesOf(v)) 
                {
                    V w = Graphs.getOppositeVertex(g, e, v);
                    contribution +=
                        dampingFactor * scores.get(w) / specifics.outgoingEdgesOf(w).size();
                }
                
                double vOldValue = scores.get(v);
                //new score := contribution from incoming links + contribution from teleport
                double vNewValue = contribution + r;
                maxChange = Math.max(maxChange, Math.abs(vNewValue - vOldValue));
                nextScores.put(v, vNewValue);
            }

            // swap scores
            Map<V, Double> tmp = scores;
            scores = nextScores;
            nextScores = tmp;

            // progress
            maxIterations--;
        }
    }

    private void run(double dampingFactor, int maxIterations, double tolerance, V origin)
    {
        // initialization
        Specifics specifics;
        if (g instanceof DirectedGraph<?, ?>) {
            specifics = new DirectedSpecifics((DirectedGraph<V, E>) g);
        } else {
            specifics = new UndirectedSpecifics(g);
        }
        
        Map<V, Double> weights = Collections.emptyMap();

        //init every non origin score to 0 and origin to 1
        for (V v : g.vertexSet()) 
            scores.put(v, 0d);
        scores.put(origin, 1d);
            

        // run PageRank
        Map<V, Double> nextScores = new HashMap<>();
        double maxChange = tolerance;
        
        while (maxIterations > 0 && maxChange >= tolerance) 
        {
            //compute score to add to origin from teleport
            double teleportContribution = 0d;
            for (V v : g.vertexSet()) 
            {
                /*
                if there are outgoing edges send to teleport only a fraction of the score
                if no outgoing edges the entire score is sent to teleport
                */
                if (specifics.outgoingEdgesOf(v).size() > 0)
                {
                    teleportContribution += (1d - dampingFactor) * scores.get(v);
                }
                else 
                {
                    teleportContribution += scores.get(v);
                }
            }

            //compute score for every node
            maxChange = 0d;
            for (V v : g.vertexSet()) 
            {
                /*
                prolem: the current vNewValue for the origin must be updated to have
                a proper maxChange check
                
                )we could either ignore this and have a
                more inaccurate maxChange check and a faster loop by adding the
                teleportContribution to origin after this for cycle
                
                )or have another map  wich is initialized only once where only the value
                paired with the origin node is updated while the rest is always 0
                
                )will need profiling and more ideas, low priority since personalized pagerank
                for each node isn't what we are looking after
                */
                double vNewValue = v.equals(origin)? teleportContribution : 0d;
                //for every incoming edge accumulate pagerank from the parent
                for (E e : specifics.incomingEdgesOf(v)) 
                {
                    V w = Graphs.getOppositeVertex(g, e, v);
                    vNewValue +=
                        dampingFactor * scores.get(w) / specifics.outgoingEdgesOf(w).size();
                }
                
                maxChange = Math.max(maxChange, Math.abs(vNewValue - scores.get(v)));
                nextScores.put(v, vNewValue);
            }
            
            //swap scores
            Map<V, Double> tmp = scores;
            scores = nextScores;
            nextScores = tmp;

            // progress
            maxIterations--;
        }
    }
    
    abstract class Specifics
    {
        public abstract Set<? extends E> outgoingEdgesOf(V vertex);

        public abstract Set<? extends E> incomingEdgesOf(V vertex);
    }

    class DirectedSpecifics
        extends Specifics
    {
        private DirectedGraph<V, E> graph;

        public DirectedSpecifics(DirectedGraph<V, E> g)
        {
            graph = g;
        }

        @Override
        public Set<? extends E> outgoingEdgesOf(V vertex)
        {
            return graph.outgoingEdgesOf(vertex);
        }

        @Override
        public Set<? extends E> incomingEdgesOf(V vertex)
        {
            return graph.incomingEdgesOf(vertex);
        }
    }

    class UndirectedSpecifics
        extends Specifics
    {
        private Graph<V, E> graph;

        public UndirectedSpecifics(Graph<V, E> g)
        {
            graph = g;
        }

        @Override
        public Set<E> outgoingEdgesOf(V vertex)
        {
            return graph.edgesOf(vertex);
        }

        @Override
        public Set<E> incomingEdgesOf(V vertex)
        {
            return graph.edgesOf(vertex);
        }
    }

}