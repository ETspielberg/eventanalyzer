package unidue.ub.batch.eventanalyzer;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import unidue.ub.media.monographs.Expression;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.settings.fachref.Stockcontrol;

import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class ExpressionReader implements ItemReader<Expression> {

    private boolean noExpressionsFound;

    private Enumeration<Expression> expressionEnumeration;

    private ManifestationReader manifestationReader;
    private Stockcontrol stockcontrol;

    ExpressionReader() {
        noExpressionsFound = true;
    }

    public ExpressionReader(ManifestationReader manifestationReader) {
        noExpressionsFound = true;
        this.manifestationReader = manifestationReader;
    }

    @Override
    public Expression read() throws Exception {
        if (noExpressionsFound) {
            collectManifestation();
        }
        try {
            if (expressionEnumeration.hasMoreElements())
                return expressionEnumeration.nextElement();
            else return null;
        } catch (Exception e) {
            return null;
        }
    }

    private void collectManifestation() throws URISyntaxException {
        Hashtable<String, Expression> expressionData = new Hashtable<>();
        manifestationReader.setStockcontrol(stockcontrol);
        manifestationReader.collectManifestation();
        List<Manifestation> manifestations = manifestationReader.getManifestations();
        if (manifestations.size() != 0) {
            for (Manifestation manifestation : manifestations) {
                if (expressionData.containsKey(manifestation.getShelfmarkBase())) {
                    expressionData.get(manifestation.getShelfmarkBase()).addManifestation(manifestation);
                } else {
                    Expression expression = new Expression(manifestation.getShelfmarkBase());
                    expression.addManifestation(manifestation);
                    expressionData.put(expression.getShelfmarkBase(), expression);
                }
            }
            expressionEnumeration = expressionData.elements();
            noExpressionsFound = false;
        }
    }

    @BeforeStep
    public void retrieveStockcontrol(StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        ExecutionContext jobContext = jobExecution.getExecutionContext();
        this.stockcontrol = (Stockcontrol) jobContext.get("stockcontrol");
    }
}
