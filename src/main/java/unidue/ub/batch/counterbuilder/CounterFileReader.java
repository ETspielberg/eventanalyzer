package unidue.ub.batch.counterbuilder;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CounterFileReader implements ItemReader<String> {


    @Value("#{jobParameters['counter.file.name'] ?: ''}")
    private String filename;

    @Value("${ub.statistics.data.dir}")
    private String dataDir;

    private Boolean collected = false;

    private String type;

    private Integer initalLines;

    private List<String> lines;

    CounterFileReader() {}

    @Override
    public String read() throws IOException {
        if (!collected)
            readLines();

        if (!lines.isEmpty())
            return lines.remove(0);
        return null;
    }

    private void readLines() throws IOException {
        lines = new ArrayList<>();
        File file = new File(dataDir + "/counterfiles/"+ filename);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        bufferedReader.skip(initalLines);
        String readLine;
        int numberOfLines = 0;
        String databaseLine = "";
        while ((readLine = bufferedReader.readLine()) != null ) {
            if (type.equals("database")) {
                databaseLine += "/" + readLine;
                numberOfLines++;
                if  (numberOfLines == 4) {
                    lines.add(databaseLine);
                }
            } else if (!readLine.isEmpty()) {
                lines.add(readLine);
            }
        }
        collected = true;
    }

    @BeforeStep
    public void retrieveTypeAndInitialLines(StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        ExecutionContext jobContext = jobExecution.getExecutionContext();
        this.type = (String) jobContext.get("type");
        this.initalLines = (Integer) jobContext.get("initalLines");
    }
}
