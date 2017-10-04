package unidue.ub.batch.eventanalyzer;

import org.springframework.batch.item.ItemReader;
import org.springframework.web.client.RestTemplate;
import unidue.ub.media.monographs.Expression;
import unidue.ub.media.monographs.Manifestation;
import unidue.ub.settings.fachref.Stockcontrol;

import java.net.URISyntaxException;
import java.util.*;

public class ExpressionReader implements ItemReader<Expression> {

    private boolean noExpressionsFound;

    private Enumeration<Expression> expressionEnumeration;

    private Stockcontrol stockcontrol;

    private RestTemplate restTemplate;

    private RestTemplate notationTemplate;

    private ManifestationReader manifestationReader;

    ExpressionReader(Stockcontrol stockcontrol) { noExpressionsFound = true;
    this.stockcontrol = stockcontrol;}

    public ExpressionReader(Stockcontrol stockcontrol, ManifestationReader manifestationReader) {
        noExpressionsFound = true;
        this.stockcontrol = stockcontrol;
        this.manifestationReader = manifestationReader;
    }

    @Override
    public Expression read() throws Exception {
        if (noExpressionsFound) {
            collectManifestation();
        }
        if (expressionEnumeration.hasMoreElements())
            return expressionEnumeration.nextElement();
        else return null;
    }

    private void collectManifestation() throws URISyntaxException {
        Hashtable<String,Expression> expressionData = new Hashtable<>();
        manifestationReader.collectManifestation();
        List<Manifestation> manifestations  = manifestationReader.getManifestations();
        if(manifestations.size() == 0)
            return;
        else {
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

    public ExpressionReader setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        return this;
    }

    public ExpressionReader setNotationTemplate(RestTemplate notationTemplate) {
        this.notationTemplate = notationTemplate;
        return this;
    }
}
