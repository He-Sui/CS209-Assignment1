import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class OnlineCoursesAnalyzer {

    private final List<Course> courses = new ArrayList<>();

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
        return courses.stream()
                .filter(x -> x.getSubject().toLowerCase().contains(courseSubject.toLowerCase()))
                .filter(x -> x.getPercentAudited() >= percentAudited)
                .filter(x -> x.getTotalHours() <= totalCourseHours)
                .map(Course::getTitle)
                .distinct()
                .sorted(String::compareTo)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        Map<String, Double> averageAge = courses.stream()
                .collect(Collectors.groupingBy(Course::getNumber, Collectors.averagingDouble(Course::getMedianAge)));
        Map<String, Double> averageMalePercent = courses.stream()
                .collect(Collectors.groupingBy(Course::getNumber, Collectors.averagingDouble(Course::getPercentMale)));
        Map<String, Double> averagePercentDegree = courses.stream()
                .collect(Collectors.groupingBy(Course::getNumber, Collectors.averagingDouble(Course::getPercentDegree)));
        return courses.stream()
                .map(course -> new AbstractMap.SimpleEntry<>(course, Math.pow(age - averageAge.get(course.getNumber()), 2) + Math.pow(gender * 100 - averageMalePercent.get(course.getNumber()), 2) + Math.pow(isBachelorOrHigher * 100 - averagePercentDegree.get(course.getNumber()), 2)))
                .sorted(Map.Entry.comparingByValue())
                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey().getNumber(), e.getValue()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, (v1, v2) -> v1)).entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(10)
                .map(e -> new AbstractMap.SimpleEntry<>(courses.stream()
                        .filter(c -> c.getNumber().equals(e.getKey()))
                        .max(Comparator.comparing(Course::getLaunchDate))
                        .get()
                        .getTitle(), e.getValue())
                )
                .sorted(Map.Entry.<String, Double>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}