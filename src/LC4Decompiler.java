import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.security.KeyStore.Entry;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.jgraph.JGraph;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

public class LC4Decompiler extends JFrame{

	ArrayList<mxGraph> graphs = new ArrayList<mxGraph>();
	
	
	//static variables
	Machine mac;
	mxGraph graph;
	Object graphParent;
	
	/**
	 * Components (GUI stuff)
	 */
	
	JSplitPane mainSplitPanel;
	
	//Left panel of the split
	JPanel mainLeftPanel;
	
	//Rightpanel of the split
	//Contains:
	//tabbing
	//disassembly
	JPanel mainRightPanel;
	
	JPanel tabPanel;
	ClosableTabbedPane subroutinePane;
	
	JPanel disassemblyPanel;
	
	//Queue of instructions still needed to be parsed 
	ArrayDeque<QueueBlock> queue = new ArrayDeque<QueueBlock>();
	
	public class QueueBlock
	{
		int pc;
		LC4Block prevBlock;
		Memory memory;
		Stack<Word> callStack;
		
		@SuppressWarnings("unchecked")
		public QueueBlock(int pc, LC4Block prevBlock, Memory memory, Stack<Word> callStack)
		{
			this.pc = pc;
			this.prevBlock = prevBlock;
			
			//make sure these two are copies
			try {
				this.memory = (Memory) memory.clone();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.callStack = (Stack<Word>) callStack.clone();
		}
	}
		
	private static final long serialVersionUID = 1L;
	
	//each pc | instruction is stored as a block
	public class LC4InstructionBlock
	{
		int pc;
		Word instruction;
		String symbolString;
		Object vertex;
		public LC4InstructionBlock(int pc, Word instruction)
		{
			this.pc = pc;
			this.instruction = instruction;
			this.symbolString = mac.getSymTable().lookupAddr(this.pc);
		}
	}
	
	//What type of block is this
	public enum BlockType
	{
		RET,
		JSRR,
		JMP,
		JSR,
		TRAP,
		NOP,
		BRANCH,
		CALCULATIONS
	}
	
	//simulate registers 0-7 and PSR and changes to data memory
	public class Memory implements Cloneable
	{
	    @SuppressWarnings("unchecked")
		@Override
	    public Memory clone() throws CloneNotSupportedException  {
	    	Memory memory = new Memory();
	    	for(int i = 0; i < 8; i++)
	    		memory.registers[i].setValue(this.registers[i].getValue());
	    	memory.PSR.setValue(this.PSR.getValue());
	    	memory.changesDataMemory = (HashMap<Integer, Word>) this.changesDataMemory.clone();
	    	return memory;
	    }
		
		Word registers[];
		Word PSR;
		HashMap<Integer, Word> changesDataMemory;
		
		public Memory()
		{
			this.registers = new Word[8];
			for (int i = 0; i < 8; ++i) {
	            this.registers[i] = new Word();
	        }
			this.PSR = new Word();
			this.changesDataMemory = new HashMap<Integer, Word>();
		}
		
		public void storeNewValue(Integer address, Word value)
		{
			changesDataMemory.put(address, value);
		}
		
		public Word getValueAt(Word address)
		{
			return changesDataMemory.get(address);
		}
		
	    public void setNZP(int n) {
	        final int n2 = this.PSR.getValue() & 0xFFFFFFF8;
	        n &= 0xFFFF;
	        int psr;
	        if ((n & 0x8000) != 0x0) {
	            psr = (n2 | 0x4);
	        }
	        else if (n == 0) {
	            psr = (n2 | 0x2);
	        }
	        else {
	            psr = (n2 | 0x1);
	        }
	        this.PSR.setValue(psr);
	    }
		
	}
	
	
	//a block of instructions
	public class LC4Block
	{
		//prev blocks that jump to this block
		ArrayList<LC4Block> prevBlocks;
		
		//next blocks that this block jumps to
		ArrayList<LC4Block> nextBlocks;
		
		//instructions that this block contains
		ArrayList<LC4InstructionBlock> instructionBlocks;
		
		//drawn object (this is the graphical object of the node)
		Object vertex;
		
		//keep track of whether or not the vertex has been drawn
		public boolean drawn = false;
		
		//keep track of extra jumps
		HashMap<LC4InstructionBlock, LC4InstructionBlock> extraBranches;
		
		//simulate memory
		Memory memory = new Memory();
		
		@Override
		public boolean equals(Object obj)
		{
			if (obj == null) {
				return false;
			}
			if (!LC4Block.class.isAssignableFrom(obj.getClass())) {
				return false;
			}
			final LC4Block otherBlock = (LC4Block)obj;
			if(this.instructionBlocks.size() != otherBlock.instructionBlocks.size()) {
				return false;
			}
			
			for(int i = 0; i < this.instructionBlocks.size(); i++)
			{
				//compare pc's
				if(this.instructionBlocks.get(i).pc != otherBlock.instructionBlocks.get(i).pc)
					return false;
			}
			
			return true;
		}
		
		public LC4Block()
		{
			nextBlocks = new ArrayList<LC4Block>();
			instructionBlocks = new ArrayList<LC4InstructionBlock>();
			extraBranches = new HashMap<LC4InstructionBlock, LC4InstructionBlock>();
		}
		
		public void addNextBlock(LC4Block block)
		{
			nextBlocks.add(block);
		}
		
		public void addInstructionBlock(int pc, Word instruction)
		{
			instructionBlocks.add(new LC4InstructionBlock(pc, instruction));
		}
		
		public void addRemainingBranches(LC4InstructionBlock instruction1, LC4InstructionBlock instruction2)
		{
			extraBranches.put(instruction1, instruction2);
		}
		
		public boolean checkRange(int pc)
		{
			if(pc >= instructionBlocks.get(0).pc && pc <= instructionBlocks.get(instructionBlocks.size() - 1).pc)
				return true;
			return false;
		}
		
		public void drawCells()
		{
			//Spawn the cell in the graph			
			final int cellWidth = 200;
			final int cellHeight = 30;
			
			vertex = graph.insertVertex(graphParent, null, "gggggg", 20, 20, cellWidth, cellHeight * instructionBlocks.size());
			
			//padding for symbol (needs to offset each instruction in the block)
			int symbolPadding = 0;
			
			for(int i = 0; i < instructionBlocks.size(); i++)
			{
				//Get instruction
            	InstructionDef instructionDef = ISA.lookupTable[instructionBlocks.get(i).instruction.getValue()];
				String instructionString = instructionDef.disassemble(instructionBlocks.get(i).instruction, instructionBlocks.get(i).pc, mac);
            	
				//Prints out a symbol if possible, then pads the vertex after it
            	//Create a vertex (prints out the pc and the instruction next to it)
//				Object lc4InstructionBlock;
//				if(instructionBlocks.get(i).symbolString != null)
//				{
//					instructionBlocks.get(i).vertex = graph.insertVertex(vertex, null, instructionBlocks.get(i).symbolString, 0, cellHeight * (i + symbolPadding), cellWidth, cellHeight);
//            		symbolPadding++;
//				}
//				instructionBlocks.get(i).vertex = graph.insertVertex(vertex, null, Word.toHex(instructionBlocks.get(i).pc, true) + " " + instructionString, 0, cellHeight * (i + symbolPadding), cellWidth, cellHeight);
				Object lc4InstructionBlock;
				if(instructionBlocks.get(i).symbolString != null)
				{
					instructionBlocks.get(i).vertex = graph.insertVertex(vertex, null, instructionBlocks.get(i).symbolString, 0, cellHeight * i, cellWidth/3, cellHeight);
				}
				instructionBlocks.get(i).vertex = graph.insertVertex(vertex, null, Word.toHex(instructionBlocks.get(i).pc, true) + " " + instructionString, cellWidth/3, cellHeight * i, 2*cellWidth/2, cellHeight);
			}
		}
		
		public void drawRemainingBranches()
		{
			for(java.util.Map.Entry<LC4InstructionBlock, LC4InstructionBlock> entry : extraBranches.entrySet())
			{
				//Get the last instruction in the current block (will be used in the edge)
				LC4InstructionBlock lastInstructionInBlock = entry.getKey();
				InstructionDef instructionDef = ISA.lookupTable[lastInstructionInBlock.instruction.getValue()];
				String instructionString = instructionDef.disassemble(lastInstructionInBlock.instruction, lastInstructionInBlock.pc, mac);
	
				System.out.println(Word.toHex(lastInstructionInBlock.pc, true) + " " + instructionString + " ---> " + Word.toHex(entry.getValue().pc));
				
				graph.insertEdge(graphParent, null, Word.toHex(lastInstructionInBlock.pc, true) + " " + instructionString , entry.getKey().vertex, entry.getValue().vertex);
			}
		}
	}
	
	public class LC4Analyzer
	{
		//For some reason, getPC returns an int...
		int startPC;
		
		//keep track of all blocks
		ArrayList<LC4Block> blocks;
		
		public LC4Analyzer(int startPC)
		{
			this.startPC = startPC;
			
			AnalyzeInstructions(startPC);
		}
		
		void AnalyzeInstructions(int pc)
		{
			blocks = new ArrayList<LC4Block>();
			
			DecodeInstructionIterative(pc, null, null, null);
			
			//simulate the rest of the branches
			while(!queue.isEmpty())
			{
				QueueBlock queueBlock = queue.remove();
				System.out.println("NEW QUEUE BLOCK " + Word.toHex(queueBlock.pc, true));
				DecodeInstructionIterative(queueBlock.pc, queueBlock.prevBlock, queueBlock.memory, queueBlock.callStack);
			}
			
			LC4Block block = blocks.get(0);
			DrawBlocks(block);
			
		}
		
		boolean ShouldBranch(Word instruction, Word PSR)
		{
			//Check branch instruction's bits and current nzp bits and see if they match
			if((instruction.getZext(11, 9) & PSR.getZext(2, 0)) != 0)
			{
				return true;
			}
			return false;
		}
		
		//Actual draw block call
		void DrawBlocks(LC4Block block)
		{
			if(block != null && !block.drawn)
			{
				block.drawCells();
				block.drawn = true;
				for(LC4Block nextBlock : block.nextBlocks)
				{
					
					//check if the nextBlock has been already drawn (if the function calls the block again)
					if(nextBlock.vertex == null)
					{
						//Draw next block
						DrawBlocks(nextBlock);
					}
					
					//Get the last instruction in the current block (will be used in the edge)
					LC4InstructionBlock lastInstructionInBlock = block.instructionBlocks.get(block.instructionBlocks.size() - 1);
					InstructionDef instructionDef = ISA.lookupTable[lastInstructionInBlock.instruction.getValue()];
					String instructionString = instructionDef.disassemble(lastInstructionInBlock.instruction, lastInstructionInBlock.pc, mac);

					//Link this block to the next block
					graph.insertEdge(graphParent, null, Word.toHex(lastInstructionInBlock.pc, true) + " " + instructionString , block.vertex, nextBlock.vertex);
				}
				//draw remaining branches
				block.drawRemainingBranches();
			}
		}
		
