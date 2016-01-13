import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


public class CPU {
	static BufferedReader rec = null;
	static Process proc = null;
	static BufferedWriter send = null;
	private int pc = 0;
	private int ir = 0;
	private int sp = 1000;
	private int x = 0;
	private int y = 0;
	private int timer = 0;
	private int ac=0;
	final static int memBoundary = 1000;
	private boolean userMode = true;
	private boolean exit = false;
	private final int instructionOP[] = {1,2,3,4,5,7,9,20,21,22,23};
	private int time=0;
	
	public static void main(String[] args){
		CPU pr = new CPU();
		pr.loadProgram(args);
		
	}
	
	public void loadProgram(String[] args) {
		// TODO Auto-generated method stub
		try{
			//Creating process to run Memory.java. The argument contains the path to the program
			proc = Runtime.getRuntime().exec("java Memory "+args[0]);
			time = Integer.parseInt(args[1]); //contains the second command line argument which is the timer
			
			rec = new BufferedReader(new InputStreamReader(proc.getInputStream())); //input from pipe
			send = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));	//output to pipe
			this.run();
			} catch(Exception e){
				System.err.println("Error "+e);
			}
	}
	
	public void run() throws IOException{
		while(true){
			cycle();	//keeps executing this method so until exit becomes false.
			if(exit){
				break;
			}
		}
		//deleting of objects since program is completed at this point
		rec.close();
		send.close();	
		System.exit(0);
	}
	
	private void cycle() throws IOException {
		// TODO Auto-generated method stub
		boolean flagOP = false;
		ir = read(this.pc);	//reads the Instruction from PC and stores in IR
		
		//Checks whether the value at PC is an instruction which requires OPCode
		for(int i =0; i <instructionOP.length;i++){
			if(ir == instructionOP[i]){
				flagOP = true; //OPCode is required
			}
		}
		if(flagOP){
			int op = read(++this.pc); //reads the OPcode which is in the next line
			boundaryCheck(op); //checking for Access violations. It'll be checked again.
			processInstruction(ir, op);
		} else {
			processInstruction(ir, 0); //Sending 0 as a dummy since no Opcode is required
		}
		this.pc++; //increments PC after every instruction is executed
	}
	
	// This is the instruction method. It contains all the instructions that the CPU can execute
	private void processInstruction(int instruction, int op) throws IOException{
		switch(instruction){
			case 1: //Loads value into the AC (Load Value)
				this.ac = op;
				break;
					
			case 2:	//Loads value from the address into the AC (Load address)
				boundaryCheck(op);
				this.ac = read(op);
				break;
				
			case 3 :	//Load the value from the address found 
						//in the address into the AC (LoadInd addr)
				boundaryCheck(op);
				boundaryCheck(read(op));
				this.ac = read(read(op));
				break;
				
			case 4 :	//Load the value at (address+X) into AC (LoadIdxX addr)
				boundaryCheck(op + this.x);
				this.ac = read(op + this.x);
				break;
				
			case 5 :	//Load the value at (address + Y) into AC
				boundaryCheck(op + this.y);
				this.ac = read(op + this.y);
				break;
				
			case 6 :	//Load value at (stack pointer +X) into AC
				boundaryCheck(this.sp + this.x);
				this.ac = read(this.sp + this.x);
				break;
				
			case 7 :	//Write the value in AC into the address
				boundaryCheck(op);
				write(op, this.ac);
				break;
				
			case 8 :	//Store a random int between 1-100 into AC
				this.ac = (int) (Math.random()*(100) + 1);
				break;
				
			case 9 :	//If 1, print value of AC as int, If 2, print value of AC as char
				if(op == 1)	System.out.print(ac);
				else if(op==2)	System.out.print((char) ac);
				break;
				
			case 10 :	//Add value in X into AC
				this.ac += this.x;
				break;
				
			case 11 :	//Add value in Y into AC
				this.ac += this.y;
				break;
				
			case 12 :	//Subtract value in X from AC
				this.ac -= this.x;
				break;
				
			case 13 :	//Subtract value in Y from AC
				this.ac -= this.y;
				break;
				
			case 14 :	//Copy value in AC into X
				this.x = this.ac;
				break;
				
			case 15 :	//Copy value in X into AC
				this.ac = this.x;
				break;
				
			case 16 :	//Copy value in AC into Y
				this.y = this.ac;
				break;
				
			case 17 :	//Copy value in Y into AC
				this.ac = this.y;
				break;
				
			case 18 :	//Copy value in AC into SP
				this.sp = this.ac;
				break;
				
			case 19 :	//Copy value in SP into AC
				this.ac = this.sp;
				break;
				
			case 20 :	//Jump to the address
				this.pc = op - 1; //increments after rest of cycle() is executed
				break;
				
			case 21 :	//Jump to the address if AC = 0
				if(this.ac == 0){
					this.pc = op - 1; //increments after rest of cycle() is executed
				}
				break;
				
			case 22 :	//Jump to the address if AC != 0
				if(this.ac != 0){
					this.pc = op - 1; //increments after rest of cycle() is executed
				}
				break;
				
			case 23 :	//Push PC into stack, and jump to the address
				this.sp--;
				write(this.sp, ++this.pc);
				this.pc = op - 1; //increments after rest of cycle() is executed
				break;
				
			case 24 :	//Pop address from stack, and jump to the address
				this.pc = read(this.sp) - 1;
				this.sp++;
				break;
				
			case 25 :	//Increment X by 1
				this.x++;
				break;
				
			case 26 :	//Decrement X by 1
				this.x--;
				break;
				
			case 27 :	//Push value in AC into Stack
				this.sp--;
				write(this.sp, this.ac);
				break;
				
			case 28 :	//Pop from stack into AC
				this.ac = read(this.sp);
				this.sp++;
				break;
				
			case 29 :	//Interrupt code (Set to system Mode)
						//Push registers to stack
						//Set new SP and PC
				if(userMode){
					userMode = false;
					write(1999, this.sp);
					write(1998, ++this.pc);
					write(1997, this.ac);
					write(1996, this.x);
					write(1995, this.y);
					this.sp = 1995;
					this.pc = 1499;	//It increments 1500 since after this method is executed, rest of cycle() is executed 
				}
				break;
				
			case 30 :	//Set to user mode
						//Read registers from stack
				if(sp<2000){
					this.y = read(1995);
					this.x = read(1996);
					this.ac = read(1997);
					this.pc = read(1998) - 1;
					this.sp = read(1999);
					userMode = true;
				}	else{
					System.out.println("Error: System Stack Empty");
					proc.destroy();
					System.exit(1);
					}
				break;
				
			case 50 :	//End of the program
				send.write("exit");	//Memory reads exit and exits itself
				exit = true; //CPU exits as per the code in cycle() method
				break;
				
			default:	//If faulty instruction is sent to the CPU
				System.err.println("Invalid Instruction: PC-" + pc + "\nIR-" + ir);
				System.exit(0);;
				break;
		}
		
		timer++;	//increments Timer after every instruction
		
		//If Timer is reached, set to system mode
		//Copy registers to stack
		//Set new SP and PC (1000)
		if(timer==time){
			if(userMode){
				write(1999, this.sp);
				write(1998, ++this.pc);
				write(1997, this.ac);
				write(1996, this.x);
				write(1995, this.y);
				this.sp = 1995;
				this.pc = 999;	//It increments 1000 since after this method is executed, rest of cycle() is executed
				userMode = false;
				//System.out.println("Timer PC " + read(pc+1));
			}
			timer = 0;
		}
	}
	
	//Checks Memory Access violations if it CPU is in user mode
	private void boundaryCheck(int address){
		if(userMode){
			if(address<0||address>=memBoundary){
				System.err.println("Access Memory Denied");
				System.exit(0);
			}
		}
	}
	
	/*
	 The difference between read and write requests is found by the arguments sent.
	 Read command sends 1 argument
	 Write sends two arguments
	 */
	
	//Sends request to pipe to read address from Memory
	public int read(int address) throws IOException{
		send.write(String.format("%d\n", address));
		send.flush();		
		String value = rec.readLine();
		return Integer.parseInt(value);
	}
	
	//Send request to write the data in Memory
	public void write(int address, int value) throws IOException{
		send.write(String.format("%d %d\n", address, value));
		send.flush();		
	}
}
