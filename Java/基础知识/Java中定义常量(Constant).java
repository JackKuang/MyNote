/** 
 * Method One 
 */  
interface ConstantInterface {  
    String SUNDAY = "SUNDAY";  
    String MONDAY = "MONDAY";  
    String TUESDAY = "TUESDAY";  
    String WEDNESDAY = "WEDNESDAY";  
    String THURSDAY = "THURSDAY";  
    String FRIDAY = "FRIDAY";  
    String SATURDAY = "SATURDAY";  
}  
/** 
 * Method Two  
 */  
enum ConstantEnum {  
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY  
}  
/** 
 * Method Three 
 */  
class ConstantClassField {  
    public static final String SUNDAY = "SUNDAY";  
    public static final String MONDAY = "MONDAY";  
    public static final String TUESDAY = "TUESDAY";  
    public static final String WEDNESDAY = "WEDNESDAY";  
    public static final String THURSDAY = "THURSDAY";  
    public static final String FRIDAY = "FRIDAY";  
    public static final String SATURDAY = "SATURDAY";  
}  
/** 
 * Method Four 
 * http://www.ibm.com/developerworks/cn/java/l-java-interface/index.html 
 */  
class ConstantClassFunction {  
    private static final String SUNDAY = "SUNDAY";  
    private static final String MONDAY = "MONDAY";  
    private static final String TUESDAY = "TUESDAY";  
    private static final String WEDNESDAY = "WEDNESDAY";  
    private static final String THURSDAY = "THURSDAY";  
    private static final String FRIDAY = "FRIDAY";  
    private static final String SATURDAY = "SATURDAY";  
    public static String getSunday() {  
        return SUNDAY;  
    }  
    public static String getMonday() {  
        return MONDAY;  
    }  
    public static String getTuesday() {  
        return TUESDAY;  
    }  
    public static String getWednesday() {  
        return WEDNESDAY;  
    }  
    public static String getThursday() {  
        return THURSDAY;  
    }  
    public static String getFirday() {  
        return FRIDAY;  
    }  
    public static String getSaturday() {  
        return SATURDAY;  
    }  
}  
public class TestConstant {  
    static final String day = "saturday";  
    public static void main(String[] args) {  
        System.out.println("Is today Saturday?");  
        System.out.println(day.equalsIgnoreCase(ConstantInterface.SATURDAY));  
        System.out.println(day.equalsIgnoreCase(ConstantEnum.SATURDAY.name()));  
        System.out.println(day.equalsIgnoreCase(ConstantClassField.SATURDAY));  
        System.out.println(day.equalsIgnoreCase(ConstantClassFunction  
                .getSaturday()));  
    }  
}  