		//helper to decode (iterative version)
		void DecodeInstructionIterative(int pc, LC4Block prevBlock, Memory prevMemory, Stack<Word> inputCallStack)
		{	
			//Create a block
			LC4Block lc4block = new LC4Block();
			
     		//check if the pc exists already. If so, link the prev block to this pc.
			for(LC4Block block : blocks)
    		{
    			for(LC4InstructionBlock instructionBlock : block.instructionBlocks)
    			{
    				//link if the block contains the pc
    				if(pc == instructionBlock.pc)
    				{
    					//set the block
    					lc4block = block;
    					
    					//clear the block's contents
    					lc4block.instructionBlocks.clear();
    					
    					break;
    				}
    			}
    		}
			
			//try to add this block to the prev block (the block that called this block)
			if(prevBlock != null)
				prevBlock.addNextBlock(lc4block);
			
			//call stack for jmpr, jsr, ret
			Stack<Word> callStack;
			if(inputCallStack == null)
			{
				callStack = new Stack<Word>();
				
				//First thing on call stack is 0
				callStack.push(new Word(0));
			}
			else
			{
				callStack = inputCallStack;
			}
			
			
			//Set R7 to 0 before we begin(first time)
			Memory memory;
			if(prevMemory == null)
			{
				memory = new Memory();
			}
			else
			{
				memory = prevMemory;
			}

			
			//loop until a breakpoint
			// or nop (?)
			boolean nopFound = false;
			while(!mac.getMemory().isBreakPointSet(pc) && !nopFound)
			{	
				//if the block has been completed (if the block has a ret, jump, etc)
				boolean endOfBlock = false;
				
				while(!endOfBlock)
				{
					//get instruction
		        	Word instruction = mac.getMemory().checkAndReadNoException(pc);
		        	
	            	//parse the instruction
	            	InstructionDef instructionDef = ISA.lookupTable[instruction.getValue()];
	            	
	            	if(instructionDef == null)
	            	{
	            		endOfBlock = true;
	            		nopFound = true;
	            		break;
	            	}
	            	//check if the instruction is nop
	            	if(instructionDef.isNop())
	            	{
	            		//add this last instruction (nop) to the block
	            		lc4block.addInstructionBlock(pc, instruction);
	            		
	        			//end this block
	            		nopFound = true;
	            		endOfBlock = true;
	            	}
	            	
	            	if(instructionDef.isRet())
	            	{
	            		//add this last instruction (ret) to the block
	            		lc4block.addInstructionBlock(pc, instruction); 
	            		
	             		//check if the pc exists already. If so, link the prev block to this pc.
	        			for(LC4Block block : blocks)
	            		{
	            			for(LC4InstructionBlock instructionBlock : block.instructionBlocks)
	            			{
	            				//link if the block contains the pc
	            				if(callStack.peek().getValue() == instructionBlock.pc)
	            				{
	            					lc4block.addRemainingBranches(lc4block.instructionBlocks.get(lc4block.instructionBlocks.size() - 1), instructionBlock);
	            					nopFound = true;
		            				endOfBlock = true;
	            					break;
	            				}
	            			}
	            		}
	        			
	        			if(endOfBlock)
	        				break;
	        			
	        			//check if the branch is in the current block
	        			for(LC4InstructionBlock instructionBlock : lc4block.instructionBlocks)
            			{
            				//link if the block contains the pc
            				if(callStack.peek().getValue() == instructionBlock.pc)
            				{
            					lc4block.addRemainingBranches(lc4block.instructionBlocks.get(lc4block.instructionBlocks.size() - 1), instructionBlock);
	            				nopFound = true;
	            				endOfBlock = true;
            					break;
            				}
            			}	      
	        			
	        			if(endOfBlock)
	        				break;
	            		
	            		//Add this block to the array of blocks
	        			blocks.add(lc4block);
	            		
	            		//if the block already exists, set it to be the existing block..
	            		for(LC4Block block : blocks)
	            		{
	            			for(LC4InstructionBlock instructionBlock : block.instructionBlocks)
	            			{
	            				//link if the block contains the pc
	            				if(lc4block.checkRange(instructionBlock.pc))
	            				{
	            					lc4block = block;

	            					break;
	            				}
	            			}
	            		}
	        			
	        			//set the new pc
	        			pc = callStack.pop().getValue();
	        			
	        			pc = memory.registers[7].getValue();
	        			
	        			System.out.println("Ret - " + Word.toHex(pc, true));
	        			
	        			//Create new a block
	        			LC4Block newLc4Block = new LC4Block();
	            		
	        			//try to add this block to the prev block (the block that called this block)
	        			lc4block.addNextBlock(newLc4Block);
	            		
	        			//set this to be the next block
	        			lc4block = newLc4Block;
	        			
	            		endOfBlock = true;
	            	}
	            	else if(instructionDef.isJump())
	            	{
	            		//add this last instruction (jmp) to the block
	            		lc4block.addInstructionBlock(pc, instruction);
	            		
	            		//check if the pc exists already. If so, link the prev block to this pc.
	        			for(LC4Block block : blocks)
	            		{
	            			for(LC4InstructionBlock instructionBlock : block.instructionBlocks)
	            			{
	            				//link if the block contains the pc
	            				if(pc + 1 + instructionDef.getPCOffset(instruction) == instructionBlock.pc)
	            				{
	            					lc4block.addRemainingBranches(lc4block.instructionBlocks.get(lc4block.instructionBlocks.size() - 1), instructionBlock);
	            					nopFound = true;
		            				endOfBlock = true;
	            					break;
	            				}
	            			}
	            		}
	        			
	        			if(endOfBlock)
	        				break;
	        			
	        			//check if the branch is in the current block
	        			for(LC4InstructionBlock instructionBlock : lc4block.instructionBlocks)
            			{
            				//link if the block contains the pc
            				if(pc + 1 + instructionDef.getPCOffset(instruction) == instructionBlock.pc)
            				{
            					lc4block.addRemainingBranches(lc4block.instructionBlocks.get(lc4block.instructionBlocks.size() - 1), instructionBlock);
	            				nopFound = true;
	            				endOfBlock = true;
            					break;
            				}
            			}	      
	        			
	        			if(endOfBlock)
	        				break;
	            		
	              		//Add this block to the array of blocks
	        			blocks.add(lc4block);
	            		
	            		//if the block already exists, set it to be the existing block..
	            		for(LC4Block block : blocks)
	            		{
	            			for(LC4InstructionBlock instructionBlock : block.instructionBlocks)
	            			{
	            				//link if the block contains the pc
	            				if(lc4block.checkRange(instructionBlock.pc))
	            				{
	            					lc4block = block;

	            					break;
	            				}
	            			}
	            		}
	        			
	        			//set the new pc
	        			pc = pc + 1 + instructionDef.getPCOffset(instruction);
	        			
	            		//Create new a block
	        			LC4Block newLc4Block = new LC4Block();
	            		
	        			//try to add this block to the prev block (the block that called this block)
	        			lc4block.addNextBlock(newLc4Block);
	            		
	        			//set this to be the next block
	        			lc4block = newLc4Block;
	        			
	            		endOfBlock = true;
	            	}
	            	else if(instructionDef.isJSR())
	            	{	            		
	            		//add this last instruction (jssr) to the block
	            		lc4block.addInstructionBlock(pc, instruction);
	            			            		
	            		//check if the pc exists already. If so, link the prev block to this pc.
	        			for(LC4Block block : blocks)
	            		{
	            			for(LC4InstructionBlock instructionBlock : block.instructionBlocks)
	            			{
	            				//link if the block contains the pc
	            				if(((pc & 0x8000) | instructionDef.getAbsAligned(instruction) << 4) == instructionBlock.pc)
	            				{
	            					lc4block.addRemainingBranches(lc4block.instructionBlocks.get(lc4block.instructionBlocks.size() - 1), instructionBlock);
		            				break;
	            				}
	            			}
	            		}
	        			
	        			if(endOfBlock)
	        				break;
	        			
	        			//check if the branch is in the current block
	        			for(LC4InstructionBlock instructionBlock : lc4block.instructionBlocks)
            			{
            				//link if the block contains the pc
            				if(((pc & 0x8000) | instructionDef.getAbsAligned(instruction) << 4) == instructionBlock.pc)
            				{
            					lc4block.addRemainingBranches(lc4block.instructionBlocks.get(lc4block.instructionBlocks.size() - 1), instructionBlock);
            					break;
            				}
            			}
	        			
	        			if(endOfBlock)
	        				break;
	            		
	            		//push pc + 1 on the stack (return address)
	            		callStack.push(new Word(pc + 1));
	            		            		
	            		//Add this block to the array of blocks
	            		blocks.add(lc4block);

	            		//if the block already exists, set it to be the existing block..
	            		for(LC4Block block : blocks)
	            		{
	            			for(LC4InstructionBlock instructionBlock : block.instructionBlocks)
	            			{
	            				//link if the block contains the pc
	            				if(lc4block.checkRange(instructionBlock.pc))
	            				{
	            					lc4block = block;

	            					break;
	            				}
	            			}
	            		}
	            		
	            		//Create new a block
	            		LC4Block newLc4Block = new LC4Block();

	            		//try to add this block to the prev block (the block that called this block)
	            		lc4block.addNextBlock(newLc4Block);

	            		//set this to be the next block
	            		lc4block = newLc4Block;

	        			//Get the previous pc
	        			int oldpc = pc + 1;
	        			
	        			//set new pc
	        			pc = (pc & 0x8000) | instructionDef.getAbsAligned(instruction) << 4;
	        			
	        			//Set next pc
	        			memory.registers[7].setValue(oldpc);
	        			memory.setNZP(oldpc);
	        			
	            		endOfBlock = true;;
	            	}
	            	else if(instructionDef.isJumpR())
	            	{
	            		//add this last instruction (jmpr) to the block
	            		lc4block.addInstructionBlock(pc, instruction);
	            		
	            		//check if this block already exists
	            		for(LC4Block block : blocks)
	            		{
	            			if(lc4block.equals(block))
	            			{
	            				nopFound = true;
	            				endOfBlock = true;
	            				break;
	            			}
	            		}
	            		
	            		if(endOfBlock)
	            			break;
	            		
	        			//check if the pc exists already. If so, link the prev block to this pc.
	        			for(LC4Block block : blocks)
	            		{
	            			for(LC4InstructionBlock instructionBlock : block.instructionBlocks)
	            			{
	            				//link if the block contains the pc
	            				if(memory.registers[7].getValue() == instructionBlock.pc)
	            				{
	            					lc4block.addRemainingBranches(lc4block.instructionBlocks.get(lc4block.instructionBlocks.size() - 1), instructionBlock);
	            					nopFound = true;
		            				endOfBlock = true;
	            					break;
	            				}
	            			}
	            		}
	        			
	        			if(endOfBlock)
	        				break;
	        			
	        			//check if the branch is in the current block
	        			for(LC4InstructionBlock instructionBlock : lc4block.instructionBlocks)
            			{
            				//link if the block contains the pc
            				if(memory.registers[7].getValue() == instructionBlock.pc)
            				{
            					lc4block.addRemainingBranches(lc4block.instructionBlocks.get(lc4block.instructionBlocks.size() - 1), instructionBlock);
	            				nopFound = true;
	            				endOfBlock = true;
            					break;
            				}
            			}	      
	        			
	        			if(endOfBlock)
	        				break;
	            		            		
	              		//Add this block to the array of blocks
	        			blocks.add(lc4block);
	            		
	            		//if the block already exists, set it to be the existing block..
	            		for(LC4Block block : blocks)
	            		{
	            			for(LC4InstructionBlock instructionBlock : block.instructionBlocks)
	            			{
	            				//link if the block contains the pc
	            				if(lc4block.checkRange(instructionBlock.pc))
	            				{
	            					lc4block = block;

	            					break;
	            				}
	            			}
	            		}
	        			
	        			//set the new pc
	        			pc = memory.registers[instructionDef.getDReg(instruction)].getValue();
	        			
	            		//Create new a block
	        			LC4Block newLc4Block = new LC4Block();
	            		
	        			//try to add this block to the prev block (the block that called this block)
	        			lc4block.addNextBlock(newLc4Block);
	            		
	        			//set this to be the next blzock
	        			lc4block = newLc4Block;
	        			
	            		endOfBlock = true;
	            	}	            	
	            	else if(instructionDef.isJSRR())
	            	{
	            		//add this last instruction (jssr) to the block
	            		lc4block.addInstructionBlock(pc, instruction);
	            		
	            		//check if this block already exists
	            		for(LC4Block block : blocks)
	            		{
	            			if(lc4block.equals(block))
	            			{
	            				nopFound = true;
	            				endOfBlock = true;
	            				break;
	            			}
	            		}
	            		
	            		if(endOfBlock)
	            			break;
	            		
	        			//check if the pc exists already. If so, link the prev block to this pc.
	        			for(LC4Block block : blocks)
	            		{
	            			for(LC4InstructionBlock instructionBlock : block.instructionBlocks)
	            			{
	            				//link if the block contains the pc
	            				if(memory.registers[7].getValue() == instructionBlock.pc)
	            				{
	            					lc4block.addRemainingBranches(lc4block.instructionBlocks.get(lc4block.instructionBlocks.size() - 1), instructionBlock);
	            					nopFound = true;
		            				endOfBlock = true;
	            					break;
	            				}
	            			}
	            		}
	        			
	        			if(endOfBlock)
	        				break;
	        			
	        			//check if the branch is in the current block
	        			for(LC4InstructionBlock instructionBlock : lc4block.instructionBlocks)
            			{
            				//link if the block contains the pc
            				if(memory.registers[7].getValue() == instructionBlock.pc)
            				{
            					lc4block.addRemainingBranches(lc4block.instructionBlocks.get(lc4block.instructionBlocks.size() - 1), instructionBlock);
	            				nopFound = true;
	            				endOfBlock = true;
            					break;
            				}
            			}	      
	        			
	        			if(endOfBlock)
	        				break;

	            		//push pc + 1 on the stack (return address)
	            		callStack.push(new Word(pc + 1));
	            		            		
	              		//Add this block to the array of blocks
	        			blocks.add(lc4block);
	        			
	            		//if the block already exists, set it to be the existing block..
	            		for(LC4Block block : blocks)
	            		{
	            			for(LC4InstructionBlock instructionBlock : block.instructionBlocks)
	            			{
	            				//link if the block contains the pc
	            				if(lc4block.checkRange(instructionBlock.pc))
	            				{
	            					lc4block = block;

	            					break;
	            				}
	            			}
	            		}
	        			
	            		//Create new a block
	        			LC4Block newLc4Block = new LC4Block();
	            		
	        			//try to add this block to the prev block (the block that called this block)
	        			lc4block.addNextBlock(newLc4Block);
	            		
	        			//set this to be the next block
	        			lc4block = newLc4Block;
	        			
	        			//Get the previous pc
	        			int oldpc = pc + 1;
	        			
	        			//set new pc
	        			pc = memory.registers[instructionDef.getDReg(instruction)].getValue();
	        			
	        			//Set next pc
	        			memory.registers[7].setValue(oldpc);
	        			memory.setNZP(oldpc);
	        			
	            		endOfBlock = true;
	            	}
	            	else if(instructionDef.isBranch())
	            	{
	            		/**
	            		 * Algo
	            		 * 1) check if jump is necessary by checking if psr's nzp == branch instruction nzp
	            		 * 	- if the block not jumping to exists
	            		 * 		add to queue the not to jumping block
	            		 * 	- if the block jumping to exists,
	            		 * 		break
	            		 * 	- if the block doesn't exist
	            		 * 		add to queue and jump
	            		 *	else if there is no jump
	            		 *	- if the block jumping to exists,
	            		 * 		don't add to queue
	            		 * 	- if the block doesn't exist
	            		 * 		add to queue and continue execution
	            		 */
	            		
	            		/*
	            		//add the instruction as vertex
	            		lc4block.addInstructionBlock(pc, instruction);

	            		if(ShouldBranch(instruction, memory.PSR))
	            		{
		            		//add to queue the non branched block
		            		queue.add(new QueueBlock(pc + 1, lc4block, memory, callStack));
	            			
		            		System.out.println("BRANCHING " + Word.toHex(pc, true));
		            		
		            		//check if this block already exists
		            		for(LC4Block block : blocks)
		            		{
		            			if(lc4block.equals(block))
		            			{
		            				nopFound = true;
		            				endOfBlock = true;
		    	            		System.out.println("FOUND ALREADY EXISTING BRANCH SYMBOL BLOCK " + Word.toHex(pc, true));
		            				break;
		            			}
		            		}
		            		
		        			//check if the pc exists already. If so, link the prev block to this pc.
		        			for(LC4Block block : blocks)
		            		{
		            			for(LC4InstructionBlock instructionBlock : block.instructionBlocks)
		            			{
		            				//link if the block contains the pc
		            				if(pc + 1 + instructionDef.getPCOffset(instruction) == instructionBlock.pc)
		            				{
		            					lc4block.addRemainingBranches(lc4block.instructionBlocks.get(lc4block.instructionBlocks.size() - 1), instructionBlock);
			            				nopFound = true;
			            				endOfBlock = true;
			    	            		System.out.println("FOUND ALREADY EXISTING BRANCH SYMBOL CELLS " + Word.toHex(pc, true));
		            					break;
		            				}
		            			}
		            		}
		        			
		        			//check if the branch is in the current block
		        			for(LC4InstructionBlock instructionBlock : lc4block.instructionBlocks)
	            			{
	            				//link if the block contains the pc
	            				if(pc + 1 + instructionDef.getPCOffset(instruction) == instructionBlock.pc)
	            				{
	            					System.out.println("LAST PC IN CURRENT BLOCK: " + Word.toHex(lc4block.instructionBlocks.get(lc4block.instructionBlocks.size() - 1).pc, true) + " CURRENT PC: " + Word.toHex(instructionBlock.pc, true));
	            					lc4block.addRemainingBranches(lc4block.instructionBlocks.get(lc4block.instructionBlocks.size() - 1), instructionBlock);
		            				nopFound = true;
		            				endOfBlock = true;
		    	            		System.out.println("FOUND ALREADY EXISTING BRANCH SYMBOL CELLS IN CURRENT BLOCK " + Word.toHex(pc, true));
	            					break;
	            				}
	            			}
		        			
		        			if(endOfBlock)
		        				break;
		        			
		        			blocks.add(lc4block);
		            		
		            		//if the block already exists, set it to be the existing block..
		            		for(LC4Block block : blocks)
		            		{
		            			for(LC4InstructionBlock instructionBlock : block.instructionBlocks)
		            			{
		            				//link if the block contains the pc
		            				if(lc4block.checkRange(instructionBlock.pc))
		            				{
		            					lc4block = block;

		            					break;
		            				}
		            			}
		            		}
		            		
		        			//set the new pc
		        			pc = pc + 1 + instructionDef.getPCOffset(instruction);
		        			
		            		//Create new a block
		            		LC4Block newLc4Block = new LC4Block();
		            		
		        			//try to add this block to the prev block (the block that called this block)
		        			lc4block.addNextBlock(newLc4Block);
		            		
		        			//set this to be the next block
		        			lc4block = newLc4Block;
		        			
		            		endOfBlock = true;	
	            		}
	            		else
	            		{
	            			
	            			boolean jumpBlockAlreadyEncountered = false;
	            			//check if this block already exists
		            		for(LC4Block block : blocks)
		            		{
		            			if(lc4block.equals(block))
		            			{
		            				jumpBlockAlreadyEncountered = true;
		            				break;
		            			}
		            		}
		            		
		        			//check if the pc exists already. If so, link the prev block to this pc.
		        			for(LC4Block block : blocks)
		            		{
		            			for(LC4InstructionBlock instructionBlock : block.instructionBlocks)
		            			{
		            				//link if the block contains the pc
		            				if(pc + 1 + instructionDef.getPCOffset(instruction) == instructionBlock.pc)
		            				{
			            				jumpBlockAlreadyEncountered = true;
		            					break;
		            				}
		            			}
		            		}
		        			
		        			//check if the branch is in the current block
		        			for(LC4InstructionBlock instructionBlock : lc4block.instructionBlocks)
	            			{
	            				//link if the block contains the pc
	            				if(pc + 1 + instructionDef.getPCOffset(instruction) == instructionBlock.pc)
	            				{
		            				jumpBlockAlreadyEncountered = true;
	            					break;
	            				}
	            			}
		        			
		        			
		            		//add to queue the non branched block
		        			if(!jumpBlockAlreadyEncountered)
		        			{
		        				System.out.println("NOT ENCOUNTERED JUMP BLOCK " + Word.toHex(pc + 1 + instructionDef.getPCOffset(instruction), true));
		        				queue.add(new QueueBlock(pc + 1 + instructionDef.getPCOffset(instruction), lc4block, memory, callStack));
		        			}
		        			
    	            		System.out.println("NON BRANCH " + Word.toHex(pc, true) + " ADDED JUMP BLOCK " + jumpBlockAlreadyEncountered);
    	            		pc++;
	            		}
	            		*/
	            		
	            		//add this last instruction (branch) to the block
	            		lc4block.addInstructionBlock(pc, instruction);
	            		
	            		//add to queue the first if statement
	            		queue.add(new QueueBlock(pc + 1, lc4block, memory, callStack));
	            		
	            		System.out.println("BRANCHING " + Word.toHex(pc, true));
	            		
	            		//check if this block already exists
	            		for(LC4Block block : blocks)
	            		{
	            			if(lc4block.equals(block))
	            			{
	            				nopFound = true;
	            				endOfBlock = true;
	    	            		System.out.println("FOUND ALREADY EXISTING BRANCH SYMBOL BLOCK " + Word.toHex(pc, true));
	            				break;
	            			}
	            		}
	            		
	            		if(endOfBlock)
	            			break;
	            		
	        			//check if the pc exists already. If so, link the prev block to this pc.
	        			for(LC4Block block : blocks)
	            		{
	            			for(LC4InstructionBlock instructionBlock : block.instructionBlocks)
	            			{
	            				//link if the block contains the pc
	            				if(pc + 1 + instructionDef.getPCOffset(instruction) == instructionBlock.pc)
	            				{
	            					lc4block.addRemainingBranches(lc4block.instructionBlocks.get(lc4block.instructionBlocks.size() - 1), instructionBlock);
		            				nopFound = true;
		            				endOfBlock = true;
		    	            		System.out.println("FOUND ALREADY EXISTING BRANCH SYMBOL CELLS " + Word.toHex(pc, true));
	            					break;
	            				}
	            			}
	            		}
	        			
	        			if(endOfBlock)
	        				break;
	        			
	        			//check if the branch is in the current block
	        			for(LC4InstructionBlock instructionBlock : lc4block.instructionBlocks)
            			{
            				//link if the block contains the pc
            				if(pc + 1 + instructionDef.getPCOffset(instruction) == instructionBlock.pc)
            				{
            					System.out.println("LAST PC: " + Word.toHex(lc4block.instructionBlocks.get(lc4block.instructionBlocks.size() - 1).pc, true) + " CURRENT PC: " + Word.toHex(instructionBlock.pc, true));
            					lc4block.addRemainingBranches(lc4block.instructionBlocks.get(lc4block.instructionBlocks.size() - 1), instructionBlock);
	            				nopFound = true;
	            				endOfBlock = true;
	    	            		System.out.println("FOUND ALREADY EXISTING BRANCH SYMBOL CELLS IN CURRENT BLOCK " + Word.toHex(pc, true));
            					break;
            				}
            			}
	        			
	        			if(endOfBlock)
	        				break;
	        			
	        			blocks.add(lc4block);
	            		
	            		//if the block already exists, set it to be the existing block..
	            		for(LC4Block block : blocks)
	            		{
	            			for(LC4InstructionBlock instructionBlock : block.instructionBlocks)
	            			{
	            				//link if the block contains the pc
	            				if(lc4block.checkRange(instructionBlock.pc))
	            				{
	            					lc4block = block;

	            					break;
	            				}
	            			}
	            		}
	        			
	        			//set the new pc
	        			pc = pc + 1 + instructionDef.getPCOffset(instruction);
	        			
	            		//Create new a block
	            		LC4Block newLc4Block = new LC4Block();
	            		
	        			//try to add this block to the prev block (the block that called this block)
	        			lc4block.addNextBlock(newLc4Block);
	            		
	        			//set this to be the next block
	        			lc4block = newLc4Block;
	        			
	            		endOfBlock = true;
	            		
	            	}
	            	else if(instructionDef.isTRAP())
	            	{
	            		//add this last instruction (jssr) to the block
	            		lc4block.addInstructionBlock(pc, instruction);
	            		
	            		//check if this block already exists
	            		for(LC4Block block : blocks)
	            		{
	            			if(lc4block.equals(block))
	            			{
	            				nopFound = true;
	            				endOfBlock = true;
	            				break;
	            			}
	            		}
	            		
	            		if(endOfBlock)
	            			break;
	            		
	        			//check if the pc exists already. If so, link the prev block to this pc.
	        			for(LC4Block block : blocks)
	            		{
	            			for(LC4InstructionBlock instructionBlock : block.instructionBlocks)
	            			{
	            				//link if the block contains the pc
	            				if(32768 + instruction.getZext(8, 0) == instructionBlock.pc)
	            				{
	            					lc4block.addNextBlock(block);
	            					break;
	            				}
	            			}
	            		}
	            		
	            		if(endOfBlock)
	            			break;
	        			
	              		//Add this block to the array of blocks
	        			blocks.add(lc4block);
	            		
	            		//push pc + 1 on the stack (return address)
	            		callStack.push(new Word(pc + 1));
	            		
	            		//Create new a block
	            		LC4Block newLc4Block = new LC4Block();
	            		
	        			//try to add this block to the prev block (the block that called this block)
	        			lc4block.addNextBlock(newLc4Block);
	            		
	        			//set this to be the next block
	        			lc4block = newLc4Block;
	        			
	        			//Get the previous pc
	        			int oldpc = pc + 1;
	        			
	        			//set new pc
	        			pc = 32768 + instruction.getZext(8, 0);
	        			
	        			//Set next pc
	        			memory.registers[7].setValue(oldpc);
	        			memory.setNZP(oldpc);
	        			
	            		endOfBlock = true;
	            	}
	            	else if(instructionDef.isCall())
	            	{
	            		
	            	}
	            	else
	            	{
	            		//rest of the instructions...
	            		if(instructionDef.isAdd())
	            		{
	            			int resultValue = memory.registers[instructionDef.getSReg(instruction)].getValue() + memory.registers[instructionDef.getTReg(instruction)].getValue();
	            			memory.registers[instructionDef.getDReg(instruction)].setValue(resultValue);
	            			memory.setNZP(resultValue);
	            		}
	            		else if(instructionDef.isMul())
	            		{
	            			int resultValue = memory.registers[instructionDef.getSReg(instruction)].getValue() * memory.registers[instructionDef.getTReg(instruction)].getValue();
	            			memory.registers[instructionDef.getDReg(instruction)].setValue(resultValue);
	            			memory.setNZP(resultValue);
	            		}
	            		else if(instructionDef.isSub())
	            		{
	            			int resultValue = memory.registers[instructionDef.getSReg(instruction)].getValue() - memory.registers[instructionDef.getTReg(instruction)].getValue();
	            			memory.registers[instructionDef.getDReg(instruction)].setValue(resultValue);
	            			memory.setNZP(resultValue);
	            		}
	            		else if(instructionDef.isDiv())
	            		{
	            			int resultValue = 0;
	            			if( memory.registers[instructionDef.getTReg(instruction)].getValue() != 0)
	            			{
	            				resultValue = memory.registers[instructionDef.getSReg(instruction)].getValue() / memory.registers[instructionDef.getTReg(instruction)].getValue();
	            			}
            				memory.registers[instructionDef.getDReg(instruction)].setValue(resultValue);
            				memory.setNZP(resultValue);
	            		}
	            		else if(instructionDef.isAddIMM())
	            		{
	            			int resultValue = memory.registers[instructionDef.getSReg(instruction)].getValue() + instructionDef.getSignedImmed(instruction);
	            			memory.registers[instructionDef.getDReg(instruction)].setValue(resultValue);
	            			memory.setNZP(resultValue);
	            		}
	            		else if(instructionDef.isCMP())
	            		{
	            			final short val1 = (short)memory.registers[instructionDef.getSReg(instruction)].getValue();
	            			final short val2 = (short)memory.registers[instructionDef.getTReg(instruction)].getValue();
	            			if (val1 > val2) {
		            			memory.setNZP(1);
	                        }
	                        else if (val1 == val2) {
		            			memory.setNZP(0);
	                        }
	                        else {
		            			memory.setNZP(32768);
	                        }
	            		}
	            		else if(instructionDef.isCMPU())
	            		{
	            			final int val1 = memory.registers[instructionDef.getSReg(instruction)].getValue();
	            			final int val2 = memory.registers[instructionDef.getTReg(instruction)].getValue();
	            			if (val1 > val2) {
		            			memory.setNZP(1);
	                        }
	                        else if (val1 == val2) {
		            			memory.setNZP(0);
	                        }
	                        else {
		            			memory.setNZP(32768);
	                        }
	            		}
	            		else if(instructionDef.isCMPI())
	            		{
	            			final short val1 = (short)memory.registers[instructionDef.getSReg(instruction)].getValue();
	            			final short val2 = (short)instructionDef.getSignedImmed(instruction);
	            			if (val1 > val2) {
		            			memory.setNZP(1);
	                        }
	                        else if (val1 == val2) {
		            			memory.setNZP(0);
	                        }
	                        else {
		            			memory.setNZP(32768);
	                        }
	            		}
	            		else if(instructionDef.isCMPIU())
	            		{
	            			final short val1 = (short)memory.registers[instructionDef.getSReg(instruction)].getValue();
	            			final short val2 = (short)instructionDef.getUnsignedImmed(instruction);
	            			if (val1 > val2) {
		            			memory.setNZP(1);
	                        }
	                        else if (val1 == val2) {
		            			memory.setNZP(0);
	                        }
	                        else {
		            			memory.setNZP(32768);
	                        }
	            		}
	            		else if(instructionDef.isAnd())
	            		{
	            			int resultValue = memory.registers[instructionDef.getSReg(instruction)].getValue() & memory.registers[instructionDef.getTReg(instruction)].getValue();
	            			memory.registers[instructionDef.getDReg(instruction)].setValue(resultValue);
	            			memory.setNZP(resultValue);
	            		}
	            		else if(instructionDef.isNot())
	            		{
	            			int resultValue = ~memory.registers[instructionDef.getSReg(instruction)].getValue();
	            			memory.registers[instructionDef.getDReg(instruction)].setValue(resultValue);
	            			memory.setNZP(resultValue);
	            		}
	            		else if(instructionDef.isOr())
	            		{
	            			int resultValue = memory.registers[instructionDef.getSReg(instruction)].getValue() | memory.registers[instructionDef.getTReg(instruction)].getValue();
	            			memory.registers[instructionDef.getDReg(instruction)].setValue(resultValue);
	            			memory.setNZP(resultValue);
	            		}
	            		else if(instructionDef.isXor())
	            		{
	            			int resultValue = memory.registers[instructionDef.getSReg(instruction)].getValue() ^ memory.registers[instructionDef.getTReg(instruction)].getValue();
	            			memory.registers[instructionDef.getDReg(instruction)].setValue(resultValue);
	            			memory.setNZP(resultValue);
	            		}
	            		else if(instructionDef.isAndIMM())
	            		{
	            			int resultValue = memory.registers[instructionDef.getSReg(instruction)].getValue() & instructionDef.getSignedImmed(instruction);
	            			memory.registers[instructionDef.getDReg(instruction)].setValue(resultValue);
	            			memory.setNZP(resultValue);
	            		}
	            		else if(instructionDef.isSLL())
	            		{
	            			int resultValue = memory.registers[instructionDef.getSReg(instruction)].getValue() << instructionDef.getUnsignedImmed(instruction);
	            			memory.registers[instructionDef.getDReg(instruction)].setValue(resultValue);
	            			memory.setNZP(resultValue);
	            		}
	            		else if(instructionDef.isSRA())
	            		{
	            			int resultValue = (short)memory.registers[instructionDef.getSReg(instruction)].getValue() >> instructionDef.getUnsignedImmed(instruction);
	            			memory.registers[instructionDef.getDReg(instruction)].setValue(resultValue);
	            			memory.setNZP(resultValue);
	            		}
	            		else if(instructionDef.isSRL())
	            		{
	            			int resultValue = memory.registers[instructionDef.getSReg(instruction)].getValue() >>> instructionDef.getUnsignedImmed(instruction);
	            			memory.registers[instructionDef.getDReg(instruction)].setValue(resultValue);
	            			memory.setNZP(resultValue);
	            		}
	            		else if(instructionDef.isMod())
	            		{
	            			int resultValue = 0;
	            			if( memory.registers[instructionDef.getTReg(instruction)].getValue() != 0)
	            			{
	            				resultValue = memory.registers[instructionDef.getSReg(instruction)].getValue() % memory.registers[instructionDef.getTReg(instruction)].getValue();
	            			}
            				memory.registers[instructionDef.getDReg(instruction)].setValue(resultValue);
            				memory.setNZP(resultValue);
	            		}
	            		else if(instructionDef.isLoad())
	            		{
	            			int resultAddress = memory.registers[instructionDef.getSReg(instruction)].getValue() + instructionDef.getSignedImmed(instruction);
	            			
	            			//check if the value is in an edited data memory
	            			boolean fromEditedDataMemory = false;
	            			int resultValue = 0;
	            			for(java.util.Map.Entry<Integer, Word> addressAndValue : memory.changesDataMemory.entrySet())
	            			{
	            				if(resultAddress == addressAndValue.getKey())
	            				{
	            					fromEditedDataMemory = true;
	            					
	            					//get this edited memory
	            					resultValue = addressAndValue.getValue().getValue();
	            					
	            					break;
	            				}
	            			}
	            			
	            			//pull from data memory
	            			
	            			if(!fromEditedDataMemory)
	            			{
	            				resultValue = mac.getMemory().read(resultAddress).getValue();
	            			}
	            			
	            			memory.registers[instructionDef.getDReg(instruction)].setValue(resultValue);
	            			
	            			System.out.println("LOAD PC " + Word.toHex(pc, true));
	            			
	            			System.out.println("Loaded address: " + Word.toHex(resultAddress) + " from " + instructionDef.getDReg(instruction) +  " with value " + Word.toHex(resultValue) + " into register " + instructionDef.getSReg(instruction));
	            			
	            			memory.setNZP(resultValue);
	            		}
	            		else if(instructionDef.isStore())
	            		{
	            			int resultAddress = memory.registers[instructionDef.getSReg(instruction)].getValue() + instructionDef.getSignedImmed(instruction);
	            			
	            			//check if the value is in an edited data memory
	            			boolean fromEditedDataMemory = false;
	            			int value = memory.registers[instructionDef.getDReg(instruction)].getValue();
	            			for(java.util.Map.Entry<Integer, Word> addressAndValue : memory.changesDataMemory.entrySet())
	            			{
	            				if(resultAddress == addressAndValue.getKey())
	            				{
	            					fromEditedDataMemory = true;
	            					
	            					//get this edited memory and change its value
	            					memory.changesDataMemory.put(resultAddress, new Word(value));
	            					break;
	            				}
	            			}
	            			
	            			//pull from data memory
	            			if(!fromEditedDataMemory)
	            			{
            					memory.changesDataMemory.put(resultAddress, new Word(value));
	            			}
	            			
	            			System.out.println("STORE PC " + Word.toHex(pc, true));
	            			
	            			System.out.println("Wrote address: " + Word.toHex(resultAddress) + " from " + instructionDef.getSReg(instruction) + " with value " + Word.toHex(value) + " into register " + instructionDef.getDReg(instruction));
	            			
	            		}
	            		else if(instructionDef.isConst())
	            		{
	            			int resultValue = instructionDef.getSignedImmed(instruction);
	            			memory.registers[instructionDef.getDReg(instruction)].setValue(resultValue);
            				memory.setNZP(resultValue);
	            		}
	            		else if(instructionDef.isConstIMM())
	            		{
	            			int resultValue = instructionDef.getSignedImmed(instruction);
	            			memory.registers[instructionDef.getDReg(instruction)].setValue(resultValue);
            				memory.setNZP(resultValue);
	            		}
	            		else if(instructionDef.isHiConst())
	            		{
	            			int resultValue = memory.registers[instructionDef.getDReg(instruction)].getValue() & 0xFF | instructionDef.getUnsignedImmed(instruction) << 8;
	            			memory.registers[instructionDef.getDReg(instruction)].setValue(resultValue);
	            			memory.setNZP(resultValue);
	            		}

	            		//add the instruction as vertex
	            		lc4block.addInstructionBlock(pc, instruction);
	            		pc++;
	            	}
	            	System.out.println("PC: " + Word.toHex(pc, true));
				}
				
			}   
		}
	}

