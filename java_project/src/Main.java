import java.io.IOException;
import java.util.Random;

public class Main {

	final static String LOCAL_TEST_NO = "01";
	
    public static void main(String[] args) throws IOException {
    	
    	String dataLocation = "";
    	
    	// RUN LOCAL (no arguments given)
    	if (args.length == 0) {
    		args = new String[]{"basic.utt", "courses.utt", "lecturers.utt", "rooms.utt", "curricula.utt", "relation.utt", "unavailability.utt"};
    		dataLocation = "./TestDataUTT/Test" + LOCAL_TEST_NO + "/";
    	}
    	
        Problem problemInstance = new Problem(dataLocation, args);
        //System.out.println(problemInstance.toString());
        
        Solution SolutionInstance = new Solution(problemInstance);
        for (int i = 0; i < 10000; i++) {
        	SolutionInstance.addRandomLecture();
        }
        //System.out.println(SolutionInstance);
        System.out.println(SolutionInstance.codeJudgeOutput());
        
        //System.out.println("Great Success!");
    	
    }

}