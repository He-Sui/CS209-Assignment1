import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class OnlineCoursesAnalyzer {

    List<Course> courses = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
                        Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
                        Integer.parseInt(info[9]), Integer.parseInt(info[10]), Double.parseDouble(info[11]),
                        Double.parseDouble(info[12]), Double.parseDouble(info[13]), Double.parseDouble(info[14]),
                        Double.parseDouble(info[15]), Double.parseDouble(info[16]), Double.parseDouble(info[17]),
                        Double.parseDouble(info[18]), Double.parseDouble(info[19]), Double.parseDouble(info[20]),
                        Double.parseDouble(info[20]), Double.parseDouble(info[21]));
                courses.add(course);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Map<String, Integer> getPtcpCountByInst() {
        return courses.stream().collect(Collectors.groupingBy(Course::getInstitution, Collectors.summingInt(Course::getParticipants))).entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        return courses.stream()
                .collect(Collectors.groupingBy(x -> x.getInstitution().concat("-").concat(x.getSubject()), Collectors.summingInt(Course::getParticipants))).entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        return courses.stream()
                .flatMap(course -> course.getInstructors().stream().map(instructor -> new AbstractMap.SimpleEntry<>(instructor, course)))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            List<String> list0 = entry.getValue().stream()
                                    .filter(course -> course.getInstructors().size() == 1)
                                    .map(Course::getTitle)
                                    .distinct()
                                    .sorted()
                                    .collect(Collectors.toList());
                            List<String> list1 = entry.getValue().stream()
                                    .filter(course -> course.getInstructors().size() > 1)
                                    .map(Course::getTitle)
                                    .distinct()
                                    .sorted()
                                    .collect(Collectors.toList());
                            return Arrays.asList(list0, list1);
                        }
                ));
    }

    public List<String> getCourses(int topK, String by) {
        if (by.equals("hours")) {
            return courses.stream()
                    .sorted(Comparator.comparing(Course::getTotalHours).reversed().thenComparing(Course::getTitle))
                    .map(Course::getTitle)
                    .distinct()
                    .limit(topK)
                    .collect(Collectors.toCollection(ArrayList::new));
        } else if (by.equals("participants")) {
            return courses.stream()
                    .sorted(Comparator.comparingInt(Course::getParticipants).reversed().thenComparing(Course::getTitle))
                    .map(Course::getTitle)
                    .distinct()
                    .limit(topK)
                    .collect(Collectors.toCollection(ArrayList::new));
        } else
            return null;
    }

    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
        return null;
    }

    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        return null;
    }
}