	public LC4Decompiler(Machine mac)
	{
		super();
		
		init(mac);
	}
	
    private void applyStyleToGraph(mxGraph mxgraph) {
    	
    	
	    // Settings for vertices
	    Map<String, Object> vertex = mxgraph.getStylesheet().getDefaultVertexStyle();
	    vertex.put(mxConstants.STYLE_ROUNDED, true);
	    vertex.put(mxConstants.STYLE_ORTHOGONAL, false);
	    vertex.put(mxConstants.STYLE_STROKECOLOR, "#000000"); // default is #6482B9
	    vertex.put(mxConstants.STYLE_FONTCOLOR, "#446299");
    	
	    // Settings for edges
	    Map<String, Object> edge = new HashMap<String, Object>();
	    edge.put(mxConstants.STYLE_ROUNDED, true);
	    edge.put(mxConstants.STYLE_ORTHOGONAL, false);
	    edge.put(mxConstants.STYLE_EDGE, "elbowEdgeStyle");
	    edge.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CONNECTOR);
	    edge.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
	    edge.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
	    edge.put(mxConstants.STYLE_ALIGN, mxConstants.ALIGN_CENTER);
	    edge.put(mxConstants.STYLE_STROKECOLOR, "#000000"); // default is #6482B9
	    edge.put(mxConstants.STYLE_FONTCOLOR, "#446299");
	
