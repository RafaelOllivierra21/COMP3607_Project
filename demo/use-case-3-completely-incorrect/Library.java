/**
 * A small library that stores book titles.
 * Use case 3 starting code. It has one mistake on purpose and every attempt
 * in STEPS.md swaps it for a new one, so it never compiles.
 */
public class Library
{
    private String[] books;
    private int count;

    public Library()
    {
        books = new String[10];
        count = 0;
    }

    public void addBook(String title)
    {
        books[count] = title;
        count = count + 1
    }

    public int countBooks()
    {
        return count;
    }

    public boolean hasBook(String title)
    {
        for (int i = 0; i < count; i++)
        {
            if (books[i].equals(title))
            {
                return true;
            }
        }
        return false;
    }
}