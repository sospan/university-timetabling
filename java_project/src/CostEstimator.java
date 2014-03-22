import java.util.Iterator;


public class CostEstimator {
	
	final static int COST_ROOMCAPACITY			= 1;
	final static int COST_ROOMSTABILITY			= 1;
	final static int COST_CURRICULUMCOMPACTNESS	= 2;
	final static int COST_MINIMUMWORKINGDAYS	= 5;
	final static int COST_UNSCHEDULED			= 10;

	private int countTotalPenalties;
	private int countUnscheduled;
	private int countRoomCapacity;
	private int countMinimumWorkingDays;
	private int countRoomStability;
	private int countCurriculumCompactness;
	
	private int[][] coursesDays;				// course + day					-> no. of lectures this day
	private int[] courseDaysBelowMinimum;		// course						-> no. of days below minimum
	private int[][] coursesRooms;				// course + room				-> no. of lectures in room
	private int[] courseNoOfRooms;				// course						-> no. of distinct rooms

	private Problem problem;
	private Solution solution;
	
	public CostEstimator(Problem problem, Solution solution) {
		this.problem = problem;
		this.solution = solution;
		
		this.coursesDays = new int[problem.noOfCourses][problem.noOfDays];
		this.coursesRooms = new int[problem.noOfCourses][problem.noOfRooms];
		this.courseNoOfRooms = new int[problem.noOfCourses];
		
		// Initialize Unscheduled & DaysBelowMinimum
		int initialUnscheduled = 0;
		int initialDaysBelowMinimum = 0;
		this.courseDaysBelowMinimum = new int[problem.noOfCourses];
		Iterator<Integer> courseIDs = problem.courses.keySet().iterator();
		while (courseIDs.hasNext()) {
			int courseID = courseIDs.next();
			Problem.Course course = problem.courses.get(courseID);
			initialUnscheduled += course.noOfLectures;
			this.courseDaysBelowMinimum[courseID] = course.minWorkDays;
			initialDaysBelowMinimum += course.minWorkDays;
		}
		
		// Initialize cost
		this.countCurriculumCompactness = 0;
		this.countRoomCapacity = 0;
		this.countRoomStability = 0;
		this.countUnscheduled = initialUnscheduled;
		this.countMinimumWorkingDays = initialDaysBelowMinimum;
		this.countTotalPenalties = countUnscheduled * COST_UNSCHEDULED + countMinimumWorkingDays * COST_MINIMUMWORKINGDAYS;
	}
	
	public void calcDeltaCost(int room, int day, int period, int courseID) {
		Problem.Course course = this.problem.courses.get(courseID);
		
		// RoomCapacity
		int deltaCountRoomCapacity = Math.max(0, course.noOfStudents - problem.roomCapacity.get(room));
		
		// CurriculumCompactness (Estimate - This is to complex to calculate!!)
		int deltaCountCurriculumCompactness = 1;
		if (isCurriculumCompact(room, day, period, courseID)) {
			deltaCountCurriculumCompactness = 0;
		}
		
		// MinimumWorkingDays
		int deltaCountMinWorkDays = 0;
		if (this.coursesDays[courseID][day] == 0) {
			this.courseDaysBelowMinimum[courseID] += -1;
			if (this.courseDaysBelowMinimum[courseID] >= 0) { // We improved
				deltaCountMinWorkDays = -1;
			}
		}
		this.coursesDays[courseID][day] += 1;
		
		// RoomStability
		int deltaCountRoomStability = 0;
		if (this.coursesRooms[courseID][room] == 0) {
			this.courseNoOfRooms[courseID] += 1;
			if (this.courseNoOfRooms[courseID] > 1) { // We used an extra room
				deltaCountRoomStability = 1;
			}
		}
		this.coursesRooms[courseID][room] += 1;
		
		// Unscheduled
		int deltaCountUnscheduled = -1;
		
		// Total
		int deltaTotalCost = deltaCountCurriculumCompactness * COST_CURRICULUMCOMPACTNESS +
						 deltaCountRoomCapacity * COST_ROOMCAPACITY +
						 deltaCountRoomStability * COST_ROOMSTABILITY +
						 deltaCountMinWorkDays * COST_MINIMUMWORKINGDAYS +
						 deltaCountUnscheduled * COST_UNSCHEDULED;
		
		// Update counts
		this.countUnscheduled += deltaCountUnscheduled;
		this.countRoomCapacity += deltaCountRoomCapacity;
		this.countRoomStability += deltaCountRoomStability;
		this.countMinimumWorkingDays += deltaCountMinWorkDays;
		this.countCurriculumCompactness += deltaCountCurriculumCompactness;
		this.countTotalPenalties += deltaTotalCost;
		
	}
	
	private boolean isCurriculumCompact(int room, int day, int period, int courseID) {
		Iterator<Integer> curricula = problem.courses.get(courseID).curricula.iterator();
		while (curricula.hasNext()) {
			int curriculum = curricula.next();
			for (int adjPeriod : new int[]{period-1, period+1}) {
				if (adjPeriod > 0 && adjPeriod < problem.periodsPerDay) {
					if (this.solution.getCurriculaTimeslots(curriculum, day, adjPeriod)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public String codeJudgeHeader() {
		return
			"UNSCHEDULED " + countUnscheduled + "\n" +
			"ROOMCAPACITY " + countRoomCapacity + "\n" +
			"ROOMSTABILITY " + countRoomStability + "\n" +
			"MINIMUMWORKINGDAYS " + countMinimumWorkingDays + "\n" +
			"CURRICULUMCOMPACTNESS " + countCurriculumCompactness + "\n" +
			"OBJECTIVE " + countTotalPenalties + "\n";
	}
}