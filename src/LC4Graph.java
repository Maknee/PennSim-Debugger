/*
 * (C) Copyright 2003-2016, by Barak Naveh and Contributors.
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
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
// resolve ambiguity
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.DirectedMultigraph;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;

/**
 * A demo applet that shows how to use JGraph to visualize JGraphT graphs.
 *
 * @author Meh
 * @since ??
 */

public class LC4Graph
    extends JFrame
{
    private static final long serialVersionUID = 3256444702936019250L;
    private static final Color DEFAULT_BG_COLOR = Color.decode("#FAFBFF");
    private static final Dimension DEFAULT_SIZE = new Dimension(800, 600);

    //JGraph setup
    private JGraphModelAdapter<String, DefaultEdge> jgAdapter;

    /**
     * An alternative starting point for this demo, to also allow running this applet as an
     * application.
     *
     * @param args ignored.
     */

    /**
     * {@inheritDoc}
     */
    
    public void init(Machine mac)
    {
    	// create a JGraphT graph
        ListenableGraph<String, DefaultEdge> g =
            new ListenableDirectedMultigraph<>(DefaultEdge.class);

        // create a visualization using JGraph, via an adapter
        jgAdapter = new JGraphModelAdapter<>(g);

        JGraph jgraph = new JGraph(jgAdapter);
        
        boolean called = false;
        int y = 40;

        //Keep track of all the calls
        HashSet<String> calls = new HashSet<String>();
        String prevVertex = new String();
        
        //Push everything onto the stack, so we can ensure everything is "safe" (registers still might get messed up...)
        mac.getRegisterFile().pushad();
        
        //attempt for 1000 instructions
        for(int i = 0; i < 1000; i++) {       
            try {
            	//get the instruction
            	final Word checkAndFetch = mac.getMemory().checkAndFetch(mac.getRegisterFile().getPC(), mac.getRegisterFile().getPrivMode());
            	//parse the instruction
            	InstructionDef instructionDef = ISA.lookupTable[checkAndFetch.getValue()];
            	
            	//Check if the instruction is a call of branch
            	if(instructionDef.isCall() || instructionDef.isBranch()) {
            		
            		//uh... push on stack?
            		mac.getRegisterFile().pushad();
            		
            		//continue execute...
            		mac.executePumpedContinues(1);
            		
            		//dunno why
            		String functionLabel = mac.getSymTable().lookupAddr(mac.getRegisterFile().getPC());
            		
            		//perserve registers
            		mac.getRegisterFile().popad();
//                    g.addVertex(instructionDef.getFormat());
//
//                    g.addEdge(instructionDef.getFormat(), v2);
//                    g.addEdge(instructionDef.getFormat(), v3);
//                    g.addEdge(instructionDef.getFormat(), v4);
            		
            		
            		String call = ISA.disassemble(checkAndFetch, mac.getRegisterFile().getPC(), mac);
            		
            		called = false;

        			for(String vertex : calls) {
        				if(vertex.equals(call)) {
            				if(!prevVertex.equals("") && !prevVertex.equals(vertex) && !g.containsEdge(prevVertex, vertex))
            					g.addEdge(prevVertex, vertex);
        					called = true;
        				}
        			}
        			if(!called) {
	         			calls.add(call);
	            		g.addVertex(call);
	            		
	        			if(!prevVertex.equals("")) {
	            			g.addEdge(prevVertex, call);
	                        // position vertices nicely within JGraph component
	                        positionVertexAt(call, 130, y);
	                        //System.out.println("PREV VERTEX");
	                        y += 50;
	        			} else {
	        				positionVertexAt(call, 130, y);
	                        //System.out.println("PREV VERTEX");
	                        y += 50;
	        			}
	            		prevVertex = call;
        			}
            	}
         		//String call = ISA.disassemble(checkAndFetch, mac.getRegisterFile().getPC(), mac);
        		//System.out.println(call);
                mac.stopImmediately = false;
    			mac.executePumpedContinues(1);
    		} catch (ExceptionException e) {
    			e.printStackTrace();
    		}
        }
        mac.getRegisterFile().popad();
        
        JGraphFacade facade = new JGraphFacade(jgraph); // Pass the facade the JGraph instance
        JGraphHierarchicalLayout layout = new JGraphHierarchicalLayout(); // Create an instance of the appropriate layout
        layout.setCompactLayout(false);
        layout.setDeterministic(true);
        layout.setFineTuning(true);
        layout.setFixRoots(true);
        layout.setInterHierarchySpacing(200);
        layout.setInterRankCellSpacing(200);
        layout.setIntraCellSpacing(200);
        layout.setParallelEdgeSpacing(200);
        layout.run(facade); // Run the layout on the facade.
        Map nested = facade.createNestedMap(true, true); // Obtain a map of the resulting attribute changes from the facade
        jgraph.getGraphLayoutCache().edit(nested); // Apply the results to the actual graph
        jgraph.getGraphLayoutCache().update();
        jgraph.refresh();
        
        class TestPane extends JPanel {
        	
            public TestPane(JGraph jgraph) {
                setLayout(new BorderLayout());
                jgraph.setAutoscrolls(true);
				add(new JScrollPane(jgraph));

				MouseAdapter ma = new MouseAdapter() {

				    private Point origin;

				    @Override
				    public void mousePressed(MouseEvent e) {
				        origin = new Point(e.getPoint());
				    }

				    @Override
				    public void mouseReleased(MouseEvent e) {
				    }

				    @Override
				    public void mouseDragged(MouseEvent e) {
				        if (origin != null) {
				            JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, jgraph);
				            if (viewPort != null) {
				                int deltaX = origin.x - e.getX();
				                int deltaY = origin.y - e.getY();

				                Rectangle view = viewPort.getViewRect();
				                view.x += deltaX;
				                view.y += deltaY;

				                jgraph.scrollRectToVisible(view);
				            }
				        }
				    }
				    double x = 1;
		            @Override
		            public void mouseWheelMoved(MouseWheelEvent e) {
		            	e.consume();
		            	if(e.getWheelRotation() > 0) {
		            		x /= 1.25;
		            	} else if(e.getWheelRotation() < 0) {
		            		x *= 1.25;
		            	}
		                jgraph.setScale(x, e.getPoint());
		            }

				};

				jgraph.addMouseListener(ma);
				jgraph.addMouseMotionListener(ma);
				jgraph.addMouseWheelListener(ma);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(200, 200);
            }

        };
        
        TestPane graphPane = new TestPane(jgraph);
        this.setLayout(new BorderLayout());
        this.setPreferredSize(DEFAULT_SIZE);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.add(graphPane, BorderLayout.CENTER);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

//    private void adjustDisplaySettings(JGraph jg)
//    {
//        jg.setPreferredSize(DEFAULT_SIZE);
//
//        Color c = DEFAULT_BG_COLOR;
//        String colorStr = null;
//
//        try {
//            colorStr = getParameter("bgcolor");
//        } catch (Exception e) {
//        }
//
//        if (colorStr != null) {
//            c = Color.decode(colorStr);
//        }
//
//        jg.setBackground(c);
//    }
    
    @SuppressWarnings("unchecked") // FIXME hb 28-nov-05: See FIXME below
    private void positionVertexAt(Object vertex, int x, int y)
    {
        DefaultGraphCell cell = jgAdapter.getVertexCell(vertex);
        AttributeMap attr = cell.getAttributes();
        Rectangle2D bounds = GraphConstants.getBounds(attr);

        Rectangle2D newBounds = new Rectangle2D.Double(x, y, bounds.getWidth() * 2, bounds.getHeight());
        
        GraphConstants.setBounds(attr, newBounds);

        // TODO: Clean up generics once JGraph goes generic
        AttributeMap cellAttr = new AttributeMap();
        cellAttr.put(cell, attr);
        jgAdapter.edit(cellAttr, null, null, null);
    }

    /**
     * a listenable directed multigraph that allows loops and parallel edges.
     */
    private static class ListenableDirectedMultigraph<V, E>
        extends DefaultListenableGraph<V, E>
        implements DirectedGraph<V, E>
    {
        private static final long serialVersionUID = 1L;

        ListenableDirectedMultigraph(Class<E> edgeClass)
        {
            super(new DirectedMultigraph<>(edgeClass));
        }
    }
}



// End JGraphAdapterDemo.java