	    mxStylesheet style = new mxStylesheet();
	    style.setDefaultEdgeStyle(edge);
	    style.setDefaultVertexStyle(vertex);
	    mxgraph.setStylesheet(style);
	}
    
    
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    
    class Address
    {
    	public int address;
    	public Object vertex;
    	
    	public Address(int address)
    	{
    		this.address = address;
    		vertex = null;
    	}
    }
    
	class Block
	{
		public ArrayList<Address> addresses = new ArrayList<Address>();
		public ArrayList<Block> prev_blocks = new ArrayList<Block>();
		public ArrayList<Block> next_blocks = new ArrayList<Block>();
		
		Object vertex;
		
		Block()
		{
			
		}
		
		public Integer GetStartAddress()
		{
			return addresses.get(0).address;
		}
		
		public Integer GetEndAddress()
		{
			return addresses.get(addresses.size() - 1).address;
		}
		
		public Integer GetLength()
		{
			return addresses.size();
		}
		
		public void AddFunction(int new_address)
		{
			addresses.add(new Address(new_address));
		}
	}
    
    public class BlockAnalyzer
    {
    	public Hashtable<Integer, String> entry_points;
    	public ArrayList<Block> blocks;
    	BlockAnalyzer()
    	{
    		//Get all entry points
    		GetFunctionEntryPoints();
    		
    		blocks = new ArrayList<Block>();
    		
    		//Break everything into function blocks
    		BreakIntoBlocks();
    		
    		//Link branch statements
    		LinkBranches();
    	}
    	
