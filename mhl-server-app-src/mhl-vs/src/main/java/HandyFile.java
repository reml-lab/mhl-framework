import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Erik Risinger on 4/21/19.
 */
public class HandyFile {

    protected String path;
    protected BufferedReader reader;
    protected PrintWriter writer;
    protected ArrayList<String> lines = null;

    public HandyFile(String filePath) {

        this.path = filePath;

        // open and read in file, close reader
        try {
            reader = new BufferedReader(new FileReader(path));
            lines = new ArrayList<>();

            String l;
            while ((l = reader.readLine()) != null) {
                lines.add(l);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void appendLine(String line) {
        lines.add(line);
    }

    public void commit() {

        // open writer to lock file
        try {
            writer = new PrintWriter(path);
            writer.write(toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public void close() {
        if (this.reader != null) {
            try {
                this.reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (this.writer != null) {
            this.writer.close();
        }
    }

    public void commitAndClose() {
        commit();
        close();
    }

    public void discard() {
        this.lines = new ArrayList<>();
    }

    public void discardAndClose() {
        discard();
        close();
    }

    public int lineCount() {
        return this.lines.size();
    }

    public boolean hasLine(String line) {
        if (lines.contains(line)) {
            return true;
        }

        return false;
    }

    public String getLine(int lineNumber) {
        return this.lines.get(lineNumber);
    }

    public boolean removeLine(String lineString) {
        if (lines.contains(lineString)) {
            lines.remove(lineString);
            return true;
        } else {
            return false;
        }
    }

    public List<String> getLines() {
        return this.lines;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (String s : this.lines) {
            builder.append(s + "\n");
        }

        return builder.toString();
    }

    protected void print(String s) {
        System.out.println(s);
    }

    public static void main(String[] args) {

        // tests
        HandyFile file = new HandyFile("./testfile.txt");

        file.print(file.toString());

        file.appendLine("new line 1");
        file.appendLine("new line 2");

        file.print(file.toString());

        file.commitAndClose();

        file = new HandyFile("./testfile.txt");

        file.print(file.toString());
        file.close();
    }
}

