package unidue.ub.batch.counterbuilder;

import org.apache.commons.lang.StringUtils;
import org.hsqldb.lib.StringUtil;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Component
@StepScope
public class TypeAndFormatDeterminerTasklet implements Tasklet{

    @Value("#{jobParameters['counter.file.name'] ?: ''}")
    private String filename;

    private String delimiter;

    @Value("${ub.statistics.data.dir}")
    private String dataDir;

    private Map<Integer, String> fieldMap;

    private Map<Integer, String> datesMap;

    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext)  {
        File file = new File(dataDir + "/counterfiles/"+ filename);
        fieldMap = new HashMap<>();
        datesMap = new HashMap<>();
        String type = "";
        Integer initialLines = 0;

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String readLine;
            while ((readLine = bufferedReader.readLine()) != null) {
                initialLines++;
                if (readLine.contains("platform") && (readLine.contains("issn")||readLine.contains("isbn")|readLine.contains("database"))) {
                    if (readLine.contains("issn"))
                        type = "journal";
                    else if (readLine.contains("isbn"))
                        type = "ebook";
                    else type = "database";
                    prepareMapsFromLine(readLine);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ExecutionContext stepContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
        stepContext.put("fieldMap", fieldMap);
        stepContext.put("datesMap", datesMap);
        stepContext.put("type", type);
        stepContext.put("delimiter", delimiter);
        stepContext.put("initalLines",initialLines);
        return RepeatStatus.FINISHED;
    }

    private void prepareMapsFromLine(String readLine) {
        determineDelimiter(readLine);

        String[] parts = readLine.split(delimiter);
        for (int i = 0; i< parts.length; i++) {
            switch (parts[i]) {
                case "database" : {
                    fieldMap.put(i, "title");
                    break;
                }
                case "online issn": {
                    fieldMap.put(i, "onlineIssn");
                    break;
                }
                case "print issn": {
                    fieldMap.put(i, "printIssn");
                    break;
                }
                case "publisher": {
                    fieldMap.put(i, "publisher");
                    break;
                }
                case "platform": {
                    fieldMap.put(i, "platform");
                    break;
                }
                case "journal": {
                    fieldMap.put(i, "fullName");
                    break;
                }
                case "user activity": {
                    fieldMap.put(i, "activity");
                    break;
                }
                case "": {
                    fieldMap.put(i, "title");
                    break;
                }
                case "book doi": {
                    fieldMap.put(i, "doi");
                    break;
                }
                case "isbn": {
                    fieldMap.put(i, "isbn");
                }
                case "proprietary identifier": {
                    fieldMap.put(i, "proprietary");
                }
                case "journal doi": {
                    fieldMap.put(i, "doi");
                }
                case "reporting period total": break;
                case "reporting period html": break;
                case "reporting period pdf": break;
                default: {
                    String part = parts[i].trim();
                    if (part.contains(" "))
                        part = part.replaceAll(" ", "-");
                    String montRegex = "(?:jan?|feb?|mar?|apr?|may?|jun?|jul?|aug?|sep?|oct?|nov?|dec?)-\\d\\d";
                    if (part.matches(montRegex)){
                        String month = part.substring(0,2);
                        String year = part.substring(4);
                        switch (month) {
                            case "jan": {
                                datesMap.put(i,"01-" + year);
                            }
                            case "feb": {
                                datesMap.put(i,"02-" + year);
                            }
                            case "mar": {
                                datesMap.put(i,"03-" + year);
                            }
                            case "apr": {
                                datesMap.put(i,"04-" + year);
                            }
                            case "may": {
                                datesMap.put(i,"05-" + year);
                            }
                            case "jun": {
                                datesMap.put(i,"06-" + year);
                            }
                            case "jul": {
                                datesMap.put(i,"07-" + year);
                            }
                            case "aug": {
                                datesMap.put(i,"08-" + year);
                            }
                            case "sep": {
                                datesMap.put(i,"09-" + year);
                            }
                            case "oct": {
                                datesMap.put(i,"10-" + year);
                            }
                            case "nov": {
                                datesMap.put(i,"11-" + year);
                            }
                            case "dec": {
                                datesMap.put(i,"12-" + year);
                            }
                        }
                    }
                }

            }
        }
    }

    private void determineDelimiter(String readLine) {
        if (StringUtils.countMatches(readLine,",") > 2)
            this.delimiter = ",";
         else if (StringUtils.countMatches(readLine,";") > 2)
            this.delimiter = ";";
         else
             this.delimiter = "\\t";
    }
}