		public InstructionDef GetInstructionAt(int pc)
		{
			//get instruction
        	Word instruction = mac.getMemory().checkAndReadNoException(pc);
        	
        	//parse the instruction
        	return ISA.lookupTable[instruction.getValue()];
		}
    	
    	//Get all possible entry points from symbol table (cut out symbols from data)
    	private void GetFunctionEntryPoints()
    	{
    		//clone entry points
    		entry_points = (Hashtable<Integer, String>) mac.getSymTable().GetatosTable().clone();
    		
    		Iterator<Integer> it = entry_points.keySet().iterator();
    		while(it.hasNext())
    		{
    			Integer entry_point_address = it.next();
    			if((entry_point_address >= 0x2000 && entry_point_address < 0x4000) || entry_point_address >= 0xa000)
    			{
    				it.remove();
    			}
    		}
    	}
    	
    	public Block CreateBlock(int addr)
    	{
			Block block = new Block();
			
			//start at entry and continue until
			//1) nop
			//2) jmp
			//3) branch
			
        	InstructionDef instructionDef = this.GetInstructionAt(addr);
        	
        	while(!instructionDef.isBranch() &&
        		  !instructionDef.isCall() &&
        		  !instructionDef.isJSR() &&
        		  !instructionDef.isJSRR() &&
        		  !instructionDef.isJump() &&
        		  !instructionDef.isJumpR() &&
        		  !instructionDef.isRet() &&
        		  !instructionDef.isNop() &&
        		  !instructionDef.isTRAP()
        		  )
        	{
        		//add the instruction
        		block.AddFunction(addr);
	        	
        		addr++;
        		
            	//parse the instruction
            	instructionDef = this.GetInstructionAt(addr);
        	}
        	
    		//add the last instruction
    		block.AddFunction(addr);
        	
        	return block;
    	}
    	
    	//Break entry points into blocks... Does not link them though
    	public void BreakIntoBlocks()
    	{
    		//convert entry_points to PriorityQueue
    		PriorityQueue<Integer> entries = new PriorityQueue(entry_points.keySet());
    		
    		while(!entries.isEmpty())
    		{
    			Integer entry = entries.poll();
    			
    			Block block = CreateBlock(entry);
    			
    			blocks.add(block);
    		}
    		
    		//now remove any blocks that are in anther block because we are only iterating through symbols
    		HashSet<Block> blocks_to_remove = new HashSet<Block>();
    		for(Block block : blocks)
    		{
    			for(Block inside_block : blocks)
    			{
    				//do not remove the same block itself
    				if(inside_block == block)
    				{
    					continue;
    				}
    				
    				//only check for the first address
    				for(Address address : block.addresses)
    				{
    					if(address.address == inside_block.GetStartAddress())
	    				{
    						System.out.println(address.address + " -- " + inside_block.GetStartAddress());
	    					blocks_to_remove.add(inside_block);
	    					break;
    					}
    				}
    			}
    		}
    		blocks.removeAll(blocks_to_remove);
    	}
    	
    	public void LinkBranches()
    	{
    		//remove blocks that after branch because we add them to next block 
    		HashSet<Block> blocks_to_remove = new HashSet<Block>();
    		Iterator<Block> block_it = blocks.iterator();
    		while(block_it.hasNext())
    		{
    			Block block = block_it.next();
    			
    			int last_address = block.GetEndAddress();
    			
    			//get instruction
            	Word instruction = mac.getMemory().checkAndReadNoException(last_address);
            	
            	//parse the instruction
            	InstructionDef instructionDef = ISA.lookupTable[instruction.getValue()];
            	
    			if(instructionDef.isBranch())
    			{
    				//link this block with the branch block
    	    		Iterator<Block> branch_block_it = blocks.iterator();
    	    		while(branch_block_it.hasNext())
    				{
    	    			Block branch_block = branch_block_it.next();
    	    			
    					//where does the branch go to? - the address after branch execution
        				if(last_address + 1 + instructionDef.getPCOffset(instruction) == branch_block.GetStartAddress())
        				{
        					block.next_blocks.add(branch_block);
        					branch_block.prev_blocks.add(block);
        					blocks_to_remove.add(branch_block);
        					//System.out.println(block.GetStartAddress() + " - " + block.GetEndAddress() + " --- " + branch_block.GetStartAddress());
        					
        					//check if the branch and non branch was the same... it's dumb, but it happens
        					if(branch_block.GetStartAddress().equals(block.GetEndAddress() + 1))
        					{
        						continue;
        					}
        					
        					//now create new block that contains the code if jmp does not execute
        					Block block_if_branch_was_not_executed = CreateBlock(block.GetEndAddress() + 1);
        					
        					block.next_blocks.add(block_if_branch_was_not_executed);
        					
        					//check which one is branch is bigger and see if one contains the other
        					Block larger_block = branch_block.addresses.size() > block_if_branch_was_not_executed.addresses.size()
        							? branch_block : block_if_branch_was_not_executed;
        					Block smaller_block = branch_block.addresses.size() < block_if_branch_was_not_executed.addresses.size()
        							? branch_block : block_if_branch_was_not_executed;
        					
        					
        					//UGH THIS IS IN AN ITERATOR. WILL NOT WORK PROPERLY. USE LIKE A DATASTRUCTURE TO REMOVE CODE LATER
        					Iterator<Address> larger_address_it = larger_block.addresses.iterator();
        					while(larger_address_it.hasNext())
        					{
        						Integer addr = larger_address_it.next().address;
        						
        						for(Address smaller_addr : smaller_block.addresses)
        						{
        							if(addr.equals(smaller_addr.address))
        							{
        								if(larger_block.addresses.size() == 1)
        								{
        									//System.out.println("!! " + mac.getSymTable().lookupAddr(block.GetStartAddress()));
        									//System.out.println(Word.toHex(larger_block.GetStartAddress()));
        									continue;
        								}
        								larger_address_it.remove();
        								break;
        							}
        						}
        					}
        					break;
        				}
    				}
    			}
    		}
    		blocks.removeAll(blocks_to_remove);
    		
    	}
    	
		int cellWidth = 200;
		int cellHeight = 30;
    	
    	private Object DrawBlock(Block block, mxGraph mxgraph)
    	{
			Object vertex = mxgraph.insertVertex(mxgraph.getDefaultParent(), null, "gggggg", 20, 20, cellWidth, cellHeight * block.addresses.size());

    		//Get the last instruction in the current block (will be used in the edge)
			for(int i = 0; i < block.addresses.size(); i++)
			{
				int addr = block.addresses.get(i).address;
				
				//get instruction
	        	Word instruction = mac.getMemory().checkAndReadNoException(addr);
				
				InstructionDef instructionDef = ISA.lookupTable[instruction.getValue()];
        		
				//get symbol for the current address
    			String symbolString = mac.getSymTable().lookupAddr(addr);

    			//add symbol if exists
    			if(symbolString != null)
				{
					mxgraph.insertVertex(vertex, null, symbolString, 0, cellHeight * i, cellWidth/3, cellHeight);
				}
    			
    			//add instruction
    			String instructionString = instructionDef.disassemble(instruction, addr, mac);
    			
				mxgraph.insertVertex(vertex, null, Word.toHex(addr, true) + " " + instructionString, cellWidth/3, cellHeight * i, 2*cellWidth/3, cellHeight);
    			
    			//System.out.println(Word.toHex(addr, true));
			}
			
			return vertex;
    	}
    	
    	//recursive call to draw next blocks
    	private void DrawNextBlock(Block block, mxGraph mxgraph, Object prev_block_vertex)
    	{	
			//Link this block to the next block
			for(Block next_block : block.next_blocks)
			{
				//check if any next blocks contain previous blocks (loops again)
				if(block.prev_blocks.contains(next_block))
				{
					break;
				}
				
				int addr = block.GetEndAddress();
				
				//get instruction
	        	Word instruction = mac.getMemory().checkAndReadNoException(addr);
				
				InstructionDef instructionDef = ISA.lookupTable[instruction.getValue()];
	        	
    			//get instruction string
    			String instructionString = instructionDef.disassemble(instruction, addr, mac);
	        	
    			Object next_block_vertex = DrawBlock(next_block, mxgraph);
    			
    			mxgraph.insertEdge(mxgraph.getDefaultParent(), null, Word.toHex(block.GetEndAddress(), true) + " " + instructionString, prev_block_vertex, next_block_vertex);
    			
    			//check for loops
    			if(next_block.GetStartAddress().equals(block.GetStartAddress()) || next_block.GetStartAddress().equals(block.GetEndAddress()) ||
    			   next_block.GetEndAddress().equals(block.GetStartAddress()) || next_block.GetEndAddress().equals(block.GetEndAddress()))
    			{
    				continue;
    			}
    			
    			System.out.println(block.GetStartAddress() + " --- " + next_block.GetStartAddress());
    			
    			//recursively call to draw next next block's vertices
    			DrawNextBlock(next_block, mxgraph, next_block_vertex);
			}
    	}
    	
