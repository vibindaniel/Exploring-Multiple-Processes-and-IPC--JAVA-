import java.io.BufferedReader;
import java.io.File;
import java.util.Scanner;


public class Memory {
	private static int[] add = new int[2000];
	Scanner file = null;
	static BufferedReader line = null;
	
	public static void main(String[] args) {
        Memory mem = new Memory();
        mem.cycle(args);	
	}
	
	public void cycle(String[] args) {
		// TODO Auto-generated method stub
		try{
			//Scans the program in the argument
			file = new Scanner(new File(args[0]));
			int i = 0;
			//initialize memory to 0
			while(i<2000){
				write(i++, "0");
			}
			i = 0;
			
			//Read every line in the program
			while(file.hasNextLine()){
				String next = file.nextLine();
				if(next.isEmpty()) continue;
				
				//Searches for address such as .1000 or .500
				else if(next.matches("(\\.)(\\d+).*")){
					i = Integer.parseInt(next.replaceFirst(".*?(\\d+).*", "$1"));
				} 
				//Searches for values or instructions
				else if(next.matches(".*?(\\d+).*")) {
					write(i++,next);
				} else continue; //If nothing matches
			}
		} catch (Exception e){
			System.out.println(e);
			System.err.println("File not found");
			System.exit(0);
		}
		
		Scanner instruct = new Scanner(System.in);
		//Scans requests in pipe
		while(instruct.hasNextLine()){
			String ins = instruct.nextLine();
			int address;
			//to exit
			if(ins.matches("exit")){
				instruct.close();
				System.exit(-1);
			}
			if (ins.isEmpty()){
				continue;
			}
			//Checks whether to execute read() or write()
            if (ins.matches("(\\d+) (\\d+).*")) {
                Scanner inst = new Scanner(ins);
                address = Integer.valueOf(inst.next());
                write(address, inst.next());
                inst.close();
            } else {
                address = Integer.valueOf(ins);
                System.out.println(read(address));
            }
		}
		instruct.close();
	}
	
	//reads and sends value in the address to the pipe
	public int read(int id) {
		// TODO Auto-generated method stub
		return add[id];
	}
	
	//write the value sent from pipe into the address
	public void write(int id, String value) {
		// TODO Auto-generated method stub
		add[id] = Integer.valueOf(value.replaceFirst(".*?(\\d+).*", "$1"));
	}
}
