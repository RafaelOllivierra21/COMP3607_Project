/**
 * A student with a name and a grade out of 100.
 * Use case 1 (fully correct): this code compiles first time with no errors.
 */
public class Student
{
    private String name;
    private int grade;

    /**
     * Makes a student with a name and a grade.
     *
     * @param name the student's name
     * @param grade the grade out of 100
     */
    public Student(String name, int grade)
    {
        this.name = name;
        this.grade = grade;
    }

    /** @return the student's name */
    public String getName()
    {
        return name;
    }

    /** @return the grade out of 100 */
    public int getGrade()
    {
        return grade;
    }

    /** @return true if the grade is 50 or more */
    public boolean isPassing()
    {
        return grade >= 50;
    }
}