    	public ClosableTabbedPane GetPanes()
    	{    		
    		ClosableTabbedPane subPane = new ClosableTabbedPane() {
                public boolean tabAboutToClose(int tabIndex) {
                    int choice = JOptionPane.showConfirmDialog(null, 
                       "You are about to close this window\nDo you want to proceed ?", 
                       "Confirmation Dialog", 
                       JOptionPane.INFORMATION_MESSAGE);
                    return choice == 0;
                }
            };
    		
    		for(Block block : blocks)
    		{
    			//Create graph
    			mxGraph mxgraph = new mxGraph();
    			
    			//get graphParent to graph
    			Object mxGraphParent = mxgraph.getDefaultParent();
    			mxgraph.getModel().beginUpdate();
    			    			
    			//draw current block
    			Object current_block_vertex = DrawBlock(block, mxgraph);
    			
    			//draw any blocks that current block is linked to
    			DrawNextBlock(block, mxgraph, current_block_vertex);
    			
    			try
    			{
    				applyStyleToGraph(mxgraph);
    				
    				//mxIGraphLayout layout = new mxHierarchicalLayout(graph);
    				mxIGraphLayout layout = new ExtendedCompactTreeLayout(mxgraph);

    				layout.execute(mxGraphParent);
    			}
    			finally
    			{
    				mxgraph.getModel().endUpdate();
    			}
    			
    			 /**
                 * Disassembly panel
                 */
        		
        		//Disassembly panel that contains graphs, etc
        		JPanel disassemblyPanel = new JPanel(new BorderLayout());
        		disassemblyPanel.setVisible(true);
                
        		//Graph Component (Similar to JPanel (which is also a component))
        		mxGraphComponent graphComponent = new mxGraphComponent(mxgraph);

        		//add the graph to disassembly panel
        		disassemblyPanel.add(graphComponent, BorderLayout.CENTER);
        		
   			 	/**
                 * Decompiled panel
                 */
        		
        		//Decompiled panel that contains code
        		JPanel decompiledPanel = new JPanel(new BorderLayout());
        		decompiledPanel.setVisible(true);

        		Decompiler decompiler = new Decompiler(block);
        		JScrollPane textPane = decompiler.DecompileToCCode();
        		
        		decompiledPanel.add(textPane, BorderLayout.CENTER);
        		
        		/**
        		 * Tab panel
        		 */
        		
        		//Tabbing window that keeps track of subroutines clicked
        		JPanel tabPanel = new JPanel(new BorderLayout());
        		tabPanel.setVisible(true);
        		
        		String symbol_string = mac.getSymTable().lookupAddr(block.GetStartAddress());
        		
        		JSplitPane splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, disassemblyPanel, decompiledPanel);
        		splitPanel.setPreferredSize(new Dimension(800, 600));
        		splitPanel.setVisible(true);
        		splitPanel.setOneTouchExpandable(true);
        		
        		//Create subroutine pane
        		subPane.addTab(symbol_string, splitPanel);
    		}
        	return subPane;
    	}
    }
	
    class Decompiler
    {
    	Block block;
    	String c_code;
    	int indent_spaces = 0;
    	
    	public Decompiler(Block block)
    	{
    		this.block = block;
    		this.c_code = new String();
    	}
    	
    	public String Repeat(int count, String with) {
    	    return new String(new char[count]).replace("\0", with);
    	}
    	
    	private String Indent()
    	{
    		return Repeat(indent_spaces, "\t");
    	}
    	
    	private String Write(String str)
    	{
    		return Indent() + str + "\n";
    	}
    	
   		final int INSTRUCTION_CMP = 1;
		final int INSTRUCTION_CMPI = 2;
		final int INSTRUCTION_CMPU = 3;
		final int INSTRUCTION_CMPIU = 4;
		final int INSTRUCTION_NOP = 5;
		final int INSTRUCTION_CALL = 6;
		final int INSTRUCTION_BRANCH = 7;
		final int INSTRUCTION_LDR = 8;
		final int INSTRUCTION_STR = 9;
		final int INSTRUCTION_JMP = 10;
		final int INSTRUCTION_JSR = 11;
		final int INSTRUCTION_JMPR = 12;
		final int INSTRUCTION_TRAP = 13;
		final int INSTRUCTION_RET = 14;
		final int INSTRUCTION_ADD = 15;
		final int INSTRUCTION_SUB = 16;
		final int INSTRUCTION_MUL = 17;
		final int INSTRUCTION_DIV = 18;
		final int INSTRUCTION_ADDIMM = 19;
		final int INSTRUCTION_AND = 20;
		final int INSTRUCTION_NOT = 21;
		final int INSTRUCTION_OR = 22;
		final int INSTRUCTION_XOR = 23;
		final int INSTRUCTION_ANDIMM = 24;
		final int INSTRUCTION_CONST = 25;
		final int INSTRUCTION_HICONST = 26;
		final int INSTRUCTION_SLL = 27;
		final int INSTRUCTION_SRA = 28;
		final int INSTRUCTION_SRL = 29;
		final int INSTRUCTION_MOD = 30;
    	
    	class InstructionBlock
    	{
    		/*
    		public enum InstructionBlockType
    		{
           		INSTRUCTION_CMP,
        		INSTRUCTION_CMPI,
        		INSTRUCTION_CMPU,
        		INSTRUCTION_CMPIU,
        		INSTRUCTION_NOP,
        		INSTRUCTION_CALL,
        		INSTRUCTION_BRANCH,
        		INSTRUCTION_LDR,
        		INSTRUCTION_STR,
        		INSTRUCTION_JMP,
        		INSTRUCTION_JSR,
        		INSTRUCTION_JMPR,
        		INSTRUCTION_TRAP,
        		INSTRUCTION_RET,
        		INSTRUCTION_ADD,
        		INSTRUCTION_SUB,
        		INSTRUCTION_MUL,
        		INSTRUCTION_DIV,
        		INSTRUCTION_ADDIMM,
        		INSTRUCTION_AND,
        		INSTRUCTION_NOT,
        		INSTRUCTION_OR,
        		INSTRUCTION_XOR,
        		INSTRUCTION_ANDIMM,
        		INSTRUCTION_CONST,
        		INSTRUCTION_HICONST,
        		INSTRUCTION_SLL,
        		INSTRUCTION_SRA,
        		INSTRUCTION_SRL,
        		INSTRUCTION_MOD
    		}
    		*/
    		
    		int type = 0;
    		String assembly_string;
    		String label_string;
    		String c_string;
    		InstructionBlock prev;
    		InstructionBlock next;
    		String Rd;
    		String Rs;
    		String Rt;
    		String Imm;
    		
    		InstructionBlock(Block block, int addr)
    		{
				//get instruction
	        	Word instruction = mac.getMemory().checkAndReadNoException(addr);
				
				InstructionDef instructionDef = ISA.lookupTable[instruction.getValue()];
				
				//get instruction string
    			String instructionString = instructionDef.disassemble(instruction, addr, mac);
    			
    			String label_string = mac.getSymTable().lookupAddr(addr);
    			
    			//
    			//parse instruction
    			//
    			
    			if(instructionDef.isAdd())
        		{
    				type = INSTRUCTION_ADD;
    				
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Rs = "R" + instructionDef.getSReg(instruction);
        			Rt = "R" + instructionDef.getTReg(instruction);
        			
        			assembly_string += Write(Rd + " = " + Rs + " + " + Rt);
        		}
        		else if(instructionDef.isMul())
        		{
    				type = INSTRUCTION_MUL;
    				
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Rs = "R" + instructionDef.getSReg(instruction);
        			Rt = "R" + instructionDef.getTReg(instruction);
        			
        			assembly_string += Write(Rd + " = " + Rs + " * " + Rt);
        		}
        		else if(instructionDef.isSub())
        		{
    				type = INSTRUCTION_SUB;
        			
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Rs = "R" + instructionDef.getSReg(instruction);
        			Rt = "R" + instructionDef.getTReg(instruction);
        			
        			assembly_string = Rd + " = " + Rs + Rt;
        		}
        		else if(instructionDef.isDiv())
        		{
    				type = INSTRUCTION_DIV;
    				
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Rs = "R" + instructionDef.getSReg(instruction);
        			Rt = "R" + instructionDef.getTReg(instruction);
        			
        			assembly_string += Write(Rd + " = " + Rs + " / " + Rt);
        		}
        		else if(instructionDef.isAddIMM())
        		{
    				type = INSTRUCTION_ADDIMM;
        			
        			String Rd = "R" + instructionDef.getDReg(instruction);
        			String Rs = "R" + instructionDef.getSReg(instruction);
        			String Imm = "" + instructionDef.getSignedImmed(instruction);
        			
        			assembly_string += Write(Rd + " = " + Rs + " + " + Rt);
        		}
        		else if(instructionDef.isCMP())
        		{
    				type = INSTRUCTION_CMP;

        			String Rs = "R" + instructionDef.getSReg(instruction);
        			String Rt = "R" + instructionDef.getTReg(instruction);
        			
        			//assume next instruction is branch
    	        	Word branch_instruction = mac.getMemory().checkAndReadNoException(++addr);
    				
    				InstructionDef branch_instruction_def = ISA.lookupTable[branch_instruction.getValue()];
        			
        			if(branch_instruction_def.isBranch())
        			{
        				int branch_bits = branch_instruction.getZext(11, 9);
        				switch (branch_bits)
        				{
        				case 0x4:
        				{
                			assembly_string += Write("if (" + Rs + " < " + Rt + ")");
                			assembly_string += Write("{");
        				}
        				break;
        				case 0x6:
        				{
                			assembly_string += Write("if (" + Rs + " <= " + Rt + ")");
                			assembly_string += Write("{");
        				}
        				break;
        				case 0x5:
        				{
                			assembly_string += Write("if (" + Rs + " != " + Rt + ")");
                			assembly_string += Write("{");
        				}
        				break;
        				case 0x2:
        				{
                			assembly_string += Write("if (" + Rs + " == " + Rt + ")");
                			assembly_string += Write("{");
        				}
        				break;
        				case 0x3:
        				{
                			assembly_string += Write("if (" + Rs + " >= " + Rt + ")");
                			assembly_string += Write("{");
        				}
        				break;
        				case 0x1:
        				{
                			assembly_string += Write("if (" + Rs + " > " + Rt + ")");
                			assembly_string += Write("{");
        				}
        				break;
        				default:
        				{
                			assembly_string += Write("if (" + Rs + " = " + Rt + ")");
                			assembly_string += Write("{");
        				}
        				break;
        				}
        			}
        			
        			/*
        			if(block.next_blocks.size() == 1)
        			{
            			//branched statement
        				indent_spaces++;
            			assembly_string += Decompileassembly_string(block.next_blocks.get(0));
        				indent_spaces--;
           			}
        			if(block.next_blocks.size() == 2)
        			{
            			//branched statement
        				indent_spaces++;
            			assembly_string += Decompileassembly_string(block.next_blocks.get(0));
        				indent_spaces--;
        				
            			assembly_string += Write("}");
            			assembly_string += Write("else");
            			assembly_string += Write("{");
        								
        				//non branched statement
        				indent_spaces++;
        				assembly_string += Decompileassembly_string(block.next_blocks.get(1));
        				indent_spaces--;
           			}
        			assembly_string += Write("}");
        			*/
        		}
        		else if(instructionDef.isCMPU())
        		{

        			
        		}
        		else if(instructionDef.isCMPI())
        		{

        		}
        		else if(instructionDef.isCMPIU())
        		{

        		}
        		else if(instructionDef.isBranch())
        		{
        			//assume next instruction is branch
    	        	Word prev_instruction = mac.getMemory().checkAndReadNoException(addr - 1);
    				
    				InstructionDef prev_instruction_def = ISA.lookupTable[prev_instruction.getValue()];
        			
    				boolean valid_prev_instruction = false;
    				
    				if(prev_instruction_def.isAdd())
    				{
    					valid_prev_instruction = true;
    				}
    				else if(prev_instruction_def.isMul())
    				{
    					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isSub())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isDiv())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isAddIMM())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isAnd())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isOr())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isXor())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isAndIMM())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isLdr())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isHiConst())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isConst())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isSLL())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isSRA())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isSRL())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isMod())
    				{
     					valid_prev_instruction = true;
    				}
    				
    				if(valid_prev_instruction)
    				{
    					String Rd = "R" + prev_instruction_def.getDReg(prev_instruction);
        				
            			if(instructionDef.isBranch())
            			{
            				int branch_bits = instruction.getZext(11, 9);
            				switch (branch_bits)
            				{
            				case 0x4:
            				{
                    			assembly_string += Write("if (" + Rd + " < 0)");
                    			assembly_string += Write("{");
            				}
            				break;
            				case 0x6:
            				{
                    			assembly_string += Write("if (" + Rd + " <= 0)");
                    			assembly_string += Write("{");
            				}
            				break;
            				case 0x5:
            				{
                    			assembly_string += Write("if (" + Rd + " != 0)");
                    			assembly_string += Write("{");
            				}
            				break;
            				case 0x2:
            				{
                    			assembly_string += Write("if (" + Rd + " == 0)");
                    			assembly_string += Write("{");
            				}
            				break;
            				case 0x3:
            				{
                    			assembly_string += Write("if (" + Rd + " >= 0)");
                    			assembly_string += Write("{");
            				}
            				break;
            				case 0x1:
            				{
                    			assembly_string += Write("if (" + Rd + " > 0)");
                    			assembly_string += Write("{");
            				}
            				break;
            				default:
            				{
                    			assembly_string += Write("if (" + Rd + " = 0)");
                    			assembly_string += Write("{");
            				}
            				break;
            				}
            			}
            			
            			/*
            			if(block.next_blocks.size() == 1)
            			{
                			//branched statement
            				indent_spaces++;
                			assembly_string += Decompileassembly_string(block.next_blocks.get(0));
            				indent_spaces--;
               			}
            			if(block.next_blocks.size() == 2)
            			{
                			//branched statement
            				indent_spaces++;
                			assembly_string += Decompileassembly_string(block.next_blocks.get(0));
            				indent_spaces--;
            				
                			assembly_string += Write("}");
                			assembly_string += Write("else");
                			assembly_string += Write("{");
            								
            				//non branched statement
            				indent_spaces++;
            				assembly_string += Decompileassembly_string(block.next_blocks.get(1));
            				indent_spaces--;
               			}	
            			assembly_string += Write("}");
            			*/
    				}
        		}
        		else if(instructionDef.isAnd())
        		{
    				type = INSTRUCTION_AND;
        			
        			String Rd = "R" + instructionDef.getDReg(instruction);
        			String Rs = "R" + instructionDef.getSReg(instruction);
        			String Rt = "R" + instructionDef.getTReg(instruction);
        			
        			assembly_string += Write(Rd + " = " + Rs + " & " + Rt);
        		}
        		else if(instructionDef.isNot())
        		{
    				type = INSTRUCTION_NOT;
        			
        			String Rd = "R" + instructionDef.getDReg(instruction);
        			String Rs = "R" + instructionDef.getSReg(instruction);
        			
        			assembly_string += Write(Rd + " = ~" + Rs);
        		}
        		else if(instructionDef.isOr())
        		{
    				type = INSTRUCTION_OR;
        			
        			String Rd = "R" + instructionDef.getDReg(instruction);
        			String Rs = "R" + instructionDef.getSReg(instruction);
        			String Rt = "R" + instructionDef.getTReg(instruction);
        			
        			assembly_string += Write(Rd + " = " + Rs + " | " + Rt);
        		}
        		else if(instructionDef.isXor())
        		{
    				type = INSTRUCTION_XOR;
        			
        			String Rd = "R" + instructionDef.getDReg(instruction);
        			String Rs = "R" + instructionDef.getSReg(instruction);
        			String Rt = "R" + instructionDef.getTReg(instruction);
        			
        			assembly_string += Write(Rd + " = " + Rs + " ^ " + Rt);
        		}
        		else if(instructionDef.isAndIMM())
        		{
    				type = INSTRUCTION_ANDIMM;
    				
        			String Rd = "R" + instructionDef.getDReg(instruction);
        			String Rs = "R" + instructionDef.getSReg(instruction);
        			String Imm = Word.toHex(instructionDef.getSignedImmed(instruction), true);
        			
        			assembly_string += Write(Rd + " = " + Rs + " & " + Imm);
        		}
        		else if(instructionDef.isSLL())
        		{
    				type = INSTRUCTION_SLL;
        			
        			String Rd = "R" + instructionDef.getDReg(instruction);
        			String Rs = "R" + instructionDef.getSReg(instruction);
        			String Imm = "" + instructionDef.getUnsignedImmed(instruction);
        			
        			assembly_string += Write(Rd + " = " + Rs + " << " + Imm);
        		}
        		else if(instructionDef.isSRA())
        		{
    				type = INSTRUCTION_SRA;
        			
        			String Rd = "R" + instructionDef.getDReg(instruction);
        			String Rs = "R" + instructionDef.getSReg(instruction);
        			String Imm = "" + instructionDef.getUnsignedImmed(instruction);
        			
        			assembly_string += Write(Rd + " = " + Rs + " >> " + Imm + " | (" + Rs + " & 0x8000)");
        		}
        		else if(instructionDef.isSRL())
        		{
    				type = INSTRUCTION_SRL;
        			
        			String Rd = "R" + instructionDef.getDReg(instruction);
        			String Rs = "R" + instructionDef.getSReg(instruction);
        			String Imm = "" + instructionDef.getUnsignedImmed(instruction);
        			
        			assembly_string += Write(Rd + " = " + Rs + " >> " + Imm);
        		}
        		else if(instructionDef.isMod())
        		{
    				type = INSTRUCTION_MOD;
        			
        			String Rd = "R" + instructionDef.getDReg(instruction);
        			String Rs = "R" + instructionDef.getSReg(instruction);
        			String Rt = "R" + instructionDef.getTReg(instruction);
        			
        			assembly_string += Write(Rd + " = " + Rs + " % " + Rt);
        		}
        		else if(instructionDef.isLoad())
        		{
    				type = INSTRUCTION_LDR;
        			
        			String Rd = "R" + instructionDef.getDReg(instruction);
        			String Rs = "R" + instructionDef.getSReg(instruction);
        			String Imm = "" + instructionDef.getSignedImmed(instruction);
        			
        			assembly_string += Write(Rs + "[" + Imm + "] = " + Rd);
        		}
        		else if(instructionDef.isStore())
        		{
    				type = INSTRUCTION_STR;
    				
        			String Rd = "R" + instructionDef.getDReg(instruction);
        			String Rs = "R" + instructionDef.getSReg(instruction);
        			String Imm = "" + instructionDef.getSignedImmed(instruction);
        			
        			assembly_string += Write(Rd + " = " + Rs + "[" + Imm + "]");
        		}
        		else if(instructionDef.isConst())
        		{
    				type = INSTRUCTION_CONST;
    				
        			String Rd = "R" + instructionDef.getDReg(instruction);
        			String Imm = Word.toHex(instructionDef.getSignedImmed(instruction), true);
        			
        			assembly_string += Write(Rd + " = " + Imm);
        		}
        		else if(instructionDef.isConstIMM())
        		{
    				type = INSTRUCTION_CONST;

        			String Rd = "R" + instructionDef.getDReg(instruction);
        			String Imm = Word.toHex(instructionDef.getSignedImmed(instruction), true);
        			
        			assembly_string += Write(Rd + " = " + Imm);
        		}
        		else if(instructionDef.isHiConst())
        		{
    				type = INSTRUCTION_HICONST;
        			
        			String Rd = "R" + instructionDef.getDReg(instruction);
        			String Imm = Word.toHex(instructionDef.getUnsignedImmed(instruction), true);
        			
        			assembly_string += Write(Rd + " = " + "( " + Rd + " & 0x00FF ) | ( " + Imm + " << 8 )");
        		}
    			else
    			{
    				assembly_string += Write(instructionString);
    			}
				
    		}
    		
    	}
    	
    	public void MergeBlocks(ArrayList<InstructionBlock> blocks)
    	{
    		HashSet<InstructionBlock> blocks_to_remove = new HashSet<InstructionBlock>();
    		for(int i = 0; i < blocks.size(); i++)
    		{	
    			InstructionBlock block = blocks.get(i);
    			
    			if(blocks_to_remove.contains(block))
    			{
    				continue;
    			}
    			
    			switch(block.type)
    			{
    			case INSTRUCTION_CMP:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_CMPI:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_CMPU:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_CMPIU:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_NOP:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_CALL:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_BRANCH:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_LDR:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_STR:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_JMP:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_JSR:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_JMPR:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_TRAP:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_RET:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_ADD:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_SUB:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_MUL:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_DIV:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_ADDIMM:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_AND:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_NOT:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_OR:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_XOR:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_ANDIMM:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_CONST:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_HICONST:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_SLL:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_SRA:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_SRL:
    			{
    				
    			}
    			break;
    			case INSTRUCTION_MOD:
    			{
    				
    			}
    			break;
    			}
    		}
    		blocks.remove(blocks_to_remove);
    	}
    	
    	public String DecompileBody(Block block)
    	{    		
    		String body = new String();
    		
    		InstructionBlock prev = null;
    		
    		ArrayList<InstructionBlock> blocks = new ArrayList<InstructionBlock>();
    		
    		for(int i = 0; i < block.addresses.size(); i++)
    		{
    			int addr = block.addresses.get(i).address;
	        	
    			/*
    			InstructionBlock instruction_block = new InstructionBlock(block, addr);
    			
    			//link each other
    			instruction_block.prev = prev;
    			if(prev != null)
    			{
    				prev.next = instruction_block;
    			}
    			prev = instruction_block;
    			
    			blocks.add(instruction_block);
    			*/
    			
    			//get instruction
	        	
    			Word instruction = mac.getMemory().checkAndReadNoException(addr);
				
				InstructionDef instructionDef = ISA.lookupTable[instruction.getValue()];
				
				//get instruction string
    			String instructionString = instructionDef.disassemble(instruction, addr, mac);
    			
    			String label_string = mac.getSymTable().lookupAddr(addr);
    			
    			if(label_string != null)
    			{
    				indent_spaces--;
    				body += label_string;
    				indent_spaces++;
    			}
    			
    			//
    			//parse instruction
    			//
    			
    			String Rd;
    			String Rs;
    			String Rt;
    			String Imm;
    			
    			if(instructionDef.isAdd())
        		{   				
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Rs = "R" + instructionDef.getSReg(instruction);
        			Rt = "R" + instructionDef.getTReg(instruction);
        			
        			body += Write(Rd + " = " + Rs + " + " + Rt);
        		}
        		else if(instructionDef.isMul())
        		{    				
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Rs = "R" + instructionDef.getSReg(instruction);
        			Rt = "R" + instructionDef.getTReg(instruction);
        			
        			body += Write(Rd + " = " + Rs + " * " + Rt);
        		}
        		else if(instructionDef.isSub())
        		{        			
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Rs = "R" + instructionDef.getSReg(instruction);
        			Rt = "R" + instructionDef.getTReg(instruction);
        			
        			body = Rd + " = " + Rs + Rt;
        		}
        		else if(instructionDef.isDiv())
        		{    				
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Rs = "R" + instructionDef.getSReg(instruction);
        			Rt = "R" + instructionDef.getTReg(instruction);
        			
        			body += Write(Rd + " = " + Rs + " / " + Rt);
        		}
        		else if(instructionDef.isAddIMM())
        		{        			
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Rs = "R" + instructionDef.getSReg(instruction);
        			Imm = "" + instructionDef.getSignedImmed(instruction);
        			
        			body += Write(Rd + " = " + Rs + " + " + Imm);
        		}
        		else if(instructionDef.isCMP())
        		{
        			Rs = "R" + instructionDef.getSReg(instruction);
        			Rt = "R" + instructionDef.getTReg(instruction);
        			
        			//assume next instruction is branch
    	        	Word branch_instruction = mac.getMemory().checkAndReadNoException(++addr);
    				
    				InstructionDef branch_instruction_def = ISA.lookupTable[branch_instruction.getValue()];
        			
        			if(branch_instruction_def.isBranch())
        			{
        				int branch_bits = branch_instruction.getZext(11, 9);
        				switch (branch_bits)
        				{
        				case 0x4:
        				{
                			body += Write("if (" + Rs + " < " + Rt + ")");
                			body += Write("{");
        				}
        				break;
        				case 0x6:
        				{
                			body += Write("if (" + Rs + " <= " + Rt + ")");
                			body += Write("{");
        				}
        				break;
        				case 0x5:
        				{
                			body += Write("if (" + Rs + " != " + Rt + ")");
                			body += Write("{");
        				}
        				break;
        				case 0x2:
        				{
                			body += Write("if (" + Rs + " == " + Rt + ")");
                			body += Write("{");
        				}
        				break;
        				case 0x3:
        				{
                			body += Write("if (" + Rs + " >= " + Rt + ")");
                			body += Write("{");
        				}
        				break;
        				case 0x1:
        				{
                			body += Write("if (" + Rs + " > " + Rt + ")");
                			body += Write("{");
        				}
        				break;
        				default:
        				{
                			body += Write("if (" + Rs + " = " + Rt + ")");
                			body += Write("{");
        				}
        				break;
        				}
        			}
        			
        			if(block.next_blocks.size() == 1)
        			{
            			//branched statement
        				indent_spaces++;
            			body += DecompileBody(block.next_blocks.get(0));
        				indent_spaces--;
           			}
        			if(block.next_blocks.size() == 2)
        			{
            			//branched statement
        				indent_spaces++;
            			body += DecompileBody(block.next_blocks.get(0));
        				indent_spaces--;
        				
            			body += Write("}");
            			body += Write("else");
            			body += Write("{");
        								
        				//non branched statement
        				indent_spaces++;
        				body += DecompileBody(block.next_blocks.get(1));
        				indent_spaces--;
           			}
        			body += Write("}");
        		}
        		else if(instructionDef.isCMPU())
        		{

        			
        		}
        		else if(instructionDef.isCMPI())
        		{

        		}
        		else if(instructionDef.isCMPIU())
        		{

        		}
        		else if(instructionDef.isBranch())
        		{
        			//assume next instruction is branch
    	        	Word prev_instruction = mac.getMemory().checkAndReadNoException(addr - 1);
    				
    				InstructionDef prev_instruction_def = ISA.lookupTable[prev_instruction.getValue()];
        			
    				boolean valid_prev_instruction = false;
    				
    				if(prev_instruction_def.isAdd())
    				{
    					valid_prev_instruction = true;
    				}
    				else if(prev_instruction_def.isMul())
    				{
    					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isSub())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isDiv())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isAddIMM())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isAnd())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isOr())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isXor())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isAndIMM())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isLdr())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isHiConst())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isConst())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isSLL())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isSRA())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isSRL())
    				{
     					valid_prev_instruction = true;
    				}
     				else if(prev_instruction_def.isMod())
    				{
     					valid_prev_instruction = true;
    				}
    				
    				if(valid_prev_instruction)
    				{
    					Rd = "R" + prev_instruction_def.getDReg(prev_instruction);
        				
            			if(instructionDef.isBranch())
            			{
            				int branch_bits = instruction.getZext(11, 9);
            				switch (branch_bits)
            				{
            				case 0x4:
            				{
                    			body += Write("if (" + Rd + " < 0)");
                    			body += Write("{");
            				}
            				break;
            				case 0x6:
            				{
                    			body += Write("if (" + Rd + " <= 0)");
                    			body += Write("{");
            				}
            				break;
            				case 0x5:
            				{
                    			body += Write("if (" + Rd + " != 0)");
                    			body += Write("{");
            				}
            				break;
            				case 0x2:
            				{
                    			body += Write("if (" + Rd + " == 0)");
                    			body += Write("{");
            				}
            				break;
            				case 0x3:
            				{
                    			body += Write("if (" + Rd + " >= 0)");
                    			body += Write("{");
            				}
            				break;
            				case 0x1:
            				{
                    			body += Write("if (" + Rd + " > 0)");
                    			body += Write("{");
            				}
            				break;
            				default:
            				{
                    			body += Write("if (" + Rd + " = 0)");
                    			body += Write("{");
            				}
            				break;
            				}
            			}
            			
            			if(block.next_blocks.size() == 1)
            			{
                			//branched statement
            				indent_spaces++;
                			body += DecompileBody(block.next_blocks.get(0));
            				indent_spaces--;
               			}
            			if(block.next_blocks.size() == 2)
            			{
                			//branched statement
            				indent_spaces++;
                			body += DecompileBody(block.next_blocks.get(0));
            				indent_spaces--;
            				
                			body += Write("}");
                			body += Write("else");
                			body += Write("{");
            								
            				//non branched statement
            				indent_spaces++;
            				body += DecompileBody(block.next_blocks.get(1));
            				indent_spaces--;
               			}	
            			body += Write("}");
    				}
        		}
        		else if(instructionDef.isAnd())
        		{        			
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Rs = "R" + instructionDef.getSReg(instruction);
        			Rt = "R" + instructionDef.getTReg(instruction);
        			
        			body += Write(Rd + " = " + Rs + " & " + Rt);
        		}
        		else if(instructionDef.isNot())
        		{        			
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Rs = "R" + instructionDef.getSReg(instruction);
        			
        			body += Write(Rd + " = ~" + Rs);
        		}
        		else if(instructionDef.isOr())
        		{        			
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Rs = "R" + instructionDef.getSReg(instruction);
        			Rt = "R" + instructionDef.getTReg(instruction);
        			
        			body += Write(Rd + " = " + Rs + " | " + Rt);
        		}
        		else if(instructionDef.isXor())
        		{        			
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Rs = "R" + instructionDef.getSReg(instruction);
        			Rt = "R" + instructionDef.getTReg(instruction);
        			
        			body += Write(Rd + " = " + Rs + " ^ " + Rt);
        		}
        		else if(instructionDef.isAndIMM())
        		{    				
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Rs = "R" + instructionDef.getSReg(instruction);
        			Imm = Word.toHex(instructionDef.getSignedImmed(instruction), true);
        			
        			body += Write(Rd + " = " + Rs + " & " + Imm);
        		}
        		else if(instructionDef.isSLL())
        		{        			
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Rs = "R" + instructionDef.getSReg(instruction);
        			Imm = "" + instructionDef.getUnsignedImmed(instruction);
        			
        			body += Write(Rd + " = " + Rs + " << " + Imm);
        		}
        		else if(instructionDef.isSRA())
        		{        			
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Rs = "R" + instructionDef.getSReg(instruction);
        			Imm = "" + instructionDef.getUnsignedImmed(instruction);
        			
        			body += Write(Rd + " = " + Rs + " >> " + Imm + " | (" + Rs + " & 0x8000)");
        		}
        		else if(instructionDef.isSRL())
        		{        			
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Rs = "R" + instructionDef.getSReg(instruction);
        			Imm = "" + instructionDef.getUnsignedImmed(instruction);
        			
        			body += Write(Rd + " = " + Rs + " >> " + Imm);
        		}
        		else if(instructionDef.isMod())
        		{        			
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Rs = "R" + instructionDef.getSReg(instruction);
        			Rt = "R" + instructionDef.getTReg(instruction);
        			
        			body += Write(Rd + " = " + Rs + " % " + Rt);
        		}
        		else if(instructionDef.isLoad())
        		{        			
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Rs = "R" + instructionDef.getSReg(instruction);
        			Imm = "" + instructionDef.getSignedImmed(instruction);
        			
        			body += Write(Rs + "[" + Imm + "] = " + Rd);
        		}
        		else if(instructionDef.isStore())
        		{    				
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Rs = "R" + instructionDef.getSReg(instruction);
        			Imm = "" + instructionDef.getSignedImmed(instruction);
        			
        			body += Write(Rd + " = " + Rs + "[" + Imm + "]");
        		}
        		else if(instructionDef.isConst())
        		{    				
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Imm = Word.toHex(instructionDef.getSignedImmed(instruction), true);
        			
        			body += Write(Rd + " = " + Imm);
        		}
        		else if(instructionDef.isConstIMM())
        		{
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Imm = Word.toHex(instructionDef.getSignedImmed(instruction), true);
        			
        			body += Write(Rd + " = " + Imm);
        		}
        		else if(instructionDef.isHiConst())
        		{        			
        			Rd = "R" + instructionDef.getDReg(instruction);
        			Imm = Word.toHex(instructionDef.getUnsignedImmed(instruction), true);
        			
        			body += Write(Rd + " = " + "( " + Rd + " & 0x00FF ) | ( " + Imm + " << 8 )");
        		}
    			else
    			{
    				body += Write(instructionString);
    			}
				
    		}
    		
    		//combine blocks that are similar
    		//MergeBlocks(blocks);
			
    		return body;
    	}
    	
    	public JScrollPane DecompileToCCode()
    	{
    		JTextPane c_code_pane = new JTextPane();
    		
    		JScrollPane c_code_scroll_pane = new JScrollPane(c_code_pane);
    		
    		String return_type = "int";
    		
    		String signature = mac.getSymTable().lookupAddr(block.GetStartAddress()) + "()\n";
    		
    		indent_spaces++;
    		
    		String body = Write("__asm {");
    		
    		indent_spaces++;
    		
    		body += DecompileBody(block);
    		
    		indent_spaces--;
    		
    		body += Write("};");
    		
    		indent_spaces--;
    		
    		c_code = return_type + " " + signature + "{\n" + body + "}";
    		
    		c_code_pane.setText(c_code);
    		
    		return c_code_scroll_pane;
    	}
    }
    
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    
	void init(Machine otherMac)
	{
		//keep a pointer to machine
		this.mac = otherMac;
	
		/**
		 * Main split panels
		 */
		
		//Set properties of this JFrame
		this.setMinimumSize(new Dimension(800, 600));
		this.setVisible(true);
		this.setLayout(new BorderLayout());
		
		//Function panel contains all the subroutines and other items
		this.mainLeftPanel = new JPanel(new BorderLayout());
		this.mainLeftPanel.setVisible(true);
		
		//Have the right panel hold the graph component and tabbing panel
		this.mainRightPanel = new JPanel(new BorderLayout());
		this.mainRightPanel.setVisible(true);
		
        /** ---------------------------
         * Add tabbed panes here
         *  --------------------------- */
		
		//Tabbing window that keeps track of subroutines clicked
		//JTabbedPane tabbedPane = new JTabbedPane();
		JPanel tabbedPane = new JPanel(new BorderLayout());
		tabbedPane.setVisible(true);
		
		BlockAnalyzer block_analyzer = new BlockAnalyzer();
		ClosableTabbedPane panes = block_analyzer.GetPanes();
		tabbedPane.add(panes, BorderLayout.CENTER);
		
		
		//Attach to right panel
		this.mainRightPanel.add(tabbedPane, BorderLayout.CENTER);
		
		//Create main split panel that contains left/right windows
		this.mainSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainLeftPanel, mainRightPanel);
		this.mainSplitPanel.setPreferredSize(new Dimension(800, 600));
		this.mainSplitPanel.setVisible(true);
		this.mainSplitPanel.setOneTouchExpandable(true);
		this.mainSplitPanel.setDividerLocation(150);
		
		this.add(mainSplitPanel, BorderLayout.CENTER);
	}
	
	//http://stackoverflow.com/questions/42487675/formatting-jgraphx-edges
	public class ExtendedCompactTreeLayout extends mxCompactTreeLayout
    {

        public ExtendedCompactTreeLayout(mxGraph graph) {
            super(graph, false);
            super.prefVertEdgeOff = 0;
        }

        @Override
        public void execute(Object parent)
        {
            // Execute the CompactTreeLayout
            super.execute(parent);

            // Modify the edges to ensure they exit the source cell at the midpoint
            if(!horizontal)
            {
                // get all the vertexes
                Object[] vertexes = mxGraphModel.getChildVertices(graph.getModel(), graph.getDefaultParent());
                for(int i=0; i < vertexes.length; i++)
                {
                    mxICell parentCell = ((mxICell)(vertexes[i]));
                    // For each edge of the vertex
                    for(int j=0; j < parentCell.getEdgeCount(); j++)
                    {
                    	mxICell edge = parentCell.getEdgeAt(j);
                    	
                    	// Only consider edges that are from the cell
                    	if(edge.getTerminal(true) != parentCell)
                    	{
                    		continue;
                    	}
                    	mxRectangle parentBounds = getVertexBounds(parentCell);
                    	List<mxPoint> edgePoints = edge.getGeometry().getPoints();

                    	// Need to check that there is always 3 points to an edge, but this will get you started
                    	if(edgePoints != null)
                    	{
                    		//System.out.println(edgePoints.size());
                    		mxPoint outPort = edgePoints.get(0);
                    		mxPoint elbowPoint = edgePoints.get(1);
                    		mxPoint outerElbowPoint = edgePoints.get(2);
                    		if(outPort.getX() != parentBounds.getCenterX())
                    		{
                    			outPort.setX(parentBounds.getCenterX());
                    			elbowPoint.setX(parentBounds.getCenterX());
                    		}
                    	}
                   }
               }
           }
        }
    }   
	
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
    
}
