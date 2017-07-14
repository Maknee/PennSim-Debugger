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
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.jgraph.JGraph;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

public class LC4Disassembler extends JFrame{

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
				DecodeInstructionIterative(queueBlock.pc, queueBlock.prevBlock, queueBlock.memory, queueBlock.callStack);
			}
			
			LC4Block block = blocks.get(0);
			DrawBlocks(block);
			
			/*
			for(LC4Block block : blocks)
			{
				block.drawCells();
				System.out.println(block);
			}
			*/
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
		
		/*
		//stack overflow version (causes overflow)
		void DecodeInstruction(int pc, LC4Block prevBlock)
		{	
			//Create a block
			LC4Block lc4block = new LC4Block();
			blocks.add(lc4block);
			
			//try to add this block to the prev block (the block that called this block)
			if(prevBlock != null)
				prevBlock.addNextBlock(lc4block);
			
			
			//loop until a breakpoint
			int i = 0;
			//while(!mac.getMemory().isBreakPointSet(pc))
			while(i < 25)
			{	
				//get instruction
	        	Word instruction = mac.getMemory().checkAndReadNoException(pc);
	        	
            	//parse the instruction
            	InstructionDef instructionDef = ISA.lookupTable[instruction.getValue()];
            	            	
            	if(instructionDef.isRet())
            	{
            		//add this last instruction (ret) to the block
            		lc4block.addInstructionBlock(pc, instruction);
            		
            		//Decode the next block (pc = callstack.pop)
            		DecodeInstruction(callStack.pop().getValue(), lc4block);
            		break;
            	}
            	else if(instructionDef.isJump())
            	{
            		//add this last instruction (jmp) to the block
            		lc4block.addInstructionBlock(pc, instruction);
            		
            		//Decode the next block (pc = callstack.pop)
            		DecodeInstruction(pc + 1 + instructionDef.getPCOffset(instruction), lc4block);
            		break;
            	}
            	else if(instructionDef.isJSR())
            	{
            		//add this last instruction (jssr) to the block
            		lc4block.addInstructionBlock(pc, instruction);
            		
            		//push pc + 1 on the stack (return address)
            		callStack.push(new Word(pc + 1));
            		
            		//Decode the next block 
            		DecodeInstruction((pc & 0x8000) | instructionDef.getAbsAligned(instruction) << 4, lc4block);
            		break;
            	}
            	else if(instructionDef.isJSRR())
            	{
            		//add this last instruction (jssr) to the block
            		lc4block.addInstructionBlock(pc, instruction);
            		
            		//push pc + 1 on the stack (return address)
            		callStack.push(new Word(pc + 1));
            		            		
            		//Decode the next block 
            		DecodeInstruction(R7.getValue(), lc4block);
            		break;
            	}
            	else if(instructionDef.isBranch())
            	{
            		//add this last instruction (branch) to the block
            		lc4block.addInstructionBlock(pc, instruction);
            		
            		//Decode the next block (pc = pc + 1)
            		DecodeInstruction(pc + 1, lc4block);
            		
            		//Decode the next block if branching
            		DecodeInstruction(pc + 1 + instructionDef.getPCOffset(instruction), lc4block);
            		break;
            	}
            	else if(instructionDef.isTRAP())
            	{
            		//add this last instruction (jssr) to the block
            		lc4block.addInstructionBlock(pc, instruction);
            		
            		//push pc + 1 on the stack (return address)
            		callStack.push(new Word(pc + 1));
            		            		
            		//Decode the next block (will be in trap vector table)
            		DecodeInstruction(32768 + instruction.getZext(8, 0), lc4block);
            		break;
            	}
            	else if(instructionDef.isCall())
            	{
            		
            	}
            	else
            	{
                	//Check if the instruction sets R7 in anyway (influences jumps)
            		if(instructionDef.isConst())
            		{
            			if(instructionDef.getDReg(instruction) == 7)
            			{
            				int resultValue = instructionDef.getSignedImmed(instruction);
            				R7.setValue(resultValue);
            			}
            		}
            		else if(instructionDef.isConstIMM())
            		{
            			if(instructionDef.getDReg(instruction) == 7)
            			{
            				int resultValue = instructionDef.getSignedImmed(instruction);
            				R7.setValue(resultValue);
            			}
            		}
            		else if(instructionDef.isHiConst())
            		{
            			if(instructionDef.getDReg(instruction) == 7)
            			{
            				int resultValue = R7.getValue() & 0xFF | instructionDef.getUnsignedImmed(instruction) << 8;
            				R7.setValue(resultValue);
            			}
            		}

            		//add the instruction as vertex
            		lc4block.addInstructionBlock(pc, instruction);
            		pc++;
            	}
            	i++;
			}   
		}
		*/

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
	            		//add this last instruction (branch) to the block
	            		lc4block.addInstructionBlock(pc, instruction);
	            		
	            		//add to queue the first if statement
	            		queue.add(new QueueBlock(pc + 1, lc4block, memory, callStack));
	            		
	            		System.out.println("BRANCHING" + Word.toHex(pc, true));
	            		
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

	public LC4Disassembler(Machine mac)
	{
		super();
		
		init(mac);
	}
	
    private void applyStyleToGraph() {
    	
    	
	    // Settings for vertices
	    Map<String, Object> vertex = graph.getStylesheet().getDefaultVertexStyle();
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
	    graph.setStylesheet(style);
	}
	
	void init(Machine otherMac)
	{
		//keep a pointer to machine
		this.mac = otherMac;
	
		//Create graph
		mxGraph graph = new mxGraph();
		this.graph = graph;
		
		//get graphParent to graph
		this.graphParent = this.graph.getDefaultParent();
		graph.getModel().beginUpdate();
		
    	//Start with the first instruction to analyze based on the PC and continue from there...		
		LC4Analyzer analyzer = new LC4Analyzer(this.mac.getRegisterFile().getPC());
		
		applyStyleToGraph();
		try
		{
			
//			Object v1 = graph.insertVertex(graphParent, null, "Hello", 20, 20, 80,
//					30);
//			Object v2 = graph.insertVertex(graphParent, null, "World!", 240, 150,
//					80, 30);
//			Object v3 = graph.insertVertex(graphParent, null, "World!", 240, 150,
//					80, 30);
//			Object v4 = graph.insertVertex(graphParent, null, "World!", 240, 150,
//					80, 30);
//			Object v5 = graph.insertVertex(graphParent, null, "World!", 240, 150,
//					80, 30);
//			Object v6 = graph.insertVertex(graphParent, null, "World!", 240, 150,
//					80, 30);
//			graph.insertEdge(graphParent, null, "Edge", v1, v2);
//			graph.insertEdge(graphParent, null, "Edge", v1, v3);
//			graph.insertEdge(graphParent, null, "Edge", v1, v4);
//			graph.insertEdge(graphParent, null, "Edge", v1, v5);
//			graph.insertEdge(graphParent, null, "Edge", v5, v6);
//			graph.insertEdge(graphParent, null, "Edge", v6, v5);

			mxIGraphLayout layout = new ExtendedCompactTreeLayout(graph);

			layout.execute(this.graphParent);
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		
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
		
		//Tabbing window that keeps track of subroutines clicked
		this.tabPanel = new JPanel(new BorderLayout());
		this.tabPanel.setVisible(true);
		
		//Create subroutine pane
		this.subroutinePane = new ClosableTabbedPane() {
            public boolean tabAboutToClose(int tabIndex) {
                String tab = subroutinePane.getTabTitleAt(tabIndex);
                int choice = JOptionPane.showConfirmDialog(null, 
                   "You are about to close '" + 
                   tab + "'\nDo you want to proceed ?", 
                   "Confirmation Dialog", 
                   JOptionPane.INFORMATION_MESSAGE);
                return choice == 0;
                // if returned false tab
                // closing will be canceled
            }
        };
        this.subroutinePane.add("HI", new JPanel());
		
        //add the pane to the panel
        this.tabPanel.add(subroutinePane, BorderLayout.CENTER);
        
        //Disassembly panel that contains graphs, etc
		this.disassemblyPanel = new JPanel(new BorderLayout());
		this.disassemblyPanel.setVisible(true);
        
		//Graph Component (Similar to JPanel (which is also a component))
		mxGraphComponent graphComponent = new mxGraphComponent(graph);

		//add the graph to disassembly panel
		this.disassemblyPanel.add(graphComponent, BorderLayout.CENTER);
		
		//Attach to right panel
		this.mainRightPanel.add(tabPanel, BorderLayout.NORTH);
		this.mainRightPanel.add(disassemblyPanel, BorderLayout.CENTER);
		
		//Create main split panel that contains left/right windows
		this.mainSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainLeftPanel, mainRightPanel);
		this.mainSplitPanel.setPreferredSize(new Dimension(800, 600));
		this.mainSplitPanel.setVisible(true);
		this.mainSplitPanel.setOneTouchExpandable(true);
		this.mainSplitPanel.setDividerLocation(150);
		
		this.add(mainSplitPanel, "Center");
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
                    		/*
                    		if(edgePoints.size() > 3)
                    		{
                    			//case where edge goes from one to another before it
                        		mxGeometry geometry = new mxGeometry();
                        		List<mxPoint> points = new ArrayList<mxPoint>();
                        		
                        		mxICell sourceCell = edge.getTerminal(true);
                        		mxICell destinationCell = edge.getTerminal(false);
                        		
                        		if(destinationCell.getGeometry().getCenterX() < sourceCell.getGeometry().getCenterX() && 
                        		   destinationCell.getGeometry().getCenterY() < sourceCell.getGeometry().getCenterY())
                        		{
                            		double distanceDiff = sourceCell.getGeometry().getCenterX() - destinationCell.getGeometry().getCenterX();
                        			points.add(new mxPoint(destinationCell.getGeometry().getCenterX() + distanceDiff/2, sourceCell.getGeometry().getCenterY()));
                        			points.add(new mxPoint(destinationCell.getGeometry().getCenterX() + distanceDiff/2, destinationCell.getGeometry().getCenterY()));	
                        		}
                        		else if(destinationCell.getGeometry().getCenterX() > sourceCell.getGeometry().getCenterX() && 
                        		   destinationCell.getGeometry().getCenterY() < sourceCell.getGeometry().getCenterY())
                        		{
                        			double distanceDiff = destinationCell.getGeometry().getCenterX() - sourceCell.getGeometry().getCenterX();
                        			points.add(new mxPoint(destinationCell.getGeometry().getCenterX() - distanceDiff/2, sourceCell.getGeometry().getCenterY()));
                        			points.add(new mxPoint(destinationCell.getGeometry().getCenterX() - distanceDiff/2, destinationCell.getGeometry().getCenterY()));	
                        		}
                        		else if(destinationCell.getGeometry().getCenterX() < sourceCell.getGeometry().getCenterX() && 
                        				destinationCell.getGeometry().getCenterY() > sourceCell.getGeometry().getCenterY())
                        		{
                            		double distanceDiff = sourceCell.getGeometry().getCenterX() - destinationCell.getGeometry().getCenterX();
                        			points.add(new mxPoint(destinationCell.getGeometry().getCenterX() + distanceDiff/2, destinationCell.getGeometry().getCenterY()));	
                        			points.add(new mxPoint(destinationCell.getGeometry().getCenterX() + distanceDiff/2, sourceCell.getGeometry().getCenterY()));
                        		}
                        		else if(destinationCell.getGeometry().getCenterX() > sourceCell.getGeometry().getCenterX() && 
                             		   destinationCell.getGeometry().getCenterY() > sourceCell.getGeometry().getCenterY())
                        		{
                        			double distanceDiff = destinationCell.getGeometry().getCenterX() - sourceCell.getGeometry().getCenterX();
                        			points.add(new mxPoint(destinationCell.getGeometry().getCenterX() - distanceDiff/2, destinationCell.getGeometry().getCenterY()));	
                        			points.add(new mxPoint(destinationCell.getGeometry().getCenterX() - distanceDiff/2, sourceCell.getGeometry().getCenterY()));
                        		}
                        		else
                        		{
                        			if(destinationCell.getGeometry().getCenterY() > sourceCell.getGeometry().getCenterY())
                        			{
                        				
                        			}
                        			else
                        			{
                        				points.add(new mxPoint(destinationCell.getGeometry().getCenterX() - 100, sourceCell.getGeometry().getCenterY()));
                            			points.add(new mxPoint(destinationCell.getGeometry().getCenterX() - 100, destinationCell.getGeometry().getCenterY()));
                        			}
                        		}
                        		geometry.setPoints(points);
                        		edge.setGeometry(geometry);
                    		}
                    		*/
                    	}
                    	else
                    	{
                    		/*
                    		//case where edge goes from one to another before it
                    		mxGeometry geometry = new mxGeometry();
                    		List<mxPoint> points = new ArrayList<mxPoint>();
                    		
                    		mxICell sourceCell = edge.getTerminal(true);
                    		mxICell destinationCell = edge.getTerminal(false);
                    		
                    		if(destinationCell.getGeometry().getCenterX() < sourceCell.getGeometry().getCenterX() && 
                    		   destinationCell.getGeometry().getCenterY() < sourceCell.getGeometry().getCenterY())
                    		{
                        		double distanceDiff = sourceCell.getGeometry().getCenterX() - destinationCell.getGeometry().getCenterX();
                    			points.add(new mxPoint(destinationCell.getGeometry().getCenterX() + distanceDiff/2, sourceCell.getGeometry().getCenterY()));
                    			points.add(new mxPoint(destinationCell.getGeometry().getCenterX() + distanceDiff/2, destinationCell.getGeometry().getCenterY()));	
                    		}
                    		else if(destinationCell.getGeometry().getCenterX() > sourceCell.getGeometry().getCenterX() && 
                    		   destinationCell.getGeometry().getCenterY() < sourceCell.getGeometry().getCenterY())
                    		{
                    			double distanceDiff = destinationCell.getGeometry().getCenterX() - sourceCell.getGeometry().getCenterX();
                    			points.add(new mxPoint(destinationCell.getGeometry().getCenterX() - distanceDiff/2, sourceCell.getGeometry().getCenterY()));
                    			points.add(new mxPoint(destinationCell.getGeometry().getCenterX() - distanceDiff/2, destinationCell.getGeometry().getCenterY()));	
                    		}
                    		else if(destinationCell.getGeometry().getCenterX() < sourceCell.getGeometry().getCenterX() && 
                    				destinationCell.getGeometry().getCenterY() > sourceCell.getGeometry().getCenterY())
                    		{
                        		double distanceDiff = sourceCell.getGeometry().getCenterX() - destinationCell.getGeometry().getCenterX();
                    			points.add(new mxPoint(destinationCell.getGeometry().getCenterX() + distanceDiff/2, destinationCell.getGeometry().getCenterY()));	
                    			points.add(new mxPoint(destinationCell.getGeometry().getCenterX() + distanceDiff/2, sourceCell.getGeometry().getCenterY()));
                    		}
                    		else
                    		{
                    			double distanceDiff = destinationCell.getGeometry().getCenterX() - sourceCell.getGeometry().getCenterX();
                    			points.add(new mxPoint(destinationCell.getGeometry().getCenterX() - distanceDiff/2, destinationCell.getGeometry().getCenterY()));	
                    			points.add(new mxPoint(destinationCell.getGeometry().getCenterX() - distanceDiff/2, sourceCell.getGeometry().getCenterY()));
                    		}
                    		geometry.setPoints(points);
                    		edge.setGeometry(geometry);
                    		*/
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
    
    //https://www.codeproject.com/Articles/18496/JTabbedPane-with-Closing-Tabs
    public class ClosableTabbedPane extends JTabbedPane{
    	private TabCloseUI closeUI = new TabCloseUI(this);
    	
    	public void paint(Graphics g){
    		super.paint(g);
    		closeUI.paint(g);
    	}
    	
    	public void addTab(String title, Component component) {
    		super.addTab(title+"  ", component);
    	}
    	
    	
    	public String getTabTitleAt(int index) {
    		return super.getTitleAt(index).trim();
    	}
    	
    	private class TabCloseUI implements MouseListener, MouseMotionListener {
    		private ClosableTabbedPane  tabbedPane;
    		private int closeX = 0 ,closeY = 0, meX = 0, meY = 0;
    		private int selectedTab;
    		private final int  width = 8, height = 8;
    		private Rectangle rectangle = new Rectangle(0,0,width, height);
    		private TabCloseUI(){}
    		public TabCloseUI(ClosableTabbedPane pane) {
    			
    			tabbedPane = pane;
    			tabbedPane.addMouseMotionListener(this);
    			tabbedPane.addMouseListener(this);
    		}
    		public void mouseEntered(MouseEvent me) {}
    		public void mouseExited(MouseEvent me) {}
    		public void mousePressed(MouseEvent me) {}
    		public void mouseClicked(MouseEvent me) {}
    		public void mouseDragged(MouseEvent me) {}
    		
    		

    		public void mouseReleased(MouseEvent me) {
    			if(closeUnderMouse(me.getX(), me.getY())){
    				boolean isToCloseTab = tabAboutToClose(selectedTab);
    				if (isToCloseTab && selectedTab > -1){			
    					tabbedPane.removeTabAt(selectedTab);
    				}
    				selectedTab = tabbedPane.getSelectedIndex();
    			}
    		}

    		public void mouseMoved(MouseEvent me) {	
    			meX = me.getX();
    			meY = me.getY();			
    			if(mouseOverTab(meX, meY)){
    				controlCursor();
    				tabbedPane.repaint();
    			}
    		}

    		private void controlCursor() {
    			if(tabbedPane.getTabCount()>0)
    				if(closeUnderMouse(meX, meY)){
    					tabbedPane.setCursor(new Cursor(Cursor.HAND_CURSOR));	
    					if(selectedTab > -1)
    						tabbedPane.setToolTipTextAt(selectedTab, "Close " +tabbedPane.getTitleAt(selectedTab));
    				}
    				else{
    					tabbedPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    					if(selectedTab > -1)
    						tabbedPane.setToolTipTextAt(selectedTab,"");
    				}	
    		}

    		private boolean closeUnderMouse(int x, int y) {		
    			rectangle.x = closeX;
    			rectangle.y = closeY;
    			return rectangle.contains(x,y);
    		}

    		public void paint(Graphics g) {
    			
    			int tabCount = tabbedPane.getTabCount();
    			for(int j = 0; j < tabCount; j++)
    				if(tabbedPane.getComponent(j).isShowing()){			
    					int x = tabbedPane.getBoundsAt(j).x + tabbedPane.getBoundsAt(j).width -width-5;
    					int y = tabbedPane.getBoundsAt(j).y +5;	
    					drawClose(g,x,y);
    					break;
    				}
    			if(mouseOverTab(meX, meY)){
    				drawClose(g,closeX,closeY);
    			}
    		}

    		private void drawClose(Graphics g, int x, int y) {
    			if(tabbedPane != null && tabbedPane.getTabCount() > 0){
    				Graphics2D g2 = (Graphics2D)g;				
    				drawColored(g2, isUnderMouse(x,y)? Color.RED : Color.WHITE, x, y);
    			}
    		}

    		private void drawColored(Graphics2D g2, Color color, int x, int y) {
    			g2.setStroke(new BasicStroke(5,BasicStroke.JOIN_ROUND,BasicStroke.CAP_ROUND));
    			g2.setColor(Color.BLACK);
    			g2.drawLine(x, y, x + width, y + height);
    			g2.drawLine(x + width, y, x, y + height);
    			g2.setColor(color);
    			g2.setStroke(new BasicStroke(3, BasicStroke.JOIN_ROUND, BasicStroke.CAP_ROUND));
    			g2.drawLine(x, y, x + width, y + height);
    			g2.drawLine(x + width, y, x, y + height);

    		}

    		private boolean isUnderMouse(int x, int y) {
    			if(Math.abs(x-meX)<width && Math.abs(y-meY)<height )
    				return  true;		
    			return  false;
    		}

    		private boolean mouseOverTab(int x, int y) {
    			int tabCount = tabbedPane.getTabCount();
    			for(int j = 0; j < tabCount; j++)
    				if(tabbedPane.getBoundsAt(j).contains(meX, meY)){
    					selectedTab = j;
    					closeX = tabbedPane.getBoundsAt(j).x + tabbedPane.getBoundsAt(j).width -width-5;
    					closeY = tabbedPane.getBoundsAt(j).y +5;					
    					return true;
    				}
    			return false;
    		}

    	}

    	public boolean tabAboutToClose(int tabIndex) {
    		return true;
    	}

    	
    }

    
}
